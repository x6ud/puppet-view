package com.github.x6ud.puppetview;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class OcrUtils {

    private static AipOcr client;

    private static synchronized void init() {
        if (client != null) {
            return;
        }
        try {
            Properties properties = new Properties();
            FileInputStream inputStream = new FileInputStream("config.properties");
            properties.load(inputStream);
            client = new AipOcr(
                    properties.getProperty("app.id"),
                    properties.getProperty("app.key"),
                    properties.getProperty("app.secret")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String accurateGeneral(BufferedImage image, String language) {
        init();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            HashMap<String, String> options = new HashMap<>();
            /*
             * language_type:
             * CHN_ENG
             * ENG
             * POR
             * FRE
             * GER
             * ITA
             * SPA
             * RUS
             * JAP
             * KOR
             */
            options.put("language_type", language);
            options.put("detect_direction", "true");
            options.put("detect_language", "true");
            options.put("probability", "true");
            JSONObject result = client.accurateGeneral(outputStream.toByteArray(), options);
            int wordsResultNum = result.getInt("words_result_num");
            if (wordsResultNum > 0) {
                JSONArray array = result.getJSONArray("words_result");
                for (int i = 0, len = array.length(); i < len; ++i) {
                    JSONObject object = array.getJSONObject(i);
                    String words = object.getString("words");
                    stringBuilder.append(words);
                    if (i < len - 1) {
                        stringBuilder.append('\n');
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
