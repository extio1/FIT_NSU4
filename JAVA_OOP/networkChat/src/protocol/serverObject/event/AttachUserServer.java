package protocol.serverObject.event;

import protocol.Event;

public interface AttachUserServer extends Event {
    String getNickname();
}
