package com.kaisa.kams.test.components.service.push;

import com.kaisa.kams.components.params.push.DataTableLoanPushParam;
import com.kaisa.kams.components.service.push.CarLoanStrategyService;
import com.kaisa.kams.components.service.push.LoanPushOrderService;
import com.kaisa.kams.components.service.push.LoanPushService;
import com.kaisa.kams.components.utils.ApiRequestUtil;
import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.components.view.push.AttachView;
import com.kaisa.kams.components.view.push.LoanPushOrderView;
import com.kaisa.kams.components.view.push.LoanPushView;
import com.kaisa.kams.enums.push.PushRepayMethodType;
import com.kaisa.kams.models.push.LoanPush;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pengyueyang created on 2017/11/7.
 */
public class LoanPushServiceTest extends BaseTest {

    @Inject
    private LoanPushService loanPushService;

    @Inject
    private LoanPushOrderService loanPushOrderService;

    @Inject
    private CarLoanStrategyService carLoanStrategyService;

    @Test
    public void testQueryUser() {
        ApiRequestUtil.queryUserByAPI("18602020282");
    }

    @Test
    public void testSendSms() {
        Map<String, String> msg = new HashMap<>();
        msg.put("loanCode", "1");
        msg.put("productType", "1");
        msg.put("minutes", "1");
        //执行发送短信
        ApiRequestUtil.sendSmsByAPI("13798366852", msg, "257");
    }

    @Test
    public void testQueryList() {
        DataTableLoanPushParam param = new DataTableLoanPushParam();
        param.setStart(0);
        param.setLength(20);
        assertEquals(3, loanPushService.queryListByParam(param).size());
    }


    @Test
    public void testGetCount() {
        DataTableLoanPushParam param = new DataTableLoanPushParam();
        assertEquals(3, loanPushService.countByParam(param));
    }

    @Test
    public void testGetLoanPushById() {
        String id = "47b6e7d5-4895-47d0-8df5-f21f1c9f622e";
        LoanPushView view = loanPushService.getLoanPushById(id);
        assertNotNull(view);
    }

    @Test
    public void testPushRepayMethodType() {
        assertEquals("一次性还款",PushRepayMethodType.getDescriptionByName("BULLET_REPAYMENT"));
    }

    @Test
    public void testMessageFormat() {
        String message = "Oh, ${name} is a pig";
        Map<String,String> value = new HashMap<>();
        value.put("name","yubei");
        assertEquals("Oh, yubei is a pig", StringFormatUtils.format(message,value));
    }

    @Test
    public void testGetBasic() {
        String id = "82c90ad6-90a2-4b9e-9077-d36e07c971a3";
        LoanPush loanPush = loanPushService.getLoanPush(id);
        carLoanStrategyService.getInitContent(loanPush);
    }

    @Test
    public void testGetAttachList() {
        String id = "9b53cc16-52a1-446a-9319-f42480dbb0c3";
        List<AttachView> list = loanPushService.getAttachList(id);
        assertEquals(9,loanPushService.getAttachList(id).size());
    }

    @Test
    public void testGetParamJson() {
        ArrayList<LoanPushOrderView> object  = new ArrayList<>();
        object.add(new LoanPushOrderView());
        String paramJson = Json.toJson(object,JsonFormat.full());
        assertNotNull(paramJson);
    }
}
