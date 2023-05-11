package client;

import java.util.ArrayList;
import java.util.List;

public class ClientSessionData {
    private final List<String> charHistory = new ArrayList<>();
    private final List<String> userList = new ArrayList<>();
    private String nickname;
    private boolean lastOperationSucceed;

    List<String> getChatHistory() {
        return charHistory;
    }

    List<String> getUserList() {
        return userList;
    }

    String getClientName() {
        return nickname;
    }

    boolean getLastOperationStatus(){
        return lastOperationSucceed;
    }

    void setChatHistory() {

    }

    void setClientName() {

    }

    void setUserList() {

    }

    void setLastOperationStatus(boolean newStatus){
        lastOperationSucceed = newStatus;
    }

}
