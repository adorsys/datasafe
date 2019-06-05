# Datasafe Directory Service

This module uses user-provided storage adapter to store users' profiles and keystore.

Module designed to provide lookup information to user and guest.  Use this module to:
- Register and manage the user profile
- Read the user public profile
- Read the user private profile
- Obtain credential to access the user dfs public space, and corresponding bucket access credentials
- Obtain credential to access to use dfs private space, and corresponding bucket access credentials

Directory service can either be implemented using a DFS based or an RDBMS based backend.

**NOTE**: Users' profile and keystore are cached to avoid unnecessary trip to storage, these things are expected to 
change infrequently. 

**BUT** in this implementation creating and removing user should not reuse username (when storage is S3 without locking):
- if user 'Joe' was created - code that does this should be globally synchronized - no other thread/instance should
create same user

**And for all storages**
- if user 'Joe' was removed - backing 
[KeyStoreCache](src/main/java/de/adorsys/datasafe/directory/impl/profile/keys/KeyStoreCache.java)
and 
[UserProfileCache](src/main/java/de/adorsys/datasafe/directory/impl/profile/operations/UserProfileCache.java)
should be cleared from his entries

So, ideally, when using storages without object locks one should always generate unique username, 
to overcome these issues.