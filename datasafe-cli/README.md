# Issues with certificates

If you observe error
`InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty` when running
cli executable, then launch it with Java CA certificates path provided, i.e.:
```
./cli -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -c ~/credentials profile create
```
where `/etc/ssl/certs/java/cacerts` is the location of Java certificates keystore.

# Build

## Using Dockerfile
You can build Datasafe-CLI for Linux using this [Dockerfile](Dockerfile)

## Manual
1. You need GraalVM compiler and BouncyCastle in java-home/jre/lib/ext/ of this compiler.
1. Edit the java-home/jre/lib/security/java.security properties file in any text editor. 
1. Add the JCE provider you’ve just downloaded to this file.
1. The java.security file contains detailed instructions for adding this provider.
1. add org.bouncycastle.jsse.provider.BouncyCastleJsseProvider to java.security as SSL provider (bctls jar) 
Basically, you need to add a line of the following format in a location with similar properties:
security.provider.n=provider-class-name

# SimpleTest with Minio
Lets assume you have a minio running with url: <code>http://localhost:9000</code>.
Further you have a bucket named <code>"affe"</code> in that minio.
Also we will assume (although minio ignores it) that you have this bucket in eu-central-1 region.

Before you run the Cli (which is the main class) in the project directory, create a temporary folder <code><$projectDir>/tmp</code>.
And than allways start the Cli in this tmp directory. In this case, you can use the default confirms and all profiles/keys/secrets will be stored in the tmp directory. As this example is to store the data in minio, the data is not stored in the tmp directory, but in minio.

1. First you create a profile. Simply confirm all asked questions with enter.
    ```
    -u=peter -p=peter -sp=system profile create
    ```
1. Add the url of minio with a new storagename. Notice that the bucket (*affe*) is at the end of the url.
Also, notice region *eu-central-1* is at the beginning of URL path.

    ```
    -u=peter -p=peter -sp=system profile storage add -i my-minio -p http://localhost:9000/eu-central-1/affe/
    ```

1. Add the accesskey and secretkey. It assumed, they are <code>simpleAcessKey</code> and <code>simpleSecretKey</code>.
    ```
    -u=peter -p=peter -sp=system profile storage credentials add -m http://.+ --username=simpleAccessKey --password=simpleSecretKey
    ```
 
1. Now you can store data to your minio. The data will be encrypted with the keys stored in the tmp directory.
   So in this case the pom is stored to minio. Of course encrypted. But with the new name pom2.xml. 
    ```
    -u=peter -p=peter -sp=system private  cp -s=my-minio ../pom.xml pom2.xml
    ```

1. Now you can see the encrypted data in your minio. To see the decrypted name, you can use the list command.    
    ```
    -u=peter -p=peter -sp=system private ls -s=my-minio
    ```
    
1. Eventually you can decrypt the content of <code>pom2.xml</code>. I will be written to stdout.    
    ```
    -u=peter -p=peter -sp=system private cat -s=my-minio pom2.xml
    ```
       
