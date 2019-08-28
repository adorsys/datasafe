# Build
You need GraalVM compiler and BouncyCastle in java-home/jre/lib/ext/ of this compiler.
Edit the java-home/jre/lib/security/java.security properties file in any text editor. 
Add the JCE provider you’ve just downloaded to this file.
The java.security file contains detailed instructions for adding this provider. 
Basically, you need to add a line of the following format in a location with similar properties:
security.provider.n=provider-class-name
