
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyClientAppTask1 {

    public static void main(String[] args) {
     
        if (args.length != 2) {
            System.out.println("Usage: java MyClientAppTask1 <SERVER_IP> <SERVER_PORT>");
            System.exit(1);
        }

        String serverIp = args[0];
        int serverPort = 0;
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
            System.out.println("Enter text to send to the server. Type 'terminate' to exit.");

            String userInput;

            // Main (Send) Loop
            System.out.print("> ");
            while ((userInput = consoleReader.readLine()) != null) {
 
                writer.println(userInput);

                if ("terminate".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Disconnecting...");
                    break; 
                }
                System.out.print("> ");
            }

        } catch (UnknownHostException e) {
            System.err.println("Error: Unknown host " + serverIp);
        } catch (IOException e) {
            System.err.println("Error: Unable to connect or communicate with " + serverIp + ":" + serverPort);
            System.err.println(e.getMessage());
        }

        System.out.println("Connection closed.");

    }
}