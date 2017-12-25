package com.kaisa.kams.components.aop;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.enums.OperationType;
import com.kaisa.kams.models.Audit;
import com.kaisa.kams.models.User;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.aop.InterceptorChain;
import org.nutz.aop.MethodInterceptor;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by sunwanchao on 2016/11/23.
 */
@IocBean
public class AuditInterceptor implements MethodInterceptor {

    private final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);
    private static final String OPERATION_INFO_FILE_PATH = "operation-type.json";

    private static final HashMap<String, HashMap<String, OperationType>> OPERATION_INFO = new HashMap<>(10);

    static {
        String json = Files.read(OPERATION_INFO_FILE_PATH);
        OPERATION_INFO.putAll((HashMap<String, HashMap<String, OperationType>>) Json.fromJson(NutType.mapStr(NutType.mapStr(OperationType.class)), json));
    }

    @Inject
    protected Dao dao;

    @Override
    public void filter(InterceptorChain chain) throws Throwable {
        log.info("audit interceptor begin.");
        chain.doChain();
        this.asyncInsertToAudit(chain);
        log.info("audit interceptor end.");
    }

    @Async
    public void asyncInsertToAudit(InterceptorChain chain) {
        //入库audit
        String method = chain.getCallingMethod().getName();
        String clazz = chain.getCallingMethod().getDeclaringClass().getName();
        String clazzName = clazz.substring(clazz.lastIndexOf(".") + 1);
        OperationType operationType = getOperationType(clazzName, method);
        if (null != operationType ) {
            addAudit(chain, operationType);
        }
    }

    private OperationType getOperationType(String clazzName, String method) {
        OperationType operationType = null;
        if (StringUtils.isEmpty(clazzName) || StringUtils.isEmpty(method)) {
            return operationType;
        }
        if (null == OPERATION_INFO.get(clazzName)) {
            return operationType;
        }
        if (null == OPERATION_INFO.get(clazzName).get(method)) {
            return operationType;
        }
        return OPERATION_INFO.get(clazzName).get(method);
    }

    private void addAudit(InterceptorChain chain, OperationType operationType) {
        User user = ShiroSession.getLoginUser();
        Audit audit = new Audit();
        if (user != null) {
            audit.setOperator(user.getName());
            audit.setUserId(user.getId());
        }
        audit.setDescription(split(chain.getArgs()));
        audit.setOperationType(operationType);
        audit.setCreateTime(new Date());
        dao.insert(audit);
    }

    /**
     * 取方法参数
     */
    private String split(Object[] args) {
        StringBuilder sb = new StringBuilder("");
        if (ArrayUtils.isEmpty(args)) {
            return sb.toString();
        }
        for (Object arg : args) {
            if (arg != null) {
                sb.append(arg.toString()).append(",");
            }
        }
        String result = sb.toString();
        if (sb.lastIndexOf(",") >= 0) {
            result = sb.substring(0, sb.lastIndexOf(","));
        }
        if (result.length() > 250) {
            result = result.substring(0, 250);
        }
        return result;
    }
}
