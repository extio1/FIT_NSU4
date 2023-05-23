package server.logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerServer {
    private static Logger logger;

    public LoggerServer(){
        logger = Logger.getLogger("Server.log");
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private volatile String logString;
    public String getLastEventLog(){
        String r = logString;
        logString = "";
        return r;
    }
    public boolean isNewStringAvailable(){
        return logString.length() > 0;
    }

    public void makeLog(Level level, String string){
        logString = string;
        logger.log(level, string);
        synchronized (this) {
            this.notifyAll();
        }
    }
}
