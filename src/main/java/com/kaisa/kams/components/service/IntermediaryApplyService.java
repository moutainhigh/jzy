package com.kaisa.kams.components.service;

import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;

import com.kaisa.kams.models.flow.ApprovalResult;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by lw on 2017/8/44.
 */
@IocBean(fields = "dao")
public class IntermediaryApplyService extends IdNameEntityService<IntermediaryApply> {
    private static final Log log = Logs.get();

    @Inject
    private LoanService loanService;

    @Inject
    private FlowService flowService;

    @Inject
    private BillMediaAttachService billMediaAttachService;

    @Inject
    ProductMediaAttachService productMediaAttachService;

    @Inject
    MediaTemplateService mediaTemplateService;

    @Inject
    ProductService productService;

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private ProductProfitService productProfitService;

    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private LoanedResultService loanedResultService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private UserService userService;

    @Inject
    private LoanOrderService loanOrderService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    private LoanSubjectAccountService loanSubjectAccountService;

    @Inject
    private BillLoanService billLoanService;

    @Inject
    private IntermediaryApplyService intermediaryApplyService;

    @Inject
    private ApprovalResultService approvalResultService;


    /**
     * 新增
     * @param intermediaryApply
     * @return
     */
    public IntermediaryApply addIntermediaryApply(IntermediaryApply intermediaryApply) {
        if (null==intermediaryApply){
            return null;
        }
        return  dao().insert(intermediaryApply);
    }

    /**
     * 通过loanid查询
     * @param loanId
     * @return
     */
    public IntermediaryApply fetchByLoanId(String loanId){
        if(StringUtils.isNotEmpty(loanId)){
            IntermediaryApply intermediaryApply = this.dao().fetch(IntermediaryApply.class, Cnd.where("loanId", "=", loanId));
            return intermediaryApply;
        }
        return null;
    }


    /**
     * 通过id查询
     * @param id
     * @return
     */
    public IntermediaryApply fetchById(String id){
        if(StringUtils.isNotEmpty(id)){
            IntermediaryApply intermediaryApply = this.dao().fetch(IntermediaryApply.class, Cnd.where("id", "=", id));
            return intermediaryApply;
        }
        return null;
    }

    /**
     * 待申请、申请列表查询
     * @param param
     * @return
     */
    public DataTables intermediaryApplyList(DataTableParam param){

        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String submitDate;
        String loanTime;
        Date loanBeginDate;
        Date loanEndDate;
        Date submitBeginDate;
        Date submitEndDate;
        String code;
        String loanStatus = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            submitDate = keys.get("submitDate");
            loanTime = keys.get("loanTime");
            code =  keys.get("businessCode");
            if(StringUtils.isNotEmpty(keys.get("loanStatus"))) {
                loanStatus = keys.get("loanStatus");
            }
        } else {
            return null;
        }
        submitBeginDate = TimeUtils.getQueryStartDateTime(submitDate);
        submitEndDate = TimeUtils.getQueryEndDateTime(submitDate);
        loanBeginDate = TimeUtils.getQueryStartDateTime(loanTime);
        loanEndDate = TimeUtils.getQueryEndDateTime(loanTime);

        String sqlStr = "SELECT * " +
                " FROM sl_intermediary_apply sia "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sia.loanId=wt.loanId" +
                " where 1=1 ";
        String countSqlStr = "SELECT  COUNT( sia.id ) AS 'number' " +
                "  FROM " +
                "  sl_intermediary_apply sia " +
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sia.loanId=wt.loanId" +
                " where 1=1 " ;
        if (null != submitBeginDate && null != submitEndDate) {
            sqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
            countSqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
        }
        if (null != loanBeginDate && null != loanEndDate) {
            sqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
            countSqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
        }
        if (StringUtils.isNotEmpty(loanStatus)) {
            if(("SUBMIT").equals(loanStatus)){
                sqlStr += " AND  sia.loanStatus in ('SUBMIT','APPROVEEND','LOANED','APPROVEREJECT') ";
                countSqlStr += " AND  sia.loanStatus in ('SUBMIT','APPROVEEND','LOANED','APPROVEREJECT') ";
            }else {
                sqlStr += " AND  sia.loanStatus=@loanStatus ";
                countSqlStr += " AND  sia.loanStatus=@loanStatus ";
            }

        }
        if (StringUtils.isNotEmpty(code)) {
            sqlStr += " AND  sia.businessCode=@code ";
            countSqlStr += " AND  sia.businessCode=@code ";
        }
        sqlStr += " order by sia.submitDate desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("code", code);
        sql.setParam("loanBeginDate",loanBeginDate);
        sql.setParam("loanEndDate",loanEndDate);
        sql.setParam("submitBeginDate",submitBeginDate);
        sql.setParam("submitEndDate",submitEndDate);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(IntermediaryApply.class));
        dao().execute(sql);
        List<IntermediaryApply> list = sql.getList(IntermediaryApply.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("code", code);
        countSql.setParam("loanBeginDate",loanBeginDate);
        countSql.setParam("loanEndDate",loanEndDate);
        countSql.setParam("submitBeginDate",submitBeginDate);
        countSql.setParam("submitEndDate",submitEndDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);
    }

    /**
     * 查询待审批列表
     * @param param
     * @return
     */
    public Object queryApprovalList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String submitDate;
        String loanTime;
        Date loanBeginDate;
        Date loanEndDate;
        Date submitBeginDate;
        Date submitEndDate;
        String code;
        String type;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            submitDate = keys.get("submitDate");
            loanTime = keys.get("loanTime");
            code =  keys.get("businessCode");
            type =  keys.get("type");
        } else {
            return null;
        }
        submitBeginDate = TimeUtils.getQueryStartDateTime(submitDate);
        submitEndDate = TimeUtils.getQueryEndDateTime(submitDate);
        loanBeginDate = TimeUtils.getQueryStartDateTime(loanTime);
        loanEndDate = TimeUtils.getQueryEndDateTime(loanTime);

        User user = userService.fetchLinksById(ShiroSession.getLoginUser().getId());
        List<Role> roles = user.getRoles();

        if(null==roles||roles.isEmpty()){
            return new DataTables(param.getDraw(),0,0,null);
        }

        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }

        //根据角色Id获取到当前需要处理的节点
        Page<WorkItem> majorPage = new Page<WorkItem>(Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOperators(roleIds);
        queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());

        List<WorkItem> majorWorks =  flowService.getEngine().query().getWorkItems(majorPage,queryFilter);
        //获取到所有的loanId
        List<String> loanIds = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (WorkItem workItem:majorWorks){
            if(StringUtils.isEmpty(workItem.getTaskName())||workItem.getTaskKey().indexOf(type)<0){
                continue;
            }
            if (StringUtils.isNotEmpty(workItem.getOrderId())) {
                orderIds.add(workItem.getOrderId());
            }
        }
        if (orderIds.size()>0) {
            loanIds = loanOrderService.getLoanIds(orderIds);
        }
        //获取到需要当前用户处理的列表
        if(null==loanIds||loanIds.size()==0){
            return new  DataTables(param.getDraw(),0,0,new ArrayList<>());
        }

        StringBuffer ids = new StringBuffer();
        ids.append("(");
        for(int i=0;i<loanIds.size(); i++){
            ids.append("'"+loanIds.get(i)+"'");
            if(i!=loanIds.size()-1){
                ids.append(",");
            }
        }
        ids.append(")");
        String sqlStr = "SELECT * "+
                " FROM sl_intermediary_apply sia "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sia.loanId=wt.loanId" +
                " WHERE 1=1"+
                " AND sia.loanStatus = 'SUBMIT' "+
                "  AND sia.loanId IN "+ids.toString();

        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT sia.id) AS 'number' "+
                " FROM sl_intermediary_apply sia "+
                " WHERE 1=1"+
                " AND sia.loanStatus = 'SUBMIT' "+
                " AND sia.loanId IN "+ids.toString();

        if (null != submitBeginDate && null != submitEndDate) {
            sqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
            countSqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
        }
        if (null != loanBeginDate && null != loanEndDate) {
            sqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
            countSqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
        }
        if (StringUtils.isNotEmpty(code)) {
            sqlStr += " AND  sia.businessCode=@code ";
            countSqlStr += " AND  sia.businessCode=@code ";
        }

        sqlStr+=" order by sia.submitDate desc ";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("loanIds",ids.toString());
        sql.setParam("code", code);
        sql.setParam("loanBeginDate",loanBeginDate);
        sql.setParam("loanEndDate",loanEndDate);
        sql.setParam("submitBeginDate",submitBeginDate);
        sql.setParam("submitEndDate",submitEndDate);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(IntermediaryApply.class));
        dao().execute(sql);
        List<IntermediaryApply> list = sql.getList(IntermediaryApply.class);
        countSql.setParam("loanIds",ids.toString());
        countSql.setParam("code", code);
        countSql.setParam("loanBeginDate",loanBeginDate);
        countSql.setParam("loanEndDate",loanEndDate);
        countSql.setParam("submitBeginDate",submitBeginDate);
        countSql.setParam("submitEndDate",submitEndDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        return new DataTables(param.getDraw(),count,count,list);
    }

    /**
     * 查询已完成审批列表
     * @param param
     * @return
     */
    public Object queryApprovalCompleteList(DataTableParam param) {
        String userId = ShiroSession.getLoginUser().getId();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String submitDate;
        String loanTime;
        Date loanBeginDate;
        Date loanEndDate;
        Date submitBeginDate;
        Date submitEndDate;
        String code = null;
        String loanStatus = null;
        String type;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            submitDate = keys.get("submitDate");
            loanTime = keys.get("loanTime");
            code =  keys.get("businessCode");
            type =  keys.get("type");
            if(StringUtils.isNotEmpty(keys.get("loanStatus"))) {
                loanStatus = keys.get("loanStatus");
            }
        } else {
            return null;
        }
        submitBeginDate = TimeUtils.getQueryStartDateTime(submitDate);
        submitEndDate = TimeUtils.getQueryEndDateTime(submitDate);
        loanBeginDate = TimeUtils.getQueryStartDateTime(loanTime);
        loanEndDate = TimeUtils.getQueryEndDateTime(loanTime);
        String sqlStr = "SELECT * "+
                " FROM sl_intermediary_apply sia "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sia.loanId=wt.loanId" +
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = sia.loanId AND flowConfigureType ='BROKERAGE_FEE' AND r.approvalType =@type)";

        String countSqlStr = "SELECT "+
                " count(sia.id) AS 'number' "+
                " FROM sl_intermediary_apply sia " +
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = sia.loanId AND r.flowConfigureType ='BROKERAGE_FEE' AND r.approvalType =@type)";

        if (null != submitBeginDate && null != submitEndDate) {
            sqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
            countSqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
        }
        if (null != loanBeginDate && null != loanEndDate) {
            sqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
            countSqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
        }
        if (StringUtils.isNotEmpty(code)) {
            sqlStr += " AND  sia.businessCode=@code ";
            countSqlStr += " AND  sia.businessCode=@code ";
        }

        sqlStr += " order by sia.submitDate desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("type",type);
        sql.setParam("userId",userId);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("code", code);
        sql.setParam("loanBeginDate",loanBeginDate);
        sql.setParam("loanEndDate",loanEndDate);
        sql.setParam("submitBeginDate",submitBeginDate);
        sql.setParam("submitEndDate",submitEndDate);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(IntermediaryApply.class));
        dao().execute(sql);
        List<IntermediaryApply> list = sql.getList(IntermediaryApply.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("type",type);
        countSql.setParam("userId",userId);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("code", code);
        countSql.setParam("loanBeginDate",loanBeginDate);
        countSql.setParam("loanEndDate",loanEndDate);
        countSql.setParam("submitBeginDate",submitBeginDate);
        countSql.setParam("submitEndDate",submitEndDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        return new DataTables(param.getDraw(),count,count,list);
    }


    /**
     * 查询待放款、已放款列表
     * @param param
     * @return
     */
    public Object queryLoanList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String submitDate;
        String loanTime;
        Date loanBeginDate;
        Date loanEndDate;
        Date submitBeginDate;
        Date submitEndDate;
        String code = null;
        String loanStatus = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            submitDate = keys.get("submitDate");
            loanTime = keys.get("loanTime");
            code =  keys.get("businessCode");
            if(StringUtils.isNotEmpty(keys.get("loanStatus"))) {
                loanStatus = keys.get("loanStatus");
            }
        } else {
            return null;
        }
        submitBeginDate = TimeUtils.getQueryStartDateTime(submitDate);
        submitEndDate = TimeUtils.getQueryEndDateTime(submitDate);
        loanBeginDate = TimeUtils.getQueryStartDateTime(loanTime);
        loanEndDate = TimeUtils.getQueryEndDateTime(loanTime);
        String sqlStr = "SELECT * "+
                " FROM sl_intermediary_apply sia "+
                " WHERE 1=1 ";

        String countSqlStr = "SELECT "+
                " count(sia.id) AS 'number' "+
                " FROM sl_intermediary_apply sia " +
                " WHERE 1=1 ";

        if (null != submitBeginDate && null != submitEndDate) {
            sqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
            countSqlStr += " AND sia.submitDate>=@submitBeginDate  AND sia.submitDate<=@submitEndDate ";
        }
        if (null != loanBeginDate && null != loanEndDate) {
            sqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
            countSqlStr += " AND sia.loanTime>=@loanBeginDate  AND sia.loanTime<=@loanEndDate ";
        }
        if (StringUtils.isNotEmpty(code)) {
            sqlStr += " AND  sia.businessCode=@code ";
            countSqlStr += " AND  sia.businessCode=@code ";
        }

        if (StringUtils.isNotEmpty(loanStatus)) {
            sqlStr += " AND  sia.loanStatus=@loanStatus ";
            countSqlStr += " AND  sia.loanStatus=@loanStatus ";
        }
        sqlStr += " order by sia.submitDate desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("code", code);
        sql.setParam("loanBeginDate",loanBeginDate);
        sql.setParam("loanEndDate",loanEndDate);
        sql.setParam("submitBeginDate",submitBeginDate);
        sql.setParam("submitEndDate",submitEndDate);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(IntermediaryApply.class));
        dao().execute(sql);
        List<IntermediaryApply> list = sql.getList(IntermediaryApply.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("code", code);
        countSql.setParam("loanBeginDate",loanBeginDate);
        countSql.setParam("loanEndDate",loanEndDate);
        countSql.setParam("submitBeginDate",submitBeginDate);
        countSql.setParam("submitEndDate",submitEndDate);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        return new DataTables(param.getDraw(),count,count,list);
    }



    public boolean update(IntermediaryApply intermediaryApply) {
        if(null == intermediaryApply){
            return false;
        }
        //忽略少数字段更新
        return Daos.ext(dao(), FieldFilter.locked(IntermediaryApply.class, "^id|applyCode|createBy|createTime$")).update(intermediaryApply)>0;
    }

    @Aop(TransAop.READ_COMMITTED)
    public NutMap loan(String loanId,User user) {
        NutMap result = new NutMap();
        IntermediaryApply intermediaryApply = fetchByLoanId(loanId);
        Date now = new Date();
        intermediaryApply.setUpdateBy(user.getLogin());
        intermediaryApply.setUpdateTime(now);
        intermediaryApply.setLoanTime(now);
        intermediaryApply.setLoanStatus(LoanStatus.LOANED);

        List<LoanRecord> loanRecordList = new ArrayList<>();
        Loan loan = loanService.fetchById(loanId);
        LoanSubject loanSubject = loanSubjectService.fetch(loan.getLoanSubjectId());
        LoanSubjectAccount account = loanSubjectAccountService.queryFormatAccountsById(loan.getLoanSubjectAccountId());
        BillLoan billLoan = this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId));
        Intermediary intermediary = billLoanService.getIntermediary(billLoan);
        //LoanBorrower loanBorrower = this.dao().fetch(LoanBorrower.class, loan.getMasterBorrowerId());
        //Borrower borrower = borrowerService.fetchById1(loanBorrower.getBorrowerId());
        String code = loan.getCode()+"002";
        LoanRecord loanRecord = new LoanRecord();
        loanRecord.setCreateBy(user.getName());
        loanRecord.setCreateTime(now);
        loanRecord.setUpdateBy(user.getName());
        loanRecord.setUpdateTime(now);
        loanRecord.setLoanCode(code);
        loanRecord.setAmountType(AmountType.INTERMEDIAR_FEE);
        loanRecord.setLoanAmount(billLoan.getIntermediaryTotalFee());
        loanRecord.setLoanId(loanId);
        loanRecord.setLoanDate(now);
        loanRecord.setLoanStatus(LoanStatus.LOANED);
        loanRecord.setPayAcount(account.getAlias());
        loanRecord.setLoanSubject(loanSubject.getName());
        loanRecord.setPayee(intermediary.getName());
        loanRecord.setPayeeAcount(intermediary.getAccount());
        loanRecord.setPosition(0);
        loanRecordList.add(loanRecord);
        this.dao().insert(loanRecordList);
        Date lastUpdateTime = intermediaryApply.getUpdateTime();
        loanedResultService.addIntermediaryApplyLoanRecode(intermediaryApply,lastUpdateTime);

        billLoan.setPhone(intermediaryApply.getPhone());
        billLoan.setAccount(intermediaryApply.getAccount());
        billLoan.setBank(intermediaryApply.getBank());
        billLoan.setAddress(intermediaryApply.getAddress());
        billLoanService.updateBillLoanForIntermediary(billLoan);

        boolean flag = update(intermediaryApply);
        if (flag) {
            result.put("ok", true);
            result.put("msg", "放款成功");
            return result;
        } else {
            result.put("ok", false);
            result.put("msg", "放款失败");
            return result;
        }

    }

    public void documentDownload(String loanId ,HttpServletResponse response ){
        Map<String,Object> map = new HashMap<String,Object>();
        Loan loan = loanService.fetch(loanId);
        Product product = productService.fetch(loan.getProductId());
        BillLoan billLoan = billLoanService.fetchBillLoanByLoanId(loanId);
        Intermediary intermediary = billLoanService.getIntermediary(billLoan);
        IntermediaryApply intermediaryApply = intermediaryApplyService.fetchByLoanId(loanId);
        List<ApprovalResult>  approvalResults =  approvalResultService.query(loanId,null, FlowConfigureType.BROKERAGE_FEE);

        map.put("loan",loan);
        map.put("product",product);
        map.put("intermediaryApply",intermediaryApply);
        map.put("intermediary",intermediary);
        map.put("approvalResults",approvalResults);
        map.put("billLoanRepayList", billLoanService.queryRepayOrderByPosition(loanId));
        map.put("billLoan",billLoan);
        try{
            PdfUtil.generalTableTypePdf(response,map,"居间费付费审批单"+"|"+intermediaryApply.getApplyCode(),ProductTempType.BROKERAGEFEE,true);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
