package com.test.commons.pdf2;

import java.io.File;

/**
 * 協助產生 Xprint 物件.<br>
 * 本工具可登錄於 Spring context 中, 並設置屬性值(如 defaultFontName 等).
 * 部分屬性(如 defaultOutputBaseDir 等)可於 web-app 啟動時順便設置
 */
public class XprintBuilderBean {
	public static final String DEFAULT_FORMAT_FILE_ENCODING = "UTF-8";
	
	private String defaultFontName; //預設(BMP)字型檔路徑
	private String defaultKaiFontName; //預設(BMP)楷體字型檔路徑
	private String plane2FontName; //第 2 字面字型檔路徑
	private String plane2KaiFontName; //第 2 字面楷體字型檔路徑
	private String plane15FontName; //第 15 字面字型檔路徑
	private String plane15KaiFontName; //第 15 字面楷體字型檔路徑
	private String defaultFormatFileEncoding; //Xprint 格式檔預設編碼. 未指定者, 使用 UTF-8 碼
	private String defaultFormatFileBaseDir; //Xprint 格式檔所在實體目錄. 未指定者, 視格式檔檔名為絕對路徑, 或以 Class.getResource(name) 的方式取格式檔
	private String defaultImageFileBaseDir; //欲嵌入報表的圖檔所在的實體目錄. 未指定者, 視圖型檔檔名為絕對路徑, 或以 Class.getResource(name) 的方式取圖檔
	private String defaultOutputBaseDir; //報表檔輸出並存檔的基準實體目錄. 未指定者, 使用 OS 暫存目錄
	private Boolean printPageNumberOnFirstPage; //如果要印頁碼時, 首頁是否需列印頁碼?
	
	public String getDefaultFontName() {
		return defaultFontName;
	}

	/** 預設(BMP)字型檔路徑 */
	public void setDefaultFontName(String defaultFontName) {
		ensurePropertySetOnce("defaultFontName", this.defaultFontName);
		this.defaultFontName = defaultFontName;
	}

	public String getDefaultKaiFontName() {
		return defaultKaiFontName;
	}

	/** 預設(BMP)楷體字型檔路徑 */
	public void setDefaultKaiFontName(String defaultKaiFontName) {
		ensurePropertySetOnce("defaultKaiFontName", this.defaultKaiFontName);
		this.defaultKaiFontName = defaultKaiFontName;
	}

	public String getPlane2FontName() {
		return plane2FontName;
	}

	/** 第 2 字面字型檔路徑 */
	public void setPlane2FontName(String plane2FontName) {
		ensurePropertySetOnce("plane2FontName", this.plane2FontName);
		this.plane2FontName = plane2FontName;
	}

	public String getPlane2KaiFontName() {
		return plane2KaiFontName;
	}

	public void setPlane2KaiFontName(String plane2KaiFontName) {
		ensurePropertySetOnce("plane2KaiFontName", this.plane2KaiFontName);
		this.plane2KaiFontName = plane2KaiFontName;
	}

	public String getPlane15FontName() {
		return plane15FontName;
	}

	/** 第 15 字面字型檔路徑 */
	public void setPlane15FontName(String plane15FontName) {
		ensurePropertySetOnce("plane15FontName", this.plane15FontName);
		this.plane15FontName = plane15FontName;
	}

	public String getPlane15KaiFontName() {
		return plane15KaiFontName;
	}

	/** 第 15 字面楷體字型檔路徑 */
	public void setPlane15KaiFontName(String plane15KaiFontName) {
		ensurePropertySetOnce("plane15KaiFontName", this.plane15KaiFontName);
		this.plane15KaiFontName = plane15KaiFontName;
	}

	public String getDefaultFormatFileEncoding() {
		return defaultFormatFileEncoding;
	}

	/** Xprint 格式檔預設編碼. 未指定者, 使用 UTF-8 碼 */
	public void setDefaultFormatFileEncoding(String defaultFormatFileEncoding) {
		ensurePropertySetOnce("defaultFormatFileEncoding", this.defaultFormatFileEncoding);
		this.defaultFormatFileEncoding = defaultFormatFileEncoding;
	}

	public String getDefaultFormatFileBaseDir() {
		return defaultFormatFileBaseDir;
	}

	/** Xprint 格式檔所在實體目錄. 未指定者, 視格式檔檔名為絕對路徑, 或以 Class.getResource(name) 的方式取格式檔 */
	public void setDefaultFormatFileBaseDir(String defaultFormatFileBaseDir) {
		ensurePropertySetOnce("defaultFormatFileBaseDir", this.defaultFormatFileBaseDir);
		this.defaultFormatFileBaseDir = defaultFormatFileBaseDir;
	}

	public String getDefaultImageFileBaseDir() {
		return defaultImageFileBaseDir;
	}

	/** 欲嵌入報表的圖檔所在的實體目錄. 未指定者, 視圖型檔檔名為絕對路徑, 或以 Class.getResource(name) 的方式取圖檔 */
	public void setDefaultImageFileBaseDir(String defaultImageFileBaseDir) {
		ensurePropertySetOnce("defaultImageFileBaseDir", this.defaultImageFileBaseDir);
		this.defaultImageFileBaseDir = defaultImageFileBaseDir;
	}

	public String getDefaultOutputBaseDir() {
		return defaultOutputBaseDir;
	}

	/** 報表檔輸出並存檔的基準實體目錄. 未指定者, 使用 OS 暫存目錄 */
	public void setDefaultOutputBaseDir(String defaultOutputBaseDir) {
		ensurePropertySetOnce("defaultOutputBaseDir", this.defaultOutputBaseDir);
		this.defaultOutputBaseDir = defaultOutputBaseDir;
	}
	
	public Boolean getPrintPageNumberOnFirstPage() {
		return printPageNumberOnFirstPage;
	}

	/** 首頁是否需列印頁碼(預設: 否) */
	public void setPrintPageNumberOnFirstPage(Boolean printPageNumberOnFirstPage) {
		ensurePropertySetOnce("printPageNumberOnFirstPage", this.printPageNumberOnFirstPage);
		this.printPageNumberOnFirstPage = printPageNumberOnFirstPage;
	}

	/**
	 * 使用大同印表格式檔 讓資料套用, 輸出 PDF 檔.
	 * @param rptFormatFilename 印表格式檔(optional). 
     * 		  null 值, 則使用預設紙張(A4)、預設邊界長, 且只能使用不依賴格式檔的 Xprint.drawFlow(), Xprint.drawLine(), 其他如 add() 等加字串及條碼等功能一概無用
	 * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
	 * @param rptFormatFileCharset 印表格式檔之字元編碼(預設 UTF-8)
	 * @return Xprint 物件
	 */
	public Xprint build(final String rptFormatFilename, final String outputFilename, final String rptFormatFileCharset) {
		final File defaultOutputBaseDir = (this.defaultOutputBaseDir == null) ? (File)null : new File(this.defaultOutputBaseDir);
		final File defaultFormatFileBaseDir = (this.defaultFormatFileBaseDir == null) ? (File)null : new File(this.defaultFormatFileBaseDir);
		final File defaultImageFileBaseDir = (this.defaultImageFileBaseDir == null) ? (File)null : new File(this.defaultImageFileBaseDir);
		final Xprint xp = new Xprint(defaultOutputBaseDir, defaultFormatFileBaseDir, defaultImageFileBaseDir, 
				outputFilename, rptFormatFilename, rptFormatFileCharset);
		applyProperties(xp);
		return xp;
	}
	
	/**
	 * 使用大同印表格式檔(UTF-8 編碼) 讓資料套用, 輸出 PDF 檔.
	 * @param rptFormatFilename 印表格式檔(optional). 
     * 		  null 值, 則使用預設紙張(A4)、預設邊界長, 且只能使用不依賴格式檔的 Xprint.drawFlow(), Xprint.drawLine(), 其他如 add() 等加字串及條碼等功能一概無用
	 * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
	 * @return Xprint 物件
	 */
	public Xprint build(final String rptFormatFilename, final String outputFilename) {
		return build(rptFormatFilename, outputFilename, getDefaultFormatFileEncoding());
	}
	
	/**
     * 不使用格式檔, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
     * 使用預設紙張(A4)及預設四邊界長度.
     * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
     */
	public Xprint build(final String outputFilename) {
		return build((String)null, outputFilename, getDefaultFormatFileEncoding());
	}
	
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
     * @see Xprint#PAGE_A3
     * @see Xprint#PAGE_A4
     * @see Xprint#PAGE_B3
     * @see Xprint#PAGE_B4
     * @see Xprint#PAGE_A3_LANDSCAPE
     * @see Xprint#PAGE_A4_LANDSCAPE
     * @see Xprint#PAGE_B3_LANDSCAPE
     * @see Xprint#PAGE_B4_LANDSCAPE
     */
	public Xprint build(final String outputFilename, final int pageSize, final double marginLeft, 
    		final double marginRight, final double marginTop, final double marginBottom) {
		final Xprint xp = new Xprint(outputFilename, pageSize, marginLeft, marginRight, marginTop, marginBottom);
		applyProperties(xp);
		return xp;
	}
	
	/**
     * 不使用格式檔, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
     * 使用預設四邊界長度.
     * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
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
	public Xprint build(final String outputFilename, final int pageSize) {
		return build(outputFilename, pageSize, -1D, -1D, -1D, -1D);
	}
	
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
	public Xprint build(final String outputFilename, final double pageWidth, final double pageHeight, 
    		final double marginLeft, final double marginRight, final double marginTop, 
    		final double marginBottom) {
		final Xprint xp = new Xprint(outputFilename, pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom);
		applyProperties(xp);
		return xp;
	}
	
	/**
     * 不使用格式檔, 自訂紙張大小, 預設四邊界長, 完全以 drawFlow(), drawLine() 等 method 自行購成 PDF 內容.
     * @param outputFilename 輸出 PDF 檔名. <b>注意: 實際輸出檔應由 getOutputFiles() 取得. 一旦已存在同名檔案者, 主檔名將附加數字以區別</b>
     * @param pageWidth 頁寬(單位: mm)
     * @param pageHeight 頁高(單位: mm)
     * @see PdfConst#DEFAULT_MARGIN_LEFT
     * @see PdfConst#DEFAULT_MARGIN_RIGHT
     * @see PdfConst#DEFAULT_MARGIN_TOP
     * @see PdfConst#DEFAULT_MARGIN_BOTTOM
     */
	public Xprint build(final String outputFilename, final double pageWidth, final double pageHeight) {
		return build(outputFilename, pageWidth, pageHeight, -1D, -1D, -1D, -1D);
	}
	
	private void ensurePropertySetOnce(final String propertyName, final Object propertyValue) {
		if(propertyValue != null)
			throw new IllegalStateException("property '" + propertyName + "' already set and can be set only once");
	}
	
	private void applyProperties(final Xprint xp) {
		xp.setDefaultFont(this.defaultFontName);
		xp.setDefaultKaiFont(this.defaultKaiFontName);
		xp.setPlane2Font(this.plane2FontName);
		xp.setPlane2KaiFont(this.plane2KaiFontName);
		xp.setPlane15Font(this.plane15FontName);
		xp.setPlane15KaiFont(this.plane15KaiFontName);
		xp.setPrintPageNumberOnFirstPage(this.printPageNumberOnFirstPage != null && this.printPageNumberOnFirstPage);
	}

	@Override
	public String toString() {
		return "XprintBuilderBean [defaultFontName=" + defaultFontName + ", defaultKaiFontName=" + defaultKaiFontName + ", plane2FontName=" +
				plane2FontName + ", plane2KaiFontName=" + plane2KaiFontName + ", plane15FontName=" + plane15FontName + ", plane15KaiFontName=" +
				plane15KaiFontName + ", defaultFormatFileEncoding=" + defaultFormatFileEncoding + ", defaultFormatFileBaseDir=" +
				defaultFormatFileBaseDir + ", defaultImageFileBaseDir=" + defaultImageFileBaseDir + ", defaultOutputBaseDir=" + defaultOutputBaseDir +
				", printPageNumberOnFirstPage=" + printPageNumberOnFirstPage + "]";
	}
}
