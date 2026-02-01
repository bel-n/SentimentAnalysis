import sequential.WebSocketConsumer;
import parallel.WebSocketConsumerParallel;
import java.util.Scanner;

public class SentimentApp {
    public static void main(String[] args) throws Exception {
        String mode;
        // Keep the scanner open for the duration of the app
        Scanner scanner = new Scanner(System.in);

        if (args.length == 0) {
            System.out.println("Please specify a mode: sequential, parallel or distributed");
            mode = scanner.nextLine().toLowerCase();
        } else {
            mode = args[0].toLowerCase();
        }

        switch (mode) {
            case "sequential":
                WebSocketConsumer.main(new String[]{});
                break;
            case "parallel":
                WebSocketConsumerParallel.main(new String[]{});
                break;
            case "distributed":
                System.out.println("Distributed mode not implemented yet");
                return; // Exit early since nothing to wait for
            default:
                System.out.println("Unknown mode");
                return;
        }

        // IMPORTANT: Prevent SentimentApp from exiting.
        // This keeps the WebSocket threads alive.
        System.out.println(">>> Sentiment Analysis is running.");

        // This loop keeps the main thread occupied
        while (true) {
            Thread.sleep(10000);
        }
    }
}