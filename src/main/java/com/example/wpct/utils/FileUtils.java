package com.example.wpct.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.Objects;

public class FileUtils {

    public static JSONArray filesToJson(String path){
        File dir = new File(path);
        if (!dir.isDirectory()){
            return null;
        }
        return getChild(dir);
    }

    private static JSONArray getChild(File dir){
        JSONArray res = new JSONArray();
        File[] files = dir.listFiles();
        for (File file : Objects.requireNonNull(files)) {
            JSONObject node = new JSONObject();
            node.put("filename",file.getName());
            node.put("filesize",file.length());
            JSONArray child;
            if (file.isDirectory()){
                node.put("dir",true);
                child = getChild(file);
                node.put("child",child);
            }else {
                node.put("dir",false);
            }
            res.add(node);
        }
        return res;
    }

}
