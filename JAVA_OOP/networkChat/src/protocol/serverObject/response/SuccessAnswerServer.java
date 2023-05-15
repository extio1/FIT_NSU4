package protocol.serverObject.response;

import protocol.Response;

public interface SuccessAnswerServer extends Response {
    long getSucceedRequestId();
}
