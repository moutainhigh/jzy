

function send_request(dir) {
	/* 获取上传Token */
	var xmlhttp = null;
	if (window.XMLHttpRequest) {
		xmlhttp=new XMLHttpRequest();
	} else if (window.ActiveXObject){
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	if (xmlhttp!=null) {
		var tokenUrl = '/media_upload/fetchAliOssToken?dir=' + dir
		xmlhttp.open( "GET", tokenUrl, false );
		xmlhttp.send( null );
		return xmlhttp.responseText
	} else{
		alert("Your browser does not support XMLHTTP.");
	}
}

function set_upload_param(up, filename, dir) {
	var obj = JSON.parse(send_request(dir));
	var new_multipart_params = {
		'key' : obj['dir'] + filename,  // 文件重命名
		'policy': obj['policy'],
		'OSSAccessKeyId': obj['accessid'],
		'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
		'signature': obj['signature']
	}
	up.setOption({
		'url': obj['host'],
		'multipart_params': new_multipart_params
	});
}
function upArryList(k){
	 mediaDetail = [];
	if(k){
		var n = parseFloat(k);
	}else{
		var n = $('.js-uploadBtn').length;
	}
	for(var i = 0 ;i<n;i++){
		mediaDetail[i] = [];
	}
}
upArryList();

var fileList;
var Uploader = {
	/**
	 * 公用参数
	 */
	common_options: {
		runtimes : 'html5,flash,silverlight,html4',
		browse_button : 'selectfiles',
		// container: document.getElementById('container'),
		flash_swf_url : 'lib/plupload-2.1.2/js/Moxie.swf',
		silverlight_xap_url : 'lib/plupload-2.1.2/js/Moxie.xap',
		url : 'http://oss.aliyuncs.com',
		filters: {
			max_file_size : '20480kb', //最大只能上传20m的文件
		}, resize:{
			quality: 50,
			preserve_headers: false
		},
		init: {
			PostInit: function() {
			},
			FilesAdded: function(up, files) {
				// 文件被选择时就自动上传, 文件重命名规则: 原文件名+时间+三位随机值
				var $browseBtn = $(up.settings.browse_button[0]);
				$('.js-uploadBtn').addClass('loading');
				$('.js-uploadBtn').siblings().find('input').attr('type','hidden');
				var $medias = $browseBtn.parents("tr").find(".js-medias");
				fileList = files;
				setDom(up,fileList,$medias);//首次执行
				up.start();
			},

			UploadProgress: function(up, file) {
				// 进度显示
				$('.label_'+file.id).parents('.js-progress').progress('set percent',file.percent);
			},

			FileUploaded: function(up, file, info) {
				var $upProgree = $("#"+up.id);
				var $browseBtn = $(up.settings.browse_button[0]);
				var $medias = $browseBtn.parents("tr").find(".js-medias");
				var fileName = $('.label_'+file.id).data('name');
				var $mediaDetail = $upProgree.parents("tr").find("input[name='mediaDetail']");
				// 上传成功
				if (info.status == 200) {
					$mediaDetail.val($mediaDetail.data("detail"));
					$("#"+up.id+" .js-progress").find(".label_"+file.id).html('上传成功&emsp;<span class="js-temp ui teal button mini" data-name="'+fileName+'" href="'+settings.accessHost + $browseBtn.data("dir") + "/" + fileName+'">查看</span><a href="javascript:;" class="js-remove">&emsp;删除</a>');
					fileList.shift();
					if(fileList.length>0){
						setDom(up,fileList,$medias);//循环执行
						up.refresh();
						up.start();
					}else{
						$('.js-uploadBtn').removeClass('loading');
						$('.js-uploadBtn').siblings().find('input').attr('type','file');
					}
				} else {
					$('.js-uploadBtn').removeClass('loading');
					$('.js-uploadBtn').siblings().find('input').attr('type','file');
					$("#" + up.id + ".js-progress").find(".label_"+file.id).text(info.response);
				}
			},
			Error: function(up, err) {
				// 上传错误
				var id = err.file.id;
				$.uiAlert({textHead:'',type:'danger',text:'上传失败，'+err.message});
				$("#"+up.id+" .js-progress").addClass("error").progress('set percent', 100);
				$("#"+up.id+" .js-progress").find(".label_"+id+"").text("上传失败(" + err.message + ")");
				$('.js-uploadBtn').removeClass('loading');
				$('.js-uploadBtn').siblings().find('input').attr('type','file');
			}
		}
	},
	set: function(option) {
		var othis = this;
		return new plupload.Uploader($.extend({}, othis.common_options, option))
	}
};

//填充提交数据&&渲染dom
function setDom(up,file,obj){
	var $browseBtn = $(up.settings.browse_button[0]);
	var lastStr = file[0].name.lastIndexOf('.');
	var t0 = file[0].name.substring(0,lastStr);
	var t1 = file[0].name.substring(lastStr+1,file[0].name.length);
	var file_name = t0 + '_' + (new Date()).Format("yyyyMMddhhmmss") + '.' + t1;
	var url = settings.accessHost + $browseBtn.data("dir") + "/" + file_name;
	var i = $('tr.mediaItem').index($browseBtn.parents('tr.mediaItem'));
	set_upload_param(up, file_name, $browseBtn.data("dir"));
	obj.append(
		'<div class="js-canBeDel">'+
		'<p>'+file_name + ' (' + plupload.formatSize(file[0].size) + ')</p>' +
		'<div class="ui indicating small progress  js-progress" data-percent="0" >' +
		'<div class="bar"><div class="progress"></div></div>' +
		'<div class="label label_'+file[0].id+'" data-name="'+file_name+'"></div>'+
		'</div>'+
		'</div>'
	).attr({
		id: up.id
	});
	mediaDetail[i].push({
		url:url,
		attachName:file_name
	});
	$browseBtn.parents("tr").find("input[name='mediaDetail']").data("detail",JSON.stringify(mediaDetail[i]))
}