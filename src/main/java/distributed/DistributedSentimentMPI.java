package distributed;

import mpi.*;
import util.LogLevel;
import util.Logger;

public class DistributedSentimentMPI {
     public static void main(String[] args){
         MPI.Init(args);

         int rank = MPI.COMM_WORLD.Rank();
         int size = MPI.COMM_WORLD.Size();

         //dispatcher + worker logic

         Logger.log("Process" + rank + " of size: " + size +  "started", LogLevel.Update);

         MPI.Finalize();

     }

     private static void startDispatcher(){
         //should open connection to the server and subscribe
         //review comes and like in the other parts gets put in a queue
         //here the dispatcher will get the reviews and distribute them among processes
     }

    private static void startWorkersMPI(){
         //every process should have its own pipeline
        //the worker sends back its work to dispatcher and goes back to waiting for new review
    }

    private static void DispatcherWebSocketClient(){
         //will manage the connection to the server and subscribe to the same topics as in the other parts
    }


}
