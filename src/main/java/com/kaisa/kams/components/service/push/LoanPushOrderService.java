package com.kaisa.kams.components.service.push;


import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.components.view.push.LoanPushOrderView;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.enums.push.ItemType;
import com.kaisa.kams.enums.push.LoanPushOrderStatus;
import com.kaisa.kams.enums.push.PushRepayMethodType;
import com.kaisa.kams.models.push.LoanPush;
import com.kaisa.kams.models.push.LoanPushOrder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 推单订单服务类
 *
 * @author pengyueyang created on 2017/11/8.
 */
@IocBean(fields = "dao")
public class LoanPushOrderService extends Service {

    private final Logger log = LoggerFactory.getLogger(LoanPushOrderService.class);

    private BaseItemTypeStrategyService baseItemTypeStrategyService;

    /**
     * 通过业务推单id获取推单记录列表
     */
    public List<LoanPushOrderView> getListByLoanPushId(String loanPushId) {
        String querySql = "select * from sl_loan_push_order $condition";
        Sql sql = Sqls.queryEntity(querySql);
        Entity<LoanPushOrderView> entity = dao().getEntity(LoanPushOrderView.class);
        sql.setEntity(entity);
        Condition cnd = Cnd.where("pushId", "=", loanPushId).desc("createTime");
        sql.setCondition(cnd);
        dao().execute(sql);
        return convertList(sql.getList(LoanPushOrderView.class));
    }

    /**
     * 根据id获取推单订单
     */
    public LoanPushOrder getLoanPushOrderById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return dao().fetch(LoanPushOrder.class, id);
    }

    /**
     * 编辑更新
     */
    public boolean updateByEdit(LoanPushOrder loanPushOrder) {
        loanPushOrder.setUpdateBy(ShiroSession.getLoginUser().getName());
        loanPushOrder.setUpdateTime(new Date());
        String updateRegex = "^updateBy|updateTime|pushTarget|platformBorrower|platformBorrowerName|" +
                "platformBorrowerId|amount|repayMethod|term|termType|channelRate|content|maxInvestor$";
        return dao().update(loanPushOrder, updateRegex) > 0;
    }

    /**
     * 获取推单订单初始化信息
     */
    public String getInitContent(LoanPush loanPush) {
        if (null == loanPush) {
            return "";
        }
        setStrategy(loanPush.getItemType());
        return baseItemTypeStrategyService.getInitContent(loanPush);
    }

    /**
     * 保存推单订单信息
     */
    public LoanPushOrder save(LoanPushOrder loanPushOrder) {
        Date now = new Date();
        loanPushOrder.setCreateBy(ShiroSession.getLoginUser().getName());
        loanPushOrder.setCreateTime(now);
        loanPushOrder.setStatus(LoanPushOrderStatus.EDIT);
        loanPushOrder.setCode(getCode(now));
        return dao().insert(loanPushOrder);
    }

    private String getCode(Date date) {
        String dateStr = TimeUtils.formatDate("yyyyMMdd", date);
        String maxCode = (String) dao().func2(LoanPushOrder.class, "max", "code", Cnd.where("DATE_FORMAT(createTime,'%Y%m%d')", "=", dateStr));
        if (null == maxCode) {
            return dateStr+String.format("%05d", 1);
        }
        int maxCodeValue = Integer.valueOf(maxCode.substring(maxCode.length() - 5)) + 1;
        return dateStr+String.format("%05d", maxCodeValue);
    }

    /**
     * 获取初始化信息
     */
    public HashMap<String, Object> getInitInfo(LoanPush loanPush) {
        HashMap<String, Object> initInfo = new HashMap<>(5);
        initInfo.put("pushId", loanPush.getId());
        initInfo.put("loanId", loanPush.getLoanId());
        initInfo.put("productTypeName", loanPush.getProductTypeName());
        initInfo.put("loanSubjectName", loanPush.getLoanSubjectName());
        initInfo.put("amount", loanPush.getAmount());
        initInfo.put("itemType", loanPush.getItemType());
        return initInfo;
    }

    private List<LoanPushOrderView> convertList(List<LoanPushOrderView> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        list.forEach(loanPushOrderView -> convert(loanPushOrderView));
        return list;
    }

    private void convert(LoanPushOrderView loanPushOrderView) {
        loanPushOrderView.setTermStr(LoanTermType.getTermStr(loanPushOrderView.getTermType(), loanPushOrderView.getTerm()));
        loanPushOrderView.setRepayMethod(PushRepayMethodType.getDescriptionByName(loanPushOrderView.getRepayMethod()));
        loanPushOrderView.setItemType(ItemType.getDescriptionByName(loanPushOrderView.getItemType()));
    }

    private void setStrategy(ItemType itemType) {
        baseItemTypeStrategyService = BaseItemTypeStrategyService.newStrategy(itemType);
    }


}
