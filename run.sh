#!/bin/bash

export PATH=/home/aaron/.sdkman/candidates/java/current/bin:$PATH
export PATH=/home/aaron/bin:$PATH

java -Xms512m -Xmx512m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc:gc.log -jar ./build/libs/http4k-api.jar 2>&1 | simplerotate logs
