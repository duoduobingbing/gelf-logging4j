package io.github.duoduobingbing.gelflogging4j.gelf.logback.config;

import java.util.ArrayList;

public class AdditionalGelfFieldsFactory {

    final ArrayList<PatternGelfLogField> patternGelfLogFields = new ArrayList<>();
    final ArrayList<StaticGelfLogField> staticGelfLogFields = new ArrayList<>();
    final ArrayList<MdcGelfLogField> mdcGelfLogFields = new ArrayList<>();

    public AdditionalGelfFieldsFactory() {
    }

    //config exposed
    public void addPatternLogField(PatternGelfLogField patternGelfLogField){
        patternGelfLogFields.add(patternGelfLogField);
    }

    //config exposed
    public void addStaticLogField(StaticGelfLogField staticGelfLogField){
        staticGelfLogFields.add(staticGelfLogField);
    }

    //config exposed
    public void addMdcLogField(MdcGelfLogField mdcGelfLogField){
        mdcGelfLogFields.add(mdcGelfLogField);
    }

    public ArrayList<PatternGelfLogField> getPatternGelfLogFields() {
        return patternGelfLogFields;
    }

    public ArrayList<StaticGelfLogField> getStaticGelfLogFields() {
        return staticGelfLogFields;
    }

    public ArrayList<MdcGelfLogField> getMdcGelfLogFields() {
        return mdcGelfLogFields;
    }
}
