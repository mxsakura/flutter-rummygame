package com.example.hello_flutter;

import androidx.annotation.RequiresApi;
import dalvik.system.PathClassLoader;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterShellArgs;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.update.HotUpdate;
import com.update.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends FlutterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public FlutterShellArgs getFlutterShellArgs() {
        FlutterShellArgs args = super.getFlutterShellArgs();
        String libPath = HotUpdate.getLibFullPath();
        args.add("--aot-shared-library-name=" + libPath);
        return args;
    }
}
