package com.sentiment.analysis;

import java.io.IOException;
import java.net.URI;
import javax.websocket.*;

import org.eclipse.jetty.util.log.Log;
import util.LogLevel;
import util.Logger;

@ClientEndpoint
public class WebSocketConsumer {

    private final ReviewDS reviewDS;

    private final Pipeline pipeline;

    private static final String[] TOPICS = {
            "movies",
            "electronics",
            "music",
            "toys",
            "pet-supplies",
            "automotive",
            "sport"
    };


    public WebSocketConsumer() {
        reviewDS = new ReviewDS();
        pipeline = new Pipeline();
    }


    @OnOpen
    public void onOpen(Session session) {

        Logger.log("Established a connection!",LogLevel.Success);
        //subscribing to topics
        for (String topic : TOPICS) {
            try {
                String subscribeMessage = "{ \"action\": \"subscribe\", \"topic\": \"" + topic + "\" }";
                session.getBasicRemote().sendText(subscribeMessage);
                Logger.log("Subscribed to topic: " + topic, LogLevel.Success);

            } catch (IOException e) {
                Logger.log("Error subscribing to topic: " + topic, LogLevel.Error);
                e.printStackTrace();
            }
        }


    }



    //storing the messages in the data structure
    @OnMessage
    public void onMessage(String message) {
        try{
            reviewDS.addReview(message);
            Logger.log("Message received: " + message, LogLevel.Success);
            reviewDS.processReviewsSequentially(pipeline);
           // Logger.log("Message processed: " + message, LogLevel.Status);
        }catch(Exception e){
            Logger.log("Error receiving and processing a review : ", LogLevel.Error);
            e.printStackTrace();
        }

    }



    @OnClose
    public void onClose(Session session) {
        Logger.log("Closed a connection!",LogLevel.Error);

      /*  try{
            reviewDS.processReviews(pipeline);
        } catch (Exception e) {
            Logger.log("Error closing reviews", LogLevel.Error);
            e.printStackTrace();
        }*/

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
            //System.out.println("Connecting to Websocket Server...");
            container.connectToServer(new WebSocketConsumer(), uri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

