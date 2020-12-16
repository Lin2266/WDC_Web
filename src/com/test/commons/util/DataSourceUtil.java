package com.test.commons.util;

import java.sql.*;
import java.util.*;
import javax.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * @deprecated 不使用外部注入的方式以取得 data source, 程式將難以測試與除錯. 本工具純為便宜行事, 不應使用.
 * 
 * <p/>
 * 搭配 Spring framework 使用, 方便取得資料庫資源.<br/>
 * <font color=red><b>警告</b>: 本工具的呼叫及相關 Connection, Statement/PreparedStatement, ResultSet
 * 的呼叫都必須在同一 thread 內完成.</font><br/>
 * 實際上為 org.springframework.jdbc.datasource.DataSourceUtils 的再包裝,
 * 以方便由 Spring 管理 transaction, 供在 Spring context 中的程式呼叫.<br/>
 * 建議的使用模式:<fieldset><pre>
 * import java.sql.*;
 * import javax.sql.*;
 * import com.tatung.commons.util.DataSourceUtil;
 * ....
 * public void queryOrUpdateXXX(...) throws XXXException {
 *     Connection conn = null;
 *     PreparedStatement pstmt = null;
 *     ResultSet rs = null;
 *
 *     try {
 *         //用 DataSourceUtil 取得資料庫連線, 而<font color="red">不是</font>直接由 dataSource 取出連線(dataSource 由外部傳入).
 *         conn = DataSourceUtil.getConnection(dataSource);
 *         //<font color="red">不要</font>自行下 conn.setAutoCommit(false) 控制 transaction, 應交由 Spring 容器管理 transaction
 *         pstmt = conn.prepareStatement(".......");
 *         pstmt.executeXXXXXX();
 *         ...
 *
 *         //資料庫連線資源(除 conn 外), 一定依序關閉, 後開的先關閉.
 *         DataSourceUtil.close(rs);
 *         DataSourceUtil.close(pstmt);
 *     } catch(Exception e) {
 *         //處理例外情形, 再包裹成 <font color="red">RuntimeException</font> 衍生類別後擲出
 *         //<font color="red">不要</font>自行 rollback, <font color="red">不要不</font>擲出 Exception, 否則 Spring 無法管理 transaction
 *     } finally {
 *         DataSourceUtil.close(rs);
 *         DataSourceUtil.close(pstmt);
 *         //一定要在最後, 且使用 DataSourceUtil 釋放連線給 pool(<font color="red">不要</font>自行下 conn.close())
 *         DataSourceUtil.releaseConnection(conn, dataSource);
 *     }
 *
 * }</pre></fieldset>
 * <p>
 * depend on: Spring framework
 */
@Deprecated
public class DataSourceUtil {
    private static final Logger log = LoggerFactory.getLogger(DataSourceUtil.class);
    
    private static DataSource _defaultDataSource;
    private static final ThreadLocal<Map<Connection, DataSource>> _currentDataSource = new ThreadLocal<Map<Connection, DataSource>>();
    private static final ThreadLocal<Map<Connection, DataSource>> _currentUnManagedDataSource = new ThreadLocal<Map<Connection, DataSource>>();
    private static final ThreadLocal<Map<Connection, Counter>> _connectionCounter = new ThreadLocal<Map<Connection, Counter>>();

    private static Map<Connection, DataSource> getCurrentDataSourceMap() {
        Map<Connection, DataSource> map = _currentDataSource.get();
        if(map == null) {
            map = new WeakHashMap<Connection, DataSource>(3); //{ Connection : DataSource }
            _currentDataSource.set(map);
        }
        return map;
    }

    private static Map<Connection, DataSource> getCurrentUnManagedDataSourceMap() {
        Map<Connection, DataSource> map = _currentUnManagedDataSource.get();
        if(map == null) {
            map = new WeakHashMap<Connection, DataSource>(3); //{ Connection : DataSource }
            _currentUnManagedDataSource.set(map);
        }
        return map;
    }

    //當前 thread 中受 Spring 管理的 Connection, 每個 Connection 被經由 getConnection(DataSource) 取得的次數
    private static Map<Connection, Counter> getConnectionCounterMap() {
        Map<Connection, Counter> map = _connectionCounter.get();
        if(map == null) {
            map = new WeakHashMap<Connection, Counter>(3); //{ Connection : Counter }
            _connectionCounter.set(map);
        }
        return map;
    }

    private static void putCurrentDataSource(Connection conn, DataSource ds) {
        getCurrentDataSourceMap().put(conn, ds);
    }

    private static void putCurrentUnManagedDataSource(Connection conn, DataSource ds) {
        getCurrentUnManagedDataSourceMap().put(conn, ds);
    }

    //把受 Spring 管理的 Connection 呼叫次數加 1
    private static void increaseConnectionCount(Connection conn) {
        Map<Connection, Counter> map = getConnectionCounterMap();
        Counter counter = map.get(conn);
        if(counter == null)
            map.put(conn, new Counter(1));
        else
            counter.increase();
    }

    //把受 Spring 管理的 Connection 呼叫次數減 1
    private static int decreaseConnectionCount(Connection conn) {
        return ((Counter)getConnectionCounterMap().get(conn)).decrease();
    }

    //當前 thread 中受 Spring 管理的 Connection, 每個 Connection 被經由 getConnection(DataSource) 取得的次數
    private static int getConnectionCount(Connection conn) {
        return ((Counter)getConnectionCounterMap().get(conn)).get();
    }

    private static DataSource getCurrentDataSource(Connection conn) {
        if(_currentDataSource.get() == null)
            return null;
        return (DataSource)getCurrentDataSourceMap().get(conn);
    }

    private static DataSource getCurrentUnManagedDataSource(Connection conn) {
        if(_currentUnManagedDataSource.get() == null)
            return null;
        return (DataSource)getCurrentUnManagedDataSourceMap().get(conn);
    }

    private static void removeCurrentDataSource(Connection conn) {
        if(_currentDataSource.get() == null)
            return;
        getCurrentDataSourceMap().remove(conn);
    }

    private static void removeCurrentUnManagedDataSource(Connection conn) {
        if(_currentUnManagedDataSource.get() == null)
            return;
        getCurrentUnManagedDataSourceMap().remove(conn);
    }

    public static void removeConnectionCounter(Connection conn) {
        if(_connectionCounter.get() == null)
            return;
        getConnectionCounterMap().remove(conn);
    }

    //false: maybe connection closed, true: nothing
    private static boolean checkConnection(Connection conn) {
        try {
            if(conn.isClosed()) {
                log.warn("You are going to close the Connection more than once.");
                return false;
            }
            return true;
        } catch(SQLException se) {
            return false;
        }
    }

    /** 指定預設 DataSource. */
    public void setDefaultDataSource(DataSource ds) {
        _defaultDataSource = ds;
    }

    /** 取系統預設 DataSource. */
    public static DataSource getDefaultDataSource() {
        if(_defaultDataSource == null)
            throw new RuntimeException("No default DataSource assigned to the util.");
        return _defaultDataSource;
    }

    /**
     * 取得資料庫連線. 如果處於 Spring TransactionManager 管理的 transaction 中,
     * 將取得當前 transaction 中的資料庫連線.
     */
    public static Connection getConnection(DataSource ds) {
        Connection conn = DataSourceUtils.getConnection(ds);
        putCurrentDataSource(conn, ds);
        increaseConnectionCount(conn);
        return conn;
    }

    /** 取得不受 Spring transaction 管理的資料庫連線. */
    public static Connection getUnManagedConnection(DataSource ds) throws SQLException {
        Connection conn = ds.getConnection();
        putCurrentUnManagedDataSource(conn, ds);
        return conn;
    }

    /** 自預設 data source 取得受 Spring transaction 控制的 Connection. */
    public static Connection getDefaultConnection() {
        return getConnection(getDefaultDataSource());
    }

    /** 自預設 data source 取得不受 Spring transaction 控制的 Connection. */
    public static Connection getDefaultUnManagedConnection() throws SQLException {
        return getUnManagedConnection(getDefaultDataSource());
    }

    /**
     * 釋放資料庫連線. 如果處於 Spring TransactionManager 管理的 transaction 中,
     * 則不自行開閉連線而由 TransactionManager 負責, 否則即真正地關閉資料庫連線.
     */
    public static void releaseConnection(Connection conn, DataSource ds) {
        if(conn == null)
            return;

        DataSource ds2 = getCurrentUnManagedDataSource(conn);
        if(ds2 != null) {
            log.warn("You should use DataSourceUtil.releaseConnection(Connection) or DataSourceUtil.close(Connection)");
            removeCurrentUnManagedDataSource(conn);
            if(!checkConnection(conn))
                return;
            try { conn.close(); } catch(SQLException se) { log.error("", se); }
            return;
        }

        ds2 = getCurrentDataSource(conn);
        if(ds2 == null) {
            log.warn("Maybe you are closing the Connection which is not gotten via this DataSourceUtil.");
            if(!checkConnection(conn))
                return;
            try { conn.close(); } catch(SQLException se) { log.error("", se); }
            return;
        }
        if(ds != ds2)
            throw new RuntimeException("The DataSource parameter obj is not where the Connection obj comes from.");
        if(decreaseConnectionCount(conn) == 0) {
            removeCurrentDataSource(conn);
            removeConnectionCounter(conn);
        }
        if(checkConnection(conn))
            DataSourceUtils.releaseConnection(conn, ds); //無 tx 時, conn 的 hashCode 可能改變
    }

    /**
     * 同 releaseConnection(Connection, DataSource).
     * @see #releaseConnection(Connection, DataSource)
     */
    public static void close(Connection conn, DataSource ds) {
        releaseConnection(conn, ds);
    }

    /** 將 Connection 歸還給當前的 data source. */
    public static void releaseConnection(Connection conn) {
        if(conn == null)
            return;

        DataSource ds = getCurrentUnManagedDataSource(conn);
        if(ds != null) {
            removeCurrentUnManagedDataSource(conn);
            if(!checkConnection(conn))
                return;
            try { conn.close(); } catch(SQLException se) { log.error("", se); }
            return;
        }

        ds = getCurrentDataSource(conn);
        if(ds == null) {
            log.warn("I am afraid you didn't get the Connection via this DataSourceUtil util.");
            if(!checkConnection(conn))
                return;
            try { conn.close(); } catch(SQLException se) { log.error("", se); }
            return;
        }
        if(decreaseConnectionCount(conn) == 0) {
            removeCurrentDataSource(conn);
            removeConnectionCounter(conn);
        }
        if(checkConnection(conn))
            DataSourceUtils.releaseConnection(conn, ds); //無 tx 時, conn 的 hashCode 可能改變
    }

    /**
     * 同 releaseConnection(Connection).
     * @see #releaseConnection(Connection)
     */
    public static void close(Connection conn) {
        releaseConnection(conn);
    }

    /** 如果 Connection 不為 null, 將本次資料庫連線的狀態, 倒回至前次 commit 後的狀態. */
    public static void rollback(Connection conn) {
        if(conn == null)
            return;
        try { conn.rollback(); } catch(SQLException se) { log.error("", se); }
    }

    private static class Counter {
        private int count;

        public Counter(int i) {
            this.count = i;
        }

        public int increase() {
            return ++this.count;
        }

        public int decrease() {
            return --this.count;
        }

        public int get() {
            return this.count;
        }
    }
}
