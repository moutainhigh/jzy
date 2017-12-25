package com.kaisa.kams.components.aop;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.OaService;
import com.kaisa.kams.models.User;

import org.apache.commons.lang.StringUtils;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by pengyueyang on 2017/1/5.
 */
public class CheckUserSession implements ActionFilter {

    private OaService oaService;
    private String name;
    private String path;
    private String h5Path;
    private String h5IndexPath;

    public CheckUserSession(String name, String path, String h5Path, String h5IndexPath) {
        this.name = name;
        this.path = path;
        this.h5Path = h5Path;
        this.h5IndexPath = h5IndexPath;
    }

    public View match(ActionContext context) {
        HttpSession session = Mvcs.getHttpSession(false);
        //h5客户端
        String userAgent = context.getRequest().getHeader("User-Agent");
        if (null != userAgent && userAgent.toLowerCase().contains("mobile")) {
            String requestPath = context.getRequest().getRequestURI() + "/";
            if (requestPath.contains("/m/")) {
                return session != null && null != session.getAttribute(this.name) ? null : new ServerRedirectView(h5Path);
            } else {
                return session != null && null != session.getAttribute(this.name) ? new ServerRedirectView(h5IndexPath) : new ServerRedirectView(h5Path);
            }
        } else {//PC,检查接口是不是对外的oa接口
            String requestPath = context.getRequest().getRequestURI();
            if (requestPath.contains("/api/oa/")) {

                boolean isOk = Boolean.FALSE;
                try {
                    BodyReaderHttpServletRequestWrapper requestWrapper = null;
                    if (context.getRequest() instanceof HttpServletRequest) {
                        requestWrapper = new BodyReaderHttpServletRequestWrapper(context.getRequest());
                    }
                    Object obj = Json.fromJson(Streams.utf8r(requestWrapper.getInputStream()));
                    if (obj != null) {
                        String oaUserAccount = (String) (((LinkedHashMap) obj).get("oaUserAccount"));
                        if (oaService == null)
                            oaService = context.getIoc().get(OaService.class);
                        if (StringUtils.isNotEmpty(oaUserAccount)) {
                            User user = oaService.getUserByOaUserAccount(oaUserAccount);
                            if (user != null) {
                                ShiroSession.setLoginUser(user);
                                isOk = Boolean.TRUE;
                            }
                        }
                    }
                    context.setRequest(requestWrapper);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!isOk) {
                    return (req, resp, obj) -> {
                        resp.setCharacterEncoding("UTF-8");
                        resp.setContentType("application/json; charset=utf-8");
                        resp.getWriter().write("{\"ok\":false,\"code\":\"401 \",\"msg\":\"你还没有授权，请去资产管理系统授权\"}");
                    };
                } else {
                    return null;

                }
            }
        }
        return session != null && null != session.getAttribute(this.name) ? null : new ServerRedirectView(this.path);
    }
}
