package com.galts.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    Server() {
        try {
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new Vector<>();
            SQLHandler.connect();
            while (true) {
                System.out.println("Ждем подключения клиента");
                Socket socket = serverSocket.accept();
                ClientHandler c = new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Error Se#001");
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();
        }
    }
//
    public void subscribe(ClientHandler client) {
        if (!(client ==null)) {
            broadcastMsg("/log " + client.getNickname() + " подключился к чату");
            clients.add(client);
            client.sendMsg("/log " + client.getNickname() + ", добро пожаловать в чат!");
        }
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        if (!(client == null)) {
            broadcastMsg("/log " + client.getNickname() + " покинул чат");
            clients.remove(client);
        }
        broadcastClientsList();
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void personalMsg(ClientHandler from, String to, String msg) {
        for (ClientHandler o : clients) {
            if(o.getNickname().equals(to)) {
                o.sendMsg("from " + from.getNickname() + ": " + msg);
                from.sendMsg("to " + to + ": " + msg);
                return;
            }
        }
        from.sendMsg("/log "+"Клиента с ником " + to + " нет в чате");
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNickname() + " ");
        }
        // '/clientslist nick1 nick2 '
        sb.setLength(sb.length() - 1);
        // '/clientslist nick1 nick2'
        String out = sb.toString();
        broadcastMsg(out);
    }
}
