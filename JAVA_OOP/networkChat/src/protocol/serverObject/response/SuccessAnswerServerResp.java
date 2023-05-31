package protocol.serverObject.response;

import protocol.ObjectServer;
import protocol.Response;
import protocol.visitor.VisitorServer;

import java.util.List;
import java.util.Set;

public class SuccessAnswerServerResp extends ObjectServer implements Response {
    private final long succeedRequestId;
    private Set<String> userList;
    private List<String[]> chatHistory;
    private String userName;

    public SuccessAnswerServerResp(String date, long id){
        super(date);
        succeedRequestId = id;
    }

   
    public long getRequestId(){
        return succeedRequestId;
    }

   
    public Set<String> getUserSet() {
        return userList;
    }

   
    public List<String[]> getChatHistory() {
        return chatHistory;
    }

   
    public String getUserName() {
        return userName;
    }

   
    public void setUserList(Set<String> list) {
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
