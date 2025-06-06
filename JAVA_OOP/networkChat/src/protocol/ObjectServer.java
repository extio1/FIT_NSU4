package protocol;

import protocol.visitor.VisitorServer;

import java.io.Serializable;

public abstract class ObjectServer implements Serializable {
    private final String date;
    public ObjectServer(String date){
        this.date = date;
    }

    public String getDate(){
        return date;
    }

    public abstract void accept(VisitorServer visitorServer);
}
