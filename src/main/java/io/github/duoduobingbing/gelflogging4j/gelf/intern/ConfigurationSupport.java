package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import io.github.duoduobingbing.gelflogging4j.gelf.DynamicMdcMessageField;
import io.github.duoduobingbing.gelflogging4j.gelf.GelfMessageAssembler;
import io.github.duoduobingbing.gelflogging4j.gelf.MdcMessageField;
import io.github.duoduobingbing.gelflogging4j.gelf.StaticMessageField;

/**
 * @author Mark Paluch
 * @author Thomas Herzog
 */
public class ConfigurationSupport {

    public static final String MULTI_VALUE_DELIMITTER = ",";
    public static final char EQ = '=';

    private ConfigurationSupport() {
    }

    /**
     * Set the additional (static) fields.
     *
     * @param spec                 field=value,field1=value1, ...
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setAdditionalFields(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null == spec) {
            return;
        }

        String[] properties = spec.split(MULTI_VALUE_DELIMITTER);

        for (String field : properties) {
            setAdditionalField(field, gelfMessageAssembler);
        }

    }

    public static void setAdditionalField(String fieldDefinition, GelfMessageAssembler gelfMessageAssembler) {
        if (fieldDefinition == null) {
            return;
        }

        final int index = fieldDefinition.indexOf(ConfigurationSupport.EQ);

        if (-1 == index) {
            return;
        }

        gelfMessageAssembler.addField(new StaticMessageField(fieldDefinition.substring(0, index), fieldDefinition.substring(index + 1)));
    }

    /**
     * Set the MDC fields.
     *
     * @param spec                 field, field2, field3
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setMdcFields(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] fields = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : fields) {
                gelfMessageAssembler.addField(new MdcMessageField(field.trim(), field.trim()));
            }
        }
    }

    /**
     * Set the dynamic MDC fields.
     *
     * @param spec                 field, .*FieldSuffix, fieldPrefix.*
     * @param gelfMessageAssembler the {@link GelfMessageAssembler}.
     */
    public static void setDynamicMdcFields(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] fields = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : fields) {
                gelfMessageAssembler.addField(new DynamicMdcMessageField(field.trim()));
            }
        }
    }

    /**
     * Set the additional field types.
     *
     * @param spec                 field=String,field1=Double, ... See {@link GelfMessage} for supported types.
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setAdditionalFieldTypes(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] properties = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : properties) {
                final int index = field.indexOf(EQ);
                if (-1 != index) {
                    gelfMessageAssembler.setAdditionalFieldType(field.substring(0, index), field.substring(index + 1));
                }
            }
        }
    }

    /**
     * Set the dynamic mdc field types.
     *
     * @param spec                 field=String,field1=Double, ... See {@link GelfMessage} for supported types.
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setDynamicMdcFieldTypes(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] properties = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : properties) {
                final int index = field.indexOf(EQ);
                if (-1 != index) {
                    gelfMessageAssembler.setDynamicMdcFieldType(field.substring(0, index), field.substring(index + 1));
                }
            }
        }
    }
}
