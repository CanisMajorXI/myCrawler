import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Test3 {
    public static void main(String[] args) throws Exception{
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://api.bilibili.com/x/relation/followers?vmid=294375").build();
        Response response = okHttpClient.newCall(request).execute();
        System.out.println(response.body().string());
    }
}
