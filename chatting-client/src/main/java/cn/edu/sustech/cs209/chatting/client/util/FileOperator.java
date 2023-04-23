package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.client.view.UserlistController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javafx.collections.FXCollections;

public class FileOperator {

    public static String basicDir = null;

    public static File judgeDir() {
        File file = new File(basicDir + UserlistController.thisuser.getUsername());
        if (!file.getParentFile().getParentFile().exists()) {
            file.getParentFile().getParentFile().mkdirs();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    public static boolean judgeGroup(String userListFile) {
        File file = new File(basicDir + userListFile);
        return file.exists();
    }

    public static void readUserList() {
        try {
            if (basicDir == null) {
                basicDir = ".\\user\\" + UserlistController.thisuser.getUsername() + "\\";
            }
            File file = judgeDir();
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            ArrayList<User> userList = (ArrayList<User>) objectIn.readObject();
            UserlistController.userList = FXCollections.observableArrayList(userList);
            userList.forEach(i -> i.setOnline(false));
            objectIn.close();
            fileIn.close();
        } catch (IOException e) {
            // 如果文件不存在，则创建一个新的 ArrayList<User> 对象
            UserlistController.userList = FXCollections.observableArrayList();
            saveUserList();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }
    }

    public static void saveUserList() {
        try {
            //读取目录
            File file = judgeDir();
            //从file读取流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            ArrayList<User> userList = new ArrayList<>(UserlistController.userList);
            out.writeObject(userList);
            out.close();
            fileOutputStream.close();
            System.out.println("用户列表保存成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Group readGroupList(ArrayList<String> userList) {
        String fileName = userList.toString();
        if (!judgeGroup(fileName)) {
            return new Group(userList);
        } else {
            try {
                File file = new File(basicDir + fileName);
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                Group group = (Group) objectIn.readObject();
                objectIn.close();
                fileIn.close();
                return group;
            } catch (IOException e) {
                System.out.println("Group"+fileName+"文件不存在");
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + e.getMessage());
            }
        }
        return null;
    }
    public static void saveGroupList(Group group){
        try{
            File file = new File(basicDir+group.groupMember.toString());
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(group);
            System.out.println("Group"+group.groupMember.toString()+"保存成功");
            out.close();
            fileOut.close();
        } catch (IOException e) {
            System.out.println("Group"+group.groupMember.toString()+"保存失败");
        }
    }
}
