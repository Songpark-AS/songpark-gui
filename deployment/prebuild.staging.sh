#!/bin/bash

DEPLOYMENT_DIR=deployment
PROJECT_ROOT=web-app
PROJECT_DIR=web-app/src/web_app
PROJECT_NAME=songpark-gui
CONFIG_JS_TEMPLATE_FILE=config.staging.js
CONFIG_JS_FILE=config.js
CONFIG_JS_BACKUP_FILE=config.local.js
VERSION=$1

echo "Building the webapp with shadow-cljs"

echo "Making backup copy of config.js"
cp $PROJECT_DIR/$CONFIG_JS_FILE $PROJECT_DIR/$CONFIG_JS_BACKUP_FILE
cp $DEPLOYMENT_DIR/$CONFIG_JS_TEMPLATE_FILE $PROJECT_DIR/$CONFIG_JS_FILE

echo "Updating variables in config.js"
sed -i '' 's/VAR__VERSION/'"$VERSION"'/g' $PROJECT_DIR/$CONFIG_JS_FILE

echo "Compiling $PROJECT_NAME"
cd $PROJECT_ROOT
npm i
npx shadow-cljs release app
cd ..

echo "Restoring backup of config.js"

mv $PROJECT_DIR/$CONFIG_JS_BACKUP_FILE $PROJECT_DIR/$CONFIG_JS_FILE
