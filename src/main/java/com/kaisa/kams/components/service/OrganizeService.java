package com.kaisa.kams.components.service;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.JsonTreeData;
import com.kaisa.kams.components.utils.TreeNodeUtil;
import com.kaisa.kams.enums.OrganizeType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Organize;
import com.kaisa.kams.models.User;
import org.nutz.dao.Cnd;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.service.IdNameEntityService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * 组织岗位服务层
 * Created by luoyj on 2016/11/24.
 */
@IocBean(fields="dao")
public class OrganizeService extends IdNameEntityService<Organize>{

    @Inject
    private UserService userService;

    /**
     * 新增机构组织岗位信息
     * @param org
     * @return
     */
    public NutMap add(Organize  org){
        NutMap nutMap=new NutMap();
        //判断组织岗位名是否重复
        Organize o=dao().fetch(Organize.class,org.getName());
        if(o!=null){
            return nutMap.setv("ok",false).setv("msg","组织岗位名重复,添加失败.");
        }
        User user= ShiroSession.getLoginUser();
        // 获取修改的记录的人员
        if(user!=null){
            org.setCreateBy(String.valueOf(user.getId()));
        }
        org.setOrganizeType(OrganizeType.getByCode(org.getOrganizeCode()));
        org.setCreateTime(new Date());
        org.setStatus(PublicStatus.ABLE);
        Organize or=dao().insert(org);
        if(or!=null){
            return nutMap.setv("ok",true).setv("msg","添加成功.");
        }
        return nutMap.setv("ok",false).setv("msg","添加失败.");
    }

    /**
     * 通过Tree递归获取机构组织岗位树结构
     * @return
     */
    public List<JsonTreeData> getOrganizeTree(){
        List<Organize> Orlist=dao().query(Organize.class, Cnd.where("status","=","ABLE"));
        List<JsonTreeData> jsonTreeDatasList=new ArrayList<JsonTreeData>();
        if(Orlist!=null){
            for (Organize o:Orlist) {
                JsonTreeData treeData=new JsonTreeData();
                treeData.setId(o.getId());
                treeData.setPid(o.getParentId());
                treeData.setText(o.getName());
                treeData.setOrgType(o.getOrganizeType().getCode());
                treeData.setPath(o.getPath());
                jsonTreeDatasList.add(treeData);
            }
        }
        return TreeNodeUtil.getfatherNode(jsonTreeDatasList);
    }

    /**
     * 修改机构组织岗位信息
     * @param org
     * @return
     */
    public NutMap update(Organize org){
        String sqlWhere="";
        NutMap nutMap=new NutMap();
        //判断组织岗位名是否重复
        Organize o=dao().fetch(Organize.class,org.getName());
        if(o!=null){
            if(!o.getName().equals(org.getName())){
                return nutMap.setv("ok",false).setv("msg","名称重复,修改失败.");
            }
        }
        User user=ShiroSession.getLoginUser();
        // 获取修改的记录的人员(session获取，后续加上)
        if(user!=null){
            org.setUpdateBy(String.valueOf(user.getId()));
        }
        // 获取修改时间
        org.setUpdateTime(new Date());
        // 根据organizeCode 判断修改字段
        if(org.getOrganizeCode().equals("1300")){
            sqlWhere="^(name|code|agencyName|agencyAddress|agencyTel|updateTime|updateBy)$";
        }else {
            sqlWhere="^(name|updateTime|updateBy)$";
        }
        int num=dao().update(org,sqlWhere);
        if(num==1){
            return nutMap.setv("ok",true).setv("msg","修改成功.");
        }
        return  nutMap.setv("ok",false).setv("msg","修改失败.");
    }

    /**
     * 通过id删除某个机构组织岗位
     * @param id
     * @return
     */
    public NutMap deleteById(String id){
        NutMap nutMap=new NutMap();
        // 查询id 是否存在
        Organize or=dao().fetch(Organize.class,id);
        if(or==null){
            return nutMap.setv("ok",false).setv("msg","该组织或者岗位不存在.");
        }
        // 判断是否是岗位
        if(or.getOrganizeType().getCode().equals("1302")){
            // 判断组织下面是否有组织，没有组织，可以删除，有组织则不能删除
            List<Organize> list=dao().query(Organize.class,Cnd.where("parentId","=",or.getId()));
            // 如果size大于0，表示组织下面挂组织，不能删除
            if(list.size()>0){
                return nutMap.setv("ok",false).setv("msg","该组织数据有下一级数据，不能删除.");
            }
        }else{
            // 如果是岗位，判断岗位下面是否有关联的用户，没有用户可以删除，有用户不能删除
            int count= userService.countUser(Cnd.where("organizeId","=",or.getId()));
            // 如果不为空，表示岗位下面有对应的用户，不能删除此岗位
            if(count!=0){
                return nutMap.setv("ok",false).setv("msg","该岗位有对应的用户，不能删除.");
            }
        }
        int num=dao().delete(Organize.class,id);
        if (num>0){
            return nutMap.setv("ok",true).setv("msg","该组织数据删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","该组织数据删除失败");
    }


    /**
     * 通过id查询机构组织方法
     * @param id
     * @return
     */
    public Organize fetch(String id){
        return dao().fetch(dao().fetch(Organize.class,id));
    }
}
