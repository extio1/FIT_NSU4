package protocol.serverObject.serialization;

import protocol.ObjectServer;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.response.SuccessAnswerServer;

import java.util.List;

public class SuccessAnswerServerSerialized extends ObjectServer implements SuccessAnswerServer {
    private final long succeedRequestId;
    private List<String> userList;
    private List<String[]> chatHistory;
    private String userName;

    public SuccessAnswerServerSerialized(String date, long id){
        super(date);
        succeedRequestId = id;
    }

    @Override
    public long getRequestId(){
        return succeedRequestId;
    }

    @Override
    public List<String> getUserList() {
        return userList;
    }

    @Override
    public List<String[]> getChatHistory() {
        return chatHistory;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserList(List<String> list) {
        userList = list;
    }

    @Override
    public void setCharHistory(List<String[]> history) {
        chatHistory = history;
    }

    @Override
    public void setUserName(String name) {
        userName = name;
    }

    @Override
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitResponse(this);
    }
}
