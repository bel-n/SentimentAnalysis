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
}
