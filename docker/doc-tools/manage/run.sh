#!/bin/bash

projectName=doc-tools
externalPort=8081
internalPort=8080
docker stop $projectName
docker rm $projectName
version=$1
docker build -f Dockerfile -t $projectName:$version .
docker run --name $projectName --privileged=true --restart=always \
  -p $externalPort:$internalPort \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e PADDLE_ENABLED=true \
  -e PADDLE_MODEL_PATH=/app/models \
  -e DJL_CACHE_DIR=/tmp/djl_cache \
  -e DJL_DEFAULT_ENGINE=OnnxRuntime \
  -e ai.djl.onnx.disable_alternative=true \
  -v $(pwd)/models:/app/models \
  -v $(pwd)/logs:/app/logs \
  -d $projectName:$version
docker logs -f -t $projectName