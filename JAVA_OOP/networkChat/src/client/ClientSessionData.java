package client;

import protocol.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSessionData {
    private List<String[]> chatHistory = new ArrayList<>();
    private List<String> userList = new ArrayList<>();
    private List<String[]> error =  new ArrayList<>();
    private volatile String nickname;
    private final Map<Long, Request> requestsWithoutAnswerYet = new HashMap<>();
    private volatile boolean agree;
    private volatile boolean askRequest = false;
    private volatile String askMess;

    synchronized public List<String> getWaitingRequestsList(){
        return requestsWithoutAnswerYet.values().stream().map((r)->r.getClass().getSimpleName()).toList();
    }

    synchronized public boolean isRegistered(){
        return nickname != null;
    }
    synchronized public List<String[]> getErrorList() {
        List<String[]> list = error.stream().toList();
        error = new ArrayList<>();
        return list;
    }

    synchronized public String getAskMess(){
        return askMess;
    }
    synchronized public boolean isAskRequest(){
        return askRequest;
    }

    synchronized public List<String[]> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    synchronized public List<String> getUserList() {
        return new ArrayList<>(userList);
    }

    synchronized public String getClientName() {
        return nickname;
    }

    synchronized public Request getWaitingRequestById(long id) {
        return requestsWithoutAnswerYet.get(id);
    }

    synchronized public void setChatHistory(List<String[]> history) {
        chatHistory = history;
        this.notifyAll();
    }

    synchronized public void setClientName(String name) {
        nickname = name;
        this.notifyAll();
    }


    synchronized public void setUserList(List<String> users) {
        userList = users;
        this.notifyAll();
    }


    synchronized public void addUserToList(String name) {
        userList.add(name);
        this.notifyAll();
    }

    synchronized public void removeUserFromList(String name) {
        userList.remove(name);
        this.notifyAll();
    }

    synchronized public void addError(String who, String reason) {
        error.add(new String[]{who, reason});
        this.notifyAll();
    }

    synchronized public boolean askYesNo(String message) {
        askMess = message;
        askRequest = true;
        this.notifyAll();

        try {
            this.wait();
        } catch (InterruptedException e){
            return false;
        }

        askRequest = false;
        return agree;
    }

    synchronized public void setLastDialogReact(boolean react){
        agree = react;
        this.notifyAll();
    }

    synchronized public void addMessage(String date, String sender, String message) {
        chatHistory.add(new String[]{date, sender, message});

        this.notifyAll();
    }

    synchronized public void addWaitingRequest(Request request){
        requestsWithoutAnswerYet.put(request.getId(), request);
        this.notifyAll();
    }

    synchronized public void removeWaitingRequest(long requestId){
        requestsWithoutAnswerYet.remove(requestId);
        this.notifyAll();
    }

    public String getNickname() {
        return nickname;
    }


}
