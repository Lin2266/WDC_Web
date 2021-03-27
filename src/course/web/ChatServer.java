package course.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import net.sf.json.JSONObject;
/**
 * 聊天伺服器類
 * @author shiyanlou
 *
 */
@ServerEndpoint("/websocket")
public class ChatServer {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");	// 日期格式化
    @OnOpen
    public void open(Session session) {
// 新增初始化操作
    }
    /**
     * 接受客戶端的訊息，並把訊息傳送給所有連線的會話
     * @param message 客戶端發來的訊息
     * @param session 客戶端的會話
     */
    @OnMessage
    public void getMessage(String message, Session session) {
// 把客戶端的訊息解析為JSON物件
        JSONObject jsonObject = JSONObject.fromObject(message);
// 在訊息中新增傳送日期
        jsonObject.put("date", DATE_FORMAT.format(new Date()));
// 把訊息傳送給所有連線的會話
        for (Session openSession : session.getOpenSessions()) {
// 新增本條訊息是否為當前會話本身發的標誌
            jsonObject.put("isSelf", openSession.equals(session));
// 傳送JSON格式的訊息
            openSession.getAsyncRemote().sendText(jsonObject.toString());
        }
    }
    @OnClose
    public void close() {
// 新增關閉會話時的操作
    }
    @OnError
    public void error(Throwable t) {
// 新增處理錯誤的操作
    }
}
