FROM openjdk:17

ENV TZ=Asia/Shanghai
ENV LANG zh_CN.UTF-8
ENV LANGUAGE zh_CN.UTF-8
ENV DJL_CACHE_DIR=/tmp/djl_cache
ENV DJL_PYTORCH_HOME=/tmp/pytorch_cache
ENV PYTORCH_HOME=/tmp/pytorch_cache
ENV ai.djl.default_engine=OnnxRuntime
ENV DJL_DEFAULT_ENGINE=OnnxRuntime
ENV ai.djl.onnx.disable_alternative=true

RUN mkdir -p /app /app/models /app/upload /tmp/djl_cache /tmp/pytorch_cache

COPY ./bin /app
COPY ./conf /app/conf
COPY ./lib/doc-tools.jar /app/doc-tools.jar

WORKDIR /app

EXPOSE 8080

CMD bash /app/init.sh