package com.example.wpct.utils;

import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ImageUtils {
    @SneakyThrows
    public static String imgToBase64(BufferedImage image){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(image != null){
            ImageIO.write(image,"jpg",stream);
        }
        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }

    @SneakyThrows
    public static BufferedImage base64ToImg(String base64){
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes1 = decoder.decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
        return ImageIO.read(bais);
    }
}
