import { IHttpResponse, IObject } from "@/types/interface";
import http from "../utils/http";
import { ElMessage } from "element-plus";
import { getToken } from "@/utils/cache";

/**
 * 常用CRUD
 */
export default {
  /**
   * 删除
   * @param path
   * @param params
   * @returns
   */
  delete(path: string, params: IObject): Promise<IHttpResponse> {
    return http({
      url: path,
      data: params,
      method: "DELETE"
    });
  },
  get(path: string, params?: IObject, headers?: IObject): Promise<IHttpResponse> {
    return new Promise((resolve, reject) => {
      http({
        url: path,
        params,
        headers,
        method: "GET"
      })
        .then(resolve)
        .catch((error) => {
          if (error !== "-999") {
            reject(error);
          }
        });
    });
  },
  put(path: string, params?: IObject, headers?: IObject): Promise<IHttpResponse> {
    return http({
      url: path,
      data: params,
      headers: {
        "Content-Type": "application/json;charset=UTF-8",
        ...headers
      },
      method: "PUT"
    });
  },
  /**
   * 通用post方法
   * @param path
   * @param body
   * @returns
   */
  post(path: string, body?: IObject, headers?: IObject): Promise<IHttpResponse> {
    // 如果body是FormData，不设置Content-Type，让浏览器自动处理
    const isFormData = body instanceof FormData;
    return http({
      url: path,
      method: "post",
      headers: isFormData ? headers : {
        "Content-Type": "application/json;charset=UTF-8",
        ...headers
      },
      data: body
    });
  },
  /**
   * 下载文件
   * @param path 下载路径
   * @param fileName 文件名（可选，用于设置下载文件名）
   */
  download(path: string, fileName?: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const token = getToken();
      const url = (import.meta.env.VITE_APP_API || '') + path;
      
      fetch(url, {
        method: 'GET',
        headers: {
          'X-Requested-With': 'XMLHttpRequest',
          'token': token || ''
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('下载失败');
        }
        return response.blob();
      })
      .then(blob => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = fileName || 'download';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);
        resolve();
      })
      .catch(error => {
        console.error('下载失败:', error);
        ElMessage.error('下载失败');
        reject(error);
      });
    });
  }
};
