# Storage API

This module provides an abstraction layer for various storage operations, enabling interaction with different types
of storage backends through a unified interface. It allows for the reading, writing, listing, and removal of data stored in different locations such as S3 buckets, local file systems, based on URI schemes or patterns.
It exposes storage API used by other modules. 

- Use [StorageService](src/main/java/de/adorsys/datasafe/storage/api/StorageService.java) interface, provided by this module,
if you want to write your own adapter.

## Key Types and Interfaces

#### StorageCheckService
- **Purpose:**  To check if a specified resource exists at a given location.
- **Key Method:**
```java
boolean objectExists(AbsoluteLocation location);
```
#### StorageListService
- **Purpose:**  To list resources at a given location.
- **Key Method:**
```java
Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location);
```
#### StorageReadService
- **Purpose:**  To read data from a specified resource location.
- **Key Method:**
```java
InputStream read(AbsoluteLocation location);
```

#### StorageRemoveService
- **Purpose:**  To remove a specified resource location.
- **Key Method:**
```java
void remove(AbsoluteLocation location);
```
#### StorageWriteService
- **Purpose:**  To write data to a specified resource location
- **Key Method:**
```java
OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback);
```
- **Additional Method:**
```java
default Optional<Integer> flushChunkSize(AbsoluteLocation location) { ... }
```
#### StorageService
- **Purpose:**   Combines all storage operations into a single interface.
- **Implements:**
*   StorageCheckService
*   StorageListService
*   StorageReadService
*   StorageRemoveService
*   StorageWriteService

## Key Classes

#### BaseDelegatingStorage
- **Purpose:**  Abstract base class that delegates storage operations to actual storage implementations.
- **Method:** Implements methods from StorageService and delegates them to an abstract service method.
```java
protected abstract StorageService service(AbsoluteLocation location);
```
#### RegexDelegatingStorage
- **Purpose:**  Delegates storage operations based on regex matching of URIs.

- **Key Fields:** 
```java
private final Map<Pattern, StorageService> storageByPattern;
```
- **Implementation of service method**
```java
protected StorageService service(AbsoluteLocation location) { ... }
```
#### SchemeDelegatingStorage
- **Purpose:**  Delegates storage operations based on URI schemes.

- **Key Fields:**
```java
private final Map<String, StorageService> storageByScheme;
```
- **Implementation of service method**
```java
protected StorageService service(AbsoluteLocation location) { ... }
```
#### UriBasedAuthStorageService
- **Purpose:**  Manages storage connections based on URIs containing credentials, such as S3 URIs.

- **Key Fields:**
```java
private final Map<AccessId, StorageService> clientByItsAccessKey = new ConcurrentHashMap<>();
private final Function<URI, String> bucketExtractor;
private final Function<URI, String> regionExtractor;
private final Function<URI, String> endpointExtractor;
private final Function<AccessId, StorageService> storageServiceBuilder;
```
- **Key Inner Class: AccessId**
```java
private final String accessKey;
private final String secretKey;
private final String region;
private final String bucketName;
private final String endpoint;
private final URI withoutCreds;
private final URI onlyHostPart;
```

#### UserBasedDelegatingStorage
- **Purpose:**  Delegates storage operations based on user-specific bucket mappings.

- **Key Fields:**
```java
private final Map<String, StorageService> clientByBucket = new ConcurrentHashMap<>();
private final List<String> amazonBuckets;
private final Function<String, StorageService> storageServiceBuilder;
```
### URI Routing Flowchart:
This flowchart depicts how storage operations are routed based on URIs and patterns:
![URI Routing flowchart](http://www.plantuml.com/plantuml/dpng/dL1DImCn4BtlhnZtj0N1GtiKQTdMAXHR3D9Z6PFPDfWcaang_VTck-lgKWNn56RcVUIzSM3q7FSckz1McgW8hilHLJdQbCuo7VacorRaWxD53EGl8NzAJzwy0NX7C4L6WHL1OETnIp1PtUU3JBm7fdsXqZMaQtjCn0ullk7JVkNTGQiaYX2jhZGfq9R9LoW9AkUR2ILhkuKtpJiueDSkXixu6UKBMHKwzytio4KO9l79Me0OrZQbSL5rb1Jce2Nr6PKs54vZmY-SH0EtQGKD9E-MhKYVx58d_YljiXwxgAArIuSvMV9Q_l2Jx95Cs_PvUtNjDJsL1YKQKuUjyMV8K-mf6TeYDvJFJonVoIDhPt_bzWhufqQ_Xp-ePE9kkTuiPlFPmxGOP6EoAkxD1m00)
## Conclusion
This module provides a robust and flexible framework for managing data storage in a system with multiple storage backends. It offers several key benefits:
- **Pluggability:**  Easily add new storage implementations without modifying existing code.
- **Testability:** Simplify testing with a clear API and mocking capabilities.
- **Maintainability:** Centralize storage logic and reduce code duplication.