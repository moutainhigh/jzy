package com.kaisa.kams.components.service;


import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.enums.ProductMediaItemType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.ProductMediaItem;
import com.kaisa.kams.models.ProductMediaTmpl;
import com.kaisa.kams.models.User;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutType;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 影像配置模板
 * Created by luoyj on 2016/11/30.
 */
@IocBean(fields = "dao")
public class MediaTemplateService extends IdNameEntityService<ProductMediaTmpl> {
    private static final Log log = Logs.get();

    /**
     * 通过参数查询影像资料配置列表
     *
     * @param param
     * @return
     */
    public DataTables queryByParam(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

        String code = "";
        String name = "";
        String status = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            code = keys.get("code");
            name = keys.get("name");
            status = keys.get("status");
        }
        PublicStatus p = null;
        DataTables dataTables = null;
        Condition cnd=null;
        if (status.equals("true")) {
            p = PublicStatus.ABLE;
        } else if (status.equals("false")){
            p = PublicStatus.DISABLED;
        }
        try {
            if(p!=null){
                cnd= Cnd.where("name", "like", "%" + name + "%").and("code", "like", "%" + code + "%").and("status", "=", p).asc("createTime");
            }else {
                cnd= Cnd.where("name", "like", "%" + name + "%").and("code", "like", "%" + code + "%").asc("createTime").asc("createTime");
            }
            //Pager pager = DataTablesUtil.getDataTableToPager(start, length);
            List<ProductMediaTmpl> mediaTmplList = dao().query(ProductMediaTmpl.class, cnd, pager);
                dataTables = new DataTables(param.getDraw(), dao().count(ProductMediaTmpl.class), dao().count(ProductMediaTmpl.class, cnd), mediaTmplList);
                dataTables.setOk(true);
                dataTables.setMsg("成功");
        } catch (Exception e) {
            log.error(e.getMessage());
            dataTables = new DataTables(0, 0, 0, null);
            dataTables.setOk(false);
            dataTables.setMsg("获取数据失败.");
        }
        return dataTables;
    }

    /**
     * 校验影像模板名称是否可用
     *
     * @return
     */
    public boolean enableName(String name, String event,String id) {
        // 判断是影像资料配置名称是否重复
        ProductMediaTmpl productMediaTmpl = dao().fetch(ProductMediaTmpl.class, Cnd.where("name", "=", name));
       if(productMediaTmpl!=null){
           if (event.equals("add")) {
               if (productMediaTmpl != null) {
                   return false;
               }
           }
           if (event.equals("update")) {
               if (!productMediaTmpl.getId().equals(id)) {
                   return false;
               }
           }
       }
        return true;
    }

    /**
     * 新增影像资料配置模板
     *
     * @param p
     * @param jsonString
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public boolean add(ProductMediaTmpl p, String jsonString) {
        // 获取修改的记录的人员
        User user = ShiroSession.getLoginUser();
        if (user != null) {
            p.setCreateBy(String.valueOf(user.getName()));
        }
        p.setCreateTime(new Date());
        p.setCode(getCode());
        if (jsonString != null || !Strings.isBlank(jsonString)) {
            ProductMediaTmpl pr = dao().insert(p);
            if (pr != null) {
                // 还需要添加产品影像配置表
                getSaveMediaItem(jsonString, pr.getId(), user.getCreateBy());
                return true;
            }
        }
        return false;
    }

    /**
     * 获取影像模板code
     *
     * @return
     */
    public String getCode() {
        String code = "YX";
        List<ProductMediaTmpl> list = dao().query(ProductMediaTmpl.class, Cnd.orderBy().desc("createTime"));
        if (list.size() > 0) {
            int num=Integer.valueOf(list.get(0).getCode().substring(2,list.get(0).getCode().length()));
            code += num+1;
        } else {
            code += 100000 + 1;
        }
        return code;
    }

    /**
     * 通过id 或者影像配置模板信息
     *
     * @param id
     * @return
     */
    public ProductMediaTmpl fetchById(String id) {
        ProductMediaTmpl productMediaTmpl = dao().fetch(ProductMediaTmpl.class, id);
        return productMediaTmpl;
    }


    /**
     * 获取影像资料接口
     *
     * @param start
     * @param length
     * @param draw
     * @param id
     * @param type
     * @return
     */
    public DataTables queryMediaItemBytmId(int start, int length, int draw, String id, String type) {
        DataTables dataTables = null;

        try {
            Cnd cnd = Cnd.where("tmplId", "=", id).and("mediaItemType", "=", type);
            //  Pager pager = DataTablesUtil.getDataTableToPager(start, length);
            List<ProductMediaItem> listMediaItem = dao().query(ProductMediaItem.class, cnd.asc("createTime"));
            dataTables = new DataTables(draw, dao().count(ProductMediaItem.class), dao().count(ProductMediaItem.class, cnd), listMediaItem);
            dataTables.setOk(true);
            dataTables.setMsg("成功");
        } catch (Exception e) {
            log.error(e.getMessage());
            dataTables = new DataTables(0, 0, 0, null);
            dataTables.setOk(false);
            dataTables.setMsg("获取数据失败.");
        }
        return dataTables;
    }

    /**
     * 修改影像资料配置模板
     *
     * @param p
     * @param jsonString
     * @return
     */
    @Aop(TransAop.READ_COMMITTED)
    public boolean update(ProductMediaTmpl p, String jsonString) {
        // 获取修改的记录的人员
        User user = ShiroSession.getLoginUser();
        if (user != null) {
            p.setUpdateBy(String.valueOf(user.getName()));
        }
        p.setUpdateTime(new Date());
        if (jsonString != null || !Strings.isBlank(jsonString)) {
            int num = dao().update(p, "^(name|updateTime|updateBy|description|status)$");
            if (num == 1) {
                //删除关联的产品影像配置表信息
                List<ProductMediaItem> listMediaItem = dao().query(ProductMediaItem.class, Cnd.where("tmplId", "=", p.getId()));
                for (ProductMediaItem item : listMediaItem) {
                    dao().delete(ProductMediaItem.class, item.getId());
                }
                //新增关联的产品影像配置表信息
                getSaveMediaItem(jsonString, p.getId(), user.getCreateBy());
            }
            return true;
        }
        return false;
    }

    private void getSaveMediaItem(String jsonString, String tmplId, String creatBy) {
        List<Map<String, Object>> list = null;
        if (!Strings.isBlank(jsonString)) {
            list = (List<Map<String, Object>>) Json.fromJson(NutType.list(NutType.mapStr(Object.class)), jsonString);
            for (int i=0;i<list.size();i++) {
                Long time=new Date().getTime()+(i*1000);
                Date date=DateUtil.getTimeToDate(time);
                ProductMediaItem p = new ProductMediaItem();
                Map<String,Object> map=list.get(i);
                    p.setTmplId(tmplId);
                    p.setName(String.valueOf(map.get("name")));
                    p.setMediaItemType(ProductMediaItemType.getEnum(String.valueOf(map.get("mediaItemType"))));
                    p.setCode(String.valueOf(map.get("code")));
                    p.setCreateBy(creatBy);
                    p.setCreateTime(date);
                    if (String.valueOf(map.get("required")).equals("true")) {
                        p.setRequired(true);
                    } else {
                        p.setRequired(false);
                    }
                    dao().insert(p);
            }
        }
    }

    /**
     * 根据templ和mediaItemTypeCode查询
     *
     * @param tmplId
     * @param mediaItemTypeCode
     * @return
     */
    public List<ProductMediaItem> queryByTmpAndType(String tmplId, ProductMediaItemType mediaItemTypeCode) {
        return dao().query(ProductMediaItem.class, Cnd.where("tmplId", "=", tmplId).and("mediaItemTypeCode", "=", mediaItemTypeCode));
    }

    /**
     * 根据tempId查询
     *
     * @param tmplId
     * @return
     */
    public List<ProductMediaItem> queryByTmp(String tmplId) {
        return dao().query(ProductMediaItem.class, Cnd.where("tmplId", "=", tmplId));
    }

    /**
     * 查询所有可用的模板
     *
     * @param status
     * @return
     */
    public List<ProductMediaTmpl> queryAll(PublicStatus status) {
        return dao().query(ProductMediaTmpl.class, Cnd.where("status", "=", status));
    }
}