package io.github.duoduobingbing.gelflogging4j.gelf;

import java.util.ArrayList;
import java.util.List;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;

/**
 * @author Mark Paluch
 * @since 27.09.13 07:45
 */
public class GelfTestSender implements GelfSender {
    private static List<GelfMessage> messages = new ArrayList<>();

    @Override
    public boolean sendMessage(GelfMessage message) {
        synchronized (messages) {
            messages.add(message);
        }
        return true;
    }

    @Override
    public void close() {

    }

    public static List<GelfMessage> getMessages() {
        return messages;
    }
}
