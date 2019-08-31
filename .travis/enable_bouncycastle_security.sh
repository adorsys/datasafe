#!/bin/bash

# SED flavors in-place replacement handling:
SED_I_EXEC="sed -i"
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    SED_I_EXEC="sed -i ''"
fi

# 3. Adds BouncyCastle Provider to JDK security providers
# 4. Adds BouncyCastle TLS to JDK security providers
# 5. Removes SunEC security provider to use SSL from BouncyCastle


# 3. Add BouncyCastle Provider to JDK security providers at place 2 (should replace sun.security.rsa.SunRsaSign)
$SED_I_EXEC "s/security\.provider\.2=.*/security.provider.2=org.bouncycastle.jce.provider.BouncyCastleProvider/g" "$JAVA_HOME/jre/lib/security/java.security"

# 4. Add BouncyCastle TLS to JDK security providers at place 3 (should replace sun.security.ec.SunEC and come before com.sun.net.ssl.internal.ssl.Provider)
$SED_I_EXEC "s/security\.provider\.3=.*/security.provider.3=org.bouncycastle.jsse.provider.BouncyCastleJsseProvider/g" "$JAVA_HOME/jre/lib/security/java.security"

# 5. Remove SunEC security provider (if it still exists)
$SED_I_EXEC "s/security\.provider\..*=sun\.security\.ec\.SunEC//g" "$JAVA_HOME/jre/lib/security/java.security"
