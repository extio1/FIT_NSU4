package protocol.serverObject.response;

import protocol.Response;

import java.util.List;

public interface SuccessAnswerServer extends Response {
    List<String> getUserList();
    List<String[]> getChatHistory();
    String getUserName();
    void setUserList(List<String> list);
    void setCharHistory(List<String[]> history);
    void setUserName(String name);
}
