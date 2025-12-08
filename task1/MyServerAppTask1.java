
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServerAppTask1 {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MyServerAppTask1 <PORT>");
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

        // Server Setup 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for a connection on port " + port + "...");


            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("[Connection] New connection from " + clientSocket.getRemoteSocketAddress());

                // I/O Setup
                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line;

     
                while ((line = reader.readLine()) != null) {

                    System.out.println("[Message from Client] " + line);

                    if ("terminate".equalsIgnoreCase(line.trim())) {
                        System.out.println("[Disconnection] Client sent 'terminate'.");
                        break; 
                    }
                }

            }
            
            System.out.println("[Disconnection] Client disconnected.");

        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Server shutting down.");
    }
}