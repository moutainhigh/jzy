package com.kaisa.kams.components.service.flow;

import com.kaisa.kams.components.job.DelayTaskQueueDaemonThread;
import com.kaisa.kams.components.utils.ApiPropsUtils;
import com.kaisa.kams.components.utils.ApiRequestUtil;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.data.ApproveWarnMessageData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.service.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by wangqx on 2017/8/14.
 */
@IocBean(fields="dao")
public class ApproveWarnMessageService extends Service {

    private final static String  MESSAGE_TEMP_ID = ApiPropsUtils.getValueByKey("approve_warn_tempId");
    private final static String MINUTE = "分钟";
    private final static String FILTER_ROLE_ID_KEY = "filter_role_id_key";
    @Inject
    JedisPool jedisPool;

    public void processTask(ApproveWarnMessageData data) {
        if (null == data || StringUtils.isEmpty(data.getTaskId())) {
            return;
        }
        List<String> roleList = filterRole(getApproveRole(data.getTaskId()));
        if (CollectionUtils.isEmpty(roleList)) {
            DelayTaskQueueDaemonThread.getInstance().removeTask(data.getTaskId());
            return;
        }
        List<String> phoneList = getPhoneList(roleList);
        if (CollectionUtils.isEmpty(phoneList)) {
            DelayTaskQueueDaemonThread.getInstance().removeTask(data.getTaskId());
            return;
        }
        Map<String,String> msg = new HashMap<>();
        if(StringUtils.isNotEmpty(data.getMortgageCode())){
            msg.put("mortgageCode", data.getMortgageCode());
            msg.put("minutes", String.valueOf(ApproveWarnMessageUtils.getDelayMinutes()) + MINUTE);
        }else{
            msg.put("loanCode", data.getLoanCode());
            msg.put("productType", getProductTypeName(data.getProductTypeId()));
            msg.put("minutes", String.valueOf(ApproveWarnMessageUtils.getDelayMinutes()) + MINUTE);
        }

        //执行发送短信
        phoneList.forEach(phone-> ApiRequestUtil.sendSmsByAPI(phone,msg,MESSAGE_TEMP_ID));
        //移除任务
        DelayTaskQueueDaemonThread.getInstance().removeTask(data.getTaskId());
    }

    private List<String> getApproveRole(String taskId) {
        Sql sql = Sqls.create("select actor_Id from wf_task_actor where task_Id=@taskId");
        sql.setParam("taskId", taskId);
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    private List<String> getPhoneList(List<String> roleList) {
        Sql sql = Sqls.create("select mobile from sl_user where status='ABLE' and id in (" +
                "select user from sl_roleuser where role in (@roleList))");
        sql.setParam("roleList",roleList.toArray());
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    private String getProductTypeName(String id) {
        Sql sql = Sqls.create("select name from sl_product_type where id=@id");
        sql.setParam("id",id);
        sql.setCallback(Sqls.callback.str());
        dao().execute(sql);
        return sql.getString();
    }

    private List<String> filterRole(List<String> roleList){
        if (CollectionUtils.isEmpty(roleList)) {
            return roleList;
        }
        List<String> filterRoleList = new ArrayList<>(roleList.size());
        String filterRole = getFilterRole();
        filterRoleList.addAll(roleList.stream().filter(role -> !filterRole.equals(role)).collect(Collectors.toList()));
        return filterRoleList;
    }

    private String getFilterRole() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String roleId = jedis.get(FILTER_ROLE_ID_KEY);
            if (null == roleId) {
                Sql sql = Sqls.create("select id from sl_role where status='ABLE' and name='风控初审岗'");
                sql.setCallback(Sqls.callback.str());
                dao().execute(sql);
                roleId = sql.getString();
                if (StringUtils.isNotEmpty(roleId)) {
                    jedis.set(FILTER_ROLE_ID_KEY,roleId);
                }
            }
            return roleId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                Streams.safeClose(jedis);
            }
        }
        return "";
    }


}
