package de.adorsys.datasafe.business.impl.e2e.performance.fixture.generator;

import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.*;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.adorsys.datasafe.business.impl.e2e.performance.fixture.generator.FixtureGenerator.GENERATE_FIXTURE_SIZE;

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

    @Test
    void generateRandomFixture() {
        do {
            session.execute(storageByUser.values());
        } while (historyList.canContinue());

        log.info("DONE");
        printResult();
    }

    private void printResult() {
        Fixture fixture = new Fixture(
                historyList.getOperations(),
                storageByUser.entrySet().stream()
                        .collect(Collectors.toMap(
                                it -> it.getKey().getUsername(),
                                it -> it.getValue().getPrivateFiles().getFiles())
                        ),
                storageByUser.entrySet().stream()
                        .collect(Collectors.toMap(
                                it -> it.getKey().getUsername(),
                                it -> it.getValue().getInboxFiles().getFiles())
                        )
        );

        log.info("Fixture:");
        log.info("{}", new Gson().toJson(fixture));
    }

    private StatelessKieSession newKieSession() {
        KieServices services = KieServices.Factory.get();
        KieFileSystem fileSystem = services.newKieFileSystem();
        fileSystem.write(ResourceFactory.newClassPathResource("performance/fixture/generator/drools/user.drl"));
        KieBuilder kb = services.newKieBuilder(fileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();

        KieContainer kContainer = services.newKieContainer(kieModule.getReleaseId());

        return kContainer.newStatelessKieSession();
    }

    private StatelessKieSession prepareSession(StatelessKieSession session) {
        session.setGlobal("randomPass", new RandomPassGate(random()));
        session.setGlobal("randomPath", new RandomPathGenerator(
                random(),
                3,
                RandomPathGenerator.DEFAULT_COMPONENTS,
                RandomPathGenerator.DEFAULT_FILENAMES)
        );
        session.setGlobal("randomContent", new RandomContentIdGenerator(random(), 10));
        session.setGlobal("historyList", historyList);

        return session;
    }

    private static Random random() {
        return new Random(0);
    }

    private static TestFileTree inbox(List<Operation> operations, TestUser user) {
        return new TestFileTree(operations::add, user, StorageType.INBOX, random());
    }

    private static TestFileTree privatespace(List<Operation> operations, TestUser user) {
        return new TestFileTree(operations::add, user, StorageType.PRIVATE, random());
    }
}
