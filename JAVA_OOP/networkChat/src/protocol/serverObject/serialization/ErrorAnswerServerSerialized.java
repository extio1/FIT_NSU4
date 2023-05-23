package protocol.serverObject.serialization;

import protocol.ObjectServer;
import protocol.Response;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.response.ErrorAnswerServer;

import java.util.List;

public class ErrorAnswerServerSerialized extends ObjectServer implements ErrorAnswerServer {
    private final long id;
    private final String reason;

    public ErrorAnswerServerSerialized(String date, long id, String reason){
        super(date);
        this.id = id;
        this.reason = reason;
    }
    @Override
    public long getRequestId() {
        return id;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public void accept(VisitorServer visitorServer) {
        visitorServer.visitResponse(this);
    }
}
