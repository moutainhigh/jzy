package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.excelUtil.ExcelChannelTmplView;
import com.kaisa.kams.components.utils.excelUtil.ReadExcelUtil;
import com.kaisa.kams.components.view.loan.ChannelView;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Channel;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.business.BusinessUser;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.upload.TempFile;
import org.nutz.service.IdNameEntityService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by pengyueyang on 2017/3/1.
 */
@IocBean(fields="dao")
public class ChannelService extends IdNameEntityService<Channel> {

    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private  BusinessUserService businessUserService;


    public List<ChannelView> findChannelsByName(String name) {
        List<ChannelView> channels =  new ArrayList<>();
        if (StringUtils.isNotEmpty(name)) {
            Sql sql = Sqls.queryEntity("select id,name,code from sl_channel where name like @name");
            sql.params().set("name", "%" + name + "%");
            sql.setEntity(dao().getEntity(ChannelView.class));
            dao().execute(sql);
            return sql.getList(ChannelView.class);
        }
        return channels;
    }

    public String findChannelNameById(String id) {
        if (StringUtils.isNotEmpty(id)) {
            Channel channel = dao().fetch(Channel.class, id);
            return null != channel ? channel.getName() : "";
        }
        return "";
    }

    public Object listByChannelName(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String channelName = "";
        String channelType = "";
        String managerId="";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            channelName = keys.get("channelName");
            channelType = keys.get("channelType");
            managerId=keys.get("managerId");
        }
        return query(channelName,channelType,pager,param.getDraw(),managerId);
    }

    public List<Channel> listAble() {
        List<Channel> channelList=null;
        channelList=dao().query(Channel.class,Cnd.where("status","=", PublicStatus.ABLE));
        return channelList;
    }

    //渠道类型
    public List<Channel> listAbleByType() {
        List<Channel> channelList=null;
        channelList=dao().query(Channel.class,Cnd.where("status","=", PublicStatus.ABLE).and("channelType","=","1"));
        return channelList;
    }

    public Object listChannelName(String channelName,String channelType) {
        Cnd cnd=null;
        List<Channel> channelList=null;

            SqlExpressionGroup e=Cnd.exps("name","like","%"+channelName+"%").or("fullName","like","%"+channelName+"%");
            SqlExpressionGroup e1=Cnd.exps("channelType","=",channelType).and("status","=", PublicStatus.ABLE);
            cnd=Cnd.where(e).and(e1);
        channelList=dao().query(Channel.class,cnd);
        return channelList;
    }

    public DataTables query(String channelName,String channelType, Pager pager, int draw,String managerId){
        Cnd cnd=null;
        List<Channel> channelList=null;
        List<Channel> channelList1=null;
        if(!Strings.isBlank(channelName)&&!Strings.isBlank(managerId)){
            SqlExpressionGroup e=Cnd.exps("name","like","%"+channelName+"%").or("fullName","like","%"+channelName+"%");
            SqlExpressionGroup e1=Cnd.exps("channelType","=",channelType);
            SqlExpressionGroup e2=Cnd.exps("managerId","=",managerId);
            cnd=Cnd.where(e).and(e1).and(e2);
        }else if(!Strings.isBlank(channelName)&&Strings.isBlank(managerId)){
            SqlExpressionGroup e=Cnd.exps("name","like","%"+channelName+"%").or("fullName","like","%"+channelName+"%");
            SqlExpressionGroup e1=Cnd.exps("channelType","=",channelType);
            cnd=Cnd.where(e).and(e1);
        }else if(Strings.isBlank(channelName)&&!Strings.isBlank(managerId)){
            SqlExpressionGroup e2=Cnd.exps("managerId","=",managerId);
            SqlExpressionGroup e1=Cnd.exps("channelType","=",channelType);
            cnd=Cnd.where(e2).and(e1);
        }else {
            SqlExpressionGroup e1=Cnd.exps("channelType","=",channelType);
            cnd=Cnd.where(e1);
        }
        channelList=this.query(cnd.desc("updateTime"),pager);
        if(channelList!=null){
            if(channelType.equals("0")){
                channelList1=new ArrayList<Channel>();
                for (Channel c: channelList) {
                    BusinessUser businessUser=businessUserService.fetchById(c.getManagerId());
                    //拼接自营合作方对接人
                    String manager=businessUser.getOrganize().getBusinessLine().getDescription()+"-"+businessUser.getOrganize().getCode()+"-"+c.getManager();
                    c.setManager(manager);
                    channelList1.add(c);
                }
                channelList=channelList1;
            }
        }

        return new DataTables(draw,this.dao().count(Channel.class),this.dao().count(Channel.class,cnd),channelList);
    }


    /**
     * 新增渠道
     * @param channel
     * @return
     */
    public boolean insert(Channel channel){
        NutMap nutMap = new NutMap();
        User user = ShiroSession.getLoginUser();
        // 获取新增的记录的人员
        if (channel!=null){
            channel.setCreateTime(new Date());
            channel.setCreateBy(String.valueOf(user.getName()));
        }
        // 添加合作方产品类型表
        Channel channel1=dao().insert(channel);
        if(channel1!=null){
           return true;
        }
        return false;
    }

    /**
     * 获取渠道code
     * @return
     */
    public String getCode(){
        String code="";
        DecimalFormat format = new DecimalFormat("00000");
        List<Channel> c_list=dao().query(Channel.class,Cnd.where("channelType","=","1").orderBy().desc("code"));
        if(c_list.size()==0){
            //判断是第一条数据
            code="QD00001";
        }else {
            //按照code排序，取最后一条数据
            Channel c=c_list.get(0);
            int codeNum=Integer.valueOf(c.getCode().substring(2,c.getCode().length()))+1;
            code="QD"+format.format(codeNum);
        }
        return  code;
    }
    public boolean update(Channel channel){
        User user= ShiroSession.getLoginUser();
        // 获取修改的记录的人员
        if(user!=null){
            channel.setUpdateBy(String.valueOf(user.getName()));
        }
        channel.setUpdateTime(new Date());
        boolean boo=Daos.ext(dao(), FieldFilter.locked(Channel.class,"^id|code|channelType|createBy|createTime")).update(channel)>0;
        return boo;
    }
    public String readExcelFile(TempFile mFile) {
        String result ="";
        //创建处理EXCEL的类
        ReadExcelUtil readExcel=new ReadExcelUtil();
        //解析excel，获取上传的事件单
        List<ExcelChannelTmplView> list = readExcel.getExcelInfo(mFile);
        //至此已经将excel中的数据转换到list里面了,接下来就可以操作list,可以进行保存到数据库
      try {
          if(list != null && !list.isEmpty()){
              excelToDb(list);
              result="初始化数据成功";
          }else {
              result="初始化数据失败";
          }
      }catch (Exception e){
          result="初始化数据失败";
      }
        return result;
    }
    public void  excelToDb(List<ExcelChannelTmplView> list){
        for (ExcelChannelTmplView e:list) {
             //通过渠道经理名称获取渠道经理id
             String managerId=businessUserService.fetchByName(e.getManager()).getId();
             //通过Id 查询渠道信息
            Channel channel = dao().fetch(Channel.class, e.getId());
            // 更新需要更新的字段
            channel.setCode(e.getCode());
            channel.setManagerId(managerId);
            channel.setManager(e.getManager());
            dao().update(channel);
        }
    }

    /**
     * 输入渠道名查询业务来源
     */
    public List<Channel> listChannelName(String channelName,String channelType,String name) {
        Sql sql = Sqls.fetchString("select id from sl_product where name = @name");
        sql.setParam("name", name);
        String cooperationProductType = dao().execute(sql).getString();
        SqlExpressionGroup section1=Cnd.exps("name","like","%"+channelName+"%").or("fullName","like","%"+channelName+"%");
        SqlExpressionGroup section2=Cnd.exps("channelType","=",channelType).and("status","=", PublicStatus.ABLE).and("cooperationProductType","like","%"+cooperationProductType+"%");
        return dao().query(Channel.class, Cnd.where(section1).and(section2));
    }

    /**
     * 输入渠道名查询业务来源
     */
    public List<Channel> listChannelNameForChannel(String channelName,String channelType,String name,User u) {
        String ids = u.getChannels();
        Sql sql = Sqls.fetchString("select id from sl_product where name = @name");
        sql.setParam("name", name);
        String cooperationProductType = dao().execute(sql).getString();
        SqlExpressionGroup section1=Cnd.exps("name","like","%"+channelName+"%").or("fullName","like","%"+channelName+"%");
        SqlExpressionGroup section2=Cnd.exps("channelType","=",channelType).and("status","=", PublicStatus.ABLE).and("cooperationProductType","like","%"+cooperationProductType+"%");
        SqlExpressionGroup section3 = null;
        if(StringUtils.isNotEmpty(ids)){
            section3=Cnd.exps("id","in",ids.split(","));
        }
        return dao().query(Channel.class, Cnd.where(section1).and(section2).and(section3));
    }

}
