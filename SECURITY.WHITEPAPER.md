# Datasafe Security Whitepaper

## General information
Datasafe is a flexible encryption library. It uses different encryption algorithms. Some of them can be 
configured by client application. It uses encryption algorithms provided by Bouncy Castle library by default.
CMS (crypto message syntax) standard [RFC5652](https://tools.ietf.org/html/rfc5652.html) employed for storing private 
files encrypted with symmetric keys as well as for sharing files with other users using asymmetric key pairs. 

## Default storage configuration
Datasafe library can operate with different type of storages: filesystem, datasabe, s3 compatible(amazon aws, ceph, minio).
Other storage types can be added by implementing storage api interface. 
Each library user can use more then one storage. All user storages form users dfs (distributed file system)
By default on system start have to be configured one system dfs where users profiles will be stored.
New user registers in datasafe directory service using his name and password.
Default user creation process consists of creation private profile, public profile, keystore with public keys, keystore with private keys.
```
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
```                   
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
By default BCFKS keystore type is used. Keystore encryption algorithm is AES256_KWP. Password key derivation algorithm 
PRF_SHA512 with salt length 32 and iteration count 20480, KeyStore authentication algorithm HmacSHA3_512, password-like 
keys encryption algorithm is PBEWithHmacSHA256AndAES_256.
All this parameters can be changed by setting keystore config. For example instead of BCFKS keystore can be used UBER, 
instead of PBKDF2 based routines can be used Scrypt based.
Keystore contains secret keys for private files and path encryption, public/private key pairs for sharing files and 
certificates for sign/verify files. 
Default keystore prefix for generated keys is "KEYSTORE-ID-0". Each signing and encryption key has unique alias which 
is formed from keystore prefix plus UUID.

## Private files encryption algoritm 
Datasafe files uploaded by users in private area are encrypted using symmetric key 256-bit Advanced Encryption Standard (AES).
By default GCM operation mode is used.
Can be configured to use another encryption algorithm. Datasafe supports AES algorithms with 128, 192 and 256 key size 
in operation modes CBC, CCM, GCM or WRAP. Also supported CHACHA20_POLY1305 algorithm.
Default key prefix is "PRIVATE_SECRET"
Encrypted data wrapped into CMS standard envelope which contents information about key ID and algorithm used for encryption.
[RFC5652 section-6.2.3](http://tools.ietf.org/html/rfc5652#section-6.2.3)

## Encryption used for file sharing
Datasafe uses CMS for exchanging and sharing files. Public key is used to encrypt content-encryption key which is then 
stored inside cms envelope with other meta information like key alias and date. Receiver decrypts content-encryption 
key with his private key. By default used RSA public keys with key size 2048 "SHA256withRSA". Prefix in keystore 
"enc-" + keystoreID + UUID. Files can be shared with other clients of library whose inbox location is known. 
Files can be shared simultaneously with any number of recipients. And it doesn't require repeating encryption for each recipient
due to support of multiple recipients from CMS standard. Anyone can send file to user's inbox if his inbox location is known.

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
Resulting encrypted path string is Base64-urlsafe.
