package client.clientImpls.clientSerialize;

import client.ClientSessionData;
import protocol.ObjectServer;
import protocol.Response;
import protocol.serverObject.DetachedUserServer;
import protocol.serverObject.MessageFromUser;
import protocol.serverObject.NewUserServer;

public class MessageHandler {

    public void handleServerObject(ObjectServer objectServer, ClientSessionData data) throws Exception {
        if(objectServer instanceof Response r){

        } else if(objectServer instanceof DetachedUserServer) {

        } else if(objectServer instanceof NewUserServer) {

        } else if(objectServer instanceof MessageFromUser) {

        } else {
            throw new Exception();
        }

    }

}
