package com.test.commons.web.widget;

/**
 * <i>(內部用)</i><br>
 * 用來協助產生 Grid 資料欄內容的 callback.
 * <p>
 * depend on: jquery.jqGrid
 * 
 * @param <T> 資料來源之成員. 若資料來源型態為 JSONArray 者, 則成員型態限定為 JSONObject
 */
public abstract class TUIGridColumnsHandler<T> {
	private boolean filterSwitch;
	
    /**
     * callback function.
     * @param rowIndex  當前該筆 bean 在容器 &lt;T&gt; 中的 index(自 0 起算)
     * @param bean  裝載一筆資料的 Java bean. 若資料來源型態為 JSONArray 者, 則此參數型態限定為 JSONObject
     * @param rowId  Grid 元件在每一次顯示資料表時, 對每一筆顯示在 grid 內的資料所賦予的 id (與資料庫無關)
     * @return 一筆資料中各欄位的值
     * @throws Throwable
     */
    public abstract Object[] generateColumns(int rowIndex, T bean, String rowId) throws Throwable;
    
    /**
     * 決定是否印出本筆 grid 成員(預設無條件通過, <span style="color:red">只可用於 server 端不分頁的情形</span>). 可覆寫本 method 以作篩選的動作.
     * @param index 當前該筆 bean 在容器 &lt;T&gt; 中的 index(自 0 起算)
     * @param bean 放置本筆資料的 JavaBean. 若資料來源型態為 JSONArray 者, 則此參數型態限定為 JSONObject
     * @return true 代表印出 option 標籤, false 則略過本筆 bean 不印出
     */
    public boolean filter(int index, T bean) {
    	if(!this.filterSwitch) //如果是不允許 filter grid 成員的狀況下, 本物件在執行過被 override 的本函數後, 再呼叫 allowFiltering() 時將得到 false
    		this.filterSwitch = true;
        return true;
    }
    
    /**
     * 用於釋放本 callback 物件所內含的暫存資源, 由實作者決定對象.
     */
    public void release() {
    	return;
    }
    
    //執行過 filter() 後再以本 method 檢查
	final boolean allowFiltering() {
		return this.filterSwitch;
	}
}
