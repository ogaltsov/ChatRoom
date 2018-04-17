package com.galts.client;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    Label messageTo;

    @FXML
    VBox chatBox;

    @FXML
    TextField textField;

    @FXML
    VBox mainBox;

    @FXML
    HBox msgPanel;

    @FXML
    Button sendMsgBtn;

    @FXML
    VBox clientListBox;

    @FXML
    private ScrollPane scrollPane;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String messageAdd="";
    //private ObservableList<Text> clients;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.vvalueProperty().bind(chatBox.heightProperty());
        chatBox.getStyleClass().add(".chatBox");

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                sendMsgBtn.setDisable(true);
            } else {
                sendMsgBtn.setDisable(false);
            }
        });

        Platform.runLater(() -> ((Stage) mainBox.getScene().getWindow()).setOnCloseRequest(t -> {
            sendMsg("/end");
            Platform.exit();
        }));
    }

    public void initFromLogin(Socket socket, DataInputStream in, DataOutputStream out, String nickname){
        this.socket=socket;
        this.in=in;
        this.out=out;
        this.nickname=nickname;
        connect();
    }

    public void sendMsg() {
        try {
            if (socket != null && !socket.isClosed()) {
                String str = textField.getText();
                if (!str.isEmpty()) {
                    if(messageAdd.equals("")) out.writeUTF(str);
                    if(!messageAdd.equals("")) out.writeUTF("/w "+messageAdd+" "+str);
                textField.clear();
                textField.requestFocus();
            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String msg) {
        try {
            if (socket != null && !socket.isClosed()) {
                if (!msg.isEmpty()) {
                    out.writeUTF(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        if (!(socket == null || socket.isClosed())) {
            new Thread(() -> {
                try {
                    sendMsg("/authok");
                    while (true) {
                        String str = in.readUTF();
                        StringBuilder temp = new StringBuilder();
                        String[] tokens = str.split("\\s");
                        for(int i = 1; i<tokens.length; i++){
                            temp.append(tokens[i]).append(" ");
                        }
                        if (!str.startsWith("/")) {
                            Text text=new Text(temp.toString());
                            text.setFill(Color.WHITE);
                            text.getStyleClass().add("message");
                            TextFlow tempFlow=new TextFlow();

                            if(!nickname.equals(tokens[0])){
                                Text txtName=new Text(tokens[0] + ":\n");
                                txtName.getStyleClass().add("txtName");
                                tempFlow.getChildren().add(txtName);
                            }
                            tempFlow.getChildren().add(text);
                            tempFlow.setMaxWidth(200);
                            TextFlow flow=new TextFlow(tempFlow);
                            HBox hbox=new HBox(12);

                            if(!nickname.equals(tokens[0])){
                                text.setFill(Color.BLACK);
                                tempFlow.getStyleClass().add("tempFlowFlipped");
                                flow.getStyleClass().add("textFlowFlipped");
                                chatBox.setAlignment(Pos.TOP_LEFT);
                                hbox.setAlignment(Pos.CENTER_LEFT);
                                hbox.getChildren().add(flow);

                            }else{
                                text.setFill(Color.WHITE);
                                tempFlow.getStyleClass().add("tempFlow");
                                flow.getStyleClass().add("textFlow");
                                hbox.setAlignment(Pos.BOTTOM_RIGHT);
                                hbox.getChildren().add(flow);
                            }

                            hbox.getStyleClass().add("hbox");
                            Platform.runLater(() -> chatBox.getChildren().addAll(hbox));

                        } else if (str.startsWith("/clientslist ")) {
                           updateUI(str);
                        } else if(str.startsWith("/cn ")){
                            if(tokens.length==2) {
                                nickname=tokens[1];

                                Platform.runLater(() -> {
                                    if (nickname.isEmpty()) {
                                        ((Stage) mainBox.getScene().getWindow()).setTitle("Java Chat Client");
                                    } else {
                                        ((Stage) mainBox.getScene().getWindow()).setTitle("Java Chat Client: " + nickname);
                                    }
                                });

                                Text text=new Text("Ник изменен на: "+nickname);

                                text.setFill(Color.BLACK);
                                text.getStyleClass().add("message");
                                TextFlow tempFlow=new TextFlow();
                                tempFlow.getChildren().add(text);
                                tempFlow.setMaxWidth(400);

                                TextFlow flow=new TextFlow(tempFlow);

                                HBox hbox=new HBox(12);

                                text.setFill(Color.BLACK);
                                tempFlow.getStyleClass().add("tempFlowLog");
                                flow.getStyleClass().add("textFlowLog");
                                hbox.setAlignment(Pos.BOTTOM_CENTER);
                                hbox.getChildren().add(flow);
                                hbox.getStyleClass().add("hbox");
                                Platform.runLater(() -> chatBox.getChildren().addAll(hbox));
                            }
                        }else if (str.startsWith("/log ")) {
                            Text text=new Text(temp.toString());

                            text.setFill(Color.BLACK);
                            text.getStyleClass().add("message");
                            TextFlow tempFlow=new TextFlow();
                            tempFlow.getChildren().add(text);
                            tempFlow.setMaxWidth(400);

                            TextFlow flow=new TextFlow(tempFlow);

                            HBox hbox=new HBox(12);

                            text.setFill(Color.BLACK);
                            tempFlow.getStyleClass().add("tempFlowLog");
                            flow.getStyleClass().add("textFlowLog");
                            hbox.setAlignment(Pos.BOTTOM_CENTER);
                            hbox.getChildren().add(flow);


                            hbox.getStyleClass().add("hbox");
                            Platform.runLater(() -> chatBox.getChildren().addAll(hbox));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
//
    private boolean updateUI(String cList) {
        Platform.runLater(() -> clientListBox.getChildren().clear());
        String[] tokens = cList.split("\\s");
        {
            HBox container=new HBox() ;
            container.setAlignment(Pos.CENTER_LEFT);
            container.setSpacing(10);
            container.setPrefWidth(clientListBox.getPrefWidth());
            container.setPadding(new Insets(3));
            container.getStyleClass().add("online-user-container");

            Label lblUsername=new Label(nickname+" (You)");
            lblUsername.getStyleClass().add("online-label");
            container.getChildren().add(lblUsername);
            Platform.runLater(() -> clientListBox.getChildren().add(container));
        }
        for (int i = 1; i < tokens.length; i++) {
            if(tokens[i].equals(nickname)) continue;
            HBox container=new HBox() ;
            container.setAlignment(Pos.CENTER_LEFT);
            container.setSpacing(10);
            container.setPrefWidth(clientListBox.getPrefWidth());
            container.setPadding(new Insets(3));
            container.getStyleClass().add("online-user-container");

            Label lblUsername=new Label(tokens[i]);
            lblUsername.getStyleClass().add("online-label");
            container.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    privateMessageTo(lblUsername.getText());
                }
            });
            container.getChildren().add(lblUsername);
            Platform.runLater(() -> clientListBox.getChildren().add(container));
        }
        return true;
    }

    private void privateMessageTo(String text) {
        messageTo.setText("To "+text);
        messageAdd = text;
    }

    public void turnBroad(){
        messageTo.setText("");
        messageAdd = "";
    }
}
