package com.update;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;


public class HotUpdate {

    enum ErrorCode {
        E_OK(0, "OK"),  //success
        E_ConfigError(2, "config error"),   //config error
        E_ConfigException(3, "config exception"),   //config error
        E_NetError(4, "network error"); //network error

        public int id;
        public String msg;
        private ErrorCode(int id, String msg) { this.id = id; this.msg = msg; }
    }

    private static String TAG = HotUpdate.class.getName();
    private static Context context = null;
    private static String _dir = "hot";
    private static String _versionFile = "version.json";
    private static String _libDir = "lib";
    private static String _libName = "app";

    private static VersionInfo versionInfo = null;
    private static VersionInfo newVersionInfo = null;
    private static ArrayList<DownloadInfo> downloadsInfo = null;

    private static final int MSG_DOWNLOAD_SUC = 1001;
    private static final int MSG_DOWNLOAD_FAIL = 1002;
    private static final int MSG_DOWNLOAD_ING = 1003;

    private static final String KEY_HOT_URL = "hot_url";
    private static final String KEY_HOT_VER = "hot_ver";

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_DOWNLOAD_SUC: {
                    DownloadInfo info = (DownloadInfo)msg.obj;
                    info.callback.onDownloadSuccess(info);
                    break;
                }
                case MSG_DOWNLOAD_FAIL: {
                    DownloadInfo info = (DownloadInfo)msg.obj;
                    info.callback.onDownloadFail(info);
                    break;
                }
                case MSG_DOWNLOAD_ING: {
                    DownloadInfo info = (DownloadInfo)msg.obj;
                    info.callback.onDownloading(info);
                    break;
                }
            }
        }
    };

    private static String getMainPath(final String fileName) {
        if (context == null) {
            throw new NullPointerException();
        }

        String path = Utils.getWritableFullPath(context, _dir);
        path = Utils.joinPath(path, fileName);
        return path;
    }

    public static String getLibFullPath() {
        String libRelativePath = getLibRelativePath();
        String libPath = getMainPath(libRelativePath);
        return libPath;
    }
    private static String getLibRelativePath() {
        String abi = Utils.getABI();
        String ret = String.format("%s/%s/%s", _libDir, abi, "lib" + _libName + ".so");
        return ret;
    }

    public static void init(Context context) {
        Log.i(TAG, TAG + " init");
        HotUpdate.context = context;
        downloadsInfo = new ArrayList<DownloadInfo>();
        String versionPath = getMainPath(_versionFile);
        Log.i(TAG, TAG + " " + Utils.getWritablePath(context));
        if (Utils.fileExist(versionPath)) {
            try {
                String text = Utils.readTextFile(versionPath);
                JSONObject json = new JSONObject(text);
                versionInfo = VersionInfo.create(json);
                if (versionInfo == null) {
                    Utils.removeFile(versionPath);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                versionInfo = null;
            }
        }

        String libPath = getLibFullPath();
        if (Utils.fileExist(libPath) == false) {
            String innerLibPath = Utils.getNativeLibraryPath(context, _libName);
            if (TextUtils.isEmpty(innerLibPath) == false) {
                Utils.copyFile(innerLibPath, libPath);
            }
        }

        if (versionInfo == null) {
            String baseUrl = getMetaData(KEY_HOT_URL);
            int baseVersion = getMetaIntData(KEY_HOT_VER);
            versionInfo = new VersionInfo(baseUrl, baseVersion);
        }

        loadVersion();
    }

    private static void loadVersion() {
        long time = new Date().getTime();
        String url = Utils.joinPath(versionInfo.getRemoteUrl(), _versionFile);
        url += ("?t=" + time);
        download(url, false, new DownloadInfo.Callback() {
            @Override
            public void onDownloadSuccess(DownloadInfo info) {
                downloadsInfo.add(info);
                checkVersion(info.savePath);
            }

            @Override
            public void onDownloadFail(DownloadInfo info) {
                updateFail(ErrorCode.E_NetError);
            }

            @Override
            public void onDownloading(DownloadInfo info) { }
        });
    }

    private static void checkVersion(String versionPath) {
        try {
            String text = Utils.readTextFile(versionPath);
            JSONObject json = new JSONObject(text);
            newVersionInfo = VersionInfo.create(json);
            if (newVersionInfo == null) {
                Utils.removeFile(versionPath);
                updateFail(ErrorCode.E_ConfigError);
                return;
            }

            int curVer = versionInfo.getVersion();
            int newVer = newVersionInfo.getVersion();
            Log.i(TAG, TAG + "checkVersion: " + curVer + "," + newVer);
            if (curVer != newVer) {
                startUpdate();
                return;
            }

            String libRelativePath = getLibRelativePath();
            String newMD5 = newVersionInfo.getMD5(libRelativePath);
            if (TextUtils.isEmpty(newMD5)) {
                updateFail(ErrorCode.E_ConfigError);
                return;
            }

            String libPath = getLibFullPath();
            if (Utils.fileExist(libPath) == false) {
                libPath = "";
            }

            String curMD5 = Utils.getFileMD5(libPath);
            if (curMD5.equals(newMD5) == false) {
                startUpdate();
                return;
            }

            updateFinish();
            return;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        updateFail(ErrorCode.E_ConfigException);
    }

    private static void startUpdate() {

        String libUrl = Utils.joinPath(newVersionInfo.getRemoteUrl(), "" + newVersionInfo.getVersion(), getLibRelativePath());
        download(libUrl, true, new DownloadInfo.Callback() {
            @Override
            public void onDownloadSuccess(DownloadInfo info) {
                downloadsInfo.add(info);
                checkUpdateLib(info.savePath);
            }

            @Override
            public void onDownloadFail(DownloadInfo info) {
                updateFail(ErrorCode.E_NetError);
            }

            @Override
            public void onDownloading(DownloadInfo info) { }
        });
    }

    private static void checkUpdateLib(String libPath) {
        String tempMD5 = Utils.getFileMD5(libPath);
        if (TextUtils.isEmpty(tempMD5)) {
            updateFail(ErrorCode.E_NetError);
            return;
        }

        String libRelativePath = getLibRelativePath();
        String newMD5 = newVersionInfo.getMD5(libRelativePath);
        if (tempMD5.equals(newMD5) == true) {
            saveDownloads();
            updateFinish();
        } else {
            updateFail(ErrorCode.E_NetError);
        }
    }

    private static void saveDownloads() {
        if (downloadsInfo == null) {
            return;
        }

        final String cachePath = Utils.getCachePath(context);
        for (DownloadInfo info: downloadsInfo) {
            String tempPath = info.savePath;
            String relativePath = "";
            if (info.isAsset) {
                String assetsCachePath = Utils.joinPath(cachePath, "" + newVersionInfo.getVersion());
                relativePath = Utils.getRelativePath(tempPath, assetsCachePath);
            } else {
                relativePath = Utils.getRelativePath(tempPath, cachePath);
            }

            String savePath = getMainPath(relativePath);
            Utils.copyFile(tempPath, savePath);
            Utils.removeFile(tempPath);
        }
    }

    private static void updateFinish() {
        Log.i(TAG, TAG + " updateFinish");
        UpdateResult handler = (UpdateResult)context;
        handler.onUpdateResult(ErrorCode.E_OK);
    }

    private static void updateFail(ErrorCode errorCode) {
        Log.i(TAG, TAG + " updateFail");
        UpdateResult handler = (UpdateResult)context;
        handler.onUpdateResult(errorCode);
    }

    private static void download(final String url, final boolean isAsset, final DownloadInfo.Callback callback) {
        String baseUrl = Utils.getBaseUrl(url);
        final String relativePath = Utils.getRelativePath(baseUrl, versionInfo.getRemoteUrl());
        final String cachePath = Utils.getCacheFullPath(context, relativePath);
        DownloadUtil.Instance().download(url, cachePath, new  DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String url) {
                Log.i(TAG, TAG + " onDownloadSuccess");
                DownloadInfo info = new DownloadInfo(url, callback).setPath(cachePath).setIsAsset(isAsset);
                handleMessage(MSG_DOWNLOAD_SUC, info);
            }

            @Override
            public void onDownloading(String url, int progress) {
                //Log.i(TAG, TAG + " onDownloading: " + progress);
                DownloadInfo info = new DownloadInfo(url, callback).setProgress(progress);
                handleMessage(MSG_DOWNLOAD_ING, info);
            }

            @Override
            public void onDownloadFailed(String url) {
                Log.i(TAG, TAG + " onDownloadFailed: " + url);
                DownloadInfo info = new DownloadInfo(url, callback);
                handleMessage(MSG_DOWNLOAD_FAIL, info);
            }
        });
    }

    private static void handleMessage(final int what, final DownloadInfo info) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = info;
        handler.sendMessage(msg);
    }

    private static String getMetaData(String key) {
        try {
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(new ComponentName(context, UpdateActivity.class), PackageManager.GET_META_DATA);
            String ret = activityInfo.metaData.getString(key);
            return ret;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static int getMetaIntData(String key) {
        try {
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(new ComponentName(context, UpdateActivity.class), PackageManager.GET_META_DATA);
            int ret = activityInfo.metaData.getInt(key);
            return ret;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public interface UpdateResult {
        public void onUpdateResult(ErrorCode errorCode);
    }
}