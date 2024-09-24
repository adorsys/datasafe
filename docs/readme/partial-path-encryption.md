### Datasafe Partial Path Encryption 

### Overview
Partial path encryption allows encrypting only specific parts of a file path while keeping other parts unencrypted. This feature is useful when you want to maintain some readable structure in your storage while still protecting sensitive information.

#### How It Works
1. The `PathEncryptionImplOverridden` class extends `PathEncryptionImpl` to provide custom encryption logic.
2. In the `encrypt` method:
    - If the path contains a "/", it splits the path into two parts: the root (first segment) and the rest.
    - The root remains unencrypted, while the rest is encrypted using the superclass method.
3. In the `decryptor` method:
    - It follows a similar pattern, keeping the root unencrypted and decrypting the rest.

### Implementation
```java
class PathEncryptionImplOverridden extends PathEncryptionImpl {
    PathEncryptionImplOverridden(PathEncryptionImplRuntimeDelegatable.ArgumentsCaptor captor) {
        super(captor.getSymmetricPathEncryptionService(), captor.getPrivateKeyService());
    }

    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        if (path.asString().contains("/")) {
            String[] rootAndInRoot = path.asString().split("/", 2);
            return new Uri(rootAndInRoot + "/" + super.encrypt(forUser, new Uri(rootAndInRoot[1])).asString());
        }
        return path;
    }

    @Override
    public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
        return rootWithEncrypted -> {
            if (rootWithEncrypted.asString().contains("/")) {
                String[] rootAndInRoot = rootWithEncrypted.asString().split("/", 2);
                return new Uri(rootAndInRoot + "/" + super.decryptor(forUser).apply(new Uri(rootAndInRoot[1])).asString());
            }
            return rootWithEncrypted;
        };
    }
}
```
### Usage
- To use partial path encryption
Create an OverridesRegistry and override the PathEncryptionImpl:
java
```java
OverridesRegistry registry = new BaseOverridesRegistry();
PathEncryptionImplRuntimeDelegatable.overrideWith(registry, PathEncryptionImplOverridden::new);
```


Build the Datasafe service with the custom registry:
```java
DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
.config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
.storage(new FileSystemStorageService(root))
.overridesRegistry(registry)
.build();
```

- Use the service as usual. Paths like "folder/file.txt" will be partially encrypted:
```text
"folder" remains unencrypted
"file.txt" gets encrypted
```

- Example
```java
UserIDAuth user = new UserIDAuth("user", "passwrd"::toCharArray);
datasafeServices.userProfile().registerUsingDefaults(user);
datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(user, "folder/file.txt"));

// The folder name "folder" will be visible in the file system
assertThat(Files.walk(root)).asString().contains("folder");
// But "file.txt" will be encrypted
assertThat(Files.walk(root)).asString().doesNotContain("file.txt");
```
