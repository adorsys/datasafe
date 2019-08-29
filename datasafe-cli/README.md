# Build
1. You need GraalVM compiler and BouncyCastle in java-home/jre/lib/ext/ of this compiler.
1. Edit the java-home/jre/lib/security/java.security properties file in any text editor. 
1. Add the JCE provider you’ve just downloaded to this file.
1. The java.security file contains detailed instructions for adding this provider. 
Basically, you need to add a line of the following format in a location with similar properties:
security.provider.n=provider-class-name
