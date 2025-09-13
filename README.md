# gelf-logging4j
              

**Fork of the popular now sunset [logstash-gelf](https://github.com/mp911de/logstash-gelf) by the amazing [mp911de](https://github.com/mp911de)**


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.duoduobingbing/gelf-logging4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.duoduobingbing/gelf-logging4j)

Provides logging functionality using the Graylog Extended Logging Format ([GELF](http://www.graylog2.org/resources/gelf/specification) 1.0 and 1.1) for using with:

* [Java Util Logging](#java-util-logging-gelf-configuration)
* [log4j 2.x](#log4j2-gelf-configuration)
* [Logback](#logback-gelf-configuration)

`gelf-logging4j` requires as of version 2.0.0 Java 21 or higher. 
If you require an older Java version, please use the original `logstash-gelf`.
See also [our docs](https://github.com/duoduobingbing/gelf-logging4j/tree/master/docs) or [the Graylog GELF specification](http://www.graylog2.org/resources/gelf/specification) for further documentation.


Including it in your project
--------------

Maven:
```xml
<dependency>
    <groupId>io.github.duoduobingbing</groupId>
    <artifactId>gelf-logging4j</artifactId>
    <version>x.y.z</version>
</dependency>
```
    
Direct download from [Maven Central](http://search.maven.org/remotecontent?filepath=io/github/duoduobingbing/gelf-logging4j/2.0.0/gelf-logging4j-2.0.0.jar)    



<a name="jul"/>

## Java Util Logging GELF configuration

**Properties**

```properties
handlers = jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler, java.util.logging.ConsoleHandler

.handlers = jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler, java.util.logging.ConsoleHandler
.level = INFO

jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.host=udp:localhost
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.port=12201
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.version=1.1
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.facility=java-test
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.extractStackTrace=true
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.filterStackTrace=true
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.timestampPattern=yyyy-MM-dd HH:mm:ss,SSS
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.maximumMessageSize=8192

# This are static fields
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.additionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
# Optional: Specify field types
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.additionalFieldTypes=fieldName1=String,fieldName2=Double,fieldName3=Long
jul.gelf.io.github.duoduobingbing.gelflogging4j.GelfLogHandler.level=INFO
```

<a name="log4j2"/>

## log4j2 GELF configuration

### Fields

Log4j v2 supports an extensive and flexible configuration in contrast to other log frameworks (JUL, log4j v1). This allows you to specify your needed fields you want to use in the GELF message. An empty field configuration results in a message containing only

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

In order to do so, use nested Field elements below the Appender element.

### Static Literals

```xml
<Field name="fieldName1" literal="your literal value" />
```
    
### MDC Fields

```xml
<Field name="fieldName1" mdc="name of the MDC entry" />
```

### Dynamic MDC Fields

```xml
<DynamicMdcFields regex="mdc.*" />
```

In contrast to the configuration of other log frameworks log4j2 config uses one `DynamicMdcFields` element per regex (not separated by comma).

### Log-Event fields

See also: [Pattern Layout](http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout)

Set the desired pattern and the field will be sent using the specified pattern value. 

Additionally, you can add the host-Field, which can supply you either the FQDN hostname, the simple hostname or the local address.

Option | Description
--- | ---
host{["fqdn"<br/>"simple"<br/>"address"]} | Outputs either the FQDN hostname, the simple hostname or the local address. You can follow the throwable conversion word with an option in the form %host{option}. <br/> %host{fqdn} default setting, outputs the FQDN hostname, e.g. www.you.host.name.com. <br/>%host{simple} outputs simple hostname, e.g. www. <br/>%host{address} outputs the local IP address of the found hostname, e.g. 1.2.3.4 or affe:affe:affe::1. 

**XML**

```xml    
<Configuration packages="biz.paluch.logging.gelf.log4j2">
    <Appenders>
        <Gelf name="gelf" host="udp:localhost" port="12201" version="1.1" extractStackTrace="true"
              filterStackTrace="true" mdcProfiling="true" includeFullMdc="true" maximumMessageSize="8192"
              originHost="%host{fqdn}" additionalFieldTypes="fieldName1=String,fieldName2=Double,fieldName3=Long">
            <Field name="timestamp" pattern="%d{dd MMM yyyy HH:mm:ss,SSS}" />
            <Field name="level" pattern="%level" />
            <Field name="simpleClassName" pattern="%C{1}" />
            <Field name="className" pattern="%C" />
            <Field name="server" pattern="%host" />
            <Field name="server.fqdn" pattern="%host{fqdn}" />
            
            <!-- This is a static field -->
            <Field name="fieldName2" literal="fieldValue2" />
             
            <!-- This is a field using MDC -->
            <Field name="mdcField2" mdc="mdcField2" /> 
            <DynamicMdcFields regex="mdc.*" />
            <DynamicMdcFields regex="(mdc|MDC)fields" />
            <DynamicMdcFieldType regex="my_field.*" type="String" />
        </Gelf>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="gelf" />
        </Root>
    </Loggers>
</Configuration>    
```    

**YAML**

```yaml
rootLogger:
    level: INFO
    appenderRef.gelf.ref: GelfAppender

appender.gelf:
    type: Gelf
    name: GelfAppender
    host: udp:localhost
    port: 12201
    version: 1.0
    includeFullMdc: true
    mdcProfiling: true
    maximumMessageSize: 32768
    dynamicMdcFields:
        type: DynamicMdcFields
        regex: "mdc.*,(mdc|MDC)fields"
    field:
        - name: fieldName2
          literal: fieldName2 # This is a static field
        - name: className
          pattern: "%C"
        - name: lineNumber
          pattern: "%line"
```

<a name="logback"/>

Logback GELF configuration
--------------------------
logback.xml Example:

```xml
<!DOCTYPE configuration>

<configuration>
    <contextName>test</contextName>
    <jmxConfigurator/>

    <appender name="gelf" class="io.github.duoduobingbing.gelflogging4j.gelf.logback.GelfLogbackAppender">
        <host>udp:localhost</host>
        <port>12201</port>
        <version>1.1</version>
        <facility>java-test</facility>
        <extractStackTrace>true</extractStackTrace>
        <filterStackTrace>true</filterStackTrace>
        <mdcProfiling>true</mdcProfiling>
        <timestampPattern>yyyy-MM-dd HH:mm:ss,SSS</timestampPattern>
        <maximumMessageSize>8192</maximumMessageSize>

        <!-- This are static fields -->
        <additionalFields>fieldName1=fieldValue1,fieldName2=fieldValue2</additionalFields>
        <!-- Optional: Specify field types -->
        <additionalFieldTypes>fieldName1=String,fieldName2=Double,fieldName3=Long</additionalFieldTypes>

        <!-- This are fields using MDC -->
        <mdcFields>mdcField1,mdcField2</mdcFields>
        <dynamicMdcFields>mdc.*,(mdc|MDC)fields</dynamicMdcFields>
        <dynamicMdcFieldTypes>my_field.*=String,business\..*\.field=double</dynamicMdcFieldTypes>
        <includeFullMdc>true</includeFullMdc>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
    </appender>

    <root level="DEBUG">
        <appender-ref ref="gelf"/>
    </root>
</configuration>
```

License
-------
* [The MIT License (MIT)](http://opensource.org/licenses/MIT)
* Contains also code from https://github.com/t0xa/gelfj

Contributing
------------
Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in and take a look into [CONTRIBUTING.md](https://github.com/duoduobingbing/gelf-logging4j/blob/main/.github/CONTRIBUTING.md)
