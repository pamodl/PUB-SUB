# PUB-SUB

Lightweight publish–subscribe middleware implemented in Java.

## Overview
This project implements a simple pub-sub broker with topic-based filtering, example publisher and subscriber clients, and concurrent client handling.

## Features
- Topic/subject-based subscriptions and filtering
- Concurrent handling of multiple publishers and subscribers
- Example publisher and subscriber clients
- Unit tests for core components

## Tech stack
- Java 11+
- TCP sockets
- Threading / synchronization
- Maven (or Gradle)
- JUnit

## Quick start
1. Build:
```bash
mvn clean package
```
2. Run broker:
```bash
java -jar target/pubsub-broker.jar
```
3. Run an example publisher:
```bash
java -jar target/pubsub-publisher.jar --topic="sports" --message="Goal!"
```
4. Run an example subscriber:
```bash
java -jar target/pubsub-subscriber.jar --topic="sports"
```

## Architecture
- Broker: accepts publisher/subscriber connections, maintains topic subscriptions, routes messages.
- Publisher: connects to broker, sends messages tagged with a topic.
- Subscriber: registers interest in topics and receives matching messages.

## Usage examples
(Include sample logs or screenshots here)

## Tests & Benchmark
- Run unit tests:
```bash
mvn test
```
- (Optional) Run benchmark script to measure throughput and concurrent client handling.

## What I learned
- Implementing networked services using sockets
- Managing concurrency and thread-safety in Java
- Designing message routing and filtering logic

## License
(Add a license file)
