package io.github.duoduobingbing.gelflogging4j.gelf.logback.config;

public class MdcGelfLogField {

    //Optional - if fieldName is not set, mdcFieldName will be used as both value and fieldName
    String fieldName;

    String mdcFieldName;

    public String getFieldName() {
        return fieldName;
    }

    //config exposed
    public void setName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getMdcFieldName() {
        return mdcFieldName;
    }

    //config exposed
    public void setMdc(String mdcFieldName) {
        this.mdcFieldName = mdcFieldName;
    }
}
