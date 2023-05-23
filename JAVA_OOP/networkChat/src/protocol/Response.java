package protocol;

import protocol.serverObject.VisitorServer;

import java.io.Serializable;
import java.util.List;

public interface Response extends Serializable {
    long getRequestId();
    void accept(VisitorServer v);
}
