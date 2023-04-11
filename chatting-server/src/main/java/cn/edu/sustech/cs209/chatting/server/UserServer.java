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

    private final Socket s;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    static private HashMap<String, String> userList = new HashMap<>();
    /*use to save the user and password*/
    static private HashMap<String,ObjectOutputStream> outList = new HashMap<>();
    /*save the username corresponding socket*/
    public UserServer(Socket s, HashMap<String, String> userList) {
        this.s = s;
        if (UserServer.userList.isEmpty()) {
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
            while(s.isConnected() && !s.isClosed()){
                msg = (Message) in.readObject();
                if(msg != null){
                    switch (msg.getType()){
                        case chat:
                            break;
                        case disconnect:
                            close();
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized void checkPassword(Message msg) throws IOException {
        String user = msg.getSentBy();
        String password = msg.getData();
        switch (msg.getType()) {
            case connect:
                if (userList.containsKey(user) & userList.containsValue(password)) {
                    send(user, "true", MessageType.connect);
                    username = msg.getSentBy();
                    outList.put(username,out);
                    sendALL(new Message(0L,"Server","ALL", String.valueOf(outList.size()),MessageType.disconnect));
                    System.out.println("right answer"+outList.size());
                } else {
                    send(user, "false", MessageType.connect);
                    System.out.println("Wrong answer");
                }
                break;
            case register:
                if (userList.containsKey(user)) {
                    send(user,"same", MessageType.register);
                } else if (password.equals("")) {
                    send(user,"null", MessageType.register);
                } else {
                    userList.put(user, password);
                    send(user,"success", MessageType.register);
                    System.out.println("Register");
                    savePassword(userList);
                }
                break;
        }
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
    void send(String username,String message,MessageType type) throws IOException {
        Message back = new Message(0L, "Server", username, message, type);
        out.writeObject(back);
        out.flush();
    }
    void sendALL(Message msg){
        outList.values().forEach(value -> {
            try {
                System.out.println("发送了信号");
                value.writeObject(msg);
                value.flush();
            } catch (IOException e) {
                System.err.println("该用户已经下号了");
                throw new RuntimeException(e);
            }
        });
    }
    void close() throws IOException {
        outList.remove(username);
        sendALL(new Message(0L,"Server","ALL", String.valueOf(outList.size()),MessageType.disconnect));
        if(in != null) in.close();
        if(out != null) out.close();
        if(s != null || !s.isClosed()) s.close();
    }
}
