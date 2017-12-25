package com.kaisa.kams.test;



import org.junit.Assert;
import org.junit.runner.RunWith;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

/**
 * 测试父类 用户获取注入对象
 * Created by weid on 2016/11/23.
 */
@RunWith(NutBaseTestRunner.class)
@IocBean // 必须有
public class BaseTest extends Assert {
    @Inject("refer:$ioc")
    protected Ioc ioc;
}
