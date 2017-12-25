package com.kaisa.kams.components.service.push;


import com.kaisa.kams.components.params.base.DataTableBaseParam;
import com.kaisa.kams.components.params.push.LoanPushApproval;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.ApprovalResultService;
import com.kaisa.kams.components.service.LoanOrderService;
import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.view.push.LoanPushOrderView;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.enums.push.LoanPushOrderStatus;
import com.kaisa.kams.enums.push.LoanPushStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.flow.ApprovalResult;
import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.models.push.LoanPush;
import com.kaisa.kams.models.push.LoanPushOrder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.model.NodeModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推单订单服务类
 *
 * @author pengyueyang created on 2017/11/8.
 */
@IocBean(fields = "dao")
public class LoanPushOrderApprovalService extends Service {

    private final Logger log = LoggerFactory.getLogger(LoanPushOrderApprovalService.class);
    private final FlowConfigureType FLOW_CONFIGURE_TYPE = FlowConfigureType.LOAN_PUSH;

    @Inject
    private FlowService flowService;
    @Inject
    private FlowConfigureService flowConfigureService;
    @Inject
    private LoanOrderService loanOrderService;
    @Inject
    private ApprovalResultService approvalResultService;


    public boolean startFlow(LoanPushOrder loanPushOrder) {
        String productId = getProductIdByPushId(loanPushOrder.getPushId());
        return flowService.startInstanceLoanPush(productId, loanPushOrder, FLOW_CONFIGURE_TYPE);
    }

    public boolean isConfigFlow(LoanPushOrder loanPushOrder) {
        if (null == loanPushOrder) {
            return false;
        }
        String productId = getProductIdByPushId(loanPushOrder.getPushId());
        if (StringUtils.isEmpty(productId)) {
            return false;
        }
        FlowConfigure flowConfigure = flowConfigureService.getFlowConfigureByFlowypeAndProductIdMaybeNull(FLOW_CONFIGURE_TYPE.name(), productId);
        if (null == flowConfigure || PublicStatus.DISABLED.equals(flowConfigure.getStatus())) {
            return false;
        }
        return flowService.existSnakerFile(productId, FLOW_CONFIGURE_TYPE);
    }

    public DataTables getUserApprovalList(DataTableBaseParam param) {
        User user = ShiroSession.getLoginUser();
        List<String> roleIds = getRoleList(user.getId());
        //获取到需要当前用户处理的列表
        if (CollectionUtils.isEmpty(roleIds)) {
            return new DataTables(param.getDraw(), 0, 0, null);
        }
        List<LoanPushOrderView> list = queryApprovalList(param, roleIds);
        int count = queryApprovalCount(roleIds);
        return new DataTables(param.getDraw(), count, count, list);
    }

    private int queryApprovalCount(List<String> roleIds) {
        Sql sql = Sqls.fetchInt("select count(t.id) from wf_task t\n" +
                "left join sl_loan_order o on t.order_Id=o.orderId\n" +
                "left join wf_task_actor ta on ta.task_id=t.id\n" +
                "left join sl_loan_push_order lpo on lpo.id=o.loanId\n" +
                "where lpo.status='APPROVAL' and ta.actor_Id in(@ids)");
        sql.setParam("ids", roleIds.toArray());
        dao().execute(sql);
        return sql.getInt();
    }

    private List<LoanPushOrderView> queryApprovalList(DataTableBaseParam param, List<String> roleIds) {
        Sql sql = Sqls.queryEntity("select t.id taskId,lpo.* from wf_task t\n" +
                "left join sl_loan_order o on t.order_Id=o.orderId\n" +
                "left join wf_task_actor ta on ta.task_id=t.id\n" +
                "left join sl_loan_push_order lpo on lpo.id=o.loanId\n" +
                "where lpo.status='APPROVAL' and ta.actor_Id in(@ids) $condition");
        Entity<LoanPushOrderView> entity = dao().getEntity(LoanPushOrderView.class);
        sql.setEntity(entity);
        sql.setParam("ids", roleIds.toArray());
        sql.setPager(DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength()));
        Condition cnd = Cnd.orderBy().desc("code");
        sql.setCondition(cnd);
        dao().execute(sql);
        return convertList(sql.getList(LoanPushOrderView.class));
    }


    private List<String> getRoleList(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        Sql sql = Sqls.create("select role from sl_roleuser where user=@userId");
        sql.setCallback(Sqls.callback.strList());
        sql.setParam("userId", id);
        dao().execute(sql);
        return sql.getList(String.class);
    }

    public List<LoanPushOrderView> getUserApprovedList(DataTableBaseParam param) {
        String querySql = "select distinct lpo.id distinctId,lpo.*  from sl_loan_push_order lpo " +
                "left join sl_approval_result pr on lpo.id=pr.loanId " +
                "where pr.userId=@userId order by pr.approvalTime";
        Sql sql = Sqls.queryEntity(querySql);
        Entity<LoanPushOrderView> entity = dao().getEntity(LoanPushOrderView.class);
        sql.setEntity(entity);
        sql.setParam("userId", ShiroSession.getLoginUser().getId());
        sql.setPager(DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength()));
        dao().execute(sql);
        return convertList(sql.getList(LoanPushOrderView.class));
    }

    public int getUserApprovedCount() {
        String countSql = "select count(distinct(lpo.id)) from sl_loan_push_order lpo " +
                "left join sl_approval_result pr on lpo.id=pr.loanId " +
                "where pr.userId=@userId";
        Sql sql = Sqls.fetchInt(countSql);
        sql.setParam("userId", ShiroSession.getLoginUser().getId());
        dao().execute(sql);
        return sql.getInt();
    }

    public Task getApprovalInfo(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            return null;
        }
        Task task = flowService.getEngine().query().getTask(taskId);
        if (null == task) {
            return task;
        }
        String loginName = ShiroSession.getLoginUser().getLogin();
        boolean isContainTaskActor = flowService.isContainTaskActor(taskId,loginName);
        if (!isContainTaskActor) {
            flowService.getEngine().task().addTaskActor(taskId, loginName);
        }
        return task;
    }

    public boolean nodeApproval(LoanPushApproval loanPushApproval) {
        Task task = flowService.getEngine().query().getTask(loanPushApproval.getTaskId());
        if (null == task) {
            return false;
        }
        boolean processResult;
        switch (loanPushApproval.getApprovalCode()) {
            case AGREE:
                processResult = processAgree(loanPushApproval);
                break;
            case BACKPRE:
                processResult = processBackPre(loanPushApproval, task);
                break;
            case BACKBEGIN:
                processResult = processBackBegin(loanPushApproval);
                break;
            case DISAGREE:
                processResult = processDisagree(loanPushApproval);
                break;
            default:
                return false;
        }
        if (processResult) {
            saveApprovalResult(task, loanPushApproval);
        }
        return processResult;
    }

    private boolean processDisagree(LoanPushApproval loanPushApproval) {
        boolean result = loanOrderService.deleteByLoanId(loanPushApproval.getLoanPushOrderId(), FLOW_CONFIGURE_TYPE);
        if (!result) {
            return result;
        }
        return result && updateLoanPushOrderStatus(loanPushApproval.getLoanPushOrderId(), LoanPushOrderStatus.REJECTED);
    }


    private boolean processBackBegin(LoanPushApproval loanPushApproval) {
        User user = ShiroSession.getLoginUser();
        ApprovalResult approvalResultFirst = approvalResultService.fetchFirst(loanPushApproval.getLoanPushOrderId(), FLOW_CONFIGURE_TYPE);
        if (null != approvalResultFirst && loanPushApproval.isNeedRepeatFlow()) {
            try {
                flowService.getEngine().executeAndJumpTask(loanPushApproval.getTaskId(), user.getLogin(), getQueryParam(loanPushApproval), approvalResultFirst.getNodeCode());
            } catch (Exception e) {
                log.error("Execute back begin task error:{}", e.getMessage());
                return false;
            }
        }
        return updateLoanPushOrderStatus(loanPushApproval.getLoanPushOrderId(), LoanPushOrderStatus.EDIT);
    }

    private boolean processBackPre(LoanPushApproval loanPushApproval, Task task) {
        ApprovalResult approvalResultFirst = approvalResultService.fetchFirst(loanPushApproval.getLoanPushOrderId(), FLOW_CONFIGURE_TYPE);
        if (null == approvalResultFirst) {
            return updateLoanPushOrderStatus(loanPushApproval.getLoanPushOrderId(), LoanPushOrderStatus.EDIT);
        }
        if (task.getTaskName().equals(approvalResultFirst.getNodeName())) {
            return updateLoanPushOrderStatus(loanPushApproval.getLoanPushOrderId(), LoanPushOrderStatus.EDIT);
        }
        Order order = flowService.getEngine().query().getOrder(task.getOrderId());
        Process process = flowService.getEngine().process().getProcessById(order.getProcessId());
        List<NodeModel> nodePass = process.getModel().getNodes();
        List<NodeModel> nodes = new ArrayList<>();
        //去掉所有的decision节点 判断当前节点金额是否需要审批
        nodes.addAll(nodePass.stream().filter(node -> !node.getDisplayName().equals("分支选择")).collect(Collectors.toList()));

        String preNodeName = null;
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (int i = 0; i < nodes.size(); i++) {
                NodeModel node = nodes.get(i);
                if (null != node && node.getName().equals(task.getTaskName()) && i > 0) {
                    preNodeName = nodes.get(i - 1).getName();
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(preNodeName)) {
            return updateLoanPushOrderStatus(loanPushApproval.getLoanPushOrderId(), LoanPushOrderStatus.EDIT);
        }
        User user = ShiroSession.getLoginUser();
        try {
            flowService.getEngine().executeAndJumpTask(loanPushApproval.getTaskId(), user.getLogin(), getQueryParam(loanPushApproval), preNodeName);
        } catch (Exception e) {
            log.error("Execute back pre task error：{}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean processAgree(LoanPushApproval loanPushApproval) {
        User user = ShiroSession.getLoginUser();
        try {
            List<Task> tasks = flowService.getEngine().executeTask(loanPushApproval.getTaskId(), user.getLogin(), getQueryParam(loanPushApproval));
            if (CollectionUtils.isEmpty(tasks)) {
                // 调用推单接口并修改状态
                //TODO ApiRequestUtil.loanPush();
                updateLoanPushStatus(loanPushApproval.getLoanPushId());
                return updateLoanPushOrderStatus(loanPushApproval.getLoanPushOrderId(), LoanPushOrderStatus.PUSHED);
            }
        } catch (Exception e) {
            log.error("Execute agree task error：{}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean updateLoanPushStatus(String pushId) {
        if (StringUtils.isEmpty(pushId)) {
            return false;
        }
        LoanPush loanPush = dao().fetch(LoanPush.class,pushId);
        if (null == loanPush) {
            return false;
        }
        if (LoanPushStatus.STAY_PUSH.equals(loanPush.getStatus())) {
            return dao().update(LoanPush.class, Chain.make("status", LoanPushStatus.PART_PUSHED), Cnd.where("id", "=", pushId)) == 1;
        }
        return true;
    }

    public boolean updateLoanPushOrderStatus(String loanPushOrderId, LoanPushOrderStatus status) {
        return dao().update(LoanPushOrder.class, Chain.make("status", status), Cnd.where("id", "=", loanPushOrderId)) == 1;
    }

    private HashMap getQueryParam(LoanPushApproval loanPushApproval) {
        HashMap param = new HashMap<>();
        param.put("approvalType", loanPushApproval.getApprovalType());
        return param;
    }

    private String getProductIdByPushId(String pushId) {
        if (StringUtils.isEmpty(pushId)) {
            return null;
        }
        String querySql = "select productId from sl_loan_push where id=@pushId";
        Sql sql = Sqls.queryString(querySql);
        sql.setParam("pushId", pushId);
        dao().execute(sql);
        return sql.getString();
    }


    private void saveApprovalResult(Task task, LoanPushApproval loanPushApproval) {
        Date now = new Date();
        User user = ShiroSession.getLoginUser();
        ApprovalResult approvalResult = new ApprovalResult();
        approvalResult.setLoanId(loanPushApproval.getLoanPushOrderId());
        approvalResult.setFlowConfigureType(FLOW_CONFIGURE_TYPE);
        approvalResult.setApprovalCode(loanPushApproval.getApprovalCode());
        approvalResult.setApprovalTime(now);
        approvalResult.setApprovalType(loanPushApproval.getApprovalType());
        approvalResult.setContent(loanPushApproval.getContent());
        approvalResult.setNodeCode(task.getTaskName());
        approvalResult.setOrderId(loanPushApproval.getOrderId());
        approvalResult.setUserId(user.getId());
        approvalResult.setUserName(user.getName());
        approvalResult.setCreateBy(user.getName());
        approvalResult.setCreateTime(now);
        approvalResult.setUpdateBy(user.getName());
        approvalResult.setUpdateTime(now);
        approvalResult.setNodeName(task.getDisplayName());
        ApprovalResult last;
        if ("start".equals(task.getParentTaskId()) && (last = approvalResultService.fetchLast(loanPushApproval.getLoanPushOrderId(), FLOW_CONFIGURE_TYPE)) != null) {
            approvalResult.setStartTime(last.getApprovalTime());
        } else {
            approvalResult.setStartTime(DateUtil.getStringToTime(task.getCreateTime()));
        }
        approvalResult.setDuration(DateUtil.minutesBetweenTowDate(approvalResult.getStartTime(), approvalResult.getCreateTime()));
        dao().insert(approvalResult);
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
    }




}
