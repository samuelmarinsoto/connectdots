#!/bin/sh
javac -d ../build/ -cp .:../lib/processing/core/library/core.jar:../lib/json-20230618.jar Inicio.java
javac -d ../build/ -cp .:../lib/processing/core/library/core.jar:../lib/json-20230618.jar Cliente.java
javac -d ../build/ -cp .:../lib/processing/core/library/core.jar:../lib/json-20230618.jar Servidor.java
