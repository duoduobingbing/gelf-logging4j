package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.impl.Log4jProvider;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ProviderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author duoduobingbing
 * @author Daniel Lundsgaard Skovenborg
 */
class GelfLogAppenderNonStringMdcTests {

    private static final String LOG_MESSAGE = "foo bar test log message";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() throws Exception {
        Class<?> customThreadMapClass = TestingMutableThreadContextMap.class;
        System.setProperty("log4j2.threadContextMap", customThreadMapClass.getName());
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-dynamic-mdc.xml");
        //PropertiesUtil.getProperties().reload(); is now a no-op.

        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();

        Provider currentProvider = ProviderUtil.getProvider();

        //if log4j is already initialized by a different test, the default implementation for ThreadContextMap is already resolved
        //causing this test to break...
        //Ugly workaround: this is super prune to break, but there is no other way to reinit/reload the provider properly :/
        if (!Objects.equals(currentProvider.getThreadContextMapInstance().getClass(), customThreadMapClass)) {
            reinitResolvedThreadMapClass(currentProvider);
        }

        AssertJAssertions.assertThat(currentProvider.getThreadContextMapInstance().getClass()).isEqualTo(customThreadMapClass);
    }

    private static void reinitResolvedThreadMapClass(Provider currentProvider) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //Just use the internal log4jprovider.resetThreadContextMap();
        Method internalTestResetMethod = Log4jProvider.class.getDeclaredMethod("resetThreadContextMap");
        internalTestResetMethod.setAccessible(true);
        internalTestResetMethod.invoke(currentProvider);
        ThreadContext.init(); //reinit
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty("log4j2.threadContextMap");
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext.reconfigure();

        Provider currentProvider = ProviderUtil.getProvider();

        if (!Objects.equals(currentProvider.getThreadContextMapInstance().getClass(), DefaultThreadContextMap.class)) {
            reinitResolvedThreadMapClass(currentProvider);
        }

        AssertJAssertions.assertThat(currentProvider.getThreadContextMapInstance().getClass()).isEqualTo(DefaultThreadContextMap.class);
    }

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    /**
     * Copy of GelfLogAppenderDynamicMdcTests#testWithMdcFieldTypes() with raw types instead of String values
     */
    @Test
    void testWithRawMdcFieldTypes() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());
        ObjectThreadContextMap contextMap = (ObjectThreadContextMap) ThreadContext.getThreadContextMap();

        contextMap.putValue("myMdcs", "String");
        contextMap.putValue("myMdcl", 1);
        contextMap.putValue("myMdci", 2L);
        contextMap.putValue("myMdcd", 2.1);
        contextMap.putValue("myMdcf", 2.2d);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().getFirst();
        Map<String, Object> jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject).containsEntry("myMdcs", "String");
        assertThat(jsonObject).containsEntry("myMdcl", 1);
        assertThat(jsonObject).containsEntry("myMdci", 2);

        assertThat(jsonObject).containsEntry("myMdcd", 2.1);
        assertThat(jsonObject).containsEntry("myMdcf", 2.2);

        ThreadContext.put("myMdcl", "1.1");
        ThreadContext.put("myMdci", "2.1");
        ThreadContext.put("myMdcd", "wrong");
        ThreadContext.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().getFirst();
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject).containsEntry("myMdcl", 1);
        assertThat(jsonObject).containsEntry("myMdci", 2);

        assertThat(jsonObject).doesNotContainKey("myMdcd");
        assertThat(jsonObject).containsEntry("myMdcf", 0.0);

        ThreadContext.put("myMdcl", "b");
        ThreadContext.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().getFirst();
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject).doesNotContainKey("myMdcl");
        assertThat(jsonObject).containsEntry("myMdci", 0);
    }

}
