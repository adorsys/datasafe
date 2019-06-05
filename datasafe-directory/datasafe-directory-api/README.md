# Datasafe Directory Service

Module designed to provide lookup information to user and guest.  Use this module to:
- Register and manage the user profile
- Read the user public profile
- Read the user private profile
- Obtain credential to access the user dfs public space, and corresponding bucket access credentials
- Obtain credential to access to use dfs private space, and corresponding bucket access credentials

Directory service can either be implemented using a DFS based or an RDBMS based backend.

Key classes:
- [PublicKeyService](src/main/java/de/adorsys/datasafe/directory/api/profile/keys/PublicKeyService.java)
responsible for getting users' public key.
- [PrivateKeyService](src/main/java/de/adorsys/datasafe/directory/api/profile/keys/PrivateKeyService.java)
responsible for getting user path encryption and document encryption secret keys and private key.
- [ProfileRetrievalService](src/main/java/de/adorsys/datasafe/directory/api/profile/operations/ProfileRetrievalService.java)
responsible for defining user profile - where are his folders, etc.
- [BucketAccessService](src/main/java/de/adorsys/datasafe/directory/api/profile/dfs/BucketAccessService.java)
responsible for providing storage access credentials

![](.images/long-term-view-architecture.JPG)
