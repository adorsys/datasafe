package de.adorsys.datasafe.directory.impl.profile.dfs;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSSystem;
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
