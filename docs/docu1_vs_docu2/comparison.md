9.4.2019

# Adorsys Docusafe vs. Datasafe

## IN BRIEF
The docusafe version has the following features:
- all documents are encrypted symmetricly (individually per user)
- all document names are encrypted (system wide same encryption)
- all documents are stored on the same distributed file system (DFS)

The datasafe version has the following features:
- all data is encrypted symmetricly (individually per user)
- all data names are encrypted (individually per user)
- each user has its own DFS

## IN DETAIL

The keystores in docusafe and datasafe actually are the same, but the usage of the keys differs significantly. 
![](../images/v1-keystore.bmp) 
A keystore allways has two locks. The first lock is used to protect the keystore at all. In docusafe this lock is to be opened with a general key. It is the same key for all users. It give the owner of the general key the chance to get the public keys of the user. As in datasafe the public keys of all users are kept in an authorization server, there is no more need to use a general key. So in datastore the general key is the users password too and thus each keystore can only be opened by the user him/herself. Inside the keystore is a private area, that only can be retrieved with another key. For this key, the users password is used. In this private area the private keys corresponding to the public keys and some symmetric keys are kept. In docusafe the users documents are encrypted with the secret key of the keystore. (actually this is since version 0.5.22. versions before use a symmetric document guard that is locked with the secret key). In datastore the symmetric key is only used for the encryption of the document path.

In docusafe and datasafe documents/data are always encrypted with a symmetric key. 
![](../images/v1-document-safe.bmp) 
In docusafe this key is protected by a document guard. The document guard may have a symmetric lock or an asymmetric lock. The document guards of the documents that are stored by the user him/herself always are locked with a (symmetric) secret key of the keystore. Since version 0.5.22 those documents do not have document guard at all. They are directly encrypted with the secret key of the key store.
![](../images/v1-symmetric-document-guard.bmp)  
The document guards of documents that are given from one user to another are asymmetricly encrypted with the users public key. 
![](../images/v1-asymmetric-document-guard.bmp)  
So in docusafe each document has a document guard that keeps the encryption key of the document. Further the document guards differ in symmetricly or asymmetricly encryption.The symmetric key of this document safe is put into a document guard (or since version 0.5.22 this is a secret key of the keystore and not put into a guard at all). This document guard is locked symmetricaly with the secret key of the users keystore. Or, if the document is given from one user to another, the document guard is asymmetrically locked with the users public key. To go slightly more into detail, in docusafe all documents of the same directory are encrypted with the same secret key. So a document guard is more a directory guard than a document guard.

In the datasafe the document guards and document safes have been replaced by the CMSEnvelopeData. This is a bouncy castle class that it a asymmetric document guard and a symmetric document safe at the same time. So rather than creating a symmetric key for the document encryption and encrypting this key into a document guard explicitly, this is all done by the Bouncy Castle stuff. 

The CMSEnvelope is locked with the users public key. 
![](../images/v2-cmsenvelope.bmp)  
It can only be unlocked with the users private key. Inside the envelope is a symmetric key and the data, that has been encrypted with that symmetric key. 

The idea of that envelope is, that the data is still encrypted symmetrically, which can be more than a thousend times faster decrypted than an asymmetrically encrypted data. But the key to decrypt the data is asymmetrically encrypted. As the key is very small the cost of the encryption is not that relevant. But the main advantage is, that the document guard and the document safe are joined in one element. In docusafe each document needs unencrypted meta information that give hint to the encryption algorithm and the document key id. All this is no more needed with the CMSEnvelope, or to be more precise this is kept inside the envelope by Bouncy Castle standard classes.

As consequence in datastore every time, data is stored, it is encrypted with another key and each CMSEnvelope is encrypted asymmetricly, inepedant if the user encrypts the data for him/herself of for another user. Even storing the same document more than once means every time a new encryption key.

Further in docusafe there is only one distributed file system to be used at one time. So all users are stored at this DFS. Of course each user is protected individually. In datastsore each user may have its own personal DFS. So one user may be stored at amazon, the other one at a local ceph or the next one at a remote minio system. For the user of the datastore, this is transparent. Data can be stored and given from one user to another user without caring about these details. 

## Overview in table
| docusafe | docusafe 0.5.22 and newer | datasafe |
|:----------|---------------------------|----------|
| Each document protected with a document guard. Documents to other users protected with an asymmetric document guard, documents for the usher him/herself with a symmetric document guard.|Documents to other users protected with an asymmetric document guard, documents for the usher him/herself are encrypted with a secret key of the users keystore.|All documents symmetricly encrypted with a CMSEnvelope (that assymetricly protects the encryption key).|
| Path encryption equal for all users. | Path encryption equal for all users.| Path encryption individually for each user.|
| One DFS for all users. | One DFS for all users. | One DFS per user.|
