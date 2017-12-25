package com.kaisa.kams.components.service;

import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.OaParam;
import com.kaisa.kams.enums.ApprovalType;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.models.AssociatedAccount;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/8/15.
 */
@IocBean(fields = "dao")
public class OaService extends IdNameEntityService<AssociatedAccount> {
    @Inject
    UserService userService;
    @Inject
    OaService oaService;
    @Inject
    ProductService productService ;
    @Inject
    LoanService loanService ;
    @Inject
    FlowService flowService;
    @Inject
    LoanOrderService loanOrderService;
    @Inject
    ChannelService channelService;


    /**
     * 通过oaUserAccount检查是否存在
     *
     * @param oaUserAccount
     * @return
     */
    public boolean check(String oaUserAccount){
        return dao().fetch(AssociatedAccount.class, Cnd.where("oaUserAccount","=",oaUserAccount))!=null?Boolean.TRUE:Boolean.FALSE;
    }
    /**
     * 通过oaUserAccount查询用户信息
     *
     * @param oaUserAccount
     * @return
     */
    public User getUserByOaUserAccount(String oaUserAccount){
        AssociatedAccount associatedAccount =  dao().fetch(AssociatedAccount.class, Cnd.where("oaUserAccount","=",oaUserAccount));
        return associatedAccount!=null?userService.fetchById(associatedAccount.getKamsUserId()):null;
    }

    /**
     * 通过oaUserAccount查询角色信息
     *
     * @param oaUserAccount
     * @return
     */
    public User getUserRolesByOaUserAccount(String oaUserAccount){
        AssociatedAccount associatedAccount =  dao().fetch(AssociatedAccount.class, Cnd.where("oaUserAccount","=",oaUserAccount));
        return associatedAccount!=null?userService.fetchLinksById(associatedAccount.getKamsUserId()):null;
    }

    public DataTables queryApprovalList(OaParam param){
        User user  = oaService.getUserRolesByOaUserAccount(param.getOaUserAccount());
        List<Role> roles = user.getRoles();
        if(CollectionUtils.isEmpty(roles)){
            return new DataTables(0,0,0,null);
        }
        List<String> loanIds = getLoanIdList(roles);
        List<Map> list = loanService.queryApprovalList(param.getStart(),param.getLength(),loanIds);
        for(Map map : list) {
            String termType = (String)map.get("termType");
            map.put("termType",convertTermType(termType));
        }
        int count = loanService.queryApprovalCount(loanIds);
        return new DataTables(0,count,count,list);
    }

    private List<String> getLoanIdList(List<Role> roles) {
        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId();
        }
        //根据角色Id获取到当前需要处理的节点
        Page<WorkItem> majorPage = new Page<>(Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOperators(roleIds);
        queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());

        List<WorkItem> majorWorks =  flowService.getEngine().query().getWorkItems(majorPage,queryFilter);
        //获取到所有的loanId
        List<String> loanIds = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (WorkItem workItem:majorWorks){
             if(StringUtils.isEmpty(workItem.getTaskName())|| !ApprovalType.contained(workItem.getTaskKey())){
                continue;
            }
            if (StringUtils.isNotEmpty(workItem.getOrderId())) {
                orderIds.add(workItem.getOrderId());
            }
        }
        if (orderIds.size()>0) {
            loanIds = loanOrderService.getLoanIds(orderIds);
        }
        return loanIds;
    }

    public int queryApprovalCount(OaParam param) {
        User user  = oaService.getUserRolesByOaUserAccount(param.getOaUserAccount());
        List<Role> roles = user.getRoles();
        if(CollectionUtils.isEmpty(roles)){
            return 0;
        }
        List<String> loanIds = getLoanIdList(roles);
        return loanService.queryApprovalCount(loanIds);
    }

    public String convertTermType(String termType) {
        LoanTermType type;
        try {
             type = LoanTermType.valueOf(termType);
        } catch (IllegalArgumentException e) {
            return "";
        }
        switch (type) {
            case DAYS:
                return "天";
            case MOTHS:
                return "个月";
            case SEASONS:
                return "季";
            case YEAS:
                return "年";
            case FIXED_DATE:
                return "";
            default:
                return "";
        }
    }

    public String convertTermTypePer(String termType) {
        LoanTermType type;
        try {
            type = LoanTermType.valueOf(termType);
        } catch (IllegalArgumentException e) {
            return "";
        }
        switch (type) {
            case DAYS:
            case FIXED_DATE:
                return "/天";
            case MOTHS:
                return "/月";
            case SEASONS:
                return "/季";
            case YEAS:
                return "/年";
            default:
                return "";
        }
    }


}
