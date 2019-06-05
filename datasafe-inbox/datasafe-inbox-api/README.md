# Datasafe Inbox Service

This module is designed to provide for message exchange with the user.

We use CMS (RFC 5652) and S/MIME (RFC 5751) to envelope message exchanged between users.

Our default implementation uses DFS backend to store document exchanged between user. 
We might also use another type of communication backend like SMTP server to provide for the same functionality.

Key class:
[InboxService](src/main/java/de/adorsys/datasafe/inbox/api/InboxService.java)