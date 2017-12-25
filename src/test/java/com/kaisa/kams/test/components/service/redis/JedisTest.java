package com.kaisa.kams.test.components.service.redis;

import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.ioc.loader.annotation.Inject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by wangqx on 2017/7/5.
 */
public class JedisTest extends BaseTest {
    @Inject
    JedisPool jedisPool;

    @Inject
    JedisAgent jedisAgent;

    @Test
    public void testJedisPool() {
        assertEquals("1",jedisPool.getResource().get("user"));
    }

    @Test
    public void testSessionSize() {
        assertEquals(1,jedisPool.getResource().get("shiro-activeSessionCache:pfd125tag2ih8rvlh749l04b4g").toString().length());
    }

    @Test
    public void testJedisPoolInfo() {
        Jedis jedis = null;
        for(int i = 0; i < 101; i++) {
            try {
                jedis = jedisAgent.getResource();
                jedis.set("foo", "2017-05-04 14:17:12,894 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=1599628260\n" +
                        "2017-05-04 14:17:12,894 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=1599628260\n" +
                        "2017-05-04 14:17:12,895 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=1910250962\n" +
                        "2017-05-04 14:17:12,898 org.snaker.engine.model.DecisionModel.exec(DecisionModel.java:56) INFO  - 8682e96bb0484f5b86f5d14bb1b0a794->decision execution.getArgs():{amount=145500.0, loanId=235723ce-c3b5-40d5-9732-b67c28b6694c}\n" +
                        "2017-05-04 14:17:12,898 org.snaker.engine.model.DecisionModel.exec(DecisionModel.java:60) INFO  - expression is org.snaker.engine.impl.JuelExpression@299f1291\n" +
                        "2017-05-04 14:17:12,898 org.snaker.engine.model.DecisionModel.exec(DecisionModel.java:68) INFO  - 8682e96bb0484f5b86f5d14bb1b0a794->decision expression[expr=${amount > 0.00 ? 'F1DecisionTransition' : 'F1DecisionTransitionNext'}] return result:F1DecisionTransition\n" +
                        "2017-05-04 14:17:12,902 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=1910250962\n" +
                        "2017-05-04 14:17:15,435 org.apache.shiro.realm.AuthorizingRealm.getAuthorizationCacheLazy(AuthorizingRealm.java:248) INFO  - No cache or cacheManager properties have been set.  Authorization cache cannot be obtained.\n" +
                        "2017-05-04 14:17:43,537 org.apache.shiro.realm.AuthorizingRealm.getAuthorizationCacheLazy(AuthorizingRealm.java:248) INFO  - No cache or cacheManager properties have been set.  Authorization cache cannot be obtained.\n" +
                        "2017-05-04 14:17:44,544 org.apache.shiro.realm.AuthorizingRealm.getAuthorizationCacheLazy(AuthorizingRealm.java:248) INFO  - No cache or cacheManager properties have been set.  Authorization cache cannot be obtained.\n" +
                        "2017-05-04 14:17:59,304 org.apache.shiro.realm.AuthorizingRealm.getAuthorizationCacheLazy(AuthorizingRealm.java:248) INFO  - No cache or cacheManager properties have been set.  Authorization cache cannot be obtained.\n" +
                        "2017-05-04 14:19:06,556 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=163207703\n" +
                        "2017-05-04 14:19:06,561 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=163207703\n" +
                        "2017-05-04 14:19:08,666 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=1873978564\n" +
                        "2017-05-04 14:19:08,668 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=1873978564\n" +
                        "2017-05-04 14:19:08,669 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=335277869\n" +
                        "2017-05-04 14:19:08,673 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=335277869\n" +
                        "2017-05-04 14:19:08,674 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=882581094\n" +
                        "2017-05-04 14:19:08,677 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=882581094\n" +
                        "2017-05-04 14:19:13,717 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=639293855\n" +
                        "2017-05-04 14:19:13,724 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=639293855\n" +
                        "2017-05-04 14:19:13,725 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=413596277\n" +
                        "2017-05-04 14:19:13,729 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=413596277\n" +
                        "2017-05-04 14:19:13,730 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=588520534\n" +
                        "2017-05-04 14:19:13,734 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=588520534\n" +
                        "2017-05-04 14:19:50,283 org.apache.shiro.realm.AuthorizingRealm.getAuthorizationCacheLazy(AuthorizingRealm.java:248) INFO  - No cache or cacheManager properties have been set.  Authorization cache cannot be obtained.\n" +
                        "2017-05-04 14:20:22,773 org.apache.shiro.realm.AuthorizingRealm.getAuthorizationCacheLazy(AuthorizingRealm.java:248) INFO  - No cache or cacheManager properties have been set.  Authorization cache cannot be obtained.\n" +
                        "2017-05-04 14:20:25,053 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.getTransaction(DataSourceTransactionInterceptor.java:52) INFO  - begin transaction=238686639\n" +
                        "2017-05-04 14:20:25,053 org.snaker.engine.access.transaction.DataSourceTransactionInterceptor.commit(DataSourceTransactionInterceptor.java:68) INFO  - commit transaction=238686639");
                System.out.println("第" + (i+1) + "个连接, 得到的值为" + jedis.get("foo"));
            } finally {
                if (jedis != null) {
                  jedis.close();
                }
            }
        }
    }



}
