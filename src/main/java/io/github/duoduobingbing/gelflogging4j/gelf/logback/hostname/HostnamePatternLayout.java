package io.github.duoduobingbing.gelflogging4j.gelf.logback.hostname;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.EnsureExceptionHandling;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.PatternLayoutBase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class HostnamePatternLayout extends PatternLayoutBase<ILoggingEvent> {


    @SuppressWarnings("rawtypes")
    public static final Map<String, Supplier<DynamicConverter>> ALL_CONVERTER_SUPPLIER_MAP = new HashMap<>();

    static{

        ALL_CONVERTER_SUPPLIER_MAP.putAll(PatternLayout.DEFAULT_CONVERTER_SUPPLIER_MAP);
        ALL_CONVERTER_SUPPLIER_MAP.put("host", HostnameConverter::new);

    }

    /**
     * @see PatternLayout#PatternLayout()
     */
    public HostnamePatternLayout() {
        this.postCompileProcessor = new EnsureExceptionHandling();
    }

    /**
     * @see PatternLayout#getDefaultConverterSupplierMap()
     * @return a map of keys and class instances by supplier
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Map<String, Supplier<DynamicConverter>> getDefaultConverterSupplierMap() {
        return ALL_CONVERTER_SUPPLIER_MAP;
    }

    /**
     * This will be completely removed in a later version.
     * @deprecated Use {@link #getDefaultConverterSupplierMap()} instead.
     * Currently kept for backwards compatibility with Logback Classic 1.5.x
     * @return {@link Map}
     */
    @Deprecated(forRemoval = true)
    public Map<String, String> getDefaultConverterMap() {
        return Map.of();
        //return ALL_CONVERTERS_MAP;
    }



    /**
     * @see PatternLayout#doLayout(ILoggingEvent)
     * @param event ILoggingEvent
     * @return String
     */
    @Override
    public String doLayout(ILoggingEvent event) {
        if (!isStarted()) {
            return CoreConstants.EMPTY_STRING;
        }
        return writeLoopOnConverters(event);
    }
}
