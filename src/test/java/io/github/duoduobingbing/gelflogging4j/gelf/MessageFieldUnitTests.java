package io.github.duoduobingbing.gelflogging4j.gelf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author Mark Paluch
 */
class MessageFieldUnitTests {

    private static final String NAME = "name";

    @Test
    void testMdcMessageField() throws Exception {
        MdcMessageField field = new MdcMessageField(NAME, "mdcName");
        assertThat(field.toString()).isEqualTo(MdcMessageField.class.getSimpleName() + " [name='name', mdcName='mdcName']");
    }

    @Test
    void testLogMessageField() throws Exception {
        LogMessageField field = new LogMessageField(NAME, LogMessageField.NamedLogField.byName("SourceMethodName"));
        assertThat(field.toString())
                .isEqualTo(LogMessageField.class.getSimpleName() + " [name='name', namedLogField=SourceMethodName]");
    }

    @Test
    void testStaticMessageField() throws Exception {
        StaticMessageField field = new StaticMessageField(NAME, "value");
        assertThat(field.toString()).isEqualTo(StaticMessageField.class.getSimpleName() + " [name='name', value='value']");
    }

    @Test
    void testDynamicMdcMessageField() throws Exception {
        DynamicMdcMessageField field = new DynamicMdcMessageField(".*");
        assertThat(field.toString()).isEqualTo(DynamicMdcMessageField.class.getSimpleName() + " [regex='.*']");
    }

    @Test
    void testGetMapping() throws Exception {
        List<LogMessageField> result = LogMessageField.getDefaultMapping(false, LogMessageField.NamedLogField.LoggerName,
                LogMessageField.NamedLogField.NDC);

        assertThat(result).hasSize(2);
    }

    @Test
    void testGetMappingAllFields() throws Exception {

        List<LogMessageField> result = LogMessageField.getDefaultMapping(false, LogMessageField.NamedLogField.values());

        assertThat(result.size()).isEqualTo(LogMessageField.NamedLogField.values().length);
    }

    @Test
    void testGetMappingAllFieldsWithDefaultFile() throws Exception {

        List<LogMessageField> result = LogMessageField.getDefaultMapping(true, LogMessageField.NamedLogField.values());

        assertThat(result.size()).isEqualTo(LogMessageField.NamedLogField.values().length);
    }
}
