java.util.logging
=========

Following settings can be used:

| Attribute Name    | Description                          | Default |
| ----------------- |:------------------------------------:|:-------:|
| host              | Hostname/IP-Address of the Logstash host. The `host` field accepts following forms: <ul><li>`tcp:hostname` for TCP transport, e. g. `tcp:127.0.0.1` or `tcp:some.host.com` </li><li>`udp:hostname` for UDP transport, e. g. `udp:127.0.0.1`, `udp:some.host.com` or just `some.host.com`  </li><li>`redis://[:password@]hostname:port/db-number#listname` for Redis transport. See [Redis transport for logstash-gelf](../redis.html) for details. </li><li>`redis-sentinel://[:password@]hostname:port/db-number?masterId=masterId#listname` for Redis transport with Sentinel lookup. See [Redis transport for logstash-gelf](../redis.html) for details. </li></ul> | none | 
| port              | Port of the Logstash host  | `12201` |
| version           | GELF Version `1.0` or `1.1` | `1.0` |
| originHost        | Originating Hostname  | FQDN Hostname |
| extractStackTrace | Send the Stack-Trace to the StackTrace field (`true`/`false`)  | `false` |
| filterStackTrace  | Perform Stack-Trace filtering (`true`/`false`)| `false` |
| includeLogMessageParameters | Include message parameters from the log event | `true` |
| includeLocation   | Include source code location | `true` |
| facility          | Name of the Facility  | `logstash-gelf` |
| level             | Log-Level threshold | `INFO` |
| filter            | Class-Name of a Log-Filter  | none |
| additionalFields  | Send additional static fields. The fields are specified as key-value pairs are comma-separated. Example: `additionalFields=fieldName=Value,fieldName2=Value2` | none |
| additionalFieldTypes | Type specification for additional fields. Supported types: `String`, `long`, `Long`, `double`, `Double` and `discover` (default if not specified, discover field type on parseability). Eg. field=String,field2=double | `discover` for all additional fields |
| maximumMessageSize| Maximum message size (in bytes). If the message size is exceeded, the appender will submit the message in multiple chunks. | `8192` |
| timestampPattern  | Date/time pattern for the `Time` field| `yyyy-MM-dd HH:mm:ss,SSS` |

The only mandatory field is `host`. All other fields are optional. 
If a message carries params (used to substitute resource bundle argument placeholders), 
the params are exposed as individual fields prefixed with `MessageParam`.

Please note: The logging Jar files need to be on the boot class path, else JUL won't load the handler. 

Glassfish/Payara configuration
-------------
You need to install the library with its dependencies (see download above) in Glassfish/Payara. Place it below the `$GFHOME/glassfish/domains/$YOURDOMAIN/lib/ext/` path, then configure your `logging.properties` file.

Java Util Logging Configuration
--------------

Simple Configuration:

    handlers = jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler, java.util.logging.ConsoleHandler

    .handlers = jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler, java.util.logging.ConsoleHandler
    .level = INFO

    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.host=udp:localhost
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.port=12201
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.level=INFO

Extended Properties:

    handlers = jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler, java.util.logging.ConsoleHandler

    .handlers = jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler, java.util.logging.ConsoleHandler
    .level = INFO

    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.host=udp:localhost
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.port=12201
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.version=1.0
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.facility=gelf-logging4j
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.extractStackTrace=true
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.filterStackTrace=true
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.includeLogMessageParameters=true
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.includeLocation=true
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.timestampPattern=yyyy-MM-dd HH:mm:ss,SSS
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.maximumMessageSize=8192
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.additionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.additionalFieldTypes=fieldName1=String,fieldName2=Double,fieldName3=Long
    jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.level=INFO
