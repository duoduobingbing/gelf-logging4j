package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import java.util.ArrayList;

public class PatternGelfLogFieldFactory {

    final ArrayList<PatternGelfLogField> patternGelfLogFields = new ArrayList<>();

    public PatternGelfLogFieldFactory() {
    }

    //config exposed
    public void addPatternLogField(PatternGelfLogField patternGelfLogField){
        patternGelfLogFields.add(patternGelfLogField);
    }

    public ArrayList<PatternGelfLogField> getPatternGelfLogFields() {
        return patternGelfLogFields;
    }
}
