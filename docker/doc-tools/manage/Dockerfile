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

RUN mkdir -p /app /app/models /tmp/djl_cache /tmp/pytorch_cache

COPY ./bin /app
COPY ./conf /app/conf
COPY ./lib/doc-tools.jar /app/doc-tools.jar
COPY ./models /app/models

WORKDIR /app/models/ppocr_v5_det
RUN ln -sf PP-OCRv5_server_det.onnx model.onnx

WORKDIR /app/models/ppocr_v5_rec
RUN ln -sf PP-OCRv5_server_rec.onnx model.onnx

WORKDIR /app/models/ppocr_mobile_v2_cls
RUN ln -sf ch_ppocr_mobile_v2.0_cls.onnx model.onnx

WORKDIR /app/models/slanet_plus
RUN ln -sf slanet-plus.onnx model.onnx

WORKDIR /app

EXPOSE 8080

CMD bash /app/init.sh