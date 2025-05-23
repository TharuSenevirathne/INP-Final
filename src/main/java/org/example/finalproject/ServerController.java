package org.example.finalproject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerController {
    public Button sendMsg;
    public TextField serverInputMsg;
    public VBox ServerTextArea;
    public ScrollPane scrollPane;

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private int clientCounter = 0;

    public void initialize() {
        ServerTextArea.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));

        new Thread(() -> {
            try {
                appendMsg("Server Started!");
                serverSocket = new ServerSocket(3000);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    clientCounter++;
                    String clientId = String.format("Client %02d", clientCounter);
                    appendMsg(clientId + " Connected: " + clientSocket.getInetAddress().getHostAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientId);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();

                    try {
                        DataOutputStream initialDos = new DataOutputStream(clientSocket.getOutputStream());
                        initialDos.writeUTF("CLIENTID:" + clientId);
                        initialDos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> appendMsg("Error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    public void appendMsg(String text) {
        Platform.runLater(() -> {
            Label label = new Label(text);
            ServerTextArea.getChildren().add(label);
        });
    }

    public void btnSend(ActionEvent actionEvent) {
        String message = serverInputMsg.getText();
        if (!message.isEmpty()) {
            broadcastMessage("Server: " + message);
            appendMsg("Server: " + message);
            serverInputMsg.clear();
        }
    }


    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        appendMsg(clientHandler.getClientId() + " disconnected. Total clients: " + clients.size());
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private ServerController server;
        private String clientId;

        public ClientHandler(Socket socket, ServerController server, String clientId) {
            this.socket = socket;
            this.server = server;
            this.clientId = clientId;

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getClientId() {
            return clientId;
        }

        @Override
        public void run() {
            try {
                String message;
                while (true) {
                    message = dataInputStream.readUTF();
                    String formattedMessage = clientId + ": " + message;
                    Platform.runLater(() -> appendMsg(formattedMessage));

                    for (ClientHandler client : clients) {
                        if (client != this) {
                            client.sendMessage(formattedMessage);
                        }
                    }
                }
            } catch (IOException e) {
                server.removeClient(this);
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            try {
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
