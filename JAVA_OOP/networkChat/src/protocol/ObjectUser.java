package protocol;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ObjectUser {
    private static final AtomicLong id = new AtomicLong(0);
    private final long myId;

    protected ObjectUser(){
        myId = id.getAndIncrement();
    }

    public long getId(){
        return myId;
    }
}
