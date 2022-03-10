PROJECT_ROOT=web-app

cd $PROJECT_ROOT
npx less-watch-compiler --run-once
npx sass ./resources/songpark.scss ./resources/public/css/songpark.css
