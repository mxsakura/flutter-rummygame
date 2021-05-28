package com.update;

public class DownloadInfo {
    public String url = "";
    public String savePath = "";
    public boolean isAsset = false;
    public int progress = 0;
    public Callback callback = null;
    public DownloadInfo(String url, Callback cb) {
        this.url = url;
        this.isAsset = isAsset;
        this.callback = cb;
    }

    public DownloadInfo setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public DownloadInfo setPath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    public DownloadInfo setIsAsset(boolean isAsset) {
        this.isAsset = isAsset;
        return this;
    }

    public interface Callback {
        void onDownloadSuccess(final DownloadInfo info);
        void onDownloadFail(final DownloadInfo info);
        void onDownloading(final DownloadInfo info);
    }
}