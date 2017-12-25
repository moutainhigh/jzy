package com.kaisa.kams.test;

import com.kaisa.MainModule;

import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.nutz.ioc.Ioc;
import org.nutz.mock.NutTestRunner;

import mockit.integration.junit4.JMockit;
import mockit.internal.startup.Startup;

/**
 * Created by wangqx on 2017/6/12.
 */
public class NutBaseTestRunner extends NutTestRunner {

    static {
        Startup.initializeIfPossible();
    }

    public NutBaseTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    public Class<?> getMainModule() {
        return MainModule.class;
    }

    /**
     * 可覆盖createIoc,实现参数覆盖, bean替换,等定制.
     */
    protected Ioc createIoc() {
        return super.createIoc();
    }
}
