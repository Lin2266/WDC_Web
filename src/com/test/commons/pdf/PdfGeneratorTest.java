package com.test.commons.pdf;

import java.io.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import junit.framework.TestCase;

public class PdfGeneratorTest extends TestCase {
	
	/** 以程式完全控制的方式繪出公文內容 */
	public void xtestDrawTextFlow() throws Exception {
        final String outputFile = "/tmp/out.pdf";
        final String defaultFont = "/usr/share/fonts/cjkuni-uming/uming.ttc,0";
        //final String defaultFont = "/mnt/dos/font/TW-NIA-Sung-Plane0.ttf";
        final String defaultKaiFont = "/usr/share/fonts/cjkuni-ukai/ukai.ttc,0";
        final String plane2Font = "/home/raymond/文件/font/TW-NIA-Sung-Plane2.ttf";
        final String plane15Font = "/home/raymond/文件/font/TW-NIA-Sung-PlaneF.ttf";
        final String watermark = "/opt/ap/lns/image/watermark1.png";
        final String perforationSeal = "/opt/ap/lns/image/seal1.png";
        System.out.println("use defaultFont=" + defaultFont);
        System.out.println("use plane2Font=" + plane2Font);
        System.out.println("use plane15Font=" + plane15Font);
        System.out.println("use watermark=" + watermark);
        
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        PdfGenerator pdf = new PdfGenerator(out);
        pdf.setDefaultFont(defaultFont);
        pdf.setDefaultKaiFont(defaultKaiFont);
        pdf.setPlane2Font(plane2Font);
        pdf.setPlane15Font(plane15Font);
        pdf.setWatermark(watermark, 50, 70);
        pdf.setPerforationSeal(perforationSeal);
        pdf.setPrintPerforationSealOnSingleSide(true);
        pdf.setPageNumberPosition(PdfConst.PAGE_NUMBER_POSITION_TOP_CENTER);
        //pdf.setPrintPageNumberOnFirstPage(true);

        pdf.newPage();
        float yy = 0F;

        //pdf.addImage("watermark1.png", 60, 20, 70, 70);
        //pdf.addImage("arrow-first.gif", 15, 15);

        pdf.drawTextFlow("勞工保險局　函", 8.75F, 67, 20);
        yy = pdf.drawTextFlow("地　　址：10422 台北市中山路三段22號", 4, 125, 33, 21);
        yy = pdf.drawTextFlow("聯 絡 人：郭大同", 4, 125, ++yy, 21);
        yy = pdf.drawTextFlow("聯絡電話：12345678", 4, 125, ++yy, 21);
        yy = pdf.drawTextFlow("傳真電話：87654321", 4, 125, ++yy, 21);

        yy = pdf.drawTextFlow("受文者：大同公司", 5, 15, 50, 21);
        yy = pdf.drawTextFlow("發文日期：中華民國93年12月30日", 4, 15, ++yy + 4, 21);
        yy = pdf.drawTextFlow("發文字號：測字第0930001004號", 4, 15, ++yy, 21);
        yy = pdf.drawTextFlow("速別：普通件", 4, 15, ++yy, 21);
        yy = pdf.drawTextFlow("密等及解密條件或保密期限：密，總有一天", 4, 15, ++yy, 21);
        yy = pdf.drawTextFlow("附件：", 4, 15, ++yy, 21);

        yy = pdf.drawTextFlow("主旨：我是主旨。", 4.5F, 15, ++yy + 4, 12);
        yy = pdf.drawTextFlow("說明：", 4.5F, 15, ++yy, 12);
        yy = pdf.drawTextFlow(new StringBuilder("測外字：U+2F81A='").appendCodePoint(0x2F81A).append("',")
        		.append("U+201C7='").appendCodePoint(0x201C7).append("',")
        		.append("U+F9E0C='").appendCodePoint(0xF9E0C).append("',")
        		.append("U+FDA11='").appendCodePoint(0xFDA11).append("'")
        		.toString(), 4.5F, 20, ++yy, 12, 150, 
        		PdfConst.FONT_MING, PdfConst.FONT_STYLE_BOLD, PdfConst.COLOR_RED);
        yy = pdf.drawTextFlow("一、說明一", 4.5F, 20, ++yy, 12);
        yy = pdf.drawTextFlow("(一)說明一(一)", 4.5F, 25, ++yy, 12);
        yy = pdf.drawTextFlow("(二)說明一(二)", 4.5F, 25, ++yy, 12);
        yy = pdf.drawTextFlow("１、說明一(二)１", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("２、說明一(二)２", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("(１)說明一(二)２(１)", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("(２)說明一(二)２(２)", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("甲、說明一(二)２(２)甲", 4.5F, 35, ++yy, 12);
        yy = pdf.drawTextFlow("乙、說明一(二)２(２)乙", 4.5F, 35, ++yy, 12);
        yy = pdf.drawTextFlow("(甲)說明一(二)２(２)乙(甲)", 4.5F, 40, ++yy, 12);
        yy = pdf.drawTextFlow("(乙)說明一(二)２(２)乙(乙)", 4.5F, 40, ++yy, 12);
        yy = pdf.drawTextFlow("(丙)說明一(二)２(２)乙(丙)", 4.5F, 40, ++yy, 12);
        yy = pdf.drawTextFlow("丙、說明一(二)２(２)丙", 4.5F, 35, ++yy, 12);
        yy = pdf.drawTextFlow("(３)說明一(二)２(３)", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("３、說明一(二)３", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("(三)說明一(三)", 4.5F, 25, ++yy, 12);
        yy = pdf.drawTextFlow("二、說明二", 4.5F, 20, ++yy, 12);
        yy = pdf.drawTextFlow("三、說明三", 4.5F, 20, ++yy, 12);

        yy = pdf.drawTextFlow("辦法：我是辦法", 4.5F, 15, ++yy, 12);
        yy = pdf.drawTextFlow("一、辦法一", 4.5F, 20, ++yy, 12);
        yy = pdf.drawTextFlow("(一)辦法一(一)", 4.5F, 25, ++yy, 12);
        yy = pdf.drawTextFlow("(二)辦法一(二)", 4.5F, 25, ++yy, 12);
        yy = pdf.drawTextFlow("１、辦法一(二)１", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("２、辦法一(二)２", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("(１)辦法一(二)２(１)", 4.5F, 35, ++yy, 12);
        yy = pdf.drawTextFlow("(２)辦法一(二)２(２)", 4.5F, 35, ++yy, 12);
        yy = pdf.drawTextFlow("甲、辦法一(二)２(２)甲", 4.5F, 40, ++yy, 12);
        yy = pdf.drawTextFlow("乙、辦法一(二)２(２)乙", 4.5F, 40, ++yy, 12);
        yy = pdf.drawTextFlow("(甲)辦法一(二)２(２)乙(甲)", 4.5F, 45, ++yy, 12);
        yy = pdf.drawTextFlow("(乙)辦法一(二)２(２)乙(乙)", 4.5F, 45, ++yy, 12);
        yy = pdf.drawTextFlow("(丙)辦法一(二)２(２)乙(丙)", 4.5F, 45, ++yy, 12);
        yy = pdf.drawTextFlow("(丁)辦法一(二)２(２)乙(丁)", 4.5F, 45, ++yy, 12);
        yy = pdf.drawTextFlow("(戊)辦法一(二)２(２)乙(戊)", 4.5F, 45, ++yy, 12);
        yy = pdf.drawTextFlow("丙、辦法一(二)２(２)丙", 4.5F, 40, ++yy, 12);
        yy = pdf.drawTextFlow("(３)辦法一(二)２(３)", 4.5F, 35, ++yy, 12);
        yy = pdf.drawTextFlow("３、辦法一(二)３", 4.5F, 30, ++yy, 12);
        yy = pdf.drawTextFlow("(三)辦法一(三)", 4.5F, 25, ++yy, 12);
        yy = pdf.drawTextFlow("二、辦法二", 4.5F, 20, ++yy, 12);
        yy = pdf.drawTextFlow("三、辦法三 一二三四五六七八九十壹貳參肆伍陸柒捌玖拾一二三四五六七八九十壹貳參肆伍陸柒捌玖拾一二三四五六七八九十壹貳參肆伍陸柒捌玖拾一二三四五六七八九十壹貳參肆伍陸柒捌玖拾一二三四五六七八九十壹貳參肆伍陸柒捌玖拾", 4.5F, 20, ++yy, 10);

        yy = pdf.drawTextFlow("正本：大同世界公司，國防部", 4, 15, ++yy, 12);
        yy = pdf.drawTextFlow("副本：小異公司", 4, 15, ++yy, 12);


        pdf.newPage();

        pdf.drawRect(15, 250, 180, 20);
        pdf.drawLine(15, 260, 90, 260);
        pdf.drawLine(90, 250, 90, 270);
        pdf.drawCircle(100, 140, 70);

        //pdf.drawBarcode39("ITEXT IN ACTION", 15, 272, 0, 0);
        //pdf.drawBarcode25("411200076041001", 100, 272, 0, 0);
        pdf.drawBarcode128("ITEXT IN ACTION", 15, 272, 0, 0);
        //pdf.addImage("/tmp/bli_34.gif", 100, 261, 75, 18);

        pdf.close();
        out.close();
        System.out.println("=> output: " + outputFile);
    }
	
	/** 列出全部 unicode plane 2 字, 使用戶役政 3 代用的字型檔 */
	public void xtestDrawTextFlow1() throws Exception {
		final String outputFile = "/tmp/unicode_plane2.pdf";
		final String defaultFont = "/usr/share/fonts/cjkunifonts-uming/uming.ttf";
        //final String defaultFont = "/mnt/dos/font/TW-NIA-Sung-Plane0.ttf";
        final String plane2Font = "/mnt/dos/font/TW-NIA-Sung-Plane2.ttf";
        final String plane15Font = "/mnt/dos/font/TW-NIA-Sung-PlaneF.ttf";
        final String perforationSeal = "/opt/ap/lns/image/seal1.png";
        final int fontType = PdfConst.FONT_MING;
        System.out.println("use defaultFont=" + defaultFont);
        System.out.println("use plane2Font=" + plane2Font);
        System.out.println("use plane15Font=" + plane15Font);
		
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        PdfGenerator pdf = new PdfGenerator(out);
        pdf.setDefaultFont(defaultFont);
        pdf.setPlane2Font(plane2Font);
        pdf.setPlane15Font(plane15Font);
        pdf.setPageNumberPosition(PdfConst.PAGE_NUMBER_POSITION_BOTTOM_CENTER);
        pdf.setPrintPageNumberOnFirstPage(true);
        pdf.setPerforationSeal(perforationSeal);
        pdf.setPrintPerforationSealOnSingleSide(true);
        pdf.newPage();
        
        pdf.drawTextBox("Unicode 第 2 字面列表 (使用中推會之移民署案字型)", 4, 10, 5, 190, 5, 
        		PdfConst.XPRINT_DISTRIBUTED_DEFAULT, fontType, PdfConst.FONT_STYLE_BOLD, PdfConst.COLOR_BLUE);
        
        final float fontSize = 4F;
        final float leading = 5F;
        final float charOffset = 5F;
        final float startX = 20F;
        final float startY = 10F;
        final float textBoxWidth = 5F;
        final float textBoxHeight = 5F;
        
        //第二字面
        float y = startY, x = startX;
        for(int i = 0x20000, col = 0; i < 0x30000; i++, col++) {
        	if(col == 35) { //一列放 35 字
        		col = 0;
        		x = startX;
        		y += leading;
        		if(y > 280F) {
        			y = startY;
        			pdf.newPage();
        		}
        	}
        	if(col == 0) //每列開頭註明第一個字的 unicode 碼
        		pdf.drawTextBox("U+" + Integer.toHexString(i), 1.6F, x - 10, y, 10, textBoxHeight, PdfConst.XPRINT_DISTRIBUTED_DEFAULT, fontType, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLUE);
        	pdf.drawTextBox(new String(new int[] { i }, 0, 1), fontSize, x, y, textBoxWidth, textBoxHeight);
        	x += charOffset;
        }
        
        pdf.close();
        out.close();
        System.out.println("=> output: " + outputFile);
	}
	
	//列出全部 unicode plane 15 字
	public void xtestDrawTextFlow2() throws Exception {
		final String outputFile = "/tmp/unicode_plane15.pdf";
		final String defaultFont = "/usr/share/fonts/cjkuni-uming/uming.ttc,0";
        //final String defaultFont = "/mnt/dos/font/TW-NIA-Sung-Plane0.ttf";
        final String plane2Font = "/home/raymond/文件/font/TW-NIA-Sung-Plane2.ttf";
        final String plane15Font = "/home/raymond/文件/font/TW-NIA-Sung-PlaneF.ttf";
        final int fontType = PdfConst.FONT_MING;
        System.out.println("use defaultFont=" + defaultFont);
        System.out.println("use plane2Font=" + plane2Font);
        System.out.println("use plane15Font=" + plane15Font);
        
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        PdfGenerator pdf = new PdfGenerator(out);
        pdf.setDefaultFont(defaultFont);
        pdf.setPlane2Font(plane2Font);
        pdf.setPlane15Font(plane15Font);
        pdf.newPage();

        pdf.drawTextBox("Unicode 第 15 字面列表 (使用中推會之移民署案字型)", 4, 10, 5, 190, 5, 
        		PdfConst.XPRINT_DISTRIBUTED_DEFAULT, fontType, PdfConst.FONT_STYLE_BOLD, PdfConst.COLOR_BLUE);
        
        final float fontSize = 4F;
        final float leading = 5F;
        final float charOffset = 5F;
        final float startX = 20F;
        final float startY = 10F;
        final float textBoxWidth = 5F;
        final float textBoxHeight = 5F;
        
        //第 15 字面
        float y = startY, x = startX;
        for(int i = 0xF0000, col = 0; i < 0x100000; i++, col++) {
        	if(col == 35) { //一列放 35 字
        		col = 0;
        		x = startX;
        		y += leading;
        		if(y > 280F) {
        			y = startY;
        			pdf.newPage();
        		}
        	}
        	if(col == 0) //每列開頭註明第一個字的 unicode 碼
        		pdf.drawTextBox("U+" + Integer.toHexString(i), 1.6F, x - 10, y, 10, textBoxHeight, 
        				PdfConst.XPRINT_DISTRIBUTED_DEFAULT, fontType, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLUE);
        	pdf.drawTextBox(new String(new int[] { i }, 0, 1), fontSize, x, y, textBoxWidth, textBoxHeight);
        	x += charOffset;
        }
        
        pdf.close();
        out.close();
        System.out.println("=> output: " + outputFile);
	}
    
    public void xtestDrawTextBox() throws Exception {
    	final String outputFile = "/tmp/out1.pdf";
    	final String defaultFont = "/usr/share/fonts/cjkuni-uming/uming.ttc,0";
        //final String defaultFont = "/mnt/dos/font/TW-NIA-Sung-Plane0.ttf";
    	final String defaultKaiFont = "/usr/share/fonts/cjkuni-ukai/ukai.ttc,0";
    	System.out.println("use defaultFont=" + defaultFont);
        
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        PdfGenerator pdf = new PdfGenerator(out);
        pdf.setDefaultFont(defaultFont);
        pdf.setDefaultFont(defaultKaiFont);
        pdf.setPageNumberPosition(PdfConst.PAGE_NUMBER_POSITION_BOTTOM_CENTER);
        pdf.setPrintPageNumberOnFirstPage(true);
        
        pdf.newPage(150, 150); //unit: mm
        pdf.drawRect(0, 0, 100, 20);
        
        pdf.drawLine(5, 0, 5, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(10, 0, 10, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(15, 0, 15, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(20, 0, 20, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(25, 0, 25, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(30, 0, 30, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(35, 0, 35, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(40, 0, 40, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(45, 0, 45, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(50, 0, 50, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(55, 0, 55, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(60, 0, 60, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(65, 0, 65, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(70, 0, 70, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(75, 0, 75, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(80, 0, 80, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(85, 0, 85, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(90, 0, 90, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(95, 0, 95, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        pdf.drawLine(100, 0, 100, 20, 0.2F, PdfConst.LINE_DASH, PdfConst.COLOR_RED);
        
        pdf.drawTextBox("主動斷行\n1.a測試 test TEST數字123文 2.a測試 test TEST數字456文 3.a測試 test TEST數字789文 4.a測試 test TEST數字012文 5.a測試 test TEST數字345文 6.a測試 test TEST數字123文 7.a測試 test TEST數字456文 8.a測試 test TEST數字789文 9.a測試 test TEST數字012文 10.a測試 test TEST數字345文 11.a測試 test TEST數字345文 12.a測試 test TEST數字345文 13.a測試 test TEST數字345文 14.a測試 test TEST數字345文 15.a測試 test TEST數字345文 16.a測試 test TEST數字345文 17.a測試 test TEST數字345文", 
                5, 0, 0, 100, 20, PdfConst.XPRINT_DISTRIBUTED_DEFAULT, PdfConst.FONT_MING, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLACK);
        
        pdf.drawRect(10, 25, 110, 40, 0.5F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLUE);
        pdf.drawTextBox("1234567890一二三四五六七八九十壹貳參肆伍陸柒捌玖拾１２３ ４５６ ７８９ ０", 
        		10, 10, 25, 110, 40, PdfConst.XPRINT_DISTRIBUTED_DEFAULT, PdfConst.FONT_KAI, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLUE);
        
        pdf.drawRect(20, 70, 110, 20, 0.7F, PdfConst.LINE_DOT, PdfConst.COLOR_GREEN);
        pdf.drawTextBox("半形點.全形點．半形逗號,全形逗號，半形分號;全形分號；半形數1全形數１半形英文A全形英文Ａ", 
        		4, 20, 70, 110, 20, PdfConst.XPRINT_DISTRIBUTED_DEFAULT, PdfConst.FONT_MING, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_GREEN);
        
        pdf.drawLine(20, 90, 70, 90, 1.5F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        pdf.drawLine(20, 100, 80, 100, 2F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        pdf.drawLine(20, 110, 90, 110, 3F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        
        pdf.close();
        out.close();
        System.out.println("=> output: " + outputFile);
    }
    
    public void testGrid() throws Exception {
    	final String outputFile = "/tmp/out2.pdf";
    	OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        PdfGenerator pdf = new PdfGenerator(out, PdfConst.PAGE_A4, 0, 0, 0, 0);
        pdf.newPage();
        
        pdf.drawLine(10, 10, 200, 10, 4, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 20, 200, 20, 4, PdfConst.LINE_DASH, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 30, 200, 30, 4, PdfConst.LINE_DOT, PdfConst.COLOR_BLACK);
        
        pdf.drawLine(10, 40, 200, 40, 5, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 50, 200, 50, 5, PdfConst.LINE_DASH, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 60, 200, 60, 5, PdfConst.LINE_DOT, PdfConst.COLOR_BLACK);
        
        pdf.drawLine(10, 70, 200, 70, 2, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 80, 200, 80, 2, PdfConst.LINE_DASH, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 90, 200, 90, 2, PdfConst.LINE_DOT, PdfConst.COLOR_BLACK);
        
        pdf.drawLine(10, 100, 200, 100, 1.5F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 110, 200, 110, 1.5F, PdfConst.LINE_DASH, PdfConst.COLOR_BLACK);
        pdf.drawLine(10, 120, 200, 120, 1.5F, PdfConst.LINE_DOT, PdfConst.COLOR_BLACK);
    
        pdf.close();
        out.close();
        System.out.println("=> output: " + outputFile);
    }
    
    //-------------------------------------------------------------------------
    //for testing pure iText code
    //-------------------------------------------------------------------------
    final int LOCATIONS = 9; //The number of locations on our time table
	final int TIMESLOTS = 32; //The number of time slots on our time table
	final float OFFSET_LEFT = 76; //The offset to the left of our time table
	final float WIDTH = 740; //The width of our time table
	final float OFFSET_BOTTOM = 36; //The offset from the bottom of our time table
	final float HEIGHT = 504; //The height of our time table
	final float OFFSET_LOCATION = 26; //The offset of the location bar next to our time table
	final float WIDTH_LOCATION = 48; //The width of the location bar next to our time table
	final float HEIGHT_LOCATION = HEIGHT / LOCATIONS; //The height of a bar showing the movies at one specific location
	final float WIDTH_TIMESLOT = WIDTH / TIMESLOTS; //The width of a time slot
    
    public void xtestIText() throws Exception {
    	final String RESULT = "/tmp/time_table.pdf";
    	
    	// step 1
        Document document = new Document(PageSize.A4.rotate());
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        //writer.setPdfVersion(PdfWriter.VERSION_1_5);
        // step 3
        document.open();
        // step 4
        testIText_drawTimeTable(writer.getDirectContentUnder());
        //document.newPage();
        testIText_drawTimeSlots1(writer.getDirectContent());
        testIText_drawTimeSlots2(writer.getDirectContent());
        
        // step 5
        document.close();
        System.out.println("testIText(): output=" + RESULT);
    }
    
    public void xtestIText2() throws Exception {
    	final String RESULT = "/tmp/time_table.pdf";
    	
    	// step 1
        Document document = new Document(PageSize.A4.rotate());
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        //writer.setPdfVersion(PdfWriter.VERSION_1_5);
        // step 3
        document.open();
        // step 4
        PdfContentByte directcontent = writer.getDirectContent();
        float x;
        { //第 1 條線
        	directcontent.saveState();
            x = OFFSET_LEFT + (1 * WIDTH_TIMESLOT);
            directcontent.moveTo(x, OFFSET_BOTTOM);
            directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
            directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
            directcontent.setLineWidth(2f);
            //directcontent.setColorStroke(BaseColor.GRAY);
            directcontent.setLineDash(10, 2, 1);
            directcontent.stroke();
            directcontent.restoreState();
        }
        { //第 2-5 條線
        	directcontent.saveState();
            for (int i = 2; i < 6; i++) {
                x = OFFSET_LEFT + (i * WIDTH_TIMESLOT);
                directcontent.moveTo(x, OFFSET_BOTTOM);
                directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
            }
            directcontent.setLineWidth(1f);
            //directcontent.setColorStroke(BaseColor.GRAY);
            directcontent.setLineDash(5, 3, 1);
            directcontent.stroke();
            directcontent.restoreState();
        }
        { //第 6-10 條線
        	directcontent.saveState();
            for (int i = 6; i < 11; i++) {
                x = OFFSET_LEFT + (i * WIDTH_TIMESLOT);
                directcontent.moveTo(x, OFFSET_BOTTOM);
                directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
            }
            directcontent.setLineWidth(0.6f);
            //directcontent.setColorStroke(BaseColor.GRAY);
            directcontent.setLineDash(3, 2, 1);
            directcontent.stroke();
            directcontent.restoreState();
        }
        { //第 11- 條線
        	directcontent.saveState();
            for (int i = 11; i < TIMESLOTS; i++) {
                x = OFFSET_LEFT + (i * WIDTH_TIMESLOT);
                directcontent.moveTo(x, OFFSET_BOTTOM);
                directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
            }
            directcontent.setLineWidth(0.3f);
            //directcontent.setColorStroke(BaseColor.GRAY);
            directcontent.setLineDash(1, 1, 1);
            directcontent.stroke();
            directcontent.restoreState();
        }
        
        // step 5
        document.close();
        System.out.println("testIText(): output=" + RESULT);
    }
    
    //Draws the time table for a day at the film festival.
    void testIText_drawTimeTable(PdfContentByte directcontent) {        
        directcontent.saveState();
 
        directcontent.setLineWidth(1.2f);
        float llx, lly, urx, ury;
 
        llx = OFFSET_LEFT;
        lly = OFFSET_BOTTOM;
        urx = OFFSET_LEFT + WIDTH;
        ury = OFFSET_BOTTOM + HEIGHT;
        directcontent.moveTo(llx, lly);
        directcontent.lineTo(urx, lly);
        directcontent.lineTo(urx, ury);
        directcontent.lineTo(llx, ury);
        directcontent.closePath();
        directcontent.stroke();
 
        llx = OFFSET_LOCATION;
        lly = OFFSET_BOTTOM;
        urx = OFFSET_LOCATION + WIDTH_LOCATION;
        ury = OFFSET_BOTTOM + HEIGHT;
        directcontent.moveTo(llx, lly);
        directcontent.lineTo(urx, lly);
        directcontent.lineTo(urx, ury);
        directcontent.lineTo(llx, ury);
        directcontent.closePathStroke();
 
        directcontent.setLineWidth(1);
        directcontent.moveTo(OFFSET_LOCATION + WIDTH_LOCATION / 2, OFFSET_BOTTOM);
        directcontent.lineTo(OFFSET_LOCATION + WIDTH_LOCATION / 2, OFFSET_BOTTOM + HEIGHT);
        float y;
        for (int i = 1; i < LOCATIONS; i++) {
            y = OFFSET_BOTTOM + (i * HEIGHT_LOCATION);
            if (i == 2 || i == 6) {
                directcontent.moveTo(OFFSET_LOCATION, y);
                directcontent.lineTo(OFFSET_LOCATION + WIDTH_LOCATION, y);
            }
            else {
                directcontent.moveTo(OFFSET_LOCATION + WIDTH_LOCATION / 2, y);
                directcontent.lineTo(OFFSET_LOCATION + WIDTH_LOCATION, y);
            }
            directcontent.moveTo(OFFSET_LEFT, y);
            directcontent.lineTo(OFFSET_LEFT + WIDTH, y);
        }
        directcontent.stroke();
 
        directcontent.restoreState();
    }
    
    //Draws the time slots for a day at the film festival
    void testIText_drawTimeSlots1(PdfContentByte directcontent) {
        directcontent.saveState();
        float x;
        
        //第 1 條虛線
        x = OFFSET_LEFT + (1 * WIDTH_TIMESLOT);
        directcontent.setLineWidth(0.3f);
        directcontent.setLineDash(6, 2, 1);
        //directcontent.setLineDash(3, 1);
        directcontent.moveTo(x, OFFSET_BOTTOM);
        directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
        directcontent.stroke(); //此行會令 Chrome 列印時, 此虛線消失; 但少了此行, 又會令此行的線條樣式被下面的設定蓋過去
//        directcontent.restoreState(); //無隔避免 Chrome 列印的問題
        
//        directcontent.saveState();
        //第 2 至第 5 條虛線
        for (int i = 2; i < 6; i++) {
            x = OFFSET_LEFT + (i * WIDTH_TIMESLOT);
            directcontent.moveTo(x, OFFSET_BOTTOM);
            directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
//directcontent.setLineWidth(0.3f);
//directcontent.setLineDash(3, 1);
//directcontent.stroke();
        }
        //directcontent.setColorStroke(BaseColor.GRAY);
        directcontent.setLineWidth(0.3f);
        directcontent.setLineDash(3, 1);
        directcontent.stroke();
        directcontent.restoreState();
    }
    
    void testIText_drawTimeSlots2(PdfContentByte directcontent) {
        directcontent.saveState();
        float x;
        for (int i = 6; i < TIMESLOTS; i++) {
            x = OFFSET_LEFT + (i * WIDTH_TIMESLOT);
            directcontent.moveTo(x, OFFSET_BOTTOM);
            directcontent.lineTo(x, OFFSET_BOTTOM + HEIGHT);
        }
        directcontent.setLineWidth(0.3f);
        directcontent.setColorStroke(BaseColor.GRAY);
        directcontent.setLineDash(1, 1, 1);
        directcontent.stroke();
        directcontent.restoreState();
    }
}
