import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final String PING_RESPONSE = "PONG";

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();

            byte[] data = new byte[100];
            int size = clientSocket.getInputStream().read(data);

            String command = new String(data, 0, size);

            if(command.startsWith("PING")) {
                String argument = command.substring(4).trim();
                System.out.println(String.format("Received command: PING %s", argument));

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                if(argument.length() > 0) {
                    //out.writeBytes(encodeAsRespString(argument));
                    out.writeBytes(encodeAsRespString(PING_RESPONSE)); // For now ignore the argument and just respond with PONG.
                } else {
                    out.writeBytes(encodeAsRespString(PING_RESPONSE));
                }
            }
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

    private static String encodeAsRespString(String string) {
        return String.format("+%s\r\n", string);
    }
}
