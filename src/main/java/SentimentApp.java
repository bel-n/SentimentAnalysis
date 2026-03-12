import distributed.DistributedSentimentMPI;
import sequential.WebSocketConsumer;
import parallel.WebSocketConsumerParallel;
import java.util.Scanner;

public class SentimentApp {
    public static void main(String[] args) throws Exception {
        String mode;
        Scanner scanner = new Scanner(System.in);

        if (args.length == 0) {
            System.out.println("Please specify a mode: sequential, parallel or distributed");
            mode = scanner.nextLine().toLowerCase();
        } else {
            mode = args[args.length - 1].toLowerCase();
        }

        switch (mode) {
            case "sequential":
                WebSocketConsumer.main(new String[]{});
                break;
            case "parallel":
                WebSocketConsumerParallel.main(new String[]{});
                break;
            case "distributed":
                DistributedSentimentMPI.main(args);
                return;
            default:
                System.out.println("Unknown mode");
                return;
        }


        System.out.println(">>> Sentiment Analysis is running.");

        while (true) {
            Thread.sleep(10000);
        }
    }
}