package com.test.commons.web.widget;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.test.commons.util.JSONArray;
import com.test.commons.util.JSONObject;

/**
 * 產生 jQuery jqGrid 所需的資料(先填入全部原始資料, 再指定如何產生每筆 grid 列之欄位, 並轉出 jqGrid 格式資料). <p>
 * 其產生的 JSON 格式, 型式如: <div style='border:1px solid black'><pre>
 * {
 *     "total": "xxx",   //查詢所得的總頁數
 *     "page": "yyy",    //當前查詢所得的頁碼
 *     "records": "zzz", //總資料筆數
 *     "rows": [         //每一筆資料
 *         { "id":"1", "cell":["cell11", "cell12", "cell13", ...] },  //id: 每一筆的唯一的內部識別代碼(本元件產生的 jqGrid 之每筆的 rowid, 即每筆 index(自 0 起算) + 1
 *         { "id":"2", "cell":["cell21", "cell22", "cell23", ...] },  //cell: 每一筆的每個欄位的值
 *         ...
 *     ]
 * }
 * </pre></div>
 * (全部的值用 Javascript string 表示)
 * <p>
 * depend on: jquery.jqGrid
 */
public class TUIGridView<T> {
	private final Collection<T> data; //for 先[填入全部資料]再[指定如何處理column] 模式
	private final JSONArray jdata; //for 先[填入全部資料]再[指定如何處理column] 模式
	private final int rowsPerPage; //每頁最多幾筆, 0 代表不作 server 端分頁
	private final int currentPageNo; //當前頁碼(自 1 起算)
	private final Integer totalRecords; //資料總筆數
	
	private int[] newColumnOrder; //欲重排順序的欄位 index. 未指定到的欄位, 排在其他欄位之前
	private int[] newAllColumnOrder; //重排後之全部欄位 index.
	private Object[] reorderedRecordCache; //暫存重排後的一筆 grid 記錄
    
	/**
	 * server 端分頁模式用, 預先備妥欲轉為 jqGrid 指定頁碼所需格式的原始資料. <br>
	 * note: 將不允許 override TUIGridColumnsHandler&lt;T&gt;.filter(int, T) method.
	 * @param partialData grid 當前頁的資料
	 * @param rowsPerPage 每頁最大筆數, 須與網頁端 grid 的每頁筆數設定一致(若為 0 者, 視為 client 端分頁, 其他參數將被忽略)
	 * @param currentPageNo 當前頁碼(自 1 起算)
	 * @param totalRecords 資料總筆數
	 */
	public TUIGridView(Collection<T> partialData, Integer rowsPerPage, Integer currentPageNo, Integer totalRecords) {
		this.data = partialData;
		this.jdata = null;
    	this.rowsPerPage = (rowsPerPage == null || rowsPerPage < 0) ? 0 : rowsPerPage;
    	this.currentPageNo = (this.rowsPerPage == 0 || currentPageNo == null || currentPageNo < 1) ? 1 : currentPageNo;
    	this.totalRecords = totalRecords;
    }
	
	/**
	 * server 端分頁模式用, 預先備妥欲轉為 jqGrid 所需格式的全部原始資料, 但只截取指定頁面所需的部分供 client 端使用. <br>
	 * note: 將不允許 override TUIGridColumnsHandler&lt;T&gt;.filter(int, T) method.
	 * @param allData grid 全部資料(不侷限當前頁的)
	 * @param rowsPerPage 每頁最大筆數, 須與網頁端 grid 的每頁筆數設定一致(若為 0 者, 視為 client 端分頁, 其他參數將被忽略)
	 * @param currentPageNo 當前頁碼(自 1 起算)
	 */
	public TUIGridView(Collection<T> allData, Integer rowsPerPage, Integer currentPageNo) {
		this(allData, rowsPerPage, currentPageNo, null);
    }
	
	/** 用於 client 端分頁模式. */
    public TUIGridView(Collection<T> allData) {
    	this(allData, 0, 1);
    }
    
    /**
     * server 端分頁模式用, 預先備妥欲轉為 jqGrid 指定頁碼所需格式的原始資料.<br>
     * note: 將不允許 override TUIGridColumnsHandler&lt;T&gt;.filter(int, T) method.
     * @param partialData JSON 格式的 grid 當前頁的資料
     * @param rowsPerPage 每頁最大筆數, 須與網頁端 grid 的每頁筆數設定一致(若為 0 者, 視為 client 端分頁, 其他參數將被忽略)
     * @param currentPageNo 當前頁碼(自 1 起算)
     * @param totalRecords 資料總筆數
     */
    public TUIGridView(JSONArray partialData, Integer rowsPerPage, Integer currentPageNo, Integer totalRecords) {
    	this.data = null;
    	this.jdata = partialData;
    	this.rowsPerPage = (rowsPerPage == null || rowsPerPage < 0) ? 0 : rowsPerPage;
    	this.currentPageNo = (this.rowsPerPage == 0 || currentPageNo == null || currentPageNo < 1) ? 1 : currentPageNo;
    	this.totalRecords = totalRecords;
    }
    
    /**
     * server 端分頁模式用, 預先備妥欲轉為 jqGrid 所需格式的全部原始資料, 但只截取指定頁面所需的部分內容供 client 端使用.<br>
     * note: 將不允許 override TUIGridColumnsHandler&lt;T&gt;.filter(int, T) method.
     * @param allData JSON 格式的 grid 分部資料(不侷限當前頁的)
     * @param rowsPerPage 每頁最大筆數, 須與網頁端 grid 的每頁筆數設定一致(若為 0 者, 視為 client 端分頁, 其他參數將被忽略)
     * @param currentPageNo 當前頁碼(自 1 起算)
     */
    public TUIGridView(JSONArray allData, Integer rowsPerPage, Integer currentPageNo) {
    	this(allData, rowsPerPage, currentPageNo, null);
    }
    
    /** 用於 client 端分頁模式. */
    public TUIGridView(JSONArray allData) {
    	this(allData, 0, 1);
    }
    
    /**
     * 輸出符合 jqGrid 所需的 grid 資料格式的 JSON 物件.
     * @param handler 將輸入資料轉為 JSONObject 物件, 以供 grid 元件繪圖用.
     * 		若輸入資料型態為 JSONArray 者, 則 handler function 內部所得到每筆資料的型態限定為 JSONObject
     */
	public JSONObject items(TUIGridColumnsHandler<T> handler) {
    	try  {
    		if(this.rowsPerPage == 0) { //client 端分頁模式
    			if(this.data != null)
    				return doWithoutPaging(this.data, handler);
    			if(this.jdata != null)
    				return doWithoutPaging(this.jdata, handler);
    		} else { //server 端分頁模式
    			if(this.data != null) {
    				if(this.totalRecords == null)
    					return doAllDataWithPaging(this.data, handler, this.rowsPerPage, this.currentPageNo);
					return doPartialDataWithPaging(this.data, handler, this.rowsPerPage, this.currentPageNo, this.totalRecords);
    			}
    			if(this.jdata != null) {
    				if(this.totalRecords == null)
    					return doAllDataWithPaging(this.jdata, handler, this.rowsPerPage, this.currentPageNo);
					return doPartialDataWithPaging(this.jdata, handler, this.rowsPerPage, this.currentPageNo, this.totalRecords);
    			}
    		}
            handler.release();
            return new JSONObject().put("total", "1").put("page", "1").putAsString("records", 0).put("rows", new JSONArray()); //空內容
    	} catch(Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }
	
	/** 改變原欄位順序, 如: [5,4,0]. 未指定到的欄位按原順序排在最前面, 且至少指定 2 個欄位 index 才會發動重排. */
	public TUIGridView<T> reorderColumnIndices(final int ... newColumnOrder) {
		if(newColumnOrder != null && newColumnOrder.length > 1)
			this.newColumnOrder = newColumnOrder;
		return this;
	}
	
	@Override
	public String toString() {
		return (this.data != null) ? this.data.toString() :
			(this.jdata != null) ? this.jdata.toString() : null;
	}
	
    //不分頁, 容許 data 成員可被過濾掉
	private JSONObject doWithoutPaging(final Collection<T> allData, final TUIGridColumnsHandler<T> handler) throws Throwable {
        JSONArray rows = new JSONArray();
        int records = 0; //筆數
        
    	int i = 0;
    	for(Iterator<T> j = allData.iterator(); j.hasNext(); i++) { //每一筆資料
            T oo = j.next();
            if(!handler.filter(i, oo))
                continue;
            records++;
            String rowId = String.valueOf(records);
            
            JSONObject row = new JSONObject();
            row.putAsString("id", rowId);
            row.put("cell", (oo == null) ? new JSONArray() : JSONArray.asJSONArrayAsString(reorderCells(handler.generateColumns(i, oo, rowId))));
            rows.add(row);
        }
        
        JSONObject json = new JSONObject();
        json.put("total", "1"); //資料表不分頁
        json.put("page", "1"); //不分頁的話, 永遠是第一頁
        json.putAsString("records", records); //總筆數
        json.put("rows", rows);
        //System.out.println("json: " + json);
        handler.release(); //釋放 handler 可能的暫存內容
        return json;
    }
	
    @SuppressWarnings("unchecked")
	//不分頁, 容許 data 成員可被過濾掉
    private JSONObject doWithoutPaging(final JSONArray allData, final TUIGridColumnsHandler<T> handler) throws Throwable {
        JSONArray rows = new JSONArray();
        int records = 0; //筆數
        
    	int i = 0;
    	for(Iterator<Object> j = allData.iterator(); j.hasNext(); i++) {
    		JSONObject oo = (JSONObject)j.next();
            if(!handler.filter(i, (T)oo))
                continue;
            records++;
            String rowId = String.valueOf(records);
            
            JSONObject row = new JSONObject();
            row.putAsString("id", rowId);
            row.put("cell", (oo == null) ? new JSONArray() : JSONArray.asJSONArrayAsString(reorderCells(handler.generateColumns(i, (T)oo, rowId))));
            rows.add(row);
    	}
        
        JSONObject json = new JSONObject();
        json.put("total", "1"); //資料表不分頁
        json.put("page", "1"); //不分頁的話, 永遠是第一頁
        json.putAsString("records", records); //總筆數
        json.put("rows", rows);
        //System.out.println("json: " + json);
        handler.release(); //釋放 handler 可能的暫存內容
        return json;
    }
    
    //分頁, 不支援 data 成員濾掉
    private JSONObject doAllDataWithPaging(final Collection<T> allData, final TUIGridColumnsHandler<T> handler, 
			final int rowsPerPage, final int currentPageNo) throws Throwable {
    	final int iStart = (rowsPerPage == 0 || currentPageNo == 1) ? 0 : (rowsPerPage * (currentPageNo - 1)); //欲置入 grid 顯示的 data 成員 index (自 0 起算)
    	final int iMax = iStart + rowsPerPage; //exclusive
    	final JSONArray rows = new JSONArray();
    	
    	if(iStart < allData.size()) {
    		if(allData instanceof List) {
    			List<T> allData2 = (List<T>)allData;
    			for(int i = iStart, ii = min(allData.size(), iMax); i < ii; i++)
    				appendForServerSidePaging(rows, allData2.get(i), i, handler);
    		} else {
    			int i = 0;
    			for(Iterator<T> iter = allData.iterator(); iter.hasNext(); i++) { //掃每一筆資料
    				T oo = iter.next();

    				if(i < iStart) //截取欲顯示在當前頁的 data 片斷
    	        		continue;
    	        	if(i >= iMax)
    	        		break;
    	        	appendForServerSidePaging(rows, oo, i, handler);
    			}
    		}
    	}
    	
    	final int totalPages = (int)Math.ceil(((double)allData.size()) / rowsPerPage); //總 grid 頁數
    	final int currentPageNo2 = (currentPageNo > totalPages) ? totalPages : currentPageNo;
    	
    	JSONObject json = new JSONObject();
        json.putAsString("total", totalPages);
        json.putAsString("page", currentPageNo2);
        json.putAsString("records", allData.size()); //總 grid 筆數
        json.put("rows", rows);
        //System.out.println("json: " + json);
        handler.release(); //釋放 handler 可能的暫存內容
        return json;
    }
	
	//分頁, 不支援 data 成員濾掉
	@SuppressWarnings("unchecked")
	private JSONObject doAllDataWithPaging(final JSONArray allData, final TUIGridColumnsHandler<T> handler, 
			final int rowsPerPage, final int currentPageNo) throws Throwable {
    	final int iStart = (rowsPerPage == 0 || currentPageNo == 1) ? 0 : (rowsPerPage * (currentPageNo - 1)); //欲置入 grid 顯示的 data 成員 index (自 0 起算)
		final JSONArray rows = new JSONArray();
		
		for(int i = iStart, ii = min(allData.size(), i + rowsPerPage); i < ii; i++) {
			JSONObject oo = allData.getAsJSONObject(i);
			appendForServerSidePaging(rows, (T)oo, i, handler);
		}
    	
    	final int totalPages = (int)Math.ceil(((double)allData.size()) / rowsPerPage); //總 grid 頁數
    	final int currentPageNo2 = (currentPageNo > totalPages) ? totalPages : currentPageNo;
    	
    	JSONObject json = new JSONObject();
        json.putAsString("total", totalPages);
        json.putAsString("page", currentPageNo2);
        json.putAsString("records", allData.size()); //總 grid 筆數
        json.put("rows", rows);
        //System.out.println("json: " + json);
        handler.release(); //釋放 handler 可能的暫存內容
        return json;
    }
	
	//分頁, 只輸入該頁的資料內容, 不支援 data 成員濾掉
	private JSONObject doPartialDataWithPaging(final Collection<T> partialData, final TUIGridColumnsHandler<T> handler, 
			final int rowsPerPage, final int currentPageNo, final int totalRecords) throws Throwable {
    	final JSONArray rows = new JSONArray();
    	
    	int i = 0, j = (currentPageNo - 1) * rowsPerPage; //j: 當前頁第一筆在 grid 總資料之 index
		for(Iterator<T> iter = partialData.iterator(); iter.hasNext() && i < rowsPerPage; i++, j++) { //每一筆資料
        	T oo = iter.next();
        	appendForServerSidePaging(rows, oo, j, handler);
		}
		
		int totalRecords2 = Math.max(j, totalRecords);
		int totalPages = (int)Math.ceil(((double)totalRecords2) / rowsPerPage);
    	
    	JSONObject json = new JSONObject();
        json.putAsString("total", totalPages);
        json.putAsString("page", currentPageNo);
        json.putAsString("records", totalRecords2);
        json.put("rows", rows);
        //System.out.println("json: " + json);
        handler.release(); //釋放 handler 可能的暫存內容
        return json;
    }
	
	//分頁, 只輸入該頁的資料內容, 不支援 data 成員濾掉
	@SuppressWarnings("unchecked")
	private JSONObject doPartialDataWithPaging(final JSONArray partialData, final TUIGridColumnsHandler<T> handler, 
			final int rowsPerPage, final int currentPageNo, final int totalRecords) throws Throwable {
		final JSONArray rows = new JSONArray();
		
		int i = 0, j = (currentPageNo - 1) * rowsPerPage; //j: 當前頁第一筆在 grid 總資料之 index
		for(Iterator<Object> iter = partialData.iterator(); iter.hasNext() && i < rowsPerPage; i++, j++) { //每一筆資料
			JSONObject oo = (JSONObject)iter.next();
        	appendForServerSidePaging(rows, (T)oo, j, handler);
		}
		
		int totalRecords2 = Math.max(j, totalRecords);
		int totalPages = (int)Math.ceil(((double)totalRecords2) / rowsPerPage);
    	
    	JSONObject json = new JSONObject();
        json.putAsString("total", totalPages);
        json.putAsString("page", currentPageNo);
        json.putAsString("records", totalRecords2);
        json.put("rows", rows);
        //System.out.println("json: " + json);
        handler.release(); //釋放 handler 可能的暫存內容
        return json;
    }

	//把 grid 成員 record 入 appendee 物件中
	//@param index grid 成員 record 在全部資料中的序數(自 0 起算), 只用於產生 grid rowid, 及傳入 TUIGridColumnsHandler 用
	private void appendForServerSidePaging(final JSONArray appendee, final T record, final int index, final TUIGridColumnsHandler<T> handler) throws Throwable {
		//確定開發者沒在 server 端分頁機制下使用 filter
		handler.filter(index, record);
		if(!handler.allowFiltering())
			throw new IllegalStateException("TUIGridColumnsHandler.filter() overriding not allowed when using server-side grid paging.");
    	
		//依 jqGrid 內部編排 rowid 的預設規則: string of (row index + 1) (自 1 起算)
    	String rowId = String.valueOf(index + 1);
    	
    	JSONObject row = new JSONObject();
        row.putAsString("id", rowId);
        row.put("cell", (record == null) ? new JSONArray() : JSONArray.asJSONArrayAsString(reorderCells(handler.generateColumns(index, record, rowId))));
        appendee.add(row);
	}
	
	//重排序欄位
	private Object[] reorderCells(final Object[] record) {
		if(this.newColumnOrder == null || record == null || record.length == 0)
			return record;
		
		if(this.newAllColumnOrder == null) {
			final List<Integer> colidx = new LinkedList<Integer>();
			for(int i = 0; i < record.length; i++) //先置入原順序欄位 index
				colidx.add(i);
			for(final int i : this.newColumnOrder) {
				if(colidx.remove((Object)i))
					colidx.add(i);
			}
			final int[] newAllColumnOrder = new int[colidx.size()];
			for(int i = 0; i < newAllColumnOrder.length; i++)
				newAllColumnOrder[i] = colidx.get(i);
			this.newAllColumnOrder = newAllColumnOrder;
		}
		if(this.reorderedRecordCache == null) {
			this.reorderedRecordCache = new Object[this.newAllColumnOrder.length];
		}
		
		for(int i = 0; i < this.newAllColumnOrder.length; i++) {
			this.reorderedRecordCache[i] = record[this.newAllColumnOrder[i]];
		}
		return this.reorderedRecordCache;
	}
	
	private int min(int m, int n) {
		return (m < n) ? m : n;
	}
}
