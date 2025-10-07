# 🧠 Amazon Review Sentiment Analysis Tool

## 📋 Project Overview
In vast marketplaces like **Amazon**, customer reviews are an invaluable source of feedback for consumers, manufacturers, and the platform itself. However, as the volume of reviews grows, analyzing them in real-time becomes computationally demanding.

This project implements a **high-performance sentiment analysis tool** that consumes live review streams from a **WebSocket-based API** and classifies their sentiments using the **Stanford CoreNLP** library.

The system can be executed in **sequential** or **parallel** mode, allowing performance comparison between single-threaded and concurrent execution models.

---

## ⚙️ Features
- **WebSocket Integration:** Connects to a live review stream at  
  `wss://prog3.student.famnit.upr.si/sentiment`
- **Real-Time Processing:** Subscribes to multiple product topics (movies, electronics, music, toys, etc.)
- **Sentiment Analysis:** Uses [Stanford CoreNLP](https://nlp.stanford.edu/software/corenlp.shtml) to determine the sentiment of each review.
- **Sequential & Parallel Modes:**
  - *Sequential Mode:* Processes one review at a time in a single thread.
  - *Parallel Mode:* Uses a **thread pool** to process reviews concurrently for higher throughput.
- **Adaptive Thread Pool:** Dynamically adjusts to available hardware and workload.
- **Performance Logging:** Measures processed reviews per second and logs system updates.

---

## 🧩 Architecture

### 🕸️ WebSocket Consumers
- `sequential.WebSocketConsumer`: Connects to the WebSocket server and processes reviews sequentially.
- `parallel.WebSocketConsumerParallel`: Connects to the same server and processes reviews concurrently using threads.

### 📦 Data Structures
- **Sequential (`ReviewDS`)**: Stores reviews in a list and processes them one by one.
- **Parallel (`ParallelReviewDS`)**: Uses a `BlockingQueue` and `ThreadPoolExecutor` to distribute sentiment analysis tasks across worker threads.

### 🔍 NLP Pipeline
- **Sequential (`Pipeline`)** and **Parallel (`PipelineParallel`)** wrap the Stanford CoreNLP sentiment analysis engine.
- The parallel version is configured as thread-safe to allow concurrent access.

---

## 🧵 Execution Modes

You can choose between **Sequential** and **Parallel** execution modes through the `SentimentApp` main class.

### Run Sequential Mode
```bash
java -cp target/SentimentAnalysis.jar SentimentApp sequential
