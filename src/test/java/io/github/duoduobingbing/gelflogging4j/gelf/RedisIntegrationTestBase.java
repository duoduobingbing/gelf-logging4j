package io.github.duoduobingbing.gelflogging4j.gelf;

import org.junit.jupiter.api.BeforeAll;

/**
 * @author tktiki
 * @author duoduobingbing
 */
public class RedisIntegrationTestBase extends RedisNonStartingIntegrationTestBase{

    @BeforeAll
    static void beforeAll() {
        createRedisMasterTestcontainer();
        startRedisMasterTestcontainer();
    }



}
