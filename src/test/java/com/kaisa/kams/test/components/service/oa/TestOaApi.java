package com.kaisa.kams.test.components.service.oa;


import com.kaisa.kams.components.utils.HttpClientUtil;

import org.junit.Test;

/**
 * Created by wangqx on 2017/8/31.
 */
public class TestOaApi {
    @Test
    public void testQueryApprovalListNum() {
//        String json = "{\"oaUserAccount\": \"zhangsan\"}";
//        String result = HttpClientUtil.sendPost(json,"http://kams-test1.shenxin99.com/api/oa/query_approval_list_num");
//        System.out.print(result);
    }

    @Test
    public void testQueryDetail() {
        String json = "{\n" +
                "\t\"oaUserAccount\": \"zhangsan\",\n" +
                "\t\"searchKeys\": {\n" +
                "\t\t\"loanId\": \"43786581-f389-4184-8a12-9f7e3cc1e0ab\"\n" +
                "\t}\n" +
                "}";
//        String result = HttpClientUtil.sendPost(json,"http://kams-test1.shenxin99.com/api/oa/finance_approval_detail");
//        System.out.print(result);
    }
}
