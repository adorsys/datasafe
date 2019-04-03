# Docusafe2
This is the simplified architecture of **Docusafe2**. 

* It only contains one DFS per user.
* It uses plain inbox folders for users to store documents given from one user to another. The format might be [S/MIME](https://en.wikipedia.org/wiki/S/MIME).
* The inbox folder is on the SYSTEM DFS, not the USERs DFS (explained later).

First all needed services are explained. The term server is not used at all. A server is stand alone application. 
Docusafe2 can be regarded as a server. For this term lookup-server is no more used. 
Its functionality can be found in the [DFSDocusafeService](#DFSDocusafeService). 
Further the old docusafe business layer is no more needed and used. 
Everything is now done in the top service, which is called [DFSDocusafeService](#DFSDocusafeService) here.

Later the functionality of the transactional layer may be put on top of that [DFSDocusafeService](#DFSDocusafeService).

#### DFSDocusafeService ####
<details><summary>DFSDocusafeService - expand for interface</summary>
<p>

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

</p>
</details>

DFS is the abbreviation for _Distritbuted File System_. The [DFSDocusafeService](#DFSDocusafeService) is the only interface given to the public. 
It has all the methods that are known from the old docusafe. Except one additional method: registerDFS. 
An instance to this DFSService basically works like the old Docusafe with creating and storing documents. 
But in general the documents can be on different DFS.

To get its task done, it needs the following subservices:

#### DFSConnectionService ####
<details><summary>DFSConnectionService - expand for interface</summary>
<p>

```
putBlob (documentContent: DocumentContent,bucketPath: BucketPath,): void
getBlob (bucketPath: BucketPath,): DocumentContent
blobExists (bucketPath: BucketPath,): boolean
removeBlob (bucketPath: BucketPath,): void
removeBlobFolder (bucketDirectory: BucketDirectory,): void
list (bucketDirectory: BucketDirectory,listRecursiveFlag: ListRecursiveFlag,): List<BucketPath>
listAllDirectories ( ): List<BucketDirectory>
```

</p>
</details>

This is the service to to store and read documents from the DFS. 
The only change to the old ExtendedStoreConnection is that the encryption of the path is no more part of this service.
It has to be done by the client, e.g. the implementation of the [DFSDocusafeService](#DFSDocusafeService). 
So this service stores and retrieves the data as they are. 
Another difference to the old ExtendedStoreConnection is that MetaInformation is no more supported. 
Only the data can be stored with a name. No further information like size, last changed data are supported.

#### BucketPathEncryptionService ####
<details><summary>BucketPathEncryptionService - expand for interface</summary>
<p>

```
encrypt (secretKey: SecretKey,bucketPath: BucketPath,): BucketPath
decrypt (secretKey: SecretKey,bucketPath: BucketPath,): BucketPath
```

</p>
</details>

The BucketPathEncryptionService has the task to encrypt and decrypt the BucketPath. 
This will be done with a symmetric key.

#### CMSEncryptionService ####
<details><summary>CMSEncryptionService - expand for interface</summary>
<p>

```
encrypt (publicKeyID: KeyID,data: DocumentContent,publicKey: PublicKey,): CMSEnvelopedData
decrypt (encryptedData: CMSEnvelopedData,keyStoreAccess: KeyStoreAccess,): DocumentContent
```

</p>
</details>

This service is encrypting and decrypting binary data. 
The binary data eventually is returned as a CMSEnvelopeData. 
To encrypt, a public key and a KeyID have to be provided. 
For the decryption the keyStoreAccess must be provided, e.g. the KeyStore and the Password to access the private Keys. 

#### DFSCredentialService ####
<details><summary>DFSCredentialService - expand for interface</summary>
<p>

```
getDFSCredentials (userIDAuth: UserIDAuth,): DFSCredentials
registerDFS (dfsCredentials: DFSCredentials,userIDAuth: UserIDAuth,): void
```

</p>
</details>

This service task is to store and return the DFSCredentials. They are used to access the "real" data of the user. 
Keep in mind the DFS Service itself uses a DFS. This will be the [DFSDocusafeService](#DFSDocusafeService) DFS all the time.

#### KeyStoreService ####
<details><summary>KeyStoreService - expand for interface</summary>
<p>

```
createKeyStore (keyStoreAuth: KeyStoreAuth,keyStoreType: KeyStoreType,config: KeyStoreCreationConfig,): KeyStore
getPublicKeys (keyStoreAccess: KeyStoreAccess,): List<PublicKeyIDWithPublicKey>
getRandomSecretKeyID (keyStoreAccess: KeyStoreAccess,): SecretKeyIDWithKey
getSecretKey (keyID: KeyID,keyStoreAccess: KeyStoreAccess,): SecretKey
getPrivateKey (keyID: KeyID,userIDAuth: UserIDAuth,): PrivateKey
```

</p>
</details>

The KeyStoreService task is to create a KeyStore and provide the client with the keys of the KeyStore. 
The KeyStore is the **[Bouncy Castle](https://www.bouncycastle.org/)** class. 
To retrieve and store the KeyStore to a DFS is task of the client.


## So how does it work?

The [DFSDocusafeService](#DFSDocusafeService) needs a supplied DFS right in the beginning. 
We call this DFS the SYSTEM DFS. Here each user gets its own user space. 
It will contain a keystore, an inbox folder, the users public keys and the users DFSCredentials (all explained later). 
Further, to store the users data, another DFSConnection Instance is used. We will call this the USERS DFS. 
Keep in mind, that those users, that do not have their own DFS will use ths SYSTEM DFS. 
But this is not visible for the [DFSDocusafeService](#DFSDocusafeService). 
The [DFSDocusafeService](#DFSDocusafeService) all the time deals with two DFS, the SYSTEM DFS and the USERs DFS. 

The USERs DFS contains another keystore and the data the user wants to store. 
So the user has one keystore in the SYSTEM DFS (we call it a public user keystore) and one keystore in the USER DFS 
(we call it the private user keystore).
Both keystores are protected by the same user password. Further are they both stored unencrypted. 
Their protection is given by the bouncy castle keystore class itself. 
The public keystore is called public because it is stored on the SYSTEM DFS.

The following picture should give a rough idea, which elements are stored  on which DFS.