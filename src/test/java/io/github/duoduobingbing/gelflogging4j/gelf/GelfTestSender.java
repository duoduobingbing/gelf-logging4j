package io.github.duoduobingbing.gelflogging4j.gelf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;

/**
 * @author Mark Paluch
 * @since 27.09.13 07:45
 */
public class GelfTestSender implements GelfSender {
    private static List<GelfMessage> messages = new CopyOnWriteArrayList<>();

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
        synchronized (messages) {
            return messages;
        }
    }
}
