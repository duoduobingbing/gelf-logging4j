package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;

import java.net.URI;

public class TestRedisGelfSenderProvider extends RedisGelfSenderProvider {

    //Change visibility in tests from package-private to protected, so that we can override the method to grab the inputs
    @Override
    protected GelfREDISSender createSenderInternal(URI hostUri, int port, ErrorReporter errorReporter) {
        return super.createSenderInternal(hostUri, port, errorReporter);
    }
}
