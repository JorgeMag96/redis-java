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
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeBytes(encodeAsRespSimpleString(PING_RESPONSE));

            /*
            String command = new String(data, 0, size);
            if(command.startsWith("PING")) {
                String argument = command.substring(4).trim();
                System.out.println(String.format("Received command: PING %s", argument));

                if(argument.length() > 0) {
                    out.writeBytes(encodeAsRespBulkString(argument));
                } else {
                    out.writeBytes(encodeAsRespSimpleString(PING_RESPONSE));
                }
            }
        */
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
