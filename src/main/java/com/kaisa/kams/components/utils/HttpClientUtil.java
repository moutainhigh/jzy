package com.kaisa.kams.components.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.nutz.lang.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * @author  by pengyueyang on 2017/1/12.
 */
public class HttpClientUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final String CHARSET_UTF_8 = "utf-8";

    public static String sendPost(List<NameValuePair> params, String baseUrl) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(baseUrl);
        UrlEncodedFormEntity urlEncodedFormEntity = null;
        try {
            urlEncodedFormEntity = new UrlEncodedFormEntity(params, CHARSET_UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding:{}",CHARSET_UTF_8);
            e.printStackTrace();
        }
        post.setEntity(urlEncodedFormEntity);
        String result = "";
        CloseableHttpResponse response = null;
        try {
            //发送请求
            response = httpClient.execute(post);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                log.error("Request response error code is:{}",response.getStatusLine().getStatusCode());
                return result;
            }
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity == null) {
                log.error("Request response is null");
               return result;
            }
            InputStream inputStream = null;
            try {
                inputStream = responseEntity.getContent();
                result = Streams.readAndClose(Streams.utf8r(inputStream));
            } catch (IOException e) {
                log.error("Read response is error msg:{}",e.getMessage());
            } finally {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
        } catch (IOException e){
            log.error("Request is error msg:{}",e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setRequestProperty("contentType", "utf-8");
            // 建立实际的连接
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }


}

