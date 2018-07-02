package com.zqw.crawlers;

import com.zqw.persistent.GetUserInfo;
import com.zqw.pojo.UserInfo;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Followcrawler {
    private ExecutorService service = Executors.newFixedThreadPool(5);
    private OkHttpClient okHttpClient = new OkHttpClient();

    public static void main(String[] args) {
        new Followcrawler().exec();
    }

    private void exec() {
        List<UserInfo> userInfoList = GetUserInfo.getUserInfoBySex(GetUserInfo.FEMALE);
        for (UserInfo userInfo : userInfoList) {
            try {
                service.execute(new MyRunnable(userInfo.getMid()));
                Thread.sleep(3000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class MyRunnable implements Runnable {
        private int mid;
        public MyRunnable(){};
        public MyRunnable(int mid) {
            this.mid = mid;
        }

        @Override
        public void run() {
            RequestBody requestBody = new FormBody.Builder()
                    .add("fid", mid+"")
                    //.add("fid", "9117212")
                    .add("act","1")
                    .add("re_src","11")
                    .add("jsonp","jsonp")
                    .add("csrf","c9230274c5b55bdf9c424e13231ef380")
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/relation/modify")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")

                    .addHeader("COOKIE", "DedeUserID=103152410;  SESSDATA=bff22279%2C1533117292%2C417deff9; bili_jct=c9230274c5b55bdf9c424e13231ef380; _dfcaptcha=6b9e8c9a5899a9686d25c4d91cbf2de4")
                    .post(requestBody).build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if(response.code() == 200) {
                    System.out.println("你关注"+mid+"成功！");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        }
    }
}
