package protocol;

import protocol.visitor.VisitorClient;
import protocol.visitor.VisitorServer;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ObjectUser implements Serializable {
    private static final AtomicLong id = new AtomicLong(0);
    private final long myId;

    protected ObjectUser(){
        myId = id.getAndIncrement();
    }
    public long getId(){
        return myId;
    }
    public abstract void accept(VisitorClient visitorClient);
}
