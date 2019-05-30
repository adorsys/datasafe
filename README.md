[![Build Status](https://travis-ci.com/adorsys/datasafe.svg?branch=develop)](https://travis-ci.com/adorsys/datasafe)
[![codecov](https://codecov.io/gh/adorsys/datasafe/branch/develop/graph/badge.svg)](https://codecov.io/gh/adorsys/datasafe)
[![Maintainability](https://api.codeclimate.com/v1/badges/06ae7d4cafc3012cee85/maintainability)](https://codeclimate.com/github/adorsys/datasafe/maintainability)


# General information
Datasafe is a cross-platform library that allows sharing and storing data and documents securely. 
This is achieved using **CMS-envelopes** for symmetric and asymmetric encryption. Symmetric encryption is used for private files. 
 Asymmetric encryption is used file sharing. 

The library is built with the idea to be as configurable as possible - it uses Dagger2 for dependency injection and modular 
architecture to combine everything into the business layer, so the user can override any aspect he wants - i.e. to change 
encryption algorithm or to turn path encryption off. Each module is as independent as it is possible - to be used separately.

- Each user has private space that can reside on Amazon S3, minio, filesystem or anything else with proper adapter. 
In his private space, each document and its path is encrypted. 
- For document sharing user has inbox space, that can be accessed from outside. Another user can write the document he
 wants to share into users' inbox space using the recipients' public key, so that only inbox owner can read it.
- For storage systems that do not support file versioning natively (i.e. minio) this library provides versioning 
capability too.

# Project overview
In short, Datasafe [core logic](datasafe-business/src/main/java/de/adorsys/datasafe/business/impl/service/DefaultDatasafeServices.java)
provides these key services:
* [Privatespace service](datasafe-privatestore/datasafe-privatestore-impl/src/main/java/de/adorsys/datasafe/privatestore/impl/PrivateSpaceServiceImpl.java) 
that securely stores private files by encrypting them using users' secret key.
* [Inbox service](datasafe-inbox/datasafe-inbox-impl/src/main/java/de/adorsys/datasafe/inbox/impl/InboxServiceImpl.java) 
that allows a user to share files with someone so that the only inbox owner can read files that that are
shared with him using  private key. 
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



You can visit the **[project homepage](https://adorsys.github.io/datasafe)** for additional information.

# JavaDoc
You can read JavaDoc [here](https://adorsys.github.io/datasafe/javadoc/0.0.9/index.html)

# Contributing
* [CodingRules](docs/codingrules/CodingRules.md)
* [Branching and commiting](docs/branching/branch-and-commit.md)
* [Deployment to maven central](docs/general/deployment_maven_central.md)
