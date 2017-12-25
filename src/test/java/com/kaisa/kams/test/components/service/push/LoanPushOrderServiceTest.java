package com.kaisa.kams.test.components.service.push;

import com.kaisa.kams.components.params.base.DataTableBaseParam;
import com.kaisa.kams.components.params.push.LoanPushApproval;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.service.push.LoanPushOrderApprovalService;
import com.kaisa.kams.components.service.push.LoanPushOrderService;
import com.kaisa.kams.components.view.push.LoanPushOrderView;
import com.kaisa.kams.components.view.push.LoanPushView;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.push.BillLoanPush;
import com.kaisa.kams.models.push.LoanPushOrder;
import com.kaisa.kams.test.BaseTest;

import org.junit.Assert;
import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.WorkItem;

import java.util.ArrayList;
import java.util.List;

import mockit.Tested;

/**
 * @author pengyueyang created on 2017/11/15.
 */
public class LoanPushOrderServiceTest extends BaseTest{


    @Inject
    private LoanPushOrderService loanPushOrderService;

    @Inject
    private LoanPushOrderApprovalService loanPushOrderApprovalService;

    @Inject
    private UserService userService;

    @Inject
    private FlowService flowService;

    @Tested
    LoanPushOrder order;



    @Test
    public void testStringReplaceMethod() {
        String sourceStr = "1,2,3";
        String targetStr = sourceStr.replaceAll(",",";");
//        assertEquals(sourceStr,targetStr);
    }

    @Test
    public void testCreateLoanPushOrder() {
        order.setPushId("56936340-150d-4be7-8938-a6591fc98dba");
        order.setLoanId("82c90ad6-90a2-4b9e-9077-d36e07c971a3");
        Assert.assertNotNull(order);
        loanPushOrderService.save(order);
    }

    @Test
    public void testStartLoanPushOrderApproval() {
        LoanPushOrder loanPushOrder = loanPushOrderService.getLoanPushOrderById("45f991c5-d205-44ad-8997-19731a7a8e41");
        loanPushOrderApprovalService.startFlow(loanPushOrder);
    }

    @Test
    public void testGetRoleIds(){
//        assertEquals(15,loanPushOrderApprovalService.getRoleList("077cb1ba-c8c4-11e6-87f0-e633af8ff460").length);
    }

    @Test
    public void testTask() {
        Task task = flowService.getEngine().query().getTask("3029ee90e64442bb90caf1f5bf5eadcd");
        assertEquals(null,task);
    }

    @Test
    public void testGetObjectJson() {
        ArrayList<BillLoanPush> arrayList = new ArrayList<>();
        arrayList.add(new BillLoanPush());
        System.out.println("##################");
        System.out.println(Json.toJson(arrayList, JsonFormat.full()));

        System.out.println(Json.toJson(new LoanPushOrder(), JsonFormat.full()));

        System.out.println(Json.toJson(new DataTableBaseParam(), JsonFormat.full()));

        List<LoanPushOrderView> list = new ArrayList<>();
        list.add(new LoanPushOrderView());

        System.out.println(Json.toJson(list, JsonFormat.full()));

        System.out.println(Json.toJson(new DataTables(), JsonFormat.full()));

        System.out.println(Json.toJson(new WorkItem(), JsonFormat.full()));

        System.out.println(Json.toJson(new LoanPushApproval(), JsonFormat.full()));

        System.out.println(Json.toJson(new LoanPushView(), JsonFormat.full()));


        System.out.println("##################");

    }

    @Test
    public void testSqlCondition() {

    }

    @Test
    public void testLoop() {
        assertEquals(true,fun());
    }

    public boolean fun() {
        ArrayList<String> list = new ArrayList<>(4);
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        for(String str:list) {
            if (str.equals("c")) {
                return true;
            }
        }
        return false;
    }

}
