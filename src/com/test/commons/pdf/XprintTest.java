package com.test.commons.pdf;

import java.io.File;

import junit.framework.TestCase;

public class XprintTest extends TestCase {
	@Override
	protected void setUp() {
		//final String rptDirPath = "/opt/ap/docflow/web/rpt";
		//final String rptDirPath = "/home/raymond/tmp";
		final String rptDirPath = "/opt/ap/tbid/web/rpt";
        final String outputDirPath = "/tmp";
        final String fontName = "/usr/share/fonts/cjkuni-uming/uming.ttc,0";
        //final String fontName = "/usr/local/share/fonts/windows/mingliu.ttc,0";
        final String kaiFontName = "/usr/share/fonts/cjkuni-ukai/ukai.ttc,0";
        final String fontName2 = "/home/raymond/文件/font/TW-NIA-Sung-Plane2.ttf";
        final String fontName15 = "/home/raymond/文件/font/TW-NIA-Sung-PlaneF.ttf";
        final String imgDir = "/opt/ap/tbid/web/images";

        //一次性設定
        Xprint.setStaticallyDefaultFont(fontName);
        Xprint.setStaticallyDefaultKaiFont(kaiFontName);
        Xprint.setStaticallyPlane2Font(fontName2);
        Xprint.setStaticallyPlane15Font(fontName15);
        Xprint.setStaticallyRptDir(rptDirPath);
        Xprint.setStaticallyOutputDir(outputDirPath);
        Xprint.setStaticallyImgDir(imgDir);
	}

	/**
	 * 測使用格式檔
	 */
	public void xtestXprint() {
		final String rptFormatFileName = "dfwbabuy01.txt";
        final String outputFileName = "a.pdf";
    
        System.out.println("memory start: " + getMemoryUse1() + " bytes");
        
        //new Xprint 物件
        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
        Xprint xp = new Xprint(rptFormatFileName, outputFileName);
        xp.setDebug(true);
        //xp.setMaxPagesPerFile(60000);
        xp.assignWatermark("watermark1.png", 35, 45); //浮水印
        xp.assignPerforationSeal("seal1.png", Xprint.PERFORATION_SEAL_BOTH_SIDE_VERTICALLY);
        xp.setPageNumberPosition(Xprint.PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE);
        
        //起新頁
        xp.newPage(); //第一頁 ---------------------------------------------------
        xp.add("預算推算人員", "預算推算人員A", 0, 0);
        xp.add("簽証號碼：", "X123456", 0, 0);
        xp.add("用途別", "測試", 0, 0);
        xp.add("計畫", "Foo", 0, 0);
        xp.add("課室", "BAR課室", 0, 0);
        xp.add("款", "0", 0, 0);
        xp.add("項", "0", 0, 0);
        xp.add("目", "0", 0, 0);
        xp.add("節", "0", 0, 0);
        xp.add("事由", "測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test ", 0, 0);
        xp.add("主任秘書", "主任秘書A", 0, 0);
        xp.add("區長", "李XX", 0, 0);
        xp.add("請購人", "請購人A", 0, 0);
        xp.add("承辦人1", "承辦人1", 0, 0);
        xp.add("承辦人2", "承辦人2", 0, 0);
        xp.add("單位主管(會辦)2", "單位主管(會辦)2", 0, 0);
        xp.add("審核", "審核XYZ", 0, 0);
        xp.add("單位主管(審核)", "單位主管(審核)", 0, 0);
        xp.add("專員", "專員X", 0, 0);
        xp.add("單位主管(會辦)1", "單位主管(會辦)1", 0, 0);
        xp.add("單位主管(請購)", "單位主管(請購)", 0, 0);
        xp.add("年", "X", 0, 0);
        xp.add("月", "X", 0, 0);
        xp.add("日", "X", 0, 0);
        xp.add("十億", "X", 0, 0);
        xp.add("億", "X", 0, 0);
        xp.add("千萬", "X", 0, 0);
        xp.add("百萬", "X", 0, 0);
        xp.add("十萬", "X", 0, 0);
        xp.add("萬", "X", 0, 0);
        xp.add("千", "X", 0, 0);
        xp.add("百", "X", 0, 0);
        xp.add("十", "X", 0, 0);
        xp.add("元", "X", 0, 0);
        
        xp.add("名稱", "名稱XXX", 0, 30);
        xp.add("物品規格及詳細用途", "物品規格及詳細用途zzz", 0, 30);
        xp.add("單位", "單位Z", 0, 30);
        xp.add("數量", "數量X", 0, 30);
        xp.add("單價", "單價X", 0, 30);
        xp.add("金額", "金額X", 0, 30);
        xp.add("備註", "備註ABC備註ABC備註ABC備註ABC備註ABC", 0, 30);
        
        xp.add("名稱", "名稱YYY", 0, 0);
        xp.add("物品規格及詳細用途", "物品規格及詳細用途KKK", 0, 0);
        xp.add("單位", "單位Z2", 0, 0);
        xp.add("數量", "數量X2", 0, 0);
        xp.add("單價", "單價X2", 0, 0);
        xp.add("金額", "金額X2", 0, 0);
        xp.add("備註", "備註XYZ備註XYZ備註XYZ備註XYZ備註XYZ", 0, 0);
        
        xp.addBarCode("條碼", "0225984299AP13F", -10, 0);
        
        xp.newPage(); //第二頁 ---------------------------------------------------
        xp.add("預算推算人員", "預算推算人員B", 0, 0);
        xp.add("簽証號碼：", "Y54321", 0, 0);
        xp.add("用途別", "測試2", 0, 0);
        xp.add("計畫", "Spam", 0, 0);
        xp.add("課室", "Eggs課室", 0, 0);
        xp.add("款", "0", 0, 0);
        xp.add("項", "0", 0, 0);
        xp.add("目", "0", 0, 0);
        xp.add("節", "0", 0, 0);
        xp.add("事由", "測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test ABCDEFGabcdefg", 0, 0);
        xp.add("主任秘書", "主任秘書B", 0, 0);
        xp.add("區長", "張XX", 0, 0);
        xp.add("請購人", "請購人B", 0, 0);
        xp.add("承辦人1", "承辦人I", 0, 0);
        xp.add("承辦人2", "承辦人II", 0, 0);
        xp.add("單位主管(會辦)2", "單位主管(會辦)3", 0, 0);
        xp.add("審核", "審核ABC", 0, 0);
        xp.add("單位主管(審核)", "單位主管(審核)", 0, 0);
        xp.add("專員", "專員X", 0, 0);
        xp.add("單位主管(會辦)1", "單位主管(會辦)I", 0, 0);
        xp.add("單位主管(請購)", "單位主管(請購)", 0, 0);
        xp.add("年", "X", 0, 0);
        xp.add("月", "X", 0, 0);
        xp.add("日", "X", 0, 0);
        xp.add("十億", "X", 0, 0);
        xp.add("億", "X", 0, 0);
        xp.add("千萬", "X", 0, 0);
        xp.add("百萬", "X", 0, 0);
        xp.add("十萬", "X", 0, 0);
        xp.add("萬", "X", 0, 0);
        xp.add("千", "X", 0, 0);
        xp.add("百", "X", 0, 0);
        xp.add("十", "X", 0, 0);
        xp.add("元", "X", 0, 0);
        
        xp.add("名稱", "名稱XXX", 0, 30);
        xp.add("物品規格及詳細用途", "物品規格及詳細用途zzz", 0, 30);
        xp.add("單位", "單位A", 0, 30);
        xp.add("數量", "數量Z", 0, 30);
        xp.add("單價", "單價Z", 0, 30);
        xp.add("金額", "金額Z", 0, 30);
        xp.add("備註", "備註ABC備註ABC備註ABC備註ABC備註ABC甲乙丙丁", 0, 30);
        
        xp.add("名稱", "名稱ZZZ", 0, 0);
        xp.add("物品規格及詳細用途", "物品規格及詳細用途KKKAAAQQQ", 0, 0);
        xp.add("單位", "單位Z2", 0, 0);
        xp.add("數量", "數量X2", 0, 0);
        xp.add("單價", "單價X2", 0, 0);
        xp.add("金額", "金額X2", 0, 0);
        xp.add("備註", "備註XYZ備註XYZ備註XYZ備註XYZ備註XYZ子丑寅卯", 0, 0);
        
        xp.addLine(Xprint.LINE_STYLE_DASH, 3, 50, 50, 150, 150);
        xp.addLine(Xprint.LINE_STYLE_DASHDOT, 3, 150, 150, 50, 150);
        xp.addLine(Xprint.LINE_STYLE_DASHDOTDOT, 3, 50, 50, 50, 150);
        
        for(int i = 0; i < 5; i++) {
        	//if((i % 2000) == 0)
        		System.out.println("memory at loop=" + i + ": " + getMemoryUse() + " bytes");
        	
	        xp.newPage(); //第三頁 ---------------------------------------------------
	        xp.add("預算推算人員", "預算推算人員C", 0, 0);
	        xp.add("簽証號碼：", "Q09876", 0, 0);
	        xp.add("用途別", "測試3", 0, 0);
	        xp.add("計畫", "PyPy", 0, 0);
	        xp.add("課室", "RuRu課室", 0, 0);
	        xp.add("款", "0", 0, 0);
	        xp.add("項", "0", 0, 0);
	        xp.add("目", "0", 0, 0);
	        xp.add("節", "0", 0, 0);
	        xp.add("事由", "測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test 測試test ABCDEFGabcdefg", 0, 0);
	        xp.add("主任秘書", "主任秘書B", 0, 0);
	        xp.add("區長", "張XX", 0, 0);
	        xp.add("請購人", "請購人C", 0, 0);
	        xp.add("承辦人1", "承辦人a", 0, 0);
	        xp.add("承辦人2", "承辦人b", 0, 0);
	        xp.add("單位主管(會辦)2", "單位主管(會辦)III", 0, 0);
	        xp.add("審核", "審核ABC", 0, 0);
	        xp.add("單位主管(審核)", "單位主管(審核)", 0, 0);
	        xp.add("專員", "專員X", 0, 0);
	        xp.add("單位主管(會辦)1", "單位主管(會辦)a", 0, 0);
	        xp.add("單位主管(請購)", "單位主管(請購)a", 0, 0);
	        xp.add("年", "X", 0, 0);
	        xp.add("月", "X", 0, 0);
	        xp.add("日", "X", 0, 0);
	        xp.add("十億", "X", 0, 0);
	        xp.add("億", "X", 0, 0);
	        xp.add("千萬", "X", 0, 0);
	        xp.add("百萬", "X", 0, 0);
	        xp.add("十萬", "X", 0, 0);
	        xp.add("萬", "X", 0, 0);
	        xp.add("千", "X", 0, 0);
	        xp.add("百", "X", 0, 0);
	        xp.add("十", "X", 0, 0);
	        xp.add("元", "X", 0, 0);
	        
	        xp.add("名稱", "名稱XXX", 0, 30);
	        xp.add("物品規格及詳細用途", "物品規格及詳細用途zzzaaa", 0, 30);
	        xp.add("單位", "單位A", 0, 30);
	        xp.add("數量", "數量Z", 0, 30);
	        xp.add("單價", "單價Z", 0, 30);
	        xp.add("金額", "金額Z", 0, 30);
	        xp.add("備註", "備註ABC備註ABC備註ABC備註ABC備註ABC甲乙丙丁戊己", 0, 30);
	        
	        xp.add("名稱", "名稱ZZZ", 0, 0);
	        xp.add("物品規格及詳細用途", "物品規格及詳細用途KKKAAAQQQiii", 0, 0);
	        xp.add("單位", "單位Z3", 0, 0);
	        xp.add("數量", "數量X3", 0, 0);
	        xp.add("單價", "單價X3", 0, 0);
	        xp.add("金額", "金額X3", 0, 0);
	        xp.add("備註", "備註XYZ備註XYZ備註XYZ備註XYZ備註XYZ子丑寅卯申西戌亥", 0, 0);
        }
        //
        //使用 Sun JDK 1.6 (未調整 max memory 參數)
        //以本例印出 50000+ 頁的 PDF 檔, 檔案大小約 154Mb
        //耗用記憶空間最大較執行前多出約 21.3Mb - 147.9Mb (視 gc 情況而定, 不代表在線上環境非得耗用到 100Mb 的量)
        //
        
        //釋放資源
        xp.close();
        
        //檢查頁數
        //assertEquals(3, xp.getPageCount());
        
        System.out.println("memory end: " + getMemoryUse1() + " bytes");
        
        //列出輸出檔名
        for(File f : xp.getOutputFiles())
        	System.out.println("output: " + f.getAbsolutePath());
    }
	
	/**
	 * 測使用格式檔
	 */
	public void xtestXprint1() {
		Xprint xp = null;
		
		try {
			final String rptFormatFileName = "advance_request_form.big5.txt";
	        final String outputFileName = "a.pdf";
	    
	        //new Xprint 物件
	        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
	        xp = new Xprint(rptFormatFileName, outputFileName);
	        xp.setDebug(true);
	        
	        xp.newPage();
	        xp.add("applicantOrgName1", "大同股份有限公司"); //申請單位(全名)
			xp.add("applicantOrgName2", "大同股份有限公司");
			xp.add("handlerName1", "方大同"); //借款人->經辦業務
			xp.add("handlerName2", "林小異");
			xp.add("handlerName3", "林小異");
			xp.add("handlerName4", "林小異");
			xp.add("handlerTel1", "12345678");
			xp.add("handlerTel2", "12345678");
			xp.add("predictReturnDate1", "107-11-09"); //預計歸還日
			xp.add("predictReturnDate2", "107-11-09");
			xp.add("serialNo1", "APPLY12345"); //申請號
			xp.add("serialNo2", "ISSUE123");
			xp.add("proprietorOrgName1", "大同公司"); //押標金抬頭(原名: 投標單位)
			xp.add("proprietorOrgName2", "勞公保險局");
	        
			xp.add("bidbond1", "123");
			xp.add("bidbond2", "321");
			xp.add("bidbond1萬", "柒");
			xp.add("bidbond2萬", "壹佰");
			xp.add("bidbond1千", "零");
			xp.add("bidbond2千", "零");
			xp.add("bidbond1百", "零");
			xp.add("bidbond2百", "零");
			xp.add("bidbond1十", "零");
			xp.add("bidbond2十", "零");
			xp.add("bidbond1元", "零");
			xp.add("bidbond2元", "零");
			xp.add("bidbond1角", "零");
			xp.add("bidbond2角", "零");
	        
			xp.add("issueDesc1", "XXX 申請 XXXX");
			xp.add("issueDesc2", "XXX 申請 XXXX");
			
			xp.add("requestDate1", "107-12-31");
			xp.add("requestDate2", "107-12-31");
			
	        xp.close();
	        xp = null;
		} finally {
			if(xp != null)
				xp.close();
		}
	}
	
	/**
	 * 測使用格式檔
	 */
	public void xtestXprint2() {
		Xprint xp = null;
		
		try {
			final String rptFormatFileName = "guarantee_form.big5.txt";
			final String rptFormatFileName1 = "guarantee_form_1.big5.txt";
	        final String outputFileName = "b.pdf";
	    
	        //new Xprint 物件
	        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
	        xp =  new Xprint(outputFileName);
	        xp.setDebug(true);
	        
	        xp.newPage(rptFormatFileName);
	        xp.add("serialNo", "SERIAL001"); //申請號
			xp.add("applicantOrg", "大同股份有限公司"); //申請單位(全名)
			xp.add("burdenCenterNo", "ABC123"); //成本中心代號
			xp.add("proprietorName", "勞工保險局"); //業主名稱
			xp.add("issueName", "大案子"); //案名
			xp.add("bidAcceptDate", "108-01-01"); //得標日期
			xp.add("bidPrice", "123,456,789"); //合約金額
			xp.add("netIncomeRatio", "10%"); //淨利率
			xp.add("guaranteeRatio", "5%"); //保證成數
			xp.add("bidbond", "200,000"); //保證金額
			xp.add("guaranteePeriod", "107-11-01"); //保證期間
			xp.add("requestDate", "107-11-20"); //需求日期
			xp.add("prepayment", "50,000"); //預付款
			xp.add("performanceBond", "200,000"); //履約
			xp.add("warrantyMargin", "100,00"); //保固
			xp.add("trustee", "              銀行辦理。"); //受妥託者(無者, 留手寫的空間)
			xp.add("applicationForm", "1"); //申請書份數
			xp.add("corporateSealBig", "1"); //大章
			xp.add("corporateSealSmall", "1"); //小章
			xp.add("otherPaymentDesc", "200,000");
			xp.add("guaranteeType0", "■");
			xp.add("guaranteeType1", "■");
			xp.add("guaranteeType2", "■");
			xp.add("guaranteeType3", "■");
			xp.add("guaranteeType4", "■");
			xp.add("guaranteeType5", "■");
			xp.add("guaranteeTypeOther", "其他");
			xp.add("guaranteeDepositReturn0", "■");
			xp.add("guaranteeDepositReturn1", "■");
			xp.add("guaranteeDepositReturnDate", "107/12/01");
			xp.add("performanceBondReturn0", "■");
			xp.add("performanceBondReturn1", "■");
			xp.add("performanceBondReturnDate", "107/12/01");
			xp.add("headerTitle1", "V");
			xp.add("headerTitle2", "V");
			xp.add("applyType0", "■");
			xp.add("applyType1", "■");
			xp.add("applyType2", "■");
			xp.add("applyType3", "■");
			xp.add("applyType4", "■");
			xp.add("applyType5", "■");
			
			
	        xp.newPage(rptFormatFileName1);
	        xp.add("serialNo", "SERIAL001"); //申請號
			xp.add("applicantOrg", "大同股份有限公司"); //申請單位(全名)
			xp.add("burdenCenterNo", "ABC123"); //成本中心代號
			xp.add("proprietorName", "勞工保險局"); //業主名稱
			xp.add("issueName", "大案子"); //案名
			xp.add("bidAcceptDate", "108-01-01"); //得標日期
			xp.add("bidPrice", "123,456,789"); //合約金額
			xp.add("netIncomeRatio", "10%"); //淨利率
			xp.add("guaranteeRatio", "5%"); //保證成數
			xp.add("bidbond", "200,000"); //保證金額
			xp.add("guaranteePeriod", "107-11-01"); //保證期間
			xp.add("requestDate", "107-11-20"); //需求日期
			xp.add("prepayment", "50,000"); //預付款
			xp.add("performanceBond", "200,000"); //履約
			xp.add("warrantyMargin", "100,00"); //保固
			xp.add("trustee", "              銀行辦理。"); //受妥託者(無者, 留手寫的空間)
			xp.add("applicationForm", "1"); //申請書份數
			xp.add("corporateSealBig", "1"); //大章
			xp.add("corporateSealSmall", "1"); //小章
			xp.add("otherPaymentDesc", "200,000");
			xp.add("guaranteeType0", "■");
			xp.add("guaranteeType1", "■");
			xp.add("guaranteeType2", "■");
			xp.add("guaranteeType3", "■");
			xp.add("guaranteeType4", "■");
			xp.add("guaranteeType5", "■");
			xp.add("guaranteeTypeOther", "其他");
			xp.add("guaranteeDepositReturn0", "■");
			xp.add("guaranteeDepositReturn1", "■");
			xp.add("guaranteeDepositReturnDate", "107/12/01");
			xp.add("performanceBondReturn0", "■");
			xp.add("performanceBondReturn1", "■");
			xp.add("performanceBondReturnDate", "107/12/01");
			xp.add("headerTitle1", "V");
			xp.add("headerTitle2", "V");
			xp.add("applyType0", "■");
			xp.add("applyType1", "■");
			xp.add("applyType2", "■");
			xp.add("applyType3", "■");
			xp.add("applyType4", "■");
			xp.add("applyType5", "■");
	        
	        xp.close();
	        xp = null;
		} finally {
			if(xp != null)
				xp.close();
		}
	}
	
	/**
	 * 測使用格式檔
	 */
	public void xtestXprint3() {
		Xprint xp = null;
		
		try {
			final String rptFormatFileName = "draft_seal_1.big5.txt";
	        final String outputFileName = "c.pdf";
	    
	        //new Xprint 物件
	        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
	        xp = new Xprint(rptFormatFileName, outputFileName);
	        xp.setDebug(true);
	        
	        xp.newPage();
	        xp.addImage("tatung.jpg", 18, 13, 45, 10); //LOGO
	        xp.add("applicantOrgFullname", "大同股份有限公司系統事業群系統事業部資通訊系統處東區業務部業務課 承辦"); //單位名稱
			xp.add("bidOpenDay", "104/12/23"); //開標日期
			xp.add("bidOpenTime", "00:00"); //開標時間
			xp.add("handlerName", "林大同"); //經辦
			xp.add("handlerTel", "12345678"); //經辦電話
			xp.add("issueName", "臺東縣政府電腦機房環境監控系統軟硬體維護"); //採購案名
			xp.add("proprietorName", "臺東縣政府"); //投標機關
			//xp.add("tenderDay", ""); //投標日期
			//xp.add("tenderTime", ""); //投標時間
			xp.add("borrowDay", " ～ ");
			xp.add("subjType0", "■");
			xp.add("subjType1", "■");
			xp.add("subjType2", "■");
			xp.add("subjType3", "■");
			xp.add("subjTypeDesc", "啦啦啦");
			xp.add("draftSealUsage0", "■");
			xp.add("draftSealUsage1", "■");
			xp.add("draftSealUsage2", "■");
			xp.add("draftSealUsage3", "■");
			xp.add("draftSealUsage4", "■");
			xp.add("draftSealUsage5", "■");
			xp.add("draftSealUsage6", "■");
			xp.add("draftSealUsage7", "■");
			xp.add("draftSealUsageDesc", "ZZZ");
			xp.add("bidPrice", "預算金額: $123,456,000");
			xp.add("netIncomeRatio", "預估毛利: 預估毛利/實際毛利: 10%");
	        
	        xp.close();
	        xp = null;
		} finally {
			if(xp != null)
				xp.close();
		}
	}
	
	/**
	 * 測使用格式檔
	 */
	public void xtestXprint4() {
		Xprint xp = null;
		
		try {
			final String rptFormatFileName = "draft_seal_2.big5.txt";
	        final String outputFileName = "d.pdf";
	    
	        //new Xprint 物件
	        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
	        xp = new Xprint(rptFormatFileName, outputFileName);
	        xp.setDebug(true);
	        
	        xp.newPage();
	        xp.addImage("tatung.jpg", 18, 13, 45, 10); //LOGO
	        xp.add("bidPrice", "$868,500"); //合約金額
			xp.add("chairmanSubject", "擬 □□(臺東縣政府)臺東縣政府電腦機房環境監控系統軟硬體維護 -□□□書，需蓋公司□□章，恭請 董事長核示用印。"); //主旨
			xp.add("chairmanSealDesc", "擬 □□(臺東縣政府)臺東縣政府電腦機房環境監控系統軟硬體維護 -□□□書，需蓋公司□□章，恭請 董事長核示用印。"); //說明
			xp.add("requestDate", "105/01/11"); //需求日
			xp.add("bidPrice", "$868,500"); //合約金額
			xp.add("netIncomeRatio", "33.0%"); //毛利率
			xp.add("chairmanSealBigQty", ""); //大章?處
			xp.add("chairmanSealSmallQty", ""); //小章?處
			xp.add("handlerName", "吳建成"); //經辦
			xp.add("handlerTel", "089-323978"); //經辦電話
	        
	        xp.close();
	        xp = null;
		} finally {
			if(xp != null)
				xp.close();
		}
	}
	
	/**
	 * 測使用格式檔
	 */
	public void testXprint5() {
		Xprint xp = null;
		
		try {
			final String rptFormatFileName = "advance_request_form_tsti.big5.txt";
	        final String outputFileName = "e.pdf";
	    
	        //new Xprint 物件
	        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
	        xp = new Xprint(rptFormatFileName, outputFileName);
	        xp.setDebug(true);
	        
	        xp.newPage();
	        xp.addImage("tsti1.jpg", 5, 8, 62, 8); //LOGO

	        xp.add("applicantName", "王大迷", 0D, 0D); //申請人 (可能不關聯至 tbid_user.id_ 而為登錄者自行輸入)
			xp.add("applicantSAPNo", "SAP123", 0D, 0D); //申請人 SAP員工編號
			xp.add("bidPrice", "123", 0D, 0D); //合約金額(預算金額, 標案金額)
			xp.add("burdenCenterNo", "12345678", 0D, 0D); //成本中心代號 (可能不關聯至 tbid_group.id_ 而為登錄者自行輸入)
			xp.add("funnelNo", "FUN321", 0D, 0D); //Funnel 案件編號
			xp.add("guaranteeRatio", "3%", 0D, 0D); //保證成數
			xp.add("handlerTel", "123321", 0D, 0D); //經辦業務電話 (可能不關聯至 tbid_user.id_ 而為登錄者自行輸入)
			xp.add("issueName", "ZZZ", 0D, 0D); //案名/用途說明
			xp.add("proprietorOrgName", "大同世界科技", 0D, 0D); //押標金抬頭
			xp.add("serialNo", "1234567890", 0D, 0D); //申請號
			xp.add("applyType0", "■", 0D, 0D);
			xp.add("applyType1", "■", 0D, 0D);
			xp.add("applyType2", "■", 0D, 0D);
			xp.add("applyType3", "■", 0D, 0D);
			xp.add("applyType4", "■", 0D, 0D);
			xp.add("applyType5", "■", 0D, 0D);
			xp.add("applyType6", "■", 0D, 0D);
			xp.add("applyType7", "■", 0D, 0D);
			xp.add("applyTypeOther", "測測", 0D, 0D);
			xp.add("bidbond", "              萬      仟      佰      拾      元整  (NT$               )", 0D, 0D);
			xp.add("createDateY", "107", 0D, 0D);
			xp.add("createDateM", "12", 0D, 0D);
			xp.add("createDateD", "09", 0D, 0D);
			xp.add("guaranteeType0", "■", 0D, 0D);
			xp.add("guaranteeType1", "■", 0D, 0D);
			xp.add("guaranteeType2", "■", 0D, 0D);
			xp.add("guaranteeType3", "■", 0D, 0D);
			xp.add("guaranteeType4", "■", 0D, 0D);
			xp.add("guaranteeType5", "■", 0D, 0D);
			xp.add("guaranteeType6", "■", 0D, 0D);
			xp.add("guaranteeType7", "■", 0D, 0D);
			xp.add("guaranteeType8", "■", 0D, 0D);
			xp.add("guaranteeTypeOther", "other", 0D, 0D);
			xp.add("attachDocType0", "■", 0D, 0D);
			xp.add("attachDocType1", "■", 0D, 0D);
			xp.add("attachDocType2", "■", 0D, 0D);
			xp.add("attachDocType3", "■", 0D, 0D);
			xp.add("attachDocType4", "■", 0D, 0D);
			xp.add("attachDocType5", "■", 0D, 0D);
			xp.add("attachDocTypeOther", "附附", 0D, 0D);
			xp.add("bpmNo", "(BPM編號：A2-     -   -     )");
			xp.add("predictReturnDateY", "108", 0D, 0D);
			xp.add("predictReturnDateM", "01", 0D, 0D);
			xp.add("predictReturnDateD", "01", 0D, 0D);
			xp.add("requestDateY", "108", 0D, 0D);
			xp.add("requestDateM", "01", 0D, 0D);
			xp.add("requestDateD", "01", 0D, 0D);
			xp.add("requestDateT", "00:00", 0D, 0D);
	        
	        xp.close();
	        xp = null;
		} finally {
			if(xp != null)
				xp.close();
		}
	}
	
	/**
	 * 測不使用格式檔, 畫單一長文字行
	 */
	public void xtestXprint7() {
		final String outputFileName = "b.pdf";
		
		Xprint xp = new Xprint(outputFileName);
        xp.setDebug(true);
        xp.assignWatermark("watermark1.png", 35, 45); //浮水印
        
        StringBuilder line = new StringBuilder();
        for(int i = 0; i < 10; i++) //長文字行
        	line.append("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ一二三四五六七八九十壹貳參肆伍陸柒捌玖拾")
        		.append("天地玄黃宇宙洪荒日月盈昃辰宿列張寒來暑往秋收冬藏閏餘成歲律召調陽test雲騰致雨露結為霜金生麗水玉出崑崗")
        		.append("劍號巨闕珠稱夜光果珍李柰菜重芥薑海鹹河淡鱗潛羽翔龍師火帝12345鳥官人皇始製文字乃服衣裳推位讓國有虞陶唐")
        		.append("吊民伐罪周發殷湯坐朝問道垂拱平章zzz愛育黎首臣伏戎羌遐邇壹體率賓歸王鳴鳳在樹白駒食場化被草木賴及萬方");
        
        //起新頁
        xp.newPage();
        
        //當前頁畫線
        xp.addLine(Xprint.LINE_STYLE_DASHDOTDOT, 2, 10, 10, 150, 150);
        xp.addLine(2, 10, 150, 150, 10);
        xp.addLine(Xprint.LINE_STYLE_DASH, 2D, 10D, 75D, 150D, 75D);
        
        double y = xp.addFlow(line.toString(), 5, 40, 100, 100, 5, 0);
        
        
        //釋放資源
        xp.close();
        
        //列出輸出檔名
        for(File f : xp.getOutputFiles())
        	System.out.println("output: " + f.getAbsolutePath());
	}
	
	private static long getMemoryUse() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (totalMemory - freeMemory);
    }
	
	private static long getMemoryUse1() {
        garbageCollect();
        garbageCollect();
        long totalMemory = Runtime.getRuntime().totalMemory();
        garbageCollect();
        garbageCollect();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (totalMemory - freeMemory);
    }
	
	public void xtestXprint5() {
		final String rptFormatFileName = "test.txt";
		//final String rptFormatFileName = "guarantee_form.big5.txt";
        final String outputFileName = "a.pdf";
        
        //Xprint xp = new Xprint(rptFormatFileName, outputFileName, "MS950");
        Xprint xp = new Xprint(rptFormatFileName, outputFileName);
        xp.setDebug(true);
        xp.setMaxPagesPerFile(1000); //optional: 限定每個 PDF 最大頁數
        xp.close();
        
        for(File f : xp.getOutputFiles())
        	System.out.println("output: " + f.getAbsolutePath());
	}
	
	private static void garbageCollect() {
        try {
            System.gc();
            Thread.sleep(100);
            System.runFinalization();
            Thread.sleep(100);
            System.gc();
            Thread.sleep(100);
            System.runFinalization();
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
	
	public static void main(String[] args) {
		XprintTest test = new XprintTest();
		test.setUp();
		//test.testXprint();
	}
}
