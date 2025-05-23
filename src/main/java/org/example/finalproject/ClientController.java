package org.example.finalproject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientController {

    public javafx.scene.control.ScrollPane ScrollPane;
    public VBox ClientTextArea;
    public TextField ClientInputMsg;
    public Button sendMsg;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String message = "";
    private String clientId = "";
    private Scanner scanner = new Scanner(System.in);


    public void initialize() {
        ClientTextArea.heightProperty().addListener((observable, oldValue, newValue) -> ScrollPane.setVvalue(1.0));

        new Thread(() -> {
            try {
                socket = new Socket("localhost", 3000);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                appendMsg("Connected to server");

                message = dataInputStream.readUTF();
                if (message.startsWith("CLIENTID:")) {
                    clientId = message.substring(9);
                    Platform.runLater(() -> {
                        try {
                            appendMsg("You are " + clientId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                while (true) {
                    message = dataInputStream.readUTF();

                    if (message.startsWith("file")) {
                        String filePath = message.substring(4);
                        continue;
                    } else {
                        Platform.runLater(() -> {
                            try {
                                appendMsg(message);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    try {
                        appendMsg("Error: Server not found or disconnected");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void appendMsg(String text) throws IOException {

        while (!socket.isClosed()) {
            System.out.print("Client: ");
            String clientMessage = scanner.nextLine();
            dataOutputStream.writeUTF(clientMessage);
            dataOutputStream.flush();

        }
    }

    public void btnSend(ActionEvent actionEvent) {
        String message = ClientInputMsg.getText();
        if (!message.isEmpty()) {
            try {
                appendMsg(clientId + ": " + message);
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                ClientInputMsg.clear();
            } catch (IOException e) {
                Platform.runLater(() -> {
                    try {
                        appendMsg("Error: Could not send message");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                e.printStackTrace();
            }
        }
    }
}
