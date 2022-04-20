package Server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Users {

    private ArrayList<Entity> entities = new ArrayList<>();

    private static Users instance = null;

    private Users(){
        loadUsers();
        loadGroups();
    }

    public static Users getInstance()
    {
        if(instance == null)
            instance = new Users();

        return instance;
    }

    public Entity getEntityById(String id){

        for (Entity e : entities)
        {
            if(e.id.equals(id))
                return e;
        }
        return null;
    }

    public Group getGroupById(String id){
        for (Entity e : entities)
        {
            if(e.id.equals(id) && e instanceof Group)
            {
                return (Group)e;
            }
        }
        return null;
    }

    public User getUserById(String id){
        for (Entity e : entities)
        {
            if(e.id.equals(id) && e instanceof User)
            {
                return (User) e;
            }
        }
        return null;
    }

    public boolean checkLogin(String login, String password){
        for(Entity e : entities)
        {
            if(e instanceof User) {
                User u = (User)e;
                if (u.id.equals(login) && u.password.equals(password)) {
                    return u.logged;
                }
            }
        }
        return false;
    }

    public boolean tryLogin(Connection connection, String login, String password){
        for(Entity e : entities)
        {
            if(e instanceof User) {
                User u = (User) e;
                if (u.id.equals(login) && u.password.equals(password)) {
                    u.logged = true;
                    u.connection = connection;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean logout(String id){
        for(Entity e : entities)
        {
            if(e instanceof User) {
                User u = (User) e;
                if (u.id.equals(id) && u.logged) {
                    u.logged = false;
                    return true;
                }
            }
        }
        return false;
    }

    private void loadUsers() {
        try {
            List<String> lines = Files.readAllLines(new File("C:\\Users\\Użytkownik\\IdeaProjects\\ChatLuft\\Chat-master\\src\\Server\\USERS.txt").toPath());
            for (String s :lines) {
                String[] data = s.split(":");
                entities.add(new User(data[0], data[1]));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGroups() {
        try {
            List<String> lines = Files.readAllLines(new File("C:\\Users\\Użytkownik\\IdeaProjects\\ChatLuft\\Chat-master\\src\\Server\\GROUPS.txt").toPath());
            for (String s :lines) {
                entities.add(new Group(s));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() {

    }

    public abstract class Entity {
        protected String id;
        public abstract void sendMessage(String message);
        public abstract void dataTransfer(byte[] data);
    }

    public class User extends Entity {

        public User(String login, String password){
            this.id = login;
            this.password = password;
        }

        public User(String login, String password, boolean logged){
            this.id = login;
            this.password = password;
            this.logged = logged;
        }

        String password;
        Connection connection;
        boolean logged = false;

        @Override
        public void sendMessage(String message) {
            connection.sendMessage(message);
        }

        @Override
        public void dataTransfer(byte[] data) {
            connection.sendFile(data);
        }
    }

    public class Group extends Entity {

        private ArrayList<User> usersInGroup;

        public Group(String groupName)
        {
            this.id = groupName;
            usersInGroup = new ArrayList<>();
        }

        public void AddUser(User user) {
            this.usersInGroup.add(user);
        }

        @Override
        public void sendMessage(String message) {
            for (User u:usersInGroup) {
                u.sendMessage(message);
            }
        }

        @Override
        public void dataTransfer(byte[] data) {
            for(User u:usersInGroup) {
                u.dataTransfer(data);
            }
        }
    }
}
