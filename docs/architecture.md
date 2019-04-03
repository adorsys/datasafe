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
This service is also known as Lookup/Directory service.
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

![Components diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/architecture.puml&fmt=png&vvv=1)

As soon as a __**new user**__ is to be created, the following actions have to be done:

- Create a new keystore for the user. This will be the public keystore as it will be stored in the SYSTEM DFS. 
  The password to read the keys is the users password. This keystore has one public/private key pair and no other keys. 
  The public key is used to encrypt the DFS Credentials. The private key is used to decrypt the DFSCredential.
- Create a new DFSCredential. (This is the default DFS Credential for each user the same. It is the SYSTEM DFS Credential). 
- Use the CMSEncryptionService with the public key of the public keystore to encrypt the DFSCredential.
- Store this DFS Credentials in the users space in SYSTEM DFS. This is done with the DFSCredentialService. 
  The DFSCredentialsService uses the SYSTEM DFS to store the credentials in the users space.
- Create a new instance of the DFS Connection with the DFS Credential.
- Create a new keystore for the user. This will become the users private keystore. 
  The password to read the keys is the users password. 
  This is the same password as for the users public keystore. 
  This keystore has a bunch of public/private key pairs, and at least one secret key. 
  The public keys are used to create the CMSEncelopeData, e.g. to encrypt the users data. 
  The private keys are used to decrypt the data. 
  The secret key will  be used to for the symmetric path encryption of the users data. 
- Store the users secret keystore in the USERS DFS.
- Extract all public Keys of the users keystore  and store them in the users space in the SYSTEM DFS.

![New user sequence diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/new_user.puml&fmt=png&vvv=1)

The DFSCredentialService is always connected to the SYSTEM DFS. When no former DFSCredentials are known,
the registration of a new DFSCredential simply stores these DFSCredentials in the user space. 
They are encrypted with the users public key. 
I would suggest  to keep only one DFS per user. This means, if the method registerDFS is called a second time, 
this means, that  all data stored so far will be moved from the previous DFS to the new DFS. 
In the previous DFS they will be deleted after successfull movement. 
The data will be retrieved and stored "as they are". E.g. neither the data nor the path will be decrypted. 
They already are. And the encryption only depends on the users secret keystore which will be moved too.

As seen above, the DFSCredentialService itself is encrypting and storing data. 
Thus it needs access to the users public keystore and the SYSTEM DFS. 
It may be better  to pass to all services needed elements like the keystore or public keys and do the 
storing outside the service. However, in the next sequence diagram more details are shown of the DFSCredentialService.

Now we look at a __**store document**__ use case:

- First the DFSCredentialService has to be asked for the users DFSCredential. 
- For that the DFSCredentialService has to read the users public keystore from the SYSTEM DFS.
- And the encrypted DFSCredentials
- Which now can be decrypted with the users public keystore (within its secret key that can be read with the users password)
- With the DFSCredentials a USERS DFS can be instantiated
- Next the document to be stored has to be encrypted. For that one of the users public keys has to be used. They are in the users space in the SYSTEM DFS. They are not encrypted, as they are public.
- Then the bucketpath of the document has to encrypted. For that a secret key of the users private keystore is needed. For that this keystore has to be read first.
- Then the bucketpath can be encrypted.
- Then the encrypted document can be stored with an encrypted bucket path in the USERs DFS

![Store file sequence diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/docusafe2/develop/docs/diagrams/store_file.puml&fmt=png&vvv=2)

All data stored on the SYSTEM DFS does not have to have a path encryption. 
Of course the DFSCredentials and the messages in the inbox have to be encrypted with a public key of the user. 
But the keystores and the public keys must neither be encrypted nor their path. 
On the USERs DFS all data except the keystore will have a path encryption and of course is encrypted too.