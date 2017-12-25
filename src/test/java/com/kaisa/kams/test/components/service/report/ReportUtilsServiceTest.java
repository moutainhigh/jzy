package com.kaisa.kams.test.components.service.report;

import com.kaisa.kams.components.service.base.ReportUtilsService;
import com.kaisa.kams.components.view.report.BusinessReportCommonData;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by wangqx on 2017/8/8.
 */
public class ReportUtilsServiceTest extends BaseTest {
    @Inject
    private ReportUtilsService reportUtilsService;

    @Test
    public void testGetInfo() {
        String loanId = "32652003-6368-40b7-b691-c515cec31948";
        BusinessReportCommonData data = reportUtilsService.getCommonInfo(loanId);
        assertNotNull(data);
        assertEquals(new BigDecimal("22300.00"),data.getActualTotalFeeAmount());
    }

    @Test
    public void testList() {
        List<String> list = null;
        for (String str : list) {

        }
    }
}
