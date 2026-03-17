package sequential;

import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.*;
import util.LogLevel;
import util.Logger;

@ClientEndpoint
public class WebSocketConsumer {

    private final ReviewDS reviewDS;
    private final Pipeline pipeline;
    private Session session = null;
    private final AtomicBoolean subscribeFlag = new AtomicBoolean(false);

    private static final String[] TOPICS = {
            "movies", "electronics", "music", "toys", "pet-supplies", "automotive", "sport"
    };

    public WebSocketConsumer() {
        this.reviewDS = new ReviewDS();
        this.pipeline = new Pipeline();
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        session.setMaxIdleTimeout(0); // Set to 0 to disable client-side timeout
        Logger.log("Established a connection!", LogLevel.Success);
        Logger.log("Session ID: " + session.getId(), LogLevel.Update);

        // Some servers close the connection if you don't send something immediately
        // or if you send it TOO fast before the handshake state is updated.
        try {
            Thread.sleep(100);
            if(session.isOpen()) {
                session.getBasicRemote().sendText("ready"); // Tell the server you are ready
                Logger.log("Sent 'ready' signal to server.", LogLevel.Info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopics() {
        for (String topic : TOPICS) {
            try {
                if (session != null && session.isOpen()) {
                    String subscribeMessage = "topic:" + topic;
                    session.getBasicRemote().sendText(subscribeMessage);
                    Logger.log("Subscribed to: " + topic, LogLevel.Successful_Subscription);
                    // Tiny pause between messages to avoid flooding the socket buffer
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                Logger.log("Error subscribing to " + topic, LogLevel.Error);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            if (!subscribeFlag.get()) {
                Logger.log("Server ready. Subscribing to topics...", LogLevel.Subscription_Update);
                subscribeFlag.set(true);
                subscribeToTopics();
            } else {
                // Sequential processing:
                // We add it to the list and process it immediately.
                reviewDS.addReview(message);
                Logger.log("Message received. Processing...", LogLevel.Update);
                reviewDS.processReviewsSequentially(pipeline);
            }
        } catch (Exception e) {
            Logger.log("Error processing message: " + e.getMessage(), LogLevel.Error);
            e.printStackTrace();
        }
    }



    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Logger.log("Connection closed: " + reason.getReasonPhrase(), LogLevel.Error);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.log("WebSocket error: " + throwable.getMessage(), LogLevel.Error);
        throwable.printStackTrace();
    }

    public static void main(String[] args) {
        try {
            URI uri = new URI("wss://prog3.student.famnit.upr.si/sentiment");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            Logger.log("Connecting to WebSocket server...", LogLevel.Info);

            WebSocketConsumer consumer = new WebSocketConsumer();
            Session session = container.connectToServer(consumer, uri);

            Logger.log("System online. Press Enter in this console to stop the app.", LogLevel.Info);
            new Scanner(System.in).nextLine();

            if (session.isOpen()) {
                session.close();
            }

        } catch (Exception e) {
            Logger.log("Critical Error: " + e.getMessage(), LogLevel.Error);
            e.printStackTrace();
        }
    }
}