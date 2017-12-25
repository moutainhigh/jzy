package com.kaisa.kams.components.controller.base;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.kaisa.kams.components.utils.OssPropsUtils;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.Param;

import java.util.Date;

/**
 * Created by pengyueyang on 2017/3/7.
 */
@IocBean
public class BaseUploadController {

    private static final Log log = Logs.get();


    public Object fetchAliOssToken(@Param("dir") String dir) {
        NutMap result = new NutMap();

        dir += "/";
        String endpoint = OssPropsUtils.getValueByKey("endpoint");  // 访问域名
        String accessId = OssPropsUtils.getValueByKey("accessId");
        String accessKey = OssPropsUtils.getValueByKey("accessKey");
        String bucket = OssPropsUtils.getValueByKey("bucket");  // 存储空间
        long expireTime = Long.valueOf(OssPropsUtils.getValueByKey("expireTime"));
        String host = "http://" + bucket + "." + endpoint;
        OSSClient client = new OSSClient(endpoint, accessId, accessKey);
        try {
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            result.put("accessid", accessId);
            result.put("policy", encodedPolicy);
            result.put("signature", postSignature);
            result.put("dir", dir);
            result.put("host", host);
            result.put("expire", String.valueOf(expireEndTime / 1000));
        } catch (Exception e) {
            log.debug("OssToken策略编码错误");
        }
        return result;
    }
}
