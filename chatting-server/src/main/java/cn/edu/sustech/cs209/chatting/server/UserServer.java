package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class UserServer implements Runnable {

    private Socket s;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    static private HashMap<String, String> userList = new HashMap<>();

    public UserServer(Socket s, HashMap<String, String> userList) {
        this.s = s;
        if (userList.isEmpty()) {
            UserServer.userList = userList;
        }
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
            Message msg = (Message) in.readObject();
            checkPassword(msg);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized void checkPassword(Message msg) throws IOException {
        String user = msg.getSentBy();
        String password = msg.getData();
        switch (msg.getType()) {
            case connect:
                if (userList.containsKey(user) & userList.containsValue(password)) {
                    Message back = new Message(0L, "Server", user, "true", MessageType.connect);
                    out.writeObject(back);
                    System.out.println("right answer");
                } else {
                    Message back = new Message(0L, "Server", user, "false", MessageType.connect);
                    out.writeObject(back);
                    System.out.println("Wrong answer");
                }
                break;
            case register:
                if (userList.containsKey(user)) {
                    Message back = new Message(0L, "Server", user, "same", MessageType.register);
                    out.writeObject(back);
                } else if (password.equals("")) {
                    Message back = new Message(0L, "Server", user, "null", MessageType.register);
                    out.writeObject(back);
                } else {
                    userList.put(user, password);
                    Message back = new Message(0L, "Server", user, "success", MessageType.register);
                    out.writeObject(back);
                    System.out.println("Register");
                    savePassword(userList);
                }
                break;
        }
        out.flush();
    }

    static void savePassword(HashMap<String, String> userList) {
        try (FileOutputStream fileOut = new FileOutputStream("pw.map");
            ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(userList);
            System.out.println("HashMap saved to " + "pw.map");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
