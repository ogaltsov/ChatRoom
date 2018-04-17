package com.galts.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            System.out.println(socket.getInetAddress().toString());
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        // /auth login1 pass1
                        if (str.startsWith("/auth ")) {
                            System.out.println("[INFO] Попытка авторизации от: "+socket);
                            String[] tokens = str.split("\\s");
                            if (tokens.length == 3) {
                                System.out.println("[INFO] Проверка данных в БД: "+socket);
                                String nickFromDB = SQLHandler.getNickByLoginAndPassword(tokens[1], tokens[2]);
                                if (nickFromDB != null) {
                                    if (!server.isNickBusy(nickFromDB)) {
                                        sendMsg("/authok " + nickFromDB);
                                        nickname = nickFromDB;
                                        System.out.println("[INFO] Успешная авторизация: "+nickname+"; "+socket);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется");
                                        System.out.println("[WARNING] Учетная запись уже используется: "+socket);
                                    }
                                } else {
                                    sendMsg("Неверный логин/пароль");
                                    System.out.println("[WARNING] Неверные данные авторизиции(логин,пароль): "+socket);
                                }
                            } else {
                                sendMsg("Неверный формат данных авторизации");
                                System.out.println("[WARNING] Комманда разбита более или менее чем на три части: "+socket);
                            }
                        }
                        if (str.startsWith("/registration ")) {
                            System.out.println("[INFO] Попытка регистрации: "+socket);
                            String[] tokens = str.split("\\s");
                            if (tokens.length == 4) {
                                if (SQLHandler.tryToRegister(tokens[1], tokens[2], tokens[3])) {
                                    sendMsg("Регистрация прошла успешно");
                                    System.out.println("[INFO] Регистрация прошла успешно: "+socket);
                                } else {
                                    sendMsg("Логин или ник уже заняты");
                                    System.out.println("[WARNING] : Логин или ник уже заняты: "+socket);
                                }
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        System.out.println("[INFO] Ожидание сообщения от: "+nickname+"; "+socket);
                        System.out.println("[INFO] Сообщение от : " + nickname + ": " + str+"; "+socket);
                        if(str.equals("/authok")) {
                            server.subscribe(this);
                            System.out.println("[INFO] Клиент ответил об успешной авторизации: "+nickname+"; "+socket);}
                            System.out.println("[INFO] Добавляем клиента в список: "+nickname+"; "+socket);

                    if (!str.startsWith("/")) {
                            server.broadcastMsg(nickname + " " + str);
                            System.out.println("[INFO] Клиент "+nickname+" оправил всем сообщение: \""+str+"\"; "+socket);

                    } else {
                            if (str.equals("/end")) {
                                System.out.println("[INFO] Клиент завершил сессию: "+nickname+"; "+socket);
                                break;
                            }
                            if (str.startsWith("/w ")) {
                                // /w nick2 hello java
                                String[] tokens = str.split("\\s", 3);
                                if (tokens.length == 3) {
                                    server.personalMsg(this, tokens[1], tokens[2]);
                                    System.out.println("[INFO] Клиент "+nickname+" отправил личное сообщение "+tokens[1]+": "+str+"; "+socket);
                                } else {
                                    sendMsg("/log Неверный формат личного сообщения");
                                    System.out.println("[WARNING] Неверный формат личного cообщения(/w): "+nickname+"; "+socket);
                                }
                            }
                            if (str.startsWith("/cn ")){
                                // /changenick newNick
                                System.out.println("[INFO] Клиент отправил запрос на смену nickname: "+nickname+"; "+socket);
                                String[] tokens = str.split("\\s", 3);
                                if (tokens.length == 2) {
                                    if(server.isNickBusy(tokens[1])){ sendMsg("/log Никнейм уже занят");
                                    System.out.println("[WARNING] Nickname на смену уже занят: "+nickname+"; "+socket);
                                    }
                                    else{ SQLHandler.changeNick(nickname ,tokens[1]);
                                        System.out.println("[INFO] Клиент "+nickname+" сменил ник на "+tokens[1]+": "+socket);
                                          nickname=tokens[1];
                                          server.broadcastClientsList();
                                        sendMsg("/cn "+nickname);

                                    }
                                } else {
                                    sendMsg("/log Неверный формат сообщения");
                                    System.out.println("[WARNING] Неверный формат смены ника(/cn): "+nickname+"; "+socket);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error CH#001");
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("Error CH#002");
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        System.out.println("Error CH#003");
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error CH#004");
                        e.printStackTrace();
                    }
                    server.unsubscribe(ClientHandler.this);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
    public void sendMsg(String msg) {
        try {
            System.out.println(socket+"\n"+msg);
            out.writeUTF(msg);
        } catch (IOException e) {
            System.out.println("Error CH#005");
            e.printStackTrace();
        }
    }
}
