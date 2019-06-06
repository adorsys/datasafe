# Datasafe privatespace default implementation
This is the default implementation of datasafe privatespace that encrypts both document and its path. 
Document is encrypted with symmetric encryption using users' document secret key.
Path is encrypted in the way it is possible to perform document traversal using encrypted path segments, so that:
path `A/B/C` will transform into `encrypted(A)/encrypted(B)/encrypted(C)`. Each path encrypted path segment 
has same position in path as its unencrypted version (another example `a/a/c -> aslkj/aslkj/ygjj`). 
Bytes of encrypted path segment are translated into strings using urlsafe Base64 encoding.

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