# Migration Guide from v2.0.x to v2.1.0

- Jackson was upgraded to 3.x, changing its coordinates (and defaults)<br>
  If you need help migration, have a look at the [official Jackson 2 → 3 Migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- `io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.GelfREDISSender` does not accept a type parameter anymore.<br>
  So `new GelfREDISSender<?>();` has to be migrated to `new GelfREDISSender();` where used.