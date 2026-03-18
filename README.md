# Sentiment Analysis 

## Project Overview

In vast marketplaces like **Amazon**, customer reviews are an invaluable source of feedback for consumers, manufacturers, and the platform itself. However, as the volume of reviews grows, analyzing them in real time becomes computationally demanding.

This project implements a **high performance sentiment analysis tool** that consumes live review streams from a **WebSocket based API** and classifies their sentiments using the **Stanford CoreNLP** library.

The system can be executed in **sequential**, **parallel**, or **distributed** mode, allowing performance comparison between single threaded, multi-threaded, and multi-process execution models.

---

## Features

- **WebSocket Integration:** Connects to a live review stream at a server provided by our faculty.
- **Real-Time Processing:** Subscribes to multiple product topics (movies, electronics, music, toys, pet-supplies, automotive, sport).
- **Sentiment Analysis:** Uses [Stanford CoreNLP](https://nlp.stanford.edu/software/corenlp.shtml) to determine the sentiment of each review.
- **Three Execution Modes:**
  - *Sequential:* Processes one review at a time in a single thread.
  - *Parallel:* Uses a pipeline pool and thread pool for concurrent processing.
  - *Distributed:* Uses MPI (MPJ Express) to distribute work across multiple JVM processes.
- **Hardware Adaptive:** Thread count and pipeline count adjust automatically based on available CPU cores and memory.
- **Performance Logging:** Measures reviews per second and logs throughput after every review.

---

## Architecture

### WebSocket Consumers
- `sequential.WebSocketConsumer` — connects and processes reviews sequentially.
- `parallel.WebSocketConsumerParallel` — connects and delegates to the parallel data structure.
- `distributed.WebSocketConsumerDistributed` — connects and fills a `BlockingQueue` for the MPI dispatcher.

### Data Structures
- **Sequential (`ReviewDS`)** — stores reviews in a list and processes them one by one.
- **Parallel (`ParallelReviewDS3`)** — uses a `LinkedList` queue with `wait()`/`notify()` and a shared `LinkedBlockingQueue` pipeline pool. Workers borrow a pipeline, analyze, and return it via `finally`.
- **Distributed (`DistributedSentimentMPI`)** — rank 0 dispatches reviews to worker processes round-robin via MPI. Each worker has its own private pipeline.

### NLP Pipeline
- `Pipeline` (sequential) and `PipelineParallel` (parallel/distributed) wrap Stanford CoreNLP with annotators: `tokenize → ssplit → pos → parse → sentiment`.
- A **majority vote** across all sentences in a review produces the final sentiment label.

### Utilities
- `util.CleanReviews` — extracts `reviewText`, topic, reviewer name, and ASIN from the raw double encoded JSON.
- `util.Logger` / `util.LogLevel` — colored console logging.

---

## Requirements

- **Java 23+** (class files compiled with Java 23 — will not run on Java 8, 11, or 17)
- **Maven** (bundled with IntelliJ — no separate install needed)
- **MPJ Express 0.44** — distributed mode only ([mpjexpress.org](http://mpjexpress.org))

---

## Running the Project

Open the project in IntelliJ and let Maven resolve all dependencies automatically via `pom.xml`. Then run `SentimentApp` with one of the following mode arguments.
### Sequential
```bash
java -Xmx6g -jar SentimentAnalysis2-1.0-SNAPSHOT-jar-with-dependencies.jar sequential
```

### Parallel
```bash
java -Xmx6g -jar SentimentAnalysis2-1.0-SNAPSHOT-jar-with-dependencies.jar parallel
```

### Distributed

Distributed mode requires MPJ Express to be installed and configured:

1. Download and install MPJ Express 0.44 from [mpjexpress.org](http://mpjexpress.org)
2. Set the `MPJ_HOME` environment variable to your install directory
3. Add `%MPJ_HOME%\bin` to your system PATH
4. Update the `systemPath` in `pom.xml` to match your local install:

<systemPath>C:/mpjexpress/lib/mpj.jar</systemPath>

**If `MPJ_HOME` is set:**
```bash mpjrun.bat -np 8 -Xmx6g -cp "SentimentAnalysis2-1.0-SNAPSHOT-jar-with-dependencies.jar; SentimentApp distributed-mpi

```

**If `MPJ_HOME` is NOT set, use the full path:**
```bash
mpjrun.bat -np 8 -Xmx6g -cp "SentimentAnalysis2-1.0-SNAPSHOT-jar-with-dependencies.jar;C:\mpjexpress\lib\mpj.jar" SentimentApp distributed-mpi
```

To check whether `MPJ_HOME` is set:
```cmd
echo %MPJ_HOME%
```
If it prints a path like `C:\mpjexpress` it is set. If it prints `%MPJ_HOME%` literally it is not set.

> **Note:** Running `SentimentApp distributed` without `mpjrun.bat` will just print these setup instructions — the actual distributed launch must go through `mpjrun.bat` since MPJ needs to spawn the JVM processes itself.

---

## Building the Fat JAR
```bash
mvn clean package
```

This produces `target/SentimentAnalysis2-1.0-SNAPSHOT-jar-with-dependencies.jar` with all dependencies bundled except `mpj.jar`, which must be installed separately.