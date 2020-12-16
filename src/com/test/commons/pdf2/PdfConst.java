package com.test.commons.pdf2;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

/**
 * PDF 相關常數.
 * <p>
 * depend on: iText
 */
final public class PdfConst {
	public static String PDF_CREATER_NAME = "PdfGenerator";
	public static String PDF_AUTHOR_NAME = "Tatung co.";
	
	/** 每 mm 所含的 pixel */
    public static float PX_PER_MM = (float)(595.0 / 210.0);
    
    /** 用來代換超出支援範圍的字元 */
    public static String UNKNOWN_CHAR = "□";
    
	/** 紙張長寬全由使用者決定 */
    public static int PAGE_USERDEFINED = -1;
    /** A3 直印 */
    public static int PAGE_A3 = 0;
    /** A3 橫印 */
    public static int PAGE_A3_LANDSCAPE = 1;
    /** A4 直印 */
    public static int PAGE_A4 = 2;
    /** A4 橫印 */
    public static int PAGE_A4_LANDSCAPE = 3;
    /** B3 直印 */
    public static int PAGE_B3 = 4;
    /** B3 橫印 */
    public static int PAGE_B3_LANDSCAPE = 5;
    /** B4 直印 */
    public static int PAGE_B4 = 6;
    /** B4 橫印 */
    public static int PAGE_B4_LANDSCAPE = 7;
    /** 預設紙張大小 */
    public static int DEFAULT_PAGE_SIZE = PAGE_A4;
    /** 實線 */
    public static int LINE_SOLID = 10;
    /** 虛線 */
    public static int LINE_DASH = 11;
    /** 點虛線 */
    public static int LINE_DOT = 12;
    /** 短線、點交錯虛線 */
    public static int LINE_DASH_DOT = 13;
    /** 短線、連續二點交錯虛線 */
    public static int LINE_DASH_DOT_DOT = 14;
    
    /** 黑色 */
    public static int[] COLOR_BLACK = { 0x00, 0x00, 0x00 };
    /** 白色 */
    public static int[] COLOR_WHITE = { 0xDD, 0xDD, 0xDD };
    /** 紅色 */
    public static int[] COLOR_RED = { 0xDD, 0x00, 0x00 };
    /** 緣色 */
    public static int[] COLOR_GREEN = { 0x00, 0xDD, 0x00 };
    /** 藍色 */
    public static int[] COLOR_BLUE = { 0x00, 0x00, 0xDD };
    /** 黃金色 */
    public static int[] COLOR_GOLD = { 0xD4, 0xA0, 0x17 };
    /** 暗橘色 */
    public static int[] COLOR_DARK_ORANGE = { 0xF8, 0x80, 0x17 };
    /** Midnight Blue 色 */
    public static int[] COLOR_MIDNIGHT_BLUE = { 0x15, 0x1B, 0x54 };

    /** 預設上邊界(mm) */
    public static float DEFAULT_MARGIN_TOP = 20F;
    /** 預設下邊界(mm) */
    public static float DEFAULT_MARGIN_BOTTOM = 15F;
    /** 預設左邊界(mm) */
    public static float DEFAULT_MARGIN_LEFT = 15F;
    /** 預設右邊界(mm) */
    public static float DEFAULT_MARGIN_RIGHT = 15F;
    
    public static final int XPRINT_DISTRIBUTED_DEFAULT = 0;
    /** 只對水平排列文字有作用, (1)文字列未折行時: 水平均佈, 垂直居中; (2)文字列有折行時: 水平緊靠, 行間垂直均佈, 首尾行觸框 */
    public static final int XPRINT_DISTRIBUTED_1 = 1;
    /** 只對垂直排列文字有作用, (1)文字列未折行時: 文字水平居中, 垂直均佈; (2)字元垂直緊靠, 行間水平均佈, 首尾行之外與框之間有間隙 */
    public static final int XPRINT_DISTRIBUTED_2 = 2;
    /** 不均佈, 只對水平排列文字有作用, (1)文字列未折行時: 文字水平垂直皆居中; (2)文字列有折行時: 文字垂直居中 */
    public static final int XPRINT_DISTRIBUTED_3 = 3;
    /** 不均佈, 只對垂直排列文字有作用, (1)文字列未折行時: 文字水平垂直皆居中; (2)文字列有折行時: 文字水平居中 */
    public static final int XPRINT_DISTRIBUTED_4 = 4;
    /** 水平靠右, 垂直置中 */
    public static final int XPRINT_DISTRIBUTED_5 = 5;
    /** 垂直分佈, 邊界對齊(橫書); 水平分佈, 邊界對齊(直書) */
    public static final int XPRINT_DISTRIBUTED_6 = 6;
    /** 水平靠左, 垂直置中 */
    public static final int XPRINT_DISTRIBUTED_7 = 7;
    
    /** 文字自左至右而下排列 */
    public static int TEXT_DIRECTION_LEFT_TO_RIGHT_HORIZONTALLY = 1;
    /** 文字自右至左而下排列 */
    public static int TEXT_DIRECTION_RIGHT_TO_LEFT_HORIZONTALLY = 2;
    /** 文字自上至下而左排列 */
    public static int TEXT_DIRECTION_RIGHT_TO_LEFT_VERTICALLY = 3;
    /** 文字自上至下而右排列 */
    public static int TEXT_DIRECTION_LEFT_TO_RIGHT_VERTICALLY = 4;

    /** 字體: 明體 */
    public static int FONT_MING = 1;
    /** 字體: 楷體 */
    public static int FONT_KAI = 2;

    /** 字形: 粗體 */
    public static int FONT_STYLE_BOLD = Font.BOLD;
    /** 字形: 粗斜體 */
    public static int FONT_STYLE_BOLDITALIC = Font.BOLDITALIC;
    /** 字形: 斜體 */
    public static int FONT_STYLE_ITALIC = Font.ITALIC;
    /** 字形: 一般 */
    public static int FONT_STYLE_NORMAL = Font.NORMAL;
    /** 字形: 加刪除線 */
    public static int FONT_STYLE_STRIKETHRU = Font.STRIKETHRU;
    /** 字形: 加底線 */
    public static int FONT_STYLE_UNDERLINE = Font.UNDERLINE;
    
    /** 頁碼: 不列印頁碼 */
    public static int PAGE_NUMBER_NONE = 0;
    /** 頁碼位置: 上方中央  */
    public static int PAGE_NUMBER_POSITION_TOP_CENTER = 1;
    /** 頁碼位置: 下方中央  */
    public static int PAGE_NUMBER_POSITION_BOTTOM_CENTER = 2;
    /** 頁碼位置: 上方靠右  */
    public static int PAGE_NUMBER_POSITION_TOP_RIGHT = 3;
    /** 頁碼位置: 上方靠左  */
    public static int PAGE_NUMBER_POSITION_TOP_LEFT = 4;
    /** 頁碼位置: 下方靠右  */
    public static int PAGE_NUMBER_POSITION_BOTTOM_RIGHT = 5;
    /** 頁碼位置: 下方靠左  */
    public static int PAGE_NUMBER_POSITION_BOTTOM_LEFT = 6;
    /** 頁碼位置: 上方兩旁(雙面印列, 左翻)  */
    public static int PAGE_NUMBER_POSITION_TOP_BOTH_SIDE = 7;
    /** 頁碼位置: 下方兩旁(雙面印列, 左翻)  */
    public static int PAGE_NUMBER_POSITION_BOTTOM_BOTH_SIDE = 8;
    
    /** 內容靠左 */
    public static int ALIGNMENT_LEFT = Element.ALIGN_LEFT;
    /** 內容水平置中 */
    public static int ALIGNMENT_CENTER = Element.ALIGN_CENTER;
    /** 內容水平靠右 */
    public static int ALIGNMENT_RIGHT = Element.ALIGN_RIGHT;
    
    /** 將以 mm 為單位的值換算為單位 pixel */
    public static float pt(float mm) {
        return mm * PX_PER_MM;
    }

    /** 將以 pt 為單位的值換算為單位 mm */
    public static float mm(float pt) {
        return pt / PX_PER_MM;
    }
}
