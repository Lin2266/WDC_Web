package com.test.commons.web.widget;

import java.util.*;
import org.apache.commons.beanutils.PropertyUtils;

import com.test.commons.util.JSONArray;
import com.test.commons.util.JSONObject;
import com.test.commons.util.MsgUtil;

/**
 * 輸出 HTML &lt;select&gt; 內容所需的 JSON 格式資料. 格式: <pre>
 * { "selectedValues": [ "value2", "value3", ...],
 *   "rows": [ ["value1", "label1"], ["value2", "label2"], ["value3", "label3"], ... ] }
 * </pre>
 * <p>
 * depend on: jquery
 */
public class TUISelect<T> {
	private List<T> data;
	private JSONArray jdonData;
	private Set<String> selectedValues;
	private String emptyOptionLabel; //TODO: jQuerySelect 已不需此屬性, 但 jQMSelect 尚未跟進修正, 仍暫予保留
	
	public TUISelect(List<T> data) {
		this.data = data;
	}

	/**
	 * 以 JSONArray 型態為資料來源者, 其組成分子之型態必須為 JSONObject.
	 * @see com.test.commons.util.JSONObject
	 * @see com.test.commons.util.JSONArray
	 */
	public TUISelect(JSONArray data) {
		this.jdonData = data;
	}
	
	/** 重新指定產生表單用的資料, 先前所作的設定將丢棄 */
	public final void reset(List<T> data) {
		this.data = data;
		this.jdonData = null;
		this.selectedValues = null;
		this.emptyOptionLabel = null;
	}
	
	/** 重新指定產生表單用的資料, 先前所作的設定將丢棄 */
	public final void reset(JSONArray data) {
		this.data = null;
		this.jdonData = data;
		this.jdonData = null;
		this.selectedValues = null;
		this.emptyOptionLabel = null;
	}

	final Set<String> getSelectedValues() {
		return selectedValues;
	}
	
	/** 
	 * 指定要被預先選取的值(在呼叫 options()/optionsJSON() 前執行),
	 * 但當自行實作 TUISelectOptionHandler 決定每個 option 內容時, 此屬性無效,
	 * 需另行在實作 TUISelectOptionHandler 時, override selected() method.
	 */
	public final TUISelect<T> setSelectedValues(String ... selectedValues) {
		if(selectedValues == null || selectedValues.length == 0) {
			this.selectedValues = null;
		} else {
			this.selectedValues = new HashSet<String>();
			for(String v : selectedValues)
				this.selectedValues.add(v);
		}
		return this;
	}
	
	final String getEmptyOptionLabel() {
		return emptyOptionLabel;
	}

	/** 指定要在選單內容最前面加一筆空值且指定其 label */
	public final TUISelect<T> setEmptyOptionLabel(String emptyOptionLabel) {
		this.emptyOptionLabel = emptyOptionLabel;
		return this;
	}

	/**
	 * 供前端 JavaScript 程式讀取的 JSON 物件, 
	 * 資料成員限定為由內含 [value, label] 所組成的陣列(T 類型為 String[], 裝載 value, label)
	 */
	public final JSONObject options() {
		return options(new TUISelectOptionHandler<T>() {
			private T currentVO;
			private String currentValue;
			private String selValue;
			
			@Override
			public String value(int index, T vo) {
				if(vo == null)
					return "";
				if(this.currentVO != vo) { //為了加速 vo 被多次取值時的速度, 且被 cache 住的 vo 所在的物件 TUISelect 非永久存在物件, 理應不會造成記憶憶空間缺失
					this.currentVO = vo;
					String value = ((String[])vo)[0];
					this.currentValue = (value == null) ? "" : value;
				}
				return this.currentValue;
			}

			@Override
			public String label(int index, T vo) {
				if(vo == null)
					return "";
				return ((String[])vo)[1];
			}

			@Override
			public boolean selected(int index, T vo) {
				if(getSelectedValues() == null || vo == null)
					return false;
				
				if(this.selValue != null) //針對單選選單, 可能有一筆被選取項目的常見情形
					return this.selValue.equals(value(index, vo));
				
				if(getSelectedValues().size() == 1) {
					this.selValue = getSelectedValues().toArray(new String[1])[0];
					return this.selValue.equals(value(index, vo));
				}
				return getSelectedValues().contains(value(index, vo));
			}

			@Override
			public void release() {
				this.currentValue = null;
				this.currentVO = null;
				this.selValue = null;
			}
		});
	}
	
	/**
	 * 以 valueName, labelName 作為資料成員 VO (Java Bean, 或 Map 物件)的屬性名, 分別向 VO 取出做為 HTML &lt;option value=""&gt;...&lt;/option&gt; 的內容. 
	 * @param valueName
	 * @param labelName
	 * @return 供前端 JavaScript 程式讀取的 JSON 物件
	 */
	public final JSONObject options(final String valueName, final String labelName) {
		return options(new TUISelectOptionHandler<T>() {
			private final int TYPE_UNDETERMINED = 0;
			private final int TYPE_MAP = 1;
			private final int TYPE_JAVABEAN = 2;
			private int typeNO; //TYPE_UNDETERMINED, TYPE_MAP, TYPE_JAVABEAN
			private T currentVO;
			private String currentValue;
			private String selValue;
			
			@Override
			public String value(int index, T vo) {
				try {
					if(vo == null)
						return "";
					if(this.currentVO != vo) { //為了加速 vo 被多次取值時的速度, 且被 cache 住的 vo 所在的物件 TUISelect 非永久存在物件, 理應不會造成記憶憶空間缺失
						this.currentVO = vo;
						Object v = (typeNO(vo) == TYPE_MAP) ? ((Map<?, ?>)vo).get(valueName) : PropertyUtils.getProperty(vo, valueName);
						this.currentValue = ((v == null) ? "" : v.toString());
					}
					return this.currentValue;
				} catch(Throwable t) {
					throw new RuntimeException(t.getMessage(), t);
				}
			}

			@Override
			public String label(int index, T vo) {
				try {
					if(vo == null)
						return "";
					Object v = (typeNO(vo) == TYPE_MAP) ? ((Map<?, ?>)vo).get(labelName) : PropertyUtils.getProperty(vo, labelName);
					return ((v == null) ? "" : v.toString());
				} catch(Throwable t) {
					throw new RuntimeException(t.getMessage(), t);
				}
			}

			@Override
			public boolean selected(int index, T vo) {
				if(getSelectedValues() == null || vo == null)
					return false;
				
				if(this.selValue != null) //針對單選選單, 可能有一筆被選取項目的常見情形
					return this.selValue.equals(value(index, vo));
				
				if(getSelectedValues().size() == 1) {
					this.selValue = getSelectedValues().toArray(new String[1])[0];
					return this.selValue.equals(value(index, vo));
				}
				return getSelectedValues().contains(value(index, vo));
			}

			@Override
			public void release() {
				this.typeNO = 0;
				this.currentValue = null;
				this.currentVO = null;
				this.selValue = null;
			}
			
			int typeNO(T vo) {
				if(this.typeNO != TYPE_UNDETERMINED)
					return this.typeNO;
				if(vo instanceof Map)
					return (this.typeNO = TYPE_MAP);
				return (this.typeNO = TYPE_JAVABEAN);
			}
		});
	}
	
	/**
	 * 以自定義 callback 函數以決定對每筆 vo 取 value/label 值的方式, 而產生供繪製 HTML select 選單用的 JSON 物件
	 */
	@SuppressWarnings("unchecked")
	public final JSONObject options(TUISelectOptionHandler<T> callback) {
		if(this.data == null && this.jdonData == null)
			return new JSONObject();

		JSONArray rows = new JSONArray();
		JSONArray selected = new JSONArray();
		
		if(getEmptyOptionLabel() != null) { //選單第一筆要放空值者
			rows.add(new JSONArray().addAsString("").addAsString(MsgUtil.message(getEmptyOptionLabel())));
			if(getSelectedValues() == null) //tag 也未指定預選項時, 把本第一筆空值項設為預選項
				selected.add("");
		}
		
		if(this.data != null) {
			for(int i = 0; i < this.data.size(); i++) {
				T vo = this.data.get(i);
				handleVO(vo, i, callback, rows, selected);
			}
		} else { //JSONArray, 成員 type 必是 JSONObject
			for(int i = 0, ii = this.jdonData.size(); i < ii; i++) {
				JSONObject vo = this.jdonData.getAsJSONObject(i);
				handleVO((T)vo, i, callback, rows, selected);
			}
		}

		JSONObject ret = new JSONObject();
		ret.put("rows", rows);
		ret.put("emptyOptionLabel", (getEmptyOptionLabel() == null) ? "" : MsgUtil.message(getEmptyOptionLabel()));
		ret.put("selectedValues", selected);
		//System.out.println("options=" + ret.toString());
		callback.release(); //釋放 callback 物件所暂存的資源
		return ret;
	}
	
	void handleVO(T vo, int index, TUISelectOptionHandler<T> handler, JSONArray retRows, JSONArray retSelected) {
		if(!handler.filter(index, vo))
			return;
		String v = handler.value(index, vo);
		if(handler.isSelectedOverrided()) {
			if(handler.selected(index, vo))
				retSelected.addAsString(v);
		} else if(getSelectedValues() != null && getSelectedValues().size() != 0) {
			if(getSelectedValues().contains(v))
				retSelected.addAsString(v);
		}
		retRows.add(new JSONArray().addAsString(v).addAsString(handler.label(index, vo)));
	}
}
