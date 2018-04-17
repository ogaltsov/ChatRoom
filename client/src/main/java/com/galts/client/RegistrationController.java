package com.galts.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;

public class RegistrationController {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    TextField login, password, nickname;

    @FXML
    Label result;

    public void tryToRegister() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("176.57.215.210", 8189);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                if(checkCombination(login.getText()) && checkCombination(nickname.getText())) {
                    out.writeUTF("/registration " + login.getText() + " " + sha256(password.getText()) + " " + nickname.getText());
                    System.out.println("Отправили в регу данные и ;ltv jndtnf");
                    String answer = in.readUTF();
                    result.setText(answer);
                }
                else result.setText("Использованы недопустимые символы");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
//
    private boolean checkCombination(String comb){
        char[] arr = comb.toCharArray();
        char[] arrEx = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','_','-','.'};
        if(arr.length>8) return false;
        if(arr[0]=='.') return false;
        outer: for(char o : arr){
            for(char p : arrEx ){
                if(o==p) continue outer;
            }
            return false;
        }
        return true;
    }
}
