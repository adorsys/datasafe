# Metainfo versioning

This module contains software, storage based versioning functionality, suitable to be used with i.e. minio.
File versioning uses following implementation:

![Versioning components](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/datasafe-metainfo/file-versioning.puml&fmt=svg&vvv=1&sanitize=true)

In current implementation actual file is a directory that contains list of its versioned blobs in privatestore.

Links to latest versions of users' files (latest privatestore snapshot) are contained in 
[UserPrivateProfile.documentVersionStorage](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/types/UserPrivateProfile.java)

Its value is obtained with:
[ProfileRetrievalService.privateProfile](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/profile/operations/ProfileRetrievalService.java)

Key class: 
[LatestPrivateSpaceImpl](src/main/java/de/adorsys/datasafe/metainfo/version/impl/version/latest/LatestPrivateSpaceImpl.java)

Version encoding class:
[DefaultVersionEncoderDecoder](src/main/java/de/adorsys/datasafe/metainfo/version/impl/version/latest/DefaultVersionEncoderDecoder.java)
