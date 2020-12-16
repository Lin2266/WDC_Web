package com.test.commons.acl;

import java.io.Serializable;
import java.util.Locale;

public interface IAccount extends Serializable {
    
    /** 本物件存在於 HTTP session 中的 key */
    static String KEY_IN_HTTP_SESSION = "_user_account_";
    
    /** 取帳號 */
    String getUserId();
    
    /** 取得被明確指定的 language, region 等資訊 */
    Locale getLocale();
}
