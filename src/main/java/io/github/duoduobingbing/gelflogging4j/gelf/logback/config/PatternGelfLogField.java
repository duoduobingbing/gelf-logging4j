package io.github.duoduobingbing.gelflogging4j.gelf.logback.config;

/**
 * @see <a href="https://logback.qos.ch/manual/layouts.html#conversionWord">Logback Patterns</a>
 */
public class PatternGelfLogField {

    String pattern;

    String name;

    boolean hostNameAware = false;

    //config exposed
    public void setPattern(String pattern){
        this.pattern = pattern;
    }

    //config exposed
    public void setName(String name) {
        this.name = name;
    }

    //config exposed
    public void setHostNameAware(boolean hostNameAware) {
        this.hostNameAware = hostNameAware;
    }

    public String getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public boolean isHostNameAware() {
        return hostNameAware;
    }

}
