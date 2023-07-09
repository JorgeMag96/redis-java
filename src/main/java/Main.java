import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final String PING_RESPONSE = "PONG";

    public static void main(String[] args) {

        int port = 6379;
        try(ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);

            // Wait for connection from client.
            System.out.println("Waiting for connections...");

            while(true) {
                Socket clientSocket = ss.accept();
                String clientId = "client-" + System.currentTimeMillis();
                System.out.println("Client connected: " + clientId);
                new Thread(() -> handleClient(clientSocket, clientId)).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage() + ", " + e.getStackTrace()[0].toString());
        }
    }

    private static void handleClient(Socket clientSocket, String clientId) {
        try {
            byte[] inputBuffer = new byte[100];
            while(true) {
                int size = clientSocket.getInputStream().read(inputBuffer);
                if(size == -1) break;

                String inputData = new String(inputBuffer, 0, size);
                System.out.println("Received inputData: " + inputData.replace("\r\n", "\\r\\n"));
                String[] command = inputData.split("\r\n");
                System.out.println("Received command: " + command[2]);

                // Send response to client.
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                out.writeBytes(encodeAsRespSimpleString(PING_RESPONSE));
            }

            System.out.printf("Client %s disconnected.%n", clientId);

//            String command = new String(data, 0, size);
//            if(command.startsWith("PING")) {
//                String argument = command.substring(4).trim();
//                System.out.println(String.format("Received command: PING %s", argument));
//
//                if(argument.length() > 0) {
//                    out.writeBytes(encodeAsRespBulkString(argument));
//                } else {
//                    out.writeBytes(encodeAsRespSimpleString(PING_RESPONSE));
//                }
//            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage() + ", " + e.getStackTrace()[0].toString());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    private static String encodeAsRespSimpleString(String string) {
        return String.format("+%s\r\n", string);
    }

    private static String encodeAsRespBulkString(String string) {
        return String.format("$%d\r\n%s\r\n",string.length(), string);
    }
}
