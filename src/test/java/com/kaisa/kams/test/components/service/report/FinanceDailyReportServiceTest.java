package com.kaisa.kams.test.components.service.report;


import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.report.FinanceDailyReportService;
import com.kaisa.kams.models.report.BillFinanceDailyReport;
import com.kaisa.kams.test.BaseTest;

import org.apache.shiro.crypto.hash.Hash;
import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by wangqx on 2017/6/12.
 */
public class FinanceDailyReportServiceTest extends BaseTest {
   @Inject
   private FinanceDailyReportService financeDailyReportService;

    @Test
    public void testGetBillList() {
        financeDailyReportService.executeDailyJob("2017-06-28");
    }



    @Test
    public void testQuery() {
        DataTableParam param = new DataTableParam();
        param.setLength(20);
        param.setStart(0);
        Map<String,String> searchKeys = new HashMap<>();
        searchKeys.put("report_name","GERENDAI");
        searchKeys.put("report_time","2017-06-29");
        param.setSearchKeys(searchKeys);
        String json = Json.toJson(financeDailyReportService.getReportDataTables(param));
        System.out.print(json);
    }

}
