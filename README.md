# aida-pva-client
Client utility class to facilitate running AIDA-PVA requests.  For more 
information on AIDA-PVA see [AIDA-PVA documentation](https://www.slac.stanford.edu/grp/cd/soft/aida/aida-pva/).

## Details
In order to write a query it is very easy.

### e.g. 1: Simple get
```java
 Float bact = getRequest("XCOR:LI03:120:LEFF", FLOAT);
```

### e.g. 2: Multiple arguments
```java
 AidaTable table = request("NDRFACET:BUFFACQ")
     .with("BPMD", 57)
     .with("NRPOS", 180)
     .with("BPMS", List.of(
             "BPMS:LI11:501",
             "BPMS:LI11:601",
             "BPMS:LI11:701",
             "BPMS:LI11:801"))
     .get();
   String firstName = table.getValues().get("name").get(0)
```

### e.g. 3: Simple set

```java
 setRequest("XCOR:LI31:41:BCON", 5.0f);
```

### e.g. 4: Alternative simple set

```java
 request("XCOR:LI31:41:BCON").set(5.0f);
```

### e.g. 5: Advanced set
```java
 Short status = ((AidaTable)request("KLYS:LI31:31:TACT")
     .with("BEAM", 8)
     .with("DGRP", "DEV_DGRP")
     .set(0)
     ).getValues().set("status").get(0);
```

### e.g. 6: Selecting the return value type

```java
String value = request("KLYS:LI31:31:TACT")
     .with("BEAM", 8)
     .with("DGRP", "DEV_DGRP")
     .returning(STRING)
     .get();
```
