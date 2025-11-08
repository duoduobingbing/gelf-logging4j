package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.github.duoduobingbing.gelflogging4j.gelf.LogMessageField;

public class LogbackPatternLogMessageField extends LogMessageField {

    private PatternLayoutBase<ILoggingEvent> patternLayout;

    public LogbackPatternLogMessageField(String name, NamedLogField namedLogField, PatternLayoutBase<ILoggingEvent> patternLayout) {
        super(name, namedLogField);
        this.patternLayout = patternLayout;
    }

    public PatternLayoutBase<ILoggingEvent> getPatternLayout() {
        return patternLayout;
    }
}
