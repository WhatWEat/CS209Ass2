package cn.edu.sustech.cs209.chatting.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {
    public static HashMap<String,String> userList = new HashMap<>();
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket ss = new ServerSocket(25565);
        readPassword();
        try{
            while(true){
                Socket s = ss.accept();
                System.out.println("Client connect");
                UserServer sp = new UserServer(s,userList);
                new Thread(sp).start();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            ss.close();
        }

    }
    static void readPassword() throws IOException {
        try (FileInputStream fileIn = new FileInputStream("pw.map");
            ObjectInputStream in = new ObjectInputStream(fileIn)) {
            userList = (HashMap<String, String>) in.readObject();
            System.out.println("HashMap loaded from " + "pw.map");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            userList = new HashMap<>();
            savePassword(new HashMap<>());
            System.out.println("new file has been created");
        }
    }
    static void savePassword(HashMap<String,String> userList){
        try (FileOutputStream fileOut = new FileOutputStream("pw.map");
            ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(userList);
            System.out.println("HashMap saved to " + "pw.map");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}