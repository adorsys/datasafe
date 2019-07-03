package de.adorsys.datasafe.business.impl.e2e.randomactions.framework;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.dto.UserSpec;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.shared.ContentGenerator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

abstract class BaseRandomActions extends WithStorageProvider {

    private static Fixture fixture;

    protected static final Map<String, StatisticService> STATS = new HashMap<>();

    /**
     * Initializes {@link Fixture} from performance/fixture/fixture.json resource file.
     */
    @BeforeAll
    @SneakyThrows
    static void init() {
        try (Reader reader = Resources.asCharSource(
                Resources.getResource("fixture/fixture.json"),
                StandardCharsets.UTF_8).openStream()) {
            fixture = new Gson().fromJson(reader, Fixture.class);
        }
    }

    protected Map<String, UserSpec> initUsers(String testId, int fileSize) {
        Map<String, UserSpec> users = new HashMap<>();
        fixture.getUserPrivateSpace().forEach((userId, space) -> {
            UserIDAuth auth = registerUser(testId + "-" + userId);
            users.put(userId, new UserSpec(auth, new ContentGenerator(fileSize)));
        });

        return users;
    }

    protected abstract UserIDAuth registerUser(String name);
}
