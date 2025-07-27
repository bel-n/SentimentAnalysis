package parallel;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.*;
import javax.websocket.Session;

import util.LogLevel;
import util.Logger;

import parallel.ParallelReviewDS;

@ClientEndpoint
public class WebSocketConsumerParallel {

    private final ParallelReviewDS parallelReviewDS;

    private Session session = null;

    private static final String[] TOPICS_PARALLEL = {  //i would parallelize this
            //in the case of having really high number of topics to subscribe to
            "movies",
            "electronics",
            "music",
            "toys",
            "pet-supplies",
            "automotive",
            "sport"
    };

    private final AtomicBoolean subscribeFlag = new AtomicBoolean(false);

    public WebSocketConsumerParallel() {
        this.parallelReviewDS = new ParallelReviewDS();

    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        Logger.log("Established a connection!", LogLevel.Success);
        Logger.log("Session ID: " + session.getId(), LogLevel.Update);
        Logger.log("Subscribing to topics...", LogLevel.SubscriptionUpdate);
        subscribeToTopics();
    }

    //OnMessage means we will be storing the messages in the thread pool once a message is received
    @OnMessage
    public void onMessage(String message) {
        /*if(message.startsWith("ERROR")){
            System.err.println("Server error"+message);
        }*/
        parallelReviewDS.handleInput(message);


    }


    private void subscribeToTopics() {
        for (String topic : TOPICS_PARALLEL) {
            try {
               String subscribeMessage = "topic:" + topic;
               session.getBasicRemote().sendText(subscribeMessage);
               Logger.log("Subscribed to topic: " + topic, LogLevel.Update);
            } catch (IOException e) {
                Logger.log("Error subscribing to topic: " + topic, LogLevel.Error);
                e.printStackTrace();
            }
        }
    }



    private void reconnect() {
        System.out.println("Attempting to reconnect...");
        try {
            Thread.sleep(5000); // Wait before reconnecting
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("wss://prog3.student.famnit.upr.si/sentiment"));
        } catch (Exception e) {
            System.err.println("Reconnect failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        URI uri = new URI("wss://prog3.student.famnit.upr.si/sentiment");
        WebSocketConsumerParallel consumer = new WebSocketConsumerParallel();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.log("Shutting down ...", LogLevel.Update);
            consumer.parallelReviewDS.shutdown();
        }));

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(consumer, uri);
    }
}
