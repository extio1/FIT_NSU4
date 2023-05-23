package protocol.serverObject.event;

import protocol.Event;

public interface DetachUserServer extends Event {
    String getNickname();
}
