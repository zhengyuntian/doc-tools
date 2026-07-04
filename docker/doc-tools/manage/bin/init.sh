#!/bin/bash

export DJL_CACHE_DIR=/tmp/djl_cache
export DJL_PYTORCH_HOME=/tmp/pytorch_cache
export PYTORCH_HOME=/tmp/pytorch_cache
export ai.djl.default_engine=OnnxRuntime
export DJL_DEFAULT_ENGINE=OnnxRuntime
export ai.djl.onnx.disable_alternative=true

mkdir -p /tmp/djl_cache /tmp/pytorch_cache /app/upload

MODEL_PATH="/app/models"

if [ ! -f "${MODEL_PATH}/ppocr_v5_det/model.onnx" ]; then
    cd "${MODEL_PATH}/ppocr_v5_det" && ln -sf PP-OCRv5_server_det.onnx model.onnx
fi

if [ ! -f "${MODEL_PATH}/ppocr_v5_rec/model.onnx" ]; then
    cd "${MODEL_PATH}/ppocr_v5_rec" && ln -sf PP-OCRv5_server_rec.onnx model.onnx
fi

if [ ! -f "${MODEL_PATH}/ppocr_mobile_v2_cls/model.onnx" ]; then
    cd "${MODEL_PATH}/ppocr_mobile_v2_cls" && ln -sf ch_ppocr_mobile_v2.0_cls.onnx model.onnx
fi

if [ ! -f "${MODEL_PATH}/slanet_plus/model.onnx" ]; then
    cd "${MODEL_PATH}/slanet_plus" && ln -sf slanet-plus.onnx model.onnx
fi

echo "[OCR模型] 模型符号链接检查完成"
echo "[OCR模型] 模型路径: ${MODEL_PATH}"

java $JAVA_OPTS -Dfile.encoding=UTF-8 -Dspring.config.location=file:/app/conf/application.yml -jar /app/doc-tools.jar