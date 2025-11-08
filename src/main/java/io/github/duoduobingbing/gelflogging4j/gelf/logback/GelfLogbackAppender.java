package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.github.duoduobingbing.gelflogging4j.gelf.LogMessageField.NamedLogField;

import java.util.Collections;

import io.github.duoduobingbing.gelflogging4j.RuntimeContainer;
import io.github.duoduobingbing.gelflogging4j.gelf.LogMessageField;
import io.github.duoduobingbing.gelflogging4j.gelf.MdcGelfMessageAssembler;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.Closer;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.ConfigurationSupport;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderFactory;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.MessagePostprocessingErrorReporter;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Logback Handler creates GELF Messages and posts them using
 * UDP (default) or TCP. Following parameters are supported/needed:
 * <ul>
 * <li>host (Mandatory): Hostname/IP-Address of the Logstash Host
 * <ul>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * <li>See docs for more details</li>
 * </ul>
 * </li>
 * <li>port (Optional): Port, default 12201</li>
 * <li>version (Optional): GELF Version 1.0 or 1.1, default 1.0</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field (true/false/throwable reference [0 = throwable, 1 =
 * throwable.cause, -1 = root cause]), default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>includeLocation (Optional): Include source code location, default true</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>filter (Optional): logback filter (incl. log level)</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg.
 * .GelfLogHandler.additionalFields=fieldName=Value,field2=value2</li>
 * <li>additionalFieldTypes (Optional): Type specification for additional and MDC fields. Supported types: String, long, Long,
 * double, Double and discover (default if not specified, discover field type on parseability). Eg. field=String,field2=double</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * mdcFields=Application,Version,SomeOtherFieldName</li>
 * <li>dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular
 * expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name.</li>
 * <li>dynamicMdcFieldTypes (Optional): Pattern-based type specification for additional and MDC fields. Key-value pairs are
 * comma-separated. Supported types: String, long, Long, double, Double. Eg. my_field.*=String,business\..*\.field=double</li>
 * <li>includeFullMdc (Optional): Include all fields from the MDC, default false</li>
 * </ul>
 * <a name="mdcProfiling"></a> <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must
 * set one value in the MDC:
 * <ul>
 * <li>profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)</li>
 * </ul>
 * <p>
 * Two values are set by the Log Appender:
 * </p>
 * <ul>
 * <li>profiling.requestEnd: End-Time of the Request-End in Date.toString-representation</li>
 * <li>profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)</li>
 * </ul>
 * <p>
 * The {@link #append(ILoggingEvent)} method is thread-safe and may be called by different threads at any time.
 *
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
public class GelfLogbackAppender extends AppenderBase<ILoggingEvent> implements ErrorReporter {

    protected GelfSender gelfSender;
    protected MdcGelfMessageAssembler gelfMessageAssembler;
    private final ErrorReporter errorReporter = new MessagePostprocessingErrorReporter(this);
    protected PatternGelfLogFieldFactory patternGelfLogFieldFactory = new PatternGelfLogFieldFactory();

    protected boolean addDefaultFields = true;

    private void addDefaultFieldMappings() {
        gelfMessageAssembler.addFields(
                LogMessageField.getDefaultMapping(
                        NamedLogField.Time,
                        NamedLogField.Severity,
                        NamedLogField.ThreadName,
                        NamedLogField.SourceClassName,
                        NamedLogField.SourceMethodName,
                        NamedLogField.SourceLineNumber,
                        NamedLogField.SourceSimpleClassName,
                        NamedLogField.LoggerName,
                        NamedLogField.Marker
                )
        );
    }

    private void beforeStart() {
        final boolean noPatternLogFieldsSet = this.patternGelfLogFieldFactory.getPatternGelfLogFields().isEmpty();

        if (addDefaultFields || noPatternLogFieldsSet) {
            addDefaultFieldMappings();
        }

        if (noPatternLogFieldsSet) {
            return;
        }

        for (PatternGelfLogField patternLogFields : this.patternGelfLogFieldFactory.getPatternGelfLogFields()) {
            final PatternLayoutBase<ILoggingEvent> patternLayout;

            if (patternLogFields.isHostNameAware()) {
                patternLayout = PatternLayoutHelper.createHostNamePatternLayout(patternLogFields.getPattern(), context, true);
            } else {
                patternLayout = PatternLayoutHelper.createPatternLayout(patternLogFields.getPattern(), context, true);
            }

            gelfMessageAssembler.addField(new LogbackPatternLogMessageField(patternLogFields.getName(), null, patternLayout));
        }

    }

    public GelfLogbackAppender() {
        super();
        gelfMessageAssembler = new MdcGelfMessageAssembler();
    }


    @Override
    protected void append(ILoggingEvent event) {

        if (event == null) {
            return;
        }

        try {
            GelfMessage message = createGelfMessage(event);
            if (!message.isValid()) {
                reportError("GELF Message is invalid: " + message.toJson(), null);
                return;
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null);
            }
        } catch (Exception e) {
            reportError("Could not send GELF message: " + e.getMessage(), e);
        }
    }

    @Override
    public void start() {

        if (null == gelfSender) {
            RuntimeContainer.initialize(errorReporter);
            gelfSender = createGelfSender();
        }

        this.beforeStart();

        super.start();
    }

    @Override
    public void stop() {

        if (null != gelfSender) {
            Closer.close(gelfSender);
            gelfSender = null;
        }

        super.stop();
    }

    protected GelfSender createGelfSender() {
        return GelfSenderFactory.createSender(gelfMessageAssembler, errorReporter, Collections.<String, Object>emptyMap());
    }

    @Override
    public void reportError(String message, Exception exception) {
        addError(message, exception);
    }

    protected GelfMessage createGelfMessage(final ILoggingEvent loggingEvent) {
        return gelfMessageAssembler.createGelfMessage(new LogbackLogEvent(loggingEvent));
    }

    //Exposed to config
    public void setPatternLogFields(PatternGelfLogFieldFactory patternGelfLogFieldFactory) {
        this.patternGelfLogFieldFactory = patternGelfLogFieldFactory;
    }

    //Exposed to config
    public void setAddDefaultFields(boolean addDefaultFields) {
        this.addDefaultFields = addDefaultFields;
    }

    //Exposed to config
    public void setAdditionalFields(String spec) {
        ConfigurationSupport.setAdditionalFields(spec, gelfMessageAssembler);
    }

    //Exposed to config
    public void setAdditionalFieldTypes(String spec) {
        ConfigurationSupport.setAdditionalFieldTypes(spec, gelfMessageAssembler);
    }

    //Exposed to config
    public void setMdcFields(String spec) {
        ConfigurationSupport.setMdcFields(spec, gelfMessageAssembler);
    }

    //Exposed to config
    public void setDynamicMdcFields(String spec) {
        ConfigurationSupport.setDynamicMdcFields(spec, gelfMessageAssembler);
    }

    //Exposed to config
    public void setDynamicMdcFieldTypes(String spec) {
        ConfigurationSupport.setDynamicMdcFieldTypes(spec, gelfMessageAssembler);
    }

    public String getGraylogHost() {
        return gelfMessageAssembler.getHost();
    }

    //Exposed to config
    public void setGraylogHost(String graylogHost) {
        gelfMessageAssembler.setHost(graylogHost);
    }

    public String getOriginHost() {
        return gelfMessageAssembler.getOriginHost();
    }

    //Exposed to config
    public void setOriginHost(String originHost) {
        gelfMessageAssembler.setOriginHost(originHost);
    }

    public int getGraylogPort() {
        return gelfMessageAssembler.getPort();
    }

    //Exposed to config
    public void setGraylogPort(int graylogPort) {
        gelfMessageAssembler.setPort(graylogPort);
    }

    public String getHost() {
        return gelfMessageAssembler.getHost();
    }

    //Exposed to config
    public void setHost(String host) {
        gelfMessageAssembler.setHost(host);
    }

    public int getPort() {
        return gelfMessageAssembler.getPort();
    }

    //Exposed to config
    public void setPort(int port) {
        gelfMessageAssembler.setPort(port);
    }

    public String getFacility() {
        return gelfMessageAssembler.getFacility();
    }

    //Exposed to config
    public void setFacility(String facility) {
        gelfMessageAssembler.setFacility(facility);
    }

    public String getExtractStackTrace() {
        return gelfMessageAssembler.getExtractStackTrace();
    }

    //Exposed to config
    public void setExtractStackTrace(String extractStacktrace) {
        gelfMessageAssembler.setExtractStackTrace(extractStacktrace);
    }

    public boolean isFilterStackTrace() {
        return gelfMessageAssembler.isFilterStackTrace();
    }

    //Exposed to config
    public void setFilterStackTrace(boolean filterStackTrace) {
        gelfMessageAssembler.setFilterStackTrace(filterStackTrace);
    }

    public boolean isIncludeLocation() {
        return gelfMessageAssembler.isIncludeLocation();
    }

    //Exposed to config
    public void setIncludeLocation(boolean includeLocation) {
        gelfMessageAssembler.setIncludeLocation(includeLocation);
    }

    public boolean isMdcProfiling() {
        return gelfMessageAssembler.isMdcProfiling();
    }

    //Exposed to config
    public void setMdcProfiling(boolean mdcProfiling) {
        gelfMessageAssembler.setMdcProfiling(mdcProfiling);
    }

    public String getTimestampPattern() {
        return gelfMessageAssembler.getTimestampPattern();
    }

    //Exposed to config
    public void setTimestampPattern(String timestampPattern) {
        gelfMessageAssembler.setTimestampPattern(timestampPattern);
    }

    public int getMaximumMessageSize() {
        return gelfMessageAssembler.getMaximumMessageSize();
    }

    //Exposed to config
    public void setMaximumMessageSize(int maximumMessageSize) {
        gelfMessageAssembler.setMaximumMessageSize(maximumMessageSize);
    }

    public boolean isIncludeFullMdc() {
        return gelfMessageAssembler.isIncludeFullMdc();
    }

    //Exposed to config
    public void setIncludeFullMdc(boolean includeFullMdc) {
        gelfMessageAssembler.setIncludeFullMdc(includeFullMdc);
    }

    public String getVersion() {
        return gelfMessageAssembler.getVersion();
    }

    //Exposed to config
    public void setVersion(String version) {
        gelfMessageAssembler.setVersion(version);
    }

}
