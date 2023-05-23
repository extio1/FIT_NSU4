package protocol.userObject.serialization.visitor;

import protocol.ObjectServer;
import protocol.serverObject.ServerMessageFabric;
import protocol.serverObject.event.MessageFromServer;
import protocol.serverObject.event.MessageFromUser;
import protocol.serverObject.response.SuccessAnswerServer;
import protocol.serverObject.serialization.fabric.ServerSerializedFabric;
import protocol.userObject.VisitorClient;
import protocol.userObject.request.DetachUser;
import protocol.userObject.request.ListUser;
import protocol.userObject.request.MessageUser;
import protocol.userObject.request.RegisterUser;
import server.ClientHandler;
import server.Server;

public class VisitorClientSerialized implements VisitorClient {
    Server server;
    ClientHandler handler;
    ServerMessageFabric fabric = new ServerSerializedFabric();

    public VisitorClientSerialized(ClientHandler handler, Server server){
        this.server = server;
        this.handler = handler;
    }
    @Override
    public void visitMessageRequest(MessageUser r) {
        if(handler.getContext().getUserName() != null) {
            String name = handler.getContext().getUserName();
            String msg = r.getMessage();
            if(msg.length() > 0) {
                MessageFromUser mess = fabric.makeServerMessageFromUserEvent(name, msg);
                server.getContext().addMsg(mess.getDate(), mess.getNickname(), mess.getMessage());

                server.doBroadcast((ObjectServer) mess);
            }

            handler.send((ObjectServer) fabric.makeSuccessAnswer(r.getId()));
        } else {
            handler.send((ObjectServer) fabric.makeErrorAnswer(r.getId(), "Not registered"));
        }
    }

    @Override
    public void visitListUserRequest(ListUser r) {
        if(handler.getContext().getUserName() != null)
            if(server.getContext().getUserList() == null){
                handler.send((ObjectServer) fabric.makeErrorAnswer(r.getId(), "User list doesn't exist"));
            } else {
                SuccessAnswerServer response = fabric.makeSuccessAnswer(r.getId());
                response.setUserList(server.getContext().getUserList());
                handler.send((ObjectServer) response);
            }
        else
            handler.send((ObjectServer) fabric.makeErrorAnswer(r.getId(), "Not registered"));
    }

    @Override
    public void visitRegisterUserRequest(RegisterUser r) {
        String name = r.getName();
        String nameNow = handler.getContext().getUserName();
        if (name != null && name.length() >= 3 && name.length() <= 20 && nameNow==null) {
            handler.getContext().setUserName(name);
            server.getContext().addUser(handler.getContext().getUserName());

            MessageFromServer mess  = fabric.makeMessageFromServer(handler.getContext().getUserName()+" join");
            server.doBroadcast((ObjectServer) mess);
            server.getContext().addMsg(mess.getDate(), "!  SERVER  !", mess.getMessage());

            SuccessAnswerServer response = fabric.makeSuccessAnswer(r.getId());
            response.setCharHistory(server.getContext().getChatHistory(server.getLastMessagesN()));
            response.setUserName(handler.getContext().getUserName());
            handler.send((ObjectServer) response);
        } else {
            handler.send((ObjectServer) fabric.makeErrorAnswer(r.getId(), "Wrong name format!"));
        }
    }

    @Override
    public void visitDetachUserRequest(DetachUser r) {
        if(handler.getContext().getUserName() != null) {
            server.getContext().removeUser(handler.getContext().getUserName());

            String message;
            if(!r.isTimeout())
                message = handler.getContext().getUserName()+" left";
            else
                message = handler.getContext().getUserName()+" left (TIMEOUT)";
            MessageFromServer mess  = fabric.makeMessageFromServer(message);

            server.doBroadcast((ObjectServer) mess);
            server.getContext().addMsg(mess.getDate(), "!  SERVER  !", mess.getMessage());

            handler.send((ObjectServer) fabric.makeSuccessAnswer(r.getId()));
            handler.shutdown();
        } else {
            handler.send((ObjectServer) fabric.makeErrorAnswer(r.getId(), "Not registered"));
        }
    }
}
