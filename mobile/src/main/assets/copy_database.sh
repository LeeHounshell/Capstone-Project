#!/bin/bash

adb -d shell "run-as com.harlie.radiotheater.radiomysterytheater chmod 666 databases/radiomysterytheater.db"
#adb -d shell "run-as com.harlie.radiotheater.radiomysterytheater cat databases/radiomysterytheater.db" > radiomysterytheater.db
#adb pull "/data/data/com.harlie.radiotheater.radiomysterytheater/databases/radiomysterytheater.db"

adb backup -f ./data.ab -noapk com.harlie.radiotheater.radiomysterytheater
dd if=data.ab bs=1 skip=24 | openssl zlib -d | tar -xvf -
cp apps/com.harlie.radiotheater.radiomysterytheater/db/radiomysterytheater.db-journal .
cp apps/com.harlie.radiotheater.radiomysterytheater/db/radiomysterytheater.db .
rm -fr apps
rm ./data.ab

