package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.github.duoduobingbing.gelflogging4j.gelf.logback.hostname.HostnamePatternLayout;

import java.util.function.Supplier;

class PatternLayoutHelper {

    private PatternLayoutHelper() {
    }

    /**
     * @see ch.qos.logback.classic.net.SMTPAppender#makeSubjectLayout(String)
     */
    private static <E extends PatternLayoutBase<ILoggingEvent>> E createPatternLayout(Supplier<E> layoutInstanceSupplier, String pattern, Context context, boolean autoStart) {
        final E layout = layoutInstanceSupplier.get();

        layout.setContext(context);
        layout.setPattern(pattern);
        // we don't want a ThrowableInformationConverter appended
        // to the end of the converter chain
        // This fixes issue LBCLASSIC-67
        layout.setPostCompileProcessor(null);
        if (autoStart) {
            layout.start();
        }
        return layout;
    }

     static PatternLayout createPatternLayout(String pattern, Context context, boolean autoStart){
        return createPatternLayout(PatternLayout::new, pattern, context, autoStart);
    }

     static HostnamePatternLayout createHostNamePatternLayout(String pattern, Context context, boolean autoStart){
        return createPatternLayout(HostnamePatternLayout::new, pattern, context, autoStart);
    }



}
