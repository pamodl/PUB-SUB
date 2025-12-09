
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyClientAppTask3 {

    private static volatile boolean isRunning = true;

    private static class Receiver implements Runnable {
        private Socket socket;
        private String clientType;

        public Receiver(Socket socket, String clientType) {
            this.socket = socket;
            this.clientType = clientType;
        }

        @Override
        public void run() {
            if (!"SUBSCRIBER".equalsIgnoreCase(clientType)) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                System.out.println("Listening for broadcasts...");

                // Continuously read messages from the server
                while (isRunning && (line = reader.readLine()) != null) {
                    System.out.println("\n" + line);
                    System.out.print("> ");
                }
            } catch (IOException e) {
                if (isRunning) {
                    if (!socket.isClosed()) {
                        System.err.println("\n[Connection] Server connection lost. Press ENTER to exit.");
                    }
                }
            }
        }
    }

    //  Main Client Function
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java MyClientAppTask3 <SERVER_IP> <SERVER_PORT> <CLIENT_TYPE> <TOPIC>");
            System.out.println("CLIENT_TYPE must be PUBLISHER or SUBSCRIBER.");
            System.exit(1);
        }

        String serverIp = args[0];
        int serverPort = 0;
        String clientType = args[2].toUpperCase();
        String topic = args[3].toUpperCase();
        
        if (!clientType.equals("PUBLISHER") && !clientType.equals("SUBSCRIBER")) {
             System.err.println("Error: CLIENT_TYPE must be PUBLISHER or SUBSCRIBER.");
             System.exit(1);
        }
        
        try {
            serverPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: SERVER_PORT must be an integer.");
            System.exit(1);
        }

        // Socket Connection and I/O
        try (
            Socket socket = new Socket(serverIp, serverPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            
            System.out.println("Connected to server at " + serverIp + ":" + serverPort);
            
            // Send Registration Message
            String registrationMessage = clientType + ":" + topic;
            writer.println(registrationMessage);
            System.out.println("Registered as: " + clientType + " on topic: " + topic);

            // Start Receiver Thread
            Thread receiverThread = new Thread(new Receiver(socket, clientType));
            receiverThread.start();
            
            // Main (Send) Loop
            System.out.println("Enter messages to send. Type 'terminate' to exit.");
            String userInput;
            
            System.out.print("> ");
            while ((userInput = consoleReader.readLine()) != null) {
                
                writer.println(userInput);

                if ("terminate".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Disconnecting...");
                    break;
                }
   
                if (isRunning) {
                    System.out.print("> ");
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Error: Unknown host " + serverIp);
        } catch (IOException e) {
            System.err.println("Error: Unable to connect or communicate with server: " + e.getMessage());
        } finally {
            isRunning = false; // Signal the receiver thread to stop
            System.out.println("Connection closed.");
        }
    }
}