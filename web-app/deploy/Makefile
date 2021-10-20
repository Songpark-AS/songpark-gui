all: build push

aws-pw: 
	aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 523581807964.dkr.ecr.eu-central-1.amazonaws.com

release-app:
	npx shadow-cljs release app

build: release-app
	docker build -t songpark-webapp:latest ../.

tag: build aws-pw
	docker tag songpark-webapp:latest 523581807964.dkr.ecr.eu-central-1.amazonaws.com/songpark-webapp:latest

push: tag
	docker push 523581807964.dkr.ecr.eu-central-1.amazonaws.com/songpark-webapp:latest

remove:
	kubectl delete -f songpark-webapp.yaml

deploy: tag
	kubectl apply -f songpark-webapp.yaml

