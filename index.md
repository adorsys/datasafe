[![Build Status](https://travis-ci.com/adorsys/datasafe.svg?branch=develop)](https://travis-ci.com/adorsys/datasafe)
[![codecov](https://codecov.io/gh/adorsys/datasafe/branch/develop/graph/badge.svg)](https://codecov.io/gh/adorsys/datasafe)
[![Maintainability](https://api.codeclimate.com/v1/badges/06ae7d4cafc3012cee85/maintainability)](https://codeclimate.com/github/adorsys/datasafe/maintainability)

# Why using Datasafe
Security of data is a major issue that needs to be addressed because companies must comply with an increasing number of 
laws, standards, and codes of conduct relating to information security:
General Data Protection Regulation (GDPR): If a company wants to achieve GDPR conformity, 
it must take data protection measures.
Companies that have critical infrastructures must also catch up IT security according to the IT security law, 
for this they must also guarantee data security.
The loss of sensitive data during hacker attacks, for example, can lead to severe penalties of up to four percent 
of the worldwide annual turnover and can have severe consequences for a company, possibly crippling 
the entire organization.

# Solving security issues with Datasafe
Datasafe is a cross-platform library that allows sharing and storing data and documents securely. 
To achieve this, Datasafe uses concept of users' _private space_ and _inbox_. 

## Features
-  Proprietary software **friendly license**
-  **Flexibility** - you can easily change encryption and configure or customize other aspects of library
-  AES encryption using **CMS-envelopes** for increased security and interoperability with other languages
-  **Extra protection layer** - encryption using securely generated keys that are completely unrelated to your password
-  **Client side encryption** - you own your data
-  Works with filesystem and Amazon S3 compatible storage - S3, minio, CEPH, etc.
-  File names are encrypted
-  Thorough testing

## Quick demo
### Datasafe-CLI

You can try Datasafe as a CLI (command-line-interface) executable for encryption of your own sensitive files. 
They can be saved either in S3 bucket or local filesystem 
(they are currently built from *feature/datasafe-cli-w-s3* branch). 

**Download CLI executable**:

1. [MacOS executable](https://github.com/adorsys/datasafe/releases/download/v0.6.0/datasafe-cli-osx-x64)
1. [Linux executable](https://github.com/adorsys/datasafe/releases/download/v0.6.0/datasafe-cli-linux-x64)
1. Windows executable (N/A yet), please use java version below 
1. [Java-based jar](https://github.com/adorsys/datasafe/releases/download/v0.6.0/datasafe-cli.jar), requires JRE (1.8+), use `java -jar datasafe-cli.jar` to execute

(Files above are built from [feature/datasafe-cli-w-s3](https://github.com/adorsys/datasafe/tree/feature/datasafe-cli-w-s3) currently)

#### Example actions:
1. Download application and create new user:

<details><summary>New profile animation transcript</summary>

- Download CLI application

```bash
curl -l https://github.com/adorsys/datasafe/releases/download/v0.6.0/datasafe-cli-osx-x64 > datasafe-cli && chmod +x datasafe-cli
```
- Create file with your credentials (they also can be passed through commandline)

```bash
echo '{"username": "john", "password": "Doe", "systemPassword": "password"}' > john.credentials
```
- Create your new user profile (credentials come from john.credentials). You can enter value or click enter to accept 
the default value when prompted.

```bash
./datasafe-cli -c john.credentials profile create
```
</details>

![new_profile](https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/demo/new_profile.gif)

2. Encrypt and decrypt some secret data for our user:

<details><summary>Encrypting/decrypting data animation transcript</summary>

- Create some unencrypted content

```bash
echo "Hello world" > unencrypted.txt
```
- Encrypt and store file from above in privatespace. In privatespace it will have decrypted name `secret.txt`
```bash
./datasafe-cli -c john.credentials private cp unencrypted.txt secret.txt
```
- Show that filename is encrypted in privatespace:

```bash
ls private
```

- Show that file content is encrypted too:

```bash
cat private/encrypted_file_name_from_above
```

- Decrypt file content:

```bash
./datasafe-cli -c john.credentials private cat secret.txt
```
</details>

![encrypt_decrypt_file](https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/demo/encrypt_decrypt_file.gif)

3. You can always list available actions in context:

<details><summary>List actions animation transcript</summary>

- Show top-level commands

```bash
./datasafe-cli -c john.credentials
```

- Show commands for privatespace

```bash
./datasafe-cli -c john.credentials private
```
</details>

![list_actions](https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/demo/list_actions.gif)

### REST based demo
[Here](datasafe-rest-impl/DEMO.md) you can find quick docker-based demo that of project capabilities with 
instructions how to use it (REST-api based to show how to deploy as encryption server).

## Key definitions and functionality overview

_Private space_ is the place where users' private files are kept in encrypted form - something like KeePass or
high-level [eCryptfs](http://ecryptfs.org/) for your files but built with Java in a way that you can customize anything. 

_Inbox_ is the place where shared files are stored in, they are also encrypted using users' public key, something like
encrypted file sharing, where only targeted recipients can read data that is shared with them. 

Such functionality is achieved using [**CMS-envelopes**](https://en.wikipedia.org/wiki/Cryptographic_Message_Syntax) 
for symmetric and asymmetric encryption. 
Symmetric encryption is used for private files. Asymmetric encryption is used for file sharing.

The library is built with the idea to be as configurable as possible - it uses Dagger2 for dependency injection and modular
architecture to combine everything into the business layer, so the user can override any aspect he wants - i.e. to change
encryption algorithm or to turn path encryption off. Each module is as independent as it is possible - to be used separately.

- Each users' private space can reside on Amazon S3, minio, filesystem or anything else with proper adapter.
In his private space, each document and its path is encrypted.
- For document sharing user has inbox space, that can be accessed from outside. Another user can write the document he
wants to share into users' inbox space using the recipients' public key so that only inbox owner can read it.
- For storage systems that do not support file versioning natively (i.e. minio) this library provides versioning
capability too.

# How it works

Datasafe functionality can be viewed as virtual filesystem, that has:
- private encrypted user section - **private** folder, where user can list, read, write, delete his own files.
- documents that are shared with user - **inbox** folder, where user can list, read, delete files that were shared
with him and send (write) file to some others' person inbox.
- **profile** section that describes user to the system.

For example:
```
│   
└───private
│   │
│   └─── amazon-S3
│   │    │
│   │    └───bucket1
│   │    │   │     private_file1.txt
│   │    │   │     private_file2.txt
│   │    │
│   │    └───bucket2
│   │       │      private_file1.txt
│   │
│   └───minio-in-datacenter
│       │
│       └───bucket1  
│           │     private_fileA.txt
│
└───inbox
│   │   file021.txt
│   │   file022.txt
│
└───my-profile
│   │
│   └─── public
│   │    │       public.key
│   │
│   └─── private
│        |       private.key
│        │       secret.key
│        │       path-encryption-secret.key
```

## Storing private files
High-level overview of what happens when user shares his file with another user or stores something in private space:
![How privatespace diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/gh-pages/diagrams/how_it_works_private.puml&fmt=svg&vvv=1&sanitize=true)

## Sharing files with another user
High-level overview of what happens when user shares his file with another user using inbox service:
![How inbox diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/gh-pages/diagrams/how_it_works_inbox.puml&fmt=svg&vvv=1&sanitize=true)

# Project overview
In short, Datasafe [core logic](https://github.com/adorsys/datasafe/blob/master/datasafe-business/src/main/java/de/adorsys/datasafe/business/impl/service/DefaultDatasafeServices.java)
provides these key services:
* [Privatespace service](https://github.com/adorsys/datasafe/blob/master/datasafe-privatestore/datasafe-privatestore-impl/src/main/java/de/adorsys/datasafe/privatestore/impl/PrivateSpaceServiceImpl.java)
that securely stores private files by encrypting them using users' secret key.
* [Inbox service](https://github.com/adorsys/datasafe/blob/master/datasafe-inbox/datasafe-inbox-impl/src/main/java/de/adorsys/datasafe/inbox/impl/InboxServiceImpl.java)
that allows a user to share files with someone so that the only inbox owner can read files that are
shared with him using private key.
* [User profile service](https://github.com/adorsys/datasafe/blob/master/datasafe-directory/datasafe-directory-impl/src/main/java/de/adorsys/datasafe/directory/impl/profile/operations/DFSBasedProfileStorageImpl.java)
that provides user metadata, such as where is user privatespace, his keystore, etc.

These services are automatically built from
[modules](https://github.com/adorsys/datasafe/blob/master/datasafe-business/src/main/java/de/adorsys/datasafe/business/impl)
and the only thing needed from a user is to provide storage adapter - by using
[predefined](https://github.com/adorsys/datasafe/blob/master/datasafe-storage) adapters,
or by implementing his own using
[this interface](https://github.com/adorsys/datasafe/blob/master/datasafe-storage/datasafe-storage-api/src/main/java/de/adorsys/datasafe/storage/api/StorageService.java).

Additionally, for file versioning purposes like reading only last file version, there is [versioned privatespace](https://github.com/adorsys/datasafe/blob/master/datasafe-business/src/main/java/de/adorsys/datasafe/business/impl/service/VersionedDatasafeServices.java)
that supports versioned and encrypted private file storage (for storage providers that do not support versioning).

# Storage adapters

Out-of-the box Datasafe supports these kinds of storage systems:
 - [Filesystem storage](https://github.com/adorsys/datasafe/tree/develop/datasafe-storage/datasafe-storage-impl-fs). 
 This storage uses default java.nio interface for filesystem 
 - [S3 compatible storage](https://github.com/adorsys/datasafe/tree/develop/datasafe-storage/datasafe-storage-impl-s3). 
 This storage uses Amazon-SDK client to get access to S3 bucket.
 
Additionally, user can implement his own storage adapter to support i.e. storing data inside RDBMS by implementing
[StorageService](https://github.com/adorsys/datasafe/blob/develop/datasafe-storage/datasafe-storage-api/src/main/java/de/adorsys/datasafe/storage/api/StorageService.java)
interface.
 
# Project overview
* [Achitecture](general/architecture.md)

# JavaDoc
You can read JavaDoc [here](javadoc/latest/index.html)

Command to generate JavaDoc from sources:
```mvn clean javadoc:aggregate -P javadoc ```

# Usage examples

# Modular design overview
* [Modular design](general/modular.md)

# Contributing
* [CodingRules](https://github.com/adorsys/datasafe/blob/develop/docs/codingrules/CodingRules.md)
* [Branching and commiting](https://github.com/adorsys/datasafe/blob/develop/docs/branching/branch-and-commit.md)
* [Deployment to maven central](https://github.com/adorsys/datasafe/blob/develop/docs/general/deployment_maven_central.md)
