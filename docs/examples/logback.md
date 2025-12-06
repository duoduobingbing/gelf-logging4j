logback
=========

Following settings can be used:

| Attribute Name             | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |               Default                |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------:|
| host                       | Hostname/IP-Address of the Logstash host. The `host` field accepts following forms: <ul><li>`tcp:hostname` for TCP transport, e. g. `tcp:127.0.0.1` or `tcp:some.host.com` </li><li>`udp:hostname` for UDP transport, e. g. `udp:127.0.0.1`, `udp:some.host.com` or just `some.host.com`  </li><li>`redis://[:password@]hostname:port/db-number#listname` for Redis transport. See [Redis transport for logstash-gelf](../redis.html) for details. </li><li>`redis-sentinel://[:password@]hostname:port/db-number?masterId=masterId#listname` for Redis transport with Sentinel lookup. See [Redis transport for logstash-gelf](../redis.html) for details. </li></ul> |                 none                 | 
| port                       | Port of the Logstash host                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |               `12201`                |
| version                    | GELF Version `1.0` or `1.1`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |                `1.0`                 |
| originHost                 | Originating Hostname                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |            FQDN Hostname             |
| extractStackTrace          | Send the Stack-Trace to the StackTrace field (`true`/`false`)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |               `false`                |
| filterStackTrace           | Perform Stack-Trace filtering (`true`/`false`)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |               `false`                |
| includeLocation            | Include source code location                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |                `true`                |
| facility                   | Name of the Facility                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |           `logstash-gelf`            |
| additionalLogFields        | Send additional fields whose values are obtained from MDC, as literal or via a pattern that is resolved by Logback.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |                 none                 |
| mdcProfiling               | Perform Profiling (Call-Duration) based on MDC Data. See [MDC Profiling](../mdcprofiling.html) for details                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |               `false`                |
| additionalFieldTypes       | Type specification for additional and MDC fields. Supported types: `String`, `long`, `Long`, `double`, `Double` and `discover` (default if not specified, discover field type on parseability). Eg. field=String,field2=double                                                                                                                                                                                                                                                                                                                                                                                                                                         | `discover` for all additional fields |
| dynamicMdcFields           | Dynamic MDC Fields allows you to extract MDC values based on one or more regular expressions. Multiple regexes are comma-separated. The name of the MDC entry is used as GELF field name.                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |                 none                 |
| dynamicMdcFieldTypes       | Pattern-based type specification for additional and MDC fields. Key-value pairs are comma-separated. Example: `my_field.*=String,business\..*\.field=double`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |                 none                 |
| includeFullMdc             | Include all fields from the MDC.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |               `false`                |
| maximumMessageSize         | Maximum message size (in bytes). If the message size is exceeded, the appender will submit the message in multiple chunks.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |                `8192`                |
| timestampPattern           | Date/time pattern for the `Time` field                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |      `yyyy-MM-dd HH:mm:ss,SSS`       |
| addAdditionalDefaultFields | Whether the additional default fields `Time`, `Severity`, `Thread`, `SourceClassName`, `SourceMethodName`, `SourceLineNumber`, `SourceSimpleClassName`, `LoggerName`, `Marker` should be added.                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |                `true`                |

The only mandatory field is `host`. All other fields are optional.

Please note: If the `debug` attribute of the `configuration` element is not set to `true`, internal appender errors are not shown.

### Fields

Logback supports an extensive and flexible configuration in contrast to other log frameworks (JUL, log4j v1). This allows you to specify your needed fields you want to use in the GELF message. An empty field configuration results in a message containing only

* timestamp
* level (syslog level)
* host
* facility
* message
* short_message

You can add different fields:

* Static Literals
* MDC Fields
* Log-Event fields (using Pattern Layout)

In order to do so, use nested Field elements below the `<additionalLogFields>` element.

### Static Literals

```xml
<staticLogField>
    <name>fieldName1</name>
    <literal>your literal value</literal>
</staticLogField>
```

> *Note:*<br>
> You can use Logback's [variable substitution](https://logback.qos.ch/manual/configuration.html#variableSubstitution) inside a literal. So:
> ```xml
> <staticLogField>
>     <name>fieldName1</name>
>     <literal>${HOSTNAME}</literal>
> </staticLogField>
> ```
> works too.

### MDC Fields

```xml
<mdcLogField>
    <name>fieldName1</name>
    <mdc>name of the MDC entry</literal>
</mdcLogField>
```

The name is optional. If you do not specify `name`, `mdc` will also be used as the name of the field.

### Dynamic MDC Fields

In contrast to the configuration of other log frameworks Logback config uses one `dynamicMdcFields` element (all values are separated by comma).

See table above.

### Dynamic Field Typing

In some cases, it's required to use a fixed type for fields transported using GELF. MDC is a
dynamic value source and since types can vary, so also data types in the GELF JSON vary. You can define
`dynamicMdcFieldTypes` rules to declare types with Regex `Pattern`-based rules.

See table above.

### Log-Event fields

See also: [Pattern Layout](https://logback.qos.ch/manual/layouts.html#conversionWord)

Set the desired pattern and the field will be sent using the specified pattern value.

Additionally, you can add the host-Field, if `hostNameAware` is set to `true`, which can supply you either the FQDN hostname, the simple hostname or the local address.

| Option                                    | Description                                                                                                                                                                                                                                                                                                                                                                                                                   |
|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| host{["fqdn"<br/>"simple"<br/>"address"]} | Outputs either the FQDN hostname, the simple hostname or the local address. You can follow the throwable conversion word with an option in the form %host{option}. <br/> %host{fqdn} default setting, outputs the FQDN hostname, e.g. www.you.host.name.com. <br/>%host{simple} outputs simple hostname, e.g. www. <br/>%host{address} outputs the local IP address of the found hostname, e.g. 1.2.3.4 or affe:affe:affe::1. |

```xml
<patternLogField>
    <name>fieldName1</name>
    <pattern>%host{'fqdn'} your other text %C</pattern>
    <hostNameAware>true</hostNameAware>
</patternLogField>
```

Logback Configuration
--------------

<tt>logback.xml</tt> Example:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration>

<configuration>
    <contextName>test</contextName>
    <jmxConfigurator/>

    <appender name="gelf" class="io.github.duoduobingbing.gelflogging4j.gelf.logback.GelfLogbackAppender">
        <host>udp:localhost</host>
        <port>12201</port>
        <version>1.0</version>
        <facility>gelf-logging4j</facility>
        <extractStackTrace>true</extractStackTrace>
        <filterStackTrace>true</filterStackTrace>
        <includeLocation>true</includeLocation>
        <mdcProfiling>true</mdcProfiling>
        <timestampPattern>yyyy-MM-dd HH:mm:ss,SSS</timestampPattern>

        <additionalLogFields>
            <!-- These are static fields -->
            <staticLogField>
                <name>fieldName1</name>
                <literal>fieldValue1</literal>
            </staticLogField>
            <staticLogField>
                <name>fieldName2</name>
                <literal>3.0</literal>
            </staticLogField>
            <staticLogField>
                <name>fieldName3</name>
                <literal>25</literal>
            </staticLogField>
            <!-- These are fields using MDC -->
            <mdcLogField>
                <mdc>mdcField1</mdc>
            </mdcLogField>
            <mdcLogField>
                <mdc>mdcField2</mdc>
            </mdcLogField>
            <!-- These are fields using a pattern -->
            <patternLogField>
                <name>levelName</name>
                <pattern>%level</pattern>
            </patternLogField>
            <patternLogField>
                <name>customMessageWithHost</name>
                <pattern>%host{'simple'}: %m</pattern>
                <hostNameAware>true</hostNameAware>
            </patternLogField>
        </additionalLogFields>
        
        <maximumMessageSize>8192</maximumMessageSize>
        <additionalFieldTypes>fieldName1=String,fieldName2=Double,fieldName3=Long</additionalFieldTypes>
        <dynamicMdcFields>myMdc.*,[a-z]+Field</dynamicMdcFields>
        <dynamicMdcFieldTypes>my_field.*=String,business\..*\.field=double</dynamicMdcFieldTypes>
        <includeFullMdc>true</includeFullMdc>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
    </appender>

    <root level="DEBUG">
        <appender-ref ref="gelf" />
    </root>
</configuration>
```