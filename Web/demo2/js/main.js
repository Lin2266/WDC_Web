(function() {
var _ = function() {};

var ta = window.ta = new _();
var ACTION_PARAM = "_action";
if(jQuery) {
	$.ajaxSetup({
		//cache:false,
		type:"POST"
	});
}

var EMPTY_ARRAY_PARAMETER_VALUE = encodeURIComponent("_ _");
var KEY_LISTSLIDE_PAGENO = "_listslide_page_";

_.prototype.loading_ = null;
_.prototype.messageBar_ = null;
_.prototype.messageBarMsg_ = null;

var hasMessage_ = false;
var _items = null;
var _contextPath = null;
var _requestURI = null;
var _tmpFormId = "_tmp_form_id_";

_.prototype.currentURI = function() {
	return !_requestURI ? location.pathname : _requestURI;
}
var ajaxURI = function(uri) {
	var p = ta.currentURI();
	if(noParam(p) && uri.indexOf("/") != 0) {
		var i, param;
		if((i = uri.indexOf("?")) != -1) {
			if((uri.length - i) > 1)
				param = uri.substring(i + 1);
			uri = uri.substring(0, i);
		}
		var uri2 = p.slice(0, -4) + ".ajax?" + ACTION_PARAM + "=" + uri;
		if(param != undefined)
			uri2 += "&" + param;
		return uri2;
	}
	return uri;
}

var actionURI = function(uri) {
	if(uri.indexOf("/") == 0)
		return uri;

	var i, param;
	if((i = uri.indexOf("?")) != -1) {
		if((uri.length - i) > 1)
			param = uri.substring(i + 1);
		uri = uri.substring(0, i);
	}
	uri = ta.currentURI() + "?" + ACTION_PARAM + "=" + uri;
	if(param != undefined)
		uri += "&" + param;
	return uri;


}
var contextPath = function() {
	if(!_contextPath)
		throw new Error("context-path not set via ta.internal.initContextPath()");
	return _contextPath;
}
var noParam = function(p) {
	return p.length > 4 && p.slice(-4) == ".jsp";
}

var _ckMsgBar = function() {
	return (ta.messageBar_ != null) && (ta.messageBarMsg_ != null);
}
var _ckJQ = function() {
	if(!jQuery)
		alert("jQuery not imported");
}
var _ckJQForm = function() {
	_ckJQ();
	if(!$.fn.ajaxForm)
		alert("jQuery.form not imported");
}
var _ckJQCascade = function() {
	_ckJQ();
	if(!$.ui || !$.ui.cascade)
		alert("jQuery.cascade not imported")
}
var _ckJQTabs = function() {
	_ckJQ();
	if(!$.ui || !$.ui.tabs)
		alert("jQuery.ui.tabs not imported");
}
var _ckJQDialog = function() {
	_ckJQ();
	if(!$.ui || !$.ui.dialog)
		alert("jQuery.ui.dialog not imported");
}
var _ckJQJSON = function() {
	_ckJQ();
	if(!$.evalJSON)
		alert("jQuery.json not imported");
}
var _ckAjaxFileUpload = function() {
	_ckJQ();
	if(!$.ajaxFileUpload2)
		alert("ajaxFileUpload2 not imported");
}
var _ckPortlet = function() {
	_ckJQ();
	if(!$.ui || !$.ui.portlet)
		alert("ui.portlet not imported");
}
var _lfToBrForFormForIE = function(map) {
	return map;
}

var mapKeys = function(m) {
	if(!m)
		return null;
	if(Object.keys)
		return Object.keys(m);
	var ret = [];
	for(var i in m)
		ret.push(i);
	return ret;
}

var arrayVal = function(a, i) {
	if(i >= a.length)
		return null;
	return a[i];
}

var arrayVals = function(a, ii) {
	var len = a.length, ii2 = [];
	for(var i in ii) {
		if(ii[i] >= len)
			return null;
		ii2.push(a[ii[i]]);
	}
	return ii2;
}

var clone = function(obj, deep) {
	return !obj ? obj :
		!deep ? $.extend({}, obj) : $.extend(true, {}, obj);
}

var isEmptyMap = function(map) {
	for(var k in map) {
		if(map.hasOwnProperty(k))
			return false;
	}
	return true;
}

var jqObj = function(id) {
	var o = id;
	if(o && o.jquery)
		return o;
	if(typeof(o) == "string") {
		var e = $("#" + o);
		return e;
	}
	return $(o);
}

var formSerializeEmptyArray = function(formId) {
	var o = jqObj(formId);
	if(o.length == 0)
		return "";
	var t, n, m = {};
    $(":input", o).each(function(i, e) {
        n = e.name;
        if(m[n])
        	return true;
        t = e.type;
        if(t == "select-multiple")
    		m[n] = !!$(e).val();
        else if(t == "checkbox")
    		m[n] = e.checked;
    });

    var p = {};
    for(var i in m) {
    	if(!m[i])
    		p[i] = EMPTY_ARRAY_PARAMETER_VALUE;
    }
    return p;
}

_.prototype.onReady = function(func) {
	_ckJQ();
	$(func);
	return ta;
}

_.prototype.registerLoadingMarkId = function(id) {
	$(function() {
		ta.loading = $("#" + id);
		ta.loading.ajaxStart(function(){$(this).show();}).ajaxStop(function(){$(this).hide();});
	});
	return ta;
}

_.prototype.registerMessageBar = function(messageBar, messageBarMsg) {
	$(function() {
		ta.messageBar_ = $("#" + messageBar);
		ta.messageBarMsg_ = $("#" + messageBarMsg);
	});
	return ta;
}

_.prototype.showMessage = function(msg) {
	alert(msg);
	hasMessage_ = true;
	return ta;
}

_.prototype.clearMessage = function() {
	if(ta.loading != null)
		ta.loading.hide();
	if(!hasMessage_)
		return ta;
	hasMessage_ = false;
	if(!_ckMsgBar())
		return ta;
	ta.messageBar_.hide();
	ta.messageBarMsg_.text("");
    return ta;
}

_.prototype.queryString = function(frm) {
	if(!frm)
		return null;
	if(typeof(frm) == "string")
		frm = ta.formToMap(frm);

	var p = [];
	for(var k in frm) {
		var v = frm[k];
		if(typeof(v) == "object") {
			if($.isArray(v)) {
				for(var i in v)
					p.push(k + "=" + encodeURIComponent((typeof(i) != "object") ? v[i] : ta.toJSON(v[i])));
			} else {
				p.push(k + "=" + encodeURIComponent(ta.toJSON(v)));
			}
		} else {
			p.push(k + "=" + encodeURIComponent(v));
		}
	}
	return p.join("&");
}

var ajax = function(url, args, opt, onSuccessful, isResponseJSON, onJSONResponseError, onBeforeSend) {
	_ckJQ();
	ta.clearMessage();

	var f = !isResponseJSON ? onSuccessful : function(res) {
			if(typeof(onSuccessful) == "function") {
				var res2;
				try {
					res2 = ta.evalJSON(res);
				} catch(e) {
					if(typeof(onJSONResponseError) == "function") {
						onJSONResponseError(res);
					} else {
						ta.showMessage(res);
					}
				}
				if(res2)
					return onSuccessful(res2, res);
			}
		};
	var settings = { type:opt.type, url:ajaxURI(url), dataType:"html", success:f, complete: function() { if(ta.loading != null) ta.loading.hide(); } };
	if(onBeforeSend != undefined)
		settings.beforeSend = onBeforeSend;
	if(opt.async != undefined && !opt.async)
		settings.async = false;

    if(typeof(args) == "string" && args) {
    	_ckJQForm();
    	settings.data = formSerializeEmptyArray(args);
        $("#" + args).ajaxSubmit(settings);
        return ta;
    }
    if(args)
    	settings.data = ta.queryString(args);
    $.ajax(settings);

    return ta;
}

_.prototype.get = function(url, args, onSuccessful) {
	var o = url;
	var s = { type:"get" };
	if(typeof(o) == "string")
		return ajax(o, args, s, onSuccessful, false);
	s.async = (o.async == undefined || o.async);
	return ajax(o.url, o.args, s, o.onSuccessful, false, null, o.onBeforeSend);
}

_.prototype.getForJSON = function(url, args, onSuccessful, onJSONResponseError) {
	var o = url;
	var s = { type:"get" };
	if(typeof(o) == "string")
		return ajax(o, args, s, onSuccessful, true, onJSONResponseError);
	s.async = (o.async == undefined || o.async);
	return ajax(o.url, o.args, s, o.onSuccessful, true, o.onJSONResponseError, o.onBeforeSend);
}

_.prototype.post = function(url, args, onSuccessful) {
	var o = url;
	var s = { type:"post" };
	if(typeof(o) == "string")
		return ajax(o, args, s, onSuccessful, false);
	s.async = (o.async == undefined || o.async);
	return ajax(o.url, o.args, s, o.onSuccessful, false, null, o.onBeforeSend);
}

_.prototype.postForJSON = function(url, args, onSuccessful, onJSONResponseError) {
	var o = url;
	var s = { type:"post" };
	if(typeof(o) == "string")
		return ajax(o, args, s, onSuccessful, true, onJSONResponseError);
	s.async = (o.async == undefined || o.async);
	return ajax(o.url, o.args, s, o.onSuccessful, true, o.onJSONResponseError, o.onBeforeSend);
}


_.prototype.submit = function(url, args, settings) {
	var act = actionURI(url);
	var v, target = "_self", method = "post";
	if(settings) {
		if(v = settings.method)
			method = v;
		else if(v = settings.target)
			target = v;
	}

	var f;
	if(typeof(args) == "string" && args) {
		f = $("#" + args)[0];
		f.action = act;
		f.method = method;
		f.target = target;
		f.submit();
	} else {
		if(method.toUpperCase() == "GET") {
			if(args)
				act += (noParam(act) ? "?" : "&") + ta.queryString(args);
			document.location.href = act;
		} else {
			f = $("#" + _tmpFormId);
			if(f.length == 0)
				f = $("<form id='" + _tmpFormId + "' style='display:hidden'></form>").appendTo($("body"));
			else
				f.empty();

			if(args) {
				for(var k in args) {
					v = args[k];
					if($.isArray(v)) {
						for(var i in v)
							f.append("<input type='checkbox' name='" + k + "' value='" + v[i] + "' checked/>");
					} else {
						f.append("<input type='hidden' name='" + k + "' value='" + v + "'/>");
					}
				}
			}
			f.attr({ method: method, action: act, target:target });
			f.submit();
		}
	}
}

_.prototype.portletAdd = function(containerId, options, onsuccessful) {
	_ckPortlet();
	var c = jqObj(containerId);
	if(!c)
		throw new Error("Portlet container '" + containerId + "' nor exists");
	var o = options;
	if(!o)
		throw new Error("argument 'options' not specified");

	var p = {
		attrs: { id: null },
		title: null,
		singleView: { enable: null, recovery: null, width: null, height: null },
		content: {
			type: "ajax", url: null, data: null, afterShow: null,
			style: null
		},
		afterRefresh: null,
		disableClose: false,
		disableMinimize: false,
		disableSingleView: false
	};
	p.attrs.id = (!o.id) ? ("p" + new Date().getTime()) : o.id;
	p.title = (!o.title) ? "" : o.title;
	p.disableClose = !!o.disableClose;
	p.disableMinimize = !!o.disableMinimize;
	p.disableSingleView = !!o.disableMaximize;
	if(o.height)
		p.content.style = { height: o.height };
	if(o.maximizedWidth)
		p.singleView.width = o.maximizedWidth;
	if(o.maximizedHeight)
		p.singleView.height = o.maximizedHeight;
	if(o.url)
		p.content.url = o.url;
	if(o.args)
		p.content.data = ta.queryString(o.args);
	if($.isFunction(o.onmaximize))
		p.singleView.enable = o.onmaximize;
	if($.isFunction(o.onnormalize))
		p.singleView.recovery = o.onnormalize;
	if($.isFunction(o.onAfterRefresh))
		p.afterRefresh = o.onAfterRefresh;
	if($.isFunction(onsuccessful))
		p.content.afterShow = onsuccessful;
	c.portlet("createPortlet", p, o.columnIndex);
	return ta;
}

_.prototype.formClean = function(formId, includeHidden, includeReadOnly) {
	_ckJQ();
	var f = jqObj(formId);
    var h = (includeHidden == undefined || includeHidden);
    var r = includeReadOnly;
    $(":input", f).each(function(i, e) {
    	if(!r && $(e).attr("readonly"))
    		return true;
        var t = e.type;
        if(t == "text" || t == "password" || t == "textarea" || (t == "hidden" && h) || t == "file" || t == "number" || t == "email" || t == "tel")
            e.value = "";
        else if(t == "checkbox" || t == "radio")
            e.checked = false;
        else if(t == "select-one")
        	e.selectedIndex = ($(e).children().length > 0) ? 0 : -1;
        else if(t == "select-multiple")
        	e.selectedIndex = -1;
        else if(t == "range")
        	e.value = "";
    });
    return ta;
}

_.prototype.formEnable = function(formId, yesNo) {
	_ckJQ();
	var f = jqObj(formId);
	if(yesNo == undefined || yesNo == "true" || yesNo) {
		$(":input", f).each(function() {
	        $(this).removeAttr("disabled");
	    });
	} else {
		$(":input", f).each(function() {
	        $(this).attr("disabled", true);
	    });
	}
    return ta;
}

_.prototype.formReadonly = function(formId, yesNo) {
	_ckJQ();
	var f = jqObj(formId);
	if(yesNo == undefined || yesNo == "true" || yesNo) {
		$(":input", f).each(function(i, e) {
			if(e.style.display != "none")
				$(e).attr("readonly", "readonly");
		});
	} else {
		$(":input", f).each(function(i, e) {
			if(e.style.display != "none")
				$(e).removeAttr("readonly");
		});
	}
	return ta;
}

_.prototype.formCopy = function(srcFormId, destFormId) {
	ta.mapToForm(ta.formToMap(srcFormId), destFormId);
	return ta;
}

_.prototype.formToMap = function(formId) {
	_ckJQ();
	var f = jqObj(formId);
	if(!f)
		return {};
    var t, v, n, m = {};
    $(":input", f).each(function(i, e) {
        t = e.type;
        n = e.name;
        if(t == "text" || t == "hidden" || t == "textarea" || t == "password" || t == "range" ||
        		t == "number" || t == "email" || t == "tel" || t == "select-one") {
        	m[n] = e.value;
        } else if(t == "select-multiple") {
        	m[n] = ((v = $(e).val()) == null) ? [] : v;
        } else if(t == "radio") {
        	if(e.checked)
        		m[n] = e.value;
        } else if(t == "checkbox") {
        	if(!(v = m[n]))
        		m[n] = v = [];
        	if(e.checked)
	        	m[n][v.length] = e.value;
        } else {
        	m[n] = $(e).val();
        }
    });
    return m;
}

_.prototype.mapToForm = function(map, formId) {
	_ckJQ();
	var f = jqObj(formId);
	$(":input", f).each(function(i, e) {
		var name = e.name;
		var t, ee, v, v2;
        if(name != "" && (v = map[name]) != undefined) {
            t = e.type;
    		v2 = !$.isArray(v) ? v :
    			(v.length == 0) ? "" : v[0];
            if(t == "text" || t == "hidden" || t == "textarea" || t == "password" || t == "range" ||
        			t == "number" || t == "email" || t == "tel") {
                e.value = v2;
            } else if(t == "radio") {
                e.checked = (v2 == e.value) ? true : false;
            } else if(t == "checkbox") {
            	e.checked = ($.inArray(e.value, v) != -1) ? true : false;
            } else if(t.indexOf("select") == 0) {
            	$(e).val(v);
            }
        }
    });
	return ta;
}

_.prototype.selectDraw = function(selectId, data) {
	var d = data, e = jqObj(selectId);
	if(typeof(d) == "string")
		try { d = eval("(" + d + ")"); } catch(er) { ta.clearMessage().showMessage(d); throw er; }
	var i, v, opt, rows = d["rows"], sels = d["selectedValues"];
	e.empty();
	for(i in rows) {
		opt = $("<option></option>").appendTo(e);
		opt.val(v = rows[i][0]);
		opt.html(rows[i][1]);
		if($.inArray(v, sels) != -1)
			opt.attr("selected", "selected");
	}
	return ta;
}

_.prototype.selectLabel = function(selectId) {
	var s = jqObj(selectId);
	return $(":selected", s).text();
}

_.prototype.selectLabels = function(selectId) {
	var v = [];
	var s = jqObj(selectId);
	$(":selected", s).each(function(i, e) {
		v[i] = $(e).text();
	});
	return v;
}

_.prototype.selectCopy = function(id1, id2) {
	_ckJQ();
	var s1 = jqObj(id1);
	var s2 = jqObj(id2);
	$("option:selected", s1).clone().appendTo(s2);
	return ta.val(s1);
}

_.prototype.selectCopyAll = function(id1, id2) {
	_ckJQ();
	var s1 = jqObj(id1);
	var s2 = jqObj(id2);
	$("option", s1).clone().appendTo(s2);
	return ta.val(s1);
}

_.prototype.selectMove = function(id1, id2) {
	_ckJQ();
	var s1 = jqObj(id1);
	var s2 = jqObj(id2);
	var v = ta.val(s1);
	$("option:selected", s1).appendTo(s2);
	return v;
}

_.prototype.selectMoveAll = function(id1, id2) {
	_ckJQ();
	var s1 = jqObj(id1);
	var s2 = jqObj(id2);
	var v = ta.val(s1);
	$("option", s1).appendTo(s2);
	return v;
}

_.prototype.selectMoveUp = function(id) {
	_ckJQ();
	var s = jqObj(id);
	var m = $("option:selected", s);
	var v = m.val();
	if(m.length != 1)
		return v;
	var i = s[0].selectedIndex;
	if(!i)
		return v;
	if(i == 0)
		return v;
	var p = $($("option", s)[i - 1]);
	p.before(m);
	return v;
}

_.prototype.selectMoveDown = function(id) {
	_ckJQ();
	var s = jqObj(id);
	var m = $("option:selected", s);
	var v = m.val();
	if(m.length != 1)
		return v;
	var o = $("option", s);
	var i = s[0].selectedIndex;
	if(!i && i != 0)
		return v;
	if(i == (o.length - 1))
		return v;
	var n = $(o[i + 1]);
	n.after(m);
	return v;
}

_.prototype.selectSelectAll = function(id) {
	_ckJQ();
	var s = jqObj(id);
	$("option", s).attr("selected", "selected");
	return ta;
}

_.prototype.selectRemove = function(id) {
	_ckJQ();
	var s = jqObj(id);
	var v = ta.val(s);
	$("option:selected", s).remove();
	return v;
}

_.prototype.selectRemoveAll = function(id, b) {
	_ckJQ();
	var tmp, sel = jqObj(id);
	var v = ta.val(id);
	b = !!b;
	if(!b) {
		sel.find("option").remove();
	} else {
		var first = ((tmp = sel.find("option:first")).length > 0 && tmp[0].value == "") ? tmp[0] : null;
		sel.find("option:not(:first)").remove();
		if(first != null)
			first.selected = true;
	}
	return v;
}

_.prototype.checkboxCheckAll = function(element) {
	_ckJQ();
	var e = element;
	if(typeof(e) == "string")
		e = eval(e);
	$(e).attr("checked", true);
	return ta;
}

_.prototype.checkboxUncheck = function(element) {
	_ckJQ();
	var e = element;
	if(typeof(e) == "string")
		e = eval(e);
	$(e).attr("checked", false);
	return ta;
}

_.prototype.checkboxIsChecked = function(id) {
	_ckJQ();
	var c = jqObj(id);
	return !!c.attr("checked");
}

_.prototype.radioIsChecked = function(id) {
	_ckJQ();
	var r = jqObj(id);
	return ta.checkboxIsChecked(r);
}

_.prototype.cascade = function(url, parentId, childId, args, onFinished) {
	_ckJQCascade();
	$(function() {
		var c = (typeof(childId) == "string") ? $("#" + childId) : ((childId.type.indexOf("select") == 0) ? $(childId) : childId);
		var p = (typeof(parentId) == "string") ? ("#" + parentId) : ((parentId.type.indexOf("select") == 0) ? parentId : parentId.get(0));
		var a = {
		    url: ajaxURI(url),
		    complete: function() {
	            if(typeof(onFinished) == "function")
	                onFinished();
	        }
	    };
		if(args != undefined)
			a.data = args;
		c.cascade(p, {
	        ajax: a,
	        template: function(i) {
	        	return "<option value='" + i.value + "'>" + i.label + "</option>";
	        },
	        match: function(v) {
	            return this.parent == v;
	        }
	    }).bind("loaded.cascade", function() {
	    	function forIE() {
				var e = $(this).find("option:first");
				if(e.length > 0 && e[0].value == "")
					e[0].selected = true;
	    	}
			window.setTimeout(forIE, 1000);
		});
    });
	return ta;
}

_.prototype.disableInput = function(element) {
	return ta.enableInput(element, false);
}

_.prototype.enableInput = function(element, isEnable) {
	_ckJQ();
	var e = (typeof(element) == "string") ? eval(element) : element;
	if(!e) throw new Error("element undefined");
	var b = (isEnable == undefined) ? true : !!isEnable;
	if(b)
		$(e).removeAttr("disabled");
	else
		$(e).attr("disabled", true);
	return ta;
}

_.prototype.readonlyInput = function(element, isReadonly) {
	_ckJQ();
	var e = (typeof(element) == "string") ? eval(element) : element;
	if(!e) throw new Error("element undefined");
	var b = (isReadonly == undefined) ? true : !!isReadonly;
	if(b)
		$(e).attr("readonly", "readonly");
	else
		$(e).removeAttr("readonly");
	return ta;
}

_.prototype.disable = function(id) {
	return ta.enable(id, false);
}

_.prototype.enable = function(id, isEnable) {
	_ckJQ();
	var e = jqObj(id);
	if(!e[0]) throw new Error("input element which id='" + id + "' is undefined");
	var b = (isEnable == undefined) ? true : !!isEnable;
	if(b)
		e.removeAttr("disabled");
	else
		e.attr("disabled", true);
	return ta;
}

_.prototype.readonly = function(id, isReadonly) {
	_ckJQ();
	var e = jqObj(id);
	if(!e[0]) throw new Error("input element which id='" + id + "' is undefined");
	var b = (isReadonly == undefined) ? true : !!isReadonly;
	if(b)
		e.attr("readonly", "readonly");
	else
		e.removeAttr("readonly");
}

_.prototype.tabs = function(containerId, options) {
	_ckJQTabs();
	var t = jqObj(containerId);
	$(function() {
		if(!options) {
			t.tabs({ ajaxOptions: { type: "POST" } });
		} else {
			var a = options.ajaxOptions;
			if(!!a && !a.type)
				a.type = "POST";
			t.tabs(options);
		}
	});
	return ta;
}

_.prototype.tabNo = function(containerId, pageIndex) {
	_ckJQTabs();
	var t = jqObj(containerId);
	t.tabs("select", pageIndex);
	return ta;
}

_.prototype.dialogOpen = function(id) {
	_ckJQDialog();
	var d = jqObj(id);
	d.dialog("open");
	ta.clearMessage();
	return ta;
}

_.prototype.dialogClose = function(id) {
	_ckJQDialog();
	var d = jqObj(id);
	d.dialog("close");
	ta.clearMessage();
	return ta;
}

_.prototype.dialogTitle = function(id, title) {
    _ckJQDialog();
    var d = jqObj(id);
    d.dialog("option", "title", title);
    ta.clearMessage();
    return ta;
}

_.prototype.pages = function(pageIdArray) {
	return ta.exclusiveItems(pageIdArray);
}

_.prototype.page = function(pageId, animateDirection) {
	var pp = _items;
	if(pp == null || pp.length == 0)
		throw new Error("Use ta.pages([ '" + pageId + "', ... ]) function to construct the pages container first!");
	return ta.exclusiveItem(pageId, animateDirection);
}

_.prototype.exclusiveItems = function(itemIdArray, groupId) {
	_ckJQ();
	var ar = itemIdArray, gid = groupId;
	if(ar instanceof Array);
	else
		throw new Error("input parameter is not an array of strings");
	if(ar.length == 0 && !groupId)
		return ta;
	if(!_items)
		_items = {};
	if(!gid) {
		for(var i = 0; i < 100; i++) {
			gid = "i" + new Date().getTime();
			if(!_items[gid])
				break;
		}
	} else if(_items[gid]) {
		throw new Error("items groupId=" + gid + " already existed, you should change another one");
	}
	_items[gid] = ar;
	if(ar.length != 0) {
		jqObj(ar[0]).show();
		for(var i = 1, ii = ar.length; i < ii; i++)
			jqObj(ar[i]).hide();
	}
	return ta;
}

_.prototype.exclusiveItem = function(itemId, groupId, animateDirection) {
	var pp = _items, id = itemId, gid = null, an = null;
	if(typeof(groupId) == "string")
	gid = (typeof(groupId) == "string") ? groupId :
		(typeof(animateDirection) == "string") ? animateDirection : null;
	an = (typeof(groupId) == "number") ? groupId :
		(typeof(animateDirection) == "number") ? animateDirection : null;

	var i, j, ar;
	if(pp == null || pp.length == 0)
		throw new Error("Use ta.exclusiveItems([ '" + itemId + "', ... ]) function to construct the items container first!");

	if(!id) {
		for(i in pp)
			exclusiveItem2(null, pp[i], an);
	} else {
		if(!gid) {
			for(i in pp) {
				if(exclusiveItem2(id, pp[i], an))
					break;
			}
		} else {
			if(!(ar = pp[gid]))
				throw new Error("specified exclusiveItems groupId=" + gid + " not existed");
			exclusiveItem2(id, ar, an);
		}
	}
	return ta;
}

var exclusiveItem2 = function(itemId, items, animation) {
	var i, e, d = document, id = itemId, ar = items, an = animation;
	var adir = (an == 1) ? "up" : (an == 2) ? "down" : (an == 3) ? "left" : (an == 4) ? "right" : null;

	if(!id) {
		for(i in ar) {
			e = d.getElementById(ar[i]);
			if(e && e.style.display != "none")
				e.style.display = "none";
		}
		return false;
	}

	var ii, m = null;
	for(i in ar) {
		if(id == ar[i]) {
			m = d.getElementById(id);
			break;
		}
	}

	var useJQUI = !!jQuery.ui;
	var hasShow = false;
	for(i = 0, ii = ar.length; i < ii; i++) {
		if((e = d.getElementById(ar[i])) && e.style.display != "none" && e != m) {
			if(!useJQUI || !adir) {
				e.style.display = "none";
				if(m)
					m.style.display = "block";
			} else {
				var o = { "direction": adir };
				$(e).hide("slide", o, 500, function() {
					$(m).show("slide", o, 500);
				});
			}
			return true;
		}
	}
	return false;
}

_.prototype.value = function(element, value) {
	var t, n = null, e = element;
	if(typeof(e) == "string") {
		n = e;
		e = eval(e);
	}
	if(!e)
		return (value == undefined) ? null : ta;
	_ckJQ();
	var o = $(e);
	if(!(t = e.type) && e.length != undefined)
		t = e[0].type;

	var v;
	if(typeof(value) == "undefined") {
		if(t == "radio") {
			v = e.length ? (o.filter(function() { return this.checked; }).val()) : (e.checked ? e.value : null);
			return (v == undefined) ? null : v;
		}
		if(t == "checkbox")
			return e.length ? (o.filter(function() { return this.checked; }).map(function() { return this.value; }).get() || []) : (e.checked ? e.value : []);
		if(t == "select-multiple")
			return o.map(function() { return $(this).val(); }).get();
		return o.val();
	}

	if(t == "radio" || t == "checkbox") {
		if(value != null)
			$(e.length ? e : [e]).val((value instanceof Array) ? value : [value]);
		else
			o.attr("checked", false);
	} else {
		if(t != undefined && t.indexOf('select-') == 0 && value == null)
			e.selectedIndex = -1;
		else
			o.val(value);
	}
	return ta;
}

_.prototype.val = function(id, value) {
	_ckJQ();
	var o = jqObj(id);
	if(typeof(value) == "undefined") {
		if(o.length == 0)
			return null;
		var v = o.val();
		if(v == null && o[0].type == "select-multiple")
			return [];
        return o.val();
	}
	o.val(value);
    return ta;
}

_.prototype.html = function(id, value) {
	_ckJQ();
	var e = jqObj(id);
	if(typeof(value) == "undefined")
		return e.html();
	e.html(value);
	return ta;
}

_.prototype.text = function(id, value) {
	_ckJQ();
	var e = jqObj(id);
	if(typeof(value) == "undefined")
		return e.text();
	e.text(value);
	return ta;
}

_.prototype.show = function(id, displayBlock) {
	_ckJQ();
	if(id instanceof Array) {
		for(var i in id)
			ta.show(id[i], displayBlock);
	} else {
		var e = jqObj(id);
		e.css("display", displayBlock ? "block" : "inline");
	}
	return ta;
}

_.prototype.hide = function(id) {
	_ckJQ();
	if(id instanceof Array) {
		for(var i in id)
			ta.hide(id[i]);
	} else {
		var e = jqObj(id);
		e.css("display", "none");
	}
	return ta;
}

_.prototype.isHidden = function(id) {
	_ckJQ();
	var o = jqObj(id);
	var e = o[0];
	if(!e || e.style.display != "none")
		return false;
	return true;
}

_.prototype.focus = function(idOrElement) {
	_ckJQ();
	jqObj(idOrElement).focus();
	return ta;
}

_.prototype.empty = function(id) {
	_ckJQ();
	jqObj(id).empty();
	return ta;
}

_.prototype.css = function(id, properties) {
	_ckJQ();
	var e = jqObj(id);
	if(typeof(properties) == "string")
		return e.css(properties);
	e.css(properties);
	return ta;
}

_.prototype.width = function(id, width) {
	_ckJQ();
	var e = jqObj(id);
	if(width == undefined)
		return e.width();
	e.width(width);
	return ta;
}

_.prototype.height = function(id, height) {
	var e = jqObj(id);
	if(height == undefined)
		return e.height();
	e.height(height);
	return ta;
}

_.prototype.eventFire = function(e, eventName) {
	_ckJQ();
	jqObj(e).trigger(eventName);
	return ta;
}

_.prototype.eventHandler = function(idOrElement, eventName, callback) {
	_ckJQ();
	var cb;
	if($.isFunction(callback)) {
		cb = function(event) {
			var e;
			if(event)
				e = event.originalEvent;
			return callback(e);
		};
	}
	jqObj(idOrElement).on(eventName, cb);
	return ta;
}

_.prototype.includePage = function(placeHolderId, url, args, onComplete) {
	_ckJQ();
	var e = jqObj(placeHolderId);
	var p = (typeof(args) == "string") ? ta.formToMap(args) : args;
	e.load(url, p, onComplete);
	return ta;
}

_.prototype.evalJSON = function(jsonObj) {
	_ckJQJSON();
	return $.evalJSON(jsonObj);
}

_.prototype.toJSON = function(map) {
	_ckJQJSON();
	return $.toJSON(map);
}

_.prototype.contains = function(objs, v) {
	if(!objs)
		return false;
	if(typeof(objs) == "string" && typeof(v) == "string")
		return objs.indexOf(v) != -1;
	if($.isArray(objs))
		return $.inArray(v, objs) != -1;
	return objs[v] != undefined;
}

_.prototype.fileUpload = function(url, formId, onSuccessful, onError) {
	return _fileUpload(url, formId, onSuccessful, onError, false);
}

_.prototype.fileUploadForJSON = function(url, formId, onSuccessful, onError) {
	return _fileUpload(url, formId, onSuccessful, onError, true);
}

var _fileUpload = function(url, formId, onSuccessful, onError, isJSON) {
	_ckAjaxFileUpload();
	ta.clearMessage();
	var args = {
		url: ajaxURI(url),
		formId: formId,
		data: ta.formToMap(formId),
		error: !onError ? undefined : onError,
		success: function(res) {
			if(onSuccessful) {
				if(isJSON) {
					var res2;
					try { res2 = ta.evalJSON(res); } catch(e) { ta.showMessage(res); }
					if(res2)
						onSuccessful(res2, res);
				} else {
					onSuccessful(res);
				}
			}
		}
	};
	$.ajaxFileUpload2(args);
	return ta;
}

var _in = ta.internal = function() {};

_in.initContextPath = function(cp) {
	if(!cp)
		throw new Error("context-path argument not given!");
	_contextPath = cp;
	return _in;
}

_in.initRequestURI = function(requestURI) {
	_requestURI = requestURI;
	return _in;
}

_in.selectInit = function(id, settings) {
	$(function() {
		_ckJQ();
		var sel = jqObj(id);
		if(sel.data("settings"))
			return;

		var s = {
			emptyOptionLabel: " -- 請選\uFFFD\uFFFD\uFFFD -- ",
			dataSourceURL: null,
			cascadeTo: null,
			cascadeFromURL: null,
			triggerChangeOnRendered: false,
			cascadeFunc: null
		};
		var i;
		for(i in settings)
			s[i] = settings[i];

		sel.data("settings", s);

		if(s.dataSourceURL) {
			ta.post(s.dataSourceURL, {}, function(ret) {
				ta.selectDraw(id, ret);
				if(s.triggerChangeOnRendered)
					sel.change();
			});
		}
		if(s.cascadeFromURL) {
			s.cascadeFunc = function(parentId, parentName) {
				var pv = ta.val(parentId);
				if(!pv || pv == "") {
					sel.empty().append("<option value=''>" + s.emptyOptionLabel + "</option>");
					if(s.triggerChangeOnRendered && s.cascadeTo)
						sel.change();
					return;
				}
				var param = {};
				param[parentName] = pv;
				ta.post(s.cascadeFromURL, param, function(ret) {
					ta.selectDraw(id, ret);
					if(s.triggerChangeOnRendered)
						sel.change();
				});
			};
		}
	});
}

_in.dialogInit = function(id, settings) {
	$(function() {
		_ckJQDialog();

		var s = {
			width: 400,
			minHeight: 150,
			title: null,
			autoOpen: false,
			modal: true,
			buttons: [],
			open: null,
			zIndex: 15000,

			_onOpen: null,
			_cleanFormOnOpen: false,
			_cleanFormReadOnlyFieldOnOpen: false,
			_cleanFormHiddenFieldOnOpen: true
		};
		var i;
		for(i in settings)
			s[i] = settings[i];
		var d = jqObj(id);

		if(s._onOpen || s._cleanFormOnOpen) {
			s.open = function(event, dia) {
				if(s._cleanFormOnOpen)
					ta.internal.dialogFormClean(id, s._cleanFormHiddenFieldOnOpen, s._cleanFormReadOnlyFieldOnOpen);
				if(s._onOpen)
					(function(_dialog) { s._onOpen(); })(dia);
			}
		}

		d.dialog(s);
		$(".ui-button-text", d.parent(".ui-dialog")).each(function() {
			var e = $(this);
			if(e.text() == "")
				e.text(e.parent().attr("text"));
		});
	});
}

_in.listSliderInit = function(id) {
	$(function() {
		_ckJQ();
		listSliderInitialize(id);
		$(window).on("resize", function() {
			listSliderInitialize(id);
		});
	});
}

var listSliderInitialize = function(id) {
	var b = $("#" + id);
	var ulc = $(".listBarContainer", b);
	ulc.outerWidth(50);

	var w = ulc.parent().innerWidth();
	ulc.outerWidth(w);

	var ul = $("ul", ulc);
	var li = $("li", ul);
	var n = li.size();
	if(n == 0)
		return;

	var idx = 0;
	li.each(function(i, e) {
		var e1 = $(e);
		var w1 = e1.position().left;
		if(w1 > w || (w1 + e1.outerWidth() > w))
			return false;
		idx = i;
	});

	var li2 = $(li[idx]);
	var w2 = li2.position().left + li2.outerWidth();
	if((idx + 1) < n) {
		w = w2;
		ulc.outerWidth(w);
	}

	var pages = Math.ceil(n / (idx + 1));
	var cookieKey = KEY_LISTSLIDE_PAGENO + id;
	var page = !$.cookie ? 1 : (parseInt($.cookie(cookieKey)) || 1);
	if(page > pages)
		page = pages;
	!$.cookie || $.cookie(cookieKey, page);
	if(page != 1) {
		ul.css("left", -$(li[(idx + 1) * (page - 1)]).position().left);
	} else if(pages == 1) {
		ul.css("left", $(li[0]).position().left);
		return;
	}

	$(".btnNext", b).unbind("click").click(function() {
		if(page != pages) {
			ul.animate({ left: -$(li[(idx + 1) * page]).position().left });
		    page++;
		    !$.cookie || $.cookie(cookieKey, page);
		}
	});
	$(".btnPrev", b).unbind("click").click(function() {
    	if(page != 1) {
    		page--;
    		ul.animate({ left: -$(li[(idx + 1) * (page - 1)]).position().left });
    		!$.cookie || $.cookie(cookieKey, page);
    	}
	});
}

_in.dialogFormClean = function(dialogId, cleanHidden, cleanReadOnly) {
	var d = jqObj(dialogId);
	$("form", d).each(function(i, e) {
		ta.formClean(e.id, cleanHidden, cleanReadOnly);
	});
	return _in;
}


var gridSelRow = function(gridId) {
	var g = jqObj(gridId);
	var i, n = 0, rid = null, ids = g.data("selectedRowids");
	for(i in ids) {
		if(ids[i]) {
			rid = i;
			n++;
		}
	}
	if(n > 1)
		rid = null;
	return [ n, rid ];
}


/* ----------------------------------------------------------------------------- */
})();
