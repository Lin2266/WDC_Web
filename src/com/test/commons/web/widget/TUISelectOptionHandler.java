package com.test.commons.web.widget;

/**
 * 協助處理 HTML select 內之 option 項目的 callback 物件.
 * <p>
 * depend on: jquery
 */
public abstract class TUISelectOptionHandler<T> {
	private boolean selectedOverrided = true;
	
	/** 
     * 自 vo 裡取用來作為 option 標籤之 value 的屬性值
     * @param index 本筆 vo 在資料來源 list 中的 index(自 0 起算)
     * @param vo 放置本筆資料的物件
     */
    abstract public String value(int index, T vo);
    
    /** 
     * 自 vo 裡取用來作為 option 標籤之 label 的屬性值
     * @param index 本筆 vo 在資料來源 list 中的 index(自 0 起算)
     * @param vo 放置本筆資料的物件 
     */
    abstract public String label(int index, T vo);
    
    /** 
     * 根據 vo 某屬性值而判斷是否本項 option 為 selected(已選取)(預設無此屬性)
     * @param index 本筆 vo 在資料來源 list 中的 index(自 0 起算)
     * @param vo 放置本筆資料的物件
     * @return true 代表本筆的 option 標籤要加上 "selected" 屬性, false 則否 
     */
    public boolean selected(int index, T vo) {
    	if(this.selectedOverrided)
    		this.selectedOverrided = false;
        return false;
    }
    
    /**
     * 決定是否印出本筆 vo 所對應的 option 標籤(預設無條件通過). 可覆寫本 method 以作篩選的動作.
     * @param index 本筆 vo 在資料來源 list 中的 index(自 0 起算)
     * @param vo 放置本筆資料的物件
     * @return true 代表印出 option 標籤, false 則略過本筆 vo
     */
    public boolean filter(int index, T vo) {
        return true;
    }
    
    /**
     * 用於釋放本 callback 物件所內含的暫存資源, 由實作者決定對象.
     */
    public void release() {
    	return;
    }
    
    boolean isSelectedOverrided() {
    	return this.selectedOverrided;
    }
}
