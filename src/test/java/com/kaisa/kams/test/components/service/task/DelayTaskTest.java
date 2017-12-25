package com.kaisa.kams.test.components.service.task;

import com.kaisa.kams.components.service.flow.ApproveWarnMessageService;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangqx on 2017/8/14.
 */
public class DelayTaskTest extends BaseTest {

    @Inject
    private ApproveWarnMessageService approveWarnMessageService;

    @Test
    public void testIoc() {

    }

    @Test
    public void testGetPhoneList() {
        List<String> roleList = new ArrayList<>();
        roleList.add("dd05f526-4dd6-4ab2-a7c2-c83d72c9e9d0");
//        List<String> phoneList = approveWarnMessageService.getPhoneList(roleList);
//        assertNotNull(phoneList);
    }

    @Test
    public void testGetApproveRole() {
//        List<String> roleList = approveWarnMessageService.getApproveRole("f7a87c73c1a4409686c244e16fe33c6b");
//        assertNotNull(roleList);
//        approveWarnMessageService.getPhoneList(roleList);
    }
}
