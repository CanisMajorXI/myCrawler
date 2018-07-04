package com.zqw.crawlers.autocommentcrawler;

import com.google.gson.*;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private OkHttpClient httpClient = new OkHttpClient();
    private static String DedeUserID;
    private static String SESSDATA;
    private static String bili_jct;
    private static String _dfcaptcha;
    private ExecutorService service = Executors.newFixedThreadPool(10);
    private ExecutorService singleService = Executors.newSingleThreadExecutor();
    private static int mid;
    private static String[] commentTemplates;


    private boolean configCookie() {
        File cookieConfig = new File(this.getClass().getResource("/cookie.json").getFile());
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        try {
            JsonArray cookieArray = jsonParser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(cookieConfig))))
                    .getAsJsonArray();
            for (JsonElement cookieElement : cookieArray) {
                JsonObject jsonObject = cookieElement.getAsJsonObject();
                switch (jsonObject.get("name").getAsString()) {
                    case "_dfcaptcha":
                        _dfcaptcha = jsonObject.get("value").getAsString();
                        break;
                    case "bili_jct":
                        bili_jct = jsonObject.get("value").getAsString();
                        break;
                    case "SESSDATA":
                        SESSDATA = jsonObject.get("value").getAsString();
                        break;
                    case "DedeUserID":
                        DedeUserID = jsonObject.get("value").getAsString();
                        break;
                }
            }
            if (_dfcaptcha == null || bili_jct == null || SESSDATA == null || DedeUserID == null)
                throw new Exception();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("cookie配置文件出问题!");
            return false;
        }
    }

    private void exec() {
        if (!configCookie()) return;
        service.execute(new VideoRunnable());
        service.shutdown();
    }

    public static void main(String[] args) {
//        int mid = 21141883;
        int mid = 21141883;
        String[] commentTemplates = {"支持UP主", "支持一个", "吼啊", "这是坠吼的"};
        Main.mid = mid;
        Main.commentTemplates = commentTemplates;
        new Main().exec();
    }

    private class VideoRunnable implements Runnable {

//        https://space.bilibili.com/ajax/member/getSubmitVideos?mid=294375&page=1&pagesize=25

        @Override
        public void run() {
            Request pageRequest = new Request.Builder()
                    .url("https://space.bilibili.com/ajax/member/getSubmitVideos?mid=" + mid + "&page=1&pagesize=25")
                    .build();
            //List<Integer> aidList = new ArrayList<>();
            try {
                Response pageResponse = httpClient.newCall(pageRequest).execute();
                String pageContent = pageResponse.body() != null ? pageResponse.body().string() : "";
                int page = new JsonParser()
                        .parse(pageContent)
                        .getAsJsonObject().get("data").getAsJsonObject()
                        .get("pages").getAsInt();
                for (int i = 1; i <= page; i++) {
                    Request request = new Request.Builder()
                            .url("https://space.bilibili.com/ajax/member/getSubmitVideos?mid=" + mid + "&page=" + i + "&pagesize=25")
                            .build();
                    Response response = httpClient.newCall(request).execute();
                    String content = response.body() != null ? response.body().string() : "";
                    JsonArray videolist = new JsonParser().parse(content).getAsJsonObject()
                            .get("data").getAsJsonObject()
                            .get("vlist").getAsJsonArray();
                    for (JsonElement jsonElement : videolist) {
                        JsonObject video = jsonElement.getAsJsonObject();
                        int aid = video.get("aid").getAsInt();
                        singleService.execute(new CommentRunnable(aid));
                       // aidList.add(video.get("aid").getAsInt());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            service.shutdown();
            singleService.shutdown();


        }
    }

    private class CommentRunnable implements Runnable {
        int aid;

        public CommentRunnable(int aid) {
            this.aid = aid;
        }

        // https://api.bilibili.com/x/v2/reply/add
        @Override
        public void run() {
            String message = commentTemplates[new Random().nextInt(commentTemplates.length)];
            RequestBody requestBody = new FormBody.Builder()
                    .add("oid", aid + "")
                    .add("type", "1")
                    .add("message", message)
                    .add("plat", "1")
                    .add("jsonp", "jsonp")
                    .add("csrf", bili_jct)
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/v2/reply/add")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("COOKIE", "DedeUserID=" + DedeUserID + ";  SESSDATA=" + SESSDATA + "; bili_jct=" + bili_jct + "; _dfcaptcha=" + _dfcaptcha)
                    .post(requestBody)
                    .build();
            try {
                Response response = httpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    System.out.println("你对" + aid + "评论" + message + "发表成功！");
                } else {
                    System.out.println("你对" + aid + "评论" + message + "发表失败！");
                }
                Thread.sleep(4000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
