package distributed;

import javax.websocket.*;
import java.net.URI;

import util.Logger;
import util.LogLevel;

@ClientEndpoint
public class WebSocketConsumerDistributed {

    private Session session;

    private static final String[] TOPICS = {
            "movies", "electronics", "music", "toys", "pet-supplies", "automotive", "sport"
    };

    @OnOpen
    public void onOpen(Session session) {}

    @OnClose
    public void onClose(Session session) {}



}
