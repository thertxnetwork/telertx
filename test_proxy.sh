#!/bin/bash
java -jar target/telertx-1.0.0-jar-with-dependencies.jar <<COMMANDS
/proxy info
/proxy set
SOCKS5
127.0.0.1
1080
no

/proxy info
/quit
COMMANDS
