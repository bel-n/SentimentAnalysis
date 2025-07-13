package parallel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.*;
import javax.websocket.Session;

import util.LogLevel;
import util.Logger;

import parallel.ParallelReviewDS;
import sequential.Pipeline;

@ClientEndpoint
public class WebSocketConsumerParallel {

    private final ParallelReviewDS parallelReviewDS;

    private final Pipeline pipeline;

    private Session session = null;

    private static final String[] TOPICS = {  //i would parallelize this
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
        this.pipeline = new Pipeline();

    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        Logger.log("Established a connection!", LogLevel.Success);
        Logger.log("Session ID: " + session.getId(), LogLevel.Update);
    }

    //OnMessage means we will be storing the messages in the thread pool once a message is received
    @OnMessage
    public void onMessage(String message) {
        if(!subscribeFlag.get()){
            subscribeFlag.set(true);
            subscribeToTopics();
        }else {
            parallelReviewDS.processReview(message);
        }

    }


    private void subscribeToTopics() {
        for (String topic : TOPICS) {
            try {
                session.getBasicRemote().sendText("Topic" + topic);
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        URI uri = new URI("wss://prog3.student.famnit.upr.si/sentiment");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(new WebSocketConsumerParallel(), uri);
    }
}
