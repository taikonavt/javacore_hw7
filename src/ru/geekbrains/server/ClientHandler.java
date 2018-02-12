package ru.geekbrains.server;

import ru.geekbrains.common.Server_API;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Server_API {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    public ClientHandler(Server server, Socket socket){
        try{
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }catch(IOException e){
            e.printStackTrace();
        }
        new Thread(()-> {
            try{
                //Auth
                while(true){
                    String message = in.readUTF();
                    if(message.startsWith(AUTH)){
                        String[] elements = message.split(" ");
                        String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                        if(nick != null){
                            sendMessage(AUTH_SUCCESSFUl + " " + nick);
                            this.nick = nick;
                            server.broadcast(this.nick + " has entered the chat room");
                            break;
                        }else sendMessage("Wrong login/password!");
                    }else sendMessage("You should authorize first!");
                }
                while(true){
                    String message = in.readUTF();
                    if(message.startsWith(SYSTEM_SYMBOL)){
                        if(message.equalsIgnoreCase(CLOSE_CONNECTION))
                            break;
                        String[] strings = message.split(" ", 3);
                        if (strings[0].equals(PRIVATE_MESSAGE)){
                            sendMessage("To " + strings[1] + ": " + strings[2]);
                            server.sendMessageTo(strings[1], strings[2]);
                        }
                        else sendMessage("Command doesn't exist!");
                    }else {
                        System.out.println("client " + message);
                        server.broadcast(message);
                    }
                }
            }catch(IOException e){
            }finally{
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String msg){
        try{
            out.writeUTF(msg);
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void disconnect(){
        sendMessage(CLOSE_CONNECTION + " You have been disconnected!");
        server.unsubscribeMe(this);
        try{
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
