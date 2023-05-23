package server;

import java.util.ArrayList;
import java.util.List;

public class ChatContext {
    private final List<String> userList = new ArrayList<>();
    private final List<String[]> chatHistory = new ArrayList<>();

    public void addUser(String name){
        userList.add(name);
    }
    public void removeUser(String name){
        userList.remove(name);
    }
    public void addMsg(String date, String name, String msg){
        chatHistory.add(new String[]{date, name, msg});
    }

    public List<String> getUserList() {
        return userList;
    }

    public List<String[]> getChatHistory(int lastN) {
        int idx = chatHistory.size()-1-lastN;
       // return  chatHistory;
        if(idx >= 0)
            return new ArrayList<>(chatHistory.subList(idx, chatHistory.size()));
        else {
            return new ArrayList<>(chatHistory.subList(0, chatHistory.size()));
        }
    }
}
