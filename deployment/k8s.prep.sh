#!/usr/bin/env bash

DEPLOYMENT_DIR=deployment
TEMPLATE_FILE=songpark-webapp.template.yaml
YAML_FILE=songpark-webapp.yaml
TAG=$1
VERSION=$2
## TODO: come up with a scheme for namespaces and hostnames that work for different types of deployments
NAMESPACE=songpark
HOSTNAME=songpark.inonit.no

echo "Copying template file"
cp $DEPLOYMENT_DIR/$TEMPLATE_FILE $DEPLOYMENT_DIR/$YAML_FILE

echo "Updating variables"
sed -i -e 's/VAR__VERSION/'"$VERSION"'/g' $DEPLOYMENT_DIR/$YAML_FILE
sed -i -e 's/VAR__TAG/'"$TAG"'/g' $DEPLOYMENT_DIR/$YAML_FILE
sed -i -e 's/VAR__NAMESPACE/'"$NAMESPACE"'/g' $DEPLOYMENT_DIR/$YAML_FILE
sed -i -e 's/VAR__HOSTNAME/'"$HOSTNAME"'/g' $DEPLOYMENT_DIR/$YAML_FILE

sed -i -e 's/songpark-production/'"songpark"'/g' $DEPLOYMENT_DIR/$YAML_FILE
sed -i -e 's/songpark-production\.inonit\.no/'"songpark.inonit.no"'/g' $DEPLOYMENT_DIR/$YAML_FILE
