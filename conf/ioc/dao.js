var ioc = {
    conf : {
        type : "org.nutz.ioc.impl.PropertiesProxy",
        fields : {
            paths : ["/data0/java/config/custom/"]
        }
    },

    dao : {
        type : "org.nutz.dao.impl.NutDao",
        args : [{refer:"dataSource"}]
    },

    dataSource : {
        type : "com.alibaba.druid.pool.DruidDataSource",
        events : {
            depose : 'close'
        },
        fields : {
            driverClassName : {java :"$conf.get('jdbc.drive')"},
            url             : {java :"$conf.get('jdbc.url')"},
            username        : {java :"$conf.get('jdbc.username')"},
            password        : {java :"$conf.get('jdbc.password')"},
            filters         : "config",
            connectionProperties : {java :"$conf.get('jdbc.connectionProperties')"},
            initialSize:100,
            maxActive:200,
            maxWait: 15000, // 若不配置此项,如果数据库未启动,druid会一直等可用连接,卡住启动过程,
            defaultAutoCommit : false // 提高fastInsert的性能
        }
    }
}