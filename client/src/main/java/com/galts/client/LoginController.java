package com.galts.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ResourceBundle;



public class LoginController implements Initializable {
    @FXML
    TextField loginField;
    @FXML
    PasswordField passField;
    @FXML
    Text textCreateAcc;
    @FXML
    Label textWarning;
    @FXML
    Button signButton;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public void initialize(URL location, ResourceBundle resources) {

        loginField.setPrefHeight(29.0);
        passField.setPrefHeight(29.0);
        signButton.getStyleClass().add("signBtn");
    }
//
    private void sendAuth() {
        // /auth login pass
        sendMsg("/auth " + loginField.getText() + " " + sha256(passField.getText()));
        loginField.clear();
        passField.clear();
    }

    public void connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("176.57.215.210", 8189);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            }
                sendAuth();
                String str = in.readUTF();
                if(str.startsWith("/authok ")){
                    nickname = str.split("\\s")[1];
                    System.out.println(5);   //////////////
                    startChat();
                }
                else{
                    textWarning.setText(str);
                }
            }
            catch (IOException e){
            e.printStackTrace();
            }
    }

    private void startChat(){
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample.fxml"));
            Parent root = fxmlLoader.load();
            stage.setTitle("Java Chat Client: Регистрация");
            stage.setScene(new Scene(root, 650, 650));
            stage.setResizable(false);
            Controller chatController = fxmlLoader.getController();
            chatController.initFromLogin(socket, in, out, nickname);
            ((Stage)passField.getScene().getWindow()).close();
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
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

    public void registerBtn() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            Parent root = loader.load();
            stage.setTitle("Java Chat Client: Регистрация");
            stage.setScene(new Scene(root, 400, 240));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
