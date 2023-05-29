package protocol.serverObject.response;

import protocol.ObjectServer;
import protocol.Response;
import protocol.visitor.VisitorServer;

import java.util.List;

public class SuccessAnswerServerResp extends ObjectServer implements Response {
    private final long succeedRequestId;
    private List<String> userList;
    private List<String[]> chatHistory;
    private String userName;

    public SuccessAnswerServerResp(String date, long id){
        super(date);
        succeedRequestId = id;
    }

   
    public long getRequestId(){
        return succeedRequestId;
    }

   
    public List<String> getUserList() {
        return userList;
    }

   
    public List<String[]> getChatHistory() {
        return chatHistory;
    }

   
    public String getUserName() {
        return userName;
    }

   
    public void setUserList(List<String> list) {
        userList = list;
    }

   
    public void setCharHistory(List<String[]> history) {
        chatHistory = history;
    }

   
    public void setUserName(String name) {
        userName = name;
    }

   
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitSuccessResponse(this);
    }
}
