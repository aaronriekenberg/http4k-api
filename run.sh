#!/bin/bash

export PATH=/home/aaron/.sdkman/candidates/java/current/bin:$PATH

exec java -Xms1g -Xmx1g -XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -Xlog:gc:gc.log -jar ./build/libs/http4k-api.jar
