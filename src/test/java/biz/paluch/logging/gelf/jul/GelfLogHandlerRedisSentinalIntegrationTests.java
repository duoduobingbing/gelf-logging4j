package biz.paluch.logging.gelf.jul;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.RedisSentinelIntegrationTestHelper;
import biz.paluch.logging.gelf.Sockets;
import org.apache.log4j.MDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GelfLogHandlerRedisSentinalIntegrationTests extends RedisSentinelIntegrationTestHelper {


    @BeforeEach
    void before() {
        assumeTrue(Sockets.isOpen("localhost", RedisSentinelIntegrationTestHelper.redisLocalMasterPort));
        assumeTrue(Sockets.isOpen("localhost", RedisSentinelIntegrationTestHelper.redisLocalSentinelPort));

        GelfTestSender.getMessages().clear();
        MDC.remove("mdcField1");
    }

    @Test
    void testSentinel() throws Exception {
        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-redis-sentinel-logging.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);

        List<String> list = jedis.lrange("list", 0, jedis.llen("list"));
        assertThat(list).hasSize(1);

        Map<String, Object> map = JsonUtil.parseToMap(list.get(0));

        assertThat(map.get("full_message")).isEqualTo(expectedMessage);
        assertThat(map.get("short_message")).isEqualTo(expectedMessage);
        assertThat(map.get("fieldName1")).isEqualTo("fieldValue1");
    }
}
