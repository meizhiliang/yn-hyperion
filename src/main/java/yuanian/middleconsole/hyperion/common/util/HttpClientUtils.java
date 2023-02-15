package yuanian.middleconsole.hyperion.common.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Base64;

/**
 * @projectName hyperion
 * @author ZhiliangMei
 * @date 2021/10/15
 * @menu: http请求调用
 */
public class HttpClientUtils {

    /**
     * http POST请求含请求地址、请求入参  json方式请求
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public static String doPostJson(String url, String json,String userName,String passWord) throws Exception {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        // 创建Http Post请求
        HttpPost httpPost = new HttpPost(url);
        // 创建请求内容
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(300000)
                .setSocketTimeout(300000).setConnectTimeout(300000).build();
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", "Basic " + Base64.getUrlEncoder().encodeToString((userName+":"+passWord).getBytes()));
        // 执行http请求
        httpPost.setConfig(requestConfig);
        response = httpClient.execute(httpPost);
        resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        response.close();
        return resultString;
    }
}
