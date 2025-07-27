import  sequential.WebSocketConsumer;
import parallel.WebSocketConsumerParallel;

import java.util.Scanner;

public class SentimentApp {
    public static void main(String[] args) throws Exception {
        String mode;
        if (args.length == 0) {
            System.out.println("Please specify a mode: " + " sequential, parallel or distributed");
            Scanner scanner = new Scanner(System.in);
            mode = scanner.nextLine().toLowerCase();
            scanner.close();
        }else {
            mode = args[0].toLowerCase();
        }
        switch (mode){
            case "sequential":
                WebSocketConsumer.main(new String[]{});
                break;
                case "parallel":
                    WebSocketConsumerParallel.main(new String[]{});
                    break;
            case "distributed":
                System.out.println("Distributed mode not implemented yet");
                break;
            default:
                System.out.println("Unknown mode");
        }
    }
}
