# Songpark GUI

Readme's exist for RN and Web app. Go into respective folders to see them.

# Release

Currently the work being done is on the Web app. Use the Makefile to make
releases. Currently only staging and production works. Copy staging for the
other types of releases necessary.

You need to be logged in to AWS in the terminal for this to work. That means you
need the AWS CLI tools installed.


```
# this is for just building the image
make build-staging
# this is for pushing to ECR (Elastic Container Registry) on AWS
make push-staging
# this is for deploying to k8s (Kubernetes)
make kube-deploy-staging
# this is for deleting everything from k8s. USE WITH CAUTION!
make kube-remove-staging
# this is for doing everything
make deploy-staging
```
