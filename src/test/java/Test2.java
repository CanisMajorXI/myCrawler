import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zqw.pojo.User;
import okhttp3.*;

import java.util.List;

public class Test2 {
    public static void main(String[] args) throws Exception{
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("mid","294375").build();
        Request request = new Request.Builder().url("https://space.bilibili.com/ajax/member/GetInfo?mid=294375").
                addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N)" +
                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 " +
                "Mobile Safari/537.36").addHeader("Referer","https://space.bilibili.com/294375").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();

//https://space.bilibili.com/ajax/member/getSubmitVideos?mid=294375&page=1&pagesize=25
       System.out.println(response.body().string());
//      //  String content = response.body().string();
//        Gson gson = new Gson();
//        JsonParser parser = new JsonParser();
//        JsonObject jsonObject = parser.parse(content).getAsJsonObject().get("data").getAsJsonObject();
//      //  System.out.println(jsonObject.get("name").getAsJsonObject());
//        String str =  gson.fromJson(jsonObject.get("sex").getAsJsonPrimitive(),String.class);
     //   System.out.println(str);
//        JsonArray list = parser.parse(content).
//                getAsJsonObject().get("data").getAsJsonObject();
//        List<User> userList = gson.fromJson(list, new TypeToken<List<User>>() {
//        }.getType());

    }
}
