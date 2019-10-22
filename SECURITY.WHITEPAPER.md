# Datasafe Security Whitepaper

## General information
Datasafe is a flexible encryption library. It uses different encryption algorithms. Some of them can be 
configured by client application. Under the hood Datasafe uses BouncyCastle library to perform encryption.

CMS (Cryptographic Message Syntax) standard [RFC5652](https://tools.ietf.org/html/rfc5652.html) employed for storing private 
files encrypted with symmetric keys as well as for sharing files with other users using asymmetric key pairs. 

## Default locations
To maintain information where user data resides, Datasafe uses concept of user profile. There are two types of user 
profile: private and public

<details>
  <summary>Default location within system dfs</summary>
  
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
</details>
      
<details>             
  <summary>Example private profile:</summary>
  
    {
        "keystore": {
            "resource": "s3://bucketname/users/username/private/keystore"
        },
        "privateStorage": [
            [{"id": "DEFAULT"}, {"resource": "s3://bucketname/users/username/private/files/"}]
        ],
        "inboxWithFullAccess": {
            "resource": "s3://bucketname/users/username/public/inbox/"
        },
        "publishPublicKeysTo": {
            "resource": "s3://bucketname/users/username/public/pubkeys"
        },
        "associatedResources": [
            {"resource": "s3://bucketname/users/username/"}
        ],
        "documentVersionStorage": {
            "resource": "s3://bucketname/users/username/versions/"
        },
        "appVersion": "BASELINE"
    }
</details>
         
<details>   
  <summary>Example public profile:</summary>
  
    {
        "publicKeys": {
            "resource": "s3://bucketname/users/username/public/pubkeys"
        },
        "inbox": {
            "resource": "s3://bucketname/users/username/public/inbox/"
        },
        "appVersion": "BASELINE"
    }   
</details>

## Keystore encryption
System wide password is used to open keystore and users' personal password to read users' keys. Password can be changed 
without the need of changing keystore content.
By default used BCFKS keystore type to store on disk with:
-  keystore encryption algorithm is AES256_KWP;
-  password key derivation algorithm PRF_SHA512;
-  salt length 32 bytes;
-  iteration count 20480;
-  keystore authentication algorithm HmacSHA3_512 (protects keystore from tampering);
-  password-like keys encryption algorithm is PBEWithHmacSHA256AndAES_256.

UBER keystore type is used to cache keys in memory 
([algorithms details](https://cryptosense.com/blog/bouncycastle-keystore-security/)).
Despite UBER keystore protected worse than BCFKS it has better performance and still a good choice to cache keys in memory. 
Anyway if someone gets to memory dump of machine there is really not that much can be done to protect application.

UBER parameters:
-  keys encryption algorithm is PBEWithSHAAnd3-KeyTripleDES-CBC;
-  salt length 20 bytes;
-  iteration count random(1024, 2047).

All this parameters can be changed by setting keystore config. For example instead of BCFKS keystore can be used UBER, 
instead of PBKDF2 (Password-Based Key Derivation Function) ) based routines can be used Scrypt based.

Keystore contains secret keys for private files and path encryption, public/private key pairs for sharing files. 

Each signing and encryption key has unique alias which is formed from keystore prefix plus UUID.

## Private files encryption algoritm 
Datasafe files uploaded by users in private area are encrypted using symmetric key 256-bit Advanced Encryption Standard (AES).

By default GCM (Galois counter mode) operation mode is used. It is considered best practice to use authenticated 
encryption modes such as CCM or GCM in preference to CBC. It prevent attacks coming from fake or tampered ciphertexts.

Can be configured to use another encryption algorithm. Datasafe supports AES algorithms with 128, 192 and 256 key size 
in operation modes CBC (Cipher-block chaining), CCM (CBC-MAC), GCM or WRAP (Key Wrap). For the cases when 
large amounts of data (> 300GB) are going to be stored one should prefer CHACHA20_POLY1305 that is also available.
Encrypted data wrapped into CMS standard envelope which contents information about key ID and algorithm used for encryption.
[RFC5652 section-6.2.3](http://tools.ietf.org/html/rfc5652#section-6.2.3)

## Encryption used for file sharing
Datasafe uses CMS for exchanging and sharing files. Public key is used to encrypt content-encryption key which is then 
stored inside cms envelope with other meta information like key alias and date. Receiver decrypts content-encryption 
key with his private key. By default used RSA public keys with key size 2048 "SHA256withRSA". For data encryption used 
AES GCM 256 symmetric key. Files can be shared with other clients of library whose inbox location is known.

Files can be shared simultaneously with any number of recipients. And it doesn't require repeating encryption for each recipient
due to support of multiple recipients from CMS standard. Anyone can send file to user's inbox if his inbox location is known.

## File location encryption
Files can be stored in subdirectories. Each part of path encrypted separately using AES-GCM-SIV algorithm. 
(Synthetic Initialization Vector) [RFC-845](https://tools.ietf.org/html/rfc845).

Default implementation of symmetric path encryption is integrity preserving which means that each segment is 
authenticated against its parent path hash (SHA-256 digest), so attacker can't move a/file to b/file without being detected. 

It requires 2 secret keys for path encryption/decryption (both 256 bit size by default): 
-  S2V AES key used in CMAC (Cipher-based Message Authentication Code) mode
-  CTR AES key used in counter mode.

After encryption each path part concatenated to result encrypted path.

Example:
-  unencrypted file location: /path/to/file/document.pdf
-  encrypted location:        /cipher(path)/cipher(to)/cipher(file)/cipher(document.pdf)

Such approach gives ability to list any directory without the need of decryption all files location.
Resulting encrypted path string is Base64-urlsafe.
