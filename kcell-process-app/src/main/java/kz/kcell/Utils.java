package kz.kcell;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.net.URI;

public class Utils {

    public static HttpPost createHttpPost(String url, String baseUri, JSONObject value) throws Exception {
        HttpPost httpPost = new HttpPost(new URI(url));
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.addHeader("Referer", baseUri);
        StringEntity inputData = new StringEntity(value.toString(), "UTF-8");
        httpPost.setEntity(inputData);
        return httpPost;
    }

    public static HttpPut createHttpPut(String url, String baseUri, JSONObject value) throws Exception {
        HttpPut httpPut = new HttpPut(new URI(url));
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.addHeader("Referer", baseUri);
        StringEntity inputData = new StringEntity(value.toString(), "UTF-8");
        httpPut.setEntity(inputData);
        return httpPut;
    }
}
