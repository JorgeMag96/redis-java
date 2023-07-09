import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final String PING_RESPONSE = "PONG";
    private static final String CLRF = "\r\n";

    public static void main(String[] args) {

        int port = 6379;
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);

            // Wait for connection from client.
            System.out.println("Waiting for connections...");

            while (true) {
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
            while (true) {
                int size = clientSocket.getInputStream().read(inputBuffer);
                if (size == -1) break;

                String input = new String(inputBuffer, 0, size);
                System.out.println("Received input: " + input.replace(CLRF, "\\r\\n"));
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                if (input.startsWith("*")) {
                    String response = handleRespArrayCommand(input);
                    System.out.println("Response: " + response.replace(CLRF, "\\r\\n"));
                    out.writeBytes(response);
                } else {
                    out.writeBytes(encodeRespSimpleString(PING_RESPONSE));
                }
            }
            System.out.printf("Client %s disconnected.%n", clientId);
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

    private static String handleRespArrayCommand(String input) {

        String respArrayLength = input.split(CLRF, 2)[0];
        int size = Integer.parseInt(respArrayLength.substring(1));
        String[] commandContent = new String[size];

        String respArrayContent = input.split(respArrayLength.substring(1) + CLRF)[1];

        for (int i = 0; i < size; i++) {
            char type = respArrayContent.charAt(0);
            //System.out.println("RESP type: " + type);

            switch (type) {
                case '+':
                    throw new UnsupportedOperationException("Simple string not supported.");
                case '$':
                    String[] respBulkStringSizeAndContent = respArrayContent.substring(1).split(CLRF, 3);
                    commandContent[i] = respBulkStringSizeAndContent[1];
                    respArrayContent = respBulkStringSizeAndContent[2];
                    break;
                default:
                    String errorMessage = "Invalid RESP type: " + type;
                    System.out.println(errorMessage);
                    return encodeErrorMessage(errorMessage);
            }
        }

        String commandName = commandContent[0];
        System.out.println("Command is: " + commandName);

        switch (commandName.toUpperCase()) {
            case "ECHO":
                return encodeRespBulkString(commandContent[1]);
            case "COMMAND":
                return String.format("*0%s", CLRF);
            case "PING":
                return encodeRespSimpleString(PING_RESPONSE);
            default:
                String errorMessage = "Unsupported command: " + commandName;
                System.out.println(errorMessage);
                return encodeErrorMessage(errorMessage);
        }
    }

    private static String encodeRespSimpleString(String string) {
        return String.format("+%s%s", string, CLRF);
    }

    private static String encodeRespBulkString(String string) {
        return String.format("$%d%s%s%s", string.length(), CLRF, string, CLRF);
    }

    private static String encodeErrorMessage(String string) {
        return String.format("-%s%s", string, CLRF);
    }
}

