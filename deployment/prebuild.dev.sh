#!/bin/bash

DEPLOYMENT_DIR=deployment
PROJECT_ROOT=web-app
PROJECT_DIR=web-app/src/web_app
PROJECT_NAME=songpark-gui
CONFIG_JS_TEMPLATE_FILE=config.dev.js
CONFIG_JS_FILE=config.js
CONFIG_JS_BACKUP_FILE=config.local.js
VERSION=$1
VERSION_DATE=$2

echo "Building the webapp with shadow-cljs"

echo "Making backup copy of $CONFIG_JS_FILE"
cp $PROJECT_DIR/$CONFIG_JS_FILE $PROJECT_DIR/$CONFIG_JS_BACKUP_FILE
cp $DEPLOYMENT_DIR/$CONFIG_JS_TEMPLATE_FILE $PROJECT_DIR/$CONFIG_JS_FILE

echo "Updating variables in $CONFIG_JS_FILE"
sed -i 's/VAR__VERSION/'"$VERSION_DATE"'/g' $PROJECT_DIR/$CONFIG_JS_FILE
sed -i 's/VAR__SHA/'"$VERSION"'/g' $PROJECT_DIR/$CONFIG_JS_FILE
echo "cat $CONFIG_JS_FILE"
cat $PROJECT_DIR/$CONFIG_JS_FILE

echo "Compiling $PROJECT_NAME"
cd $PROJECT_ROOT
rm -rf resources/public/js/compiled/
npm i
npx shadow-cljs release :app
rm -rf resources/public/js/compiled/cljs-runtime
rm -rf resources/public/js/manifest.edn
cd ..

echo "Restoring backup of $CONFIG_JS_FILE"

mv $PROJECT_DIR/$CONFIG_JS_BACKUP_FILE $PROJECT_DIR/$CONFIG_JS_FILE
