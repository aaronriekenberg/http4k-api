#!/bin/bash -x

DONE=false

PROJECT_PATH=$(realpath $(dirname $0)/..)
echo "PROJECT_PATH = $PROJECT_PATH"

cd $PROJECT_PATH

while [ $DONE = "false" ] ; do

  echo "begin loop $(date)"

  git pull -v

  RELEASE=$(git describe --abbrev=0 --tags)
  echo "RELEASE=$RELEASE"

  URL="https://github.com/aaronriekenberg/http4k-api/releases/download/${RELEASE}/http4k-api.jar"

  wget $URL
  WGET_RESULT=$?
  if [ $WGET_RESULT -eq 0 ] ; then
    DONE=true
  else
    echo "wget failure result $WGET_RESULT sleeping"
    DONE=false
    sleep 60
  fi

done

systemctl --user stop http4k-api.service

rm -fr build
mkdir -p build/libs
cd build/libs

mv $PROJECT_PATH/http4k-api.jar .

systemctl --user restart http4k-api.service
