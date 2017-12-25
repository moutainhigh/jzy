package com.kaisa.kams.test.components.service.product;

import com.kaisa.kams.components.service.LoanRepayService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.models.LoanRepay;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wangqx on 2017/6/19.
 */
public class LoanServiceTest extends BaseTest {
    @Inject
    LoanService loanService;

    @Inject
    LoanRepayService loanRepayService;

    @Inject
    Dao dao;

    @Test
    public void testGetMaxCode() {
        assertEquals("00001353",loanService.fetchMaxCode("44dcc7ac-1b55-4fe3-9c80-5e27ea9fe33d"));
    }

    @Test
    public void testGetOldLastLoanRepay() {
        LoanRepay oldLastLoanRepay = dao.fetch(LoanRepay.class, Cnd.where("loanId","=","c5ae2044-3a04-42d9-a99b-71a326d6e431").orderBy("period","desc"));
        assertEquals("621ca40d-60d4-40e6-be39-b454c2766bad",oldLastLoanRepay.getId());
    }

    @Test
    public void testStringFormat(){
        Object[] arr = {"1","2"};
        String  s = String.format("111%s111%s",arr);
        assertEquals("11111112",s);
    }


    @Test
    public void testJson() {
        Map object = (Map)Json.fromJson("{\"val\":[\"fa24a36e-14ce-41ef-aebe-c6f436cc0f53\",\"268e2850-754b-42aa-a341-007eb01dca2a\"]}");
        List<String>  list = (List) object.get("val");
        System.out.print(list);
    }

    @Test
    public void testGetRepayDate() {
        Date date = loanRepayService.getRepayDateByRepayId("eccdf4ea-b779-4052-894f-a96c3a14cf9f");
        System.out.print(date.toString()+"##############");

    }

}
