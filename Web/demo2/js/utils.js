(function(ta) {

/////////////////////////////////////////////////////////////////////////////////
//private fields and functions
/////////////////////////////////////////////////////////////////////////////////
var _YYMMDDHHMISS_TW; //(-)YYYMMDD hh:mm:ss.nnn
var _YYYYMMDDHHMISS_AD; //YYYYMMDD hh:mm:ss.nnn
var _YYMMDDHHMISS; //(-)(Y)YYY-MM-DD hh:mm:ss.nnn
var _YYMMDD_TW; //(-)YYYMMDD
var _YYYYMMDD_AD; //YYYYMMDD
var _YYMMDD; //(-)(Y)YY-MM-DD
//var _YYMM_TW; //(-)YYYMM
//var _YYMM; //(-)(Y)YYY-MM or (-)(Y)YYY/MM
var ymdhmsPatternTW = function() { return (_YYMMDDHHMISS_TW != null) ? _YYMMDDHHMISS_TW : (_YYMMDDHHMISS_TW = /(-?\d{3})(\d{2})(\d{2}) (\d{2}):(\d{2}):(\d{2})(\.\d+)?/); }
var ymdhmsPatternAD = function() { return (_YYYYMMDDHHMISS_AD != null) ? _YYYYMMDDHHMISS_AD : (_YYYYMMDDHHMISS_AD = /(\d{4})(\d{2})(\d{2}) (\d{2}):(\d{2}):(\d{2})(\.\d+)?/); }
var ymdhmsPattern = function() { return (_YYMMDDHHMISS != null) ? _YYMMDDHHMISS : (_YYMMDDHHMISS = /(-?\d{1,4})[-\/](\d{1,2})[-\/](\d{1,2}) (\d{1,2}):(\d{2}):(\d{2})(\.\d+)?/); }
var ymdPatternTW = function() { return (_YYMMDD_TW != null) ? _YYMMDD_TW : (_YYMMDD_TW = /(-?\d{3})(\d{2})(\d{2})/); }
var ymdPatternAD = function() { return (_YYYYMMDD_AD != null) ? _YYYYMMDD_AD : (_YYYYMMDD_AD = /(\d{4})(\d{2})(\d{2})/); }
var ymdPattern = function() { return (_YYMMDD != null) ? _YYMMDD : (_YYMMDD = /(-?\d{1,4})[-\/](\d{1,2})[-\/](\d{1,2})/); }
//var ymPatternTW = function() { return (_YYMM_TW != null) ? _YYMM_TW : (_YYMM_TW = /(-?\d{3})(\d{2})/); }
//var ymPattern = function() { return (_YYMM != null) ? _YYMM : (_YYMM = /(-?\d{1,4})[-\/](\d{1,2})/); }

//年月日或年月日時分秒字串(民國年)轉為字串陣列 (長度為 3 或 6)
//輸入格式: '(-)YYYMMDD hh:mm:ss' or '(-)YYY-MM-DD hh:mm:ss' or '(-)YYY/MM/DD hh:mm:ss' or '(-)YYYMMDD' or '(-)YYY-MM-DD' or '(-)YYY/MM/DD'
var sTwDateToArray = function(d) {
	var m;
	if((m = d.match(ymdhmsPatternTW())) != null || //YYYMMDD hh:mm:ss.nnn
			(m = d.match(ymdhmsPattern())) != null) { //YYY-MM-DD hh:mm:ss.nnn
		return [ m[1], m[2], m[3], m[4], m[5], m[6] ];
	} else if((m = d.match(ymdPatternTW())) != null || //YYYMMDD
			(m = d.match(ymdPattern())) != null) { //YYY-MM-DD
		return [ m[1], m[2], m[3] ];
	}
	return null;
}

//西元年月日或年月日時分秒字串(限西元元年後)轉為字串陣列 (長度為 3 或 6)
//輸入格式: 'YYYYMMDD hh:mm:ss' or 'YYYY-MM-DD hh:mm:ss' or 'YYY/MM/DD hh:mm:ss' or 'YYYMMDD' or 'YYY-MM-DD' or 'YYY/MM/DD'
var sAdDateToArray = function(d) {
	var m;
	if((m = d.match(ymdhmsPatternAD())) != null || //YYYYMMDD hh:mm:ss.nnn
			(m = d.match(ymdhmsPattern())) != null) { //YYYY-MM-DD hh:mm:ss.nnn
		return [ m[1], m[2], m[3], m[4], m[5], m[6] ];
	} else if((m = d.match(ymdPatternAD())) != null || //YYYYMMDD
			(m = d.match(ymdPattern())) != null) { //YYYY-MM-DD
		return [ m[1], m[2], m[3] ];
	}
	return null;
}

//年月(民國年)轉為字串陣列 (長度為 2)
//輸入格式: '(-)YYYMM' or '(-)YYY-MM' or '(-)YYY/MM'
//var yearMonthStringToArray = function(d) {
//	var m;
//	if((m = d.match(ymPatternTW())) != null || //YYYMM
//			(m = d.match(ymPattern())) != null) { //YYY-MM
//		return [ m[1], m[2] ];
//	}
//	return null;
//}

/////////////////////////////////////////////////////////////////////////////////
//public functions
/////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////
//about date
/////////////////////////////////////////////////////////////////////////////////

/**
 * 民國年換算為西元年
 * @param y (number 或 string) 民國年
 * @return (number) 西元年
 */
ta.twToADYear = function(y) {
	var yy = (typeof(y) == "number") ? y : ta.toNumber(y);
	return (yy < 0) ? (++yy + 1911) : (yy + 1911); //民國元年=西元1912年, 民國前一年=西元1911年
}

/**
 * 西元年換算為民國年
 * @param y (number 或 string) 西元年
 * @return (number) 民國年
 */
ta.adToTWYear = function(y) {
	var yy = ((typeof(y) == "number") ? y : ta.toNumber(y)) - 1911;
	return (yy < 1) ? --yy : yy; //民國元年=西元1912年, 民國前一年=西元1911年
}

/**
 * 取當前日期(西年).
 * @return (string)格式: 'YYYYMMDD'
 */
ta.currentDateCompact = function() {
  var now = new Date();
  return ta.alignRight(now.getFullYear(), 4, '0') + ta.alignRight(now.getMonth() + 1, 2, '0') + ta.alignRight(now.getDate(), 2, '0');
}

/**
 * 取當前日期(西年).
 * @param sep (string, optional)分隔符號, 預設 "/"
 * @return (string)格式: 'YYYY/MM/DD'
 */
ta.currentDate = function(sep) {
	sep || (sep = "/");
	var now = new Date();
	return ta.alignRight(now.getFullYear(), 4, '0') + sep + ta.alignRight(now.getMonth() + 1, 2, '0') + sep + ta.alignRight(now.getDate(), 2, '0');
}

/**
 * 取當前時刻.
 * @return (string)格式: 'hh:mm:ss'
 */
ta.currentTime = function() {
	var now = new Date();
	return ta.alignRight(now.getHours(), 2, '0') + ":" + ta.alignRight(now.getMinutes(), 2, '0') + ":" + ta.alignRight(now.getSeconds(), 2, '0');
}

/**
 * 取當前日期時刻(西年).
 * @return (string)格式: 'YYYYMMDD hh:mm:ss'
 */
ta.currentDateTimeCompact = function() {
	return ta.currentDateCompact() + " " + ta.currentTime();
}

/**
 * 取當前日期時刻(西元年).
 * @return (string)格式: 'YYYY/MM/DD hh:mm:ss'
 */
ta.currentDateTime = function() {
	return ta.currentDate() + " " + ta.currentTime();
}

/**
 * 把日期字串(民國年)轉成 JavaScript Date 物件.
 * @param d (string) 格式: '(-)YYYMMDD hh:mm:ss' or '(-)YYY-MM-DD hh:mm:ss' or '(-)YYY/MM/DD hh:mm:ss' or '(-)YYYMMDD' or '(-)YYY-MM-DD' or '(-)YYY/MM/DD'
 * @return Date 物件
 */
ta.twDateToDateObj = function(d) {
	var dd = sTwDateToArray(d);
	if(dd == null)
		return null;
	var yy = ta.twToADYear(ta.toNumber(dd[0]));
	if(dd.length == 3)
		return new Date(yy, ta.toNumber(dd[1]) - 1, ta.toNumber(dd[2]), 0, 0, 0);
	return new Date(yy, ta.toNumber(dd[1]) - 1, ta.toNumber(dd[2]), ta.toNumber(dd[3]), ta.toNumber(dd[4]), ta.toNumber(dd[5]));
}

/**
 * 把年月日字串(民國年)加上時分成為 "YYY/MM/DD hh:mm:ss" 格式的字串.
 * @param dateString (string) 'YYYMMDD'
 * @param hour (number 或 string) 時
 * @param minute (number 或 string) 分
 * @param sep (string, optional)分隔符號, 預設 "/"
 * @return (string)格式: "YYY/MM/DD hh:mm:ss"
 */
ta.toTWDateTime = function(dateString, hour, minute, sep) {
	var dd = sTwDateToArray(dateString);
	if(dd == null)
		return null;
	
	var t = [hour, minute];
	for(var i in t) {
		t[i] = (t[i] == null) ? "00" : 
			((typeof(t[i]) != "number") ? ta.alignRight(t[i], 2, "0") :
			((t[i] < 0) ? "00" :
			((t[i] < 10) ? ("0" + t[i]) : String(t[i]))));
	}
	
	sep || (sep = "/");
	if(ta.startsWith(dd[0], "-"))
		dd[0] = "-" + ta.alignRight(dd[0].substring(1), 3, "0");
	return dd[0] + sep + ta.alignRight(dd[1], 2, "0") + sep + ta.alignRight(dd[2], 2, "0") + " " +
		ta.alignRight(t[0], 2, "0") + ":" + ta.alignRight(t[1], 2, "0") + ":" + "00";
}

/**
 * 檢查日期(年月日或年月日時分秒, 民國年)字串的格式及值是否合法.
 * @param dateString (string) 格式: '(-)YYYMMDD hh:mm:ss' or '(-)YYY-MM-DD hh:mm:ss' or '(-)YYY/MM/DD hh:mm:ss' or '(-)YYYMMDD' or '(-)YYY-MM-DD' or '(-)YYY/MM/DD'
 * @return true/false
 */
ta.isTWDateValid = function(dateString) {
	try {
		var dd = sTwDateToArray(dateString);
		if(dd == null)
			return false;
	} catch(e) {
		return false;
	}

	//月
	var m = ta.toNumber(dd[1]);
	if(m < 1 || m > 12)
      return false;

	//西元年
	var y = ta.twToADYear(ta.toNumber(dd[0]));

	//日
	var d = ta.toNumber(dd[2]);
	if(d < 1 || d > 31)
		return false;
	if(m == 4 || m == 6 || m == 9 || m == 11) {
		if(d > 30)
			return false;
	} else if(m == 2) {
		if(ta.isLeapYear(y)) { //閏年
			if(d > 29)
				return false;
		} else {
			if(d > 28)
				return false;
		}
	}

	if(dd.length == 6) { //含時分秒
		if(ta.toNumber(dd[3]) > 23)
			return false; //hh
		if(ta.toNumber(dd[4]) > 59)
			return false; //mm
		if(ta.toNumber(dd[5]) > 59)
			return false; //ss
	}
	return true;
}

/**
 * 檢查年月格式(民國年)及值是否合法.
 * @param yearMonth (string) 年月, 格式: (-)YYYMM
 * @retrun (boolean)
 */
ta.isTWYearMonthValid = function(yearMonth) {
	var len = yearMonth.length;
	if(len == 6 && !ta.startsWith(yearMonth, "-"))
		return false;
	else if(len < 5 || len > 6)
		return false;
	if(ta.toNumber(yearMonth.slice(-2)) > 12)
		return false;
	return true;
}

/**
 * 兩日(民國年)之差 (後日減前日)
 * @param previousDate (string) 格式 '(-)YYYMMDD hh:mm:ss' or '(-)YYY-MM-DD hh:mm:ss' or '(-)YYY/MM/DD hh:mm:ss' or '(-)YYYMMDD' or '(-)YYY-MM-DD' or '(-)YYY/MM/DD'
 * @param laterDate (string) 格式 '(-)YYYMMDD hh:mm:ss' or '(-)YYY-MM-DD hh:mm:ss' or '(-)YYY/MM/DD hh:mm:ss' or '(-)YYYMMDD' or '(-)YYY-MM-DD' or '(-)YYY/MM/DD'
 * @return 單位:日 (float)
 */
ta.twDateDiff = function(previousDate, laterDate) {
	var d1 = ta.twDateToDateObj(previousDate).getTime();
	var d2;
	if(laterDate != undefined)
		d2 = ta.twDateToDateObj(laterDate).getTime();
	else
		d2 = new Date().getTime();
	return Math.floor((d2 - d1) / (1000 * 60 * 60 * 24));
}

/**
 * 民國日期字串轉為西元年日期字串.
 * @param twDate (string) 格式:'(-)YYYMMDD hh:mm:ss' or '(-)YYY-MM-DD hh:mm:ss' or '(-)YYY/MM/DD hh:mm:ss' or '(-)YYYMMDD' or '(-)YYY-MM-DD' or '(-)YYY/MM/DD'
 * @param sep (string, optional)分隔符號, 預設 "/"
 * @return (string) 格式:'YYYY/MM/DD' or 'YYYY/MM/DD hh:mm:ss'
 */
ta.twToADDate = function(twDate, sep) {
	var dd = sTwDateToArray(twDate);
	if(dd == null)
		return null;

	sep || (sep = "/");
	var d = ta.alignRight(ta.twToADYear(dd[0]), 4, "0") + sep + ta.alignRight(dd[1], 2, "0") + sep + ta.alignRight(dd[2], 2, "0");
	if(dd.length == 6)
		d += " " + dd[3] + ":" + dd[4] + ":" + dd[5];
	return d;
}

/**
 * 西元年日期(限西元元年後)字串轉成民國年日期字串.
 * @param adDate (string) 格式: 'YYYYMMDD hh:mm:ss' or 'YYYY-MM-DD hh:mm:ss' or 'YYYY/MM/DD hh:mm:ss' or 'YYYYMMDD' or 'YYYY-MM-DD' or 'YYYY/MM/DD'
 * @param sep (string, optional)分隔符號, 預設 "/"
 * @return (string) 格式:'(-)YYY/MM/DD' or '(-)YYY/MM/DD hh:mm:ss'
 */
ta.adToTWDate = function(adDate, sep) {
	var dd = sAdDateToArray(adDate);
	if(dd == null)
		return null;

	if(sep == null)
		sep = "/"
			
	var y = ta.adToTWYear(dd[0]);
	y = (y < 0) ? ("-" + ta.alignRight(-y, 3, "0")) : ta.alignRight(y, 3, "0");
	
	var d = y + sep + ta.alignRight(dd[1], 2, "0") + sep + ta.alignRight(dd[2], 2, "0");
	if(dd.length == 6)
		d += " " + dd[3] + ":" + dd[4] + ":" + dd[5];
	return d;
}

/**
 * 是否閏年(西元年).
 * @param year (string or number) 西元年
 * return (boolean)
 */
ta.isLeapYear = function(year) {
	var y = (typeof(year) == "number") ? year : ta.toNumber(year);
	return ((y % 4) == 0 && !(((y % 100) == 0 && (y % 400) != 0)));
}

/**
 * 求指定年月的當月最多日數.
 * @param year (string or number) 西元年
 * @param month (string or number) 月
 * @return (number)
 */
ta.getMaxDay = function(year, month) {
	if(typeof(month) == "string")
		month = parseInt(month);
	if(month < 1 || month > 12) {
		ta.showMessage("月份 month 不合法: " + month);
		return null;
	}
	var d = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
	if(ta.isLeapYear(year) && month == 2)
		return 29;
	return d[month - 1];
}

/////////////////////////////////////////////////////////////////////////////////
//about string
/////////////////////////////////////////////////////////////////////////////////

/**
 * 判斷傳入字串是否為空(null、undefined、空字串)
 * @param s (string)
 * @return (string)
 */
ta.isEmpty = function(s) {
	return (s == null || ta.trim(s.toString()) == "");
}


/**
 * 把字串左右之空白除去(含TAB, UTF-8 全形 white space)
 * @param s (string)
 * @return (string)
 */
ta.trim = function(s) {
	return (s == null) ? s : s.trim(); //IE 9+
}

/**
 * 把字串左側空白除去後傳回(含TAB, UTF-8 全形 white space)
 * @param s (string)
 * @return (string)
 */
ta.trimLeft = function(s) {
	if(s == null)
		return;
	var p = /[　\s]*([^　\s].*)/;
	var m = s.match(p);
	if(m == null)
		return "";
	return m[1];
}

/**
 * 把字串右側空白除去後傳回(含TAB, UTF-8 全形 white space)
 * @param s (string)
 * @return (string)
 */
ta.trimRight = function(s) {
	if(s == null)
		return s;
	var p = /(.*[^　\s])[　\s]*/;
	var m = s.match(p);
	if(m == null)
		return "";
	return m[1];
}

/**
 * 把字串(由數字組成)之左側的 '0' 字元除去.
 * @param s (string) 由數字組成的字串
 * @return (string)
 */
ta.trimLeftZero = function(s) {
	if(s == null)
		return s;
	var p = /0*(.*)/;
	var m = s.match(p);
	return m[1];
}

/**
 * 把指定字元(或字串)重複數次組成字串.
 * @param s (string)
 * @param n (number)
 * @retrun (string)
 */
ta.repeat = function(s, n) {
	if(s == null || n == null || n < 1)
		return "";

	var s2 = (typeof(s) != "string") ? String(s) : s;
	if(n == 1)
		return s2;
	
	if(typeof(s2.repeat) == "function") //ES6
		return s2.repeat(n);
	
	//from 陳皓(左耳朵耗子) (new Array(n + 1).join(s) 慢)
	var n2 = n;
	var ret = "";
	while(true) {
		if(n2 & 1)
			ret += s2;
		n2 >>= 1;
		if(n2)
			s2 += s2;
		else
			break;
	}
	return ret;
}

/**
 * 把字串按指定寬度左靠, 右側補上指定字元.
 * @param s (string)
 * @param len (number)欲傳回的字串寬度
 * @param c (string)填補用的單一字元
 * @return (string)
 */
ta.alignLeft = function(s, len, c) {
	if(s == null)
		s = "";
	else if(typeof(s) != "string")
		s = String(s);
	if(s.length >= len)
		return s.substring(0, len);
	return s + ta.repeat(c, len - s.length);
}

/**
 * 把字串按指定寬度右靠, 左側補上指定字元.
 * @param s (string)
 * @param len (number)欲傳回的字串寬度
 * @param c (string)填補用的單一字元
 * @return (string)
 */
ta.alignRight = function(s, len, c) {
	if(s == null)
		s = "";
	else if(typeof(s) != "string")
		s = String(s);
	if(s.length >= len)
		return s.substring(0, len);
	var f = ta.repeat(c, len - s.length);
	return (f + s);
}

/**
 * 檢查字串 str 的開頭是否為 s.
 * @param str (string)
 * @param s (string)
 * @return true/false
 */
ta.startsWith = function(str, s) {
	if(str == null || s == null || str.length < s.length)
		return false;
	return (str.substring(0, s.length) == s);
}

/**
 * 檢查字串 str 的結尾是否為 s.
 * depended by ajaxURI().
 * @param str (string)
 * @param s (string)
 * @return true/false
 */
ta.endsWith = function(str, s) {
	if(str == null || s == null || str.length < s.length)
		return false;
	return (str.slice(-s.length) == s);
}

/**
 * 判斷字串否全由 multi-byte 字元組成.
 * @param s (string)
 * @return (boolean)
 */
ta.chkWideChar = function(s){
	if(s == null)
		return false;
	for(var i = 0; i < s.length; i++) {
		if(s.charCodeAt(i) <= 127)
			return false;
	}
	return true;
}

/**
 * 對輸入字串中的特定字元, 在其前插入 escape 字元.
 * @param s (string) 待修改的字串
 * @param charToEscape (string)需被 escape 的特定單一字元
 * @param escapeChar (string, optional, default='\')
 * @return (string)
 */
ta.escape = function(s, charToEscape, escapeChar) {
	if(s == null)
		return null;
	var c = charToEscape;
	var i = s.indexOf(c);
	if(i == -1) //原字串傳回
		return s;

	var e = escapeChar;
	if(e == undefined)
		e = "\\";

	var ss = "";
	if(i >= 0) { //前段先處理掉
		ss += s.substring(0, i) + e + c;
		i++;
	}

	var len = s.length;
	for(var n = 0; i < len && (n = s.indexOf(c, i)) != -1; i = n + 1)
		ss += s.substring(i, n) + e + c;
	if(i < len)
		ss += s.substring(i);
	return ss;
}

/////////////////////////////////////////////////////////////////////////////////
//about number
/////////////////////////////////////////////////////////////////////////////////

/**
 * 把字串形態的數字轉為數字形態, 不計字串開頭的 "0" 字元.
 * @param s (string or number)
 * @return (number)
 */
ta.toNumber = function(s) {
	if(s == null)
		return s;
	if(typeof(s) == "number")
		return s;
	if(typeof(s) != "string")
		return Number(s); //try try
	if(s.length == 0)
		return 0;
	var h = s.charAt(0);
	s = ta.trimLeftZero((h == "+" || h == "-") ? s.substring(1) : s);
	if(h == "-")
		s = "-" + s;
	if(s == "" || s == "-")
		return 0;
	return parseFloat(s);
}

/**
 * 判斷輸入字串是否由半形數字字元(含正負號)組成.
 * @param s
 * @return (boolean)
 */
ta.isNumber = function(s) {
	if(s == null)
		return false;
	if(typeof(s) == "number")
		return true;
	var p = /[-\+]?[0123456789\.]+/;
	return (s == s.match(p));
}

//if the input string are all full-width arabic characters
/**
 * 判斷輸入字串是否由 UTF-8 全形字元(含半形全形正負號)組成
 * @param s
 * @return (boolean)
 */
ta.isUtf8FullWidthNumber = function(s) {
	if(typeof(s) != "string")
		return false;
	var p = /[－＋]?[０１２３４５６７８９]+/;
	return (s == s.match(p));
}

/**
 * 判斷輸入字串是否由 UTF-8 中文大寫數字(含正負號文字)組成
 * @param s
 * @return (boolean)
 */
ta.isUtf8CNumber = function(s) {
	if(typeof(s) != "string")
		return false;
	var p = /[正負]?[零一二三四五六七八九十百千萬億兆]+/;
	return (s == s.match(p));
}

/**
 * 將數字每三位加一逗號
 */
ta.numberGrouping = function(n) {
	if(n == null)
		return n;
	if(typeof(n) == "number")
		n = n.toString();
	var i, ii;
	var f = ((i = n.indexOf(".")) == -1) ? null : n.substring(i); //小數點後的部分
	var d = (i == -1) ? n : n.substring(0, i); //整數部分

	if(d.length < 3)
		return n;

	var s = d.charAt(0);
	if(s == "+" || s == "-")
		d = d.substring(1);
	else
		s = null;

	var n2 = !f ? "" : f;
	for(i = ii = d.length; i > 0; i -= 3) {
		if(i != ii)
			n2 = "," + n2;
		n2 = d.substring((i < 3) ? 0 : (i - 3), i) + n2;
	}
	return !s ? n2 : (s + n2);
}

})(ta);
