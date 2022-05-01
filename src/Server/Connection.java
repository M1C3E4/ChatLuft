package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Connection implements Runnable{

    private final FileChatLogDAO chatLogDAO;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private DataOutputStream dataOutput;
    private DataInputStream dataInput;

    public Connection(Socket socket, FileChatLogDAO chatLogDAO)
    {
        this.chatLogDAO = chatLogDAO;
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try
        {
            while (true)
            {
                String line;
                line = reader.readLine();
                action(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        writer.println(Operation.RECEIVE_MESSAGE + ":" + message);
        writer.flush();
        System.out.println("[SERVER-LOG] RESPONSE SEND -> " + message);
    }

    public void sendFile(byte[] bytes){
        try {
            FileInputStream fileInputStream = new FileInputStream("F:\\rolly.txt");
            byte b[] = new byte[2002];
            try {
                fileInputStream.read(b, 0, b.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            OutputStream outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // line - [login]:[hasło]:[typoperacji]:[arg1]:[arg2]:[arg...n]
    public void action(String line) throws IOException {
        System.out.println("[SERVER-LOG] REQUEST RECEIVED -> " + line);
        String data[] = line.split(":");
        Users users = Users.getInstance();

        switch (Operation.valueOf(data[2]))
        {
            case LOGIN:
                if(users.tryLogin(this, data[0], data[1]))
                {
                    sendMessage(Operation.LOGIN_OK.toString());
                }
                else
                {
                    sendMessage(Operation.LOGIN_FAILED.toString());
                }
                break;

            case SEND_MESSAGE:
                if(users.checkLogin(data[0], data[1]))
                {
                    //wysylanie do innych userow
                    Users.Entity targetEntity = users.getEntityById(data[4]);
                    targetEntity.sendMessage(data[3]);
                    chatLogDAO.saveMessage(new MessageDTO(data[0], data[4], data[3], determineChatType(targetEntity)));
                    sendMessage(Operation.SEND_MESSAGE_OK.toString());
                }
                else
                {
                    sendMessage(Operation.SEND_MESSAGE_FAILED.toString());
                }
                break;

            case FILE_TRANSFER:
                if(users.checkLogin(data[0], data[1]))
                {
                }
                else
                {
                    sendMessage(Operation.FILE_TRANSFER_FAILED.toString());
                }
                break;

            case GET_HISTORY:
                if(users.checkLogin(data[0], data[1]))
                {
                    String from = data[0];
                    String to = data[3];

                    Users.Entity entity = users.getEntityById(to);
                    List<MessageDTO> messages = (entity instanceof Users.Group) ? chatLogDAO.getGroupMessages(to) : chatLogDAO.getPrivateMessages(from, to);
                    messages.stream()
                            .map(MessageDTO::toString)
                            .forEach(this::sendMessage);

                    sendMessage(Operation.GET_HISTORY_OK.toString());
                }
                else
                {
                    sendMessage(Operation.GET_HISTORY_FAILED.toString());
                }
                break;

            case JOIN_GROUP:
                if(users.checkLogin(data[0], data[1]))
                {
                    users.getGroupById(data[3]).AddUser(Users.getInstance().getUserById(data[0]));
                    sendMessage(Operation.JOIN_GROUP_OK.toString());
                }
                else
                {
                    sendMessage(Operation.JOIN_GROUP_FAILED.toString());
                }
                break;

            case LOGOUT:
                if(users.checkLogin(data[0], data[1]))
                {
                    users.logout(data[0]);
                    sendMessage(Operation.LOGOUT_OK.toString());
                }
                else
                {
                    sendMessage(Operation.LOGOUT_FAILED.toString());
                }
                break;
        }
    }

    private ChatType determineChatType(Users.Entity targetEntity) {
        if (targetEntity instanceof Users.Group) {
            return ChatType.GROUP;
        } else {
            return ChatType.PRIVATE;
        }
    }
}