
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyServerAppTask3 {

    private static final Map<String, List<PrintWriter>> topicSubscribers = 
        new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MyServerAppTask3 <PORT>");
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
            if (port <= 1024 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid PORT. Must be an integer between 1025 and 65535.");
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Pub/Sub Server (Task 3) started. Listening on port " + port + "...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (IOException e) {
                    System.err.println("Accept error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Server socket error: " + e.getMessage());
            System.exit(1);
        }
    }

    
     // Sends a message to all registered subscribers of a specific topic.
     
    private static void broadcastMessage(String topic, String message) {
        
        String formattedMessage = "[" + topic + "] " + message;
        System.out.println("[Broadcast] Topic: " + topic + " | Sending: " + message);

        List<PrintWriter> subscribers = topicSubscribers.get(topic);

        if (subscribers == null || subscribers.isEmpty()) {
            System.out.println("[Broadcast] No subscribers found for topic '" + topic + "'.");
            return;
        }

        // List to collect disconnected clients for later removal
        List<PrintWriter> deadWriters = new LinkedList<>();
        
        
        synchronized (subscribers) {
            for (PrintWriter writer : subscribers) {
                try {
                    writer.println(formattedMessage);
                } catch (Exception e) {
                    // If sending fails
                    deadWriters.add(writer);
                }
            }
        }
        
        // Remove the disconnected clients
        if (!deadWriters.isEmpty()) {
            synchronized (subscribers) {
                subscribers.removeAll(deadWriters);
                System.out.println("Cleaned up " + deadWriters.size() + " disconnected subscribers from topic '" + topic + "'.");
            }
        }
    }
    
    
     // Inner class to handle a single client connection in its own thread.
     
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private String clientType = "UNKNOWN";
        private String topic = null;
        private PrintWriter writer = null;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            List<PrintWriter> topicList = null; // Reference to the specific topic's list

            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                // 1. Client Registration: Expecting "ROLE:TOPIC"
                String registration = reader.readLine();
                if (registration == null || !registration.contains(":")) {
                    System.out.println("Error: " + clientAddress + " sent invalid registration. Disconnecting.");
                    return;
                }
                
                String[] parts = registration.trim().toUpperCase().split(":", 2);
                clientType = parts[0];
                topic = parts[1].trim();

                if (!clientType.equals("PUBLISHER") && !clientType.equals("SUBSCRIBER")) {
                    System.out.println("Error: " + clientAddress + " sent invalid role: " + clientType + ". Disconnecting.");
                    return;
                }

                System.out.println("[Registration] " + clientAddress + " registered as " + clientType + " on topic: " + topic);

                // Handle SUBSCRIBER Role 
                if (clientType.equals("SUBSCRIBER")) {
                    this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    
                    topicList = topicSubscribers.computeIfAbsent(topic, k -> Collections.synchronizedList(new LinkedList<>()));
                    
                    synchronized (topicList) {
                        topicList.add(this.writer);
                    }
                    System.out.println("Total subscribers for '" + topic + "': " + topicList.size());
                }

                // Main Message Loop 
                String line;
                while ((line = reader.readLine()) != null) {
                    
                    System.out.println("[Message] From " + clientAddress + " (" + clientType + " on " + topic + "): " + line);

                    if ("terminate".equalsIgnoreCase(line.trim())) {
                        System.out.println("[Disconnection] Client " + clientAddress + " sent 'terminate'.");
                        break; 
                    }

                    // broadcast using the registered topic
                    if (clientType.equals("PUBLISHER")) {
                        broadcastMessage(topic, line);
                    }
                }

            } catch (IOException e) {
                System.out.println("[Disconnection] " + clientAddress + " (" + clientType + " on " + topic + ") connection lost.");
            } finally {
                // Cleanup 
                if (clientType.equals("SUBSCRIBER") && this.writer != null && topicList != null) {
               
                    synchronized (topicList) {
                        topicList.remove(this.writer);
                    }
                    System.out.println("Removed subscriber: " + clientAddress + " from topic '" + topic + "'. Remaining: " + topicList.size());

                    if (topicList.isEmpty()) {
                        topicSubscribers.remove(topic);
                        System.out.println("Topic '" + topic + "' is now empty and removed.");
                    }
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // Ignore error on close
                }
                System.out.println("[Connection] Closed connection for " + clientAddress);
            }
        }
    }
}