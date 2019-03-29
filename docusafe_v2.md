# Docusafe2 virtual filesystem analogy
Docusafe2 can be viewed as virtual filesystem, that has:
- private encrypted user section - **private** folder 
- documents that are shared with user - **inbox** folder
- folder where user can share documents with other users - **share**. 

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
└───share
    │
    └─── my_friend
         │       shared_file1.txt
```

# Lookup server API's
Primary purpose of each lookup server api element is to answer **where** 
(i.e. in what *s3 bucket* and using which *credentials*) **storage service** should look and decrypt files.

### Operations with private files

Full CRUD on private file section

* [List files / get file content](docs/api/private/get.md) : `GET /api/mount/private/<path to folder or file>`
* [Add some private file](docs/api/private/put.md) : `PUT /api/mount/private/<path>`
* [Remove private file](docs/api/private/delete.md) : `DELETE /api/mount/private/<path to folder or file>`

### Operations with inbox

List-Read-Delete operations

* [List files / get file content](docs/api/inbox/get.md) : `GET /api/mount/inbox/<filename or empty>`
* [Remove file from inbox](docs/api/inbox/delete.md) : `DELETE /api/mount/inbox/<filename>`

### File sharing operations

User-list and write-only operations

* [List users who we can share with](docs/api/share/get.md) : `GET /api/mount/share/`
* [Share file (snapshot) with user](docs/api/share/put.md) : `PUT /api/mount/share/<username>/<filename>`

# Top level application architecture for server based application
![Top level architecture](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/top_level.puml&fmt=png&vvv=5)

# General view of lookup server operations
![Lookup server general view](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/generic_view.puml&fmt=png&vvv=9)

# Sequence diagram for CRUD on private files
![Private files CRUD](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/sequence_private.puml&fmt=png&vvv=3)

# Sequence diagram for INBOX file sharing
![INBOX file sharing](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/sequence_put_inbox.puml&fmt=png&vvv=1)

# Sequence diagram for INBOX reading
![INBOX reading](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/sequence_read_inbox.puml&fmt=png&vvv=1)