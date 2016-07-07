#!/bin/bash
#
# This is a script to generate the SQLite ContentProvider Java code for Radio Mystery Theater
# It uses the generator jar from: https://github.com/BoD/android-contentprovider-generator
#
# run: 'java -jar android_contentprovider_generator-1.9.3-bundle.jar -i <input folder> -o <output folder>'
#       - Input folder: where to find `_config.json` and your entity json files
#       - Output folder: where the resulting files will be generated
#

ORIG_DIR=`pwd`
BASEDIR=$(dirname $0)
cd ${BASEDIR}
OUTPUT="../data"
TMP="./tmp"
rm -fr ${OUTPUT} ${TMP}
mkdir -p ${TMP}
java -jar ./android_contentprovider_generator-1.9.3-bundle.jar -i . -o ${TMP}
mv ${TMP}/com/harlie/radiotheater/radiomysterytheater/data ${OUTPUT}
rm -fr ${TMP}
cd ${ORIG_DIR}

#
# FIXME: remember to hand-edit the data/RadioTheaterProvider.java
# FIXME: use different content AUTHORITY via #IFDEF 'PAID' and 'TRIAL'
#

#
#    // NOTE: these values must match the values/values.xml:radio_theater_content_authority
#
#//#IFDEF 'PAID'
#    //public static final String AUTHORITY = "com.harlie.radiotheater.radiomysterytheater.paid.data.radiotheaterprovider";
#//#ENDIF
#
#//#IFDEF 'TRIAL'
#    public static final String AUTHORITY = "com.harlie.radiotheater.radiomysterytheater.trial.data.radiotheaterprovider";
#//#ENDIF
#