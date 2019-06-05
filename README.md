[![Build Status](https://travis-ci.com/adorsys/datasafe.svg?branch=develop)](https://travis-ci.com/adorsys/datasafe)
[![codecov](https://codecov.io/gh/adorsys/datasafe/branch/develop/graph/badge.svg)](https://codecov.io/gh/adorsys/datasafe)
[![Maintainability](https://api.codeclimate.com/v1/badges/06ae7d4cafc3012cee85/maintainability)](https://codeclimate.com/github/adorsys/datasafe/maintainability)


# General information
Datasafe is a cross-platform library that allows sharing and storing data and documents securely.
This is achieved using **CMS-envelopes** for symmetric and asymmetric encryption. Symmetric encryption is used for private files.
Asymmetric encryption is used for file sharing.

The library is built with the idea to be as configurable as possible - it uses Dagger2 for dependency injection and modular
architecture to combine everything into the business layer, so the user can override any aspect he wants - i.e. to change
encryption algorithm or to turn path encryption off. Each module is as independent as it is possible - to be used separately.

- Each user has private space that can reside on Amazon S3, minio, filesystem or anything else with proper adapter.
In his private space, each document and its path is encrypted.
- For document sharing user has inbox space, that can be accessed from outside. Another user can write the document he
wants to share into users' inbox space using the recipients' public key so that only inbox owner can read it.
- For storage systems that do not support file versioning natively (i.e. minio) this library provides versioning
capability too.

# Project overview
In short, Datasafe [core logic](datasafe-business/src/main/java/de/adorsys/datasafe/business/impl/service/DefaultDatasafeServices.java)
provides these key services:
* [Privatespace service](datasafe-privatestore/datasafe-privatestore-impl/src/main/java/de/adorsys/datasafe/privatestore/impl/PrivateSpaceServiceImpl.java)
that securely stores private files by encrypting them using users' secret key.
* [Inbox service](datasafe-inbox/datasafe-inbox-impl/src/main/java/de/adorsys/datasafe/inbox/impl/InboxServiceImpl.java)
that allows a user to share files with someone so that the only inbox owner can read files that are
shared with him using private key.
* [User profile service](datasafe-directory/datasafe-directory-impl/src/main/java/de/adorsys/datasafe/directory/impl/profile/operations/DFSBasedProfileStorageImpl.java)
that provides user metadata, such as where is user privatespace, his keystore, etc.

These services are automatically built from
[modules](datasafe-business/src/main/java/de/adorsys/datasafe/business/impl)
and the only thing needed from a user is to provide storage adapter - by using
[predefined](datasafe-storage) adapters,
or by implementing his own using
[this interface](datasafe-storage/datasafe-storage-api/src/main/java/de/adorsys/datasafe/storage/api/StorageService.java).

Additionally, for file versioning purposes like reading only last file version, there is [versioned privatespace](datasafe-business/src/main/java/de/adorsys/datasafe/business/impl/service/VersionedDatasafeServices.java)
that supports versioned and encrypted private file storage (for storage providers that do not support versioning).

# How it works
## Storing private files
![How it works diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/valb3r/datasafe/develop/docs/diagrams/high-level/how_it_works.puml&fmt=svg&vvv=1&sanitize=true)
## Sharing files

# Examples of how to use the library
<!--
To update snippets you can use embed.sh
MacOS: Install gnused and gnugrep:
`brew install gnu-sed`
`brew install grep`

Example script usage:
./embed.sh Example README.md > README-tmp.md && mv README-tmp.md README.md
-->

## Generic Datasafe usage
First, you want to create Datasafe services. This snippet provides you Datasafe that uses filesystem storage adapter:
[Example:Create Datasafe services](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L42-L48)
```groovy
// this will create all Datasafe files and user documents under <temp dir path>
defaultDatasafeServices = DaggerDefaultDatasafeServices.builder()
        .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"))
        .storage(new FileSystemStorageService(root))
        .build();
```

Second you want to add new users:
[Example:Create new user](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L56-L63)
```groovy
// Creating new user with username 'user' and private/secret key password 'passwrd':
/*
IMPORTANT: For cases when user profile is stored on S3 based systems, this requires some global
synchronization due to eventual consistency or you need to supply globally unique username on registration
*/
defaultDatasafeServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"));
```

After you have a user, he wants to store some data or document securely in his privatespace:
[Example:Store file in privatespace](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L74-L84)
```groovy
// creating new user
UserIDAuth user = registerUser("john");

// writing string "Hello" to my/own/file.txt:
// note that both resulting file content and its path are encrypted:
try (OutputStream os = defaultDatasafeServices.privateService()
        .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
    os.write("Hello".getBytes(StandardCharsets.UTF_8));
}
```

Now user wants to read again his secured file:
[Example:Read file from privatespace](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L95-L108)
```groovy
// creating new user
UserIDAuth user = registerUser("jane");

// writing string "Hello Jane" to my/secret.txt into users' Jane privatespace:
writeToPrivate(user, "my/secret.txt", "Hello Jane");

byte[] helloJane;
// reading encrypted data from my/secret.txt, note that path is also encrypted
try (InputStream is = defaultDatasafeServices.privateService()
        .read(ReadRequest.forDefaultPrivate(user, "my/secret.txt"))) {
    helloJane = ByteStreams.toByteArray(is);
}
```

But he doesn't remember the name of file he stored, so he will list all files in privatespace and read first:
[Example:Read file from privatespace using list](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L216-L230)
```groovy
// creating new user
UserIDAuth user = registerUser("john");

// let's create 1 file:
writeToPrivate(user, "home/my/secret.txt", "secret");

List<AbsoluteLocation<ResolvedResource>> johnsPrivateFilesInMy = defaultDatasafeServices.privateService()
        .list(ListRequest.forDefaultPrivate(user, "home/my")).collect(Collectors.toList());

// we have successfully read that file
assertThat(defaultDatasafeServices.privateService().read(
        ReadRequest.forPrivate(user, johnsPrivateFilesInMy.get(0).getResource().asPrivate()))
).hasContent("secret");
```

Now he wants to share some data with another user:
[Example:Send file to INBOX](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L120-L130)
```groovy
// create John, so his INBOX does exist
UserIDAuth john = registerUser("john");
UserID johnUsername = new UserID("john");

// We send message "Hello John" to John just by his username
try (OutputStream os = defaultDatasafeServices.inboxService()
        .write(WriteRequest.forDefaultPublic(johnUsername, "hello.txt"))) {
    os.write("Hello John".getBytes(StandardCharsets.UTF_8));
}
```

And finally it is time to read data that was shared with you:
[Example:Read file from INBOX](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithDefaultDatasafe.java#L238-L254)
```groovy
// creating new user
UserIDAuth user = registerUser("john");
UserID johnUsername = new UserID("john");

// let's create 1 file:
shareMessage(johnUsername, "home/my/shared.txt", "shared message");

// Lets list our INBOX
List<AbsoluteLocation<ResolvedResource>> johnsInboxFilesInMy = defaultDatasafeServices.inboxService()
        .list(ListRequest.forDefaultPrivate(user, "")).collect(Collectors.toList());

// we have successfully read that file
assertThat(defaultDatasafeServices.inboxService().read(
        ReadRequest.forPrivate(user, johnsInboxFilesInMy.get(0).getResource().asPrivate()))
).hasContent("shared message");
```

## Datasafe with file versioning
Suppose we need to preserve file history, so accidental file removal won't destroy everything. In such case
we can use storage provider that supports versioning. But if we have storage provider does not support versions
(i.e. minio) we can turn-on software versioning, here is its usage examples;

First, we will obtain versioned Datasafe services that uses filesystem storage adapter:
[Example:Create versioned Datasafe services](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithVersionedDatasafe.java#L45-L51)
```groovy
// this will create all Datasafe files and user documents under <temp dir path>
versionedServices = DaggerVersionedDatasafeServices.builder()
        .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"))
        .storage(new FileSystemStorageService(root))
        .build();
```

Next we will create user, this is same as in non-versioned services:
[Example:Creating user for versioned services looks same](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithVersionedDatasafe.java#L59-L66)
```groovy
// Creating new user:
/*
IMPORTANT: For cases when user profile is stored on S3 based systems, this requires some global
synchronization due to eventual consistency or you need to supply globally unique username on registration
*/
versionedServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"));
```

This is how file versioning works when saving file multiple times:
[Example:Saving file couple of times - versioned](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithVersionedDatasafe.java#L78-L102)
```groovy
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

And we can work with file versions too, of course, everything is encrypted:
[Example:Lets check how to read oldest file version](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithVersionedDatasafe.java#L104-L120)
```groovy
// so lets collect all versions
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

Another important case to mention  is how to determine if file has changed on storage compared to some copy we have:
[Example:Check if we have latest file locally](datasafe-examples/datasafe-examples-business/src/test/java/de/adorsys/datasafe/examples/business/filesystem/BaseUserOperationsTestWithVersionedDatasafe.java#L130-L161)
```groovy
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
// Modified date of saved file has changed and it is newer that our cached date
// So mobile application should download latest file version
assertThat(savedOnPC).isAfter(savedOnMobile);
```

You can visit the **[project homepage](https://adorsys.github.io/datasafe)** for additional information.

# JavaDoc
You can read JavaDoc [here](https://adorsys.github.io/datasafe/javadoc/0.0.9/index.html)

# Contributing
* [CodingRules](docs/codingrules/CodingRules.md)
* [Branching and commiting](docs/branching/branch-and-commit.md)
* [Deployment to maven central](docs/general/deployment_maven_central.md)
