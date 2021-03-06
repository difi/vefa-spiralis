# spiralis

## Introduction

Operating an access point consists of three tasks pertaining to inbound and outbound traffic:

 * Receive
 * Process
 * Transmit

Message flow is relative to the PEPPOL network, i.e. "outbound" traffic is always transmitted
into the network, while "inbound" traffic is all about receiving messages
from the network.

```

                                O U T B O U N D
             +-----------+     +------------+     +--------------+
Back-end --> | Reception | --> | Processing | --> | Transmission | --> PEPPOL
             +-----------+     +------------+     +--------------+

                                   I N B O U N D
             +--------------+     +------------+     +-----------+
Back-end <-- | Transmission | <-- | Processing | <-- | Reception | <-- PEPPOL
             +--------------+     +------------+     +-----------+

```

Spiralis was created to do the *Processing* of messages received by
 [Oxalis](http://github.com/difi/oxalis)

Oxalis is only responsible for receiving messages and will simply dump a set of files in your
 local file system.

Spiralis' job is to monitor the local file system and process the messages received by Oxalis.

Spiralis consists of two major components, working together:

 1. *oxalis-plugin*, which is an Oxalis plugin responsible for writing the data received by Oxalis
    into the local file system in a format suitable for later processing.
 1. *spiralis-inbound*; reads the data produced by *oxalis-plugin* and performs the processing.

In addition there is the `azureblob`, which is a plugin for the Ringo-server, allowing content stored in Microsoft Azure
to be retrieved and downloaded.

## Oxalis-plugin

Oxalis-plugin is installed as a plugin in accordance with the installation instructions of Oxalis.

It produces the following files in the *spiralis.inbound.directory*, the *transmissionId* being assigned by Oxalis:

 * {transmissionId}`.doc.xml` - holds the acutal payload, wrapped in an SBDH
 * {transmissionId}`.receipt.dat` - holds the REM evidence
 * {transmissionId}`.receipt.smime` - holds the AS2 MDN receipt
 * {transmissionId}`.meta.json` - holds the meta data of the reception.


## spiralis-inbound

This standalone executable program will scan the "inbound" directory and for each set of transmission files:

 1. Parses the meta data helding in the `.meta.json` file
 1. Uploads the files to persistent storage
 1. Writes the metadata to the DBMS using JDBC.

## Configuration

```
spiralis {
    inbound.directory : /var/peppol/IN
    azure {
	    account    : ****testdata01
	    access-key : "eOAASM/8sB****************************************************************************=="
	    protocol   : http
	    connect    : "DefaultEndpointsProtocol="${spiralis.azure.protocol};"AccountName="${spiralis.azure.account};"AccountKey="${spiralis.azure.access-key}
	}
}
include "jdbc.conf"
```

### JDBC Configuration

The following describes how to set up the configuration for JDBC in a separate file:

```
// Default configuration uses H2, which should be on the classpath
jdbc {
	connection.uri : "jdbc:h2:~/.oxalis/ap;AUTO_SERVER=TRUE"
	driver.class {
	    path : ""
	    name : "org.h2.Driver"
	}
	user: "SA"
	password : ""
	validation.query : "select now()"
}
```

Please note that this configuration assumes that you are using the H2 database, which is included as
 part of Spiralis.

If you intend to use a different database, you must to set the property `jdbc.driver.class.path` to ensure
that Spiralis is able to instantiate the JDBC driver.

#### MS SQL Server example configuration
```
// Example configuration for MS SQL Server
jdbc {
  connection.uri : "jdbc:sqlserver://hmaptestdb001.database.windows.net:1433;database=dbname;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
  driver.class {
    path : "file:///Users/steinar/.m2/repository/com/microsoft/sqlserver/sqljdbc42/4.2/sqljdbc42-4.2.jar"
    name : "com.microsoft.sqlserver.jdbc.SQLServerDriver"
  }
  user: "user@dbname"
  password : "************"
  validation.query : ""
}
```


## Configuration files

Spiralis uses the Typesafe Config library.

Configuration properties take presedence in this order:

 1. System properties, set using `-D` argument.
 2. External configuration file referenced by:
     1. System property `spiralis.home`
     1. System environment variable `SPIRALIS_HOME`
     1. System property `user.home` + `/.spiralis/spiralis.conf`
 1. `reference.conf` files embedded in each component

 This is depicted below

 ```
    +---------------------------+
    | Config created from       |
    | command line              |   "-d dirname" --> "spiralis.inbound.directory"
    +------------+--------------+
                 |
                 V
    +------------+--------------+
    | Java system properties    |   Tip: -Doxalis.home=yourfile.conf
    +------------+--------------+
                 |
                 V
    +------------+--------------+
    | sysprop('spiralis.home')/ |   (optional)           +----------------+
    | spiralis.conf             +---include xxx.conf --->+ jdbcxxxxxx.conf|
    +------------+--------------+                        +----------------+
                 |
                 V
    +------------+--------------+
    | reference.conf*           |
    +------------v--------------+

 ```

## Concepts

**Reception** : represents the reception of an incoming message. A unique identification is assigned.

**ReceptionId** : unique identification in the form of a UUID, identifying inbound and outbound messages.

**AS2 Message-ID** : see Transmission id

**Transmission id** : unique identification of a transmission between two access points. The value of the "Transmission ID"
 can be found in the AS2 Message-ID header.

 

