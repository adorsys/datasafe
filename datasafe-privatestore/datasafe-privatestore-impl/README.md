# Datasafe privatespace default implementation
This is the default implementation of datasafe privatespace that encrypts both document and its path. Path is encrypted
in the way it is possible to perform document traversal using encrypted path segments, so that:
path A/B/C will transform into encrypted(A)/encrypted(B)/encrypted(C).

User Privatespace location is defined with:
[UserPrivateProfile.privateStorage](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/types/UserPrivateProfile.java)

Its value is obtained with:
[ProfileRetrievalService.privateProfile](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/profile/operations/ProfileRetrievalService.java)

Key class:
[PrivateSpaceServiceImpl](src/main/java/de/adorsys/datasafe/privatestore/impl/PrivateSpaceServiceImpl.java)

## Writing private file
![Writing details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/private_write.puml&fmt=svg&vvv=1&sanitize=true)

## Reading private file
![Reading details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/private_read.puml&fmt=svg&vvv=1&sanitize=true)