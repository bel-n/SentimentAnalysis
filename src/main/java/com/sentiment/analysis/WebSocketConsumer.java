
package com.sentiment.analysis;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.*;
import javax.websocket.Session;

import org.eclipse.jetty.util.log.Log;
import org.glassfish.tyrus.client.ClientManager;
import util.LogLevel;
import util.Logger;

@ClientEndpoint
public class WebSocketConsumer {

    private final ReviewDS reviewDS;

    private final Pipeline pipeline;

    private Session session = null;

    private static final String[] TOPICS = {
            "movies",
            "electronics",
            "music",
           "toys",
            "pet-supplies",
            "automotive",
            "sport"
    };

    private final AtomicBoolean subscribeFlag = new AtomicBoolean(false);


    public WebSocketConsumer() {
        this.reviewDS = new ReviewDS();
        this.pipeline = new Pipeline();
    }


    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        Logger.log("Established a connection!",LogLevel.Success);
        Logger.log("Session ID: " + session.getId(), LogLevel.Update);

    }



    //storing the messages in the data structure
    @OnMessage
    public void onMessage(String message,Session session) {
        try{
       //  Logger.log("Message received" +message, LogLevel.Update);
            //check server
            if (!subscribeFlag.get()){
                Logger.log("Sever ready. Subscribing to topics...",LogLevel.SubscriptionUpdate);
                subscribeFlag.set(true);
                subscribeToTopics();

            }else if(subscribeFlag.get()){
                reviewDS.addReview(message);
                Logger.log("Message received." + message,LogLevel.Update);
                reviewDS.processReviewsSequentially(pipeline);
            }
        }catch(Exception e){
            Logger.log("Error receiving and processing a message:",LogLevel.Error);
            e.printStackTrace();
        }

    }

    private void subscribeToTopics(){
        for(String topic: TOPICS){
            try{
                String subscribeMessage = "topic:" + topic;
                Logger.log("Subscribing to topic: " + topic,LogLevel.SubscriptionUpdate);

                if(session != null && session.isOpen()){
                    session.getBasicRemote().sendText(subscribeMessage);
                    Logger.log("Subscribed to topic: " + topic,LogLevel.SuccessfulSubscription);
                }else{
                    Logger.log("Session not open. Unable to subscribe to topic:" + topic,LogLevel.Error);
                }

            }catch (IOException e){
                Logger.log("Error subscribing to topic:" + topic,LogLevel.Error);
                e.printStackTrace();
            }

        }
    }

    @OnClose
    public void onClose(Session session) {
        Logger.log("Closed a connection!",LogLevel.Error);

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.log("WebSocket error occured:" + throwable.getMessage(), LogLevel.Error);

        throwable.printStackTrace();
    }
    public static void main(String[] args) {
        try {
            URI uri = new URI("wss://prog3.student.famnit.upr.si/sentiment");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            Logger.log("Connecting to WebSocket server...",LogLevel.Info);
            container.connectToServer(new WebSocketConsumer(), uri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
