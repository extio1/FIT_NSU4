package client.clientImpls.clientSerialize;

import client.ClientSessionData;
import protocol.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientSessionDataSerialize implements ClientSessionData {
    private List<String[]> chatHistory = new ArrayList<>();
    private List<String> userList = new ArrayList<>();
    private List<String[]> error =  new ArrayList<>();
    private volatile String nickname;
    private final Map<Long, Request> requestsWithoutAnswerYet = new HashMap<>();

    @Override
    public List<String> getWaitingRequestsList(){
        return requestsWithoutAnswerYet.values().stream().map((r)->r.getClass().getSimpleName()).toList();
    }
    @Override
    public boolean isRegistered(){
        return nickname != null;
    }
    @Override
    public List<String[]> getErrorList() {
        List<String[]> list = error.stream().toList();
        error = new ArrayList<>();
        return list;
    }

    public List<String[]> getChatHistory() {
        return chatHistory;
    }

    public List<String> getUserList() {
        return userList;
    }

    public String getClientName() {
        return nickname;
    }

    @Override
    public Request getWaitingRequestById(long id) {
        return requestsWithoutAnswerYet.get(id);
    }

    @Override
    synchronized public void setChatHistory(List<String[]> history) {
        chatHistory = history;
        this.notifyAll();
    }
    @Override
    synchronized public void setClientName(String name) {
        nickname = name;
        this.notifyAll();
    }

    @Override
    synchronized public void setUserList(List<String> users) {
        userList = users;
        this.notifyAll();
    }

    @Override
    synchronized public void addUserToList(String name) {
        userList.add(name);
        this.notifyAll();
    }

    @Override
    synchronized public void removeUserFromList(String name) {
        userList.remove(name);
        this.notifyAll();
    }

    @Override
    synchronized public void addError(String who, String reason) {
        error.add(new String[]{who, reason});
        this.notifyAll();
    }

    @Override
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

    @Override
    public String getNickname() {
        return nickname;
    }


}
