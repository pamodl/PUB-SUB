
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MyServerAppTask2 {


    private static final List<PrintWriter> subscriberWriters = 
        Collections.synchronizedList(new LinkedList<>());

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MyServerAppTask2 <PORT>");
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
            System.out.println("Pub/Sub Server (Task 2) started. Listening on port " + port + "...");

            // Main server loop
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // Create a new thread for each connection
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

    
     // Sends a message to all registered subscribers.
    
    private static void broadcastMessage(String message) {
        String formattedMessage = "[BROADCAST] " + message;
        System.out.println("[Broadcast] Sending: " + message);

        List<PrintWriter> deadWriters = new LinkedList<>();

        synchronized (subscriberWriters) {
            for (PrintWriter writer : subscriberWriters) {
                try {
                    // Send message
                    writer.println(formattedMessage);
                } catch (Exception e) {
                    deadWriters.add(writer);
                    System.out.println("Error sending to a subscriber. Marking for removal.");
                }
            }
        }
        
        // Remove the disconnected clients
        if (!deadWriters.isEmpty()) {
            subscriberWriters.removeAll(deadWriters);
            System.out.println("Cleaned up " + deadWriters.size() + " disconnected subscribers.");
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private String clientType = "UNKNOWN";
        private PrintWriter writer = null;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            System.out.println("[Connection] New connection from " + clientAddress);

            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                String registration = reader.readLine();
                if (registration == null) return; 
                
                clientType = registration.trim().toUpperCase();

                if (!clientType.equals("PUBLISHER") && !clientType.equals("SUBSCRIBER")) {
                    System.out.println("Error: " + clientAddress + " sent invalid role: " + clientType + ". Disconnecting.");
                    return;
                }

                System.out.println("[Registration] " + clientAddress + " registered as " + clientType);

                // SUBSCRIBER Role
                if (clientType.equals("SUBSCRIBER")) {
                    this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
   
                    subscriberWriters.add(this.writer);
                    System.out.println("Total active subscribers: " + subscriberWriters.size());
                }

                // Main Message Loop 
                String line;
                while ((line = reader.readLine()) != null) {
 
                    System.out.println("[Message] From " + clientAddress + " (" + clientType + "): " + line);

                    if ("terminate".equalsIgnoreCase(line.trim())) {
                        System.out.println("[Disconnection] Client " + clientAddress + " sent 'terminate'.");
                        break; 
                    }

                    // If it's a PUBLISHER, broadcast the message
                    if (clientType.equals("PUBLISHER")) {
                        broadcastMessage(line);
                    }
                }

            } catch (IOException e) {
                System.out.println("[Disconnection] " + clientAddress + " (" + clientType + ") connection lost.");
            } finally {
                // Cleanup 
                if (clientType.equals("SUBSCRIBER") && this.writer != null) {
                    subscriberWriters.remove(this.writer);
                    System.out.println("Removed subscriber: " + clientAddress + ". Remaining: " + subscriberWriters.size());
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                }
                System.out.println("[Connection] Closed connection for " + clientAddress);
            }
        }
    }
}