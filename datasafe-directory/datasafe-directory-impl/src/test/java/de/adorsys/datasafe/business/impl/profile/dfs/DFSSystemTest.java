package de.adorsys.datasafe.business.impl.profile.dfs;

import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFSSystemTest {
    @Test
    public void getKeyStoreAuth() {
        String passwordString = "keystorepassword";
        DFSSystem dfsSystem = new DFSSystem(new DFSConfig() {
            @Override
            public String keystorePassword() {
                return passwordString;
            }

            @Override
            public URI systemRoot() {
                return null;
            }
        });
        assertEquals(passwordString, dfsSystem.publicKeyStoreAuth().getReadKeyPassword().getValue());
    }
}
