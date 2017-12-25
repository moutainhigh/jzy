package com.kaisa.kams.components.service.push;

import com.kaisa.kams.models.BillLoanRepay;
import com.kaisa.kams.models.push.BillLoanPush;
import com.kaisa.kams.models.push.LoanPushOrder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 票据推单票号关联服务层
 * @author by pengyueyang on 2017/11/6.
 */
@IocBean(fields = "dao")
public class BillLoanPushService extends Service {

    private final Logger log = LoggerFactory.getLogger(BillLoanPushService.class);

    public void batchSaveBills(String loanId, String loanPushId) {
        List<BillLoanPush> billLoanPushList = createBillLoanPushList(loanId, loanPushId);
        saveBillLoanPushList(billLoanPushList);
    }

    public void updateBillLoanPush(BillLoanPush billLoanPushArray[], String orderId) {
        if (ArrayUtils.isEmpty(billLoanPushArray) || StringUtils.isEmpty(orderId)) {
            return;
        }
        Trans.exec((Atom) () -> {
            List<BillLoanPush> billLoanPushList = Arrays.asList(billLoanPushArray);
            unRelateOrder(orderId);
            billLoanPushList.forEach(billLoanPush -> relateOrder(billLoanPush, orderId));
        });
    }

    private void relateOrder(BillLoanPush billLoanPush, String orderId) {
        int version = billLoanPush.getVersion();
        Cnd cnd = Cnd.where("id", "=", billLoanPush.getId()).and("pushOrderId","is",null).and("version", "=", version);
        Chain chain = Chain.makeSpecial("version", "+1").add("pushOrderId", orderId);
        int count = dao().update(BillLoanPush.class, chain, cnd);
        if (count != 1) {
            throw new RuntimeException("Relate bill to order error because update BillLoanPush");
        }
    }

    private void unRelateOrder(String orderId) {
        Cnd cnd = Cnd.where("pushOrderId", "=", orderId);
        Chain chain = Chain.makeSpecial("pushOrderId", null);
        dao().update(BillLoanPush.class, chain, cnd);
    }

    public List<BillLoanPush> getBillLoanPushListByPushId(String loanPushId) {
        if (StringUtils.isEmpty(loanPushId)) {
            return null;
        }
        Condition cnd = Cnd.where("pushId", "=", loanPushId).and("pushOrderId","is",null).asc("billNo");
        return dao().query(BillLoanPush.class, cnd);
    }

    public List<BillLoanPush> getBillLoanPushListByPushAndOrderId(String loanPushId, String loanPushOrderId) {
        if (StringUtils.isEmpty(loanPushId) || StringUtils.isEmpty(loanPushOrderId)) {
            return null;
        }
        SqlExpressionGroup orQuery = Cnd.exps("pushOrderId","is",null).or("pushOrderId", "=", loanPushOrderId);
        Condition cnd = Cnd.where("pushId", "=", loanPushId).and(orQuery).asc("billNo");
        return dao().query(BillLoanPush.class, cnd);
    }

    private void saveBillLoanPushList(List<BillLoanPush> billLoanPushList) {
        if (CollectionUtils.isNotEmpty(billLoanPushList)) {
            dao().insert(billLoanPushList);
        }
    }

    private List<BillLoanPush> createBillLoanPushList(String loanId, String loanPushId) {
        List<BillLoanRepay> billLoanRepayList = dao().query(BillLoanRepay.class, Cnd.where("loanId", "=", loanId));
        if (CollectionUtils.isNotEmpty(billLoanRepayList)) {
            List<BillLoanPush> result = new ArrayList<>();
            billLoanRepayList.forEach(loanRepay ->
                    result.add(createBillLoanPush(loanRepay, loanPushId))
            );
            return result;
        }
        return null;
    }

    private BillLoanPush createBillLoanPush(BillLoanRepay loanRepay, String loanPushId) {
        loanRepay = dao().fetchLinks(loanRepay, "loanRepay");
        BillLoanPush billLoanPush = new BillLoanPush();
        billLoanPush.setPushId(loanPushId);
        billLoanPush.setBillNo(loanRepay.getBillNo());
        billLoanPush.setBillAmount(loanRepay.getLoanRepay().getAmount());
        return billLoanPush;
    }
}
