package protocol.serverObject.event;

import protocol.Event;
import protocol.ObjectServer;
import protocol.visitor.VisitorServer;

public class MessageFromUserEv extends ObjectServer implements Event {
    private final String nickname;
    private final String message;

    public MessageFromUserEv(String date, String nickname, String message){
        super(date);
        this.nickname = nickname;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getNickname() {
        return nickname;
    }

    public void accept(VisitorServer visitorServer) {
        visitorServer.visitMessageFromUser(this);
    }
}
