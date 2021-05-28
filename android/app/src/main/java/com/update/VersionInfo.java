package com.update;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class VersionInfo {

    private static final String KEY_VERSION = "version";
    private static final String KEY_REMOTEURL = "remoteUrl";
    private static final String KEY_ASSETS = "assets";
    private static final String KEY_SIZE = "size";
    private static final String KEY_MD5 = "md5";

    private int version = 0;
    private String url = "";
    private HashMap<String, FileInfo> assetsMap = null;
    private boolean valid = false;

    public static VersionInfo create(JSONObject json) {
        try {
            int version = json.getInt(KEY_VERSION);
            String remoteUrl = json.getString(KEY_REMOTEURL);
            HashMap<String, FileInfo> map = parseAssets(json.getJSONObject(KEY_ASSETS));
            if (map == null) {
                return null;
            }

            VersionInfo info = new VersionInfo(remoteUrl, version);
            info.assetsMap = map;
            info.valid = true;
            return info;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public VersionInfo(String url, int version) {
        this.url = url;
        this.version = version;
        assetsMap = new HashMap<String, FileInfo>();
        valid = false;
    }

    public void saveToFile(String path) {
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_VERSION, version);
            json.put(KEY_REMOTEURL, url);
            JSONObject assetsJson = new JSONObject();
            for (HashMap.Entry<String, FileInfo> entry : assetsMap.entrySet()) {
                String key = entry.getKey();
                FileInfo fileInfo = entry.getValue();
                JSONObject item = new JSONObject();
                item.put(KEY_MD5, fileInfo.md5);
                item.put(KEY_SIZE, fileInfo.size);
                assetsJson.put(key, item);
            }

            String jsonString = json.toString();
            Utils.writeTextFile(path, jsonString);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public boolean isValid() {
        return valid;
    }

    public int getVersion() {
        return version;
    }

    public String getRemoteUrl() {
        return url;
    }

    public String getMD5(String key) {
        if (assetsMap == null || assetsMap.containsKey(key) == false) {
            return "";
        }

        FileInfo info = assetsMap.get(key);
        return info.md5;
    }

    private HashMap<String, FileInfo> getAssetsMap() {
        return assetsMap;
    }

    private static HashMap<String, FileInfo> parseAssets(JSONObject assetsJson) {
        HashMap<String, FileInfo> map = new HashMap<String, FileInfo>();
        Iterator<String> iter = assetsJson.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                JSONObject item = assetsJson.getJSONObject(key);
                String md5 = item.getString(KEY_MD5);
                int size = item.getInt(KEY_SIZE);
                FileInfo info = new FileInfo(md5, size);
                map.put(key, info);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return map;
    }


    public static class FileInfo {

        public String md5 = "";
        public int size = 0;

        public FileInfo(String md5, int size) {
            this.md5 = md5;
            this.size = size;
        }
    }
}