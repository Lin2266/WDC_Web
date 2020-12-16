package com.test.commons.pdf2;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeInter25;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.VerticalText;
import com.test.commons.util.StrUtil;

/**
 * 產生 PDF 文件的基礎元件(主要以配合 Xprint 版面設定規格為主).
 * 基本用法:<pre>
 * OutputStream out = new BufferedOutputStream(new FileOutputStream(OUTPUT_FILE)); //開寫入的 PDF 檔
 * PdfGenerator pdf = new PdfGenerator(out); //建立 PdfGenerator 物件
 * pdf.setDefaultFont(UNICODE_PLANE_0_FONT_PATH_OR_FONT_NAME); //指定預設字型 
 * pdf.setPlane2Font(UNICODE_PLANE_2_FONT_PATH_OR_FONT_NAME); //指定 unicode 第二字面字型
 * pdf.setPlane15Font(UNICODE_PLANE_15_FONT_PATH_OR_FONT_NAME); //指定 unicode 第 15 字面字型
 * pdf.setWatermark(WATERMARK_IMAGE_PATH, 50, 70); //optional: 指定浮水印圖檔
 * 
 * pdf.newPage(); //起新頁(也可用其他帶參數的 newPage(...) 指定紙張樣式
 * 
 * float y = pdf.drawTextFlow(...); //optional: 寫文字(無下邊界)
 * pdf.drawTextBox(...); //optional: 寫文字欄
 * pdf.drawRect(...); //optional: 畫框
 * pdf.drawLine(...); //optional: 畫線
 * pdf.drawCircle(...); //optional: 畫圓
 * pdf.addImage(...); //貼圖
 * ...
 * 
 * pdf.close(); //關閉一切 IO 資源
 * out.close();
 * </pre>
 * <p>
 * depend on: iText
 */
public class PdfGenerator {
	private static Log log = LogFactory.getLog(PdfGenerator.class);
	
	/** 預設文字列間距(mm) */
	public static final float DEFAULT_ROW_GAP = mm(1F);
	
	private boolean isLastPage; //當前頁是否為 pdf close() 前的最後一頁
	
    private boolean debug;
    private Rectangle pageStyle; //當前頁大小
    private int pageStyle1; //當前頁 style
    private float marginTop; //(pt) iText 無內建簡便 API 以取得紙張四周邊界長度(考慮多種情況之故), 故在此自行維護當前頁的四邊界
    private float marginBottom; //(pt)
    private float marginLeft; //(pt)
    private float marginRight; //(pt)
    private Document document; //PDF document
    private PdfWriter writer;
    private PdfContentByte cb; //畫布
    private String currentImagePath;
    private Image currentImage;
    private Map<String, Image> imageCache;
    private Map<String, Image> barcode25Cache;
    private Map<String, Image> barcode39Cache;
    private Map<String, Image> barcode128Cache;
    private Image watermarkImage;
    private Image perforationSealImage; //騎縫章圖物件
    private float[] perforationSealPosition; //騎縫章寬高 [width, height, x1, y1, x2, y2], (x1, y1) 左/上側; (x2, y2) 右/下側, 單位: pt, 原點: 紙張左下角
    private boolean printPerforationSealOnSingleSide; //是否採雙面列印模式(從第 2 頁開始, 每頁只有一邊印出騎縫章)(預設: false)
    private boolean printPerforationSealOnTopDown; //騎縫章是否印在每頁上下側(預設: false, 印在左右側)
    private boolean printPerforationSealOnFirstPageFromRight; //騎縫章蓋左右側且為單面列印模式時, 首頁是否蓋在右側(預設: false)
    private String defaultFontName;
    private String defaultKaiFontName;
    private String plane2FontName;
    private String kaiPlane2FontName;
    private String plane15FontName;
    private String kaiPlane15FontName;
    private BaseFont fontPlane0H; //橫書式文字字型 (Unicode plane 0, BMP)
    private BaseFont fontPlane0V;
    private BaseFont fontKaiPlane0H; //橫書式楷體字型 (Kai Unicode plane 0, BMP)
    private BaseFont fontKaiPlane0V;
    private BaseFont fontPlane2H; //橫書式文字字型 (Unicode plane 2)
    private BaseFont fontPlane2V;
    private BaseFont fontKaiPlane2H; //橫書式楷體字型 (Unicode plane 2)
    private BaseFont fontKaiPlane2V;
    private BaseFont fontPlane15H; //橫書式文字字型 (Unicode plane 15)
    private BaseFont fontPlane15V;
    private BaseFont fontKaiPlane15H; //橫書式楷體字型 (Unicode plane 15)
    private BaseFont fontKaiPlane15V;
    private Font currentFont0H; //當前文字行之字型 (Unicode plane 0, BMP)
    private Font currentFont0V;
    private Font currentFont2H; //當前文字行之字型 (Unicode plane 2)
    private Font currentFont2V;
    private Font currentFont15H; //當前文字行之字型 (Unicode plane 15)
    private Font currentFont15V;
    private int pageCount; //當前 PDF document 的頁數
    private int totalPages; //由本 class 呼叫者所維護的文件總頁數. 本 class 只負責在換頁時將此變數值加 1
    private int pageNumberPosition; //頁碼位置 (PdfConst.PAGE_NUMBER_NONE/PAGE_NUMBER_POSITION_TOP_CENTER/PAGE_NUMBER_POSITION_BOTTOM_CENTER
    private boolean printPageNumberOnFirstPage; //如果要印頁碼時, 首頁是否需列印頁碼?
    private float[] pgNumTopology; //頁碼佈局 (mm) [ 字型大小,  x(固定位置時的 x 坐標), y(y 坐標值), x1(靠右時的 x 坐標), x2(靠左時的 x 坐標) ]
    private PageNoHandler pageNoHandler; //自訂頁碼框處理物件了

    /**
     * 使用預設紙張大小, 預設邊界(可在起新頁時自行指定紙張樣式).
     * @param out
     * @see PdfConst#DEFAULT_PAGE_SIZE
     * @see PdfConst#DEFAULT_MARGIN_LEFT
     * @see PdfConst#DEFAULT_MARGIN_RIGHT
     * @see PdfConst#DEFAULT_MARGIN_TOP
     * @see PdfConst#DEFAULT_MARGIN_BOTTOM
     */
    public PdfGenerator(OutputStream out) {
        this(out, PdfConst.DEFAULT_PAGE_SIZE, PdfConst.DEFAULT_MARGIN_LEFT, PdfConst.DEFAULT_MARGIN_RIGHT, 
        		PdfConst.DEFAULT_MARGIN_TOP, PdfConst.DEFAULT_MARGIN_BOTTOM, (PageNoHandler)null);
    }
    
    /**
     * 使用預設紙張大小, 預設邊界(可在起新頁時自行指定紙張樣式).
     * @param out
     * @param pageNoHandler 自訂頁碼框處理物件
     * @see PdfConst#DEFAULT_PAGE_SIZE
     * @see PdfConst#DEFAULT_MARGIN_LEFT
     * @see PdfConst#DEFAULT_MARGIN_RIGHT
     * @see PdfConst#DEFAULT_MARGIN_TOP
     * @see PdfConst#DEFAULT_MARGIN_BOTTOM
     * @see #PageNoHandler
     */
    public PdfGenerator(final OutputStream out, final PageNoHandler pageNoHandler) {
        this(out, PdfConst.DEFAULT_PAGE_SIZE, PdfConst.DEFAULT_MARGIN_LEFT, PdfConst.DEFAULT_MARGIN_RIGHT, 
        		PdfConst.DEFAULT_MARGIN_TOP, PdfConst.DEFAULT_MARGIN_BOTTOM, pageNoHandler);
    }

    /**
     * @param out
     * @param pageSize PDF 初始紙張大小設定(可在起新頁時自行指定紙張樣式), 可為: 
     * 		PdfConst.PAGE_A3, PdfConst.PAGE_A4, PdfConst.PAGE_B3, PdfConst.PAGE_B4, PdfConst.PAGE_A3_LANDSCAPE, 
     * 		PdfConst.PAGE_A4_LANDSCAPE, PdfConst.PAGE_B3_LANDSCAPE, PdfConst.PAGE_B4_LANDSCAPE 之一
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @see PdfConst#PAGE_A3
     * @see PdfConst#PAGE_A4
     * @see PdfConst#PAGE_B3
     * @see PdfConst#PAGE_B4
     * @see PdfConst#PAGE_A3_LANDSCAPE
     * @see PdfConst#PAGE_A4_LANDSCAPE
     * @see PdfConst#PAGE_B3_LANDSCAPE
     * @see PdfConst#PAGE_B4_LANDSCAPE
     */
    public PdfGenerator(final OutputStream out, final int pageSize, final float marginLeft, final float marginRight, 
    		final float marginTop, final float marginBottom) {
    	this(out, pageSize, marginLeft, marginRight, marginTop, marginBottom, (PageNoHandler)null);
    }
    
    /**
     * @param out
     * @param pageSize PDF 初始紙張大小設定(可在起新頁時自行指定紙張樣式), 可為: 
     * 		PdfConst.PAGE_A3, PdfConst.PAGE_A4, PdfConst.PAGE_B3, PdfConst.PAGE_B4, PdfConst.PAGE_A3_LANDSCAPE, 
     * 		PdfConst.PAGE_A4_LANDSCAPE, PdfConst.PAGE_B3_LANDSCAPE, PdfConst.PAGE_B4_LANDSCAPE 之一
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @param pageNoHandler 自訂頁碼框處理物件
     * @see PdfConst#PAGE_A3
     * @see PdfConst#PAGE_A4
     * @see PdfConst#PAGE_B3
     * @see PdfConst#PAGE_B4
     * @see PdfConst#PAGE_A3_LANDSCAPE
     * @see PdfConst#PAGE_A4_LANDSCAPE
     * @see PdfConst#PAGE_B3_LANDSCAPE
     * @see PdfConst#PAGE_B4_LANDSCAPE
     * @see #PageNoHandler
     */
    public PdfGenerator(final OutputStream out, final int pageSize, final float marginLeft, final float marginRight, 
    		final float marginTop, final float marginBottom, final PageNoHandler pageNoHandler) {
        try {
            this.pageStyle1 = pageSize;
            
            if(pageSize == PdfConst.PAGE_A3)
            	this.pageStyle = PageSize.A3;
            else if(pageSize == PdfConst.PAGE_A3_LANDSCAPE)
            	this.pageStyle = PageSize.A3.rotate();
            else if(pageSize == PdfConst.PAGE_A4)
            	this.pageStyle = PageSize.A4;
            else if(pageSize == PdfConst.PAGE_A4_LANDSCAPE)
            	this.pageStyle = PageSize.A4.rotate();
            else if(pageSize == PdfConst.PAGE_B3)
            	this.pageStyle = PageSize.B3;
            else if(pageSize == PdfConst.PAGE_B3_LANDSCAPE)
            	this.pageStyle = PageSize.B3.rotate();
            else if(pageSize == PdfConst.PAGE_B4)
            	this.pageStyle = PageSize.B4;
            else if(pageSize == PdfConst.PAGE_B4_LANDSCAPE)
            	this.pageStyle = PageSize.B4.rotate();
            else
            	this.pageStyle = PageSize.A4;
            
            this.marginLeft = pt(marginLeft);
            this.marginRight = pt(marginRight);
            this.marginTop = pt(marginTop);
            this.marginBottom = pt(marginBottom);
            
            this.document = new Document(this.pageStyle, this.marginLeft, this.marginRight, this.marginTop, this.marginBottom);
            this.writer = PdfWriter.getInstance(this.document, out);
            //this.writer.setEncryption(null, null, ALLOW_PRINTING  | ALLOW_COPY  |
            //        ALLOW_SCREENREADERS  | ALLOW_DEGRADED_PRINTING ,
            //        STANDARD_ENCRYPTION_40); //可檢視, 可列印
            this.writer.setFullCompression();
            
            this.document.addAuthor(PdfConst.PDF_AUTHOR_NAME);
            this.document.addCreator(PdfConst.PDF_CREATER_NAME);
            this.document.addProducer();
            this.document.addCreationDate();
            this.pageCount = 0; //尚未執行 newPage()
            this.totalPages = 0; //實際總頁數要在執行 setTotalPage() 後才有意義
            this.pageNumberPosition = PdfConst.PAGE_NUMBER_NONE; //預設不印頁碼
            
            //自訂頁碼文字框(在 document.open() 前執行)
            if(pageNoHandler != null) {
            	this.pageNoHandler = pageNoHandler;
            	this.writer.setPageEvent(pageNoHandler);
            }

            //規定執行完 newPage() 後才可新增 PDF 內容
            this.document.open();
            this.cb = this.writer.getDirectContent();
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }
    
    /**
     * 在換頁動作(可能發生在 newPage() 或 drawTextFlow() 之類的動作)之前呼叫過 setTotalPage(int) 後, 才有意義.
     * @return 本 class 之呼叫者所維護的文件總數.
     */
    public int getTotalPages() {
    	return this.totalPages;
    }
    
    /**
     * 設定 PDF 輸出檔案的總頁數. 由本 class 呼叫者所維護的文件總頁數, 本 class 只負責在換頁時將此變數值加 1.
     * 呼叫者可能進行拆分多 PDF 檔(多 PDF document)輸出的動作, 而本 class 只保存單一 PDF document 的狀態, 不可能得知分檔的動作.
     */
    public void setTotalPages(int totalPages) {
    	this.totalPages = totalPages;
    }
    
    /**
     * @return 當前 PDF 檔的頁數
     */
    public int getPageCount() {
    	return this.pageCount;
    }
    
    /**
     * 指定 Unicode BMP 字型.
     * @param fontName 字型名稱, 或該字型的字型檔之完整實體路徑
     */
    public void setDefaultFont(String fontName) {
    	this.defaultFontName = fontName;
    }
    
    /**
     * 指定楷體 Unicode BMP 字型.
     * @param fontName 字型名稱, 或該字型的字型檔之完整實體路徑
     */
    public void setDefaultKaiFont(String fontName) {
    	this.defaultKaiFontName = fontName;
    }
    
    /**
     * 指定 Unicode 第 2 字面字型.
     * @param fontName 字型名稱, 或該字型的字型檔之完整實體路徑
     */
    public void setPlane2Font(String fontName) {
    	this.plane2FontName = fontName;
    }
    
    /**
     * 指定楷體 Unicode 第 2 字面字型.
     * @param fontName 字型名稱, 或該字型的字型檔之完整實體路徑
     */
    public void setKaiPlane2Font(String fontName) {
    	this.kaiPlane2FontName = fontName;
    }
    
    /**
     * 指定 Unicode 第 15 字面字型.
     * @param fontName 字型名稱, 或該字型的字型檔之完整實體路徑
     */
    public void setPlane15Font(String fontName) {
    	this.plane15FontName = fontName;
    }
    
    /**
     * 指定楷體 Unicode 第 15 字面字型.
     * @param fontName 字型名稱, 或該字型的字型檔之完整實體路徑
     */
    public void setKaiPlane15Font(String fontName) {
    	this.kaiPlane15FontName = fontName;
    }
    
    //水平排列 BMP 字型
    private BaseFont getPlane0FontH() {
    	try {
    		if(this.fontPlane0H== null) {
    			if(this.defaultFontName == null)
    				throw new IllegalStateException("setDefaultFont() not executed in advance (for Unicode BMP font)");
    			this.fontPlane0H = BaseFont.createFont(this.defaultFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		}
    		//log.debug("BaseFont plane0FontH: \n" + getBaseFontInfo(this.fontPlane0H));
    		return this.fontPlane0H;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //垂直排列 BMP 字型
    private BaseFont getPlane0FontV() {
    	try {
    		if(this.fontPlane0V == null) {
    			if(this.defaultFontName == null)
    				throw new IllegalStateException("setDefaultFont() not executed in advance (for Unicode BMP font)");
        		this.fontPlane0V = BaseFont.createFont(this.defaultFontName, BaseFont.IDENTITY_V, BaseFont.EMBEDDED);
    		}
    		//log.debug("BaseFont plane0FontV: \n" + getBaseFontInfo(this.fontPlane0V));
    		return this.fontPlane0V;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //水平排列楷體 BMP 字型
    private BaseFont getKaiPlane0FontH() {
    	try {
    		if(this.defaultKaiFontName == null)
    			return getPlane0FontH();
    		if(this.fontKaiPlane0H == null)
    			this.fontKaiPlane0H = BaseFont.createFont(this.defaultKaiFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		//log.debug("BaseFont kaiPlane0FontH: \n" + getBaseFontInfo(this.fontKaiPlane0H));
    		return this.fontKaiPlane0H;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //垂直排列楷體 BMP 字型
    private BaseFont getKaiPlane0FontV() {
    	try {
    		if(this.defaultKaiFontName == null)
    			return getPlane0FontV();
    		if(this.fontKaiPlane0V == null)
    			this.fontKaiPlane0V = BaseFont.createFont(this.defaultKaiFontName, BaseFont.IDENTITY_V, BaseFont.EMBEDDED);
    		//log.debug("BaseFont kaiPlane0FontV: \n" + getBaseFontInfo(this.fontKaiPlane0V));
    		return this.fontKaiPlane0V;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //水平排列 plane 2 字型, 無者傳回 null(要換成替代字元, 不能傳回 plane 0 字型)
    private BaseFont getPlane2FontH() {
    	try {
    		if(this.plane2FontName == null)
    			return null;
    		if(this.fontPlane2H == null)
    			this.fontPlane2H = BaseFont.createFont(this.plane2FontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		//log.debug("BaseFont plane2FontH: \n" + getBaseFontInfo(this.fontPlane2H));
    		return this.fontPlane2H;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //垂直排列 plane 2 字型, 無者傳回 null(要換成替代字元, 不能傳回 plane 0 字型)
    private BaseFont getPlane2FontV() {
    	try {
    		if(this.plane2FontName == null)
    			return null;
    		if(this.fontPlane2V == null)
    			this.fontPlane2V = BaseFont.createFont(this.plane2FontName, BaseFont.IDENTITY_V, BaseFont.EMBEDDED);
    		//log.debug("BaseFont plane2FontV: \n" + getBaseFontInfo(this.fontPlane2V));
    		return this.fontPlane2V;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //楷體水平排列 plane 2 字型, 無者傳回明體 plane 2 字型
    private BaseFont getKaiPlane2FontH() {
    	try {
    		if(this.kaiPlane2FontName == null)
    			return getPlane2FontH();
    		if(this.fontKaiPlane2H == null)
    			this.fontKaiPlane2H = BaseFont.createFont(this.kaiPlane2FontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		//log.debug("BaseFont kaiPlane2FontH: \n" + getBaseFontInfo(this.fontKaiPlane2H));
    		return this.fontKaiPlane2H;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //楷體垂直排列 plane 2 字型, 無者傳回明體 plane 2 字型
    private BaseFont getKaiPlane2FontV() {
    	try {
    		if(this.kaiPlane2FontName == null)
    			return getPlane2FontV();
    		if(this.fontKaiPlane2V == null)
    			this.fontKaiPlane2V = BaseFont.createFont(this.kaiPlane2FontName, BaseFont.IDENTITY_V, BaseFont.EMBEDDED);
    		//log.debug("BaseFont kaiPlane2FontV: \n" + getBaseFontInfo(this.fontKaiPlane2V));
    		return this.fontKaiPlane2V;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //水平排列 plane 15 字型, 無者傳回 null(要換成替代字元, 不能傳回 plane 0 字型)
    private BaseFont getPlane15FontH() {
    	try {
    		if(this.plane15FontName == null)
    			return null;
    		if(this.fontPlane15H == null)
    			this.fontPlane15H = BaseFont.createFont(this.plane15FontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		//log.debug("BaseFont plane15FontH: \n" + getBaseFontInfo(this.fontPlane15H));
    		return this.fontPlane15H;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //垂直排列 plane 15 字型, 無者傳回 null(要換成替代字元, 不能傳回 plane 0 字型)
    private BaseFont getPlane15FontV() {
    	try {
    		if(this.plane15FontName == null)
    			return null;
    		if(this.fontPlane15V == null)
    			this.fontPlane15V = BaseFont.createFont(this.plane15FontName, BaseFont.IDENTITY_V, BaseFont.EMBEDDED);
    		//log.debug("BaseFont plane15FontV: \n" + getBaseFontInfo(this.fontPlane15V));
    		return this.fontPlane15V;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //楷體水平排列 plane 15 字型, 無者傳回明體 plane 15 字型
    private BaseFont getKaiPlane15FontH() {
    	try {
    		if(this.kaiPlane15FontName == null)
    			return getPlane15FontH();
    		if(this.fontKaiPlane15H == null)
    			this.fontKaiPlane15H = BaseFont.createFont(this.kaiPlane15FontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		//log.debug("BaseFont kaiPlane15FontH: \n" + getBaseFontInfo(this.fontKaiPlane15H));
    		return this.fontKaiPlane15H;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //楷體垂直排列 plane 15 字型, 無者傳回明體 plane 15 字型
    private BaseFont getKaiPlane15FontV() {
    	try {
    		if(this.kaiPlane15FontName == null)
    			return getPlane15FontV();
    		if(this.fontKaiPlane15V == null)
    			this.fontKaiPlane15V = BaseFont.createFont(this.kaiPlane15FontName, BaseFont.IDENTITY_V, BaseFont.EMBEDDED);
    		//log.debug("BaseFont kaiPlane15FontV: \n" + getBaseFontInfo(this.fontKaiPlane15V));
    		return this.fontKaiPlane15V;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    /** 取當前頁高度(單位: mm) */
    public float getPageHeight() {
    	return mm(this.pageStyle.getHeight());
    }
    
    /** 取當前頁寬度(單位: mm) */
    public float getPageWidth() {
    	return mm(this.pageStyle.getWidth());
    }
    
    /** 取當前頁上邊界長度(單位: mm) */
    public float getMarginTop() {
    	return mm(this.marginTop);
    }
    
    /** 取當前頁下邊界長度(單位: mm) */
    public float getMarginBottom() {
    	return mm(this.marginBottom);
    }
    
    /** 取當前頁左邊界長度(單位: mm) */
    public float getMarginLeft() {
    	return mm(this.marginLeft);
    }
    
    /** 取當前頁右邊界長度(單位: mm) */
    public float getMarginRight() {
    	return mm(this.marginRight);
    }
    
    protected Image getWatermarkImage() {
    	return this.watermarkImage;
    }
    
    //@location { tlx, tly, width, height }. 為 null 時, 不改變圖形位置大小. 單位: pt, 坐標原點: 紙張左下角
    protected PdfGenerator setWatermark(final Image img, final float[] location) {
    	this.watermarkImage = img;
    	if(this.watermarkImage != null && location != null) {
	    	if(location[2] > 0 && location[3] > 0) //縮放圖形
	    		this.watermarkImage.scaleAbsolute(location[2], location[3]);
	    	this.watermarkImage.setAbsolutePosition(location[0], location[1] - this.watermarkImage.getPlainHeight()); //以圖左下角定位
    	}
    	return this;
    }
    
    /**
     * 指定浮水印圖型 (本函數呼叫後的頁面開始有作用). 單位: mm, 原點: 紙張左上角
     * @param imgPath 圖形檔實體路徑或 HTTP 網址(以 "http://" 開頭)
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     */
    public PdfGenerator setWatermark(final String imgPath, final float x, final float y) {
    	try {
    		return setWatermark(getImage(imgPath), new float[] { pt(x), transY(pt(y)), 0, 0 });
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    /**
     * 指定浮水印圖型 (本函數呼叫後的頁面開始有作用). 單位: mm, 原點: 紙張左上角
     * @param img 圖形檔內容來源
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     */
    public PdfGenerator setWatermark(final InputStream img, final float x, final float y) {
    	try {
    		return setWatermark(getImage(img), new float[] { pt(x), transY(pt(y)), 0, 0 });
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }

    /**
     * 指定浮水印圖型 (本函數呼叫後的頁面開始有作用). 單位: mm, 原點: 紙張左上角
     * @param imgPath 圖形檔實體路徑或 HTTP 網址(以 "http://" 開頭)
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @param width 貼圖後的圖寬 (mm)
     * @param height 貼圖後的圖高 (mm)
     */
    public PdfGenerator setWatermark(final String imgPath, final float x, final float y, final float width, final float height) {
        try {
        	return setWatermark(getImage(imgPath), new float[] { pt(x), transY(pt(y)), pt(width), pt(height) });
        } catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
   /**
    * 指定浮水印圖型 (本函數呼叫後的頁面開始有作用). 單位: mm, 原點: 紙張左上角
    * @param img 圖形檔內容來源
    * @param x 圖形左上角 x 坐標 (mm)
    * @param y 圖形左上角 y 坐標 (mm)
    * @param width 貼圖後的圖寬 (mm)
    * @param height 貼圖後的圖高 (mm)
    */
   public PdfGenerator setWatermark(final InputStream img, final float x, final float y, final float width, final float height) {
       try {
    	   return setWatermark(getImage(img), new float[] { pt(x), transY(pt(y)), pt(width), pt(height) });
       } catch(Throwable t) {
    	   throw new PdfException(t.getMessage(), t);
       }
   }
    
    protected Image getPerforationSeal() {
    	return this.perforationSealImage;
    }
    
    protected float[] getPerforationSealPosition() {
    	return this.perforationSealPosition;
    }
    
    //@location { width, height, tlx, tly, brx, bry }, 單位: pt, 坐標原點: 紙張左下角
    protected PdfGenerator setPerforationSeal(final Image img, final float[] location) {
    	this.perforationSealPosition = (location != null) ? location : (new float[] { 0, 0, 0, 0, 0, 0 });
    	this.perforationSealImage = img;
    	if(this.perforationSealImage != null && this.perforationSealPosition[0] > 0 && this.perforationSealPosition[1] > 0) //騎縫章伸縮至指定大小
    		this.perforationSealImage.scaleAbsolute(this.perforationSealPosition[0], this.perforationSealPosition[1]);
    	return this;
    }

    /**
     * 指定騎縫章圖型
     * @param imgPath 圖形檔實體路徑或 HTTP 網址(以 "http://" 開頭)
     */
    public PdfGenerator setPerforationSeal(final String imgPath) {
    	try {
    		return setPerforationSeal(getImage(imgPath), (float[])null);
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    /**
     * 指定騎縫章圖型
     * @param img 圖形檔來源 (呼叫者自行處理資源的關閉)
     */
    public PdfGenerator setPerforationSeal(final InputStream img) {
    	try {
    		return setPerforationSeal(getImage(img), (float[])null);
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    /**
     * 指定騎縫章圖型及其寬高(單位: mm)
     * @param imgPath 圖形檔實體路徑或 HTTP 網址(以 "http://" 開頭)
     * @param width
     * @param height
     */
    public PdfGenerator setPerforationSeal(final String imgPath, final float width, final float height) {
    	try {
    		return setPerforationSeal(getImage(imgPath), new float[] { pt(width), pt(height), 0, 0, 0, 0 });
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    /**
     * 指定騎縫章圖型及其寬高(單位: mm)
     * @param img 圖形檔來源 (呼叫者自行處理資源的關閉)
     * @param width
     * @param height
     */
    public PdfGenerator setPerforationSeal(final InputStream img, final float width, final float height) {
    	try {
    		return setPerforationSeal(getImage(img), new float[] { pt(width), pt(height), 0, 0, 0, 0 });
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    public boolean isPrintPerforationSealOnSingleSide() {
    	return this.printPerforationSealOnSingleSide;
    }
    
    /** 是否採雙面列印模式(從第 2 頁開始, 每頁只有一邊印出騎縫章) */
	public void setPrintPerforationSealOnSingleSide(boolean printPerforationSealOnSingleSide) {
		this.printPerforationSealOnSingleSide = printPerforationSealOnSingleSide;
	}
	
	public boolean isPrintPerforationSealOnTopDown() {
		return this.printPerforationSealOnTopDown;
	}

	/** 騎縫章是否印在每頁上下側(預設印在左右側) */
	public void setPrintPerforationSealOnTopDown(boolean printPerforationSealOnTopDown) {
		this.printPerforationSealOnTopDown = printPerforationSealOnTopDown;
	}
	
	public boolean isPrintPerforationSealOnFirstPageFromRight() {
		return printPerforationSealOnFirstPageFromRight;
	}

	/** 騎縫章蓋左右側且為單面列印模式時, 首頁是否蓋在右側(預設: false, 第 1 頁蓋在左側) */
	public void setPrintPerforationSealOnFirstPageFromRight(boolean printPerforationSealOnFirstPageFromRight) {
		this.printPerforationSealOnFirstPageFromRight = printPerforationSealOnFirstPageFromRight;
	}

	protected int getPageNumberPosition() {
		return this.pageNumberPosition;
	}

	/**
     * 指定頁碼位置
     * @param pageNumberPosition 可指定 
     * 		PdfConst.PAGE_NUMBER_NONE(不顯示頁碼, 預設值), 
     * 		PAGE_NUMBER_POSITION_TOP_CENTER, 
     * 		PAGE_NUMBER_POSITION_BOTTOM_CENTER
     * 		PAGE_NUMBER_POSITION_TOP_BOTH_SIDE (上方, 奇數頁靠右, 偶數頁靠左)
     * 		PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE (下方, 奇數頁靠右, 偶數頁靠左)
     * @see PdfConst#PAGE_NUMBER_NONE
     * @see PdfConst#PAGE_NUMBER_POSITION_TOP_CENTER
     * @see PdfConst#PAGE_NUMBER_POSITION_BOTTOM_CENTER
     * @see PdfConst#PAGE_NUMBER_POSITION_TOP_BOTH_SIDE
     * @see PdfConst#PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE
     */
	public void setPageNumberPosition(int pageNumberPosition) {
		this.pageNumberPosition = pageNumberPosition;
		if(pageNumberPosition == PdfConst.PAGE_NUMBER_NONE) {
			this.pgNumTopology = null;
			return;
		}
		
		this.pgNumTopology = new float[5]; //[ 字型大小,  x(固定位置時的 x 坐標), y(y 坐標值), x1(靠右的 x 坐標), x2(靠左時的 x 坐標) ]
		float fontSize = this.pgNumTopology[0] = 4;
		this.pgNumTopology[1] = getPageWidth() / 2;  //TODO: 暫時只直接由中央印起, 且未計入字串變長後的置中問題, 也未慮奇偶數頁靠左靠右的選項
		
    	if(pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_CENTER || //y 坐標值. TODO: 暫直接定在上下邊緣處, 距一字元的距離
    			pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_BOTH_SIDE ||
    			pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_LEFT ||
    			pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_RIGHT) {
    		this.pgNumTopology[2] = fontSize;
    	} else if(pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_CENTER ||
    			pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE ||
    			pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_LEFT ||
    			pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_RIGHT) {
    		this.pgNumTopology[2] = getPageHeight() - fontSize * 2;
    	}
    	
    	this.pgNumTopology[3] = getPageWidth() - fontSize * 10; //TODO: 假定一律左翻頁, 奇數頁頁碼靠右距邊界一字元的距離. 需在畫頁碼時, 即時算出頁碼字串長度才能得出真正的起始 x 坐標
    	this.pgNumTopology[4] = fontSize; //TODO: 假定一律左翻頁, 偶數頁靠左, 距邊界一字元的距離
	}

	/**
	 * 如果要印頁碼時, 第一頁是否也印出頁碼(預設 false)
	 */
	public void setPrintPageNumberOnFirstPage(boolean printPageNumberOnFirstPage) {
		this.printPageNumberOnFirstPage = printPageNumberOnFirstPage;
	}
	
	protected float[] getPgNumTopology() {
		return pgNumTopology;
	}

	protected void setPgNumTopology(float[] pgNumTopology) {
		this.pgNumTopology = pgNumTopology;
	}

	/** 設為 true 者, 文字區塊加框以供目視除錯 */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public boolean isDebug() {
        return this.debug;
    }

    /** 按當前頁同版面起新頁 */
    public PdfGenerator newPage() {
        return newPage(this.pageStyle1);
    }
    
    /**
     * 起新頁並指定新的紙張樣式, 但沿用上一頁或預設的四邊界寬值.
     * @param pageSize 紙張樣式, 可為: 
     * 		PdfConst.PAGE_A3, PdfConst.PAGE_A4, PdfConst.PAGE_B3, PdfConst.PAGE_B4, PdfConst.PAGE_A3_LANDSCAPE, 
     * 		PdfConst.PAGE_A4_LANDSCAPE, PdfConst.PAGE_B3_LANDSCAPE, PdfConst.PAGE_B4_LANDSCAPE 之一
     * @see PdfConst#PAGE_A3
     * @see PdfConst#PAGE_A4
     * @see PdfConst#PAGE_B3
     * @see PdfConst#PAGE_B4
     * @see PdfConst#PAGE_A3_LANDSCAPE
     * @see PdfConst#PAGE_A4_LANDSCAPE
     * @see PdfConst#PAGE_B3_LANDSCAPE
     * @see PdfConst#PAGE_B4_LANDSCAPE
     */
    PdfGenerator newPage(int pageSize) {
        return newPage(pageSize, -1F, -1F, -1F, -1F);
    }

    /**
     * 起新頁並指定新紙張樣式, 並自行指定四邊界值.
     * @param pageSize 紙張樣式, 可為: 
     * 		PdfConst.PAGE_A3, PdfConst.PAGE_A4, PdfConst.PAGE_B3, PdfConst.PAGE_B4, PdfConst.PAGE_A3_LANDSCAPE, 
     * 		PdfConst.PAGE_A4_LANDSCAPE, PdfConst.PAGE_B3_LANDSCAPE, PdfConst.PAGE_B4_LANDSCAPE 之一
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @see PdfConst#PAGE_A3
     * @see PdfConst#PAGE_A4
     * @see PdfConst#PAGE_B3
     * @see PdfConst#PAGE_B4
     * @see PdfConst#PAGE_A3_LANDSCAPE
     * @see PdfConst#PAGE_A4_LANDSCAPE
     * @see PdfConst#PAGE_B3_LANDSCAPE
     * @see PdfConst#PAGE_B4_LANDSCAPE
     */
    public PdfGenerator newPage(final int pageSize, final float marginLeft, final float marginRight, 
    		final float marginTop, final float marginBottom) {
        try {
        	//貼騎縫章(在換 document 頁前蓋印, 以便控首頁末頁的蓋印與否)
    		drawPerforationSeal();
        	
            if(this.pageStyle1 != pageSize) { //改變新頁的大小
                this.pageStyle1 = pageSize;
                
                if(pageSize == PdfConst.PAGE_A3)
                	this.pageStyle = PageSize.A3;
                else if(pageSize == PdfConst.PAGE_A3_LANDSCAPE)
                	this.pageStyle = PageSize.A3.rotate();
                else if(pageSize == PdfConst.PAGE_A4)
                	this.pageStyle = PageSize.A4;
                else if(pageSize == PdfConst.PAGE_A4_LANDSCAPE)
                	this.pageStyle = PageSize.A4.rotate();
                else if(pageSize == PdfConst.PAGE_B3)
                	this.pageStyle = PageSize.B3;
                else if(pageSize == PdfConst.PAGE_B3_LANDSCAPE)
                	this.pageStyle = PageSize.B3.rotate();
                else if(pageSize == PdfConst.PAGE_B4)
                	this.pageStyle = PageSize.B4;
                else if(pageSize == PdfConst.PAGE_B4_LANDSCAPE)
                	this.pageStyle = PageSize.B4.rotate();
                else
                	this.pageStyle = PageSize.A4;
                
                this.document.setPageSize(this.pageStyle); //新頁才發生作用
                if(marginLeft >= 0 && marginRight >= 0 && marginTop >= 0 && marginBottom >= 0) {
                    this.marginLeft = pt(marginLeft);
                    this.marginRight = pt(marginRight);
                    this.marginTop = pt(marginTop);
                    this.marginBottom = pt(marginBottom);
                }
                this.document.setMargins(this.marginLeft, this.marginRight, this.marginTop, this.marginBottom);
            }
            this.document.newPage();
            this.pageCount++;
            this.totalPages++;

            //貼浮水印
            if(this.watermarkImage != null)
                this.cb.addImage(this.watermarkImage);

            //標自訂格式頁碼
    		if(this.pageNoHandler != null && getTotalPages() != 0)
    			this.pageNoHandler.newPage(this.cb, getTotalPages());
    		
            //印頁碼
        	drawPageNumber();
            
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }
    
    /**
     * 起任意長寬的新頁, 沿用上一頁或預設的四邊界寬值.
     * @param pageWidth 頁寬(單位: mm)
     * @param pageHeight 頁高(單位: mm)
     * @throws PdfException
     */
    public PdfGenerator newPage(float pageWidth, float pageHeight) {
    	return newPage(pageWidth, pageHeight, -1F, -1F, -1F, -1F);
    }
    
    /**
     * 起任意長寬的新頁, 並自行指定四邊界值.
     * @param pageWidth 頁寬(單位: mm)
     * @param pageHeight 頁高(單位: mm)
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @throws PdfException
     */
    public PdfGenerator newPage(final float pageWidth, final float pageHeight, final float marginLeft, 
    		final float marginRight, final float marginTop, final float marginBottom) {
        try {
        	//貼騎縫章(在換 document 頁前蓋印, 以便控首頁末頁的蓋印與否)
            drawPerforationSeal();
            
            this.pageStyle1 = PdfConst.PAGE_USERDEFINED;
            this.pageStyle = new Rectangle(pt(pageWidth), pt(pageHeight));
            this.document.setPageSize(this.pageStyle); //新頁後才發生作用
            if(marginLeft >= 0 && marginRight >= 0 && marginTop >= 0 && marginBottom >= 0) {
                this.marginLeft = pt(marginLeft);
                this.marginRight = pt(marginRight);
                this.marginTop = pt(marginTop);
                this.marginBottom = pt(marginBottom);
            }
            this.document.setMargins(this.marginLeft, this.marginRight, this.marginTop, this.marginBottom);
            this.document.newPage();
            this.pageCount++;
            this.totalPages++;

            //貼浮水印
            if(this.watermarkImage != null)
                this.cb.addImage(this.watermarkImage);

            //標自訂格式頁碼
    		if(this.pageNoHandler != null && getTotalPages() != 0)
    			this.pageNoHandler.newPage(this.cb, getTotalPages());
    		
            //印頁碼
            drawPageNumber();
            
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }

    /**
     * 畫線, 預設型式: 實線, 黑色, 線寬 2.2mm .
     * 單位: mm
     * @param xStart 線段起點 x 坐標
     * @param yStart 線段起點 y 坐標
     * @param xEnd 線段終點 x 坐標
     * @param yEnd 線段終點 y 坐標
     */
    public PdfGenerator drawLine(float xStart, float yStart, float xEnd, float yEnd) {
        return drawLine(xStart, yStart, xEnd, yEnd, 2.2F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
    }

    /**
     * 畫線(單位: mm).
     * @param xStart 線段起點 x 坐標
     * @param yStart 線段起點 y 坐標
     * @param xEnd 線段終點 x 坐標
     * @param yEnd 線段終點 y 坐標
     * @param lineWidth 線段寬度
     * @param lineStyle 線段型式(PdfConst.LINE_SOLID, PdfConst.LINE_DASH, PdfConst.LINE_DOT)
     * @param lineColor 顏色(自訂 RGB 值或下列之一: PdfConst.COLOR_BLACK, PdfConst.COLOR_WHITE, PdfConst.COLOR_RED, PdfConst.COLOR_GREEN, PdfConst.COLOR_BLUE)
     */
    public PdfGenerator drawLine(float xStart, float yStart, float xEnd, float yEnd, float lineWidth, int lineStyle, int[] lineColor) {
        try {
        	this.cb.saveState(); //改變線條樣式前先記下當前狀態
            prepareDrawLine(tuneLineWidth(lineWidth), lineStyle, lineColor); //line width: 經驗值
            this.cb.moveTo(pt(xStart), transY(pt(yStart))); //line start
            
            float x2 = pt(xEnd), y2 = transY(pt(yEnd));
            this.cb.lineTo(x2, y2); //draw to line-end
            if(lineStyle != PdfConst.LINE_SOLID) { //當線型為實線時, prepareDrawLine() 裡不呼叫 cb.setLineDash() 指定線型
            	//WORKAROUND: Chrome 內建的 PDF viewer 在列印時, 似乎具有某種最佳化機制而產生副作用:
            	//當前線段 a 有呼叫 cb.setLineDash() 指定線型且已呼叫 cb.stroke() 後,
                //如果稍後又立刻再畫其他線段(也有呼叫 cb.setLineDash() 指定線型, 且又呼叫 cb.stroke()),
            	//最後產生的 PDF 檔中的此線段 a, 在 Chrome PDF viewer 送印時, 將消失或截斷.
            	//在此以在線段 a 後再畫一條長度為 0 的線段來躲避這種現象
            	this.cb.lineTo(x2, y2);  
            }
            
            this.cb.stroke();
            this.cb.restoreState(); //恢復改變線條樣式之前的狀態
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }

    /**
     * 畫框(單位: mm), 預設型式: 實線, 黑色, 線寬 2.2mm .
     * @param x 框左上角 x 坐標
     * @param y 框左上角 y 坐標
     * @param width 寬
     * @param height 高
     */
    public PdfGenerator drawRect(float x, float y, float width, float height) {
        return drawRect(x, y, width, height, 2.2F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
    }

    /**
     * 畫框(單位: mm).
     * @param x 框左上角 x 坐標
     * @param y 框左上角 y 坐標
     * @param width 寬
     * @param height 高
     * @param lineWidth 線寬
     * @param lineStyle 線段型式(PdfConst.LINE_SOLID, PdfConst.LINE_DASH, PdfConst.LINE_DOT)
     * @param lineColor 顏色(自訂 RGB 值或下列之一: PdfConst.COLOR_BLACK, PdfConst.COLOR_WHITE, PdfConst.COLOR_RED, PdfConst.COLOR_GREEN, PdfConst.COLOR_BLUE)
     */
    public PdfGenerator drawRect(float x, float y, float width, float height, float lineWidth, int lineStyle, int[] lineColor) {
        try {
        	this.cb.saveState(); //改變線條樣式前先記下當前狀態
            prepareDrawLine(tuneLineWidth(lineWidth), lineStyle, lineColor); //line width: 經驗值
            this.cb.rectangle(pt(x), this.pageStyle.getHeight() - pt(y) - pt(height), pt(width), pt(height));
            this.cb.stroke();
            this.cb.restoreState(); //恢復改變線條樣式之前的狀態
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }

    /**
     * 畫圓, 預設型式: 實線, 黑色, 線寬 2.2mm .
     * @param x 框左上角 x 坐標
     * @param y 框左上角 y 坐標
     * @param radius 半徑
     */
    public PdfGenerator drawCircle(float x, float y, float radius) {
        return drawCircle(x, y, radius, 2.2F, PdfConst.LINE_SOLID, PdfConst.COLOR_BLACK);
    }

    /**
     * 畫圓.
     * @param x 框左上角 x 坐標
     * @param y 框左上角 y 坐標
     * @param radius 半徑
     * @param lineWidth 線寬
     * @param lineStyle 線段型式(LINE_SOLID, LINE_DASH, LINE_DOT)
     * @param lineColor 顏色(COLOR_BLACK, COLOR_WHITE, COLOR_RED, COLOR_GREEN, COLOR_BLUE)
     */
    public PdfGenerator drawCircle(float x, float y, float radius, float lineWidth, int lineStyle, int[] lineColor) {
        try {
        	this.cb.saveState(); //改變線條樣式前先記下當前狀態
            prepareDrawLine(tuneLineWidth(lineWidth), lineStyle, lineColor); //line width: 經驗值
            this.cb.circle(pt(x), pt(y), pt(radius));
            this.cb.stroke();
            this.cb.restoreState(); //恢復改變線條樣式之前的狀態
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }

    /**
     * 寫字, 直到字串結束為止, 至頁面右邊界後折行, 過了頁底自動換頁, 從最頂端繼續顯示(x 坐標仍相同).
     * @param text 文字
     * @param fontSize 字型大小(mm)
     * @param x 文字區左上角 x 坐標(mm)
     * @param y 文字區左上角 y 坐標(mm)
     * @return 印完最後一行時的文字最後字元下緣的坐標(mm)
     */
    public float[] drawTextFlow(String text, float fontSize, float x, float y) {
        return drawTextFlow(text, fontSize, x, y, 0F,
                mm(this.pageStyle.getWidth() - this.marginRight) - x, 
                PdfConst.FONT_MING, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLACK);
    }

    /**
     * 寫字, 直到字串結束為止, 至頁面右邊界後折行, 第二行後可縮排, 過了頁底自動換頁, 從最頂端繼續顯示(x 坐標仍相同).
     * @param text 文字
     * @param fontSize 字型大小(mm)
     * @param x 文字區左上角 x 坐標(mm)
     * @param y 文字區左上角 y 坐標(mm)
     * @param indent 折行後內縮的寬度
     * @return 印完最後一行時的文字最後字元下緣的坐標(mm)
     */
    public float[] drawTextFlow(String text, float fontSize, float x, float y, float indent) {
        return drawTextFlow(text, fontSize, x, y, indent,
                mm(this.pageStyle.getWidth() - this.marginRight) - x, 
                PdfConst.FONT_MING, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLACK);
    }
    
    /**
     * 寫字, 直到字串結束為止, 至區間邊界後折行, 第二行後可縮排, 過了頁底自動換頁, 從最頂端繼續顯示(x 坐標仍相同).
     * 換頁等動作的狀態改變可藉 status 參數得知.
     * @param text 文字
     * @param fontSize 字型大小(mm)
     * @param x 文字區左上角 x 坐標(mm)
     * @param y 文字區左上角 y 坐標(mm)
     * @param indent 折行後內縮的寬度
     * @param boundWidth 文字區間寬(mm)
     * @param fontType 字型, 可為 PdfConst.FONT_MING, PdfConst.FONT_KAI
     * @param fontStyle 文字樣式, 可為: PdfConst.FONT_STYLE_BOLD, PdfConst.FONT_STYLE_BOLDITALIC, PdfConst.FONT_STYLE_ITALIC, 
     *        PdfConst.FONT_STYLE_NORMAL, PdfConst.FONT_STYLE_STRIKETHRU, PdfConst.FONT_STYLE_UNDERLINE 之一
     * @param color 文字顏色, 可自行指定 RGB 值(16進位的整數之陣列), 或指定: 
     * 		  PdfConst.COLOR_BLACK, PdfConst.COLOR_WHITE, PdfConst.COLOR_RED, PdfConst.COLOR_GREEN, PdfConst.COLOR_BLUE 之一
     * @return 印完最後一行時的文字最後字元下緣的坐標(mm)
     * @see PdfConst#FONT_MING
     * @see PdfConst#FONT_KAI
     * @see PdfConst#FONT_STYLE_BOLD
     * @see PdfConst#FONT_STYLE_BOLDITALIC
     * @see PdfConst#FONT_STYLE_ITALIC
     * @see PdfConst#FONT_STYLE_NORMAL
     * @see PdfConst#FONT_STYLE_STRIKETHRU
     * @see PdfConst#FONT_STYLE_UNDERLINE
     * @see PdfConst#COLOR_BLACK
     * @see PdfConst#COLOR_WHITE
     * @see PdfConst#COLOR_RED
     * @see PdfConst#COLOR_GREEN
     * @see PdfConst#COLOR_BLUE
     */
    public float[] drawTextFlow(final String text, final float fontSize, final float x, final float y, final float indent, 
    		final float boundWidth, final int fontType, final int fontStyle, final int[] color) {
    	return drawTextFlow(text, fontSize, x, y, indent, boundWidth, fontType, fontStyle, color, null);
    }
    
    /**
     * 寫字, 直到字串結束為止, 至區間邊界後折行, 第二行後可縮排, 過了下邊界自動換頁, 從次頁上邊界繼續顯示(x 坐標仍相同).
     * 換頁等動作的狀態改變可藉 status 參數得知.
     * @param text 文字
     * @param fontSize 字型大小(mm)
     * @param x 文字區左上角 x 坐標(mm)
     * @param y 文字區左上角 y 坐標(mm)
     * @param indent 折行後內縮的寬度
     * @param boundWidth 文字區間寬(mm)
     * @param marginTop 上邊界長度(mm)
     * @param marginBottom 下邊界長度(mm)
     * @param fontType 字型, 可為 PdfConst.FONT_MING, PdfConst.FONT_KAI
     * @param fontStyle 文字樣式, 可為: PdfConst.FONT_STYLE_BOLD, PdfConst.FONT_STYLE_BOLDITALIC, PdfConst.FONT_STYLE_ITALIC, 
     *        PdfConst.FONT_STYLE_NORMAL, PdfConst.FONT_STYLE_STRIKETHRU, PdfConst.FONT_STYLE_UNDERLINE 之一
     * @param lineGap 多列文字列之間距(mm)
     * @param alignment 文字排列方式, 可指定:
     * 		  PdfConst.ALIGNMENT_LEFT, PdfConst.ALIGNMENT_CENTER, PdfConst.ALIGNMENT_RIGHT
     * @param color 文字顏色, 可自行指定 RGB 值(16進位的整數之陣列), 或指定: 
     * 		  PdfConst.COLOR_BLACK, PdfConst.COLOR_WHITE, PdfConst.COLOR_RED, PdfConst.COLOR_GREEN, PdfConst.COLOR_BLUE 之一
     * @param callback 在發生換頁之際, 允許呼叫者做額外的處理
     * @return 印完最後一行最後一字元時的文字下緣的坐標(mm)
     * @see PdfConst#FONT_MING
     * @see PdfConst#FONT_KAI
     * @see PdfConst#FONT_STYLE_BOLD
     * @see PdfConst#FONT_STYLE_BOLDITALIC
     * @see PdfConst#FONT_STYLE_ITALIC
     * @see PdfConst#FONT_STYLE_NORMAL
     * @see PdfConst#FONT_STYLE_STRIKETHRU
     * @see PdfConst#FONT_STYLE_UNDERLINE
     * @see PdfConst#COLOR_BLACK
     * @see PdfConst#COLOR_WHITE
     * @see PdfConst#COLOR_RED
     * @see PdfConst#COLOR_GREEN
     * @see PdfConst#COLOR_BLUE
     * @see PdfConst#ALIGNMENT_LEFT
     * @see PdfConst#ALIGNMENT_CENTER
     * @see PdfConst#ALIGNMENT_RIGHT
     */
    public float[] drawTextFlow(final String text, final float fontSize, final float x, final float y, final float indent, 
    		final float boundWidth, final float marginTop, final float marginBottom, final int fontType, final int fontStyle, 
    		final float lineGap, final int alignment, final int[] color, final PdfGeneratorOnNewPage callback) {
        try {
        	{
        		float pw = mm(this.pageStyle.getWidth());
        		float ph = mm(this.pageStyle.getHeight());
	        	if(boundWidth <= 0) {
	        		log.warn("param boundWidth=" + boundWidth + " <= 0");
	        		return new float[] { x, y };
	        	}
	        	if((pw - fontSize) < x) {
	        		log.warn("param x=" + x + " too large to fit texts (fontSize=" + fontSize + ")");
	        		return new float[] { x, y };
	        	}
	        	if((ph - marginTop - marginBottom) < fontSize) {
	        		log.warn("param marginTop=" + marginTop + " plus marginBottom=" + marginBottom + " too large to fit texts (fontSize=" + fontSize + ")");
	        		return new float[] { x, y };
	        	}
	        	if((boundWidth - indent) < fontSize) {
	        		log.warn("param indent=" + indent + " too large to fit texts (boundWidth=" + boundWidth + ", fontSize=" + fontSize + ")");
	        		return new float[] { x, y };
	        	}
        	}
        	
            checkPageReady();
            float fs = pt(tuneFontSize(fontSize)); //font size (單位: pt)
            
            ColumnText ct = new ColumnText(this.cb);
            addTextIntoColumnText(ct, text, fontType, fs, fontStyle, color);
            ct.setLeading(fs + pt(lineGap)); //NOTE: 第一行上方也會出現間距, 所以實際文字框得上移 lineGap (還要注意 debug 用框的位置則不上移)
            
            if(alignment != PdfConst.ALIGNMENT_LEFT) //文字排列方式
            	ct.setAlignment(alignment);
            
            float llx = pt(x); //(llx, lly) 單行文字框左下角之坐標
            float lly = transY(pt(y)) - ct.getLeading() + pt(lineGap); //上移 lineGap 以抵銷第一行之上的行間距
            float urx = llx + pt(boundWidth); //單行文字框右上角 x 坐標
            float marginTop1 = pt(marginTop); //pt
            float marginBottom1 = pt(marginBottom); //pt
            
            drawTextFlowBoundForDebug(x, y, x + boundWidth, y + fontSize, color, true); //方便追蹤文字左右邊界
            
            //第一行
            if(lly < marginBottom1) { //第一行底若超出下邊界者, 需換頁
            	if(PdfGeneratorOnNewPage.SUPPRESS_NEW_PAGE != executeBeforeNewPageForDrawTextFlow(callback))
            		newPage();
            	executeAfterNewPageForDrawTextFlow(callback);
            	//drawTextFlowBoundForDebug(x, y, x + boundWidth, y + fontSize + lineGap, color, false); //方便追蹤文字左右邊界
            	ct.setSimpleColumn(llx, this.pageStyle.getHeight() - marginTop1 - ct.getLeading() + pt(lineGap), urx, this.pageStyle.getHeight() - marginTop1 + pt(lineGap) + 3); //WORKAROUND: 欄高 +3 確保欄位能容下單列文字
            } else {
            	ct.setSimpleColumn(llx, lly, urx, lly + ct.getLeading() + 3); //WORKAROUND: 欄高 +3 確保欄位能容下單列文字
            }
            
            //第二行及其後, 一次畫一行文字
            llx += pt(indent); //內縮
            while(ColumnText.hasMoreText(ct.go())) {
            	if((ct.getYLine() - marginBottom1) < ct.getLeading()) { //新行之框底已超過下邊界, 需換頁
            		if(PdfGeneratorOnNewPage.SUPPRESS_NEW_PAGE != executeBeforeNewPageForDrawTextFlow(callback))
            			newPage();
            		executeAfterNewPageForDrawTextFlow(callback);
            		drawTextFlowBoundForDebug(x, mm(atransY(this.pageStyle.getHeight() - marginTop1)), x + boundWidth, mm(atransY(this.pageStyle.getHeight() - marginTop1 - ct.getLeading())), color, false); //方便追蹤文字左右邊界
            		ct.setSimpleColumn(llx, this.pageStyle.getHeight() - marginTop1 - ct.getLeading() + pt(lineGap), urx, this.pageStyle.getHeight() - marginTop1 + pt(lineGap) + 3); //WORKAROUND: 欄高 +3 確保欄位能容下單列文字
            	} else {
            		drawTextFlowBoundForDebug(x, mm(atransY(ct.getYLine())), x + boundWidth, mm(atransY(ct.getYLine() - ct.getLeading())), color, false); //方便追蹤文字左右邊界
            		ct.setSimpleColumn(llx, ct.getYLine() - ct.getLeading(), urx, ct.getYLine() + 3); //WORKAROUND: 欄高 +3 確保欄位能容下單列文字
            	}
            }
            
            if(isDebug())
            	drawLine(x, mm(atransY(ct.getYLine())), x + boundWidth, mm(atransY(ct.getYLine())), 0.5F, PdfConst.LINE_DOT, color);
            return new float[] { mm(ct.getLastX()),  mm(atransY(ct.getYLine())) };
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }
    
    /**
     * 寫字, 直到字串結束為止, 至區間邊界後折行, 第二行後可縮排, 過了頁底自動換頁, 從最頂端繼續顯示(x 坐標仍相同).
     * 換頁等動作的狀態改變可藉 status 參數得知.
     * @param text 文字
     * @param fontSize 字型大小(mm)
     * @param x 文字區左上角 x 坐標(mm)
     * @param y 文字區左上角 y 坐標(mm)
     * @param indent 折行後內縮的寬度
     * @param boundWidth 文字區間寬(mm)
     * @param fontType 字型, 可為 PdfConst.FONT_MING, PdfConst.FONT_KAI
     * @param fontStyle 文字樣式, 可為: PdfConst.FONT_STYLE_BOLD, PdfConst.FONT_STYLE_BOLDITALIC, PdfConst.FONT_STYLE_ITALIC, 
     *        PdfConst.FONT_STYLE_NORMAL, PdfConst.FONT_STYLE_STRIKETHRU, PdfConst.FONT_STYLE_UNDERLINE 之一
     * @param color 文字顏色, 可自行指定 RGB 值(16進位的整數之陣列), 或指定: 
     * 		  PdfConst.COLOR_BLACK, PdfConst.COLOR_WHITE, PdfConst.COLOR_RED, PdfConst.COLOR_GREEN, PdfConst.COLOR_BLUE 之一
     * @param callback 在發生換頁之際, 允許呼叫者做額外的處理
     * @return 印完最後一行文字最後字元下緣的坐標(mm)
     * @see PdfConst#FONT_MING
     * @see PdfConst#FONT_KAI
     * @see PdfConst#FONT_STYLE_BOLD
     * @see PdfConst#FONT_STYLE_BOLDITALIC
     * @see PdfConst#FONT_STYLE_ITALIC
     * @see PdfConst#FONT_STYLE_NORMAL
     * @see PdfConst#FONT_STYLE_STRIKETHRU
     * @see PdfConst#FONT_STYLE_UNDERLINE
     * @see PdfConst#COLOR_BLACK
     * @see PdfConst#COLOR_WHITE
     * @see PdfConst#COLOR_RED
     * @see PdfConst#COLOR_GREEN
     * @see PdfConst#COLOR_BLUE
     */
    public float[] drawTextFlow(final String text, final float fontSize, final float x, final float y, final float indent, 
    		final float boundWidth, final int fontType, final int fontStyle, final int[] color, final PdfGeneratorOnNewPage callback) {
    	return drawTextFlow(text, fontSize, x, y, indent, boundWidth, 
    			getMarginTop(), getMarginBottom(), fontType, fontStyle, DEFAULT_ROW_GAP, PdfConst.ALIGNMENT_LEFT, color, callback);
    }
    
    /**
     * 在文字框填入文字 (單位: mm).
     * @param text
     * @param fontSize 字型高
     * @param x 文字框左上角 x 坐標 
     * @param y 文字框左上角 y 坐標
     * @param boundWidth 文字框寬(0 者, 使用紙張寬減去 x 值)
     * @param boundHeight 文字框高(0 者, 使用紙張高減去 y 值)
     * @param rowGap 連續行之上行字元底部, 與下行字元頂部的距離(只在 distributed=PdfConst.XPRINT_DISTRIBUTED_DEFAULT 才有作用)
     * @param charSpacing 字元間距(只在 distributed=PdfConst.XPRINT_DISTRIBUTED_DEFAULT 才有作用)
     * @param textDirection 文字排列方向, 可為 PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY, PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY,
     * 	      PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY, PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY
     * @param distributed 文字排列方式, 依大同 Xprint 前端列印軟體所定義的值 
     * @param fontType 字型, 可為 PdfConst.FONT_MING, PdfConst.FONT_KAI
     * @param fontStyle 文字樣式, 可為: PdfConst.FONT_STYLE_BOLD, PdfConst.FONT_STYLE_BOLDITALIC, PdfConst.FONT_STYLE_ITALIC, 
     *        PdfConst.FONT_STYLE_NORMAL, PdfConst.FONT_STYLE_STRIKETHRU, PdfConst.FONT_STYLE_UNDERLINE 之一
     * @param color 文字顏色, 可自行指定 RGB 值(16進位的整數之陣列), 或指定: PdfConst.COLOR_BLACK, PdfConst.COLOR_WHITE, PdfConst.COLOR_RED, PdfConst.COLOR_GREEN, PdfConst.COLOR_BLUE 之一
     * @throws PdfException
     * @see PdfConst#FONT_MING
     * @see PdfConst#FONT_KAI
     * @see PdfConst#FONT_STYLE_BOLD
     * @see PdfConst#FONT_STYLE_BOLDITALIC
     * @see PdfConst#FONT_STYLE_ITALIC
     * @see PdfConst#FONT_STYLE_NORMAL
     * @see PdfConst#FONT_STYLE_STRIKETHRU
     * @see PdfConst#FONT_STYLE_UNDERLINE
     * @see PdfConst#COLOR_BLACK
     * @see PdfConst#COLOR_WHITE
     * @see PdfConst#COLOR_RED
     * @see PdfConst#COLOR_GREEN
     * @see PdfConst#COLOR_BLUE
     */
    public PdfGenerator drawTextBox(final String text, final float fontSize, final float x, final float y, 
    		final float boundWidth, final float boundHeight, final float rowGap, final float charSpacing, 
    		final int textDirection, final int distributed, final int fontType, final int fontStyle, 
    		final int[] color) {
        try {
            checkPageReady();
            
            float bw = boundWidth, bh = boundHeight; //unit: mm
            if(boundWidth == 0F)
            	bw = mm(this.pageStyle.getWidth()) - x;
            if(boundHeight == 0F)
            	bh = mm(this.pageStyle.getHeight()) - y;
            if(isDebug()) //方便追蹤文字框
                drawRect(x, y, bw, bh, 0.5F, PdfConst.LINE_DOT, color);
            if(text == null || text.length() == 0)
        		return this;

            final float fs = pt(fontSize);
            final float xl = pt(x);
            final float xr = pt(x + bw); //框右側 x 坐標值(pt)
            final float yt = transY(pt(y));
            final float yb = transY(pt(y + bh)); //框底 y 坐標值(pt)
    
            //分直書, 橫書兩種排列方式
            if(textDirection == PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY || //直書
            		textDirection == PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY) { //TODO: 暫不支援直書向右排列
            	final int MAX_LINES = 10000;
            	final float height = pt(bh);
            	VerticalText vt = new VerticalText(this.cb);
            	
            	//先模擬(務必保證全部文字都有畫完)
            	addTextIntoVerticalText(vt, text, fontType, fs, fontStyle, color); //以全型字高當成連續行文字 leading
            	vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs);
            	final int status = vt.go(true); 
            	final float lastX = vt.getOriginX();
            	final int lines = lines(xr - lastX, fs);
            	
            	addTextIntoVerticalText(vt, text, fontType, fs, fontStyle, color);
            	if(lines == 1) { //代表本段文字為單行
            		//垂直均佈/垂直置中/靠上(預設)
            		if(distributed == PdfConst.XPRINT_DISTRIBUTED_4 || distributed == PdfConst.XPRINT_DISTRIBUTED_5 || 
            				distributed == PdfConst.XPRINT_DISTRIBUTED_7 || distributed == PdfConst.XPRINT_DISTRIBUTED_2) {
        				vt.setAlignment(Element.ALIGN_CENTER); //TODO: XPRINT_DISTRIBUTED_2 時本應垂直均佈, 但 Element.ALIGN_JUSTIFIED_ALL 無效
            		}
            		
            		//靠左/置中/均佈/靠右(預設)
            		if(textDirection == PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY) {
            			if(distributed == PdfConst.XPRINT_DISTRIBUTED_2 || distributed == PdfConst.XPRINT_DISTRIBUTED_4 || distributed == PdfConst.XPRINT_DISTRIBUTED_6) //水平置中
        					vt.setVerticalLayout((xr - (lastX - xl) / 2), yt, height, MAX_LINES, fs);
        				else if(distributed == PdfConst.XPRINT_DISTRIBUTED_5) //靠右
        					vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs);
        				else //靠左
        					vt.setVerticalLayout(xr - (lastX - xl), yt, height, MAX_LINES, fs);
            		} else {
	            		if(distributed == PdfConst.XPRINT_DISTRIBUTED_7) //要靠左
	        				vt.setVerticalLayout(xr - (lastX - xl), yt, height, MAX_LINES, fs);
	        			else if(distributed == PdfConst.XPRINT_DISTRIBUTED_2 || distributed == PdfConst.XPRINT_DISTRIBUTED_4) //水平置中
	        				vt.setVerticalLayout((xr - (lastX - xl) / 2), yt, height, MAX_LINES, fs);
	        			else //靠右
	        				vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs);
            		}
            	} else {
            		if(status != VerticalText.NO_MORE_TEXT && status != VerticalText.NO_MORE_COLUMN) { //未全部印出, 表示沒多餘空間排版, 直接印出了
            			vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs);
            		} else { //靠左/置中/靠右/均佈
            			if(distributed == PdfConst.XPRINT_DISTRIBUTED_2) { //水平均佈, 首尾行之外與框之間有間隙
            				float gap = (xr - xl - lines * fs) / (lines + 1); //from: lines * (gap + fs) + gap = xr - xl
            				vt.setVerticalLayout(xr - gap, yt, height, MAX_LINES, fs + gap);
        				} else if(distributed == PdfConst.XPRINT_DISTRIBUTED_6) { //水平均佈, 邊界對齊
        					float gap = (xr - xl - fs) / (lines - 1) - fs; //from: (fs + gap) * (lines - 1) + fs = xr - xl
        					vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs + gap);
        				} else { //其他非均佈, 分自右向右換行/自左向右換行
        					if(textDirection == PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY) {
                				if(distributed == PdfConst.XPRINT_DISTRIBUTED_4) //水平置中
                					vt.setVerticalLayout((xr - (lastX - xl) / 2), yt, height, MAX_LINES, fs);
                				else if(distributed == PdfConst.XPRINT_DISTRIBUTED_5) //靠右
                					vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs);
                				else //靠左
                					vt.setVerticalLayout(xr - (lastX - xl), yt, height, MAX_LINES, fs);
                			} else {
    	            			if(distributed == PdfConst.XPRINT_DISTRIBUTED_7) //靠左
    	            				vt.setVerticalLayout(xr - (lastX - xl), yt, height, MAX_LINES, fs);
                				else if(distributed == PdfConst.XPRINT_DISTRIBUTED_4) //水平置中
    	            				vt.setVerticalLayout((xr - (lastX - xl) / 2), yt, height, MAX_LINES, fs);
                				else //靠右
    	            				vt.setVerticalLayout(xr, yt, height, MAX_LINES, fs);
                			}
        				}
            		}
            	}
            	vt.go();
            } else { //預設橫書
            	final float gapr = (distributed == PdfConst.XPRINT_DISTRIBUTED_DEFAULT) ? pt(rowGap) : 0F;
            	String text2 = text; //文字可能反向排列
            	if(textDirection == PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY) //把輸入文倒排
            		text2 = reverse(text);
            	
            	final float leading = (distributed == PdfConst.XPRINT_DISTRIBUTED_DEFAULT) ? pt(rowGap + fontSize) : pt(fontSize);
            	final float spacing = (distributed == PdfConst.XPRINT_DISTRIBUTED_DEFAULT) ? pt(charSpacing) : 0F;
	            ColumnText ct = new ColumnText(this.cb);
	            addTextIntoColumnText(ct, text, fontType, fs, spacing, fontStyle, color);
	            ct.setSimpleColumn(xl, yb, xr, yt + gapr);
	            ct.setLeading(leading);
	            ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
	        	
	            if(distributed == PdfConst.XPRINT_DISTRIBUTED_DEFAULT || //0
	            		distributed == PdfConst.XPRINT_DISTRIBUTED_2 || //只對垂直排列文字有作用 
	            		distributed == PdfConst.XPRINT_DISTRIBUTED_4) { //只對垂直排列文字有作用
	            	if(textDirection == PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY) { //由右至左的文字, 預設靠右
	            		//先試畫看行數
		            	ct.go(true);
		            	int lines = ct.getLinesWritten();
		            	
		            	ct.setText(null);
		            	addTextIntoColumnText(ct, (lines == 1) ? text2 : text, fontType, fs, spacing, fontStyle, color); //TODO: 暫不支援多行自右向左排列文字
	    	            ct.setSimpleColumn(xl, yb, xr, yt + gapr);
	    	            ct.setLeading(leading);
	    	            ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
		            	if(lines == 1) //TODO: 因不支援多行自右向左排列文字, 所以多行時仍然靠左
		            		ct.setAlignment(Element.ALIGN_RIGHT); //單行時靠右
	            	}
	            } else { //distribute=1, 3, 5, 6, 7 時
	            	//先以預設版面模擬
	            	int status = ct.go(true);
	            	if(ColumnText.hasMoreText(status)) { //表示沒空間排版花樣了, 直接以預設樣式印出了
	            		ct.setText(null);
	            		addTextIntoColumnText(ct, text, fontType, fs, 0F, fontStyle, color);
	    	            ct.setSimpleColumn(xl, yb, xr, yt + gapr); //distributed=0
	    	            ct.setLeading(leading);
	    	            ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
	            	} else {
	            		final int lines = ct.getLinesWritten(); //模擬出來的可能的文字列數
	            		if(lines == 1) { //單行時, 垂直置中
	            			float shift = (yt - yb - fs) / 2;
	            			ct.setText(null);
	            			addTextIntoColumnText(ct, text2, fontType, fs, 0F, fontStyle, color);
	            			
	            			if(distributed == PdfConst.XPRINT_DISTRIBUTED_1) {
	            				float charGap = ensureNonnegative((xr - xl - fs * text2.length()) / (text2.length() + 1));
	            				ct.setSimpleColumn(xl + charGap, yb, xr - charGap, yt - shift); //單行水平均佈, 文字不緊貼左右框  -> 框左右內側先讓出空間
		            			ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
	            	            ct.setAlignment((text2.length() == 1) ? Element.ALIGN_CENTER : Element.ALIGN_JUSTIFIED_ALL);
	            			} else {
	            				ct.setSimpleColumn(xl, yb, xr, yt - shift); 
		            			ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
		            			
	            				if(distributed == PdfConst.XPRINT_DISTRIBUTED_3)
		            				ct.setAlignment(Element.ALIGN_CENTER);
		            			else if(distributed == PdfConst.XPRINT_DISTRIBUTED_5)
		            				ct.setAlignment((textDirection == PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY) ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
		            			else if(distributed == PdfConst.XPRINT_DISTRIBUTED_6)
		            				ct.setAlignment((textDirection == PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
		            			else if(distributed == PdfConst.XPRINT_DISTRIBUTED_7)
		            				ct.setAlignment((textDirection == PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
	            			}
	            		} else { //多行 (TODO: 不支援由右向左排列)
	            			ct.setText(null);
            				addTextIntoColumnText(ct, text, fontType, fs, 0F, fontStyle, color);
            				ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
            				
	            			if(distributed == PdfConst.XPRINT_DISTRIBUTED_1 || distributed == PdfConst.XPRINT_DISTRIBUTED_6) { //水平緊靠, 行間垂直均佈, 首尾行觸框
	            				float leading2 = pt((bh - fontSize) / (lines - 1)); //新列距 (依 bh + (x - fontSize) = x*lines 式運算)
	            				float yt2 = yb + pt(bh) + leading2 - fs;
		            			ct.setSimpleColumn(xl, yb, xr, yt2);
		            			ct.setLeading(leading2);
		            			ct.setAlignment(Element.ALIGN_LEFT);
	            			} else { //文字垂直置中
	            				float shift = pt((bh - fontSize * lines) / 2);
		            			ct.setSimpleColumn(xl, yb, xr, yt - shift);
		            			ct.setLeading(leading);
		            			ct.setAlignment((distributed == PdfConst.XPRINT_DISTRIBUTED_5) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
	            			}
	            		}
	            	}
	            }
	            ct.go();
            }
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }
    
    /**
     * @see #drawTextBox(String, float, float, float, float, float, float, float, int, int, int, int[])
     */
    public PdfGenerator drawTextBox(final String text, final float fontSize, final float x, final float y, 
    		final float boundWidth, final float boundHeight, final int distributed, final int fontType, 
    		final int fontStyle, final int[] color) {
    	return drawTextBox(text, fontSize, x, y, boundWidth, boundHeight, 0F, 0F, 
    			PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY, distributed, fontType, fontStyle, color);
    }
    
    /**
     * 在文字框填入文字 (單位: mm).
     * @see #drawTextBox(String, float, float, float, float, float, int, int, int, int[])
     */
    public PdfGenerator drawTextBox(String text, float fontSize, float x, float y, float boundWidth, float boundHeight, int distributed) {
    	return drawTextBox(text, fontSize, x, y, boundWidth, boundHeight, distributed, 
    			PdfConst.FONT_MING, PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLACK);
    }
    
    /**
     * 在文字框填入文字 (單位: mm).
     * @see #drawTextBox(String, float, float, float, float, float, int, int, int[])
     */
    public PdfGenerator drawTextBox(String text, float fontSize, float x, float y, float boundWidth, float boundHeight) {
    	return drawTextBox(text, fontSize, x, y, boundWidth, boundHeight, 
    			PdfConst.XPRINT_DISTRIBUTED_DEFAULT, PdfConst.FONT_MING, 
    			PdfConst.FONT_STYLE_NORMAL, PdfConst.COLOR_BLACK);
    }
    
    //畫頁碼
    PdfGenerator drawPageNumber() {
    	if(this.pageNoHandler != null) //有指定自訂頁碼框時
    		return this;
    	if(this.pgNumTopology == null)
    		return this;
    	if(this.totalPages == 1 && !this.printPageNumberOnFirstPage)
    		return this;
    	String pgno = String.valueOf(this.totalPages);
    	float boundWidth = pgno.length() * this.pgNumTopology[0] + 1;
    	
    	float x = this.pgNumTopology[1];
    	if(this.pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_RIGHT || this.pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_RIGHT) {
    		x = getPageWidth() - boundWidth - this.pgNumTopology[0];
    	} else if(this.pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_LEFT || this.pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_LEFT) {
    		x = this.pgNumTopology[4];
    	} else if(this.pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_TOP_BOTH_SIDE || this.pageNumberPosition == PdfConst.PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE) { //雙面列印
	    	if(this.totalPages % 2 == 0) //偶數頁. TODO: 假設一律左翻頁, 偶數頁靠左
	    		x = this.pgNumTopology[4];
	    	else //奇數頁. TODO: 假設一律左翻頁, 奇數頁靠右
	    		x = getPageWidth() - boundWidth - this.pgNumTopology[0];
    	}
    	
    	drawTextBox(pgno, this.pgNumTopology[0], x, this.pgNumTopology[2], boundWidth, this.pgNumTopology[0] + 1); 
    	return this;
    }
    
    /**
     * 畫 25 條碼.
     * @param code
     * @param x 條碼左下角 x 坐標(mm)
     * @param y 條碼左下角 y 坐標(mm)
     * @param width 條碼寬(mm), 指定 0 者, 使用其原始預設值
     * @param height 條碼高(mm), 指定 0 者, 使用其原始預設值
     */
    public PdfGenerator drawBarcode25(String code, float x, float y, float width, float height) throws DocumentException {
    	Image barcode = getCachedBarcode25(code);
    	barcode.scaleAbsolute((width > 0) ? pt(width) : barcode.getPlainWidth(), (height > 0) ? pt(height) : barcode.getPlainHeight());
    	barcode.setAbsolutePosition(pt(x), transY(pt(y)) - barcode.getScaledHeight());
    	this.cb.addImage(barcode);
    	return this;
    }
    
    /**
     * 畫 39 條碼.
     * @param code
     * @param x 條碼左下角 x 坐標(mm)
     * @param y 條碼左下角 y 坐標(mm)
     * @param width 條碼寬(mm), 指定 0 者, 使用其原始預設值
     * @param height 條碼高(mm), 指定 0 者, 使用其原始預設值
     */
    public PdfGenerator drawBarcode39(String code, float x, float y, float width, float height) throws DocumentException {
    	Image barcode = getCachedBarcode39(code);
    	barcode.scaleAbsolute((width > 0) ? pt(width) : barcode.getPlainWidth(), (height > 0) ? pt(height) : barcode.getPlainHeight());
    	barcode.setAbsolutePosition(pt(x), transY(pt(y)) - barcode.getScaledHeight());
    	this.cb.addImage(barcode);
    	return this;
    }
    
    /**
     * 畫 128 條碼.
     * @param code
     * @param x 條碼左下角 x 坐標(mm)
     * @param y 條碼左下角 y 坐標(mm)
     * @param width 條碼寬(mm), 指定 0 者, 使用其原始預設值
     * @param height 條碼高(mm), 指定 0 者, 使用其原始預設值
     * @throws DocumentException
     */
    public PdfGenerator drawBarcode128(String code, float x, float y, float width, float height) throws DocumentException {
    	Image barcode = getCachedBarcode128(code);
    	barcode.scaleAbsolute((width > 0) ? pt(width) : barcode.getPlainWidth(), (height > 0) ? pt(height) : barcode.getPlainHeight());
    	barcode.setAbsolutePosition(pt(x), transY(pt(y)) - barcode.getScaledHeight());
    	this.cb.addImage(barcode);
    	return this;
    }

    /**
     * 在當前頁貼圖.
     * @param imgPath 圖形檔實體路徑或 HTTP 網址(以 "http://" 開頭)
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @return
     */
    public PdfGenerator addImage(String imgPath, float x, float y) {
        try {
            checkPageReady();
            this.currentImage = getImage(imgPath);
            this.currentImage.scaleAbsolute(this.currentImage.getPlainWidth(), this.currentImage.getPlainHeight());
            this.currentImage.setAbsolutePosition(pt(x), transY(pt(y)) - this.currentImage.getPlainHeight());
            this.cb.addImage(this.currentImage);
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }

    /**
     * 在當前頁貼圖, 圖型伸縮以填滿圖框.
     * @param imgPath 圖形檔實體路徑或 HTTP 網址(以 "http://" 開頭)
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @param width 貼圖後的圖寬 (mm)
     * @param height 貼圖後的圖高 (mm)
     * @return
     */
    public PdfGenerator addImage(String imgPath, float x, float y, float width, float height) {
        try {
            checkPageReady();
            this.currentImage = getImage(imgPath);
            this.currentImage.scaleAbsolute(pt(width), pt(height));
            this.currentImage.setAbsolutePosition(pt(x), transY(pt(y)) - this.currentImage.getScaledHeight());
            this.cb.addImage(this.currentImage);
            return this;
        } catch(Throwable t) {
            throw new PdfException(t.getMessage(), t);
        }
    }

    /** 完成並釋放資源(除 OutputStream 以外). */
    public void close() {
    	try {
	    	this.isLastPage = true;
	    	
	    	//貼騎縫章(蓋末頁之印)
	        drawPerforationSeal();
	        
	        if(this.pageNoHandler != null)
	        	this.pageNoHandler.setTotapPages(getTotalPages()); //在 document.close() 指定總頁數, 以便印出
	    	
	        this.document.close();
	        this.cb = null;
	        this.writer = null;
	        this.document = null;
	        this.fontPlane0H = null;
	        this.fontPlane0V = null;
	        this.fontKaiPlane0H = null;
	        this.fontKaiPlane0V = null;
	        this.fontPlane2H = null;
	        this.fontPlane2V = null;
	        this.fontKaiPlane2H = null;
	        this.fontKaiPlane2V = null;
	        this.fontPlane15H = null;
	        this.fontPlane15V = null;
	        this.fontKaiPlane15H = null;
	        this.fontKaiPlane15V = null;
	        this.currentFont0H = null;
	        this.currentFont0V = null;
	        this.currentFont2H = null;
	        this.currentFont2V = null;
	        this.currentFont15H = null;
	        this.currentFont15V = null;
    	} catch(Throwable t) {
    		throw new PdfException(t.getMessage(), t);
    	}
    }
    
    //確定已起新頁(才可以畫線、填字)
    private void checkPageReady() throws Exception {
        if(this.cb == null)
            throw new Exception("newPage() should be called first!");
    }

    //取圖型
    private Image getImage(final String imgPath) throws MalformedURLException, IOException, BadElementException {
        if(this.imageCache == null)
            this.imageCache = new HashMap<String, Image>();

        if(!imgPath.equals(this.currentImagePath)) { //cache
            this.currentImage = this.imageCache.get(imgPath);
            if(this.currentImage == null) {
                if(imgPath.startsWith("http://"))
                    this.currentImage = Image.getInstance(new URL(imgPath));
                else
                    this.currentImage = Image.getInstance(imgPath);
                this.imageCache.put(imgPath, this.currentImage);
            }
            this.currentImagePath = imgPath;
        }

        return this.currentImage;
    }
    
    private Image getImage(final InputStream img) throws IOException, BadElementException {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	final byte[] buff = new byte[128];
    	for(int n; (n = img.read(buff)) != -1; )
    		out.write(buff, 0, n);
    	return Image.getInstance(out.toByteArray());
    }

    //取騎縫章圖型(位置在畫圖時才選定)(newPage() 被呼叫, 產生新 cb 之前才對前一頁蓋印)
    private void drawPerforationSeal() throws MalformedURLException, IOException, DocumentException {
    	if(this.perforationSealImage == null || this.totalPages == 0)
    		return;
    	
    	final boolean isEvenPage = (this.totalPages % 2 == 0); //當前頁是否為偶數頁
    	
    	//取得圖型物件
    	final float width = this.perforationSealPosition[0];
    	final float height = this.perforationSealPosition[1];
    	
    	//設定位置
    	float x1 = this.perforationSealPosition[2]; //(x1, y1) 左/上側 (PDF 坐標原點在紙張左下角)
    	float y1 = this.perforationSealPosition[3];
    	float x2 = this.perforationSealPosition[4]; //(x2, y2) 右/下側
    	float y2 = this.perforationSealPosition[5];
    	if(this.printPerforationSealOnSingleSide) { //雙面列印成冊
    		if(this.printPerforationSealOnTopDown) { //上翻頁
    			x1 = x2;
    			if(isEvenPage) //偶數頁時才取新騎縫章 x 坐標
    				x2 = (float)((this.pageStyle.getWidth() - width) * Math.random());
    			y2 = -(height / 2);
    			y1 = this.pageStyle.getHeight() + y2; //y2 為負值
    		} else { //左翻頁
    			x1 = -(width / 2);
    			x2 = this.pageStyle.getWidth() + x1; //x1 為負值
    			y1 = y2;
    			if(isEvenPage) //偶數頁時才取右側新騎縫章 y 坐標
    				y2 = (float)((this.pageStyle.getHeight() - height) * Math.random());
    		}
    	} else { //單面列印平鋪
    		if(this.printPerforationSealOnTopDown) { //上下平鋪
    			x1 = x2;
    			x2 = (float)((this.pageStyle.getWidth() - width) * Math.random());
    			y2 = -(height / 2);
    			y1 = this.pageStyle.getHeight() + y2; //y2 為負值
    		} else { //左右平鋪
    			x1 = -(width / 2);
    			x2 = this.pageStyle.getWidth() + x1; //x1 為負值
    			if(this.printPerforationSealOnFirstPageFromRight) { //改成第 1 頁自右側開始蓋浮水印
    				float x = x1; //左右側 x 坐標值對調
    				x1 = x2;
    				x2 = x;
    			}
    			y2 = y1;
    			y1 = (float)((this.pageStyle.getHeight() - height) * Math.random());
    		}
    	}
    	
    	//畫騎縫章
    	if(this.printPerforationSealOnSingleSide) { //雙面列印
    		if(this.totalPages > 1) {
    			if(isEvenPage)
    				this.perforationSealImage.setAbsolutePosition(x2, y2);
    			else //奇數頁印在左/上側
    				this.perforationSealImage.setAbsolutePosition(x1, y1);
    			this.cb.addImage(this.perforationSealImage);
    		}
    	} else { //平鋪
    		boolean printLT = false, printRB = false;
    		
    		if(this.totalPages == 1) { //第 1 頁
    			if(!this.isLastPage) { //當前不為末頁(總共不止一頁)
    				if(!this.printPerforationSealOnTopDown) { //左右平鋪
    					printLT = true;
    					printRB = false;
    				} else { //向下平鋪
    					printLT = false;
    					printRB = true;
    				}
    			}
    		} else { //第 1 頁以外之頁
    			if(!this.isLastPage) { //當前不為末頁
    				printLT = true;
					printRB = true;
    			} else { //末頁
    				if(!this.printPerforationSealOnTopDown) { //左右平鋪
    					printLT = false;
    					printRB = true;
    				} else { //向下平鋪
    					printLT = true;
    					printRB = false;
    				}
    			}
    		}
    		
    		if(printLT) { //印左/上側
    			this.perforationSealImage.setAbsolutePosition(x1, y1);
    			this.cb.addImage(this.perforationSealImage);
    		}
    		if(printRB) { //印右/下側
    			this.perforationSealImage.setAbsolutePosition(x2, y2);
				this.cb.addImage(this.perforationSealImage);
    		}
    	}
    	
    	//位置留給下一頁利用
    	this.perforationSealPosition[2] = x1;
    	this.perforationSealPosition[3] = y1;
    	this.perforationSealPosition[4] = x2;
    	this.perforationSealPosition[5] = y2;
    }
    
    //取 25 條碼之圖
    private Image getCachedBarcode25(String code) {
    	if(this.barcode25Cache == null)
    		this.barcode25Cache = new HashMap<String, Image>();
    	Image barcode = this.barcode25Cache.get(code);
    	if(barcode == null) {
    		BarcodeInter25 code25 = new BarcodeInter25();
    		code25.setGenerateChecksum(true);
            code25.setCode(code);
            code25.setAltText(""); //不顯示條碼文字
            barcode = code25.createImageWithBarcode(this.cb, null, null);
            this.barcode25Cache.put(code, barcode);
    	}
    	return barcode;
    }
    
    //取 39 條碼之圖
    private Image getCachedBarcode39(String code) {
    	if(this.barcode39Cache == null)
    		this.barcode39Cache = new HashMap<String, Image>();
    	Image barcode = this.barcode39Cache.get(code);
    	if(barcode == null) {
    		Barcode39 code39 = new Barcode39();
            code39.setCode(code);
            code39.setAltText(""); //不顯示條碼文字
            barcode = code39.createImageWithBarcode(this.cb, null, null);
            this.barcode39Cache.put(code, barcode);
    	}
    	return barcode;
    }
    
    //取 128 條碼之圖
    private Image getCachedBarcode128(String code) {
    	if(this.barcode128Cache == null)
    		this.barcode128Cache = new HashMap<String, Image>();
    	Image barcode = this.barcode128Cache.get(code);
    	if(barcode == null) {
    		Barcode128 code128 = new Barcode128();
    		code128.setCode(code);
    		code128.setAltText(""); //不顯示條碼文字
    		barcode = code128.createImageWithBarcode(this.cb, null, null);
    		this.barcode128Cache.put(code, barcode);
    	}
    	return barcode;
    }

    //將以當前頁左上角為坐標原點的 y 坐標值轉為 PDF 之以左下角為原點之值(單位: pt)
    private float transY(float y) {
        return this.pageStyle.getHeight() - y;
    }
    
    //將以 PDF 之以左下角為原點之值, 轉成以當前頁左上角為坐標原點的 y 坐標值轉為(單位: pt)
    private float atransY(float y) {
        return this.pageStyle.getHeight() - y;
    }

    //將以 mm 為單位的值換算為單位 pixel
    private float pt(float mm) {
        return PdfConst.pt(mm);
    }

    //將以 pt 為單位的值換算為單位 mm
    private static float mm(float pt) {
        return PdfConst.mm(pt);
    }
    
    //調整線寬
    //input: mm
    //return: pt
    private float tuneLineWidth(float lineWidth) {
        return lineWidth / 1.1F;
    }
    
    //調整字型大小
    private float tuneFontSize(float fontSize) {
        return fontSize * 0.97F;
    }

    //準備線條型式(for line, rectangle, circle, ...)(pt)
    private void prepareDrawLine(final float lineWidth, final int lineStyle, final int[] lineColor) throws Exception {
        checkPageReady();
        
        //line width
        this.cb.setLineWidth(lineWidth);
        
        //line style
        if(lineStyle == PdfConst.LINE_DOT)
            this.cb.setLineDash(2, 2, 0);
        else if(lineStyle == PdfConst.LINE_DASH)
            this.cb.setLineDash(4, 3, 0);
        else if(lineStyle == PdfConst.LINE_DASH_DOT)
        	this.cb.setLineDash(new float[] { 4, 2, 1, 2 }, 0);
        else if(lineStyle == PdfConst.LINE_DASH_DOT_DOT)
        	this.cb.setLineDash(new float[] { 4, 1, 1, 0.5F, 1, 1 }, 0);
        //else //實線 (預設)
        //    this.cb.setLineDash(1, 0, 1);

        this.cb.setRGBColorStroke(lineColor[0], lineColor[1], lineColor[2]); //line color: RGB
    }
    
    //對 ColumnText 寫入一段文字 (fontSize, charSpacing: 單位pt)
    private void addTextIntoColumnText(final ColumnText ct, final String text, final int fontType, final float fontSize, 
    		final float charSpacing, final int fontStyle, final int[] color) {
    	if(text == null || text.length() == 0)
    		return;
    	this.currentFont0H = this.currentFont2H = this.currentFont15H = null; //開始寫入文字塊前, 先重設將用到的字型
    	
    	int plane = 0; //unicode 字面
    	int segStart = 0; //當前字串片斷的起始 index
    	for(int i = 0, ii = text.length(); i < ii; ) {
    		final int codePoint = text.codePointAt(i);
    		final int ccount = Character.charCount(codePoint); //當前字碼對應的字元數 (BMP 以外的字碼由二字元組成)
    		
    		if(codePoint < 0x10000) { //plane 0
    			if(plane != 0) {
    				addTextIntoColumnText1(ct, text.substring(segStart, i), plane, fontType, fontSize, charSpacing, fontStyle, color);
    				segStart = i;
    				plane = 0;
    			}
    			i += ccount;
    		} else if(codePoint > 0x1FFFF && codePoint < 0x30000) { //plane 2
    			if(plane != 2) {
    				addTextIntoColumnText1(ct, text.substring(segStart, i), plane, fontType, fontSize, charSpacing, fontStyle, color);
    				segStart = i;
    				plane = 2;
    			}
    			i += ccount;
    		} else if(codePoint > 0xEFFFF && codePoint < 0x100000) { //plane 15
    			if(plane != 15) {
    				addTextIntoColumnText1(ct, text.substring(segStart, i), plane, fontType, fontSize, charSpacing, fontStyle, color);
    				segStart = i;
    				plane = 15;
    			}
    			i += ccount;
    		} else { //其他字面的字碼換成替代符號印出
    			addTextIntoColumnText1(ct, PdfConst.UNKNOWN_CHAR, plane, fontType, fontSize, charSpacing, fontStyle, color); //直接印出替代字
    			i += ccount;
    			segStart = i;
    			plane = 0;
    		}
    	}
    	addTextIntoColumnText1(ct, text.substring(segStart), plane, fontType, fontSize, charSpacing, fontStyle, color);
    }
    
    //對 ColumnText 寫入一段文字, charSpacing=0(預設字距) (fontSize: 單位pt)
    private void addTextIntoColumnText(final ColumnText ct, final String text, final int fontType, final float fontSize, 
    		final int fontStyle, final int[] color) {
    	addTextIntoColumnText(ct, text, fontType, fontSize, 0F, fontStyle, color);
    }
    
    //對 ColumnText 寫入一段文字 (fontSize: 單位pt)
    private void addTextIntoVerticalText(final VerticalText vt, final String text, final int fontType, final float fontSize, 
    		final int fontStyle, final int[] color) {
    	if(text == null || text.length() == 0)
    		return;
    	this.currentFont0V = this.currentFont2V = this.currentFont15V = null; //開始寫入文字塊前, 先重設將用到的字型
    	
    	int plane = 0; //unicode 字面
    	int segStart = 0; //當前字串片斷的起始 index
    	for(int i = 0, ii = text.length(); i < ii; ) {
    		final int codePoint = text.codePointAt(i);
    		final int ccount = Character.charCount(codePoint); //當前字碼對應的字元數 (BMP 以外的字碼由二字元組成)
    		
    		if(codePoint < 0x10000) { //plane 0
    			if(plane != 0) {
    				addTextIntoVerticalText1(vt, text.substring(segStart, i), plane, fontType, fontSize, fontStyle, color);
    				segStart = i;
    				plane = 0;
    			}
    			i += ccount;
    		} else if(codePoint > 0x1FFFF && codePoint < 0x30000) { //plane 2
    			if(plane != 2) {
    				addTextIntoVerticalText1(vt, text.substring(segStart, i), plane, fontType, fontSize, fontStyle, color);
    				segStart = i;
    				plane = 2;
    			}
    			i += ccount;
    		} else if(codePoint > 0xEFFFF && codePoint < 0x100000) { //plane 15
    			if(plane != 15) {
    				addTextIntoVerticalText1(vt, text.substring(segStart, i), plane, fontType, fontSize, fontStyle, color);
    				segStart = i;
    				plane = 15;
    			}
    			i += ccount;
    		} else { //其他字面的字碼換成替代符號印出
    			addTextIntoVerticalText1(vt, PdfConst.UNKNOWN_CHAR, plane, fontType, fontSize, fontStyle, color); //直接印出替代字
    			i += ccount;
    			segStart = i;
    			plane = 0;
    		}
    	}
    	addTextIntoVerticalText1(vt, text.substring(segStart), plane, fontType, fontSize, fontStyle, color);
    }
    
    //對 ColumnText 寫入一段同字面的文字 (fontSize, charSpacing: 單位pt)
    private void addTextIntoColumnText1(final ColumnText ct, final String segment, final int plane, final int fontType, 
    		final float fontSize, final float charSpacing, final int fontStyle, final int[] color) {
    	switch(plane) {
	    	case 0: {
	    		Chunk chunk = new Chunk(segment, getCurrentFontH(0, fontType, fontSize, fontStyle, color));
	    		if(charSpacing != 0F)
	    			chunk.setCharacterSpacing(charSpacing);
	    		ct.addText(chunk);
	    		break;
	    	}
	    	case 2: {
	    		Chunk chunk = null;
	    		if(getPlane2FontH() != null) {
	        		chunk = new Chunk(segment, getCurrentFontH(2, fontType, fontSize, fontStyle, color));
	    		} else { //無所需字型者, 用替代字元代表
	    			chunk = new Chunk(repeat(PdfConst.UNKNOWN_CHAR, segment.codePointCount(0, segment.length())), getCurrentFontH(0, fontType, fontSize, fontStyle, color));
	    		}
	    		if(charSpacing != 0F)
	    			chunk.setCharacterSpacing(charSpacing);
	    		ct.addText(chunk);
	    		break;
	    	}
	    	case 15: {
	    		Chunk chunk = null;
	    		if(getPlane15FontH() != null) {
	    			chunk = new Chunk(segment, getCurrentFontH(15, fontType, fontSize, fontStyle, color));
	    		} else { //無所需字型者, 用替代字元代表
	    			chunk = new Chunk(repeat(PdfConst.UNKNOWN_CHAR, segment.codePointCount(0, segment.length())), getCurrentFontH(0, fontType, fontSize, fontStyle, color));
	    		}
	    		if(charSpacing != 0F)
	    			chunk.setCharacterSpacing(charSpacing);
	    		ct.addText(chunk);
	    		break;
	    	}
    	}
    }
    
    //對 VerticalText 寫入一段同字面的文字 (fontSize: 單位pt)
    private void addTextIntoVerticalText1(final VerticalText vt, final String segment, final int plane, final int fontType, 
    		final float fontSize, final int fontStyle, final int[] color) {
    	switch(plane) {
	    	case 0:
	    		vt.addText(new Chunk(segment, getCurrentFontV(0, fontType, fontSize, fontStyle, color)));
	    		break;
	    	case 2:
	    		if(getPlane2FontH() != null)
	        		vt.addText(new Chunk(segment, getCurrentFontV(2, fontType, fontSize, fontStyle, color)));
	    		else //無所需字型者, 用替代字元代表
	    			vt.addText(new Chunk(repeat(PdfConst.UNKNOWN_CHAR, segment.codePointCount(0, segment.length())), getCurrentFontV(0, fontType, fontSize, fontStyle, color)));
	    		break;
	    	case 15:
	    		if(getPlane15FontH() != null)
	        		vt.addText(new Chunk(segment, getCurrentFontV(15, fontType, fontSize, fontStyle, color)));
	    		else //無所需字型者, 用替代字元代表
	    			vt.addText(new Chunk(repeat(PdfConst.UNKNOWN_CHAR, segment.codePointCount(0, segment.length())), getCurrentFontV(0, fontType, fontSize, fontStyle, color)));
	    		break;
    	}
    }

    //取當前文字塊中所使用的字型 (plane: unicode 字面, fontType: 字體(明/楷體))
    private Font getCurrentFontH(final int plane, final int fontType, float fontSize, final int fontStyle, final int[] color) {
    	switch(plane) {
    		case 2: return (this.currentFont2H != null) ? this.currentFont2H : 
    			(this.currentFont2H = new Font((fontType == PdfConst.FONT_KAI) ? getKaiPlane2FontH() : getPlane2FontH(), fontSize, fontStyle, new BaseColor(color[0], color[1], color[2])));
	    	case 15: return (this.currentFont15H != null) ? this.currentFont15H : 
	    		(this.currentFont15H = new Font((fontType == PdfConst.FONT_KAI) ? getKaiPlane15FontH() : getPlane15FontH(), fontSize, fontStyle, new BaseColor(color[0], color[1], color[2])));
			default: return (this.currentFont0H != null) ? this.currentFont0H : 
				(this.currentFont0H = new Font((fontType == PdfConst.FONT_KAI) ? getKaiPlane0FontH() : getPlane0FontH(), fontSize, fontStyle, new BaseColor(color[0], color[1], color[2])));
    	}
    }
    
    //取當前文字塊中所使用的垂直排列字型 (plane: unicode 字面, fontType: 字體(明/楷體))
    private Font getCurrentFontV(final int plane, final int fontType, float fontSize, final int fontStyle, final int[] color) {
    	switch(plane) {
    		case 2: return (this.currentFont2V != null) ? this.currentFont2V : 
    			(this.currentFont2V = new Font((fontType == PdfConst.FONT_KAI) ? getKaiPlane2FontV() : getPlane2FontV(), fontSize, fontStyle, new BaseColor(color[0], color[1], color[2])));
	    	case 15: return (this.currentFont15V != null) ? this.currentFont15V : 
	    		(this.currentFont15V = new Font((fontType == PdfConst.FONT_KAI) ? getKaiPlane15FontV() : getPlane15FontV(), fontSize, fontStyle, new BaseColor(color[0], color[1], color[2])));
			default: return (this.currentFont0V != null) ? this.currentFont0V : 
				(this.currentFont0V = new Font((fontType == PdfConst.FONT_KAI) ? getKaiPlane0FontV() : getPlane0FontV(), fontSize, fontStyle, new BaseColor(color[0], color[1], color[2])));
    	}
    }
    
    private int executeBeforeNewPageForDrawTextFlow(PdfGeneratorOnNewPage callback) {
    	if(callback != null)
    		return callback.beforeNewPageForDrawTextFlow(this);
    	return PdfGeneratorOnNewPage.NORMAL;
    }
    
    private int executeAfterNewPageForDrawTextFlow(PdfGeneratorOnNewPage callback) {
    	if(callback != null)
    		return callback.afterNewPageForDrawTextFlow(this);
    	return PdfGeneratorOnNewPage.NORMAL;
    }
    
    //畫除錯用的 text flow 左右兩邊界的線, 上下達紙張邊界(單位: mm)
    private void drawTextFlowBoundForDebug(float x1, float x2, int[] color) {
	    drawTextFlowBoundForDebug(x1, mm(this.marginTop), x2, mm(this.pageStyle.getHeight() - this.marginBottom), color, false);
	}
	
	//畫除錯用的 text flow 左右兩邊界的線(單位: mm)
	//(x1, y1): 左上角, (x2, y2): 右下角
	private void drawTextFlowBoundForDebug(float x1, float y1, float x2, float y2, int[] color, boolean drawTopBorder) {
		if(!isDebug())
			return;
		if(drawTopBorder)
			drawLine(x1, y1, x2, y1, 0.5F, PdfConst.LINE_DOT, color);
		drawLine(x1, y1, x1, y2, 0.5F, PdfConst.LINE_DOT, color);
	    drawLine(x2, y1, x2, y2, 0.5F, PdfConst.LINE_DOT, color);
	}
	
    /**
     * 產生一個字串，該字串會重複指定的字串 n 次.
     * (自 com.tatung.commons.util.StrUtil 中獨立出來)
     * @param s 要重複的字串
     * @param n 重複次數
     * @return
     */
	private String repeat(String s, int n) {
    	if(s == null || n == 0)
    		return "";
    	if(n == 1)
    		return s;
        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < n; i++)
        	buffer.append(s);
        return buffer.toString();
    }
    
    private String reverse(String s) {
    	if(s == null || s.length() == 0)
    		return s;
        StringBuilder sb = new StringBuilder(s.length()).append(s);
        return sb.reverse().toString();
    }
    
    private int lines(float span, float lineWidth) {
    	double n = span / lineWidth;
    	int ret = (int)Math.floor(n);
    	if((n - ret) > 0.5)
    		ret++;
    	return ret;
    }
    
    private float ensureNonnegative(float value) {
    	return (value < 0) ? 0 : value;
    }
    
    //取字型資訊(很長!)
    private String getBaseFontInfo(final BaseFont font) {
    	if(font == null)
    		return "";
    	
    	final StringBuilder s = new StringBuilder();
    	
    	//name entries
    	s.append("allNameEntries ([Name ID, Platform ID, Platform Encoding ID, Language ID, font name]): [\n");
    	for(String[] e : font.getAllNameEntries())
    		s.append("  [").append(StrUtil.join(", ", e)).append("]\n");
    	s.append("]\n");
    	
    	//family font name
    	s.append("familyFontName ([Platform ID, Platform Encoding ID, Language ID, font name]): [\n");
    	for(String[] e : font.getFamilyFontName())
    		s.append("  [").append(StrUtil.join(", ", e)).append("]\n");
    	s.append("]\n");

    	//full font name
    	s.append("fullFontName ([Platform ID, Platform Encoding ID, Language ID, font name]): [\n");
    	for(String[] e : font.getFullFontName())
    		s.append("  [").append(StrUtil.join(", ", e)).append("]\n");
    	s.append("]\n");
    	
    	s.append("codePagesSupported: [").append(StrUtil.join(", ", font.getCodePagesSupported())).append("]\n");
    	s.append("compressionLevel: ").append(font.getCompressionLevel()).append("\n");
    	s.append("differences: ").append(StrUtil.join(", ", font.getDifferences())).append("\n");
    	s.append("enoding: ").append(font.getEncoding()).append("\n");
    	s.append("fontType: ").append(font.getFontType()).append("\n");
    	s.append("postscriptFontName: ").append(font.getPostscriptFontName()).append("\n");
    	s.append("widths: [").append(StrUtil.join(", ", font.getWidths())).append("]\n");
    	s.append("unicodeDifferences: [").append(StrUtil.join(", ", font.getUnicodeDifferences())).append("]\n");
    	
    	return s.toString();
    }
    
    public static class PageNoHandler extends PdfPageEventHelper {
    	private static final String TPL_PAGE_NO = "${pageNo}";
    	private static final String TPL_TOTAL_PAGES = "${totalPages}";
    	private static final float TEXT_COLUMN_WIDTH = PdfConst.pt(200);
    	
    	private String pageNoPrefix;
    	private String pageNoSufix;
    	private String totalPageSegment;
    	private boolean isPageNoAfterTotalPage; //頁碼 排在 總頁數欄 之後
    	private PdfTemplate pageNoBlock;
    	private int totapPages;
    	private final float fontSize; //單位: pt
    	private final Font font;
    	private final float x; //文字框左側距紙張左邊界的距離(單位: pt)
    	private final float lowerBondFromBottom; //文字框距紙張底部的距離(單位: pt)
    	
    	/**
    	 * @param textTemplate 頁碼文字區塊樣版, 文字中可含下列佔位符號:
	     *   <code><ul>
	     *     <li>${pageNo}   (當前頁頁碼)
	     *     <li>${totalPages}   (總頁數)
	     *   </ul></code>
	     *   例: <code>第 ${pageNo} 頁、共 ${totalPages} 頁</code>
	     * @param fontName
	     * @param fontSize (單位: mm)
	     * @param x 文字框左側距紙張左邊界的距離(單位: mm)
	     * @param lowerBondFromBottom 文字框距紙張底部的距離(單位: mm)
    	 */
    	public PageNoHandler(final String textTemplate, final String fontName, final float fontSize, final float x, final float lowerBondFromBottom) 
    			throws DocumentException, IOException {
    		this.fontSize = PdfConst.pt(fontSize);
    		this.x = PdfConst.pt(x);
    		this.lowerBondFromBottom = PdfConst.pt(lowerBondFromBottom);
    		this.font = new Font(BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED), this.fontSize);
    		
    		if(!StrUtil.isEmpty(textTemplate)) {
    			final int iPageNo = textTemplate.indexOf(TPL_PAGE_NO);
    			final int iTotalPages = textTemplate.indexOf(TPL_TOTAL_PAGES);
    			
    			if(iTotalPages < 0) { //無總頁數欄
    				if(iPageNo >= 0) { //有每頁頁碼
    					if(iPageNo > 0)
							this.pageNoPrefix = textTemplate.substring(0, iPageNo);
    					if((iPageNo + TPL_PAGE_NO.length()) < textTemplate.length())
    						this.pageNoSufix = textTemplate.substring(iPageNo + TPL_PAGE_NO.length());
    				}
    			} else { //有總頁數欄
    				if(iPageNo >= 0) { //有每頁頁碼
    					if(iPageNo < iTotalPages) { //例: "第x頁, 共y頁" 拆為: "第x頁, 共" + "y頁"
    						if(iPageNo > 0)
    							this.pageNoPrefix = textTemplate.substring(0, iPageNo);
    						if((iPageNo + TPL_PAGE_NO.length()) < iTotalPages)
    							this.pageNoSufix = textTemplate.substring(iPageNo + TPL_PAGE_NO.length(), iTotalPages);
    						this.totalPageSegment = textTemplate.substring(iTotalPages);
    					} else { //例: "全y頁之第x頁" 拆為: "全y" + "頁之第x頁" 
    						//this.isPageNoAfterTotalPage = true;
    						//this.totalPageSegment = textTemplate.substring(0, iTotalPages + TPL_TOTAL_PAGES.length());
    						//if((iTotalPages + TPL_TOTAL_PAGES.length()) < iPageNo)
    						//	this.pageNoPrefix = textTemplate.substring(iTotalPages + TPL_TOTAL_PAGES.length(), iPageNo);
    						//if((iPageNo + TPL_PAGE_NO.length()) < textTemplate.length())
    						//	this.pageNoSufix = textTemplate.substring(iPageNo + TPL_PAGE_NO.length());
    						
    						//TODO: 尚不支援此排列方式
    						throw new IllegalArgumentException("position of 'page number' behind 'total pages' not supported currently");
    					}
    				} else { //只有總頁數欄
    					this.totalPageSegment = textTemplate;
    				}
    			}
    		}
    	}
    	
    	@Override
    	public void onOpenDocument(final PdfWriter writer, final Document document) {
    		if(this.totalPageSegment != null)
    			this.pageNoBlock = writer.getDirectContent().createTemplate(TEXT_COLUMN_WIDTH, this.fontSize + 3);
        }
    	
    	//@Override
    	//public void onEndPage(final PdfWriter writer, final Document document) { //須傳入自行控制的頁碼, 故改以如下自訂 method, 自行於換頁時呼叫
    	public void newPage(final PdfContentByte cb, final int pageNo) {
    		
    		try {
    			//final ColumnText ct = new ColumnText(writer.getDirectContent());
    			final ColumnText ct = new ColumnText(cb);
    			ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
    			
    			//ct 無法連續 addText() Phrase + Image + Phrase, 只好個別地 setText() + go()
    			if(!this.isPageNoAfterTotalPage) { //例: "第x頁, 共y頁" 拆為: "第x頁, 共" + "y頁"
    				drawPageNo(ct, this.x, pageNo); //頁碼
    				drawTotalPages(ct, ct.getLastX()); //總頁數
    			} else { //例: "全y頁之第x頁" 拆為: "全y" + "頁之第x頁"
    				//TODO: 尚不支援此排列方式
    				//drawTotalPages(ct, this.x); //總頁數(此時無法由 ct.getLastX() 確知最後的 x 坐標值, 只能預估)
    				//drawPageNo(ct, ct.getLastX(), pageNo); //頁碼
    			}
    		} catch(Throwable t) {
    			throw new PdfException(t.getMessage(), t);
    		}
    	}
    	
    	@Override
    	public void onCloseDocument(final PdfWriter writer, final Document document) {
    		if(this.totalPageSegment != null) {
    			final String totalPage = this.totalPageSegment.replace(TPL_TOTAL_PAGES, String.valueOf(this.totapPages)); //填入真實總頁數(自行在 document.close() 前呼叫 setTotalPages(int) 指定之)
    			ColumnText.showTextAligned(this.pageNoBlock, Element.ALIGN_LEFT, new Phrase(totalPage, this.font), 0, 0, 0);    			
    		}
    	}
    	
    	/** 在 document close() 前設值此屬性值 */
    	public void setTotapPages(int totapPages) {
    		this.totapPages = totapPages;
    	}
    	
    	//畫頁碼文字段
    	private void drawPageNo(final ColumnText ct, final float llx, final int pageNo) throws DocumentException {
    		final StringBuilder s = new StringBuilder();
    		if(this.pageNoPrefix != null)
				s.append(this.pageNoPrefix);
			//s.append(writer.getPageNumber()); //此種取頁碼方式不適用於多 PDF 輸出檔的模式
			s.append(pageNo);
			if(this.pageNoSufix != null)
				s.append(this.pageNoSufix);
			ct.setText(new Phrase(s.toString(), this.font));
			ct.setSimpleColumn(llx, this.lowerBondFromBottom, llx + TEXT_COLUMN_WIDTH, this.lowerBondFromBottom + this.fontSize, this.fontSize, Element.ALIGN_LEFT);
			ct.go();
    	}
    	
    	//畫總頁數保留文字段
    	private void drawTotalPages(final ColumnText ct, final float llx) throws DocumentException {
    		if(this.pageNoBlock != null) {
				ct.addElement(Image.getInstance(this.pageNoBlock));
    			ct.setSimpleColumn(llx, this.lowerBondFromBottom, llx + TEXT_COLUMN_WIDTH, this.lowerBondFromBottom + this.fontSize + 2, this.fontSize, Element.ALIGN_LEFT);
    			//WORKAROUND: PdfTemplate 裡的文字框文字會稍小, 只好加大框高 2pt
    			ct.go();
			}
    	}
    }

	@Override
	public String toString() {
		return "PdfGenerator [isLastPage=" + isLastPage + ", debug=" + debug + ", pageStyle=" + pageStyle + ", pageStyle1=" + pageStyle1 +
				", marginTop=" + marginTop + ", marginBottom=" + marginBottom + ", marginLeft=" + marginLeft + ", marginRight=" + marginRight +
				", document=" + document + ", writer=" + writer + ", cb=" + cb + ", currentImagePath=" + currentImagePath + ", currentImage=" +
				currentImage + ", imageCache=" + imageCache + ", barcode25Cache=" + barcode25Cache + ", barcode39Cache=" + barcode39Cache +
				", barcode128Cache=" + barcode128Cache + ", watermarkImage=" + watermarkImage + ", perforationSealImage=" + perforationSealImage +
				", perforationSealPosition=" + Arrays.toString(perforationSealPosition) + ", printPerforationSealOnSingleSide=" +
				printPerforationSealOnSingleSide + ", printPerforationSealOnTopDown=" + printPerforationSealOnTopDown +
				", printPerforationSealOnFirstPageFromRight=" + printPerforationSealOnFirstPageFromRight + ", defaultFontName=" + defaultFontName +
				", defaultKaiFontName=" + defaultKaiFontName + ", plane2FontName=" + plane2FontName + ", kaiPlane2FontName=" + kaiPlane2FontName +
				", plane15FontName=" + plane15FontName + ", kaiPlane15FontName=" + kaiPlane15FontName + ", fontPlane0H=" + fontPlane0H +
				", fontPlane0V=" + fontPlane0V + ", fontKaiPlane0H=" + fontKaiPlane0H + ", fontKaiPlane0V=" + fontKaiPlane0V + ", fontPlane2H=" +
				fontPlane2H + ", fontPlane2V=" + fontPlane2V + ", fontKaiPlane2H=" + fontKaiPlane2H + ", fontKaiPlane2V=" + fontKaiPlane2V +
				", fontPlane15H=" + fontPlane15H + ", fontPlane15V=" + fontPlane15V + ", fontKaiPlane15H=" + fontKaiPlane15H + ", fontKaiPlane15V=" +
				fontKaiPlane15V + ", currentFont0H=" + currentFont0H + ", currentFont0V=" + currentFont0V + ", currentFont2H=" + currentFont2H +
				", currentFont2V=" + currentFont2V + ", currentFont15H=" + currentFont15H + ", currentFont15V=" + currentFont15V + ", pageCount=" +
				pageCount + ", totalPages=" + totalPages + ", pageNumberPosition=" + pageNumberPosition + ", printPageNumberOnFirstPage=" +
				printPageNumberOnFirstPage + ", pgNumTopology=" + Arrays.toString(pgNumTopology) + ", pageNoHandler=" + pageNoHandler + "]";
	}
}
