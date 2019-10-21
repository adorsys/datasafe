# Datasafe Security Whitepaper

## General information
Datasafe is a flexible encryption library. It uses different encryption algorithms. Some of them can be 
configured by client application. It uses encryption algorithms provided by Bouncy Castle library by default. 

## Default storage configuration
Datasafe library can operate with different type of storages: filesystem, datasabe, s3 compliant(amazon aws, ceph, minio).
Other storage types can be added by implementing storage api interface. 
Each library user can use more then one storage. All users storages form users dfs (distributed file system)
By default on system start have to be configured one system dfs where users profiles will be stored.
New user registers in datasafe directory service using his name and password.
Default user creation process consists of creation private profile, public profile, keystore with public keys, keystore with private keys.
Default location within system dfs:
    /profiles
        /private
            /username - user's private profile 
        /public
            /username - user's public profile   
    /users
        /public
            /pubkeys - keystore consists user's public key
            /inbox - location of shared with user files    
        /private
            /keystore - keystore consists user's private key
            /files/SIV - location of private files. SIV is 3 symbol path encryption algorithm identifier.
                   
Example private profile:
```
    {
        "keystore": {
            "resource": "s3://adorsys-docusafe/users/username/private/keystore"
        },
        "privateStorage": [
            [{"id": "DEFAULT"}, {"resource": "s3://adorsys-docusafe/users/username/private/files/"}]
        ],
        "inboxWithFullAccess": {
            "resource": "s3://adorsys-docusafe/users/username/public/inbox/"
        },
        "publishPublicKeysTo": {
            "resource": "s3://adorsys-docusafe/users/username/public/pubkeys"
        },
        "associatedResources": [
            {"resource": "s3://adorsys-docusafe/users/username/"}
        ],
        "documentVersionStorage": {
            "resource": "s3://adorsys-docusafe/users/username/versions/"
        },
        "appVersion": "BASELINE"
    }
```
            
Example public profile:
```
    {
        "publicKeys": {
            "resource": "s3://adorsys-docusafe/users/username/public/pubkeys"
        },
        "inbox": {
            "resource": "s3://adorsys-docusafe/users/username/public/inbox/"
        },
        "appVersion": "BASELINE"
    }   
```
    
## Keystore encryption
User personal password is used to access keystore. Password can be changed without the need of changing keystore content. 
Default keystore prefix for generated keys is "KEYSTORE-ID-0"
Keystore contains secret keys for private files and path encryption, public/private key pairs for sharing files and 
certificates for sign/verify files. 
Keystore keeps secret keys, public/private key pairs and certificates. It secures them by encrypting content 
with following default algorithms:
BCFKS keystore type is used
EncryptionAlgorithm AES256_KWP
PBKDFConfig PRF_SHA512
MacAlgorithm HmacSHA3_512

## Private files encryption algoritm
Datasafe files uploaded by users are encrypted using 256-bit Advanced Encryption Standard
(AES).
For private files encryption AES algorithm is used with default key length 256 bit.
256_GCM wit default AES256_GCM

default secret key algoritm PBEWithHmacSHA256AndAES_256 

## Encryption used for file sharing
Datasafe uses CMS (crypto message syntax) standard for exchanging and sharing files.
For encryption used RSA public keys with key size 2048 "SHA256withRSA". Prefix in keystore "enc-" + keystoreID + UUID
Files can be shared with other clients of library whose inbox location is known. 
Files can be shared simultaneously with any number of recipients. And it doesn't require repeating encryption for each recipient
due to support of multiple recipients from CMS standard. 

## File location encryption
Files can be stored in subdirectories. Each part of path encrypted separately using AES-GCM-SIV algorithm. 
(Synthetic Initialization Vector) [RFC-845]("https://tools.ietf.org/html/rfc845").
Default implementation of symmetric path encryption is integrity preserving which means that each invocation of 
cipher(segment) yields same result. Additionally each segment is authenticated against its parent path hash (SHA-256 digest), so 
attacker can't move a/file to b/file without being detected 

It requires 2 secret keys for path encryption/decryption. They stored inside keystore with prefixes "PATH_SECRET" for 
S2V AES key used in Cipher-based Message Authentication Code(CMAC) mode and "PATH_CTR_SECRET_" for CTR AES key used in 
counter mode. Both 256 bit size by default.
After encryption each path part concatenated to result encrypted path.
Example:
unencrypted file location: /path/to/file/document.pdf
encrypted location:        /cipher(path)/cipher(to)/cipher(file)/cipher(document.pdf)
Such approach gives ability to list any directory without the need of decryption all files location.
Base64-urlsafe path serialization
