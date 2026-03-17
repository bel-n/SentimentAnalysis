package parallel;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.*;
import javax.websocket.Session;

import util.LogLevel;
import util.Logger;

@ClientEndpoint
public class WebSocketConsumerParallel {

    private final ParallelReviewDS3 parallelReviewDS3;

    private Session session = null;

    private static final String[] TOPICS_PARALLEL = {
            "movies",
            "electronics",
            "music",
            "toys",
            "pet-supplies",
            "automotive",
            "sport"
    };

    private final AtomicBoolean subscribeFlag = new AtomicBoolean(false);

    public WebSocketConsumerParallel() throws InterruptedException {
        this.parallelReviewDS3 = new ParallelReviewDS3();

    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        Logger.log("Established a connection!", LogLevel.Success);
        Logger.log("Session ID: " + session.getId(), LogLevel.Update);
        Logger.log("Subscribing to topics...", LogLevel.Subscription_Update);
        subscribeToTopics();
    }

    @OnMessage
    public void onMessage(String message) {

        parallelReviewDS3.handleInput(message);

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
            Thread.sleep(5000); // Wait before reconnectinga
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
            consumer.parallelReviewDS3.shutdown();
        }));

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(consumer, uri);
    }
}
