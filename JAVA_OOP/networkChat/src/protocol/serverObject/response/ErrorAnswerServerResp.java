package protocol.serverObject.response;

import protocol.ObjectServer;;
import protocol.Response;
import protocol.visitor.VisitorServer;

public class ErrorAnswerServerResp extends ObjectServer implements Response {
    private final long id;
    private final String reason;

    public ErrorAnswerServerResp(String date, long id, String reason){
        super(date);
        this.id = id;
        this.reason = reason;
    }
    
    public long getRequestId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public void accept(VisitorServer visitorServer) {
        visitorServer.visitErrorResponse(this);
    }
}
