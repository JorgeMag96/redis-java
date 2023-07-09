import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
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
            // Get input and output streams
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            // Read the message from the client and reply
            String clientMessage = in.readUTF();
            System.out.println("Message from client: " + clientMessage);

            // Respond with a message
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeUTF("+PONG\r\n");
        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
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
}
