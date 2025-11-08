package io.github.duoduobingbing.gelflogging4j;

import java.util.Arrays;
import java.util.List;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StackTraceFilterUnitTests {

    @BeforeEach
    void before() throws Exception {
        StackTraceFilter.loadSetttings(StackTraceFilter.FILTER_SETTINGS);
    }

    @Test
    void testNull() throws Exception {
        StackTraceFilter.loadSetttings("nonexistent");
    }

    @Test
    void testOwnProperties() throws Exception {
        StackTraceFilter.loadSetttings("StackTraceFilterTest.properties");
    }

    @Test
    void testFindThrowable() {

        AssertJAssertions.assertThat(StackTraceFilter.getThrowable(entryMethod(), 0)).isExactlyInstanceOf(RuntimeException.class);
        AssertJAssertions.assertThat(StackTraceFilter.getThrowable(entryMethod(), 1)).isExactlyInstanceOf(MyException.class);
        AssertJAssertions.assertThat(StackTraceFilter.getThrowable(entryMethod(), 3)).isExactlyInstanceOf(IllegalStateException.class);
        AssertJAssertions.assertThat(StackTraceFilter.getThrowable(entryMethod(), -1)).isExactlyInstanceOf(IllegalStateException.class);

        AssertJAssertions.assertThat(StackTraceFilter.getThrowable(entryMethod(), -10)).isExactlyInstanceOf(RuntimeException.class);
        AssertJAssertions.assertThat(StackTraceFilter.getThrowable(entryMethod(), 10)).isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void filterWholeStackTrace() {

        String filteredStackTrace = StackTraceFilter.getFilteredStackTrace(entryMethod());
        List<String> lines = Arrays.asList(filteredStackTrace.split(System.getProperty("line.separator")));

        AssertJAssertions.assertThat(lines).contains("\tSuppressed: java.lang.RuntimeException: suppressed");
        AssertJAssertions.assertThat(lines).contains("\t\tCaused by: java.lang.NumberFormatException: For input string: \"text\"");
    }

    @Test
    void getStackTrace() {

        String plainStackTrace = StackTraceFilter.getStackTrace(entryMethod());
        List<String> lines = Arrays.asList(plainStackTrace.split(System.getProperty("line.separator")));

        AssertJAssertions.assertThat(lines).contains("\tSuppressed: java.lang.RuntimeException: suppressed");
        AssertJAssertions.assertThat(lines).contains("\t\tCaused by: java.lang.NumberFormatException: For input string: \"text\"");
        AssertJAssertions.assertThat(lines).doesNotContain("\t\t\t\t\t1 line skipped for [org.jboss]");
    }

    @Test
    void printStackTraceRef2() {

        String plainStackTrace = StackTraceFilter.getStackTrace(entryMethod(), 2);
        List<String> lines = Arrays.asList(plainStackTrace.split(System.getProperty("line.separator")));

        AssertJAssertions. assertThat(lines).containsSequence(
                "java.lang.RuntimeException: entryMethod",
                "Caused by: io.github.duoduobingbing.gelflogging4j.StackTraceFilterUnitTests$MyException: Intermediate 2"
        );

        AssertJAssertions.assertThat(lines).doesNotContain("\t\tCaused by: java.lang.NumberFormatException: For input string: \"text\"");
        AssertJAssertions.assertThat(lines).doesNotContain("\tCaused by: java.lang.NumberFormatException: For input string: \"text\"");
        AssertJAssertions.assertThat(lines).contains("\tSuppressed: java.lang.IllegalStateException: Some illegal state");
        AssertJAssertions.assertThat(lines).doesNotContain("\t\t\t\t\t1 line skipped for [org.jboss]");
    }

    @Test
    void filterRootCause() {

        String filteredStackTrace = StackTraceFilter.getFilteredStackTrace(entryMethod(), -1);
        List<String> lines = Arrays.asList(filteredStackTrace.split(System.getProperty("line.separator")));

        AssertJAssertions.assertThat(filteredStackTrace).doesNotContain("NumberFormatException");

        AssertJAssertions.assertThat(lines).containsSequence(
                "java.lang.RuntimeException: entryMethod",
                "Caused by: io.github.duoduobingbing.gelflogging4j.StackTraceFilterUnitTests$MyException: Intermediate 2",
                "Caused by: io.github.duoduobingbing.gelflogging4j.StackTraceFilterUnitTests$MyException: Message",
                "Caused by: java.lang.IllegalStateException: Some illegal state"
        );
    }

    private RuntimeException entryMethod() {
        return new RuntimeException("entryMethod", intermediate1());
    }

    private Exception intermediate1() {
        return intermediate2();
    }

    private Exception intermediate2() {

        MyException myException = new MyException("Intermediate 2", intermediate3());
        RuntimeException suppressed1 = new RuntimeException("suppressed");
        RuntimeException suppressed2 = new RuntimeException("suppressed");

        suppressed1.addSuppressed(suppressed2);

        try {
            throw new IllegalArgumentException(new NumberFormatException("For input string: \"text\""));
        } catch (Exception e) {
            suppressed1.addSuppressed(e);
        }
        myException.addSuppressed(suppressed1);

        return myException;
    }

    private Exception intermediate3() {

        MyException myException = new MyException("Message", cause());
        myException.addSuppressed(cause());
        myException.addSuppressed(cause());
        return myException;
    }

    private Exception cause() {
        return new IllegalStateException("Some illegal state");
    }

    static class MyException extends RuntimeException {
        MyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
