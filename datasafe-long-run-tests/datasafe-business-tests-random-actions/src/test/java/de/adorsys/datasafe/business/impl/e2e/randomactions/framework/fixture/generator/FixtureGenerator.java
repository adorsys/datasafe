package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator;

import com.google.gson.GsonBuilder;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.StorageType;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.TestFileTreeOper;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.TestUser;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.UserFileSystem;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator.FixtureGenerator.GENERATE_FIXTURE_SIZE;

/**
 * This is not a real test, instead it generates test fixture that can be used in other tests.
 * This fixture consists of file operations done by user and resulting file tree of this user to assert.
 * Note that fixture expects synchronization on a user, so multiple user can do work concurrently, but concrete
 * user can't.
 */
@Slf4j
@EnabledIfEnvironmentVariable(named = GENERATE_FIXTURE_SIZE, matches = ".+")
class FixtureGenerator extends BaseMockitoTest {

    static final String GENERATE_FIXTURE_SIZE = "GENERATE_FIXTURE_SIZE";

    private static final int DESIRED_SOFT_OPERATION_COUNT = Integer.parseInt(System.getenv(GENERATE_FIXTURE_SIZE));
    private static final int USER_COUNT = 10;

    private Map<TestUser, UserFileSystem> storageByUser;
    private HistoryList historyList;

    private StatelessKieSession session;

    @BeforeEach
    void init() {
        storageByUser = new HashMap<>();
        historyList = new HistoryList(new ArrayList<>(), DESIRED_SOFT_OPERATION_COUNT);
        IntStream.range(0, USER_COUNT).forEach(it -> storageByUser.computeIfAbsent(
                new TestUser("user-" + it, UUID.randomUUID().toString()),
                id -> new UserFileSystem(
                        id,
                        privatespace(historyList.getOperations(), id),
                        inbox(historyList.getOperations(), id))
                )
        );
        session = prepareSession(newKieSession());
    }

    /**
     * Generates random operations done on file tree, expected filesystem content and writes resulting JSON
     * {@link Fixture} into console
     */
    @Test
    void generateRandomFixture() {
        do {
            session.execute(storageByUser.values());
        } while (historyList.canContinue());

        log.info("DONE");
        printResult();
    }

    @SneakyThrows
    private void printResult() {
        Fixture fixture = new Fixture(
                historyList.getOperations(),
                storageByUser.entrySet().stream()
                        .collect(Collectors.toMap(
                                it -> it.getKey().getUsername(),
                                it -> it.getValue().getPrivateOper().getFiles())
                        ),
                storageByUser.entrySet().stream()
                        .collect(Collectors.toMap(
                                it -> it.getKey().getUsername(),
                                it -> it.getValue().getInboxOper().getFiles())
                        )
        );

        String path = "./src/test/resources/fixture/result.json";
        System.out.println("Fixture has been written to: " + path);
        try (FileOutputStream os = new FileOutputStream(path)) {
            os.write(new GsonBuilder().setPrettyPrinting().create().toJson(fixture).getBytes());
        }
    }

    /**
     * Using drools to supply random actions on file tree.
     */
    private StatelessKieSession newKieSession() {
        KieServices services = KieServices.Factory.get();
        KieFileSystem fileSystem = services.newKieFileSystem();

        // rules to use when generating random actions:
        fileSystem.write(ResourceFactory.newClassPathResource("fixture/generator/drools/user.drl"));

        KieBuilder kb = services.newKieBuilder(fileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();

        KieContainer kContainer = services.newKieContainer(kieModule.getReleaseId());

        return kContainer.newStatelessKieSession();
    }

    private StatelessKieSession prepareSession(StatelessKieSession session) {
        // classes that are available to the rules:
        session.setGlobal("randomPass", new RandomPassGate(random()));
        session.setGlobal("randomPath", new RandomPathGenerator(
                30,
                70,
                random(),
                3,
                RandomPathGenerator.DEFAULT_COMPONENTS,
                RandomPathGenerator.DEFAULT_FILENAMES)
        );
        session.setGlobal("randomContent", new RandomContentIdGenerator(random(), 10));
        session.setGlobal("randomUsers", new RandomUsers(random()));
        session.setGlobal("historyList", historyList);

        return session;
    }

    private static Random random() {
        return new Random(0);
    }

    private static TestFileTreeOper inbox(List<Operation> operations, TestUser user) {
        return new TestFileTreeOper(operations::add, user, StorageType.INBOX, random());
    }

    private static TestFileTreeOper privatespace(List<Operation> operations, TestUser user) {
        return new TestFileTreeOper(operations::add, user, StorageType.PRIVATE, random());
    }
}
