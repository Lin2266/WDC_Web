package com.test.commons.pdf2;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.regex.Matcher;

/**
 * 讀取大同文稿格式檔(內容長度單位 mm, 預設編碼 Big5)及資料(預設編碼 UTF8)後,
 * 輸出 PDF 檔(預設超過 DEFAULT_MAX_PAGES_PER_OUTPUT_FILE 頁即拆檔).<br>
 * 一般程式中藉 Xprint 填入資料並產生 PDF 檔, 例:
 * <pre><code>
 * {@literal @}Resource(name="xprintBuilder")
 * private <b>XprintBuilderBean</b> xprintBuilder; //取已登錄於 Spring context 內的 XprintBuilderBean 物件
 * ...
 * 
 * try (Xprint xp = this.xprintBuilder.build("format.txt", "output.pdf") { //自動關閉資源
 *     List&lt;VO&gt; data = ...; //欲填入 PDF 內容的資料
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
 */
public class Xprint implements AutoCloseable {
	private static Log log = LogFactory.getLog(Xprint.class);

	public static final String DEFAULT_RPT_CHARSET = "UTF-8";
	
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
    /** 印騎縫章於每頁左右兩側, 第 1 頁自左側開始蓋印 */
    public static final int PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_LEFT = 1;
    /** 印騎縫章於每頁左右兩側, 第 1 頁自右側開始蓋印 */
    public static final int PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT = 2;
    /** 印騎縫章於每頁上/下單側(用於雙面列印, 上翻) */
    public static final int PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY = 3;
    /** 印騎縫章於每頁上下兩側 */
    public static final int PERFORATION_SEAL_BOTH_SIDE_VERTICALLY = 4;
    
    /** 字體: 明體 */
    public static final int FONT_MING = PdfConst.FONT_MING;
    /** 字體: 楷體 */
    public static final int FONT_KAI = PdfConst.FONT_KAI;
    
    /** 字形: 一般 */
    public static final int FONT_STYLE_NORMAL = PdfConst.FONT_STYLE_NORMAL;
    /** 字形: 粗體 */
    public static final int FONT_STYLE_BOLD = PdfConst.FONT_STYLE_BOLD;
    /** 字形: 斜體 */
    public static final int FONT_STYLE_ITALIC = PdfConst.FONT_STYLE_ITALIC;
    /** 字形: 粗斜體 */
    public static final int FONT_STYLE_BOLDITALIC = PdfConst.FONT_STYLE_BOLDITALIC;
    /** 字形: 加刪除線 */
    public static final int FONT_STYLE_STRIKETHRU = PdfConst.FONT_STYLE_STRIKETHRU;
    /** 字形: 加底線 */
    public static final int FONT_STYLE_UNDERLINE = PdfConst.FONT_STYLE_UNDERLINE;
    
    /** 文字自左至右而下排列 */
    public static int TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY = PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY;
    /** 文字自右至左而下排列 */
    public static int TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY = PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY;
    /** 文字自上至下而左排列 */
    public static int TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY = PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY;
    /** 文字自上至下而右排列 */
    public static int TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY = PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY;
    
    /** 文字分佈: 由文字框角落開始排列 */
    public static final int DISTRIBUTED_DEFAULT = PdfConst.XPRINT_DISTRIBUTED_DEFAULT;
    /** 文字分佈: 只對水平排列文字有作用, (1)文字列未折行時: 水平均佈, 垂直居中; (2)文字列有折行時: 水平緊靠, 行間垂直均佈, 首尾行觸框 */
    public static final int DISTRIBUTED_1 = PdfConst.XPRINT_DISTRIBUTED_1;
    /** 文字分佈: 只對垂直排列文字有作用, (1)文字列未折行時: 文字水平居中, 垂直均佈; (2)字元垂直緊靠, 行間水平均佈, 首尾行之外與框之間有間隙 */
    public static final int DISTRIBUTED_2 = PdfConst.XPRINT_DISTRIBUTED_2;
    /** 文字分佈: 不均佈, 只對水平排列文字有作用, (1)文字列未折行時: 文字水平垂直皆居中; (2)文字列有折行時: 文字垂直居中 */
    public static final int DISTRIBUTED_3 = PdfConst.XPRINT_DISTRIBUTED_3;
    /** 文字分佈: 不均佈, 只對垂直排列文字有作用, (1)文字列未折行時: 文字水平垂直皆居中; (2)文字列有折行時: 文字水平居中 */
    public static final int DISTRIBUTED_4 = PdfConst.XPRINT_DISTRIBUTED_4;
    /** 文字分佈: 水平靠右, 垂直置中 */
    public static final int DISTRIBUTED_5 = PdfConst.XPRINT_DISTRIBUTED_5;
    /** 文字分佈: 垂直分佈, 邊界對齊(橫書); 水平分佈, 邊界對齊(直書) */
    public static final int DISTRIBUTED_6 = PdfConst.XPRINT_DISTRIBUTED_6;
    /** 文字分佈: 水平靠左, 垂直置中 */
    public static final int DISTRIBUTED_7 = PdfConst.XPRINT_DISTRIBUTED_7;
    
    /** 顏色: 黑色 */
    public static final int[] COLOR_BLACK = PdfConst.COLOR_BLACK;
    /** 顏色: 白色 */
    public static final int[] COLOR_WHITE = PdfConst.COLOR_WHITE;
    /** 顏色: 紅色 */
    public static final int[] COLOR_RED = PdfConst.COLOR_RED;
    /** 顏色: 緣色 */
    public static final int[] COLOR_GREEN = PdfConst.COLOR_GREEN;
    /** 顏色: 藍色 */
    public static final int[] COLOR_BLUE = PdfConst.COLOR_BLUE;
    /** 顏色: 黃金色 */
    public static final int[] COLOR_GOLD = PdfConst.COLOR_GOLD;
    /** 顏色: 暗橘色 */
    public static final int[] COLOR_DARK_ORANGE = PdfConst.COLOR_DARK_ORANGE;
    /** 顏色: 暗夜藍 */
    public static final int[] COLOR_MIDNIGHT_BLUE = PdfConst.COLOR_MIDNIGHT_BLUE;
    
    /** 內容靠左 */
    public static int ALIGNMENT_LEFT = PdfConst.ALIGNMENT_LEFT;
    /** 內容水平置中 */
    public static int ALIGNMENT_CENTER = PdfConst.ALIGNMENT_CENTER;
    /** 內容水平靠右 */
    public static int ALIGNMENT_RIGHT = PdfConst.ALIGNMENT_RIGHT;

    private PdfGenerator pdf;

    private File outputBaseDir; //輸出檔實際放置的基準實體目錄
    private File rptFormatFileDir; //Xprint 格式檔所在實體目錄. 未指定者, 視指定的格式檔名為絕對路徑, 否則以 Class.getResource(name) 的方式取格式檔
    private File imageFileDir; //欲嵌入報表的圖檔所在的實體目錄. 未指定者, 視指定的圖檔名為絕對路徑, 否則以 Class.getResource(name) 的方式取圖檔
    private String fontNamePlane0; //預設字型名稱, 或該字型的字型檔之完整實體路徑
    private String fontNameKaiPlane0; //預設楷體字型名稱, 或該字型的字型檔之完整實體路徑
    private String fontNamePlane2; //unicode 第二字面字型名稱, 或該字型的字型檔之完整實體路徑
    private String fontNameKaiPlane2; //楷體 unicode 第二字面字型名稱, 或該字型的字型檔之完整實體路徑
    private String fontNamePlane15; //unicode 第 15 字面字型名稱, 或該字型的字型檔之完整實體路徑
    private String fontNameKaiPlane15; //楷體 unicode 第 15 字面字型名稱, 或該字型的字型檔之完整實體路徑
    
    private int pageStyle; //PdfGenerator.PAGE_*
    private float userDefinedWidth; //自定義紙寬(mm)
    private float userDefinedHeight; //自定義紙高(mm)
    private float userDefinedMarginLeft; //自定義左邊界(mm)
    private float userDefinedMarginRight; //自定義右邊界(mm)
    private float userDefinedMarginTop; //自定義上邊界(mm)
    private float userDefinedMarginBottom; //自定義下邊界(mm)
    private String rptFormatFileCharset; //Xprint 格式檔編碼. 未指定者, 使用 UTF-8 碼
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
    private String outputFileSuffix; //輸出檔名之後段
    private String currentImgPath; //最近一次貼圖的檔案實體路徑
    private PagingHandler beforePagingHandler;
    private PagingHandler afterPagingHandler;
    private PageNumberHandler pageNumberHandler; //自訂頁碼文字框
    private boolean printPageNumberOnFirstPage; //首頁是否需列印頁碼?
    private boolean debug; //運行中印出額外資訊, PDF 內的文字框加虛線框以供識別
    private boolean coverException; //掩蓋製作 PDF 內容過程中的 exception(預設 false)
    
    /**
     * 使用大同印表格式檔 讓資料套用, 輸出 PDF 檔.
     * @param outputBaseDir 輸出檔實際放置的基準實體目錄(optional, 未指定者使用 OS 暫存目錄)
     * @param rptFormatFileDir 格式檔所在目錄(optional, 未指定者, 指定的格式檔名須為絕對路徑, 否則以 Class.getResource() 的方式試取)
     * @param imageFileDir 圖檔所在目錄(optional, 未指定者, 指定的圖檔檔名須為絕對路徑, 否則以 Class.getResource() 的方式試取)
     * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
     * @param rptFormatFilename 印表格式檔(optional). 
     * 		  null 值, 則使用預設紙張(A4)、預設邊界長, 且只能使用不依賴格式檔的 drawFlow(), drawLine(), 其他如 add() 等加字串及條碼等功能一概無用
     * @param rptFormatFileCharset 印表格式檔之字元編碼
     */
    public Xprint(final File outputBaseDir, final File rptFormatFileDir, final File imageFileDir, 
    		final String outputFilename, final String rptFormatFilename, 
    		final String rptFormatFileCharset) {
    	try {
    		if(rptFormatFileDir != null && !rptFormatFileDir.isDirectory())
    			throw new IllegalArgumentException("argument 'rptFormatFileDir' does not refer to a existed directory");
    		if(outputBaseDir != null && !outputBaseDir.isDirectory())
    			throw new IllegalArgumentException("argument 'outputDir' does not refer to a existed directory");
    		if(imageFileDir != null && !imageFileDir.isDirectory())
    			throw new IllegalArgumentException("argument 'imageFileBaseDir' does not refer to a existed directory");
    		if(outputFilename == null)
    			throw new IllegalArgumentException("argument 'outputFilename' not specified");
    		
    		this.rptFormatFileDir = rptFormatFileDir;
			this.outputBaseDir = ((outputBaseDir != null) ? Files.createTempDirectory(outputBaseDir.toPath(), "xprint-") : Files.createTempDirectory("xprint-")).toFile();
			this.imageFileDir = imageFileDir;
    		
    		//將輸出檔名折分為主檔名, 副檔名(含點號)二段
    		{
    			int i = outputFilename.lastIndexOf('.');
    			if(i != -1) {
    				this.outputFilePrefix = outputFilename.substring(0, i);
    				this.outputFileSuffix = outputFilename.substring(i);
    			} else {
    				this.outputFilePrefix = outputFilename;
    				this.outputFileSuffix = ".pdf";
    			}
    		}
    		
    		//確定當輸出檔
    		this.outputFile = getFileForWrite(this.outputBaseDir, this.outputFilePrefix + this.outputFileSuffix);
    		if(!this.outputFile.createNewFile()) { //輸出檔一旦已存在者, 主檔名末加 1, 2, ... 直至無重複檔名為止
    			for(int i = 1; ; i++) {
    				if((this.outputFile = getFileForWrite(this.outputBaseDir, this.outputFilePrefix + i + this.outputFileSuffix)).createNewFile()) { //仍不保證多人環境中, 不會發生輸出檔名一致的情況
    					this.outputFilePrefix += i;
    					break;
    				}
    			}
    		}
    		
    		this.outputFiles = new ArrayList<File>();
    		this.rptFormatFileCharset = (rptFormatFileCharset == null) ? DEFAULT_RPT_CHARSET : rptFormatFileCharset;
    		this.pageCount = 0; //this.pageInFile 在 newOutput() 中設初始值
    		this.maxPages = DEFAULT_MAX_PAGES_PER_OUTPUT_FILE;
    		this.coverException = false;
    		this.pageStyle = PdfConst.DEFAULT_PAGE_SIZE;
			this.userDefinedWidth = -1;
            this.userDefinedHeight = -1;
            this.userDefinedMarginLeft = -1;
        	this.userDefinedMarginRight = -1;
        	this.userDefinedMarginTop = -1;
        	this.userDefinedMarginBottom = -1;
            
    		//依格式檔而決定 this.pageStyle, this.userDefinedWidth, this.userDefinedHeight, this.userDefinedMarginLeft, this.userDefinedMarginRight, this.userDefinedMarginTop, this.userDefinedMarginBottom, this.originOffsetX, this.originOffsetY
    		if(rptFormatFilename != null) //讀入格式檔
    			loadRptData(rptFormatFilename);
    	} catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    ///**
    // * 使用大同印表格式檔 讓資料套用, 輸出 PDF 檔.
    // * @param rptFormatFilename 印表格式檔名(內容編碼為 Big5)
    // * 		  null 值者, 則使用預設紙張(A4)、預設邊界長, 且只能使用不依賴格式檔的 drawFlow(), drawLine(), 其他如 add() 等加字串及條碼等功能一概無用
    // * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
    // */
    //public Xprint(final String rptFormatFilename, final String outputFilename) {
    //    this((File)null, (File)null, (File)null, outputFilename, rptFormatFilename, null);
    //}
    
    /**
     * 不使用格式檔, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
     * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
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
    public Xprint(final String outputFilename, final int pageSize, final double marginLeft, 
    		final double marginRight, final double marginTop, final double marginBottom) {
    	this((File)null, (File)null, (File)null, outputFilename, (String)null, (String)null);
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
    
    ///**
    // * 不使用格式檔, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
    // * 使用預設四邊界長度.
    // * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
    // * @param pageSize PDF 初始紙張大小設定(可在起新頁時自行指定紙張樣式), 可為: 
    // * 		PAGE_A3, PAGE_A4, PAGE_B3, PAGE_B4, PAGE_A3_LANDSCAPE, 
    // * 		PAGE_A4_LANDSCAPE, PAGE_B3_LANDSCAPE, PAGE_B4_LANDSCAPE 之一
    // * @see #PAGE_A3
    // * @see #PAGE_A4
    // * @see #PAGE_B3
    // * @see #PAGE_B4
    // * @see #PAGE_A3_LANDSCAPE
    // * @see #PAGE_A4_LANDSCAPE
    // * @see #PAGE_B3_LANDSCAPE
    // * @see #PAGE_B4_LANDSCAPE
    // * @see PdfConst#DEFAULT_MARGIN_LEFT
    // * @see PdfConst#DEFAULT_MARGIN_RIGHT
    // * @see PdfConst#DEFAULT_MARGIN_TOP
    // * @see PdfConst#DEFAULT_MARGIN_BOTTOM
    // */
    //public Xprint(final String outputFilename, final int pageSize) {
    //	this(outputFilename, pageSize, -1D, -1D, -1D, -1D);
    //}
    
    ///**
    // * 不使用格式檔, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
    // * 使用預設紙張(A4)及預設四邊界長度.
    // * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
    // * @see PdfConst#DEFAULT_MARGIN_LEFT
    // * @see PdfConst#DEFAULT_MARGIN_RIGHT
    // * @see PdfConst#DEFAULT_MARGIN_TOP
    // * @see PdfConst#DEFAULT_MARGIN_BOTTOM
    // */
    //public Xprint(final String outputFilename) {
    //	this(outputFilename, PAGE_A4, -1D, -1D, -1D, -1D);
    //}
    
    /**
     * 不使用格式檔, 自訂紙張大小, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
     * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦同名檔案已存在者, 主檔名將附加數字以區別</b>
     * @param pageWidth 頁寬(單位: mm)
     * @param pageHeight 頁高(單位: mm)
     * @param marginLeft 左邊界長(mm)
     * @param marginRight 右邊界長(mm)
     * @param marginTop 上邊界長(mm)
     * @param marginBottom 下邊界長(mm)
     */
    public Xprint(final String outputFilename, final double pageWidth, final double pageHeight, 
    		final double marginLeft, final double marginRight, final double marginTop, 
    		final double marginBottom) {
    	this((File)null, (File)null, (File)null, outputFilename, (String)null, (String)null);
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
    
    ///**
    // * 不使用格式檔, 自訂紙張大小, 預設四邊界長, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
    // * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
    // * @param pageWidth 頁寬(單位: mm)
    // * @param pageHeight 頁高(單位: mm)
    // * @see PdfConst#DEFAULT_MARGIN_LEFT
    // * @see PdfConst#DEFAULT_MARGIN_RIGHT
    // * @see PdfConst#DEFAULT_MARGIN_TOP
    // * @see PdfConst#DEFAULT_MARGIN_BOTTOM
    // */
    //public Xprint(final String outputFilename, final double pageWidth, final double pageHeight) {
    //	this(outputFilename, pageWidth, pageHeight, -1D, -1D, -1D, -1D);
    //}
    
	/**
     * 指定預設字型檔(以 Unicode BMP 為主)所在的實體路徑.<br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-uming/uming.ttc,0
     * </code>
     */
    public void setDefaultFont(final String fontName) {
		this.fontNamePlane0 = fontName;
    }
    
    /**
     * 指定楷體預設字型檔(以 Unicode BMP 為主)所在的實體路徑.<br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-ukai/ukai.ttc,0
     * </code>
     */
    public void setDefaultKaiFont(final String fontName) {
		this.fontNameKaiPlane0 = fontName;
    }
    
    /** 指定 Unicode 第 2 字面字型檔所在的實體路徑. <br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-uming/uming.ttc,0
     * </code>
     */
    public void setPlane2Font(final String fontName) {
		this.fontNamePlane2 = fontName;
    }
    
    /** 指定楷體 Unicode 第 2 字面字型檔所在的實體路徑. <br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-uming/uming.ttc,0
     * </code>
     */
    public void setPlane2KaiFont(final String fontName) {
		this.fontNameKaiPlane2 = fontName;
    }
    
    /** 指定 Unicode 第 15 字面字型檔所在的實體路徑. <br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-uming/uming.ttc,0
     * </code>
     */
    public void setPlane15Font(final String fontName) {
		this.fontNamePlane15 = fontName;
    }
    
    /** 指定楷體 Unicode 第 15 字面字型檔所在的實體路徑. <br>
     * 如果字型檔為內含多種字型的 ttc 檔, 需明確指定, 例:<br><code>
     * /usr/share/fonts/cjkuni-uming/uming.ttc,0
     * </code>
     */
    public void setPlane15KaiFont(final String fontName) {
		this.fontNameKaiPlane15 = fontName;
    }
    
    /**
     * 指定頁碼位置(只單純顯示數字, 不能控制額外的內容, 如總頁數等資訊)
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
    	ensurePdfSetup();
    	this.pdf.setPageNumberPosition(pageNumberPosition);
    }
    
    /**
     * 自訂頁碼區塊位置, 字型, 字寬, 額外內容等 (<b>注意: 必須在第一次呼叫 newPage() 之前呼本函數)</b>).<br>
     * <b>注意</b>: 執行本 method 後, 將強制只能輸出一個 PDF 檔(無分頁多 PDF 檔輸出功能了)
     * @param textTemplate 頁碼文字區塊樣版, 文字中可含下列佔位符號:
     *   <ul>
     *     <li>${pageNo}  (當前頁頁碼)
     *     <li>${totalPages}   (總頁數)
     *   </ul>
     *   例: <code>第 ${pageNo} 頁、共 ${totalPages} 頁</code>
     * @param x 文字框左側距紙張左邊界的距離 (單位: mm)
	 * @param lowerBondFromBottom 文字框距紙張底部的距離(單位: mm)
	 * @param fontType FONT_MING 或 FONT_KAI 之一
	 * @param fontSize 全形字寬 (單位: mm)
     */
    public void setPageNumberPosition(final String textTemplate, final double x, final double lowerBondFromBottom, final int fontType, final double fontSize) {
    	if(this.pdf != null)
    		throw new IllegalStateException("this method must be called right after this Xprint being instanciated");
    	this.maxPages = Integer.MAX_VALUE; //頁碼區塊含總頁數資訊, 強制只輸出一個 PDF 檔
    	log.warn("user-defined pageNumber layout been set, the output files will be exact 1 file correspondingly");
    	this.pageNumberHandler = new PageNumberHandler(textTemplate, x, lowerBondFromBottom, fontType, fontSize);
    }

    /** 如果要印頁碼時, 首頁是否需列印頁碼(預設: 否) */
    public void setPrintPageNumberOnFirstPage(boolean printPageNumberOnFirstPage) {
		this.printPageNumberOnFirstPage = printPageNumberOnFirstPage;
	}

	/** 設為 true 者, 將在所有文字外圍加虛線框, 以供觀察文字位置 */
    public void setDebug(final boolean debug) {
    	this.debug = debug;
    	ensurePdfSetup();
    	this.pdf.setDebug(debug);
    	if(debug)
			System.out.println("write to '" + this.outputFile.getAbsolutePath() + "'");
    }
    
    /** 掩蓋製作 PDF 內容文字, 及畫線過程中的 exception, 儘量地輸出 PDF 檔(預設 false) */
    public void setCoverException(final boolean coverException) {
    	this.coverException = coverException;
    }
    
    /**
     * 加浮水印, 呼叫 newPage() 後每頁均有作用(在起新頁前呼叫).
     * @param imgFilePath 圖形檔完整路徑
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     */
    public void setWatermark(final String imgFilePath, final double x, final double y) {
    	if(imgFilePath == null ||imgFilePath.length() == 0)
    		return;
    	ensurePdfSetup();
    	this.pdf.setWatermark(imgFilePath, (float)x, (float)y);
    }
    
    /**
     * 加浮水印, 呼叫 newPage() 後每頁均有作用(在起新頁前呼叫).
     * @param img 騎縫章圖檔來源
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     */
    public void setWatermark(final InputStream img, final double x, final double y) {
    	ensurePdfSetup();
    	this.pdf.setWatermark(img, (float)x, (float)y);
    }
    
    /**
     * 加浮水印.
     * @see #setWatermark(String, double, double)
     */
    public void setWatermark(final String imgFilePath, final Double x, final Double y) {
    	setWatermark(imgFilePath, (x == null) ? 0D : x, (y == null) ? 0D : y);
    }
    
    /**
     * 加騎縫章, 只需執行一次.
     * @param imgFilePath
     * @see #setPerforationSeal(String, double, double, int)
     */
    public void setPerforationSeal(final String imgFilePath, final int option) {
    	setPerforationSeal(imgFilePath, 0, 0, option);
    }
    
    /**
     * 加騎縫章, 只需執行一次.
     * @param img 騎縫章圖檔來源
     * @see #setPerforationSeal(InputStream, double, double, int)
     */
    public void setPerforationSeal(final InputStream img, final int option) {
    	setPerforationSeal(img, 0, 0, option);
    }
    
    /**
     * 加騎縫章, 只需執行一次.
     * @param imgFilePath 騎縫章圖檔完整路徑
     * @param width 印在 PDF 上的圖寬(單位: mm)
     * @param height 印在 PDF 上的圖高(單位: mm)
     * @param option 印騎縫章的方式, 可指定下列之一 PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY, PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_LEFT,
     * 				PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT, PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY, PERFORATION_SEAL_BOTH_SIDE_VERTICALLY
     * @see #PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY
     * @see #PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_LEFT
     * @see #PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT
     * @see #PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY
     * @see #PERFORATION_SEAL_BOTH_SIDE_VERTICALLY
     */
    public void setPerforationSeal(final String imgFilePath, final double width, final double height, final int option) {
    	if(imgFilePath == null || imgFilePath.length() == 0)
    		return;
    	ensurePdfSetup();
		this.pdf.setPerforationSeal(imgFilePath, (float)width, (float)height);
    	this.pdf.setPrintPerforationSealOnSingleSide(option == PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY || option == PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY);
    	this.pdf.setPrintPerforationSealOnTopDown(option == PERFORATION_SEAL_BOTH_SIDE_VERTICALLY || option == PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY);
    	this.pdf.setPrintPerforationSealOnFirstPageFromRight(option == PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT);
    }
    
    /**
     * 加騎縫章, 只需執行一次.
     * @param img 騎縫章圖檔來源
     * @param width 印在 PDF 上的圖寬(單位: mm)
     * @param height 印在 PDF 上的圖高(單位: mm)
     * @param option 印騎縫章的方式, 可指定下列之一 PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY, PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_LEFT,
     * 				PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT, PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY, PERFORATION_SEAL_BOTH_SIDE_VERTICALLY
     * @see #PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY
     * @see #PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_LEFT
     * @see #PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT
     * @see #PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY
     * @see #PERFORATION_SEAL_BOTH_SIDE_VERTICALLY
     */
    public void setPerforationSeal(final InputStream img, final double width, final double height, final int option) {
    	ensurePdfSetup();
		this.pdf.setPerforationSeal(img, (float)width, (float)height);
    	this.pdf.setPrintPerforationSealOnSingleSide(option == PERFORATION_SEAL_SINGLE_SIDE_HORIZONTALLY || option == PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY);
    	this.pdf.setPrintPerforationSealOnTopDown(option == PERFORATION_SEAL_BOTH_SIDE_VERTICALLY || option == PERFORATION_SEAL_SINGLE_SIDE_VERTICALLY);
    	this.pdf.setPrintPerforationSealOnFirstPageFromRight(option == PERFORATION_SEAL_BOTH_SIDE_HORIZONTALLY_FROM_RIGHT);
    }
    
    /**
     * TODO: 1.在 drawXXX() 正在執行中的場合, 換檔輸出時, 尚未完全印出的資料將會丟失.
     * TODO: 2.換擋輸出時, 每個輸出檔最後一頁 和下個輸出檔第一頁仍要蓋騎縫章(如果有指定的話)
     * <p>
     * 修改輸出檔最大容許頁數.<br>
     * 已設定自訂頁碼樣式時, 固定輸出一個 PDF 檔, 不讓修改每個 PDF 檔頁數設定.
     * @param pages
     * @see #DEFAULT_MAX_PAGES_PER_OUTPUT_FILE
     */
    public void setMaxPagesPerFile(final int pages) {
    	if(this.pageNumberHandler != null)
    		throw new IllegalStateException("user-defined page-no layout been set, there can only be one output file, and you can't change the max pages per file setting");
    	this.maxPages = pages;
    	throw new IllegalStateException("not supported yet"); //待 drawXXX() + 換檔輸出丟失資料的問題解後再開發
    }
    
    /**
     * 產生一新頁, 並於新頁畫框線及 Label 字串(固定位置).
     */
    public void newPage() {
        try {
        	if(this.beforePagingHandler != null)
        		this.beforePagingHandler.execute();
        	
        	if(!ensurePdfSetup() && this.pageInFile >= this.maxPages) //換檔輸出
        		setupNewOutputPDF();
            if(this.pageStyle == PdfConst.PAGE_USERDEFINED) {
                this.pdf.newPage(this.userDefinedWidth, this.userDefinedHeight, this.userDefinedMarginLeft, 
                		this.userDefinedMarginRight, this.userDefinedMarginTop, this.userDefinedMarginBottom);
            } else {
                this.pdf.newPage(this.pageStyle, this.userDefinedMarginLeft, this.userDefinedMarginRight, 
                		this.userDefinedMarginTop, this.userDefinedMarginBottom);
            }
            drawPreDefinedLine();
            drawPreDefinedLabel();
            this.pageInFile = this.pdf.getPageCount();
            this.pageCount = this.pdf.getTotalPages(); //在設置 this.pdf.setTotalPages() 後, 求最新的總頁數數值
            
            if(this.afterPagingHandler != null)
            	this.afterPagingHandler.execute();
        } catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    /**
     * 產生一新的套用指定格式檔畫面頁, 並於新頁畫框線及 Label 字串(固定位置).
     * @param rptFormatFilename 新頁欲套用的格式檔名
     */
    public void newPage(final String rptFormatFilename) {
        try {
            if(rptFormatFilename != null && rptFormatFilename.length() != 0)
                loadRptData(rptFormatFilename);
            newPage();
        } catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    /**
     * 產生一新的套用指定格式檔畫面頁, 並於新頁畫框線及 Label 字串(固定位置).
     * @param rptFormatResource 新頁欲套用的格式檔來源(可能來自實體路徑的檔案, 或含在 jar 中, 或其他任何來源). 本 method 呼叫者負責此來源的關閉動作
     */
    public void newPage(final InputStream rptFormatResource) {
        try {
            if(rptFormatResource != null)
                loadRptData(new BufferedReader(new InputStreamReader(rptFormatResource, this.rptFormatFileCharset)));
            newPage();
        } catch(Throwable t) {
            throw new XprintException(t.getMessage(), t);
        }
    }
    
    /**
     * 結束產生報表, 並作必要的處理.<br>
     * 注意: 如果至呼叫本 method 為止的輸出檔總頁數為 0, 仍會自動呼叫 newPage()　以產生一具備格線及 LABEL 文字的空白頁.
     */
    @Override
    public void close() {
    	try {
    		if(this.pdf == null)
    			return;
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
     * 塞一筆資料到 block 裡(配合 xprint 格式檔, 只對當前頁有作用).
     * @param label 代表本 BLOCK 的 key (index)
     * @param data 文字資料
     * @param xOffset 相對於 BLOCK_INFO 裡定義的文字框左上角的 x 坐標的值 (單位: mm)
     * @param yOffset 相對於 BLOCK_INFO 裡定義的文字框左上角的 y 坐標的值 (單位: mm)
     * @return Xprint 物件自身
     */
    public Xprint add(final String label, final String data, double xOffset, double yOffset) {
    	ensurePdfSetup();
    	
        try {
        	if(!checkUseTemplate())
        		return this;
        		
            final double[] v = this.blockInfo.get(label);
            if(v == null)
            	throw new Exception("no such label in template: " + label);
            final float x = (float)v[2] + (float)xOffset + this.originOffsetX;
            final float y = (float)v[3] + (float)yOffset + this.originOffsetY;
            final float fontSize = tuneFontSize((float)v[10]);
            final int colorBGR = (int)v[0];
            final int[] color = (colorBGR != 0) ? new int[] { colorBGR & 0xFF, (colorBGR >> 8) & 0xFF, (colorBGR >> 16) & 0xFF } : //RGB
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
    public Xprint add(final String label, final String data, final Double xOffset, final Double yOffset) {
    	return add(label, data, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset);
    }
    
    /**
     * 塞一筆資料到 block 裡(配合 xprint 格式檔, 只對當前頁有作用).
     * @param label 代表本 BLOCK 的 key (index)
     * @param data 文字資料
     * @return Xprint 物件自身
     */
    public Xprint add(final String label, final String data) {
    	return add(label, data, 0D, 0D);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 39 碼, 只在當前頁面有作用).
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode(final String label, final String barcodeData, final double xOffset, final double yOffset, final double width, final double height) {
    	ensurePdfSetup();
    	
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
    public Xprint addBarCode(final String label, final String barcodeData, final Double xOffset, final Double yOffset, final Double width, final Double height) {
    	return addBarCode(label, barcodeData, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 39 碼, 只在當前頁面有作用), 寬度及高度受 BLOCK 大小限制.
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode(final String label, final String barcodeData, final double xOffset, final double yOffset) {
    	return addBarCode(label, barcodeData, xOffset, yOffset, 0D, 0D);
    }
    
    /** @see #addBarCode(String, String, double, double) */
    public Xprint addBarCode(final String label, final String barcodeData, final Double xOffset, final Double yOffset) {
    	return addBarCode(label, barcodeData, xOffset, yOffset, null, null);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 39 碼, 只在當前頁面有作用), 寬度及高度受 BLOCK 大小限制.
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @return Xprint 物件自身
     */
    public Xprint addBarCode(final String label, final String barcodeData) {
    	return addBarCode(label, barcodeData, 0D, 0D, 0D, 0D);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼 (配合 xprint 格式檔, 25 碼, 只在當前頁面有作用).
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode25(final String label, final String barcodeData, final double xOffset, final double yOffset, final double width, final double height) {
    	ensurePdfSetup();
    	
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
    public Xprint addBarCode25(final String label, final String barcodeData, final Double xOffset, final Double yOffset, final Double width, final Double height) {
    	return addBarCode25(label, barcodeData, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 25 碼, 只在當前頁面有作用), 寬度及高度受 BLOCK 大小限制.
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode25(final String label, final String barcodeData, final double xOffset, final double yOffset) {
    	return addBarCode25(label, barcodeData, xOffset, yOffset, 0D, 0D);
    }
    
    /** @see #addBarCode25(String, String, double, double) */
    public Xprint addBarCode25(final String label, final String barcodeData, final Double xOffset, final Double yOffset) {
    	return addBarCode25(label, barcodeData, xOffset, yOffset, null, null);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 25 碼, 只在當前頁面有作用), 寬度及高度受 BLOCK 大小限制.
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @return Xprint 物件自身
     */
    public Xprint addBarCode25(final String label, final String barcodeData) {
    	return addBarCode25(label, barcodeData, 0, 0, 0, 0);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入印條碼 (配合 xprint 格式檔, 128 碼, 只在當前頁面有作用).
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode128(final String label, final String barcodeData, final double xOffset, final double yOffset, final double width, final double height) {
    	ensurePdfSetup();
    	
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
    public Xprint addBarCode128(final String label, final String barcodeData, final Double xOffset, final Double yOffset, final Double width, final Double height) {
    	return addBarCode128(label, barcodeData, (xOffset == null) ? 0D : xOffset, (yOffset == null) ? 0D : yOffset, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 128 碼, 只在當前頁面有作用), 寬度及高度受 BLOCK 大小限制.
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @param barcodeData
     * @param xOffset 相對於圖框左上角 x 坐標的位移量 (單位: mm).
     * @param yOffset 相對於圖框左上角 y 坐標的位移量 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint addBarCode128(final String label, final String barcodeData, final double xOffset, final double yOffset) {
    	return addBarCode128(label, barcodeData, xOffset, yOffset, 0D, 0D);
    }
    
    /** @see #addBarCode128(String, String, double, double) */
    public Xprint addBarCode128(final String label, final String barcodeData, final Double xOffset, final Double yOffset) {
    	return addBarCode128(label, barcodeData, xOffset, yOffset, null, null);
    }
    
    /**
     * 在格式檔 BLOCK 圖框填入條碼(配合 xprint 格式檔, 128 碼, 只在當前頁面有作用), 寬度及高度受 BLOCK 大小限制.
     * @param label 放置條碼之 BLOCK 的 key (index)
     * @return Xprint 物件自身
     */
    public Xprint addBarCode128(final String label, final String barcodeData) {
    	return addBarCode128(label, barcodeData, 0D, 0D, 0D, 0D);
    }
    
    /**
     * 在當前頁貼圖, 自行指定位置, 圖型伸縮以填滿圖框.
     * @param imgFilePath 圖形檔名
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @param width 貼圖後的圖寬 (mm)
     * @param height 貼圖後的圖高 (mm)
     * @return Xprint 物件自身
     */
    public Xprint drawImage(final String imgFilePath, final double x, final double y, final double width, final double height) {
    	ensurePdfSetup();
    	
    	try {
    		if(imgFilePath == null || imgFilePath.length() == 0) {
    			this.currentImgPath = null;
    			return this;
    		}
    		final File imgFile = getImageFile(imgFilePath);
    		if(!imgFile.isFile())
    			throw new FileNotFoundException("argument 'imgFilePath' does not refer to a regular file");
    		
    		final String imgPath = imgFile.getAbsolutePath();
    		if(this.currentImgPath == null || !this.currentImgPath.equals(imgPath))
    			this.currentImgPath = imgPath;
    		this.pdf.addImage(this.currentImgPath, (float)x, (float)y, (float)width, (float)height);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #drawImage(String, double, double, double, double) */
    public Xprint drawImage(final String imgFilePath, final Double x, final Double y, final Double width, final Double height) {
    	return drawImage(imgFilePath, (x == null) ? 0D : x, (y == null) ? 0D : y, 
    			(width == null) ? 0D : width, (height == null) ? 0D : height);
    }
    
    /**
     * 在當前頁貼圖, 自行指定位置, 大小比照原圖檔.
     * @param imgFilePath 圖形檔名
     * @param x 圖形左上角 x 坐標 (mm)
     * @param y 圖形左上角 y 坐標 (mm)
     * @return Xprint 物件自身
     */
    public Xprint drawImage(final String imgFilePath, final double x, final double y) {
    	ensurePdfSetup();
    	
    	try {
    		if(imgFilePath == null || imgFilePath.length() == 0) {
    			this.currentImgPath = null;
    			return this;
    		}
    		final File imgFile = getImageFile(imgFilePath);
    		if(!imgFile.isFile())
    			throw new FileNotFoundException("argument 'imgFilePath' does not refer to a regular file");
    		
    		final String imgPath = imgFile.getAbsolutePath();
    		if(this.currentImgPath == null || !this.currentImgPath.equals(imgPath))
    			this.currentImgPath = imgPath;
    		this.pdf.addImage(this.currentImgPath, (float)x, (float)y);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** @see #drawImage(String, double, double) */
    public Xprint drawImage(final String imgFilePath, final Double x, final Double y) {
    	return drawImage(imgFilePath, (x == null) ? 0D : x, (y == null) ? 0D : y);
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
    public Xprint drawLine(final int lineStyle, final double lineWidth, final double xStart, final double yStart, final double xEnd, final double yEnd, 
    		final int[] colorRGB) {
    	ensurePdfSetup();
    	
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
     * @see #drawLine(int, double, double, double, double, double, int[])
     */
    public Xprint drawLine(final Integer lineStyle, final Double lineWidth, final Double xStart, final Double yStart, final Double xEnd, final Double yEnd, 
    		final Integer[] colorRGB) {
    	int[] color = null;
    	if(colorRGB != null)
    		color = new int[] { (colorRGB.length < 1 || colorRGB[0] == null) ? 0 : colorRGB[0], (colorRGB.length < 2 || colorRGB[1] == null) ? 0 : colorRGB[1], (colorRGB.length < 3 || colorRGB[2] == null) ? 0 : colorRGB[2] };
    	return drawLine((lineStyle == null) ? Xprint.LINE_STYLE_SOLID : lineStyle, (lineWidth == null) ? 0D : lineWidth, 
    			(xStart == null) ? 0D : xStart, (yStart == null) ? 0D : yStart, 
				(xEnd == null) ? 0D : xEnd, (yEnd == null) ? 0D : yEnd,
				color);
    }

    /**
     * 在當前頁畫黑色線.
     * @see #drawLine(int, double, double, double, double, double, int[])
     */
    public Xprint drawLine(final int lineStyle, final double lineWidth, final double xStart, final double yStart, final double xEnd, final double yEnd) {
    	return drawLine(lineStyle, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁畫黑色線.
     * @see #drawLine(int, double, double, double, double, double, int[])
     */
    public Xprint drawLine(final Integer lineStyle, final Double lineWidth, final Double xStart, final Double yStart, final Double xEnd, final Double yEnd) {
    	return drawLine(lineStyle, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁畫黑色實線.
     * @see #drawLine(int, double, double, double, double, double, int[])
     */
    public Xprint drawLine(final double lineWidth, final double xStart, final double yStart, final double xEnd, final double yEnd) {
        return drawLine(Xprint.LINE_STYLE_SOLID, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁畫黑色實線.
     * @see #drawLine(int, double, double, double, double, double, int[])
     */
    public Xprint drawLine(final Double lineWidth, final Double xStart, final Double yStart, final Double xEnd, final Double yEnd) {
    	return drawLine((Integer)null, lineWidth, xStart, yStart, xEnd, yEnd, null);
    }
    
    /**
     * 在當前頁加一筆文字框
     * @param data 文字資料
     * @param fontSize 字型高 (單位: mm)
     * @param x 文字框左上角 x 坐標 (單位: mm)
     * @param y 文字框左上角 y 坐標 (單位: mm)
     * @param boundWidth 文字框寬 (單位: mm)
     * @param boundHeight 文字框高 (單位: mm)
     * @param lineGap 連續行之上行字元底部, 與下行字元頂部的距離(只在 distributed=XPRINT_DISTRIBUTED_DEFAULT 才有作用) (單位: mm)
     * @param charSpacing 字元間距(只在 distributed=DISTRIBUTED_DEFAULT 才有作用) (單位: mm)
     * @param textDirection 文字排列方向, 可為 TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY, TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY, TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY, TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY 之一
     * @param distributed 文字分佈. 可為 DISTRIBUTED_DEFAULT, DISTRIBUTED_1, DISTRIBUTED_2, DISTRIBUTED_3, DISTRIBUTED_4, DISTRIBUTED_5, DISTRIBUTED_6, DISTRIBUTED_7 之一
     * @param fontType 字型, 可為 FONT_MING, FONT_KAI 之一
     * @param fontStyle 文字樣式, 可為: FONT_STYLE_BOLD, FONT_STYLE_BOLDITALIC, FONT_STYLE_ITALIC, FONT_STYLE_NORMAL, FONT_STYLE_STRIKETHRU, FONT_STYLE_UNDERLINE 之一
     * @param colorRGB 文字顏色, 可自行指定 RGB 值(16進位的整數之陣列), 或指定 COLOR_BLACK, COLOR_WHITE, COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_GOLD, COLOR_DARK_ORANGE, COLOR_MIDNIGHT_BLUE 之一
     * @return Xprint 物件自身
     * @see #TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY(文字自左至右而下排列)
     * @see #TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY(文字自右至左而下排列)
     * @see #TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY(文字自上至下而左排列)
     * @see #TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY(文字自上至下而右排列)
     * @see #DISTRIBUTED_DEFAULT DISTRIBUTED_DEFAULT(由文字框角落開始排列)
     * @see #DISTRIBUTED_1
     * @see #DISTRIBUTED_2
     * @see #DISTRIBUTED_3
     * @see #DISTRIBUTED_4
     * @see #DISTRIBUTED_5
     * @see #DISTRIBUTED_6
     * @see #DISTRIBUTED_7
     * @see #FONT_MING FONT_MING(明體)
     * @see #FONT_KAI FONT_KAI(楷體)
     * @see #FONT_STYLE_NORMAL FONT_STYLE_NORMAL(正常字)
     * @see #FONT_STYLE_BOLD FONT_STYLE_BOLD(粗體字)
     * @see #FONT_STYLE_ITALIC FONT_STYLE_ITALIC(斜體字)
     * @see #FONT_STYLE_BOLDITALIC FONT_STYLE_BOLDITALIC(粗斜體字)
     * @see #FONT_STYLE_STRIKETHRU FONT_STYLE_STRIKETHRU(刪除字)
     * @see #FONT_STYLE_UNDERLINE FONT_STYLE_UNDERLINE(底線字)
     * @see #COLOR_BLACK COLOR_BLACK(黑色)
     * @see #COLOR_WHITE COLOR_WHITE(白色)
     * @see #COLOR_RED COLOR_RED(紅色)
     * @see #COLOR_GREEN COLOR_GREEN(綠色)
     * @see #COLOR_BLUE COLOR_BLUE(藍色)
     * @see #COLOR_GOLD COLOR_GOLD(金色)
     * @see #COLOR_DARK_ORANGE COLOR_DARK_ORANGE(暗橘色)
     * @see #COLOR_MIDNIGHT_BLUE COLOR_MIDNIGHT_BLUE(暗夜藍色)
     */
    public Xprint drawTextBox(final String data, final double fontSize, final double x, final double y, 
    		final double boundWidth, final double boundHeight, final double lineGap, final double charSpacing, 
    		final int textDirection, final int distributed, final int fontType, final int fontStyle, 
    		final int[] colorRGB) {
    	ensurePdfSetup();
    	
    	try {
	    	final int[] color = (colorRGB != null) ? colorRGB : //有指定字型顏色者
	    		this.pdf.isDebug() ? PdfConst.COLOR_GOLD : PdfConst.COLOR_BLACK; //預設黑色字
	    	this.pdf.drawTextBox(data, (float)fontSize, (float)x, (float)y, (float)boundWidth, (float)boundHeight, (float)lineGap, (float)charSpacing, textDirection, distributed, fontType, fontStyle, color);
    	} catch(Throwable t) {
        	handleException(t);
        }
        return this;
    }
    
    /** 
     * 在當前頁加一筆文字框
     * @see #drawTextBox(String, double, double, double, double, double, double, double, int, int, int, int, int[])
     */
    public Xprint drawTextBox(final String data, final Double fontSize, final Double x, final Double y, 
    		final Double boundWidth, final Double boundHeight, final Double lineGap, final Double charSpacing, 
    		final Integer textDirection, final Integer distributed, final Integer fontType, final Integer fontStyle, 
    		final Integer[] colorRGB) {
    	final int[] color = (colorRGB == null) ? null :
    		new int[] { (colorRGB.length < 1 || colorRGB[0] == null) ? 0 : colorRGB[0], (colorRGB.length < 2 || colorRGB[1] == null) ? 0 : colorRGB[1], (colorRGB.length < 3 || colorRGB[2] == null) ? 0 : colorRGB[2] };
    	return drawTextBox(data, 
    			(fontSize == null) ? 0D : fontSize, 
				(x == null) ? 0D : x, 
				(y == null) ? 0D : y, 
        		(boundWidth == null) ? 0D : boundWidth, 
				(boundHeight == null) ? 0D : boundHeight, 
				(lineGap == null) ? 0D : lineGap, 
				(charSpacing == null) ? 0D : charSpacing, 
        		(textDirection == null) ? TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY : textDirection, 
				(distributed == null) ? DISTRIBUTED_DEFAULT : distributed, 
				(fontType == null) ? FONT_MING : fontType, 
				(fontStyle == null) ? FONT_STYLE_NORMAL : fontStyle, 
        		color);
    }
    
    /**
     * 在當前頁加一筆文字框(自左而右横排)
     * @param data 文字資料
     * @param fontSize 字型高 (單位: mm)
     * @param x 文字框左上角 x 坐標 (單位: mm)
     * @param y 文字框左上角 y 坐標 (單位: mm)
     * @param boundWidth 文字框寬 (單位: mm)
     * @param boundHeight 文字框高 (單位: mm)
     * @param distributed 文字分佈. 可為 DISTRIBUTED_DEFAULT, DISTRIBUTED_1, DISTRIBUTED_2, DISTRIBUTED_3, DISTRIBUTED_4, DISTRIBUTED_5, DISTRIBUTED_6, DISTRIBUTED_7 之一
     * @param fontType 字型, 可為 FONT_MING, FONT_KAI 之一
     * @param fontStyle 文字樣式, 可為: FONT_STYLE_BOLD, FONT_STYLE_BOLDITALIC, FONT_STYLE_ITALIC, FONT_STYLE_NORMAL, FONT_STYLE_STRIKETHRU, FONT_STYLE_UNDERLINE 之一
     * @return Xprint 物件自身
     * 
     * @see #FONT_MING FONT_MING(明體)
     * @see #FONT_KAI FONT_KAI(楷體)
     * @see #FONT_STYLE_NORMAL FONT_STYLE_NORMAL(正常字)
     * @see #FONT_STYLE_BOLD FONT_STYLE_BOLD(粗體字)
     * @see #FONT_STYLE_ITALIC FONT_STYLE_ITALIC(斜體字)
     * @see #FONT_STYLE_BOLDITALIC FONT_STYLE_BOLDITALIC(粗斜體字)
     * @see #FONT_STYLE_STRIKETHRU FONT_STYLE_STRIKETHRU(刪除字)
     * @see #FONT_STYLE_UNDERLINE FONT_STYLE_UNDERLINE(底線字)
     */
    public Xprint drawTextBox(final String data, final double fontSize, final double x, final double y, final double boundWidth, final double boundHeight, 
    		final int distributed, final int fontType, final int fontStyle) {
    	return drawTextBox(data, fontSize, x, y, boundWidth, boundHeight, 0D, 0D, TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY, distributed, fontType, fontStyle, (int[])null);
    }
    
    /** 
     * 在當前頁加一筆文字框(自左而右横排)
     * @see #drawTextBox(String, double, double, double, double, double, int, int, int)
     */
    public Xprint drawTextBox(final String data, final Double fontSize, final Double x, final Double y, final Double boundWidth, final Double boundHeight, 
    		final Integer distributed, final Integer fontType, final Integer fontStyle) {
    	return drawTextBox(data, fontSize, x, y, boundWidth, boundHeight, (Double)null, (Double)null, (Integer)null, distributed, fontType, fontStyle, (Integer[])null);
    }
    
    /**
     * 在當前頁加一筆文字框(黑色, 靠框横排, 自左而右)
     * @param data 文字資料
     * @param fontSize 字型高 (單位: mm)
     * @param x 文字框左上角 x 坐標 (單位: mm)
     * @param y 文字框左上角 y 坐標 (單位: mm)
     * @param boundWidth 文字框寬 (單位: mm)
     * @param boundHeight 文字框高 (單位: mm)
     * @param fontType 字型, 可為 FONT_MING, FONT_KAI 之一
     * @return Xprint 物件自身
     * 
     * @see #FONT_MING FONT_MING(明體)
     * @see #FONT_KAI FONT_KAI(楷體)
     */
    public Xprint drawTextBox(final String data, final double fontSize, final double x, final double y, final double boundWidth, final double boundHeight, final int fontType) {
    	return drawTextBox(data, fontSize, x, y, boundWidth, boundHeight, 0D, 0D, TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY, DISTRIBUTED_DEFAULT, fontType, FONT_STYLE_NORMAL, null);
    }
    
    /**
     * 在當前頁加一筆文字框(黑色, 靠框横排, 自左而右)
     * @see #drawTextBox(String, double, double, double, double, double, int)
     */
    public Xprint drawTextBox(final String data, final Double fontSize, final Double x, final Double y, final Double boundWidth, final Double boundHeight, final Integer fontType) {
    	return drawTextBox(data, fontSize, x, y, boundWidth, boundHeight, (Double)null, (Double)null, (Integer)null, (Integer)null, fontType, (Integer)null, (Integer[])null);
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
     * @param lineGap 文字列間距(單位: mm)
     * @param alignment 文字排列方式, 可指定:
     * 		  ALIGNMENT_LEFT, ALIGNMENT_CENTER, ALIGNMENT_RIGHT
     * @param colorRGB 顏色碼(紅綠藍, 各自範圍 0-255), 或指定 COLOR_BLACK, COLOR_WHITE, COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_GOLD, COLOR_DARK_ORANGE, COLOR_MIDNIGHT_BLUE 之一
     * @return 印完最後一行時的文字最後字元下緣的坐標 (單位: mm)
     * 
     * @see #FONT_MING FONT_MING(明體)
     * @see #FONT_KAI FONT_KAI(楷體)
     * @see #COLOR_BLACK COLOR_BLACK(黑色)
     * @see #COLOR_WHITE COLOR_WHITE(白色)
     * @see #COLOR_RED COLOR_RED(紅色)
     * @see #COLOR_GREEN COLOR_GREEN(綠色)
     * @see #COLOR_BLUE COLOR_BLUE(藍色)
     * @see #COLOR_GOLD COLOR_GOLD(金色)
     * @see #COLOR_DARK_ORANGE COLOR_DARK_ORANGE(暗橘色)
     * @see #COLOR_MIDNIGHT_BLUE COLOR_MIDNIGHT_BLUE(暗夜藍色)
     * @see #ALIGNMENT_LEFT
     * @see #ALIGNMENT_CENTER
     * @see #ALIGNMENT_RIGHT
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double yThreshold, 
    		final double yRestart, final double boundWidth, final double indent, final int fontType, final double lineGap, 
    		final int alignment, final int[] colorRGB) {
    	ensurePdfSetup();
    	
    	try {
	    	final float[] ret = this.pdf.drawTextFlow(data, (float)fontSize, (float)x, (float)y, 
	    			(float)indent, (float)boundWidth, (float)yRestart, this.pdf.getPageHeight() - (float)yThreshold,
	    			fontType, PdfConst.FONT_STYLE_NORMAL, (float)lineGap, alignment,
	    			this.debug ? PdfConst.COLOR_DARK_ORANGE : PdfConst.COLOR_BLACK,
	    			new PdfGeneratorOnNewPage() {
						@Override
						public int beforeNewPageForDrawTextFlow(PdfGenerator pdf) {
							newPage();
							return PdfGeneratorOnNewPage.SUPPRESS_NEW_PAGE; //在本 Xprint 物內處理換頁, 不令底層的 PdfGenerator 自行換頁
						}
			    	});
	    	return new double[] { ret[0], ret[1] };
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return new double[] { x, y }; //出錯時, 只單純傳回原預定填入文字的位置的左上角 y 坐標值
    }
    
    /** 
     * 在當前頁加一筆文字.
     * @see #drawFlow(String, double, double, double, double, double, double, double, int, int[]) 
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double yThreshold, 
    		final Double yRestart, final Double boundWidth, final Double indent, final Integer fontType, final Double lineGap, 
    		final Integer alignment, final Integer[] colorRGB) {
    	final int[] color = (colorRGB == null) ? null :
    		new int[] { (colorRGB.length < 1 || colorRGB[0] == null) ? 0 : colorRGB[0], (colorRGB.length < 2 || colorRGB[1] == null) ? 0 : colorRGB[1], (colorRGB.length < 3 || colorRGB[2] == null) ? 0 : colorRGB[2] };
    	return drawFlow(data, 
    			(fontSize == null) ? 0D : fontSize, 
				(x == null) ? 0D : x, 
    			(y == null) ? 0D : y, 
				(yThreshold == null) ? 0D : yThreshold, 
        		(yRestart == null) ? 0D : yRestart, 
				(boundWidth == null) ? 0D : boundWidth, 
				(indent == null) ? 0D : indent, 
				(fontType == null) ? FONT_MING : fontType,
				(lineGap == null) ? PdfGenerator.DEFAULT_ROW_GAP : lineGap,
				(alignment == null) ? ALIGNMENT_LEFT : alignment,
				color);
    }
    
    /**
     * 在當前頁加一筆黑色文字.
     * @see #drawFlow(String, double, double, double, double, double, double, double, int, double, int, int[])
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double yThreshold, 
    		final double yRestart, final double boundWidth, final double indent, final int fontType) {
    	return drawFlow(data, fontSize, x, y, yThreshold, yRestart, boundWidth, indent, fontType, PdfGenerator.DEFAULT_ROW_GAP, ALIGNMENT_LEFT, null);
    }

    /**
     * 在當前頁列印一筆黑色文字.
     * @see #drawFlow(String, double, double, double, double, double, double, double, int, double, int, int[])
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double yThreshold, 
    		final Double yRestart, final Double boundWidth, final Double indent, final Integer fontType) {
    	return drawFlow(data, fontSize, x, y, yThreshold, yRestart, boundWidth, indent, fontType, null, null, null);
    }
    
    /**
     * 在當前頁加一筆黑色文字.
     * @see #drawFlow(String, double, double, double, double, double, double, double, int, double, int, int[])
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double yThreshold, 
    		final double yRestart, final double boundWidth, final double indent, final int fontType, final double lineGap) {
    	return drawFlow(data, fontSize, x, y, yThreshold, yRestart, boundWidth, indent, fontType, lineGap, ALIGNMENT_LEFT, null);
    }

    /**
     * 在當前頁列印一筆黑色文字.
     * @see #drawFlow(String, double, double, double, double, double, double, double, int, double, int, int[])
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double yThreshold, 
    		final Double yRestart, final Double boundWidth, final Double indent, final Integer fontType, final Double lineGap) {
    	return drawFlow(data, fontSize, x, y, yThreshold, yRestart, boundWidth, indent, fontType, lineGap, null, null);
    }
    
    /**
     * 在當前頁加一筆文字(第二行及以後要內縮), 自行指定位置, 直到字串結束為止, 過了頁底自動換頁, 從最頂端開始(x 坐標與內縮後起始位置相同).
     * @param data 文字資料
     * @param fontSize 字型大小 (單位: mm)
     * @param x 文字起點左上角 x 坐標 (單位: mm)
     * @param y 文字起點左上角 y 坐標 (單位: mm)
     * @param boundWidth 文字區寬 (單位: mm)
     * @param indent 第一次折行後內縮的寬度, 負數者成為外凸 (單位: mm)
     * @param fontType 字型, 可為 FONT_MING, FONT_KAI 之一
     * @param colorRGB 顏色碼(紅綠藍, 各自範圍 0-255), 或指定 COLOR_BLACK, COLOR_WHITE, COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_GOLD, COLOR_DARK_ORANGE, COLOR_MIDNIGHT_BLUE 之一
     * @return 印完最後一行時的文字最後字元下緣的坐標 (單位: mm)
     * 
     * @see #FONT_MING FONT_MING(明體)
     * @see #FONT_KAI FONT_KAI(楷體)
     * @see #COLOR_BLACK COLOR_BLACK(黑色)
     * @see #COLOR_WHITE COLOR_WHITE(白色)
     * @see #COLOR_RED COLOR_RED(紅色)
     * @see #COLOR_GREEN COLOR_GREEN(綠色)
     * @see #COLOR_BLUE COLOR_BLUE(藍色)
     * @see #COLOR_GOLD COLOR_GOLD(金色)
     * @see #COLOR_DARK_ORANGE COLOR_DARK_ORANGE(暗橘色)
     * @see #COLOR_MIDNIGHT_BLUE COLOR_MIDNIGHT_BLUE(暗夜藍色)
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double boundWidth, 
    		final double indent, final int fontType, final int[] colorRGB) {
    	return drawFlow(data, fontSize, x, y, this.pdf.getPageHeight() - this.pdf.getMarginBottom(), 
        		this.pdf.getMarginTop(), boundWidth, indent, fontType, PdfGenerator.DEFAULT_ROW_GAP, ALIGNMENT_LEFT, colorRGB);
    }
    
    /**
     * 在當前頁加一筆文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #drawFlow(String, double, double, double, double, double, int, int[])
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double boundWidth, 
    		final Double indent, final Integer fontType, final Integer[] colorRGB) {
    	return drawFlow(data, fontSize, x, y, (double)(this.pdf.getPageHeight() - this.pdf.getMarginBottom()), 
        		(double)this.pdf.getMarginTop(), boundWidth, indent, fontType, null, null, colorRGB);
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
     * @param lineGap 文字列間距(單位: mm)
     * @param colorRGB 顏色碼(紅綠藍, 各自範圍 0-255)
     * @return 印完最後一行時的文字最後字元下緣的坐標 (單位: mm)
     * 
     * @see #FONT_MING FONT_MING(明體)
     * @see #FONT_KAI FONT_KAI(楷體)
     * @see #COLOR_BLACK COLOR_BLACK(黑色)
     * @see #COLOR_WHITE COLOR_WHITE(白色)
     * @see #COLOR_RED COLOR_RED(紅色)
     * @see #COLOR_GREEN COLOR_GREEN(綠色)
     * @see #COLOR_BLUE COLOR_BLUE(藍色)
     * @see #COLOR_GOLD COLOR_GOLD(金色)
     * @see #COLOR_DARK_ORANGE COLOR_DARK_ORANGE(暗橘色)
     * @see #COLOR_MIDNIGHT_BLUE COLOR_MIDNIGHT_BLUE(暗夜藍色)
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double boundWidth, 
    		final double indent, final int fontType, final double lineGap, final int[] colorRGB) {
    	return drawFlow(data, fontSize, x, y, this.pdf.getPageHeight() - this.pdf.getMarginBottom(), 
        		this.pdf.getMarginTop(), boundWidth, indent, fontType, lineGap, ALIGNMENT_LEFT, colorRGB);
    }
    
    /**
     * 在當前頁加一筆文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #drawFlow(String, double, double, double, double, double, int, double, int[])
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double boundWidth, 
    		final Double indent, final Integer fontType, final Double lineGap, final Integer[] colorRGB) {
    	return drawFlow(data, fontSize, x, y, (double)(this.pdf.getPageHeight() - this.pdf.getMarginBottom()), 
        		(double)this.pdf.getMarginTop(), boundWidth, indent, fontType, lineGap, null, colorRGB);
    }
    
    /**
     * 在當前頁加一筆黑色文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #drawFlow(String, double, double, double, double, double, int, int[])
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double boundWidth, 
    		final double indent, final int fontType) {
    	return drawFlow(data, fontSize, x, y, this.pdf.getPageHeight() - this.pdf.getMarginBottom(), 
        		this.pdf.getMarginTop(), boundWidth, indent, fontType, PdfGenerator.DEFAULT_ROW_GAP, ALIGNMENT_LEFT, null);
    }
    
    /**
     * 在當前頁加一筆黑色文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #drawFlow(String, double, double, double, double, double, int, int[])
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double boundWidth, 
    		final Double indent, final Integer fontType) {
    	return drawFlow(data, fontSize, x, y, (double)(this.pdf.getPageHeight() - this.pdf.getMarginBottom()), 
        		(double)this.pdf.getMarginTop(), boundWidth, indent, fontType, null, (Integer)null, (Integer[])null);
    }
    
    /**
     * 在當前頁加一筆黑色文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #drawFlow(String, double, double, double, double, double, int, double, int[])
     */
    public double[] drawFlow(final String data, final double fontSize, final double x, final double y, final double boundWidth, 
    		final double indent, final int fontType, final double lineGap) {
    	return drawFlow(data, fontSize, x, y, this.pdf.getPageHeight() - this.pdf.getMarginBottom(), 
        		this.pdf.getMarginTop(), boundWidth, indent, fontType, lineGap, ALIGNMENT_LEFT, null);
    }
    
    /**
     * 在當前頁加一筆黑色文字, 過了頁底自動換頁, 從最頂端開始.
     * @see #drawFlow(String, double, double, double, double, double, int, double, int[])
     */
    public double[] drawFlow(final String data, final Double fontSize, final Double x, final Double y, final Double boundWidth, 
    		final Double indent, final Integer fontType, final Double lineGap) {
    	return drawFlow(data, fontSize, x, y, (double)(this.pdf.getPageHeight() - this.pdf.getMarginBottom()), 
        		(double)this.pdf.getMarginTop(), boundWidth, indent, fontType, lineGap, (Integer)null, (Integer[])null);
    }
    
    /**
     * 在當前頁畫條碼(39 碼).
     * @param barcodeData
     * @param x 左上角 x 坐標 (單位: mm).
     * @param y 左上角 y 坐標 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (mm).
     * @return Xprint 物件自身
     */
    public Xprint drawBarCode(final String barcodeData, final double x, final double y, final double width, final double height) {
    	ensurePdfSetup();
    	
    	try {
            this.pdf.drawBarcode39(barcodeData, (float)x, (float)y, (float)width, (float)height);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /**
     * 在當前頁畫條碼 (25 碼).
     * @param barcodeData
     * @param x 左上角 x 坐標 (單位: mm).
     * @param y 左上角 y 坐標 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint drawBarCode25(final String barcodeData, final double x, final double y, final double width, final double height) {
    	ensurePdfSetup();
    	
    	try {
            this.pdf.drawBarcode25(barcodeData, (float)x, (float)y, (float)width, (float)height);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /**
     * 在當前頁畫條碼 (128 碼).
     * @param barcodeData
     * @param x 左上角 x 坐標 (單位: mm).
     * @param y 左上角 y 坐標 (單位: mm).
     * @param width 條碼寬 (單位: mm).
     * @param height 條碼高度 (單位: mm).
     * @return Xprint 物件自身
     */
    public Xprint drawBarCode128(final String barcodeData, final double x, final double y, final double width, final double height) {
    	ensurePdfSetup();
    	
    	try {
            this.pdf.drawBarcode128(barcodeData, (float)x, (float)y, (float)width, (float)height);
    	} catch(Throwable t) {
    		handleException(t);
        }
    	return this;
    }
    
    /** 取輸出的 PDF 檔(並關閉已開啟的資源). */
    public File[] getOutputFiles() {
    	close();
    	return this.outputFiles.toArray(new File[this.outputFiles.size()]);
    }
    
    /** 取輸出文件總頁數. */
    public int getPageCount() {
        return this.pageCount;
    }
    
    

    public void beforePaging(final PagingHandler handler) {
    	this.beforePagingHandler = handler;
    }
    
    public void afterPaging(final PagingHandler handler) {
    	this.afterPagingHandler = handler;
    }
    
    private boolean ensurePdfSetup() {
    	try {
	    	if(this.pdf == null) {
	    		setupNewOutputPDF(); //設置輸出檔
	    		return true;
	    	}
	    	return false;
    	} catch(Throwable t) {
    		throw new XprintException(t.getMessage(), t);
    	}
    }
    
    //載入格式檔
    private void loadRptData(final String rptFormatFilename) throws Exception {
    	final File rpt = getRptFile(rptFormatFilename);
    	if(!rpt.isFile())
    		throw new IllegalStateException("'" + rptFormatFilename + "' resource can't be found");
    	if(this.debug)
        	log.debug("read rpt file: '" + rpt.getAbsolutePath() + "' using " + this.rptFormatFileCharset);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(rpt), this.rptFormatFileCharset))) {
        	loadRptData(in);
        }
    }
    
    //載入格式檔
    private void loadRptData(final BufferedReader in) throws Exception {
        this.lineInfo = new ArrayList<double[]>();
        this.labelInfo = new ArrayList<String[]>();
        this.blockInfo = new HashMap<String, double[]>();
        String line = null;

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
    }
    
    //換檔輸出, 第二檔及其後, 在副檔名 ".pdf" 之前依序插入 ".1", ".2", ...
    private File setupNewOutputPDF() throws Exception {
    	try {
    		//原 PDF 物件的一些設定
    		final float[] pgNumTopology = (this.pdf == null) ? null : this.pdf.getPgNumTopology(); //頁碼
    		final int pageNumberPosition = (this.pdf == null) ? PdfConst.PAGE_NUMBER_NONE : this.pdf.getPageNumberPosition();
    		final com.itextpdf.text.Image perforationSeal = (this.pdf == null) ? null : this.pdf.getPerforationSeal(); //騎縫章
    		final float[] perforationSealPosition = (this.pdf == null) ? null : this.pdf.getPerforationSealPosition();
    		final boolean printPerforationSealOnSingleSide = (this.pdf == null) ? false : this.pdf.isPrintPerforationSealOnSingleSide();
    		final boolean printPerforationSealOnTopDown = (this.pdf == null) ? false : this.pdf.isPrintPerforationSealOnTopDown();
    		final com.itextpdf.text.Image watermark = (this.pdf == null) ? null : this.pdf.getWatermarkImage(); //浮水印
    		
    		if(this.outputFiles.size() != 0) { //當前輸出檔關閉, 再開新檔 (this.outputFiles.size() 為 0 時, 已存在在 Xprint() 初始時所開的 outputFile 物件了)
    			close(); //關閉 this.pdf, this.out
    			this.outputFile = getFileForWrite(this.outputBaseDir, this.outputFilePrefix + "." + this.outputFiles.size() + this.outputFileSuffix);
    		}
			this.outputFiles.add(this.outputFile);
			this.out = new BufferedOutputStream(new FileOutputStream(this.outputFile));
			
			if(this.pageNumberHandler == null) {
				this.pdf = new PdfGenerator(this.out);
			} else { //自訂頁碼文字框
				final String pageNoFontName = (PdfConst.FONT_KAI == this.pageNumberHandler.getFontType() && this.fontNameKaiPlane0 != null) ? 
						this.fontNameKaiPlane0 : this.fontNamePlane0;
				this.pdf = new PdfGenerator(this.out, 
					new PdfGenerator.PageNoHandler(
							this.pageNumberHandler.getTextTemplate(), pageNoFontName, (float)this.pageNumberHandler.getFontSize(), 
							(float)this.pageNumberHandler.getX(), (float)this.pageNumberHandler.getLowerBondFromBottom()));
			}
			
			this.pdf.setDefaultFont(this.fontNamePlane0);
			this.pdf.setDefaultKaiFont(this.fontNameKaiPlane0);
			this.pdf.setPlane2Font(this.fontNamePlane2);
			this.pdf.setKaiPlane2Font(this.fontNameKaiPlane2);
			this.pdf.setPlane15Font(this.fontNamePlane15);
			this.pdf.setKaiPlane15Font(this.fontNameKaiPlane15);
			this.pdf.setTotalPages(this.pageCount); //設置總頁數(通知 PdfGenerator 物件, 目前實際總頁數為何)
			this.pdf.setPrintPageNumberOnFirstPage(this.printPageNumberOnFirstPage);
			this.pdf.setPgNumTopology(pgNumTopology);
			this.pdf.setPageNumberPosition(pageNumberPosition);
			this.pdf.setPerforationSeal(perforationSeal, perforationSealPosition);
			this.pdf.setPrintPerforationSealOnSingleSide(printPerforationSealOnSingleSide);
			this.pdf.setPrintPerforationSealOnTopDown(printPerforationSealOnTopDown);
			this.pdf.setWatermark(watermark, null);
			
			this.pageInFile = 0; //初始化當前 PDF 檔的頁數
			
			if(this.debug) {
				log.debug("write to '" + this.outputFile.getAbsolutePath() + "'");
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
    private void drawPreDefinedLine() throws Exception {
    	if(this.lineInfo == null)
    		return;
        for(double[] v : this.lineInfo) {
        	int colorBGR = (int)v[0];
        	int[] color = (colorBGR == 0) ? PdfConst.COLOR_RED : new int[] { colorBGR & 0xFF, (colorBGR >> 8) & 0xFF, (colorBGR >> 16) & 0xFF }; //RGB  TODO:原 BCB 版前端預覽軟體設框線為紅色
            this.pdf.drawLine((float)v[3] + this.originOffsetX, (float)v[4] + this.originOffsetY, 
            		(float)v[5] + this.originOffsetX, (float)v[6] + this.originOffsetY, 
            		tuneLineWidth((float)v[2]), decidePdfLineStyle((int)v[1]), 
            		color);
        }
    }
    
    //畫格式檔已預定的 label 文字
    private void drawPreDefinedLabel() throws Exception {
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
            	this.debug ? PdfConst.COLOR_BLUE : PdfConst.COLOR_RED; //TODO: 原 BCB 版前端預覽軟體預設 label 文字為紅字
            this.pdf.drawTextBox(text, fontSize, x, y, boundWidth, boundHeight, rowGap, charSpacing, textDirection, distribute, fontType, PdfConst.FONT_STYLE_NORMAL, color);
        }
    }
    
    //決定字型(明/楷體)
    //font: Xprint 格式檔裡的字型號碼
    private int decideFontType(int font) {
    	switch(font) {
    		case 3:
    		case 1: return PdfConst.FONT_KAI;
    		case 2:
    		case 0:
    		default: return PdfConst.FONT_MING;
    	}
    }
    
    private int decideTextDirectionStyle(int n) {
    	switch(n) {
    		case 1: return PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY;
    		case 2: return PdfConst.TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY;
    		case 3: return PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY;
    		default: return PdfConst.TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY;
    	}
    }
    
    //調整字型大小(應憑實際操作表格製作軟體得來的經驗值來修改)
    private float tuneFontSize(final float fontSize) {
        return fontSize * 0.907F;
    }
    
    //調整線寬
    private float tuneLineWidth(final double lineWidth) {
        return (float)lineWidth / 2;
    }

    //Xprint 的線條型式代碼轉為 PdfGenerator 的線條型式代碼
    private int decidePdfLineStyle(final int xprintLineStyle) {
    	switch(xprintLineStyle) {
	        case 1: return PdfConst.LINE_DASH;
	        case 2: return PdfConst.LINE_DOT;
	        case 3: return PdfConst.LINE_DASH_DOT;
	        case 4: return PdfConst.LINE_DASH_DOT_DOT;
	        default: return PdfConst.LINE_SOLID;
        }
    }
    
    private File getFileForWrite(final File dir, final String fileName) {
    	File f = new File(fileName);
    	if(!f.isAbsolute())
    		f = new File(dir, fileName);
    	
    	final File p = f.getParentFile();
    	if(p != null && !p.isDirectory())
    		p.mkdirs();
    	
    	return f;
    }
    
    private File getRptFile(final String rptFormatFilename) {
    	File rpt = new File(rptFormatFilename);
    	if(rpt.isFile())
    		return rpt;
    	if(this.rptFormatFileDir != null)
    		rpt = new File(this.rptFormatFileDir, rptFormatFilename);
    	else
    		rpt = new File(Xprint.class.getResource(rptFormatFilename).getPath());
    	return rpt;
    }
    
    //imgFilePath: 圖檔路徑(檔名或絕對路徑)
    private File getImageFile(final String imgFilePath) {
    	if(imgFilePath == null || imgFilePath.length() == 0)
    		throw new IllegalArgumentException("image file path not specified");
    	File img = new File(imgFilePath);
    	if(img.isFile())
    		return img;
    	
    	if(this.imageFileDir != null)
    		img = new File(this.imageFileDir, imgFilePath);
    	else
    		img = new File(Xprint.class.getResource(imgFilePath).getPath());
    	return img;
    }
    
    private boolean checkUseTemplate() {
    	if(this.blockInfo == null) {
    		log.error("rpt template file not given");
    		return false;
    	}
    	return true;
    }
    
    private void handleException(final Throwable t) {
    	if(!this.coverException) {
    		if(t instanceof XprintException)
    			throw (XprintException)t;
			throw new XprintException(t.getMessage(), t);
    	} else {
    		log.error(t.getMessage(), t);
    	}
    }

    private static class PageNumberHandler {
		private String textTemplate;
		private double x; //文字框左側距紙張左邊界的距離, 單位: mm
    	private double lowerBondFromBottom; //文字框距紙張底部的距離, 單位: mm
    	private int fontType; //FONT_MING 或 FONT_KAI 之一
    	private double fontSize; //mm
    	
		public PageNumberHandler(final String textTemplate, final double x, final double lowerBondFromBottom, final int fontType, final double fontSize) {
			this.textTemplate = textTemplate;
			this.x = x;
    		this.lowerBondFromBottom = lowerBondFromBottom;
    		this.fontType = fontType;
    		this.fontSize = fontSize;
		}
		
		public String getTextTemplate() {
			return textTemplate;
		}

		public void setTextTemplate(String textTemplate) {
			this.textTemplate = textTemplate;
		}

		public double getX() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public double getLowerBondFromBottom() {
			return this.lowerBondFromBottom;
		}

		public void setLowerBondFromBottom(double lowerBondFromBottom) {
			this.lowerBondFromBottom = lowerBondFromBottom;
		}

		public int getFontType() {
			return fontType;
		}

		public void setFontType(int fontType) {
			this.fontType = fontType;
		}

		public double getFontSize() {
			return fontSize;
		}

		public void setFontSize(double fontSize) {
			this.fontSize = fontSize;
		}

		@Override
		public String toString() {
			return "PageNumberHandler [textTemplate=" + textTemplate + ", x=" + x + ", lowerBondFromBottom=" + lowerBondFromBottom + ", fontType=" +
					fontType + ", fontSize=" + fontSize + "]";
		}
	}
    
    public static interface PagingHandler {
    	void execute();
    }

	@Override
	public String toString() {
		return "Xprint [pdf=" + pdf + ", outputBaseDir=" + outputBaseDir + ", rptFormatFileDir=" + rptFormatFileDir + ", imageFileDir=" +
				imageFileDir + ", fontNamePlane0=" + fontNamePlane0 + ", fontNameKaiPlane0=" + fontNameKaiPlane0 + ", fontNamePlane2=" +
				fontNamePlane2 + ", fontNameKaiPlane2=" + fontNameKaiPlane2 + ", fontNamePlane15=" + fontNamePlane15 + ", fontNameKaiPlane15=" +
				fontNameKaiPlane15 + ", pageStyle=" + pageStyle + ", userDefinedWidth=" + userDefinedWidth + ", userDefinedHeight=" +
				userDefinedHeight + ", userDefinedMarginLeft=" + userDefinedMarginLeft + ", userDefinedMarginRight=" + userDefinedMarginRight +
				", userDefinedMarginTop=" + userDefinedMarginTop + ", userDefinedMarginBottom=" + userDefinedMarginBottom +
				", rptFormatFileCharset=" + rptFormatFileCharset + ", outputFile=" + outputFile + ", outputFiles=" + outputFiles +
				", originOffsetX=" + originOffsetX + ", originOffsetY=" + originOffsetY + ", lineInfo=" + lineInfo + ", labelInfo=" + labelInfo +
				", blockInfo=" + blockInfo + ", pageCount=" + pageCount + ", pageInFile=" + pageInFile + ", maxPages=" + maxPages + ", out=" + out +
				", outputFilePrefix=" + outputFilePrefix + ", outputFileSuffix=" + outputFileSuffix + ", currentImgPath=" + currentImgPath +
				", beforePagingHandler=" + beforePagingHandler + ", afterPagingHandler=" + afterPagingHandler + ", pageNumberHandler=" +
				pageNumberHandler + ", printPageNumberOnFirstPage=" + printPageNumberOnFirstPage + ", debug=" + debug + ", coverException=" +
				coverException + "]";
	}
}
