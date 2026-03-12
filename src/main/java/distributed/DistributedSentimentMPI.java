package distributed;

import mpi.*;
import parallel.PipelineParallel;
import util.LogLevel;
import util.Logger;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DistributedSentimentMPI {
     public static void main(String[] args) throws Exception {
         MPI.Init(args);

         int rank = MPI.COMM_WORLD.Rank();
         int size = MPI.COMM_WORLD.Size();

         //dispatcher + worker logic

         Logger.log("Process" + rank + " of size: " + size +  "started", LogLevel.Update);

         if(rank == 0){
             startDispatcher(size);
         }else{
             startWorkersMPI(rank);
         }

         MPI.Finalize();

     }

     private static void startDispatcher(int size) throws Exception {

         BlockingQueue<String> reviewQueue = new LinkedBlockingQueue<>();

         MPI.COMM_WORLD.Barrier();
         Logger.log("All workers are ready, connection can be established and dispatch can start...", LogLevel.Update);

         WebSocketContainer container = ContainerProvider.getWebSocketContainer();
         container.connectToServer(
                 new WebSocketConsumerDistributed(reviewQueue),
                 new URI("wss://prog3.student.famnit.upr.si/sentiment")
         );




         Logger.log("Entering dispatch loop...", LogLevel.Update);


         int nextWorker = 1;
         int totalProcessed = 0;
         long startTime = System.currentTimeMillis();

         while(true){
             Logger.log("Waiting for review from queue...", LogLevel.Update);

             String review = reviewQueue.take();
             Logger.log("Got review, sending to worker " + nextWorker, LogLevel.Update); // and this

             if(review.length() > 300) {
                 review = review.substring(0, 300);
             }

             byte[] reviewBytes = review.getBytes();
             int [] length = {reviewBytes.length};

             MPI.COMM_WORLD.Send(length, 0,1,MPI.INT, nextWorker,0);
             MPI.COMM_WORLD.Send(reviewBytes,0, reviewBytes.length, MPI.BYTE, nextWorker,1);

             byte[] resultBytes = new byte[32];
             MPI.COMM_WORLD.Recv(resultBytes,0,resultBytes.length, MPI.BYTE, nextWorker,2);
            String sentiment = new String(resultBytes).trim();

             totalProcessed++;

             long elapsed = System.currentTimeMillis() - startTime;
             double throughput = totalProcessed / (elapsed / 1000);

             Logger.log("Review: " + review
                     + "\nSentiment: " + sentiment
                     + "\nThroughput: " + String.format("%.2f", throughput) + " reviews/sec"
                     + " | Total: " + totalProcessed, LogLevel.Success);

             nextWorker++;
             if (nextWorker >= size){
                 nextWorker = 1;
             }
         }


         //should open connection to the server and subscribe
         //review comes and like in the other parts gets put in a queue
         //here the dispatcher will get the reviews and distribute them among processes
     }

    private static void startWorkersMPI(int rank){
        PipelineParallel pipeline = new PipelineParallel();
        Logger.log("Worker" + rank + " pipeline ready", LogLevel.Update);

        MPI.COMM_WORLD.Barrier();

        while (true){
            int[] length = new int[1];
            MPI.COMM_WORLD.Recv(length,0,1,MPI.INT, 0, 0);

            if(length[0] == -1){
                Logger.log("Worker" + rank +" was signaled for shutdown", LogLevel.Update);
                break;

            }
            byte[] reviewBytes = new byte[length[0]];
            MPI.COMM_WORLD.Recv(reviewBytes, 0, reviewBytes.length, MPI.BYTE, 0, 1);
            String review = new String(reviewBytes);

            String sentiment = pipeline.analyzeSentiment(review);

            byte[] resultBytes = new byte[32];
            byte[] sentimentBytes = sentiment.getBytes();
            System.arraycopy(sentimentBytes, 0, resultBytes, 0, sentimentBytes.length);
            MPI.COMM_WORLD.Send(resultBytes,0,resultBytes.length, MPI.BYTE, 0,2);
        }

         //every process should have its own pipeline
        //the worker sends back its work to dispatcher and goes back to waiting for new review
    }


}
