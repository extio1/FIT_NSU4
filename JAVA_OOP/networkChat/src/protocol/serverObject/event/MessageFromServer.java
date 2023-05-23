package protocol.serverObject.event;

import protocol.Event;

public interface MessageFromServer extends Event {
    public String getDate();
    public String getMessage();
}
