package com.kaisa.kams.components.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liuwen01 on 2016/11/22.
 */
public class ApiRequestUtil {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestUtil.class);
    private static final String QUERY_USER_ERROR_RESULT = "{\n" +
            "    \"body\": { }, \n" +
            "    \"head\": {\n" +
            "        \"errorCode\": 99997, \n" +
            "        \"errorMsg\": \"请求错误\", \n" +
            "        \"method\": \"com.jzy.kaisafax.openApi.api.user.queryUser\"\n" +
            "    }\n" +
            "}";
    private static final String QUERY_USER_PARAM_ERROR_RESULT = "{\n" +
            "    \"body\": { }, \n" +
            "    \"head\": {\n" +
            "        \"errorCode\": 99999, \n" +
            "        \"errorMsg\": \"请输入关联用户\", \n" +
            "        \"method\": \"com.jzy.kaisafax.openApi.api.user.queryUser\"\n" +
            "    }\n" +
            "}";

    private static final  Map head = new HashMap();
    static {
        head.put("version","1.0");
        head.put("appKey", ApiPropsUtils.getValueByKey("TRADE_API_KEY"));
        head.put("sessionId", "no-session");
    }

    public static String sendSmsByAPI(String phone, Map<String,String> msg, String temId) {
        String result;
        head.put("method", ApiPropsUtils.getValueByKey("method"));
        Map body = new HashMap<String,String>();
        String msgId = UUID.randomUUID().toString();
        body.put("msgId",msgId);
        body.put("phone",phone);
        body.put("msg",Json.toJson(msg, JsonFormat.compact()).toString());
        body.put("temId",temId);
        body.put("assessToken", ApiPropsUtils.getValueByKey("TRADE_SMS_TOKEN"));
        body.put("assessSecret", ApiPropsUtils.getValueByKey("TRADE_SMS_SECRET"));
        Map<String,String> signMap = new HashMap<>();
        signMap.putAll(body);
        signMap.putAll(head);
        String sign = SignUtils.sign(signMap, ApiPropsUtils.getValueByKey("TRADE_API_SECRET"));
        Map<String,String> requestHead = new HashMap<>();
        requestHead.putAll(head);
        requestHead.put("sign", sign);

        List<NameValuePair> params = new ArrayList();
        params.add(new BasicNameValuePair("head", Json.toJson(requestHead, JsonFormat.compact()).toString()));
        params.add(new BasicNameValuePair("body", Json.toJson(body,  JsonFormat.compact()).toString()));
        String param = null;
        try {
            param = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = HttpClientUtil.sendGet(ApiPropsUtils.getValueByKey("TRADE_API_HOST"),param.toString());
        log.info("Send sms on api phone:{},temId:{},msg:{}",phone,temId,msg.toString());
        return result;
    }


    public static String queryUserByAPI(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return QUERY_USER_PARAM_ERROR_RESULT;
        }
        head.put("method", ApiPropsUtils.getValueByKey("QUERY_USER_METHOD"));
        Map body = new HashMap<String,String>();
        body.put("mobile",mobile);

        Map<String,String> signMap = new HashMap<>();
        signMap.putAll(body);
        signMap.putAll(head);
        String sign = SignUtils.sign(signMap, ApiPropsUtils.getValueByKey("TRADE_API_SECRET"));

        Map<String,String> requestHead = new HashMap<>();
        requestHead.putAll(head);
        requestHead.put("sign", sign);

        List<NameValuePair> params = new ArrayList();
        params.add(new BasicNameValuePair("head", Json.toJson(requestHead, JsonFormat.compact()).toString()));
        params.add(new BasicNameValuePair("body", Json.toJson(body,  JsonFormat.compact()).toString()));

        String result = HttpClientUtil.sendPost(params,ApiPropsUtils.getValueByKey("TRADE_API_HOST"));
        if (StringUtils.isEmpty(result)) {
            return QUERY_USER_ERROR_RESULT;
        }
        return result;
    }




}
