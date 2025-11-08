package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestErrorRecordingGelfLogHandler extends GelfLogHandler {
    static record RecordedError(String message, Exception exception){}

    final List<RecordedError> recordedErrors = new CopyOnWriteArrayList<>();

    @Override
    public void reportError(String message, Exception e) {
        recordedErrors.add(new RecordedError(message,e));
        super.reportError(message, e);
    }

}
