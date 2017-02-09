# spiralis
Oxalis workflow engine


## JDBC configuration

Configuration properties take presedence in this order:

 1. System properties
 2. External configuration file referenced by:
     1. System property `spiralis.home`
     1. System environment variable `SPIRALIS_HOME`
     1. System property `user.home` + `/spiralis/spiralis.conf`
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


Reception : represents the reception of an incoming message. A unique identifcation is assigned.

AS2 Message-ID : see Transmission id

Transmission id : unique identification of a transmission between two access points. The value of the "Transmission ID"
 can be found in the AS2 Message-ID header.

 

