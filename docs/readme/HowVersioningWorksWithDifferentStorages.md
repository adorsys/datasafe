# Versioning in Datasafe

## Introduction
Datasafe provides comprehensive versioning capabilities for files stored across various storage systems, including local filesystems, Minio, and Amazon S3. This feature allows users to track changes over time, facilitating file recovery, auditing, and collaboration, even in storage systems that do not natively support versioning.

## Overview
Datasafe supports a variety of storage systems, including:

### Supported Storage Systems
- Filesystem storage adapter
- Minio storage adapter
- Amazon S3

## How It Works

### Storage-Specific Versioning
1. **Versioning with Filesystem Storage Adapter**
    - When using the filesystem storage adapter:
        - File versions are stored in a directory structure. Each version is saved as a separate file.
        - The latest version is linked to the original file path.
        - **Example**:
            - File: `example.txt` is stored in the home directory.
            - When a new version is uploaded, Datasafe creates a new file `example.txt.v1` in the same directory.
            - The latest version remains accessible via the original path `home/example.txt`.

2. **Versioning with Minio Storage Adapter**
    - For Minio:
        - Datasafe leverages Minio's built-in versioning capabilities.
        - File versions are stored in a separate bucket, and Datasafe manages these versions through the Minio API.
        - **Example**:
            - File: `example.txt` is stored in the home bucket.
            - Uploading a new version results in Minio creating a new version of the file within the bucket.

3. **Versioning with Amazon S3**
    - For storage systems with Amazon S3:
        - Datasafe implements software-based versioning, saving file versions in a separate storage system.
        - **Example**:
            - File: `example.txt` is stored in the home directory.
            - A new version upload leads to Datasafe creating `example.txt.v1` in a separate storage system.

## Implementation

### Datasafe Versioning APIs
Datasafe offers several APIs for managing file versions:
- `VersionedPrivateSpaceService`: Methods for listing, reading, and writing file versions.
- `VersionInfoService`: Retrieves file version information.
- `EncryptedLatestLinkService`: Provides the latest version of a file.

### Datasafe Versioning Configuration
Versioning can be configured using the `DatasafeConfig` class, which offers methods to set properties such as the storage system to use and the versioning strategy. To see the Datasafe class locally, navigate to `DatasafeConfig` class locally and to get the class on the repository, navigate to the `DatasafeConfig` class on the repository.

### Example: Create Versioned Datasafe Services
```java
// this will create all datasafe files and user documents under <temp dir path>
versionedServices = DaggerVersionedDatasafeServices.builder()
        .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
        .storage(new FileSystemStorageService(root))
        .build();
```
### Example: Creating User for Versioned Services

```java
// Creating new user:
/*
IMPORTANT: For cases when user profile is stored on S3 without object locks,
this requires some global synchronization due to eventual consistency or you need to supply globally unique username on registration
*/
versionedServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"::toCharArray));

```
## Usage
### Example: Saving File Couple of Times - Versioned
This is how file versioning works when saving a file multiple times:

```java
    // creating new user
UserIDAuth user = registerUser("john");

// writing string "Hello " + index to my/own/file.txt 3 times:
// note that both resulting file content and its path are encrypted:
for (int i = 1; i <= 3; ++i) {
        try (OutputStream os = versionedServices.latestPrivate()
        .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
        os.write(("Hello " + i).getBytes(StandardCharsets.UTF_8));
        Thread.sleep(1000L); // this will change file modified dates
    }
            }

// and still we read only latest file
assertThat(versionedServices.latestPrivate()
        .read(ReadRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasContent("Hello 3");

// but there are 3 versions of file stored physically in users' privatespace:
assertThat(versionedServices.privateService().list(
        ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasSize(3);

// and still only one file visible on latest view
assertThat(versionedServices.latestPrivate().list(ListRequest.forDefaultPrivate(user, ""))).hasSize(1);

```

### Example: Let's Check How to Read Oldest File Version
We can work with file versions too; of course, everything is encrypted:

```java
    // so let's collect all versions
List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> withVersions =
        versionedServices.versionInfo().versionsOf(
                ListRequest.forDefaultPrivate(user, "my/own/file.txt")
        ).collect(Collectors.toList());

// so that we can find oldest
Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion> oldest =
        withVersions.stream()
                .sorted(Comparator.comparing(it -> it.absolute().getResource().getModifiedAt()))
                .collect(Collectors.toList())
                .get(0);

// and read oldest content
assertThat(versionedServices.privateService()
    .read(ReadRequest.forPrivate(user, oldest.absolute().getResource().asPrivate()))
        ).hasContent("Hello 1");

```

### Example: Check if We Have Latest File Locally
Another important case to mention is how to determine if a file has changed on storage compared to some copy we have:

```java
    // creating new user
UserIDAuth user = registerUser("john");

// First lets store some file, for example John stored it from mobile phone
try (OutputStream os = versionedServices.latestPrivate()
        .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
        os.write(("Hello old version").getBytes(StandardCharsets.UTF_8));
        }

// Application on mobile phone caches file content to improve performance, so it should cache timestamp too
Instant savedOnMobile = versionedServices.latestPrivate()
        .list(ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        .findAny().get().getResource().getModifiedAt();

// Now John uses PC to write data to my/own/file.txt with some updated data
Thread.sleep(1000L); // it took some time for him to get to PC
try (OutputStream os = versionedServices.latestPrivate()
        .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
        os.write(("Hello new version").getBytes(StandardCharsets.UTF_8));
        }

// John takes his mobile phone and application checks if it needs to sync content
Instant savedOnPC = versionedServices.latestPrivate()
        .list(ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        .findAny().get().getResource().getModifiedAt();

// This indicates that we need to update our cache on mobile phone
// Modified date of saved file has changed and it is newer than our cached date
// So mobile application should download the latest file version
assertThat(savedOnPC).isAfter(savedOnMobile);

```

### Example: Versioned Storage Support - Writing File and Reading Back
If you have storage for user files on a versioned S3 bucket and want to get object version when you write an object or to read some older version encrypted object, you can follow this example:

```java
// creating new user
UserIDAuth user = registerUser("john");

// writing data to my/own/file.txt 3 times with different content:
// 1st time, writing into my/own/file.txt:
// Expanded snippet of how to capture file version when writing object:
AtomicReference<String> version = new AtomicReference<>();
try (OutputStream os = defaultDatasafeServices.privateService()
        .write(WriteRequest.forDefaultPrivate(user, MY_OWN_FILE_TXT)
                .toBuilder()
                .callback((PhysicalVersionCallback) version::set)
                .build())
) {
    // Initial version will contain "Hello 1":
    os.write("Hello 1".getBytes(StandardCharsets.UTF_8));
}
// this variable has our initial file version:
String version1 = version.get();

// Write 2 more times different data to the same file - my/own/file.txt:
String version2 = writeToPrivate(user, MY_OWN_FILE_TXT, "Hello 2");
// Last version will contain "Hello 3":
String version3 = writeToPrivate(user, MY_OWN_FILE_TXT, "Hello 3");

// now, when we read a file without specifying version - we see latest file content:
assertThat(defaultDatasafeServices.privateService().read(
        ReadRequest.forDefaultPrivate(user, MY_OWN_FILE_TXT))
).hasContent("Hello 3");

```

