package com.update;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dalvik.system.PathClassLoader;

public class Utils {

    private static String TAG = "Utils";
    public static String getNativeLibraryPath(Context context, String libraryName) {

        if (TextUtils.isEmpty(libraryName)) {
            return null;
        }

        String dexPath = context.getPackageCodePath();
        String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
        PathClassLoader pathClassLoader = new PathClassLoader(dexPath, nativeLibraryDir,
                ClassLoader.getSystemClassLoader());
        return pathClassLoader.findLibrary(libraryName);
    }

    public static String getABI() {
        String[] abis = new String[]{};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }

        if (abis[0].equals("x86")) {
            return abis[1];
        } else {
            return abis[0];
        }
    }

    public static byte[] readAssetsFile(Context context, String path) {

        try {
            AssetManager am = context.getResources().getAssets();
            InputStream is = am.open(path);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return  buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean fileExist(String path) {
        File file = new File(path);
        return  file.exists();
    }

    public static byte[] readFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            boolean ret = file.canRead();
            if (ret == false) {
                Log.i(TAG, TAG + " readFile: canRead false");
                return null;
            }

            try {
                FileInputStream fs = new FileInputStream(file);
                int len = fs.available();
                byte[] buffer = new byte[len];
                int byteCount = 0;
                int offset = 0;
                while (true) {
                    int readLen = Math.min(len - offset, 10240);
                    if (readLen <= 0) {
                        break;
                    }

                    byteCount = fs.read(buffer, offset, readLen);
                    offset += byteCount;
                }

                fs.close();
                return buffer;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return null;
    }

    public static void writeFile(String path, byte[] bytes) {

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        try {
            File parentFile = file.getParentFile();
            parentFile.mkdirs();
            file.createNewFile();
            FileOutputStream fs = new FileOutputStream(file);
            fs.write(bytes);
            fs.flush();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readTextFile(String path) {
        byte[] bytes = readFile(path);
        if (bytes == null) {
            return "";
        }

        try {
            String ret = new String(bytes, "utf-8");
            return ret;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void writeTextFile(String path, String text) {
        if (text == null) {
            return;
        }

        writeFile(path, text.getBytes());
    }

    public static void copyFile(String srcPath, String desPath) {
        byte[] bytes = readFile(srcPath);
        if (bytes == null) {
            return;
        }

        writeFile(desPath, bytes);
    }

    public static void removeFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String getWritableFullPath(Context context, String relativePath) {
        String dir = getWritablePath(context);
        return joinPath(dir, relativePath);
    }

    public static String getCacheFullPath(Context context, String relativePath) {
        String dir = getCachePath(context);
        return joinPath(dir, relativePath);
    }

    public static String getWritablePath(Context context) {
        File dir = context.getFilesDir();
        return dir.getAbsolutePath();
    }

    public static String getCachePath(Context context) {
        File dir = context.getCacheDir();
        return dir.getAbsolutePath();
    }

    public static String getPathDir(String path) {
        File file = new File(path);
        File parentFile = file.getParentFile();
        return parentFile.getAbsolutePath();
    }

    public static String joinPath(final String path, String ... paths) {

        String fullPath = path;
        if (fullPath == null) {
            fullPath = "";
        }

        if (paths == null) {
            paths = new String[0];
        }

        if (paths.length == 0) {
            return fullPath;
        }

        String separator = "/"/*File.separator*/;
        if (fullPath.endsWith(separator)) {
            int len = fullPath.length();
            fullPath = fullPath.substring(0, len - 1);
        }

        for (int i = 0; i < paths.length; i++) {
            String part = paths[i];
            if (part == null || part.length() == 0) {
                continue;
            }

            if (part.startsWith(separator)) {
                part = part.substring(1);
            }

            if (fullPath.endsWith(separator)) {
                fullPath += part;
            } else {
                fullPath += separator + part;
            }
        }

        return fullPath;
    }

    //eg. url = http://www.aaa.com?a=1&b=2  return http://www.aaa.com
    public static String getBaseUrl(String url) {
        int pos = url.indexOf('?');
        if (pos < 0) {
            return url;
        }

        String ret = url.substring(0, pos);
        return ret;
    }

    public static String getRelativePath(String fullPath, String basePath) {
        if (fullPath == null || fullPath.length() == 0 || basePath == null || basePath.length() == 0) {
            return "";
        }

        if (fullPath.startsWith(basePath) == false || fullPath.length() <= basePath.length()) {
            return fullPath;
        }

        if (basePath.endsWith("/") == false) {
            basePath += "/";
        }

        String ret = fullPath.substring(basePath.length());
        return ret;
    }

    public static String md5(String str) {

        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return md5(str.getBytes());
    }

    public static String md5(byte[] byteArr) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(byteArr);
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", new Integer(b & 0xff)));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getFileMD5(String path) {
        if (path == null || path.length() == 0) {
            return "";
        }

        byte[] bytes = readFile(path);
        if (bytes == null) {
            return "";
        }

        String ret = md5(bytes);
        return ret;
    }
}