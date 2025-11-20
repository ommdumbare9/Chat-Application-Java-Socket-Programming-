import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatServerGUI gui;
    private final PrintWriter out;
    private final Server server;
    private String username;

    public ServerHandler(Socket clientSocket, ChatServerGUI gui, Server server) throws IOException {
        this.clientSocket = clientSocket;
        this.gui = gui;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            // Get the username from the client
            username = in.readLine();
            gui.appendMessage("User '" + username + "' connected.");

            // Add the client to the server's client list
            server.addClient(username, this);

            // Broadcast the user's entry to all clients
            server.broadcastMessage("Server", "User '" + username + "' joined the chat.");

            while (true) {
                String clientMessage = in.readLine();
                if (clientMessage == null || "bye".equalsIgnoreCase(clientMessage)) {
                    gui.appendMessage("User '" + username + "' disconnected.");
                    break;
                }

                gui.appendMessage("User '" + username + "': " + clientMessage);
                // Broadcast the message to all clients
                server.broadcastMessage(username, clientMessage);
            }

            // Remove the client from the server's client list
            server.removeClient(username);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
