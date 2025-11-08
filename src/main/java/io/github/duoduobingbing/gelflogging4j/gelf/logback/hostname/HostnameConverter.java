package io.github.duoduobingbing.gelflogging4j.gelf.logback.hostname;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.duoduobingbing.gelflogging4j.RuntimeContainer;

/**
 * Provides the servername/Hostname.
 * Also see {@link io.github.duoduobingbing.gelflogging4j.gelf.log4j2.HostnameConverter Log4j2HostnameConverter} for supported options
 */
public class HostnameConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        final String opt = getFirstOption();

        return switch (opt) {
            case "simple" -> RuntimeContainer.HOSTNAME;
            case "address" -> RuntimeContainer.ADDRESS;
            case "fqdn" -> RuntimeContainer.FQDN_HOSTNAME;
            case null, default -> RuntimeContainer.FQDN_HOSTNAME;
        };
    }
}
