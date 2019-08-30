#!/bin/bash

# 3. Adds BouncyCastle Provider to JDK security providers
# 4. Adds BouncyCastle TLS to JDK security providers
# 5. Removes SunEC security provider to use SSL from BouncyCastle

# 3. Add BouncyCastle Provider to JDK security providers at place 2 (should replace sun.security.rsa.SunRsaSign)
sed -i "s/security\.provider\.2=.*/security.provider.2=org.bouncycastle.jce.provider.BouncyCastleProvider/g" "$JAVA_HOME/jre/lib/security/java.security"

# 4. Add BouncyCastle TLS to JDK security providers at place 3 (should replace sun.security.ec.SunEC and come before com.sun.net.ssl.internal.ssl.Provider)
sed -i "s/security\.provider\.3=.*/security.provider.3=org.bouncycastle.jsse.provider.BouncyCastleJsseProvider/g" "$JAVA_HOME/jre/lib/security/java.security"

# 5. Remove SunEC security provider (if it still exists)
sed -i "s/security\.provider\..*=sun\.security\.ec\.SunEC//g" "$JAVA_HOME/jre/lib/security/java.security"
