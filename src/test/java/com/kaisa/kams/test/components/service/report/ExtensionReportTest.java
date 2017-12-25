package com.kaisa.kams.test.components.service.report;

import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.service.report.ExtensionReportService;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;

/**
 * @author pengyueyang created on 2017/12/14.
 */
public class ExtensionReportTest extends BaseTest {
    @Inject
    private ExtensionReportService extensionReportService;

    @Test
    public void testGetList() {
        DataTableFinanceReportParam param = new DataTableFinanceReportParam();
        param.setLength(1);
        param.setStart(0);
        assertEquals(184,extensionReportService.getCount(param));
        System.out.println("###############");
        System.out.println(Json.toJson(extensionReportService.getList(param)));
        System.out.println("###############");
    }

}
