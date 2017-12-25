package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.params.push.DataTableLoanPushParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.view.push.AttachView;
import com.kaisa.kams.components.view.push.LoanPushView;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.enums.push.ItemType;
import com.kaisa.kams.enums.push.LoanPushStatus;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.LoanSubject;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductInfoTmpl;
import com.kaisa.kams.models.ProductType;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.BusinessUser;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.entity.Task;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


/**
 * 推单服务层
 *
 * @author pengyueyang
 */
@IocBean(fields = "dao")
public class LoanPushService extends Service {

    private static final Logger log = LoggerFactory.getLogger(LoanPushService.class);

    private final String LEADER_APPROVAL_PREFIX = "G";
    private final String CREATOR = "系统";
    private final String SOURCE_SELF = "0";
    private final String SOURCE_CHANNEL = "1";
    private final String SOURCE_SELF_CN = "自营|";
    private final String SOURCE_CHANNEL_CN = "渠道|";
    private final String STRIKE = "-";

    @Inject
    private BillLoanPushService billLoanPushService;

    public List<LoanPushView> queryListByParam(DataTableLoanPushParam queryParam) {
        String querySql = "select lp.*,l.actualAmount,l.loanTime,l.loanStatus,l.clearDate from " +
                "sl_loan_push lp left join sl_loan l on lp.loanId=l.id $condition";
        Sql sql = Sqls.queryEntity(querySql);
        Entity<LoanPushView> entity = dao().getEntity(LoanPushView.class);
        sql.setEntity(entity);
        sql.setPager(DataTablesUtil.getDataTableToPager(queryParam.getStart(), queryParam.getLength()));
        Criteria cnd = setQueryCnd(queryParam);
        setQueryOrder(cnd, queryParam.getLoanPushStatus());
        sql.setCondition(cnd);
        dao().execute(sql);
        return convertList(sql.getList(LoanPushView.class));
    }


    public int countByParam(DataTableLoanPushParam queryParam) {
        String countSql = "select count(*) from sl_loan_push lp $condition";
        Sql sql = Sqls.fetchInt(countSql);
        sql.setCondition(setQueryCnd(queryParam));
        dao().execute(sql);
        return sql.getInt();
    }

    public boolean update(LoanPush loanPush) {
        loanPush.setUpdateBy(CREATOR);
        loanPush.setUpdateTime(new Date());
        FieldFilter filter = FieldFilter.create(LoanPush.class, "^status|loanMaxDueDate|updateBy|updateTime$", true);
        return Daos.ext(dao(), filter).update(loanPush) > 0;
    }

    public LoanPushView getLoanPushById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        String querySql = "select lp.*,l.createBy loanCreateBy,l.submitTime from " +
                "sl_loan_push lp left join sl_loan l on lp.loanId=l.id $condition";
        Sql sql = Sqls.queryEntity(querySql);
        Entity<LoanPushView> entity = dao().getEntity(LoanPushView.class);
        sql.setEntity(entity);
        sql.setCondition(Cnd.where("lp.id", "=", id));
        dao().execute(sql);
        return sql.getObject(LoanPushView.class);
    }

    public LoanPush getLoanPush(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return dao().fetch(LoanPush.class, id);
    }

    public List<AttachView> getAttachList(String loanId) {
        if (StringUtils.isEmpty(loanId)) {
            return null;
        }
        String querySql = "select a.itemName,GROUP_CONCAT(ad.url) urls,GROUP_CONCAT(ad.attachName) attachNames " +
                "from sl_product_media_attach a " +
                "left join sl_product_media_attach_detail ad on a.id=ad.productMediaAttachId " +
                "where a.loanId= @loanId group by a.itemName order by a.itemName";
        Sql sql = Sqls.queryEntity(querySql);
        Entity<AttachView> entity = dao().getEntity(AttachView.class);
        sql.setEntity(entity);
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        return sql.getList(AttachView.class);
    }

    @Async
    public void saveLoanPush(Loan loan) {
//        if (!firstLeaderApprovalCompleted(task, taskList)) {
//            return;
//        }
        if (hasInsertedPushLoan(loan.getId())) {
            return;
        }
        try {
            LoanPush loanPush = dao().insert(createLoanPush(loan));
            batchSaveBills(loanPush);
        } catch (Exception e) {
            log.error("Create loanPush error loanId is:{} error msg is:{}", loan.getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    private List<LoanPushView> convertList(List<LoanPushView> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        list.forEach(loanPushView -> convert(loanPushView));
        return list;
    }

    private void convert(LoanPushView loanPushView) {
        loanPushView.setSource(getSource(loanPushView));
        loanPushView.setDueDate(getDueDateStr(loanPushView));
        loanPushView.setTermStr(LoanTermType.getTermStr(loanPushView.getTermType(), loanPushView.getTerm()));
    }

    private void setQueryOrder(Criteria cnd, LoanPushStatus loanPushStatus) {
        if (LoanPushStatus.STAY_PUSH.equals(loanPushStatus)) {
            cnd.getOrderBy().desc("lp.leaderApprovedTime");
            return;
        }
        if (LoanPushStatus.PART_PUSHED.equals(loanPushStatus)) {
            cnd.getOrderBy().asc("lp.loanMaxDueDate");
            return;
        }
        if (LoanPushStatus.PUSHED.equals(loanPushStatus)) {
            cnd.getOrderBy().desc("lp.loanMaxDueDate");
        }
    }

    private Criteria setQueryCnd(DataTableLoanPushParam queryParam) {
        Criteria cri = Cnd.cri();
        if (null == queryParam) {
            return cri;
        }
        if (StringUtils.isNotEmpty(queryParam.getLoanCode())) {
            cri.where().andEquals("lp.loanCode", queryParam.getLoanCode());
        }
        if (StringUtils.isNotEmpty(queryParam.getSaleName())) {
            cri.where().andEquals("lp.saleName", queryParam.getSaleName());
        }
        if (StringUtils.isNotEmpty(queryParam.getMasterBorrowerName())) {
            cri.where().andEquals("lp.masterBorrowerName", queryParam.getMasterBorrowerName());
        }
        if (StringUtils.isNotEmpty(queryParam.getChannelName())) {
            cri.where().andEquals("lp.channelName", queryParam.getChannelName());
        }
        if (StringUtils.isNotEmpty(queryParam.getProductTypeId())) {
            cri.where().andEquals("lp.productTypeId", queryParam.getProductTypeId());
        }
        if (StringUtils.isNotEmpty(queryParam.getProductId())) {
            cri.where().andEquals("lp.productId", queryParam.getProductId());
        }
        if (StringUtils.isNotEmpty(queryParam.getLoanSubjectId())) {
            cri.where().andEquals("lp.loanSubjectId", queryParam.getLoanSubjectId());
        }
        if (null != queryParam.getLoanPushStatus()) {
            cri.where().andEquals("lp.status", queryParam.getLoanPushStatus());
            if (LoanPushStatus.PUSHED.equals(queryParam.getLoanPushStatus()) && StringUtils.isNotEmpty(queryParam.getLoanMaxDueDate())) {
                cri.where().andBetween("lp.loanMaxDueDate", queryParam.getLoanMaxBeginDueDate(), queryParam.getLoanMaxEndDueDate());
            }
            if (LoanPushStatus.PART_PUSHED.equals(queryParam.getLoanPushStatus()) && StringUtils.isNotEmpty(queryParam.getLoanMaxDueDate())) {
                cri.where().andBetween("lp.loanMinDueDate", queryParam.getLoanMaxBeginDueDate(), queryParam.getLoanMaxEndDueDate());
            }
        }

        return cri;
    }

    private void batchSaveBills(LoanPush loanPush) {
        if (ItemType.isBill(loanPush.getItemType())) {
            billLoanPushService.batchSaveBills(loanPush.getLoanId(), loanPush.getId());
        }
    }

    private boolean hasInsertedPushLoan(String loanId) {
        return dao().fetch(LoanPush.class, Cnd.where("loanId", "=", loanId)) == null ? false : true;
    }

    private boolean firstLeaderApprovalCompleted(Task task, List<Task> taskList) {
        if (null == task) {
            return false;
        }
        if (!task.getTaskName().startsWith(LEADER_APPROVAL_PREFIX)) {
            return false;
        }
        if (CollectionUtils.isEmpty(taskList)) {
            return true;
        }
        Task nextTask = taskList.get(0);
        if (!nextTask.getTaskName().startsWith(LEADER_APPROVAL_PREFIX)) {
            return true;
        }
        return false;
    }

    private LoanPush createLoanPush(Loan loan) throws RuntimeException {
        LoanPush loanPush = new LoanPush();
        loanPush = setInsertBaseInfo(loanPush);
        loanPush = setProductInfo(loanPush, loan);
        loanPush = setLoanSubjectInfo(loanPush, loan);
        loanPush = setSourceInfo(loanPush, loan);
        loanPush = setBorrowerInfo(loanPush, loan);
        loanPush.setAmount(loan.getAmount());
        loanPush.setExtensionLabel(false);
        loanPush.setLoanCode(loan.getCode());
        loanPush.setLoanId(loan.getId());
        loanPush.setStatus(LoanPushStatus.STAY_PUSH);
        loanPush.setTermType(loan.getTermType());
        loanPush.setTerm(loan.getTerm());
        loanPush.setSaleName(loan.getSaleName());
        return loanPush;
    }

    private LoanPush setBorrowerInfo(LoanPush loanPush, Loan loan) {
        LoanBorrower borrower = dao().fetch(LoanBorrower.class, loan.getMasterBorrowerId());
        if (null != borrower) {
            loanPush.setMasterBorrowerName(borrower.getName());
        }
        return loanPush;
    }

    private LoanPush setSourceInfo(LoanPush loanPush, Loan loan) {
        Channel channel = null;
        if (null != loan.getChannelId()) {
            channel = dao().fetch(Channel.class, loan.getChannelId());
        }
        if (null == channel || SOURCE_SELF.equals(channel.getChannelType())) {
            loanPush.setSourceType(SOURCE_SELF);
            BusinessUser user = dao().fetch(BusinessUser.class, loan.getSaleId());
            BusinessOrganize organize = dao().fetchLinks(user, "organize").getOrganize();
            if (null != organize) {
                loanPush.setBusinessLine(organize.getBusinessLine().getDescription());
                loanPush.setOrganizeCode(organize.getCode());
            }
        } else {
            loanPush.setSourceType(SOURCE_CHANNEL);
            loanPush.setChannelName(channel.getName());
        }
        return loanPush;
    }

    private LoanPush setLoanSubjectInfo(LoanPush loanPush, Loan loan) {
        loanPush.setLoanSubjectId(loan.getLoanSubjectId());
        LoanSubject subject = dao().fetch(LoanSubject.class, loan.getLoanSubjectId());
        if (null != subject) {
            loanPush.setLoanSubjectName(subject.getName());
        }
        return loanPush;
    }

    private LoanPush setProductInfo(LoanPush loanPush, Loan loan) {
        Product product = dao().fetch(Product.class, loan.getProductId());
        ProductType productType = dao().fetch(ProductType.class, loan.getProductTypeId());
        loanPush.setProductId(loan.getProductId());
        loanPush.setProductTypeId(loan.getProductTypeId());
        if (null != product) {
            loanPush.setProductName(product.getName());
        }
        if (null != productType) {
            loanPush.setProductTypeName(productType.getName());
        }

        loanPush.setItemType(getItemType(product.getInfoTmpId()));
        return loanPush;
    }

    private ItemType getItemType(String infoTmpId) {
        ProductInfoTmpl infoTemp = dao().fetch(ProductInfoTmpl.class, infoTmpId);
        if (infoTemp != null) {
            return convertTempTypeToItemType(infoTemp.getProductTempType());
        }
        return null;
    }

    private LoanPush setInsertBaseInfo(LoanPush loanPush) {
        Date now = new Date();
        loanPush.setCreateTime(now);
        loanPush.setCreateBy(CREATOR);
        loanPush.setLeaderApprovedTime(now);
        return loanPush;
    }

    private ItemType convertTempTypeToItemType(ProductTempType productTempType) {
        if (null != productTempType) {
            switch (productTempType) {
                case RRC:
                case CHEDAI:
                    return ItemType.CAR_LOAN;
                case HONGBEN:
                    return ItemType.HOUSE_MORTGAGE_LOAN;
                case SHULOUPLAT:
                case SHULOU:
                    return ItemType.BANK_HOUSE_LOAN;
                case GERENDAI:
                    return ItemType.PERSONAL_LOAN;
                case BAOLI:
                    return ItemType.FACTORING;
                case PIAOJU:
                    return ItemType.BILL;
                case YINPIAO:
                    return ItemType.BANK_BILL;
                default:
                    return null;
            }
        }
        return null;
    }

    private String getSource(LoanPushView view) {
        if (SOURCE_SELF.equals(view.getSourceType())) {
            return SOURCE_SELF_CN + view.getBusinessLine() + STRIKE + view.getOrganizeCode() + STRIKE + view.getSaleName();
        }
        return SOURCE_CHANNEL_CN + view.getChannelName();
    }

    private String getDueDateStr(LoanPushView view) {
        Date loanTime = view.getLoanTime();
        if (null == loanTime) {
            return "";
        }
        if (LoanTermType.FIXED_DATE.equals(view.getTermType())) {
            return view.getTerm();
        }
        loanTime = getDueDate(loanTime, view.getTerm(), view.getTermType());
        return DateUtil.formatDateToString(loanTime);
    }

    private Date getDueDate(Date date, String term, LoanTermType termType) {
        if (LoanTermType.DAYS.equals(termType)) {
            return DateUtil.addDays(date, Integer.valueOf(term), 0, 0);
        }
        if (LoanTermType.MOTHS.equals(termType)) {
            return DateUtil.addDays(date, 0, Integer.valueOf(term), 0);
        }
        if (LoanTermType.SEASONS.equals(termType)) {
            return DateUtil.addDays(date, 0, 3 * Integer.valueOf(term), 0);
        }
        return date;
    }

    public boolean isComplete(LoanPush loanPush) {
        Date platformLoanMaxDueDate = loanPush.getLoanMaxDueDate();
        if (null == platformLoanMaxDueDate) {
            return false;
        }
        Loan loan = dao().fetch(Loan.class, loanPush.getLoanId());
        if (null == loan) {
            return false;
        }
        Date loanDueDate = getLoanDueDate(loan);
        return DateUtil.daysBetweenTowDate(loanDueDate, platformLoanMaxDueDate) >= 0;
    }

    private Date getLoanDueDate(Loan loan) {
        if (null == loan.getLoanTime()) {
            if (LoanTermType.FIXED_DATE.equals(loan.getTermType())) {
                return DateUtil.parseStringToDate(loan.getTerm());
            }
            return getDueDate(new Date(), loan.getTerm(), loan.getTermType());
        }
        Sql sql = Sqls.fetchTimestamp("select max(dueDate) from sl_loan_repay where loanId=@loanId");
        sql.setParam("loanId", loan.getId());
        dao().execute(sql);
        return (Date) sql.getResult();
    }

    public boolean completePush(LoanPush loanPush) {
        loanPush.setStatus(LoanPushStatus.PUSHED);
        return 1 == dao().update(loanPush, "^status$");
    }
}
