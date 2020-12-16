(function(ta){var _=ta.menu=function(){};
_.initMainMenu=function(id){
	var COOKIE_PID="_current_pid",COOKIE_URL="_current_url";
	var FOLD1=" <span class='fold'></span>";
	var FOLD2=" <span class='expand'></span>";
	var saveItemStatus=function(i,a){
		$.cookie(COOKIE_PID,i.id,{expires:1});
		var a1=$(i).find("a")[0];
		if(a1){$.cookie(COOKIE_URL,a?a.pathname:a1.pathname,{expires:1})}};
		$("#"+id+" > ul").each(function(i,e){var tmp;
		var ul=$(e).addClass("menu-items").data("level",0);
		var title=ul.prev("a");
		if(!title.length){title=$(document.createElement("a")).attr("href","#").insertBefore(ul).html((tmp=ul.attr("data-menu-title"))?tmp:"&nbsp;")}title.addClass("menu-title");
		ul.children("li").each(function(){$(this).click(function(){saveItemStatus(this)})});$("ul",ul).each(function(i1,e1){var ul=$(e1).addClass("menu-items");
		var hidden=(e1.style.display=="none");var title=ul.prev("a");
		if(!title.length){title=$(document.createElement("a")).attr("href","#").insertBefore(ul).html((tmp=ul.attr("data-menu-subtitle"))?tmp:"&nbsp;")}title.addClass("menu-subtitle");
		if(hidden){title.append("<span class='folding'>"+FOLD1+"</span>").click(function(){
			if(e1.style.display=="none"){ul.slideDown("slow").parent().siblings("li").children("ul.menu-items").filter(function(){return this.style.display!="none"}).each(function(i,e2){
				if(e2!=e1&&e2.style.display!="none"){
			var t=$(e2).slideUp("slow").prev(".menu-title,.menu-subtitle");
			$("span.folding",t).html(FOLD1)}});$("span.folding",title).html(FOLD2)
			}else{ul.slideUp("slow");$("span.folding",title).html(FOLD1)}})}var li=ul.parent("li");var ul0=li.parent("ul.menu-items");
			ul.data("level",ul0.data("level")+1);
			ul.children("li").each(function(){$(this).css("padding-left","1em").click(function(){saveItemStatus(this)})})})});
			var pid=(function(){var p=location.pathname;
			var pid=null;
			if(!$.cookie||!(pid=$.cookie(COOKIE_PID))||p!=$.cookie(COOKIE_URL)){
				var a=null;$("#"+id+" ul.menu-items a").each(function(i,e){
					var href=e.href;
					if(!href||href.charAt(href.length-1)=="#"){return true}
					if(e.pathname==p){a=e;return false}});
					if(!a){return pid}var li=$(a).parent("li")[0];
					if(li.length==0){return pid}
					if($.cookie){saveItemStatus(li,a)}pid=li.id}return pid})();
					if(pid){$("#"+id+" #"+pid).addClass("highlight").parents("ul.menu-items").filter(function(){return this.style.display=="none"}).each(function(i,e){
						e.style.display="block";
						$("span.folding",$(e).prev()).html(FOLD2)})}return _};
						_.prepareMenuSkeleton=function(id,json){
							var d=$("#"+id);
							var items=(typeof(json)=="string")?eval("("+json+")"):json;
							for(var i in items){
								var item=items[i];
							if(item.folder&&item.folder.length>0){
								(ul=drawMenuSkeleton(0,item))&&d.append(ul)}}return _};
								var drawMenuSkeleton=function(level,item){var d=document;
								var ul=$(d.createElement("ul")).attr((level==0)?"data-menu-title":"data-menu-subtitle",(item.label==null)?"&nbsp;":item.label);
								if(item.expand!=undefined&&!item.expand){ul.css("display","none")}
								var n=0;
								for(var i in item.folder){
									var item2=item.folder[i];
									if(item2.folder){if(item2.folder.length>0){
										var ul2=drawMenuSkeleton(level+1,item2);
										if(ul2){n++;$(d.createElement("li")).appendTo(ul).append(ul2)}}
										}else{n++;$(d.createElement("li")).appendTo(ul).attr("id",item2.id).append($(d.createElement("a")).attr("href",item2.url).html(!item2.label?"&nbsp;":item2.label))}}
										if(n>0){return ul}return null}})(ta);