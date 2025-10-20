package io.github.duoduobingbing.gelflogging4j.gelf;

import java.io.IOException;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderConfiguration;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderProvider;

/**
 * @author Mark Paluch
 */
public class GelfTestSenderProvider implements GelfSenderProvider {

    @Override
    public boolean supports(String host) {
        return host.startsWith("test:");
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        return new GelfTestSender();
    }

}
