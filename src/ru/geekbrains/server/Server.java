package ru.geekbrains.server;

import ru.geekbrains.common.ServerConst;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server implements ServerConst{
    private Vector<ClientHandler> clients;
    private AuthService authService;
    public AuthService getAuthService(){
        return authService;
    }
    public Server(){
        ServerSocket serverSocket = null;
        Socket socket = null;
        clients = new Vector<>();
        try{
            serverSocket = new ServerSocket(PORT);
            authService = new BaseAuthService();
            authService.start(); //placeholder
            System.out.println("Сервер запущен, ждем клиентов");
            while(true){
                socket = serverSocket.accept(); //ждем подключений, сервер становится на паузу
                clients.add(new ClientHandler(this, socket));
                System.out.println("Клиент подключился");
            }
        }catch(IOException e){
            System.out.println("Ошибка инициализации");
        }finally{
            try{
                serverSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void broadcast(String message){
        for(ClientHandler client : clients){
            client.sendMessage(message);
        }
    }
    public void sendMessageTo(String nick, String message){
        for(ClientHandler client : clients){
            if (nick.equalsIgnoreCase(client.getNick())) {
                client.sendMessage(message);
            }
        }
    }
    public void unsubscribeMe(ClientHandler c){
        clients.remove(c);
    }
}
