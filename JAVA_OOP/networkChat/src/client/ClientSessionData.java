package client;

import protocol.ObjectUser;
import protocol.userObject.MessageUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSessionData {
    private final List<String> charHistory = new ArrayList<>();
    private final List<String> userList = new ArrayList<>();
    private String nickname;
    private boolean lastOperationSucceed;

    private final Map<Long, ObjectUser> requestsWithoutAnswerYet = new HashMap<>();

    public List<String> getChatHistory() {
        return charHistory;
    }

    public List<String> getUserList() {
        return userList;
    }

    public String getClientName() {
        return nickname;
    }

    public boolean getLastOperationStatus(){
        return lastOperationSucceed;
    }

    public void setChatHistory() {

    }

    public void setClientName() {

    }

    public void setUserList() {

    }

    public void addWaitingRequest(ObjectUser request){
        requestsWithoutAnswerYet.put(request.getId(), request);
    }

    public void removeWaitingRequest(long requestId){
        requestsWithoutAnswerYet.remove(requestId);
    }

    void setLastOperationStatus(boolean newStatus){
        lastOperationSucceed = newStatus;
    }

}
