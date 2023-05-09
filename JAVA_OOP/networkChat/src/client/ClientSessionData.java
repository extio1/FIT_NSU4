package client;

import java.util.List;

public interface ClientSessionData {
    List<String> getChatHistory();
    void setChatHistory();

    String getClientName();
    void setClientName();

}
