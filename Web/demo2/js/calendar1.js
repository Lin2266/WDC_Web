var sMon = new Array(12);
        sMon[0] = "Jan"
        sMon[1] = "Feb"
        sMon[2] = "Mar"
        sMon[3] = "Apr"
        sMon[4] = "May"
        sMon[5] = "Jun"
        sMon[6] = "Jul"
        sMon[7] = "Aug"
        sMon[8] = "Sep"
        sMon[9] = "Oct"
        sMon[10] = "Nov"
        sMon[11] = "Dec"

function calendar(y, m, d) {
        var sPath = contextPath + "/calendar.html";
        strFeatures = "dialogWidth=168px;dialogHeight=200px;help=no;status=no;resizable=yes;scroll=no;";
        sDate = showModalDialog(sPath,"",strFeatures);
//      strFeatures = "width=180px;height=200px;help=no;status=no;resizable=yes;scroll=no;";
//      sDate = window.open(sPath,"",strFeatures);

        if(formatYear(sDate, 0) != "")  y.value = formatYear(sDate, 0);
        if(formatMonth(sDate, 0) != "") m.value = formatMonth(sDate, 0);
        if(formatDay(sDate, 0) != "")   d.value = formatDay(sDate, 0);
}

function checkDate(t) {
        dDate = new Date(t.value);
        if (dDate == "NaN") {t.value = ""; return;}

        iYear = dDate.getFullYear()

        if ((iYear > 1899)&&(iYear < 1950)) {

                sYear = "" + iYear + ""
                if (t.value.indexOf(sYear,1) == -1) {
                        iYear += 100
                        sDate = (dDate.getMonth() + 1) + "/" + dDate.getDate() + "/" + iYear
                        dDate = new Date(sDate)
                }
        }
        t.value = formatDate(dDate);
}

function formatDate(sDate) {
        var sScrap = "";
        var dScrap = new Date(sDate);
        if (dScrap == "NaN") return sScrap;

        iDay = dScrap.getDate();
        iMon = dScrap.getMonth();
        iYea = dScrap.getFullYear();

        sScrap = iYea + "-" + (iMon + 1) + "-" + iDay ;
        return sScrap;
}

function formatYear(DateY) {
        var yScrap = new Date(DateY);
        if (yScrap == "NaN") return "";

        iYea2 = yScrap.getFullYear() - 1911;
        return iYea2;
}

function formatMonth(DateM) {
        var mScrap = new Date(DateM);
        if (mScrap == "NaN") return "";

        iMon2 = mScrap.getMonth() + 1;
        return iMon2;
}

function formatDay(DateD) {
        var aScrap = new Date(DateD);
        if (aScrap == "NaN") return "";

        iDay2 = aScrap.getDate();
        return iDay2;
}
