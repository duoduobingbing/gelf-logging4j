package io.github.duoduobingbing.gelflogging4j.gelf.logback.config;

public class StaticGelfLogField {

    String name;

    String literal;


    public String getName() {
        return name;
    }

    //config exposed
    public void setName(String name) {
        this.name = name;
    }

    public String getLiteral() {
        return literal;
    }

    //config exposed
    public void setLiteral(String value) {
        this.literal = value;
    }
}
