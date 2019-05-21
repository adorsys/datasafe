package de.adorsys.datasafe.business.impl.profile.dfs;

import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class DFSSystemTest {
    @Test
    public void getKeyStoreAuth() {
        String PASS = "keystorepassword";
        DFSSystem dfsSystem = new DFSSystem(new DFSConfig() {
            @Override
            public String keystorePassword() {
                return PASS;
            }

            @Override
            public URI systemRoot() {
                return null;
            }
        });
        Assertions.assertEquals(PASS, dfsSystem.publicKeyStoreAuth().getReadKeyPassword().getValue());
    }
}
