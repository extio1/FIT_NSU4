package server.clientHandler;

public class ServerSessionContext {
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if(this.userName == null){
            this.userName = userName;
        }
    }
}
