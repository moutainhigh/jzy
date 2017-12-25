package com.kaisa.kams.components.aop;

import org.nutz.lang.Stopwatch;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 处理每次请求时间
 * TODO 目录结构应该改变，暂时放这里，确定后再移动
 * Created by lenovo on 2016/11/23.
 */
public class LogTimeProcessor extends AbstractProcessor {

    private final Logger log = LoggerFactory.getLogger(LogTimeProcessor.class);

    @Override
    public void process(ActionContext ac) throws Throwable {
        Stopwatch sw = Stopwatch.begin();
        try {
            doNext(ac);
        } finally {
            sw.stop();
            if (log.isDebugEnabled()) {
                HttpServletRequest req = ac.getRequest();
                log.info("Method={},URI={},use {}ms", req.getMethod(), req.getRequestURI(), sw.getDuration());
            }
        }
    }

}