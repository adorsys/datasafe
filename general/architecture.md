## Library modules
![Modules map](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/modules_map.puml&fmt=svg&vvv=1&sanitize=true)

## Users' files - where are they?

Whenever user wants to store or read file at some location - be it inbox or his private space, following things do happen:
1. System resolves his profile location
1. His profile is read from some storage (and typically cached, then direct cache access happens)
1. Based on his profile content, root folder where data should be read/written is deduced
1. If data is going to private space - request path is encrypted
1. Root path is prepended to request path
1. Encryption/decryption of data happens
1. Credentials required to access the storage are added ([BucketAccessService](https://github.com/adorsys/datasafe/tree/master/datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/profile/dfs/BucketAccessService.java))
1. Data stream with path is sent to storage adapter
1. Optionally, storage adapter analyzes based on protocol which storage service to use
1. Storage adapter stores the data

This diagram shows path resolution flow for private space with more details. It is mostly same both for private and
inbox files, with the only difference that private files have relative path (relative to private space location)
additionally encrypted.

![Path resolution](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/profiles/locate_profile.puml&fmt=svg&vvv=1&sanitize=true)

## Storing private files

Private files are always encrypted using users' secret symmetric key. Additionally their path is encrypted too, but
this encryption is very special in the sense that it has form of a/b/c encrypted as
encrypted(a)/encrypted(b)/encrypted(c), so that folder traversal operations are efficient.

![How privatespace diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works_private.puml&fmt=svg&vvv=1&sanitize=true)

### Writing files to privatespace

![Write private](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works_private_write_modules.puml&fmt=svg&vvv=1&sanitize=true)

### Reading files from privatespace

![Read private](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works_private_read_modules.puml&fmt=svg&vvv=1&sanitize=true)

[More details](https://github.com/adorsys/datasafe/tree/master/datasafe-privatestore)

## Sharing files with another user

Shared files are protected using asymmetrical cryptography, so that sender encrypts file with recipients' public key
and only recipient can read it using his private key. Paths are kept unencrypted for inbox.

![How inbox diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works_inbox.puml&fmt=svg&vvv=1&sanitize=true)

### Writing files to inbox

![Write inbox](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works_inbox_write_modules.puml&fmt=svg&vvv=1&sanitize=true)

### Reading files from inbox

[Read inbox](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works_inbox_read_modules.puml&fmt=svg&vvv=1&sanitize=true)

[More details](https://github.com/adorsys/datasafe/tree/master/datasafe-inbox)
