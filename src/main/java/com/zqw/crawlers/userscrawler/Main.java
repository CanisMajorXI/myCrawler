package com.zqw.crawlers.userscrawler;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.zqw.persistent.SaveUserInfo;
import com.zqw.persistent.SaveVideoInfo;
import com.zqw.pojo.User;
import com.zqw.pojo.UserInfo;
import com.zqw.pojo.Video;
import okhttp3.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    //    private Logger logger = Logger.getLogger(this.getClass());
    private static final int MAX_COUNT = 20;
    private volatile Set<Integer> foundUsers = new HashSet<>(200);
    private ReentrantReadWriteLock idLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock countLock = new ReentrantReadWriteLock();
    private OkHttpClient okHttpClient = new OkHttpClient();
    private ExecutorService service = Executors.newFixedThreadPool(50);
    private ExecutorService singleService = Executors.newSingleThreadExecutor();
    private volatile int curCount = 0;

    private void addAnIdIntoList(int id) {
        try {
            idLock.writeLock().lock();
            foundUsers.add(id);
        } finally {
            idLock.writeLock().unlock();
        }
    }

    private boolean hasUserBeenFound(int id) {
        try {
            idLock.readLock().lock();
            return foundUsers.contains(id);
        } finally {
            idLock.readLock().unlock();
        }
    }

    private int getCurCountSecurely() {
        try {
            countLock.readLock().lock();
            return curCount;
        } finally {
            countLock.readLock().unlock();
        }
    }

    private void addCurCountSecurely() {
        try {
            countLock.writeLock().lock();
            curCount++;
        } finally {
            countLock.writeLock().unlock();
        }
    }

    private void exec() {
        //service.execute(new UserInfoRunnable(294375));
        singleService.execute(new FansRunnable(294375));
    }

    public static void main(String[] args) {
        new Main().exec();
    }

    private class UserInfoRunnable implements Runnable {
        private int mid;

        UserInfoRunnable(int mid) {
            this.mid = mid;
        }

        @Override
        public void run() {
            RequestBody requestBody = new FormBody.Builder().add("mid", mid + "").build();
            Request request = new Request.Builder().url("https://space.bilibili.com/ajax/member/GetInfo").
                    addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N)" +
                            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 " +
                            "Mobile Safari/537.36").addHeader("Referer", "https://space.bilibili.com/" + mid).post(requestBody).build();
            String content = null;
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.code() == 200)
                    content = Objects.requireNonNull(response.body()).string();
                else {
                    System.out.println("错误代码！");
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println(mid + "爬取用户信息时发生异常！");
                //   e.printStackTrace();
                return;
            }
            System.out.println("------已爬取的个数:" + getCurCountSecurely() + "当前爬取用户: " + mid + "------");
            if (!singleService.isShutdown())
                singleService.execute(new FansRunnable(mid));
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject dataObj = parser.parse(content).getAsJsonObject().get("data").getAsJsonObject();
            UserInfo userInfo = gson.fromJson(dataObj, UserInfo.class);
            if (!service.isShutdown()) service.execute(new VideoRunnable(mid));
            // System.out.println(userInfo.getName()+" "+userInfo.getSign());
            //SaveUserInfo.save(userInfo);
        }
    }


    private class FansRunnable implements Runnable {
        private int mid;

        FansRunnable(int mid) {
            this.mid = mid;
        }

        @Override
        public void run() {
            Request request = new Request.Builder()
                    .url("http://api.bilibili.com/x/relation/followers?vmid=" + mid + "")
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N)" +
                            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 " +
                            "Mobile Safari/537.36")
                    .build();
            String content = null;
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.code() == 200)
                    content = response.body() != null ? response.body().string() : "";
                else {
                    System.out.println("错误代码！");
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("爬取粉丝时发生错误！");
                //e.printStackTrace();
                return;
            }
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray fans = parser.parse(content).
                    getAsJsonObject().get("data").getAsJsonObject().get("list").getAsJsonArray();
            for (JsonElement element : fans) {
                int mid = element.getAsJsonObject().get("mid").getAsInt();
                if (!foundUsers.contains(mid) && getCurCountSecurely() < MAX_COUNT) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addAnIdIntoList(mid);
                    addCurCountSecurely();
                    if (!service.isShutdown()) service.execute(new UserInfoRunnable(mid));
                } else {
                    service.shutdown();
                    singleService.shutdown();
                }
            }
        }
    }

    private class VideoRunnable implements Runnable {
        private int mid;

        VideoRunnable(int mid) {
            this.mid = mid;
        }

        @Override
        public void run() {
            Request request = new Request.Builder()
                    .url("https://space.bilibili.com/ajax/member/getSubmitVideos?mid=" + mid + "&page=1&pagesize=25")
                    .build();
            String content = null;
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.code() == 200) {
                    content = response.body() != null ? response.body().string() : "";
                    System.out.println(content);
                } else {
                    System.out.println("错误代码！");
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println(mid + "爬取视频信息时发生异常！");
                //   e.printStackTrace();
                return;
            }
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonList = jsonParser.parse(content)
                    .getAsJsonObject()
                    .get("data")
                    .getAsJsonObject()
                    .get("vlist").getAsJsonArray();
            List<Video> videoList = gson.fromJson(jsonList, new TypeToken<List<Video>>() {
            }.getType());
            SaveVideoInfo.save(videoList);
        }
    }
}
