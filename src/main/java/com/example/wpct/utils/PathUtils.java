package com.example.wpct.utils;

import org.springframework.boot.system.ApplicationHome;

import java.io.File;

public class PathUtils {
    //linux和windows下通用
    private static String getJarFilePath() {
        ApplicationHome home = new ApplicationHome(PathUtils.class);
        File jarFile = home.getSource();
        if(jarFile != null){
            String path = jarFile.getParentFile().getPath().toString();
            return path;
        }
        return null;
    }

    /**
     * 获取运行环境所在的目录
     * @return 运行目录
     */
    public static String getRunPath(){
        File jarFile = new File(getJarFilePath());
        return jarFile.getParent();
    }
}
