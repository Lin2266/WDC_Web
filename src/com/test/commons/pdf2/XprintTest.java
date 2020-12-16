package com.test.commons.pdf2;

import java.io.File;

import junit.framework.TestCase;

public class XprintTest extends TestCase {
	private XprintBuilderBean builder;
	
	@Override
	protected void setUp() {
		this.builder = new XprintBuilderBean();
		this.builder.setDefaultOutputBaseDir("/tmp"); //輸出基準目錄
		this.builder.setDefaultImageFileBaseDir("/opt/ap/tbid/web/images"); //圖檔目錄
		this.builder.setDefaultFormatFileBaseDir("/opt/ap/tbid/web/rpt"); //格式檔目錄

		this.builder.setDefaultFormatFileEncoding("MS950"); //預設格式檔編碼
		this.builder.setPrintPageNumberOnFirstPage(true); //如果要印頁碼時, 首頁是否印頁出頁碼?
		this.builder.setDefaultFontName("/usr/share/fonts/cjkuni-uming/uming.ttc,0");
		this.builder.setDefaultKaiFontName("/usr/share/fonts/cjkuni-ukai/ukai.ttc,0");
		this.builder.setPlane2FontName("/home/raymond/文件/font/TW-NIA-Sung-Plane2.ttf"); //Unicode 第 2 字面明體 TrueType 字型
		this.builder.setPlane15FontName("/home/raymond/文件/font/TW-NIA-Sung-PlaneF.ttf"); //Unicode 第 2 字面楷體 TrueType 字型
	}
	
	/**
	 * 使用格式檔.
	 */
	public void xtestXprint() {
		System.out.println("testXprint():");
		try (Xprint xp = this.builder.build("advance_request_form.big5.txt", "bbb.pdf")) {
			xp.setDebug(true);
			xp.setPageNumberPosition(Xprint.PAGE_NUMBER_POSITION_BOTTOM_CENTER);
			xp.newPage();
			
			xp.add("applicantOrgName1", "測試單位"); //申請單位(全名)
			xp.add("applicantOrgName2", "測試單位2");
			xp.add("handlerName1", "經辦業務1"); //借款人->經辦業務
			xp.add("handlerName2", "經辦業務2");
			xp.add("handlerName3", "經辦業務3");
			xp.add("handlerName4", "經辦業務4");
			xp.add("handlerTel1", "經辦業務電話1");
			xp.add("handlerTel2", "經辦業務電話2");
			xp.add("predictReturnDate1", "107年5月15日"); //預計歸還日
			xp.add("predictReturnDate2", "107年5月16日");
			xp.add("serialNo1", "AAA001"); //申請號
			xp.add("serialNo2", "BBB002");
			xp.add("proprietorOrgName1", "勞保局"); //押標金抬頭(原名: 投標單位)
			xp.add("proprietorOrgName2", "社會局");

			String bidbond = "100,000"; //保證金額
			String[] bidbonds = { "壹", "貳", "參", "肆", "伍" };
			xp.add("bidbond1", bidbond);
			xp.add("bidbond2", bidbond);
			xp.add("bidbond1萬", (bidbonds[0] == null) ? "零" : bidbonds[0]);
			xp.add("bidbond2萬", (bidbonds[0] == null) ? "零" : bidbonds[0]);
			xp.add("bidbond1千", (bidbonds[1] == null) ? "零" : bidbonds[1]);
			xp.add("bidbond2千", (bidbonds[1] == null) ? "零" : bidbonds[1]);
			xp.add("bidbond1百", (bidbonds[2] == null) ? "零" : bidbonds[2]);
			xp.add("bidbond2百", (bidbonds[2] == null) ? "零" : bidbonds[2]);
			xp.add("bidbond1十", (bidbonds[3] == null) ? "零" : bidbonds[3]);
			xp.add("bidbond2十", (bidbonds[3] == null) ? "零" : bidbonds[3]);
			xp.add("bidbond1元", (bidbonds[4] == null) ? "零" : bidbonds[4]);
			xp.add("bidbond2元", (bidbonds[4] == null) ? "零" : bidbonds[4]);
			xp.add("bidbond1角", "零");
			xp.add("bidbond2角", "零");
			
			String issueDesc = "案案案 申請 測報表";
			xp.add("issueDesc1", issueDesc);
			xp.add("issueDesc2", issueDesc);
			
			String requestDate = "107年5月14日"; //需求日期
			xp.add("requestDate1", requestDate);
			xp.add("requestDate2", requestDate);
			
			for(final File f : xp.getOutputFiles())
				System.out.println("  => " + f.getAbsolutePath());
		}
	}

	/**
	 * 測不使用格式檔, 畫單一長文字行
	 */
	public void xtestDrawTextFlow() {
		System.out.println("testDrawTextFlow():");
		try (Xprint xp = this.builder.build("aaa.pdf")) { //自動釋放資源
	        xp.setDebug(true);
	        
	        StringBuilder line = new StringBuilder();
	        for(int i = 0; i < 10; i++) //長文字行
	        	line.append("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ一二三四五六七八九十壹貳參肆伍陸柒捌玖拾")
	        		.append("天地玄黃宇宙洪荒日月盈昃辰宿列張寒來暑往秋收冬藏閏餘成歲律召調陽test雲騰致雨露結為霜金生麗水玉出崑崗")
	        		.append("劍號巨闕珠稱夜光果珍李柰菜重芥薑海鹹河淡鱗潛羽翔龍師火帝12345鳥官人皇始製文字乃服衣裳推位讓國有虞陶唐")
	        		.append("吊民伐罪周發殷湯坐朝問道垂拱平章zzz愛育黎首臣伏戎羌遐邇壹體率賓歸王鳴鳳在樹白駒食場化被草木賴及萬方");
	        
	        //起新頁
	        xp.newPage();
	        
	        //當前頁畫線
	        xp.drawLine(Xprint.LINE_STYLE_DASHDOTDOT, 2, 10, 10, 150, 150);
	        xp.drawLine(2, 10, 150, 150, 10);
	        xp.drawLine(Xprint.LINE_STYLE_DASH, 2D, 10D, 75D, 150D, 75D);
	        
	        xp.drawFlow(line.toString(), 5, 40, 100, 100, 5, 0);
	        
	        //列出輸出檔名
	        for(File f : xp.getOutputFiles())
	        	System.out.println(" => " + f.getAbsolutePath());
		}
	}
	
	public void testDrawTextBox() {
		System.out.println("testDrawTextBox():");
		final String s = "一1二2三3四4五5六6七7八8九9十0壹1貳2參3肆4伍5陸6柒7仈8玖9拾0";
		final String s2 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ一二三四五六七八九十壹貳參肆伍陸柒捌玖拾" +
				"天地玄黃宇宙洪荒日月盈昃辰宿列張寒來暑往秋收冬藏閏餘成歲律召調陽test雲騰致雨露結為a霜金生麗水玉出崑z崗123";
		
		try (Xprint xp = this.builder.build("ccc.pdf")) { //自動釋放資源
			xp.setDebug(true);
			
			xp.drawTextBox(s, 5, 10, 10, 100, 20, Xprint.FONT_MING);
			xp.drawTextBox(s, 5, 110, 30, 100, 20, Xprint.FONT_MING);
			xp.drawTextBox(s2, 5, 10, 60, 150, 20, Xprint.FONT_KAI);
			
			for(File f : xp.getOutputFiles())
	        	System.out.println(" => " + f.getAbsolutePath());
		}
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
