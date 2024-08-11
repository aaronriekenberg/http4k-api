#!/bin/bash

java -XX:+UseZGC -XX:+ZGenerational -Xlog:gc:gc.log -jar ./build/libs/http4k-api.jar
