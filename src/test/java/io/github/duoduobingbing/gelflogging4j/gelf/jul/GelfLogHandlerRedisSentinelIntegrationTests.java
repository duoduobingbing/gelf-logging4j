package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.RedisSentinelIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.Sockets;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PropertiesHelper;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author tktiki
 * @author duoduobingbing
 */
public class GelfLogHandlerRedisSentinelIntegrationTests extends RedisSentinelIntegrationTestBase {


    @BeforeEach
    void before() {
        Assumptions.assumeTrue(Sockets.isOpen("localhost", RedisSentinelIntegrationTestBase.redisMasterResolvedPort));
        Assumptions.assumeTrue(Sockets.isOpen("localhost", RedisSentinelIntegrationTestBase.redisLocalSentinelTestcontainer.getMappedPort(redisLocalSentinelPort)));

        GelfTestSender.getMessages().clear();
        MDC.remove("mdcField1");
    }

    @Test
    void testSentinel() throws Exception {
        InputStream propertiesStream = PropertiesHelper.replacePortInResource(
                "/jul/test-redis-sentinel-logging.properties",
                redisLocalSentinelTestcontainer,
                redisLocalSentinelPort,
                26379
        );

        LogManager.getLogManager()
                .readConfiguration(propertiesStream);

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);

        List<String> list = jedisMaster.lrange("list", 0, jedisMaster.llen("list"));
        AssertJAssertions.assertThat(list).hasSize(1);

        Map<String, Object> map = JsonUtil.parseToMap(list.get(0));

        AssertJAssertions.assertThat(map.get("full_message")).isEqualTo(expectedMessage);
        AssertJAssertions.assertThat(map.get("short_message")).isEqualTo(expectedMessage);
        AssertJAssertions.assertThat(map.get("fieldName1")).isEqualTo("fieldValue1");
    }
}
