#!/bin/bash

export PATH=/home/aaron/.sdkman/candidates/java/current/bin/:$PATH

java -Xmx512m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc:gc.log -jar ./build/libs/http4k-api.jar 2>&1 | simplerotate logs
