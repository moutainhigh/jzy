package com.kaisa.kams.components.controller.push;

import com.kaisa.kams.components.aop.ApiFilter;
import com.kaisa.kams.components.params.common.ApiHeadParam;
import com.kaisa.kams.enums.push.ApiResponseType;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by pengyueyang on 2017/11/7.
 */
@IocBean
@At("/api")
@Ok("json")
@Filters(@By(type = ApiFilter.class))
public class ApiController {

    Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final static HashSet METHODS = new HashSet(10);
    private final static HashMap<String, String> AUTHENTICATIONS = new HashMap<>();

    static {
        METHODS.add("loanInfoReceive");
        AUTHENTICATIONS.put("kaisafax001", "dc0f503dd01911e7b177000c2915fb01");
    }

    @At("/")
    @AdaptBy(type = JsonAdaptor.class)
    public NutMap dispatch(@Param("head") ApiHeadParam head, @Param("body") String body) {
        ApiResponseType validateResult = validateRequest(head);
        if (!ApiResponseType.VALIDATE_OK.equals(validateResult)) {
            String requestMethod = null;
            if (null != head) {
                requestMethod = head.getMethod();
            }
            return createResponse(createHead(validateResult, requestMethod), null);
        }
        Class clazz = this.getClass();
        Method method;
        try {
            method = clazz.getMethod(head.getMethod(), new Class[]{String.class});
            Object response = method.invoke(clazz.newInstance(), body);
            return createResponse(createHead(ApiResponseType.PROCESS_OK, head.getMethod()), response);
        } catch (NoSuchMethodException e) {
            logger.error("NoSuchMethodException:{}", e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException:{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException:{}", e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("InvocationTargetException:{}", e.getMessage());
        } catch (Exception e) {
            logger.error("Process error:{}", e.getMessage());
        }
        return createResponse(createHead(ApiResponseType.PROCESS_ERROR, head.getMethod()), null);
    }


    public HashMap<String, String> loanInfoReceive(String body) {
        HashMap<String, String> result = new HashMap<>(1);
        result.put("status", "0");
        return result;
    }

    private NutMap createResponse(ResponseHead head, Object body) {
        NutMap result = new NutMap();
        result.put("head", head);
        result.put("body", body);
        return result;
    }

    private ResponseHead createHead(ApiResponseType responseType, String method) {
        return new ResponseHead(responseType.getCode(), responseType.getDescription(), method);
    }

    private ApiResponseType validateRequest(ApiHeadParam head) {
        if (null == head) {
            return ApiResponseType.VALIDATE_ERROR_ILLEGAL_ARGUMENT;
        }
        if (!authentication(head)) {
            return ApiResponseType.VALIDATE_ERROR_AUTHENTICATION;
        }
        if (!validateMethod(head)) {
            return ApiResponseType.VALIDATE_ERROR_NO_METHOD;
        }
        return ApiResponseType.VALIDATE_OK;
    }

    private boolean authentication(ApiHeadParam head) {
        if (StringUtils.isEmpty(head.getAppKey()) || StringUtils.isEmpty(head.getAppSecret())) {
            return false;
        }
        if (!AUTHENTICATIONS.containsKey(head.getAppKey())) {
            return false;
        }
        return head.getAppSecret().equals(AUTHENTICATIONS.get(head.getAppKey()));
    }

    private boolean validateMethod(ApiHeadParam head) {
        if (StringUtils.isEmpty(head.getMethod())) {
            return false;
        }
        return METHODS.contains(head.getMethod());
    }


}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ResponseHead {
    /** 返回码 */
    private String code;

    /** 返回信息 */
    private String errorMsg;

    /** 请求方法 */
    private String method;

}
