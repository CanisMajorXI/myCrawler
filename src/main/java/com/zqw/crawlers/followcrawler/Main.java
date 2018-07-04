package com.zqw.crawlers.followcrawler;

import com.google.gson.*;
import com.zqw.persistent.GetUserInfo;
import com.zqw.pojo.UserInfo;
import okhttp3.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private ExecutorService service = Executors.newFixedThreadPool(5);
    private OkHttpClient okHttpClient = new OkHttpClient();
    private static String DedeUserID;
    private static String SESSDATA;
    private static String bili_jct;
    private static String _dfcaptcha;
    private static final int TYPE_FOLLOW = 1;
    private static final int TYPE_UNFOLLOW = 2;


    public static void main(String[] args) {
        new Main().exec();
    }

    private void exec() {
        File cookieConfig = new File(this.getClass().getResource("/cookie.json").getFile());
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        try {
            JsonArray cookieArray = jsonParser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(cookieConfig))))
                    .getAsJsonArray();
            for (JsonElement cookieElement : cookieArray) {
                JsonObject jsonObject = cookieElement.getAsJsonObject();
                // System.out.println(jsonObject.get("value").getAsString());
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

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("cookie配置文件出问题!");
            return;
        }
        List<UserInfo> userInfoList = GetUserInfo.getUserInfoBySex(GetUserInfo.FEMALE);
        int i = 0;
        for (UserInfo userInfo : userInfoList) {
            try {
                service.execute(new MyRunnable(userInfo.getMid(), TYPE_FOLLOW));
                Thread.sleep(3000);
                if (++i > 15) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        service.shutdown();
    }

    class MyRunnable implements Runnable {
        private int mid;
        private int type;

        public MyRunnable(int mid, int type) {
            this.mid = mid;
            this.type = type;
        }

        @Override
        public void run() {
            RequestBody requestBody = new FormBody.Builder()
                    .add("fid", mid + "")
                    //.add("fid", "9117212")
                    .add("act", type + "")
                    .add("re_src", "11")
                    .add("jsonp", "jsonp")
                    .add("csrf", bili_jct)
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/relation/modify")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("COOKIE", "DedeUserID=" + DedeUserID + ";  SESSDATA=" + SESSDATA + "; bili_jct=" + bili_jct + "; _dfcaptcha=" + _dfcaptcha)
                    // .addHeader("COOKIE", "DedeUserID=103152410;  SESSDATA=bff22279%2C1533117292%2C417deff9; bili_jct=c9230274c5b55bdf9c424e13231ef380; _dfcaptcha=6b9e8c9a5899a9686d25c4d91cbf2de4")
                    .post(requestBody).build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.code() == 200) {
                    System.out.println("你关注" + mid + "成功！");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        }
    }
}
