package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class UserServer implements Runnable{
    private Socket s;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private HashMap<String,String> userList;
    public UserServer(Socket s,HashMap<String,String> userList) {
        this.s = s;
        this.userList = userList;
    }

    @Override
    public void run() {
        try{
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
            Message msg = (Message) in.readObject();
            checkPassword(msg);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    synchronized void checkPassword(Message msg){
        String user = msg.getSentBy();
        String password = msg.getData();
        switch (msg.getType()){
            case connect:
                if(userList.containsKey(user) & userList.containsValue(password)){
                    System.out.println("right answer");
                } else {
                    System.out.println("Wrong answer");
                }
                break;
            case register:
                if(userList.containsKey(user)) {
                    System.out.println("Already have the same user");
                } else {
                    userList.put(user,password);
                    System.out.println("Register");
                    savePassword(userList);
                }
                break;
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
