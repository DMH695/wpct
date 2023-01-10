package com.example.wpct.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.wpct.utils.PathUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
@Api(tags = "wangEditor富文本")
@RestController
@RequestMapping("/upload")
public class UploadForWangEditorApi {

    @ApiOperation("上传图片")
    @RequestMapping(value = "/editor/picture",method = RequestMethod.GET)
    @ResponseBody
    public Object editor(@RequestParam("file") MultipartFile file) {
        String fileName = "";
        if (!file.isEmpty()) {
            //返回的是字节长度,1M=1024k=1048576字节 也就是if(fileSize<5*1048576)
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            if (StringUtils.isBlank(suffix)) {
                return new PictureResponse(1);
            }

            fileName = System.currentTimeMillis() + suffix;
            if (!new File(PathUtils.getRunPath() + "/upload").exists()) {
                new File(PathUtils.getRunPath() + "/upload").mkdir();
            }
            String saveFileName = PathUtils.getRunPath() + "/upload/examinePicture/" + fileName;
            File dest = new File(saveFileName);
            if (!dest.getParentFile().exists()) { //判断文件父目录是否存在
                dest.getParentFile().mkdir();
            }
            try {
                file.transferTo(dest); //保存文件
            } catch (Exception e) {
                e.printStackTrace();
                return new PictureResponse(1);
            }
        } else {
            return new PictureResponse(1);
        }
        String imgUrl = "http://124.71.9.121:8080/api/upload/examinePicture/" + fileName;
        List<JSONObject> res = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", imgUrl);
        res.add(jsonObject);
        return new PictureResponse(0, res);
    }

    @ApiOperation("上传视频")
    @RequestMapping(value = "/editor/video",method = RequestMethod.GET)
    @ResponseBody
    public Object editorOfVideo(@RequestParam("file") MultipartFile file) {
        String fileName = "";
        if (!file.isEmpty()) {
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            if (StringUtils.isBlank(suffix)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", "文件没有后缀名，请重新上传");
                return new WangEditorResponse(1, jsonObject);
            }

            fileName = System.currentTimeMillis() + suffix;
            if (!new File(PathUtils.getRunPath() + "/upload").exists()) {
                new File(PathUtils.getRunPath() + "/upload").mkdir();
            }
            String saveFileName = PathUtils.getRunPath() + "/upload/examineVideo/" + fileName;
            File dest = new File(saveFileName);
            if (!dest.getParentFile().exists()) { //判断文件父目录是否存在
                dest.getParentFile().mkdir();
            }
            try {
                file.transferTo(dest); //保存文件
            } catch (Exception e) {
                e.printStackTrace();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", e.getMessage());
                return new WangEditorResponse(1, jsonObject);
            }
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "上传出错");
            return new WangEditorResponse(1, jsonObject);
        }
        String imgUrl = "http://124.71.9.121:8080/api/upload/examineVideo/" + fileName;
        JSONObject res = new JSONObject();
        res.put("url", imgUrl);
        return new WangEditorResponse(0, res);
    }

    @Data
    private class PictureResponse{
        int errno;
        List<JSONObject> data;
        public PictureResponse(int errno,List<JSONObject> data){
            this.errno=errno;
            this.data=data;
        }
        public PictureResponse(int errno){
            this.errno=errno;
        }
    }

    @Data
    private class WangEditorResponse{
        int errno;
        JSONObject data;
        public WangEditorResponse(int errno,JSONObject data){
            this.errno=errno;
            this.data=data;
        }
    }
}