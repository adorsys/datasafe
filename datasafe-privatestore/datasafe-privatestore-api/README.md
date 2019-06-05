# Datasafe Private Space

This module is designed to provide access to the user private space. 
All document stored on this space are encrypted with secret keys held by the user.
Key components of API:
- EncryptedResourceResolver, performs encryption of file path
- ListPrivate, lists users' privatespace and provides file paths that have both encrypted and decrypted view.
- ReadFromPrivate,RemoveFromPrivate,WriteToPrivate are relevant actions that can be done in privatespace.

Key class:
[PrivateSpaceService](src/main/java/de/adorsys/datasafe/privatestore/api/PrivateSpaceService.java)