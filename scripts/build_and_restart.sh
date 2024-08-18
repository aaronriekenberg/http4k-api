#!/bin/bash

set -e
set -x

cd ~/http4k-api

systemctl --user stop http4k-api.service

git pull -v

./gradlew clean shadowJar

killall java

systemctl --user restart http4k-api.service
