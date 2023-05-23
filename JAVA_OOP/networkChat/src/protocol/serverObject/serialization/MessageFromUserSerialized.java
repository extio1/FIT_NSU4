package protocol.serverObject.serialization;

import protocol.ObjectServer;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.event.MessageFromUser;

public class MessageFromUserSerialized extends ObjectServer implements MessageFromUser {
    private final String nickname;
    private final String message;

    public MessageFromUserSerialized(String date, String nickname, String message){
        super(date);
        this.nickname = nickname;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitMessageFromUser(this);
    }
}
