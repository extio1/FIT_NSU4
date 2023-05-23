package protocol.serverObject.response;

import protocol.Response;

public interface ErrorAnswerServer extends Response {
    String getReason();
}
