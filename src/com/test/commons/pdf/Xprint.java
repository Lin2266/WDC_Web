package com.test.commons.pdf;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 讀取大同文稿格式檔(內容長度單位 mm, 預設編碼 Big5)及資料(預設編碼 UTF8)後,
 * 輸出 PDF 檔(預設超過 DEFAULT_MAX_PAGES_PER_OUTPUT_FILE 頁即拆檔).
 * 
 * <hr>
 * coding 用法: 首先應於 AP 初始階段對 Xprint 進行 global 設定, 如:
 * <pre><code> Xprint.setStaticallyRptDir(...); //格式檔所在實體目錄
 * Xprint.setStaticallyOutputDir(...); //輸出檔根目錄實體路徑
 * Xprint.setStaticallyImgDir(...); //貼圖用之圖檔所在實體路徑(optional)
 * Xprint.setStaticallyDefaultFont(...); //預設字型檔實體路徑
 * Xprint.setStaticallyDefaultKaiFont(...); //第二字型實體路徑(未指定者, 用預設字型)
 * ...
 * //上述切始設定只能設一次; 除輸出檔目錄設定外, 其他未指定者, 系統將嘗試由 classpath 路徑尋找.
 * </cdoe></pre>
 * 
 * 一般程式中藉 Xprint 填入資料並產生 PDF 檔, 例:
 * <pre><code>
 * Xprint xp = null;
 * 
 * try {
 *     List&lt;VO&gt; data = ...; //欲填入 PDF 內容的資料
 *     xp = new Xprint("format.txt", "output.pdf"); //初始化 Xprint 並指定格式檔, 輸出檔
 * 
 *     for(VO vo : data) {
 *         //xp.setDebug(true); //開發階段, 可用以校正文字位置(因報表製作軟體及 PDF 產生器差異所致的誤差)
 *         xp.newPage(); //起新頁
 *         xp.add("...", vo.getXXX()); //對格式檔中的 BLOCK 區塊填入資料
 *         ...
 *         //其他藉 addXXX() 填入資料, 其不含 x,y 坐標參數的 method, 表示是針對格式檔之 BLOCK 欄位而設的
 *     }
 * 
 *     File[] outputFiles = xp.getOutputFiles(); //得到輸出檔(頁數太多時 Xprint 會自動分檔)
 *     ...
 * } finally {
 *     if(xp != null) try { xp.close(); } catch(Throwable t) {} //要保證 Xprint 物件會被關閉
 * }
 * </code></pre>
 * 
 * <hr>
 * 格式檔內容, Xprint 預設視為使用 <b>Big5</b> 編碼, 其主要格式:
 * <pre><code> size xoffset yoffset
 * LINE_INFO  [資料筆數]
 * style width x1 y1 x2 y2
 * LABEL_STRING [資料筆數]
 * style left top width height rowgap colgap distributed font ychar xmargin ymargin label
 * BLOCK_INFO [資料筆數]
 * style left top width height rowgap colgap distributed font ychar xmargin ymargin label
 * </code></pre>
 * <ul>
 * <li>以內容版面左上角為原點
 * <li>size = 0(A4 直印) or 1(A4 橫印) or 2(B4 橫印) 3, 4(A3直印), 5(A3橫印), 9(自訂)
 * <li>xoffset, yoffset: 報表內容版面相對於印表原點的座標(非即印表紙左上角座標原點), 單位 mm<br>
 *     size=9 時, 此時後面兩個數值代表紙寬與紙高
 * <li>LINE_INFO, LABEL_STRING, BLOCK_INFO 為關鍵字, 每個表格檔都要有此三個參數.
 * <li>LINE_INFO, LABEL_STRING, BLOCK_INFO 以下相關的資料欄以 TAB 字元隔開; 除 label 為字串外, 其於為數字(可能含小數), 單位 mm.
 * <li>LINE_INFO:
 *   <ol>
 *   <li>style: 線段種類. 0: Solid, 1: Dash, 2: Dot, 3: DashDot, 4: DashDotDot<br>
 *       optional: 個位數字前的數字, 代表顏色碼 BGR 16 進位碼轉成 10 進位後的數字.<br>
 *       例: <code>顏色 BGR 碼: F4 A4 CE =&gt; F4A4CE =&gt; 16032974 (10進位數)</code>
 *   <li>width: 線段寬度
 *   <li>x1: 線段起始 x 座標
 *   <li>y1: 線段起始 y 座標
 *   <li>x2: 線段結束 x 座標
 *   <li>y2: 線段結束 y 座標 (x1, x2, x3, x4 為相對於內容版面原點的座標)
 *   </ol>
 * <li>LABEL_STRING:
 *   <ol>
 *   <li>style: 最右邊的個位數字, 表示文字書寫習慣
 *     <ul>
 *     <li>0: 由左至右, 由上到下
 *     <li>1: 由右至左, 由上到下
 *     <li>2: 由上到下, 由右至左
 *     <li>3: 由上到下, 由左至右
 *     </ul>
 *     optional: 個位數前的數字, 代表顏色碼 BGR 16進位碼轉成 10進位後的數字.<br>
 *     例: <code>顏色 BGR: F4 A4 CE =&gt; F4A4CE =&gt; 16032974 (10進位數)</code><br>
 *     右接一位方向碼 0 =&gt; "160329740"
 *   <li>left: 文字框左上角 x 坐標
 *   <li>top: 文字框左上角 y 坐標
 *   <li>width: 文字框寬
 *   <li>height: 文字框高
 *   <li>rowgap: 文字列距(只有 distributed 為 0 時才有作用)
 *   <li>colgap: 文字行距(只有 distributed 為 0 時才有作用)
 *   <li>distributed: 文字在文字框內的排法
 *     <ul>
 *     <li>0: 預設排列
 *     <li>1: 均布, 只對水平排列文字有作用<br>
 *         (1)文字列未折行時: 水平均佈, 垂直居中<br>
 *         (2)文字列有折行時: 水平緊靠, 行間垂直均佈, 首尾行觸框
 *     <li>2: 均布, 只對垂直排列文字有作用<br>
 *         (1)文字列未折行時: 文字水平居中, 垂直均佈<br>
 *         (2)字元垂直緊靠, 行間水平均佈, 首尾行之外與框之間有間隙
 *     <li>3: 垂直靠中, 不均佈, 只對水平排列文字有作用<br>
 *         (1)文字列未折行時: 文字水平垂直皆居中<br>
 *         (2)文字列有折行時: 文字垂直居中
 *     <li>4: 水平靠中, 不均佈, 只對垂直排列文字有作用<br>
 *         (1)文字列未折行時: 文字水平垂直皆居中<br>
 *         (2)文字列有折行時: 文字水平居中
 *     <li>5: 水平靠右, 垂直置中
 *     <li>6: 垂直分佈, 邊界對齊(橫書); 水平分佈, 邊界對齊(直書)
 *     <li>7: 水平靠左, 垂直置中
 *     </ul>
 *   <li>font: 字型
 *     <ul>
 *     <li>0: 細明體
 *     <li>1: 標楷體
 *     <li>2: 細明體(轉 90 度)(未支援此選項)
 *     <li>3: 標楷體(轉 90 度)(未支援此選項)
 *     </ul>
 *   <li>ychar: 字型高度
 *   <li>xmargin: (無作用)
 *   <li>ymargin: (無作用)
 *   <li>label: 文字內容
 *   </ol>
 * <li>BLOCK_INFO:
 *   <ol>
 *   <li>style, left, top, width, height, rowgap, colgap, distributed, font, ychar, xmargin, ymargin 說明皆同 LABEL_STRING
 *   <li>label: 代表文字框之 id, Xprint 藉此 id 把資料填入對應的文字框中
 *   </ol>
 * </ul>
 * 
 * <hr>
 * depend on: <a href="http://itextpdf.com/" target="_blank">iText</a>
 * @see #DEFAULT_MAX_PAGES_PER_OUTPUT_FILE
 * @deprecated 由 com.tatung.commons.pdf2.Xprint 取代
 */
@Deprecated
public class Xprint {
	private static final Logger log = LoggerFactory.getLogger(Xprint.class);
	
	/** 預設的印表格式檔的編碼(MS950). */
    public static final String DEFAULT_RPT_CHARSET = "MS950";
    /** 單一輸出檔內容最大容許頁數 (50000) */
    public static final int DEFAULT_MAX_PAGES_PER_OUTPUT_FILE = 50000;
    
    /** 線段種類 - 實線. */
    public static final int LINE_STYLE_SOLID = 0;
    /** 線段種類 - 虛線. */
    public static final int LINE_STYLE_DASH = 1;
    /** 線段種類 - 點狀虛線. */
    public static final int LINE_STYLE_DOT = 2;
    /** 線段種類 - 短線段與點混合虛線. */
    public static final int LINE_STYLE_DASHDOT = 3;
    /** 線段種類 - 短線段與雙點混合虛線. */
    public static final int LINE_STYLE_DASHDOTDOT = 4;
    
    /** 紙張: A3 直印 */
    public static final int PAGE_A3 = PdfConst.PAGE_A3;
    /** 紙張: A3 橫印 */
    public static final int PAGE_A3_LANDSCAPE = PdfConst.PAGE_A3_LANDSCAPE;
    /** 紙張: A4 直印 */
    public static final int PAGE_A4 = PdfConst.PAGE_A4;
    /** 紙張: A4 橫印 */
    public static final int PAGE_A4_LANDSCAPE = PdfConst.PAGE_A4_LANDSCAPE;
    /** 紙張: B3 直印 */
    public static final int PAGE_B3 = PdfConst.PAGE_B3;
    /** 紙張: B3 橫印 */
    public static final int PAGE_B3_LANDSCAPE = PdfConst.PAGE_B3_LANDSCAPE;
    /** 紙張: B4 直印 */
    public static final int PAGE_B4 = PdfConst.PAGE_B4;
    /** 紙張: B4 橫印 */
    public static final int PAGE_B4_LANDSCAPE = PdfConst.PAGE_B4_LANDSCAPE;
    
    /** 頁碼: 不列印頁碼 */
    public static final int PAGE_NUMBER_NONE = PdfConst.PAGE_NUMBER_NONE;
    /** 頁碼位置: 上方中央  */
    public static final int PAGE_NUMBER_POSITION_TOP_CENTER = PdfConst.PAGE_NUMBER_POSITION_TOP_CENTER;
    /** 頁碼位置: 下方中央  */
    public static final int PAGE_NUMBER_POSITION_BOTTOM_CENTER = PdfConst.PAGE_NUMBER_POSITION_BOTTOM_CENTER;
    /** 頁碼位置: 上方靠右  */
    public static final int PAGE_NUMBER_POSITION_TOP_RIGHT = PdfConst.PAGE_NUMBER_POSITION_TOP_RIGHT;
    /** 頁碼位置: 上方靠左  */
    public static final int PAGE_NUMBER_POSITION_TOP_LEFT = PdfConst.PAGE_NUMBER_POSITION_TOP_LEFT;
    /** 頁碼位置: 下方靠右  */
    public static final int PAGE_NUMBER_POSITION_BOTTOM_RIGHT = PdfConst.PAGE_NUMBER_POSITION_BOTTOM_RIGHT;
    /** 頁碼位置: 下方靠左  */
    public static final int PAGE_NUMBER_POSITION_BOTTOM_LEFT = PdfConst.PAGE_NUMBER_POSITION_BOTTOM_LEFT;
    /** 頁碼位置: 上方兩旁(雙面印列, 左翻)  */
    public static final int PAGE_NUMBER_POSITION_TOP_BOTH_SIDE = PdfConst.PAGE_NUMBER_POSITION_TOP_BOTH_SIDE;
    /** 頁碼位置: 下方兩旁(雙面印列, 左翻)  */
    public static final int PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE = PdfConst.PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE;
    
    /** 印騎縫章於每頁左/右單側(用於雙面列印, 左翻) */
    public static final int PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY = 0;
    /** 印騎縫章於每頁左右兩側 */
    public static final int PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY = 1;
    /** 印騎縫章於每頁上/下單側(用於雙面列印, 上翻) */
    public static final int PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY = 2;
    /** 印騎縫章於每頁上下兩側 */
    public static final int PERFORATION_SEAL_BOTH_SIDE_VERTICALLY = 3;
    
    /** 字體: 明體 */
    public static final int FONT_MING = PdfConst.FONT_MING;
    /** 字體: 楷體 */
    public static final int FONT_KAI = PdfConst.FONT_KAI;
    
    //以下欄位需在 AP 啟動時即給值, 使呼叫者可不需再自行設值
    private static File _rptDir; //rpt 格式檔所在目錄
    private static File _outputDir; //輸出 PDF 的目錄
    private static File _imgDir; //放置圖檔的目錄
    private static String _fontNamePlane0; //預設字型名稱, 或該字型的字型檔之完整實體路徑
    private static String _fontNameKaiPlane0; //預設楷體字型名稱, 或該字型的字型檔之完整實體路徑
    private static String _fontNamePlane2; //unicode 第二字面字型名稱, 或該字型的字型檔之完整實體路徑
    private static String _fontNameKaiPlane2; //楷體 unicode 第二字面字型名稱, 或該字型的字型檔之完整實體路徑
    private static String _fontNamePlane15; //unicode 第 15 字面字型名稱, 或該字型的字型檔之完整實體路徑
    private static String _fontNameKaiPlane15; //楷體 unicode 第 15 字面字型名稱, 或該字型的字型檔之完整實體路徑
    
    private PdfGenerator pdf;
    private int pageStyle; //PdfGenerator.PAGE_*
    private float userDefinedWidth; //自定義紙寬(mm)
    private float userDefinedHeight; //自定義紙高(mm)
    private float userDefinedMarginLeft; //自定義左邊界(mm)
    private float userDefinedMarginRight; //自定義右邊界(mm)
    private float userDefinedMarginTop; //自定義上邊界(mm)
    private float userDefinedMarginBottom; //自定義下邊界(mm)
    private String rptCharset;
    private File outputDir; //輸出檔實際放置的實體目錄
    private File outputFile; //當前的輸出 pdf 檔
    private List<File> outputFiles;
    private float originOffsetX; //總偏移量
    private float originOffsetY; //總偏移量
    private List<double[]> lineInfo; //每頁會出現的線段資訊(null 者代表無格式檔). [ style(顏色BGR), style(線型), 線寬, x起, y起, x迄, y迄 ]
    private List<String[]> labelInfo; //每頁會出現的 label 資訊(null 者代表無格式檔). [ style(顏色BGR的十位進數字), style(排列方式), x左上角, y左上角, 文字框寬, 文字框高, 列距, 行距, 分佈方式, 字型, 字高, x文字與框距離, y文字與框距離, 文字內容 ]
    private HashMap<String, double[]> blockInfo; //rpt 格式檔中的 BLOCK_INFO 資訊(null 者代表無格式檔). { label(String) : [ color(BGR), direction, blockStartX, blockStartY, blockWidth, blockHeight, distributed, fontSize](double[]) }
    private int pageCount; //總頁數
    private int pageInFile; //當前輸出檔中的頁數
    private int maxPages; //每個輸出檔最大容許頁數
    private OutputStream out; //輸出 PDF
    private String outputFilePrefix; //輸出檔名之前段
    private String outputFilePostfix; //輸出檔名之後段
    private String watermarkImg; //浮水印圖檔路徑
    private double watermarkX; //浮水印 x 位置
    private double watermarkY; //浮水印 y 位置
    private String currentImgFilename; //最近一次貼圖的檔名
    private String currentImgPath; //最近一次貼圖的檔案實體路徑
    private boolean debug; //運行中印出額外資訊, PDF 內的文字框加虛線框以供識別
    private boolean coverException; //掩蓋製作 PDF 內容過程中的 exception(預設 true)
    
    //### static method #########################################################################
    
    /** 一次性地指定格式檔所在的實體目錄, 最好在 ap 啟動時即予指定. 如果未指定(null)者, 則自當前 classlocader 下的 classpath 根目錄找(當 fileName 為相對路徑時), 或直接找 fileName(當 fileName 為絕對路徑時). */
    public static void setStaticallyRptDir(String rptDir) {
    	if(rptDir != null && _rptDir == null)
    		_rptDir = new File(rptDir);
    }
    
    /** 一次性地指定 PDF 輸出檔存放的頂層實體目錄, 最好在 ap 啟動時即予指定. 如果未指定(null)者, 取 OS 的系統暂存目錄(由 JVM 環境變數 java.io.tmpdir 決定). */
    public static void setStaticallyOutputDir(String outputDir) {
    	if(outputDir != null && _outputDir == null)
    		_outputDir = new File(outputDir);
    }
    
    /** 一次性地指定圖檔所在的實體目錄, 最好在 ap 啟動時即予指定. 如果未指定(null)者, 則自當前 classlocader 下的 classpath 根目錄找(當 fileName 為相對路徑時), 或直接找 fileName(當 fileName 為絕對路徑時). */
    public static void setStaticallyImgDir(String imgDir) {
    	if(_imgDir == null)
    		_imgDir = new File(imgDir);
    }
    
    /**
     * 一次性地指定預設字型檔(以 Unicode BMP 為主)所在的實體目錄, 最好在 ap 啟動時即予指定.
     * 如果 fontName 非絕對路徑, 將自當前 classlocader 下的 classpath 根目錄找字型.<br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-uming/uming.ttc,0
     * </code>
     */
    public static void setStaticallyDefaultFont(String fontName) {
    	if(fontName != null && _fontNamePlane0 == null)
    		_fontNamePlane0 = getFileForRead(null, fontName).getAbsolutePath();
    }
    
    /**
     * 一次性地指定楷體預設字型檔(以 Unicode BMP 為主)所在的實體目錄, 最好在 ap 啟動時即予指定.
     * 如果 fontName 非絕對路徑, 將自當前 classlocader 下的 classpath 根目錄找字型.<br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-ukai/ukai.ttc,0
     * </code>
     */
    public static void setStaticallyDefaultKaiFont(String fontName) {
    	if(fontName != null && _fontNameKaiPlane0 == null)
    		_fontNameKaiPlane0 = getFileForRead(null, fontName).getAbsolutePath();
    }
    
    /** 一次性地指定 Unicode 第 2 字面字型檔所在的實體目錄, 最好在 ap 啟動時即予指定. 如果 fontName 非絕對路徑, 將自當前 classlocader 下的 classpath 根目錄找字型. */
    public static void setStaticallyPlane2Font(String fontName) {
    	if(fontName != null && _fontNamePlane2 == null)
    		_fontNamePlane2 = getFileForRead(null, fontName).getAbsolutePath();
    }
    
    /** 一次性地指定楷體 Unicode 第 2 字面字型檔所在的實體目錄, 最好在 ap 啟動時即予指定. 如果 fontName 非絕對路徑, 將自當前 classlocader 下的 classpath 根目錄找字型. */
    public static void setStaticallyPlane2KaiFont(String fontName) {
    	if(fontName != null && _fontNameKaiPlane2 == null)
    		_fontNameKaiPlane2 = getFileForRead(null, fontName).getAbsolutePath();
    }
    
    /** 一次性地指定 Unicode 第 15 字面字型檔所在的實體目錄, 最好在 ap 啟動時即予指定. 如果 fontName 非絕對路徑, 將自當前 classlocader 下的 classpath 根目錄找字型. */
    public static void setStaticallyPlane15Font(String fontName) {
    	if(fontName != null && _fontNamePlane15 == null)
    		_fontNamePlane15 = getFileForRead(null, fontName).getAbsolutePath();
    }
    
    /** 一次性地指定楷體 Unicode 第 15 字面字型檔所在的實體目錄, 最好在 ap 啟動時即予指定. 如果 fontName 非絕對路徑, 將自當前 classlocader 下的 classpath 根目錄找字型. */
    public static void setStaticallyPlane15KaiFont(String fontName) {
    	if(fontName != null && _fontNameKaiPlane15 == null)
    		_fontNameKaiPlane15 = getFileForRead(null, fontName).getAbsolutePath();
    }
    
    //### end of static method ##################################################################
    
    /** 僅供用於登錄於 IoC 容器中設置 defaultFontName 屬性之用 */
    public Xprint() {}
    
    /**
     * @param rptFormatFileName 印表格式檔檔名. 
     * 		  null 值者, 則使用預設紙張(A4)、預設邊界長, 且只能使用不依賴格式檔的 addFlow(), addLine(), addStyleLine(), 其他如 add() 等加字串及條碼等功能一概無用
     * @param outputFileName 建議的輸出 PDF 檔. <b>注意: 本輸入物件不即是實際輸出的檔案物件, 應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     * @param rptCharset 印表格式檔之字元編碼
     */
    public Xprint(final String rptFormatFileName, final String outputFileName, final String rptCharset) {
    	try {
    		//將輸出檔名折分為主檔名, 副檔名(含點號)二段
    		{
    			int i = outputFileName.lastIndexOf('.');
    			if(i != -1) {
    				this.outputFilePrefix = outputFileName.substring(0, i);
    				this.outputFilePostfix = outputFileName.substring(i);
    			} else {
    				this.outputFilePrefix = outputFileName;
    				this.outputFilePostfix = ".pdf";
    			}
    		}
    		
    		//確定輸出檔
    		this.outputFile = getFileForWrite(getOutputDir(), outputFileName);
    		if(!this.outputFile.createNewFile()) { //輸出檔一旦已存在者, 主檔名末加 1, 2, ... 直至無重複檔名為止
    			for(int i = 1; ; i++) {
    				if((this.outputFile = getFileForWrite(getOutputDir(), this.outputFilePrefix + i + this.outputFilePostfix)).createNewFile()) { //仍不保證多人環境中, 不會發生輸出檔名一致的情況
    					this.outputFilePrefix += i;
    					break;
    				}
    			}
    		}
    		
    		this.outputFiles = new ArrayList<File>();
    		this.rptCharset = (rptCharset == null) ? DEFAULT_RPT_CHARSET : rptCharset;
    		this.pageCount = 0; //this.pageInFile 在 newOutput() 中設初始值
    		this.maxPages = DEFAULT_MAX_PAGES_PER_OUTPUT_FILE;
    		this.coverException = true;
    		this.pageStyle = PdfConst.DEFAULT_PAGE_SIZE;
			this.userDefinedWidth = -1;
            this.userDefinedHeight = -1;
            this.userDefinedMarginLeft = -1;
        	this.userDefinedMarginRight = -1;
        	this.userDefinedMarginTop = -1;
        	this.userDefinedMarginBottom = -1;
            
    		newOutput(); //設置輸出檔

    		//依格式檔而決定 this.pageStyle, this.userDefinedWidth, this.userDefinedHeight, this.userDefinedMarginLeft, this.userDefinedMarginRight, this.userDefinedMarginTop, this.userDefinedMarginBottom, this.originOffsetX, this.originOffsetY
    		if(rptFormatFileName != null) //讀入格式檔
    			loadRptData(getFileForRead(_rptDir, rptFormatFileName));
    	} catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    /**
     * @param rptFormatFileName 印表格式檔檔名(內容編碼為 Big5)
     * 		  null 值者, 則使用預設紙張(A4)、預設邊界長, 且只能使用不依賴格式檔的 addFlow(), addLine(), addStyleLine(), 其他如 add() 等加字串及條碼等功能一概無用
     * @param outputFileName 建議的輸出 PDF 檔. <b>注意: 本輸入物件不即是實際輸出的檔案物件, 應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     */
    public Xprint(String rptFormatFileName, String outputFileName) {
        this(rptFormatFileName, outputFileName, DEFAULT_RPT_CHARSET);
    }
    
    /**
     * 不使用格式檔, 完全以 addFlow(), addLine(), addStyleLine() 等 method 自行購成 PDF 內容.
     * @param outputFileName 建議的輸出 PDF 檔. <b>注意: 本輸入物件不即是實際輸出的檔案物件, 應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     * @param pageSize PDF 初始紙張大小設定(可在起新頁時自行指定紙張樣式), 可為: 
     * 		PAGE_A3, PAGE_A4, PAGE_B3, PAGE_B4, PAGE_A3_LANDSCAPE, 
     * 		PAGE_A4_LANDSCAPE, PAGE_B3_LANDSCAPE, PAGE_B4_LANDSCAPE 之一
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     * @see #PAGE_A3
     * @see #PAGE_A4
     * @see #PAGE_B3
     * @see #PAGE_B4
     * @see #PAGE_A3_LANDSCAPE
     * @see #PAGE_A4_LANDSCAPE
     * @see #PAGE_B3_LANDSCAPE
     * @see #PAGE_B4_LANDSCAPE
     */
    public Xprint(final String outputFileName, int pageSize, final double marginLeft, 
    		final double marginRight, final double marginTop, final double marginBottom) {
    	this(null, outputFileName);
    	this.pageStyle = pageSize;
    	//this.userDefinedWidth; //由 this.pageStyle 決定
    	//this.userDefinedHeight;
    	this.originOffsetX = 0;
    	this.originOffsetY = 0;
    	this.userDefinedMarginLeft = (float)marginLeft;
    	this.userDefinedMarginRight = (float)marginRight;
    	this.userDefinedMarginTop = (float)marginTop;
    	this.userDefinedMarginBottom = (float)marginBottom;
    }
    
    /**
     * 不使用格式檔, 完全以 addFlow(), addLine(), addStyleLine() 等 method 自行購成 PDF 內容.
     * 使用預設四邊界長度.
     * @param outputFileName 輸出 PDF 檔檔名. 一旦同名檔案已存在者, 主檔名將附加數字以區別
     * @param pageSize PDF 初始紙張大小設定(可在起新頁時自行指定紙張樣式), 可為: 
     * 		PAGE_A3, PAGE_A4, PAGE_B3, PAGE_B4, PAGE_A3_LANDSCAPE, 
     * 		PAGE_A4_LANDSCAPE, PAGE_B3_LANDSCAPE, PAGE_B4_LANDSCAPE 之一
     * @see #PAGE_A3
     * @see #PAGE_A4
     * @see #PAGE_B3
     * @see #PAGE_B4
     * @see #PAGE_A3_LANDSCAPE
     * @see #PAGE_A4_LANDSCAPE
     * @see #PAGE_B3_LANDSCAPE
     * @see #PAGE_B4_LANDSCAPE
     * @see PdfConst#DEFAULT_MARGIN_LEFT
     * @see PdfConst#DEFAULT_MARGIN_RIGHT
     * @see PdfConst#DEFAULT_MARGIN_TOP
     * @see PdfConst#DEFAULT_MARGIN_BOTTOM
     */
    public Xprint(final String outputFileName, int pageSize) {
    	this(outputFileName, pageSize, -1D, -1D, -1D, -1D);
    }
    
    /**
     * 不使用格式檔, 完全以 addFlow(), addLine(), addStyleLine() 等 method 自行購成 PDF 內容.
     * 使用預設紙張(A4)及預設四邊界長度.
     * @param outputFileName 建議的輸出 PDF 檔. <b>注意: 本輸入物件不即是實際輸出的檔案物件, 應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     * @see PdfConst#DEFAULT_MARGIN_LEFT
     * @see PdfConst#DEFAULT_MARGIN_RIGHT
     * @see PdfConst#DEFAULT_MARGIN_TOP
     * @see PdfConst#DEFAULT_MARGIN_BOTTOM
     */
    public Xprint(final String outputFileName) {
    	this(outputFileName, PAGE_A4, -1D, -1D, -1D, -1D);
    }
    
    /**
     * 不使用格式檔, 自訂紙張大小, 完全以 addFlow(), addLine(), addStyleLine() 等 method 自行購成 PDF 內容.
     * @param outputFileName 建議的輸出 PDF 檔. <b>注意: 本輸入物件不即是實際輸出的檔案物件, 應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     * @param pageWidth 頁寬(單位: mm)
     * @param pageHeight 頁高(單位: mm)
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     */
    public Xprint(final String outputFileName, final double pageWidth, final double pageHeight, 
    		final double marginLeft, final double marginRight, final double marginTop, 
    		final double marginBottom) {
    	this(null, outputFileName);
    	this.pageStyle = PdfConst.PAGE_USERDEFINED;
    	this.userDefinedWidth = (float)pageWidth;
    	this.userDefinedHeight = (float)pageHeight;
    	this.originOffsetX = 0;
    	this.originOffsetY = 0;
    	this.userDefinedMarginLeft = (float)marginLeft;
    	this.userDefinedMarginRight = (float)marginRight;
    	this.userDefinedMarginTop = (float)marginTop;
    	this.userDefinedMarginBottom = (float)marginBottom;
    }
    
    /**
     * 不使用格式檔, 自訂紙張大小, 預設四邊界長, 完全以 addFlow(), addLine(), addStyleLine() 等 method 自行購成 PDF 內容.
     * @param outputFileName 建議的輸出 PDF 檔. <b>注意: 本輸入物件不即是實際輸出的檔案物件, 應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     * @param pageWidth 頁寬(單位: mm)
     * @param pageHeight 頁高(單位: mm)
     * @see PdfConst#DEFAULT_MARGIN_LEFT
     * @see PdfConst#DEFAULT_MARGIN_RIGHT
     * @see PdfConst#DEFAULT_MARGIN_TOP
     * @see PdfConst#DEFAULT_MARGIN_BOTTOM
     */
    public Xprint(final String outputFileName, final double pageWidth, final double pageHeight) {
    	this(outputFileName, pageWidth, pageHeight, -1D, -1D, -1D, -1D);
    }
    
    /**
     * 一次性地指定預設字型檔, 最好在 ap 啟動時即予指定.
     * 預設字型設定應以使用 static method setDefaultFont(String) 為主, 本 method 只是為了方便舊有 BLI 專案 AP 中在 Spring xml 設定檔中以 bean 屬性的方式進行設定.
     * @param fontName 字型檔(以 Unicode BMP 為主)所在的實體路徑或在 Windows 系統下的字型名稱
     */
    public void setDefaultFontName(String fontName) {
    	setStaticallyDefaultFont(fontName);
    }
    
    /**
     * 一次性地指定楷體預設字型檔, 最好在 ap 啟動時即予指定.
     * 預設字型設定應以使用 static method setDefaultFont(String) 為主, 本 method 只是為了方便舊有 BLI 專案 AP 中在 Spring xml 設定檔中以 bean 屬性的方式進行設定.
     * @param fontName 楷體字型檔(以 Unicode BMP 為主)所在的實體路徑或在 Windows 系統下的字型名稱
     */
    public void setDefaultKaiFontName(String fontName) {
    	setStaticallyDefaultKaiFont(fontName);
    }
    
    /**
     * 指定頁碼位置
     * @param pageNumberPosition 可指定 
     * 		PdfConst.PAGE_NUMBER_NONE(不顯示頁碼, 預設值), 
     * 		PAGE_NUMBER_POSITION_TOP_CENTER, 
     * 		PAGE_NUMBER_POSITION_BOTTOM_CENTER
     * 		PAGE_NUMBER_POSITION_TOP_RIGHT
     * 		PAGE_NUMBER_POSITION_TOP_LEFT
     * 		PAGE_NUMBER_POSITION_BOTTOM_RIGHT
     * 		PAGE_NUMBER_POSITION_BOTTOM_LEFT
     * 		PAGE_NUMBER_POSITION_TOP_BOTH_SIDE (上方, 雙面列印, 奇數頁靠右, 偶數頁靠左)
     * 		PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE (下方, 雙面列印, 奇數頁靠右, 偶數頁靠左)
     * @see #PAGE_NUMBER_NONE
     * @see #PAGE_NUMBER_POSITION_TOP_CENTER
     * @see #PAGE_NUMBER_POSITION_BOTTOM_CENTER
     * @see #PAGE_NUMBER_POSITION_TOP_RIGHT
     * @see #PAGE_NUMBER_POSITION_TOP_LEFT
     * @see #PAGE_NUMBER_POSITION_BOTTOM_RIGHT
     * @see #PAGE_NUMBER_POSITION_BOTTOM_LEFT
     * @see #PAGE_NUMBER_POSITION_TOP_BOTH_SIDE
     * @see #PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE
     */
    public void setPageNumberPosition(int pageNumberPosition) {
    	this.pdf.setPageNumberPosition(pageNumberPosition);
    }
    
    /** 設為 true 者, 將在所有文字外圍加虛線框, 以供觀察文字位置 */
    public void setDebug(boolean debug) {
    	this.debug = debug;
    	this.pdf.setDebug(debug);
    	if(debug)
			System.out.println("write to '" + this.outputFile.getAbsolutePath() + "'");
    }
    
    /** 掩蓋製作 PDF 內容文字, 及畫線過程中的 exception, 儘量地輸出 PDF 檔(預設 true) */
    public void setCoverException(boolean coverException) {
    	this.coverException = coverException;
    }
    
    /**
     * 修改輸出檔最大容許頁數.
     * @param pages
     * @see #DEFAULT_MAX_PAGES_PER_OUTPUT_FILE
     */
    public void setMaxPagesPerFile(int pages) {
    	this.maxPages = pages;
    }
    
    /**
     * 產生一新頁, 並於新頁畫框線及 Label 字串(固定位置).
     */
    public void newPage() {
        try {
        	if(this.pageInFile >= this.maxPages) //換檔輸出
        		newOutput();
            if(this.pageStyle == PdfConst.PAGE_USERDEFINED)
                this.pdf.newPage(this.userDefinedWidth, this.userDefinedHeight, this.userDefinedMarginLeft, 
                		this.userDefinedMarginRight, this.userDefinedMarginTop, this.userDefinedMarginBottom);
            else
                this.pdf.newPage(this.pageStyle, this.userDefinedMarginLeft, this.userDefinedMarginRight, 
                		this.userDefinedMarginTop, this.userDefinedMarginBottom);
            drawPreDefinedLine();
            drawPreDefinedLabel();
            this.pageInFile = this.pdf.getPageCount();
            this.pageCount = this.pdf.getTotalPages(); //在設置 this.pdf.setTotalPages() 後, 求最新的總頁數數值
        } catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    /**
     * 產生一新的套用指定格式檔畫面頁, 並於新頁畫框線及 Label 字串(固定位置).
     * @param rptFormatFileName 新頁欲套用的格式檔檔名.
     */
    public void newPage(String rptFormatFileName) {
        try {
            if(rptFormatFileName != null && !"".equals(rptFormatFileName))
                loadRptData(getFileForRead(_rptDir, rptFormatFileName));
            newPage();
        } catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    /**
     * 結束產生報表, 並作必要的處理.<br>
     * 注意: 如果至呼叫本 method 為止的輸出檔總頁數為 0, 仍會自動呼叫 newPage()　以產生一具備格線及 LABEL 文字的空白頁.
     */
    public void close() {
    	try {
    		if(getPageCount() == 0)
    			newPage();
            this.pdf.close();
            this.pdf = null;
            this.out.close();
            this.out = null;
        } catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        } finally {
            if(this.pdf != null) try { this.pdf.close(); } catch(PdfException pe) {}
            if(this.out != null) try { this.out.close(); } catch(IOException ie) {}
        }
    }
    
    /**
     * 塞一筆資料到 block 裡(只對當前頁有作用).
     * @param label 代表本 block 的 key (index)
     * @param data 文字資料
     * @param xOffset 相對於 BLOCK_INFO 裡定義的文字框左上角的 x 坐標的值 (單位: mm)
     * @param yOffset 相對於 BLOCK_INFO 裡定義的文字框左上角的 y 坐標的值 (單位: mm)
     * @return Xprint 物件自身
     */
    public Xprint add(String label, String data, double xOffset, double yOffset) {
        try {
        	if(!checkUseTemplate())
        		return this;
        		
            double[] v = this.blockInfo.get(label);
            if(v == null)
            	throw new Exception("no such label in template: " + label);
            float x = (float)v[2] + (float)xOffset + this.originOffsetX;
            float y = (float)v[3] + (float)yOffset + this.originOffsetY;
            float fontSize = tuneFontSize((float)v[10]);
            int colorBGR = (int)v[0];
            int[] color = (colorBGR != 0) ? new int[] { colorBGR & 0xFF, (colorBGR >> 8) & 0xFF, (colorBGR >> 16) & 0xFF } : //RGB
            	this.pdf.isDebug() ? PdfConst.COLOR_GREEN : PdfConst.COLOR_BLACK;
            this.pdf.drawTextBox(data, fontSize, x, y, (float)v[4], (float)v[5], (float)v[6], (float)v[7], 
            		decideTextDirectionStyle((int)v[1]), 
            		(int)v[8], 
            		decideFontType((int)v[9]), 
            		PdfConst.FONT_STYLE_NORMAL, 
            		color);
        } catch(Throwable t) {
        	handleException(t);
        }
        return this;
    }
    
    /** @see #add(String, String, double, double) */
    public Xprint add(String label, String data, Double xOffset, Double yOffset) {
    	return add(label, data, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset);
    }
    
    /**
     * 塞一筆資料到 block 裡(只對當前頁有作用).
     * @param label 代表本 block 的 key (index)
     * @param data 文字資料
     * @return Xprint 物件自身
     */
    public Xprint add(String label, String data) {
    	return add(label, data, 0D, 0D);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(39 碼, 只在當前頁面有作用).
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode(String label, String barcodeData, double xOffset, double yOffset, double width, double height) {
    	try {
    		if(!checkUseTemplate())
        		return this;
    		
    		double[] v = this.blockInfo.get(label);
            if(v == null)
            	throw new Exception("no such label in template: " + label);
            float x = (float)v[2] + (float)xOffset + this.originOffsetX;
            float y = (float)v[3] + (float)yOffset + this.originOffsetY;
            float w = (width == 0D) ? (float)v[4] : (float)width;
            float h = (height == 0D) ? (float)v[5] : (float)height;
            this.pdf.drawBarcode39(barcodeData, (float)x, (float)y, w, h);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #addBarCode(String, String, double, double, double, double) */
    public Xprint addBarCode(String label, String barcodeData, Double xOffset, Double yOffset, Double width, Double height) {
    	return addBarCode(label, barcodeData, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(39 碼, 只在當前頁面有作用), 寬度及高度受 block 大小限制.
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode(String label, String barcodeData, double xOffset, double yOffset) {
    	return addBarCode(label, barcodeData, xOffset, yOffset, 0D, 0D);
    }
    
    /** @see #addBarCode(String, String, double, double) */
    public Xprint addBarCode(String label, String barcodeData, Double xOffset, Double yOffset) {
    	return addBarCode(label, barcodeData, xOffset, yOffset, null, null);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(39 碼, 只在當前頁面有作用), 寬度及高度受 block 大小限制.
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @return Xprint 物件自身
     */
    public Xprint addBarCode(String label, String barcodeData) {
    	return addBarCode(label, barcodeData, 0D, 0D, 0D, 0D);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼 (25 碼, 只在當前頁面有作用).
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode25(String label, String barcodeData, double xOffset, double yOffset, double width, double height) {
    	try {
    		if(!checkUseTemplate())
        		return this;
    		
    		double[] v = this.blockInfo.get(label);
            if(v == null)
            	throw new Exception("no such label in template: " + label);
            float x = (float)v[2] + (float)xOffset + this.originOffsetX;
            float y = (float)v[3] + (float)yOffset + this.originOffsetY;
            float w = (width == 0D) ? (float)v[4] : (float)width;
            float h = (height == 0D) ? (float)v[5] : (float)height;
            this.pdf.drawBarcode25(barcodeData, (float)x, (float)y, w, h);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #addBarCode25(String, String, double, double, double, double) */
    public Xprint addBarCode25(String label, String barcodeData, Double xOffset, Double yOffset, Double width, Double height) {
    	return addBarCode25(label, barcodeData, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(25 碼, 只在當前頁面有作用), 寬度及高度受 block 大小限制.
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode25(String label, String barcodeData, double xOffset, double yOffset) {
    	return addBarCode25(label, barcodeData, xOffset, yOffset, 0D, 0D);
    }
    
    /** @see #addBarCode25(String, String, double, double) */
    public Xprint addBarCode25(String label, String barcodeData, Double xOffset, Double yOffset) {
    	return addBarCode25(label, barcodeData, xOffset, yOffset, null, null);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(25 碼, 只在當前頁面有作用), 寬度及高度受 block 大小限制.
     * @param label 放置條碼之 block 的 key (index)
     * @return Xprint 物件自身
     */
    public Xprint addBarCode25(String label, String barcodeData) {
    	return addBarCode25(label, barcodeData, 0, 0, 0, 0);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入印條碼 (128 碼, 只在當前頁面有作用).
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode128(String label, String barcodeData, double xOffset, double yOffset, double width, double height) {
    	try {
    		if(!checkUseTemplate())
        		return this;
    		
    		double[] v = this.blockInfo.get(label);
            if(v == null)
            	throw new Exception("no such label in template: " + label);
            float x = (float)v[2] + (float)xOffset + this.originOffsetX;
            float y = (float)v[3] + (float)yOffset + this.originOffsetY;
            float w = (width == 0D) ? (float)v[4] : (float)width;
            float h = (height == 0D) ? (float)v[5] : (float)height;
            this.pdf.drawBarcode128(barcodeData, (float)x, (float)y, w, h);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #addBarCode128(String, String, double, double, double, double) */
    public Xprint addBarCode128(String label, String barcodeData, Double xOffset, Double yOffset, Double width, Double height) {
    	return addBarCode128(label, barcodeData, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(128 碼, 只在當前頁面有作用), 寬度及高度受 block 大小限制.
     * @param label 放置條碼之 block 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode128(String label, String barcodeData, double xOffset, double yOffset) {
    	return addBarCode128(label, barcodeData, xOffset, yOffset, 0D, 0D);
    }
    
    /** @see #addBarCode128(String, String, double, double) */
    public Xprint addBarCode128(String label, String barcodeData, Double xOffset, Double yOffset) {
    	return addBarCode128(label, barcodeData, xOffset, yOffset, null, null);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(128 碼, 只在當前頁面有作用), 寬度及高度受 block 大小限制.
     * @param label 放置條碼之 block 的 key (index)
     * @return Xprint 物件自身
     */
    public Xprint addBarCode128(String label, String barcodeData) {
    	return addBarCode128(label, barcodeData, 0D, 0D, 0D, 0D);
    }
    
    /**
     * 在當前頁貼圖, 自行指定位置, 圖型伸縮以填滿圖框.
     * @param imgFileName 圖形檔檔名
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @param width 貼圖後的圖寬 (mm)
     * @param height 貼圖後的圖高 (mm)
     * @return Xprint 物件自身
     */
    public Xprint addImage(String imgFileName, double x, double y, double width, double height) {
    	try {
    		if(this.currentImgFilename == null || !this.currentImgFilename.equals(imgFileName)) {
    			this.currentImgFilename = imgFileName;
    			this.currentImgPath = getFileForRead(_imgDir, imgFileName).getAbsolutePath();
    		}
    		this.pdf.addImage(this.currentImgPath, (float)x, (float)y, (float)width, (float)height);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #addImage(String, double, double, double, double) */
    public Xprint addImage(String imgPath, Double x, Double y, Double width, Double height) {
    	return addImage(imgPath, (x == null) ? 0D : x, (y == null) ? 0D : y, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在當前頁貼圖, 自行指定位置, 大小比照原圖檔.
     * @param imgFileName 圖形檔檔名
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @return Xprint 物件自身
     */
    public Xprint addImage(String imgFileName, double x, double y) {
    	try {
    		if(this.currentImgFilename == null || !this.currentImgFilename.equals(imgFileName)) {
    			this.currentImgFilename = imgFileName;
    			this.currentImgPath = getFileForRead(_imgDir, imgFileName).getAbsolutePath();
    		}
    		this.pdf.addImage(this.currentImgPath, (float)x, (float)y);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #addImage(String, double, double) */
    public Xprint addImage(String imgPath, Double x, Double y) {
    	return addImage(imgPath, (x == null) ? 0D : x, (y == null) ? 0D : y);
    }
    
    /**
     * 畫線條 (只在當前頁面有作用).
     * @param lineStyle LINE_STYLE_SOLID 或 LINE_STYLE_DASH 或 LINE_STYLE_DOT 或 LINE_STYLE_DASHDOT 或 LINE_STYLE_DASHDOTDOT
     * @param lineWidth 線寬 (單位: mm)
     * @param xStart 線條起點 x 坐標 (單位: mm)
     * @param yStart 線條起點 y 坐標 (單位: mm)
     * @param xEnd 線條終點 x 坐標 (單位: mm)
     * @param yEnd 線條終點 y 坐標 (單位: mm)
     * @param colorRGB 顏色碼(紅綠藍, 各自範圍 0-255)
     * @see #LINE_STYLE_SOLID
     * @see #LINE_STYLE_DASH
     * @see #LINE_STYLE_DOT
     * @see #LINE_STYLE_DASHDOT
     * @see #LINE_STYLE_DASHDOTDOT
     * @return Xprint 物件自身
     */
    public Xprint addLine(int lineStyle, double lineWidth, double xStart, double yStart, double xEnd, double yEnd, int[] colorRGB) {
    	try {
    		float x1 = (float)xStart + this.originOffsetX;
            float y1 = (float)yStart + this.originOffsetY;
            float x2 = (float)xEnd + this.originOffsetX;
            float y2 = (float)yEnd + this.originOffsetY;
            this.pdf.drawLine(x1, y1, x2, y2, tuneLineWidth(lineWidth), decidePdfLineStyle(lineStyle), (colorRGB == null) ? PdfConst.COLOR_BLACK : colorRGB);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /**
     * 在當前頁畫線.
     * @see #addLine(int, double, double, double, double, double, int[])
     */
    public Xprint addLine(Integer lineStyle, Double lineWidth, Double xStart, Double yStart, Double xEnd, Double yEnd, Integer[] colorRGB) {
    	int[] color = null;
    	if(colorRGB != null)
    		color = new int[] { (colorRGB.length < 1 || colorRGB[0] == null) ? 0 : colorRGB[0], (colorRGB.length < 2 || colorRGB[1] == null) ? 0 : colorRGB[1], (colorRGB.length < 3 || colorRGB[2] == null) ? 0 : colorRGB[2] };
    	return addLine((lineStyle == null) ? Xprint.LINE_STYLE_SOLID : lineStyle, (lineWidth == null) ? 0D : lineWidth, 
    			(xStart == null) ? 0D : xStart, (yStart == null) ? 0D : yStart, 
				(xEnd == null) ? 0D : xEnd, (yEnd == null) ? 0D : yEnd,
				color);
    }

    /**
     * 在當前頁畫黑色線.
     * @see #addLine(int, double, double, double, double, double, int[])
     */
    public Xprint addLine(int lineStyle, double lineWidth, double xStart, double yStart, double xEnd, double yEnd) {
    	return addLine(lineStyle, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁畫黑色線.
     * @see #addLine(int, double, double, double, double, double, int[])
     */
    public Xprint addLine(Integer lineStyle, Double lineWidth, Double xStart, Double yStart, Double xEnd, Double yEnd) {
    	return addLine(lineStyle, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁畫黑色實線.
     * @see #addLine(int, double, double, double, double, double, int[])
     */
    public Xprint addLine(double lineWidth, double xStart, double yStart, double xEnd, double yEnd) {
        return addLine(Xprint.LINE_STYLE_SOLID, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁畫黑色實線.
     * @see #addLine(int, double, double, double, double, double, int[])
     */
    public Xprint addLine(Double lineWidth, Double xStart, Double yStart, Double xEnd, Double yEnd) {
    	return addLine((Integer)null, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁加一筆文字(第二行及以後要內縮), 自行指定位置, 直到字串結束為止, 過了頁底自動換頁, 從最頂端開始(x 坐標與內縮後起始位置相同).<br>
     * @param data 文字資料
     * @param fontSize 字型大小 (單位: mm)
     * @param x 文字起點左上角 x 坐標 (單位: mm)
     * @param y 文字起點左上角 y 坐標 (單位: mm)
     * @param yThreshold 文字行下緣的 y 坐標至換頁前的門檻(單位: mm)
     * @param yRestart 換頁後, 文字起點上緣 y 坐標(單位: mm)
     * @param boundWidth 文字區寬 (單位: mm)
     * @param indent 第一次折行後內縮的寬度, 負數者成為外凸 (單位: mm)
     * @param fontType 字型, 可為 FONT_MING, FONT_KAI
     * @param colorRGB 顏色碼(紅綠藍, 各自範圍 0-255)
     * @return 印完最後一行時的文字下緣的 y 坐標 (單位: mm)
     */
    public double addFlow(String data, double fontSize, double x, double y, double yThreshold, 
    		double yRestart, double boundWidth, double indent, int fontType, int[] colorRGB) {
    	try {
	    	return this.pdf.drawTextFlow(data, (float)fontSize, (float)x, (float)y, 
	    			(float)indent, (float)boundWidth, (float)yRestart, this.pdf.getPageHeight() - (float)yThreshold,
	    			fontType, PdfConst.FONT_STYLE_NORMAL, this.debug ? PdfConst.COLOR_DARK_ORANGE : PdfConst.COLOR_BLACK, 
	    			new PdfGeneratorOnNewPage() {
						@Override
						public int beforeNewPageForDrawTextFlow(PdfGenerator pdf) {
							newPage();
							return PdfGeneratorOnNewPage.SUPPRESS_NEW_PAGE; //在本 Xprint 物內處理換頁, 不令底層的 PdfGenerator 自行換頁
						}
			    	});
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return y; //出錯時, 只單純傳回原預定填入文字的位置的左上角 y 坐標值
    }
    
    /** 
     * 在當前頁加一筆文字.
     * @see #addFlow(String, double, double, double, double, double, double, double, int, int[]) */
    public double addFlow(String data, Double fontSize, Double x, Double y, Double yThreshold, 
    		Double yRestart, Double boundWidth, Double indent, Integer fontType, Integer[] colorRGB) {
    	int[] color = null;
    	if(colorRGB != null)
    		color = new int[] { (colorRGB.length < 1 || colorRGB[0] == null) ? 0 : colorRGB[0], (colorRGB.length < 2 || colorRGB[1] == null) ? 0 : colorRGB[1], (colorRGB.length < 3 || colorRGB[2] == null) ? 0 : colorRGB[2] };
    	return addFlow(data, 
    			(fontSize == null) ? 0D : fontSize, (x == null) ? 0D : x, 
    			(y == null) ? 0D : y, (yThreshold == null) ? 0D : yThreshold, 
        		(yRestart == null) ? 0D : yRestart, (boundWidth == null) ? 0D : boundWidth, 
				(indent == null) ? 0D : indent, (fontType == null) ? FONT_MING : fontType,
				color);
    }
    
    /**
     * 在當前頁加一筆黑色文字.
     * @see #addFlow(String, double, double, double, double, double, double, double, int, int[])
     */
    public double addFlow(String data, double fontSize, double x, double y, double yThreshold, 
    		double yRestart, double boundWidth, double indent, int fontType) {
    	return addFlow(data, fontSize, x, y, yThreshold, yRestart, boundWidth, indent, fontType, null);
    }

    /**
     * 在當前頁列印一筆黑色文字.
     * @see #addFlow(String, double, double, double, double, double, double, double, int, int[])
     */
    public double addFlow(String data, Double fontSize, Double x, Double y, Double yThreshold, 
    		Double yRestart, Double boundWidth, Double indent, Integer fontType) {
    	return addFlow(data, fontSize, x, y, yThreshold, yRestart, boundWidth, indent, fontType, null);
    }
    
    /**
     * 在當前頁加一筆文字(第二行及以後要內縮), 自行指定位置, 直到字串結束為止, 過了頁底自動換頁, 從最頂端開始(x 坐標與內縮後起始位置相同).
     * @param data 文字資料
     * @param fontSize 字型大小 (單位: mm)
     * @param x 文字起點左上角 x 坐標 (單位: mm)
     * @param y 文字起點左上角 y 坐標 (單位: mm)
     * @param boundWidth 文字區寬 (單位: mm)
     * @param indent 第一次折行後內縮的寬度, 負數者成為外凸 (單位: mm)
     * @param fontType 字型, 可為 FONT_MING, FONT_KAI
     * @param colorRGB 顏色碼(紅綠藍, 各自範圍 0-255)
     * @return 印完最後一行時的文字下緣的 y 坐標 (單位: mm)
     */
    public double addFlow(String data, double fontSize, double x, double y, double boundWidth, 
    		double indent, int fontType, int[] colorRGB) {
    	return addFlow(data, fontSize, x, y, this.pdf.getPageHeight() - this.pdf.getMarginBottom(), 
        		this.pdf.getMarginTop(), boundWidth, indent, fontType, colorRGB);
    }
    
    /**
     * 在當前頁加一筆文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #addFlow(String, double, double, double, double, double, int, int[])
     */
    public double addFlow(String data, Double fontSize, Double x, Double y, Double boundWidth, 
    		Double indent, Integer fontType, Integer[] colorRGB) {
    	return addFlow(data, fontSize, x, y, (double)(this.pdf.getPageHeight() - this.pdf.getMarginBottom()), 
        		(double)this.pdf.getMarginTop(), boundWidth, indent, fontType, colorRGB);
    }
    
    /**
     * 在當前頁加一筆黑色文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #addFlow(String, double, double, double, double, double, int, int[])
     */
    public double addFlow(String data, double fontSize, double x, double y, double boundWidth, 
    		double indent, int fontType) {
    	return addFlow(data, fontSize, x, y, this.pdf.getPageHeight() - this.pdf.getMarginBottom(), 
        		this.pdf.getMarginTop(), boundWidth, indent, fontType, null);
    }
    
    /**
     * 在當前頁加一筆黑色文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #addFlow(String, double, double, double, double, double, int, int[])
     */
    public double addFlow(String data, Double fontSize, Double x, Double y, Double boundWidth, 
    		Double indent, Integer fontType) {
    	return addFlow(data, fontSize, x, y, (double)(this.pdf.getPageHeight() - this.pdf.getMarginBottom()), 
        		(double)this.pdf.getMarginTop(), boundWidth, indent, fontType, (Integer[])null);
    }
    
    /**
     * 加浮水印, 呼叫 newPage() 後每頁均有作用(在起新頁前呼叫).
     * @param imgFileName 圖形檔檔名(但需於最初始時已執行 Xprint.setStaticallyImgDir(), 否則自 classpath 下找圖檔)
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     */
    public void assignWatermark(String imgFileName, double x, double y) {
    	this.watermarkImg = getFileForRead(_imgDir, imgFileName).getAbsolutePath();
    	this.watermarkX = x;
    	this.watermarkY = y;
    	assignWatermark();
    }
    
    /**
     * 加浮水印.
     * @see #assignWatermark(String, double, double)
     */
    public void assignWatermark(String imgFileName, Double x, Double y) {
    	assignWatermark(imgFileName, (x == null) ? 0D : x, (y == null) ? 0D : y);
    }
    
    void assignWatermark() {
    	if(this.watermarkImg != null)
    		this.pdf.setWatermark(this.watermarkImg, (float)this.watermarkX, (float)this.watermarkY);
    }

    /**
     * 加騎縫章, 只需執行一次.
     * @param imgFileName 騎縫章圖檔檔名檔名(但需於最初始時已執行 Xprint.setStaticallyImgDir(), 否則自 classpath 下找圖檔)
     * @param option 印騎縫章的方式, 可指定 PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY, PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY,
     * 				PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY, PERFORATION_SEAL_BOTH_SIDE_VERTICALLY
     * @see #PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY
     * @see #PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY
     * @see #PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY
     * @see #PERFORATION_SEAL_BOTH_SIDE_VERTICALLY
     */
    public void assignPerforationSeal(String imgFileName, int option) {
    	String perforationSealImg = getFileForRead(_imgDir, imgFileName).getAbsolutePath();
    	this.pdf.setPerforationSeal(perforationSealImg);
    	this.pdf.setPrintPerforationSealOnSingleSide(option == PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY || option == PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY);
    	this.pdf.setPrintPerforationSealOnTopDown(option == PERFORATION_SEAL_BOTH_SIDE_VERTICALLY || option == PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY);
    }
    
    /**
     * 取輸出檔存放實體路徑, 在 Xprint 物件生成時決定(最頂層基準路徑下接一層不定名稱的目錄).
     * 其最頂層基準路徑在 AP 初始化階段藉 Xprint.setStaticallyOutputDir(String) 決定。
     * ＠see {@link Xprint#setStaticallyOutputDir(String)}
     */
    public File getOutputDir() {
    	if(this.outputDir == null) {
    		if(_outputDir == null)
    			_outputDir = new File(System.getProperty("java.io.tmpdir"));
    		File tmpDir = new File(_outputDir, String.valueOf(System.nanoTime()));
    		tmpDir.mkdirs();
    		this.outputDir = tmpDir;
    	}
    	return this.outputDir;
    }
    
    /** 取輸出的 PDF 檔. */
    public File[] getOutputFiles() {
    	return this.outputFiles.toArray(new File[this.outputFiles.size()]);
    }
    
    /** 取輸出文件總頁數. */
    public int getPageCount() {
        return this.pageCount;
    }
    
    //載入格式檔
    void loadRptData(File rptFile) throws Exception {
        BufferedReader in = null;

        try {
            this.lineInfo = new ArrayList<double[]>();
            this.labelInfo = new ArrayList<String[]>();
            this.blockInfo = new HashMap<String, double[]>();
            in = new BufferedReader(new InputStreamReader(new FileInputStream(rptFile), this.rptCharset));
            String line = null;
            if(this.debug)
            	System.out.println("read rpt file: '" + rptFile.getAbsolutePath() + "' as " + this.rptCharset);

            //先讀到有文字的第一行
            for(int i = 0; i < 100; i++) { //限定在 100 行內一定要讀到有資料之行
                line = in.readLine();
                if(line != null && !line.trim().equals(""))
                    break;
                if(line == null || i == 99)
                    throw new Exception("the rpt file contains no data");
            }

            //讀取 rpt 第一行資料, 抓出 xoffset 與 yoffset(單位: pt), 並且起新頁
            Matcher mainInfoMatcher = Pattern.compile("\\s*([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\s*").matcher(line);
            if(mainInfoMatcher.matches()) {
                String layoutOpt = mainInfoMatcher.group(1);
                if("9".equals(layoutOpt)) {
                    //此時後面兩個數值代表紙寬與紙高
                    this.pageStyle = PdfConst.PAGE_USERDEFINED;
                    this.userDefinedWidth = Float.parseFloat(mainInfoMatcher.group(2));
                    this.userDefinedHeight = Float.parseFloat(mainInfoMatcher.group(3));
                    this.userDefinedMarginLeft = -1;
                	this.userDefinedMarginRight = -1;
                	this.userDefinedMarginTop = -1;
                	this.userDefinedMarginBottom = -1;
                    this.originOffsetX = this.originOffsetY = 0;
                } else {
                    this.pageStyle = PdfConst.PAGE_A4; //default
                    switch(Integer.parseInt(layoutOpt)) {
                        case 0: this.pageStyle = PdfConst.PAGE_A4; break;
                        case 1: this.pageStyle = PdfConst.PAGE_A4_LANDSCAPE; break;
                        case 2: this.pageStyle = PdfConst.PAGE_B4_LANDSCAPE; break;
                        case 3:
                        case 4: this.pageStyle = PdfConst.PAGE_A3; break;
                        case 5: this.pageStyle = PdfConst.PAGE_A3_LANDSCAPE; break;
                    }
                    this.userDefinedWidth = -1;
                    this.userDefinedHeight = -1;
                    this.userDefinedMarginLeft = -1;
                	this.userDefinedMarginRight = -1;
                	this.userDefinedMarginTop = -1;
                	this.userDefinedMarginBottom = -1;
                    this.originOffsetX = Float.parseFloat(mainInfoMatcher.group(2));
                    this.originOffsetY = Float.parseFloat(mainInfoMatcher.group(3));
                }
            }

            //逐行解析
            Matcher lineInfoMatcher = Pattern.compile("\\s*LINE_INFO\\t(\\d+)\\s*").matcher("");
            Matcher lineDataMatcher = Pattern.compile("\\s*(\\d+)\\t([\\d\\.]+)\\t([-\\d\\.]+)\\t([-\\d\\.]+)\\t([-\\d\\.]+)\\t([-\\d\\.]+)\\s*").matcher("");
            Matcher labelStringMatcher = Pattern.compile("\\s*LABEL_STRING\\t(\\d+)\\s*").matcher("");
            Matcher labelDataMatcher = Pattern.compile("\\s*(\\d+)\\t([-\\d\\.]+)\\t([-\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t(\\d+)\\t(\\d+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t(.*)").matcher("");
            Matcher blockInfoMatcher = Pattern.compile("\\s*BLOCK_INFO\\t(\\d+)\\s*").matcher("");
            Matcher blockDataMatcher = Pattern.compile("\\s*(\\d+)\\t([-\\d\\.]+)\\t([-\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t(\\d+)\\t(\\d+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t([\\d\\.]+)\\t(.*)").matcher("");
            while((line = in.readLine()) != null) {
                //LINE_INFO 資訊(不變動資料)
                if(lineInfoMatcher.reset(line).matches()) {
                    int j = Integer.parseInt(lineInfoMatcher.group(1));
                    for(int k = 0; k < j; k++) {
                        line = in.readLine();
                        if(lineDataMatcher.reset(line).matches()) {
                        	String appearance = lineDataMatcher.group(1); //\d*\d{1}: [顏色BGR的十進位表示法] + style
                        	int style = Integer.parseInt(appearance.substring(appearance.length() - 1));
                        	int color = (appearance.length() > 1) ? Integer.parseInt(appearance.substring(0, appearance.length() - 1)) : 0;
                            this.lineInfo.add(new double[] { color, style, 
                            		Double.parseDouble(lineDataMatcher.group(2)), Double.parseDouble(lineDataMatcher.group(3)), 
                            		Double.parseDouble(lineDataMatcher.group(4)), Double.parseDouble(lineDataMatcher.group(5)), 
                            		Double.parseDouble(lineDataMatcher.group(6)) });
                        }
                    }
                }

                //LABEL_STRING 資訊(不變動資料)
                if(labelStringMatcher.reset(line).matches()) {
                    int j = Integer.parseInt(labelStringMatcher.group(1));
                    for(int k = 0; k < j; k++) {
                        line = in.readLine();
                        if(labelDataMatcher.reset(line).matches()) {
                        	String appearance = labelDataMatcher.group(1); //\d*\d{1}: [顏色BGR的十進位表示法] + 排列方式
                        	String direction = appearance.substring(appearance.length() - 1);
                        	String color = (appearance.length() > 1) ? appearance.substring(0, appearance.length() - 1) : "0";
                            this.labelInfo.add(new String[] { color, direction, labelDataMatcher.group(2), 
                            		labelDataMatcher.group(3), labelDataMatcher.group(4), labelDataMatcher.group(5), 
                                    labelDataMatcher.group(6), labelDataMatcher.group(7), labelDataMatcher.group(8), 
                                    labelDataMatcher.group(9), labelDataMatcher.group(10), labelDataMatcher.group(11), 
                                    labelDataMatcher.group(12), labelDataMatcher.group(13) });
                        }
                    }
                }

                //BLOCK_INFO 資訊
                if(blockInfoMatcher.reset(line).matches()) {
                    int j = Integer.parseInt(blockInfoMatcher.group(1));
                    for(int k = 0; k < j; k++) {
                        line = in.readLine();
                        if(blockDataMatcher.reset(line).matches()) {
                            String label = blockDataMatcher.group(13); //label 文字
                            String appearance = blockDataMatcher.group(1); //\d*\d{1}: [顏色BGR的十進位表示法] + style
                            int direction = Integer.parseInt(appearance.substring(appearance.length() - 1));
                            int color = (appearance.length() > 1) ? Integer.parseInt(appearance.substring(0, appearance.length() - 1)) : 0;
                            this.blockInfo.put(label, new double[] { color, direction, 
                            		Double.parseDouble(blockDataMatcher.group(2)), Double.parseDouble(blockDataMatcher.group(3)), 
                            		Double.parseDouble(blockDataMatcher.group(4)), Double.parseDouble(blockDataMatcher.group(5)), 
                            		Double.parseDouble(blockDataMatcher.group(6)), Double.parseDouble(blockDataMatcher.group(7)), 
                            		Double.parseDouble(blockDataMatcher.group(8)), Double.parseDouble(blockDataMatcher.group(9)), 
                            		Double.parseDouble(blockDataMatcher.group(10)), Double.parseDouble(blockDataMatcher.group(11)), 
                            		Double.parseDouble(blockDataMatcher.group(12)) });
                        }
                    }
                }
            }
            in.close();
            in = null;
        } finally {
            if(in != null) in.close();
        }
    }
    
    //換檔輸出, 第二檔及其後, 在副檔名 ".pdf" 之前依序插入 ".1", ".2", ...
    File newOutput() throws Exception {
    	try {
			//原 PDF 物件的一些設定
			final float[] pgNumTopology = (this.pdf == null) ? null : this.pdf.getPgNumTopology(); //頁碼
			final int pageNumberPosition = (this.pdf == null) ? PdfConst.PAGE_NUMBER_NONE : this.pdf.getPageNumberPosition();
			final String perforationSeal = (this.pdf == null) ? null : this.pdf.getPerforationSeal(); //騎縫章
			final float[] perforationSealPosition = (this.pdf == null) ? null : this.pdf.getPerforationSealPosition();
    		final boolean printPerforationSealOnSingleSide = (this.pdf == null) ? false : this.pdf.isPrintPerforationSealOnSingleSide();
    		final boolean printPerforationSealOnTopDown = (this.pdf == null) ? false : this.pdf.isPrintPerforationSealOnTopDown();

    		if(this.outputFiles.size() != 0) { //當前輸出檔關閉, 再開新檔
    			close(); //關閉 this.pdf, this.out
    			this.outputFile = getFileForWrite(getOutputDir(), this.outputFilePrefix + "." + this.outputFiles.size() + this.outputFilePostfix);
    		}
			this.outputFiles.add(this.outputFile);
			this.out = new BufferedOutputStream(new FileOutputStream(this.outputFile));
	        this.pdf = new PdfGenerator(this.out);
			this.pdf.setDefaultFont(_fontNamePlane0);
			this.pdf.setDefaultKaiFont(_fontNameKaiPlane0);
			this.pdf.setPlane2Font(_fontNamePlane2);
			this.pdf.setKaiPlane2Font(_fontNameKaiPlane2);
			this.pdf.setPlane15Font(_fontNamePlane15);
			this.pdf.setKaiPlane15Font(_fontNameKaiPlane15);
			this.pdf.setTotalPages(this.pageCount); //設置總頁數(通知 PdfGenerator 物件, 目前實際總頁數為何)
			this.pdf.setPgNumTopology(pgNumTopology);
			this.pdf.setPageNumberPosition(pageNumberPosition);
			this.pdf.setPerforationSeal(perforationSeal, perforationSealPosition);
			this.pdf.setPrintPerforationSealOnSingleSide(printPerforationSealOnSingleSide);
			this.pdf.setPrintPerforationSealOnTopDown(printPerforationSealOnTopDown);

			this.pageInFile = 0; //初始化當前 PDF 檔的頁數
			assignWatermark();
			if(this.debug) {
				System.out.println("write to '" + this.outputFile.getAbsolutePath() + "'");
				this.pdf.setDebug(debug);
			}
			
			return this.outputFile;
    	} catch(Exception e) {
    		if(this.pdf != null) try { this.pdf.close(); } catch(Throwable t) {}
    		if(this.out != null) try { this.out.close(); } catch(Throwable t) {}
    		throw e;
    	}
    }
    
    //畫格式檔已預定的線
    void drawPreDefinedLine() throws Exception {
    	if(this.lineInfo == null)
    		return;
        for(double[] v : this.lineInfo) {
        	int colorBGR = (int)v[0];
        	int[] color = (colorBGR == 0) ? PdfConst.COLOR_BLACK : new int[] { colorBGR & 0xFF, (colorBGR >> 8) & 0xFF, (colorBGR >> 16) & 0xFF }; //RGB
            this.pdf.drawLine((float)v[3] + this.originOffsetX, (float)v[4] + this.originOffsetY, 
            		(float)v[5] + this.originOffsetX, (float)v[6] + this.originOffsetY, 
            		tuneLineWidth((float)v[2]), decidePdfLineStyle((int)v[1]), 
            		color);
        }
    }
    
    //畫格式檔已預定的 label 文字
    void drawPreDefinedLabel() throws Exception {
    	if(this.labelInfo == null)
    		return;
        for(String[] ss : this.labelInfo) {
            String text = ss[13];
            float fontSize = tuneFontSize(Float.parseFloat(ss[10]));
            float x = Float.parseFloat(ss[2]) + this.originOffsetX;
            float y = Float.parseFloat(ss[3]) + this.originOffsetY;
            float boundWidth = Float.parseFloat(ss[4]);
            float boundHeight = Float.parseFloat(ss[5]);
            float rowGap = Float.parseFloat(ss[6]);
            float charSpacing = Float.parseFloat(ss[7]);
            int textDirection = decideTextDirectionStyle(Integer.parseInt(ss[1]));
            int distribute = Integer.parseInt(ss[8]);
            int fontType = decideFontType(Integer.parseInt(ss[9]));
            int colorBGR = Integer.parseInt(ss[0]);
            int[] color = (colorBGR != 0) ? new int[] { colorBGR & 0xFF, (colorBGR >> 8) & 0xFF, (colorBGR >> 16) & 0xFF } : //RGB
            	this.debug ? PdfConst.COLOR_BLUE : PdfConst.COLOR_BLACK;
            this.pdf.drawTextBox(text, fontSize, x, y, boundWidth, boundHeight, rowGap, charSpacing, textDirection, distribute, fontType, PdfConst.FONT_STYLE_NORMAL, color);
        }
    }
    
    //決定字型(明/楷體)
    //font: Xprint 格式檔裡的字型號碼
    int decideFontType(int font) {
    	switch(font) {
    		case 3:
    		case 1: return PdfConst.FONT_KAI;
    		case 2:
    		case 0:
    		default: return PdfConst.FONT_MING;
    	}
    }
    
    int decideTextDirectionStyle(int n) {
    	switch(n) {
    		case 1: return PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY;
    		case 2: return PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY;
    		case 3: return PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY;
    		default: return PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY;
    	}
    }
    
    //調整字型大小(應憑實際操作表格製作軟體得來的經驗值來修改)
    float tuneFontSize(float fontSize) {
        return fontSize * 0.907F;
    }
    
    //調整線寬
    float tuneLineWidth(double lineWidth) {
        return (float)lineWidth / 2;
    }

    //Xprint 的線條型式代碼轉為 PdfGenerator 的線條型式代碼
    int decidePdfLineStyle(int xprintLineStyle) {
    	switch(xprintLineStyle) {
	        case 1: return PdfConst.LINE_DASH;
	        case 2: return PdfConst.LINE_DOT;
	        case 3: return PdfConst.LINE_DASH_DOT;
	        case 4: return PdfConst.LINE_DASH_DOT_DOT;
	        default: return PdfConst.LINE_SOLID;
        }
    }
    
    File getFileForWrite(File dir, String fileName) {
    	File f = new File(fileName);
    	if(!f.isAbsolute()) {
    		if(dir == null)
    			throw new IllegalArgumentException("dir path must be specified");
    		f = new File(dir, fileName);
    	}
    	File p = f.getParentFile();
    	if(p != null && !p.exists())
    		p.mkdirs();
    		
    	return f;
    }
    
    boolean checkUseTemplate() {
    	if(this.blockInfo == null) {
    		log.error("rpt template file not given");
    		return false;
    	}
    	return true;
    }
    
    void handleException(Throwable t) {
		if(!this.coverException) {
    		if(t instanceof XprintException)
    			throw (XprintException)t;
			throw new XprintException(t.getMessage(), t);
    	} else {
    		log.error(t.getMessage(), t);
    	}
    }
    
    /**
     * @param dir 來源檔案所在目錄. 如果未指定(null)者, 則自當前 classlocader 下的 classpath 根目錄找(當 fileName 為相對路徑時), 或直接找 fileName(當 fileName 為絕對路徑時).
     * @param fileName 來源檔案
     */
    static File getFileForRead(File dir, String fileName) {
    	if(dir != null)
    		return new File(dir, fileName);
    	
    	File f = new File(fileName);
    	if(f.isAbsolute())
    		return f;
    	
		URL url = Thread.currentThread().getContextClassLoader().getResource(fileName); //NOTE: Tomcat 7- 對於 fileName 是否為絕對路徑型式會得出不同的結果
		return (url == null) ? f : new File(url.getPath());
    }
}
