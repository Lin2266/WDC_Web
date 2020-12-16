package com.test.commons.util.internal;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.util.DBRowMappingPostProcessing;
import com.test.commons.util.FileUtil;

/**
 * 把 ResultSet 每筆資料以 JavaBean 裝載起來.
 */
public abstract class AbstractBeanRowMapper<T> {
	private static final Logger log = LoggerFactory.getLogger(AbstractBeanRowMapper.class);
	private static final int TYPE_BOOLEAN = 1;
    private static final int TYPE_BOOLEAN_OBJ = 2;
    private static final int TYPE_STRING = 3;
    private static final int TYPE_BYTE = 4;
    private static final int TYPE_BYTE_OBJ = 5;
    private static final int TYPE_BYTE_ARRAY = 6; //for blob
    private static final int TYPE_SHORT = 7;
    private static final int TYPE_SHORT_OBJ = 8;
    private static final int TYPE_INT = 9;
    private static final int TYPE_INT_OBJ = 10;
    private static final int TYPE_LONG = 11;
    private static final int TYPE_LONG_OBJ = 12;
    private static final int TYPE_FLOAT = 13;
    private static final int TYPE_FLOAT_OBJ = 14;
    private static final int TYPE_DOUBLE = 15;
    private static final int TYPE_DOUBLE_OBJ = 16;
    private static final int TYPE_NUMBER = 17;
    private static final int TYPE_BIGDECIMAL = 18;
    private static final int TYPE_DATETIME = 19; //java.util.Date
    private static final int TYPE_TIMESTAMP = 20; //java.sql.Timestamp
    private static final int TYPE_DATE = 21; //java.sql.Date
    private static final int TYPE_TIME = 22; //java.sql.Time
    private static final int TYPE_BLOB = 23;
    private static final int TYPE_CLOB = 24;
    private static final int TYPE_FILE = 25; //java.io.File 接收 DB blob 欄位資料, 存至暫存檔 (see com.tatung.commons.util.FileUtil.createTempFile())
    private static final int TYPE_UNKNOWN = 26;
    
    private Class<?> valueClass;
    private Map<String, ColumnDescriptor> columnDescriptorsMap; //{ bean_property_name : ColumnDescriptor }
    private ColumnDescriptor[] columnDescriptors; //bean 及 result 的欄位中同時存在的
    private boolean isFirstRow; //ResultSet 是否當前為第一筆
    private DBRowMappingPostProcessing<T> postProcessing; //每筆 clazz 被裝載資料完畢後, 欲接續進行處理的動作
    
    /**
     * @param clazz 用來裝載每一筆 table 資料的 Java Bean
     */
    protected void init(Class<?> clazz) {
    	Field[] fields = clazz.getDeclaredFields();
        this.valueClass = clazz;
        this.columnDescriptorsMap = new HashMap<String, ColumnDescriptor>((int)(fields.length / 0.75 + 1), 0.75F); //以最常用的 field 個數為基準預留空間
        
        //先掃描 bean 可用的 field 及 property (以 @Column 裝飾)
        //scan 標注以 @Column 的 field (但設值一律透過 property setter)
        for(int i = 0, ii = fields.length; i < ii; i++) {
            Field field = fields[i];
            Annotation an = field.getAnnotation(Column.class);
            if(an == null)
                continue;
            
            String columnName = ((Column)an).name();
            if("".equals(columnName))
                columnName = field.getName();
            
            //this.columnDescriptorsMap.put(columnName, new ColumnDescriptor(columnName, field, null, field.getType())); //棄用, 不對 field 直接設值了
            PropertyDescriptor pd = org.springframework.beans.BeanUtils.getPropertyDescriptor(clazz, field.getName()); //由 field name 取property
            Method setter = null;
            if(pd == null || (setter = pd.getWriteMethod()) == null || !Modifier.isPublic(setter.getModifiers()))
                throw new RuntimeException("class " + clazz.getName() + "'s field '" + field.getName() + "' annotated by @Column has no corresponding public property setter method.");
            this.columnDescriptorsMap.put(columnName, new ColumnDescriptor(columnName, null, setter, pd.getPropertyType()));
        }
        
        //scan 標注以 @Column 的 property setter/getter
        Method[] methods = clazz.getMethods();
        for(int i = 0, ii = methods.length; i < ii; i++) {
            Method method = methods[i];
            Annotation an = method.getAnnotation(Column.class);
            if(an == null)
                continue;
            
            PropertyDescriptor pd = org.springframework.beans.BeanUtils.findPropertyForMethod(method);
            String columnName = ((Column)an).name();
            if("".equals(columnName))
                columnName = pd.getName();
            if(this.columnDescriptorsMap.containsKey(columnName))
                continue;
            
            Method setter = null;
            if(pd == null || (setter = pd.getWriteMethod()) == null || !Modifier.isPublic(setter.getModifiers()))
                throw new RuntimeException("class '" + clazz.getName() + "', which method '" + method.getName() + "' annotated by @Column is not a property or has no public property setter method.");
            this.columnDescriptorsMap.put(columnName, new ColumnDescriptor(columnName, null, setter, pd.getPropertyType()));
        }
        
        this.isFirstRow = true;
    }
    
    /**
     * @param clazz 用來裝載每一筆 table 資料的 Java Bean
     * @param postProcessing 定義每筆 clazz 物件被裝載資料完畢後, 欲接著後續處理的動作(例如修改物件中某些屬性值)
     */
    protected void init(Class<?> clazz, DBRowMappingPostProcessing<T> postProcessing) {
    	init(clazz);
    	this.postProcessing = postProcessing;
    }
    
    /**
     * 將單筆的查詢結果置入一個 bean 的屬性中
     * @param rs
     * @return 裝載一筆資料的一個 Java Bean 物件
     */
    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs) throws SQLException {
        try {
        	Object vo = this.valueClass.newInstance();
            
            if(this.isFirstRow) { //先由第一筆資料之各欄位, 找出 bean properties 中確實有和 result set 的欄位對應到的 property
            	final StringBuilder msg = log.isDebugEnabled() ? new StringBuilder() : null;
            	
            	for(Iterator<Map.Entry<String, ColumnDescriptor>> i = this.columnDescriptorsMap.entrySet().iterator(); i.hasNext(); ) {
            		Map.Entry<String, ColumnDescriptor> entry = i.next();
            		ColumnDescriptor cd = entry.getValue();
            		//if(cd.isField()) { //直接對 bean field 設值
            		//	Field field = cd.field;
					//	field.setAccessible(true);
					//	try { field.set(vo, getColumnValue(i, rs, cd)); } catch(SQLException se) {}
            		//} else { //針對 bean property setter 來設值
        			Method method = cd.setter;
                    try { method.invoke(vo, getColumnValue(i, rs, cd, msg)); } catch(SQLException se) {}
            		//}
            	}
                this.isFirstRow = false;
                
                if(msg != null && msg.length() != 0)
                	log.debug("abort mapping: {}.{}", this.valueClass.getName(), msg);
            } else { //第二筆之後只針對必要欄位(使用陣列裝載)操作
                if(this.columnDescriptors == null) { //lazy init
                    this.columnDescriptors = this.columnDescriptorsMap.values().toArray((ColumnDescriptor[])Array.newInstance(ColumnDescriptor.class, this.columnDescriptorsMap.size()));
                    this.columnDescriptorsMap = null;
                }
                
                for(int i = 0, ii = this.columnDescriptors.length; i < ii; i++) {
                    ColumnDescriptor cd = this.columnDescriptors[i];
                    //if(cd.isField()) { //直接對 bean field 設值
                    //    Field field = cd.field;
                    //    field.setAccessible(true);
                    //    field.set(vo, getColumnValue(rs, cd));
                    //} else { //針對 bean property setter 來設值
                    Method method = cd.setter;
                    method.invoke(vo, getColumnValue(rs, cd));
                    //}
                }
            }
            
			//vo 設值後之後續
            if(this.postProcessing != null)
                this.postProcessing.execute(rs, (T)vo);
            
            return (T)vo;
        } catch(InstantiationException e) {
            throw new RuntimeException("class '" + this.valueClass.getName() + "' probably has no default constructor.", e);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    //改自 org.springframework.jdbc.core.SingleColumnRowMapper 源碼
    private Object getColumnValue(ResultSet rs, ColumnDescriptor cd) throws SQLException {
        String columnName = cd.columnName;
        Object value = null;
        boolean wasNullCheck = false;

        //Explicitly extract typed value, as far as possible.
        switch(cd.type) {
        	case TYPE_BOOLEAN: value = (rs.getBoolean(columnName) ? Boolean.TRUE : Boolean.FALSE); break; //primary type: boolean 非 true 即 false
        	case TYPE_BOOLEAN_OBJ: value = (rs.getBoolean(columnName) ? Boolean.TRUE : Boolean.FALSE); wasNullCheck = true; break; //Boolean Object: 可能為 null 或 false 或 true
        	case TYPE_STRING: value = rs.getString(columnName); break;
        	case TYPE_BYTE:
        	case TYPE_BYTE_OBJ: value = new Byte(rs.getByte(columnName)); wasNullCheck = true; break;
        	case TYPE_BYTE_ARRAY: value = rs.getBytes(columnName); break; //bytes
        	case TYPE_SHORT:
        	case TYPE_SHORT_OBJ: value = new Short(rs.getShort(columnName)); wasNullCheck = true; break;
        	case TYPE_INT:
        	case TYPE_INT_OBJ: value = new Integer(rs.getInt(columnName)); wasNullCheck = true; break;
        	case TYPE_LONG:
        	case TYPE_LONG_OBJ: value = new Long(rs.getLong(columnName)); wasNullCheck = true; break;
        	case TYPE_FLOAT:
        	case TYPE_FLOAT_OBJ: value = new Float(rs.getFloat(columnName)); wasNullCheck = true; break;
        	case TYPE_DOUBLE:
        	case TYPE_DOUBLE_OBJ:
        	case TYPE_NUMBER: value = new Double(rs.getDouble(columnName)); wasNullCheck = true; break;
        	case TYPE_BIGDECIMAL: value = rs.getBigDecimal(columnName); break;
        	case TYPE_DATETIME:
        	case TYPE_TIMESTAMP: value = rs.getTimestamp(columnName); break;
        	case TYPE_DATE: value = rs.getDate(columnName); break;
        	case TYPE_TIME: value = rs.getTime(columnName); break;
        	case TYPE_BLOB: value = rs.getBlob(columnName); break;
        	case TYPE_CLOB: value = rs.getClob(columnName); break;
        	case TYPE_FILE: value = dumpToTempFile(rs, columnName); break;
        	default: value = rs.getObject(columnName); //Some unknown type desired -> rely on getObject. 
        }

        //Perform was-null check if demanded (for results that the JDBC driver returns as primitives).
        if(wasNullCheck && value != null && rs.wasNull())
            value = null;
        return value;
    }
    
    private Object getColumnValue(final Iterator<Map.Entry<String, ColumnDescriptor>> i, final ResultSet rs, final ColumnDescriptor cd, final StringBuilder debugMsg) 
    		throws SQLException {
        try {
            return getColumnValue(rs, cd);
        } catch(SQLException se) {
            if(debugMsg != null) {
            	debugMsg.append("[");
            	if(cd.isField())
            		debugMsg.append(cd.field.getName());
            	else
            		debugMsg.append(cd.setter.getName()).append("()");
            	debugMsg.append("=>").append(cd.columnName).append(" for:").append(se.getMessage()).append("]");
            }
            i.remove(); //可能該 bean field/property 在 ResultSet 物件中, 沒有對應的欄位
            throw se;
        }
    }

    private final File dumpToTempFile(final ResultSet rs, final String columnName) throws SQLException {
    	InputStream in = null;
    	
    	try {
    		in = rs.getBinaryStream(columnName);
    		final File f = FileUtil.createTempFile("BeanRow-", "");
    		FileUtil.dump(in, f);
    		in.close();
    		in = null;
    		return f;
		} catch(IOException ie) {
			throw new RuntimeException(ie.getMessage(), ie);
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
		}
    }
    
    private final class ColumnDescriptor {
        public String columnName;
        public Field field;
        public Method setter;
        public int type;
        
        public ColumnDescriptor(String columnName, Field field, Method setter, Class<?> type) {
            this.columnName = columnName;
            this.field = field;
            this.setter = setter;
            
            //改自 org.springframework.jdbc.core.SingleColumnRowMapper 源碼
            //Explicitly extract typed value, as far as possible.
            if(boolean.class.equals(type))
            	this.type = TYPE_BOOLEAN;
            else if(Boolean.class.equals(type))
            	this.type = TYPE_BOOLEAN_OBJ;
            else if(String.class.equals(type))
            	this.type = TYPE_STRING;
            else if(byte.class.equals(type))
            	this.type = TYPE_BYTE;
            else if(Byte.class.equals(type))
            	this.type = TYPE_BYTE_OBJ;
            else if(byte[].class.equals(type))
            	this.type = TYPE_BYTE_ARRAY;
            else if(short.class.equals(type))
            	this.type = TYPE_SHORT;
            else if(Short.class.equals(type))
            	this.type = TYPE_SHORT_OBJ;
            else if(int.class.equals(type))
            	this.type = TYPE_INT;
            else if(Integer.class.equals(type))
            	this.type = TYPE_INT_OBJ;
            else if(long.class.equals(type))
            	this.type = TYPE_LONG;
            else if(Long.class.equals(type))
            	this.type = TYPE_LONG_OBJ;
            else if(float.class.equals(type))
            	this.type = TYPE_FLOAT;
            else if(Float.class.equals(type))
            	this.type = TYPE_FLOAT_OBJ;
            else if(double.class.equals(type))
            	this.type = TYPE_DOUBLE;
            else if(Double.class.equals(type))
            	this.type = TYPE_DOUBLE_OBJ;
            else if(Number.class.equals(type))
            	this.type = TYPE_NUMBER;
            else if(BigDecimal.class.equals(type))
            	this.type = TYPE_BIGDECIMAL;
            else if(java.util.Date.class.equals(type))
            	this.type = TYPE_DATETIME;
            else if(java.sql.Timestamp.class.equals(type))
            	this.type = TYPE_TIMESTAMP;
            else if(java.sql.Date.class.equals(type))
            	this.type = TYPE_DATE;
            else if(java.sql.Time.class.equals(type))
            	this.type = TYPE_TIME;
            else if(Blob.class.equals(type))
            	this.type = TYPE_BLOB;
            else if(Clob.class.equals(type))
            	this.type = TYPE_CLOB;
            else if(File.class.equals(type))
            	this.type = TYPE_FILE;
            else
            	this.type = TYPE_UNKNOWN;
        }
        
        public boolean isField() {
            return (this.field != null);
        }
    }
}
