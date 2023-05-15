package protocol.serverObject.serialization;

import protocol.Response;
import protocol.serverObject.response.ErrorAnswerServer;

public class ErrorAnswerServerSerialized implements ErrorAnswerServer {
    @Override
    public long getSucceedRequestId() {
        return 0;
    }

    @Override
    public void getResponseData() {

    }

    @Override
    public String getReason() {
        return null;
    }
}
