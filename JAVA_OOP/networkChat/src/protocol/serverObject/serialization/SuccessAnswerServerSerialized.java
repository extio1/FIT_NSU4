package protocol.serverObject.serialization;

import protocol.serverObject.response.SuccessAnswerServer;

public class SuccessAnswerServerSerialized implements SuccessAnswerServer {
    private final long succeedRequestId;

    public SuccessAnswerServerSerialized(long id){
        succeedRequestId = id;
    }

    @Override
    public long getSucceedRequestId(){
        return succeedRequestId;
    }

    @Override
    public void getResponseData() {

    }
}
