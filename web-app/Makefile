#!/usr/bin/env make -f
export VERSION := $(shell git rev-parse HEAD)
AWS = 523581807964.dkr.ecr.eu-central-1.amazonaws.com

all: docker-build-staging docker-push-staging

aws-pw:
	aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin $(AWS)


# Build release versions of the app
release-app-staging:
	cp src/web_app/config.js src/web_app/config.local.js
	cp deploy/config.staging.js src/web_app/config.js
	npm i
	npx shadow-cljs release app
	mv src/web_app/config.local.js src/web_app/config.js

release-app-prod:
	cp src/web_app/config.js src/web_app/config.local.js
	cp deploy/config.prod.js src/web_app/config.js
	npm i
	npx shadow-cljs release app
	mv src/web_app/config.local.js src/web_app/config.js


# Docker
docker-build:
	docker build -t songpark-webapp:$(VERSION) .

docker-build-staging: release-app-staging docker-build

docker-build-prod: release-app-prod docker-build

docker-tag-staging:
	docker tag songpark-webapp:$(VERSION) $(AWS)/songpark-webapp:staging

docker-tag-prod:
	docker tag songpark-webapp:$(VERSION) $(AWS)/songpark-webapp:prod

docker-push-staging: aws-pw docker-tag-staging
	docker push $(AWS)/songpark-webapp:staging

docker-push-prod: aws-pw docker-tag-prod
	docker push $(AWS)/songpark-webapp:prod


# k8s
k8s-remove-staging:
	kubectl delete -f deploy/songpark-webapp-staging.yaml

k8s-deploy-staging: docker-tag-staging
	kubectl apply -f deploy/songpark-webapp-staging.yaml

k8s-remove-prod:
	kubectl delete -f deploy/songpark-webapp-prod.yaml

k8s-deploy-prod: docker-tag-prod
	kubectl apply -f deploy/songpark-webapp-prod.yaml

