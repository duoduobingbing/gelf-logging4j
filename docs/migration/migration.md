# Migration Guide from v2.0.x to v2.1.0

- Jackson was upgraded to 3.x, changing its coordinates (and defaults)<br>
  If you need help with the migration, have a look at the [official Jackson 2 → 3 Migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- Kafka Clients have been upgraded to Kafka 4.1
- `io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.GelfREDISSender` does not accept a type parameter anymore.<br>
  So `new GelfREDISSender<?>();` has to be migrated to `new GelfREDISSender();` where used.
- `StackTraceFilter.getFilteredStackTrace(Throwable t, boolean shouldFilter)` has been removed.
  - For `StackTraceFilter.getFilteredStackTrace(throwable, true)` migrate to `StackTraceFilter.getFilteredStackTrace(throwable)`
  - For `StackTraceFilter.getFilteredStackTrace(throwable, false)` migrate to `StackTraceFilter.getStackTrace(throwable)`
- `GelfMessageAssembler.PROPERTY_USE_POOLING` has been removed. Please use `PoolingGelfMessageBuilder.PROPERTY_USE_POOLING`.
- Logback XML configuration has changed. If you are using `additionalFields` or `mdcFields`, you have to migrate to the new syntax:
  So, this:<br>
  ```xml
  <additionalFields>fieldName1=fieldValue1,fieldName2=fieldValue2</additionalFields>
  <mdcFields>mdcField1,mdcField2</mdcFields>
  ```
  becomes that:
  ```xml
  <additionalLogFields>
  
    <staticLogField>
        <name>fieldName1</name>
        <literal>fieldValue1</literal>
    </staticLogField>
    <staticLogField>
        <name>fieldName2</name>
        <literal>fieldValue2</literal>
    </staticLogField>
  
    <mdcLogField>
        <mdc>mdcField1</mdc>
    </mdcLogField>
    <mdcLogField>
        <mdc>mdcField2</mdc>
    </mdcLogField>
  
  </additionalLogFields>
  ```