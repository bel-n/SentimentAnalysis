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
                System.out.println("========================================");
                System.out.println("  DISTRIBUTED MODE - Manual Launch Required");
                System.out.println("========================================");
                System.out.println("MPJ Express requires a special launcher.");
                System.out.println("First, make sure you are inside the root folder of the project; not in the distributed folder");
                System.out.println("Please run the following command instead:");
                System.out.println();
                System.out.println("  mpjrun.bat -np <num_processes> -Xmx6g -cp [classpath - make sure it is closed under quotation marks] SentimentApp distributed-mpi ");
                System.out.println();
                System.out.println("  <num_processes>: minimum 2, recommended 4-5");
                System.out.println("  Example: mpjrun.bat -np 4 ...");
                System.out.println("========================================");
                return;

            case "distributed-mpi":
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