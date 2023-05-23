package protocol.serverObject.event;

import protocol.Event;
import protocol.ObjectServer;

public interface MessageFromUser extends Event {
    String getMessage();
    String getNickname();
}
