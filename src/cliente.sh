#!/bin/sh
cd /home/sms/projects/tec/connectdots/build
java -Xmx16384m -cp .:../lib/processing/core/library/core.jar:../lib/json-20230618.jar Cliente &
