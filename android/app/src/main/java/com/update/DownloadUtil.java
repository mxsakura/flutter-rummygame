package com.update;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 文件下载工具
 */
public class DownloadUtil {
    private static final String TAG = DownloadUtil.class.getName();
    private static DownloadUtil downloadUtil = null;
    private final OkHttpClient okHttpClient;

    public static DownloadUtil Instance() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * url 下载连接
     * saveDir 储存下载文件的SDCard目录
     * listener 下载监听
     */
    public void download(final String url, final String savePath, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed(url);    // 下载失败
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                ensurePathExist(savePath);  // 储存下载文件的目录
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        listener.onDownloading(url, progress);   // 下载中
                    }
                    fos.flush();

                    listener.onDownloadSuccess(url);   // 下载完成
                } catch (Exception e) {
                    listener.onDownloadFailed(url);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * saveDir
     * 判断下载目录是否存在
     */
    private String ensurePathExist(String savePath) throws IOException {
        File file = new File(savePath);
        File dir = file.getParentFile();
        if (dir.exists() == false) {
            dir.mkdirs();
        }

        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        String ret = file.getAbsolutePath();
        return ret;
    }

    /**
     * url
     * 从下载连接中解析出文件名
     */
    @NonNull
    public static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(final String url);

        /**
         * @param progress 下载进度
         */
        void onDownloading(final String url, int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed(final String url);
    }
}