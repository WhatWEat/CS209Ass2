package cn.edu.sustech.cs209.chatting.client.util;

import java.io.IOException;
import cn.edu.sustech.cs209.chatting.common.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import jdk.internal.util.xml.impl.Input;

public class Sender implements Runnable{
    public static final int port = 25565;
    private String username;
    private String password;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private boolean register = false;
    public Sender(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public Sender(String username, String password,boolean register) {
        this.username = username;
        this.password = password;
        this.register = register;
    }
    @Override
    public void run() {
        try {
            socket = new Socket("localhost",port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if(register) {
                toRegister();
                register = false;
            }else {
                connect();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    void toRegister() throws IOException {
        Message pw = new Message(0L,username, "Server",password,MessageType.register);
        out.writeObject(pw);
    }
    void connect() throws IOException {
        Message pw = new Message(0L,username, "Server",password,MessageType.connect);
        out.writeObject(pw);
        //out.flush();
    }
}
