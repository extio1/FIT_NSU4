package protocol.visitor;

import protocol.clientObject.request.DetachUserReq;

import protocol.clientObject.request.ListUserReq;
import protocol.clientObject.request.MessageUserReq;
import protocol.clientObject.request.RegisterUserReq;

import protocol.serverObject.ServerObjFabric;
import protocol.serverObject.event.MessageFromServerEv;
import protocol.serverObject.event.MessageFromUserEv;
import protocol.serverObject.response.SuccessAnswerServerResp;
import server.ClientHandler;
import server.Server;

import java.util.Objects;

public class VisitorClient {
    Server server;
    ClientHandler handler;
    ServerObjFabric fabric = new ServerObjFabric();

    public VisitorClient(ClientHandler handler, Server server){
        this.server = server;
        this.handler = handler;
    }
    
    public void visitMessageRequest(MessageUserReq r) {
        String name = handler.getContext().getUserName();
        if(name != null) {
            String msg = r.getMessage();
            if(msg.length() > 0) {
                MessageFromUserEv mess = fabric.makeServerMessageFromUserEvent(name, msg);
                server.getContext().addMsg(mess.getDate(), mess.getNickname(), mess.getMessage());
                server.doBroadcast(mess);
            }

            handler.send(fabric.makeSuccessAnswer(r.getId()));
        } else {
            handler.send(fabric.makeErrorAnswer(r.getId(), "Not registered"));
        }
    }
    public void visitListUserRequest(ListUserReq r) {
        if(handler.getContext().getUserName() != null) {
            if (server.getContext().getUserList() == null) {
                handler.send(fabric.makeErrorAnswer(r.getId(), "User list doesn't exist"));
            } else {
                SuccessAnswerServerResp response = fabric.makeSuccessAnswer(r.getId());
                response.setUserList(server.getContext().getUserList());
                handler.send(response);
            }
        } else {
            handler.send(fabric.makeErrorAnswer(r.getId(), "Not registered"));
        }
    }
    public void visitRegisterUserRequest(RegisterUserReq r) {
        String name = r.getName();
        if (name != null && name.length() >= 3 && name.length() <= 20) {
            if(!Objects.equals(handler.getContext().getUserName(), name))
                handler.getContext().setUserName(name);

            if(!server.getContext().getUserList().contains(name)) {
                server.getContext().addUser(name);
            } else {
                handler.send(fabric.makeErrorAnswer(r.getId(), "User with the same nickname already exists"));
                return;
            }

            MessageFromServerEv mess  = fabric.makeMessageFromServer(name+" join");
            server.getContext().addMsgServer(mess.getDate(), mess.getMessage());

            SuccessAnswerServerResp response = fabric.makeSuccessAnswer(r.getId());
            response.setCharHistory(server.getContext().getChatHistory(server.getLastMessagesN()));
            response.setUserName(name);

            server.doBroadcast(mess);
            handler.send(response);
        } else {
            handler.send(fabric.makeErrorAnswer(r.getId(), "Wrong name format!"));
        }
    }
    public void visitDetachUserRequest(DetachUserReq r) {
        String name = handler.getContext().getUserName();
        if(name != null) {
            server.getContext().removeUser(name);

            MessageFromServerEv mess = fabric.makeMessageFromServer(name+" left");
            server.getContext().addMsgServer(mess.getDate(), mess.getMessage());

            server.doBroadcast(mess);

            handler.send(fabric.makeSuccessAnswer(r.getId()));
            handler.shutdown();
        } else {
            handler.send(fabric.makeErrorAnswer(r.getId(), "Not registered"));
        }
    }
}
