import com.google.gson.Gson;
import com.zqw.pojo.User;

public class Test1 {
    public static void main(String[] args) {
        Gson gson =  new Gson();
       String str = "{\"id\":1234,\"password\":\"dsadda\",\"note\":\"hello\"}";
        User user = gson.fromJson(str,User.class);

    }
}
