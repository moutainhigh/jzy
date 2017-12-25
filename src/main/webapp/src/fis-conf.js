/**
 * Created by pengs on 2017/1/6.
 */
// 加 md5
fis.match('/{js,css,image,h5}/**.*', {
    useHash: true,
    release: '/dist/$0'
});

fis.match('/js/plugins/**.*', {
    useHash: false,
    optimizer: null,
    release: '/dist/$0'
});

//html copy to templates
fis.match('/templates/**.*', {
    release:'/WEB-INF/$0'
});


fis.match('/js/**.js', {
    // fis-optimizer-uglify-js 插件进行压缩，已内置
    optimizer: fis.plugin('uglify-js')
});

fis.match('/css/**.css', {
    // fis-optimizer-clean-css 插件进行压缩，已内置
    optimizer: fis.plugin('clean-css')
});



fis.media('debug').match('*.{js,css,png}', {
    useSprite: false,
    optimizer: null
})

fis.media('prod').match('/js/common/settings.js', {
    parser: fis.plugin('jdists', {
        remove: "notprod"
    })
})

//开发环境：fis3 release debug -w -d ../
//测试发布：fis3 release  -d ../
//生产发布：fis3 release prod  -d ../