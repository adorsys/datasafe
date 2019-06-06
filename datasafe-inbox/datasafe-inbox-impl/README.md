# Datasafe Inbox Service

This module is designed to provide for message exchange with the user.

We use CMS (RFC 5652) and will use S/MIME (RFC 5751) to envelope message exchanged between users.

Our default implementation uses DFS backend to store document exchanged between user. 
We might also use another type of communication backend like SMTP server to provide for the same functionality.

Key class:
[InboxServiceImpl](src/main/java/de/adorsys/datasafe/inbox/impl/InboxServiceImpl.java)

User INBOX location is defined with:
- [UserPublicProfile.inbox](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/types/UserPublicProfile.java) used for sharing
- [UserPrivateProfile.inboxWithWriteAccess](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/types/UserPrivateProfile.java) used for managing inbox by its owner 

Both fields should resolve to same physical folder.

Its value is obtained with:
[ProfileRetrievalService.publicProfile](../../datasafe-directory/datasafe-directory-api/src/main/java/de/adorsys/datasafe/directory/api/profile/operations/ProfileRetrievalService.java)

## Sharing file
![Sharing details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/inbox_write.puml&fmt=svg&vvv=1&sanitize=true)

## Reading shared file
![Reading shared details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/inbox_read.puml&fmt=svg&vvv=1&sanitize=true)