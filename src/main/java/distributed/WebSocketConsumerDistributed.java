package distributed;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jetty.util.log.Log;
import util.Logger;
import util.LogLevel;

@ClientEndpoint
public class WebSocketConsumerDistributed {

    private Session session;
    private final BlockingQueue<String> reviewQueue;

    private static final String[] TOPICS = {
            "movies", "electronics", "music", "toys", "pet-supplies", "automotive", "sport"
    };

    public WebSocketConsumerDistributed(BlockingQueue<String> reviewQueue){
        this.reviewQueue = reviewQueue;
    }

    @OnOpen
    public void onOpen(Session session) {

        this.session = session;
        Logger.log("Established a connection!", LogLevel.Success);
        Logger.log("Session ID: " + session.getId(), LogLevel.Update);

        Logger.log("Subscribing to topics...",LogLevel.Update);

        for (String topic : TOPICS) {
            try{
                session.getBasicRemote().sendText(topic);
                Logger.log("Subscribed to: " + topic, LogLevel.Update);
            }catch (Exception e){
                Logger.log("Failed to subscribe to: " + topic, LogLevel.Error);
            }
        }
    }


    @OnMessage
    public void onMessage(String message) {
        if(!message.startsWith("{")) return;
        try{
            reviewQueue.put(message);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }




    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.log("WebSocket error: " + throwable.getMessage(), LogLevel.Error);
        throwable.printStackTrace();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Logger.log("Connection closed: " + reason.getReasonPhrase(), LogLevel.Error);
    }




}
