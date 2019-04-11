# Datasafe2 virtual filesystem analogy
Datasafe2 can be viewed as virtual filesystem, that has:
- private encrypted user section - **private** folder 
- documents that are shared with user - **inbox** folder
- folder where user can share documents with other users (recipients) - **send_to**. 

For example:
```
│   
└───private
│   │
│   └─── s3
│   │    │
│   │    └───bucket1
│   │    │   │     private_file1.txt
│   │    │   │     private_file2.txt
│   │    │
│   │    └───bucket2
│   │       │      private_file1.txt
│   │
│   └───minio
│       │
│       └───bucket1  
│           │     private_fileA.txt
│   
└───inbox
│   │   file021.txt
│   │   file022.txt
│   
└───send_to
│   │
│   └───joe
│       │ shared_file1.txt
│
└───whitelist
│   │       friend-of-mine.pub-key
│
└───public-keys
    │         jane-doe.pub-key
```

# Directory service REST API / library exposed interfaces
Directory service is responsible for user creation/data sharing/shared location management. It consists of 
*Lookup service* and *Registration service*  

### Registration sub-service API
* [Create new VFS (register user)](docs/api/users/put.md) : `PUT /api/users`
* [Update user details (i.e. change key pair)](docs/api/users/post.md) : `POST /api/users/<id>`
* [Delete VFS (de-register user)](docs/api/users/delete.md) : `DELETE /api/users/<id>`

### Lookup sub-service API
Lookup service is the replacement and extension of Datasafe 1.0 
[UserIDUtil](https://github.com/adorsys/docusafe/blob/master/docusafe-business/src/main/java/org/adorsys/docusafe/business/utils/UserIDUtil.java) class. 
UserIDUtil to Lookup service sequence diagram mapping can be found [**here**](../docu1_vs_docu2/useridutil_2_lookup.md).

* [Get **private** storage access details](docs/api/lookup/private/get.md) : `GET /api/lookup/private`
* [Get **inbox** storage access details](docs/api/lookup/inbox/get.md) : `GET /api/lookup/inbox`
* [List available recipients](docs/api/lookup/send_to/get.md) : `GET /api/lookup/send_to`
* [Get recipient inbox access details](docs/api/lookup/send_to/get_recipient.md) : `GET /api/lookup/send_to/<recipient_id>`

Primary purpose of each lookup server api element is to answer **where** 
(i.e. in what *s3 bucket* and using which *credentials*) **storage service** should look and decrypt files.

### User sub-service API

CRUD-like on private file section

* [List files](../api/private/get.md) : `GET /api/mount/private`
* [Get file content](docs/api/private/get_file.md) : `GET /api/mount/private/<path to folder or file>`
* [Add some private file](../api/private/put.md) : `PUT /api/mount/private/<path>`
* [Remove private file](../api/private/delete.md) : `DELETE /api/mount/private/<path to folder or file>`

### Operations with inbox API

List-Read-Delete operations

* [List files](../api/inbox/get.md) : `GET /api/mount/inbox`
* [Get file content](../api/inbox/get_file.md) : `GET /api/mount/inbox/<filename>`
* [Remove file from inbox](../api/inbox/delete.md) : `DELETE /api/mount/inbox/<filename>`

### File sharing operations API

User-list and write-only operations

* [List users who we can share with](../api/share/get.md) : `GET /api/mount/share/`
* [Share file (snapshot) with user](../api/share/put.md) : `PUT /api/mount/share/<username>/<filename>`

# Top level application architecture for server based application
Datasafe2 supports command chain request delegation using routers for each API (within request context), so that we can do `REST->library->REST->library` chains for data retrieval.
![Top level architecture](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/top_level.puml&fmt=png&vvv=9)

# General view of lookup server operations
![Lookup server general view](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/generic_view.puml&fmt=png&vvv=9)

# Sequence diagrams for VFS/user creation
* [VFS and user creation](docusafe_diagrams.md)

# Sequence diagram for CRUD on private files
### Detailed view
![Private files CRUD](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/sequence_private.puml&fmt=png&vvv=3)

# Sequence diagram for INBOX file sharing
![INBOX file sharing](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/sequence_put_inbox.puml&fmt=png&vvv=1)

# Sequence diagram for INBOX reading
![INBOX reading](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/sequence_read_inbox.puml&fmt=png&vvv=1)