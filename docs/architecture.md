# Docusafe2
This is the simplified architecture of **Docusafe2**. 

* It only contains one DFS per user.
* It uses plain inbox folders for users to store documents given from one user to another. The format might be [S/MIME](https://en.wikipedia.org/wiki/S/MIME).
* The inbox folder is on the SYSTEM DFS, not the USERs DFS (explained later).

First all needed services are explained. The term server is not used at all. A server is stand alone application. 
Docusafe2 can be regarded as a server. For this term lookup-server is no more used. 
Its functionality can be found in the **DFSCredentialService**. 
Further the old docusafe business layer is no more needed and used. 
Everything is now done in the top service, which is called **DFSDocusafeService** here.

Later the functionality of the transactional layer may be put on top of that **DFSDocusafeService**.

<details><summary>DFSDocusafeService</summary>
```
registerDFS (dfsCredentials: DFSCredentials,userIDAuth: UserIDAuth,): void
storeDocument (dsDocument: DSDocument,userIDAuth: UserIDAuth,): void
readDocument (userIDAuth: UserIDAuth,documentFQN: DocumentFQN,): DSDocument
deleteDocument (userIDAuth: UserIDAuth,documentFQN: DocumentFQN,): void
documentExists (userIDAuth: UserIDAuth,documentFQN: DocumentFQN,): boolean
deleteFolder (documentDirectoryFQN: DocumentDirectoryFQN,userIDAuth: UserIDAuth,): void
list (documentDirectoryFQN: DocumentDirectoryFQN,userIDAuth: UserIDAuth,recursiveFlag: ListRecursiveFlag,): List<DocumentFQN>
listInbox (userIDAuth: UserIDAuth,): List<DocumentFQN>
writeDocumentToInboxOfUser (document: DSDocument,receiverUserID: UserID,destDocumentFQN: DocumentFQN,): void
readDocumentFromInbox (source: DocumentFQN,userIDAuth: UserIDAuth,): DSDocument
deleteDocumentFromInbox (userIDAuth: UserIDAuth,documentFQN: DocumentFQN,): void
createUser (userIDAuth: UserIDAuth,): void
destroyUser (userIDAuth: UserIDAuth,): void
userExists (userID: UserID,): boolean
```
</details>

DFS is the abrevation for Distritbuted File System. The DFSDocusafeService is the only interface given to the public. 
It has all the methods that are known from the old docusafe. Except one additional method: registerDFS. 
An instance to this DFSSrevice basically works like the old docusafe with creating and storing dorcumets. 
But in general the documents can be on different DFS.

To get its task done, it needs the following subservices: