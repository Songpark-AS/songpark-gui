#!/usr/bin/env bash

DEPLOYMENT_DIR=deployment
PROJECT_DIR=web-app

# copy git version to the root
cp VERSION.git $PROJECT_DIR/resources/public/
# rsync with archive (a) and compressed (z)
rsync -az $PROJECT_DIR/resources/public/ root@live.songpark.com:/var/www/live.songpark.com/
ssh root@live.songpark.com chown -R www-data:www-data /var/www/live.songpark.com
