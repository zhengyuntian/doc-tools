#!/bin/bash

projectName=doc-tools
externalPort=8081
internalPort=8080

docker stop $projectName 2>/dev/null
docker rm $projectName 2>/dev/null

version=$1
if [ -z "$version" ]; then
    version="latest"
fi

docker build -f Dockerfile -t $projectName:$version .

docker run --name $projectName --privileged=true --restart=always \
  -p $externalPort:$internalPort \
  -e PADDLE_ENABLED=true \
  -e PADDLE_MODEL_PATH=/app/models \
  -e DJL_CACHE_DIR=/tmp/djl_cache \
  -e DJL_DEFAULT_ENGINE=OnnxRuntime \
  -e ai.djl.onnx.disable_alternative=true \
  -v $(pwd)/models:/app/models \
  -v $(pwd)/upload:/app/upload \
  -v $(pwd)/logs:/app/logs \
  -d $projectName:$version

docker logs -f -t $projectName