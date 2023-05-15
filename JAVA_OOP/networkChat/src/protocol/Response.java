package protocol;

public interface Response extends ObjectServer{
    long getSucceedRequestId();
    void getResponseData();
}
