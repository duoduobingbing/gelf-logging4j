package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import org.apache.logging.log4j.core.layout.PatternLayout;

import io.github.duoduobingbing.gelflogging4j.gelf.LogMessageField;

/**
 * @author Mark Paluch
 */
public class PatternLogMessageField extends LogMessageField {

    private PatternLayout patternLayout;

    public PatternLogMessageField(String name, NamedLogField namedLogField, PatternLayout patternLayout) {
        super(name, namedLogField);
        this.patternLayout = patternLayout;
    }

    public PatternLayout getPatternLayout() {
        return patternLayout;
    }
}
