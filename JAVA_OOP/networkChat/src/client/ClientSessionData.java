package client;

import protocol.Request;

import java.util.List;

public interface ClientSessionData {

    /***
     * Отдаёт копию, длину своего внутреннего листа с ошибками устнавливает в ноль,
     * таким образом на след. запросе, если новых ошибок не возникло, то и их список
     * будет пуст
     * */
    public List<String[]> getErrorList();
    public boolean isRegistered();
    public List<String[]> getChatHistory();
    public List<String> getUserList();
    public String getClientName();
    public Request getWaitingRequestById(long id);
    public void setChatHistory(List<String[]> history);
    public void setClientName(String name);
    public void setUserList(List<String> users);
    public void addUserToList(String name);
    public void removeUserFromList(String name);
    public void addError(String who, String reason);
    public void addMessage(String date, String sender, String message);
    public void addWaitingRequest(Request request);
    public void removeWaitingRequest(long requestId);
    public String getNickname();
    public List<String> getWaitingRequestsList();
}
