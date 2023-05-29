package protocol;

import protocol.visitor.VisitorServer;

import java.io.Serializable;

public interface Response extends Serializable {
    long getRequestId();
    void accept(VisitorServer v);
}
