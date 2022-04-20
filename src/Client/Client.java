package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;

    private String connectedEntityId;

    private Receiver reader;
    private Sender sender;

    private String login, password;

    public Client(String address, int port) throws IOException {

        socket = new Socket(address, port);

        try {
            reader = new Receiver(new BufferedReader(new InputStreamReader(socket.getInputStream())), new DataInputStream(socket.getInputStream()));
            sender = new Sender(new PrintWriter(new OutputStreamWriter(socket.getOutputStream())), new DataOutputStream(socket.getOutputStream()));
            new Thread(reader).start();
            new Thread(sender).start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mainMenu();
    }

    public void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        int choose = 0;
        while (true) {
            System.out.println("********  USER MENU  ********");
            System.out.println("1. LOGIN");
            System.out.println("2. SEND MESSAGE");
            System.out.println("3. GET HISTORY");
            System.out.println("4. FILE TRANSFER");
            System.out.println("5. CONNECT TO USER");
            System.out.println("6. CONNECT TO GROUP");
            System.out.println("7. LOGOUT");
            System.out.println("8. EXIT");
            System.out.println("********--------------********");
            choose = scanner.nextInt();

            switch (choose)
            {
                case 1:
                    sendLogin();
                    break;
                case 2:
                    sendMessage();
                    break;
                case 3:
                    //POBIERANIE HISTORII
                    break;
                case 4:
                    sendFile();
                    break;
                case 5:
                    connectToUser();
                    break;
                case 6:
                    connectToGroup();
                    break;
                case 7:
                    sendLogout();
                    break;
                case 8:
                    System.out.println("Closing app...");
                    return;
            }
        }
    }

    public void sendMessage(){
        String mess;
        Scanner scanner = new Scanner(System.in);

        System.out.print("TYPE MESSAGE:");
        mess = scanner.next();
        sendMessage(mess, connectedEntityId);
    }

    public void sendLogin(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("TYPE LOGIN:");
        login = scanner.next();
        System.out.println("TYPE PASSWORD:");
        password = scanner.next();
        sender.output(login + ":" + password + ":" + Operation.LOGIN);
    }

    public void connectToUser(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("TYPE NAME OF USER:");
        connectedEntityId = scanner.nextLine();
    }

    public void connectToGroup(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("TYPE NAME OF GROUP:");
        connectedEntityId = scanner.nextLine();
        sender.output(login + ":" + password + ":" + Operation.JOIN_GROUP + ":" + connectedEntityId);
    }

    public void sendMessage(String message, String id){
        sender.output(login + ":" + password + ":" + Operation.SEND_MESSAGE + ":" + message + ":" + id);
    }

    public void sendFile(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("TYPE PATH TO FILE:");
        String path = scanner.next();
        System.out.println("TYPE NAME OF USER/GROUP WHERE YOU WANT TO SEND FILE");
        String id = scanner.next();
        //sender.sendFile(login + ":" + password + ":" + Operation.FILE_TRANSFER + ":" + id, path);
    }

    public void sendLogout(){
        sender.output(login + ":" + password + ":" + Operation.LOGOUT);
    }

    class Sender implements Runnable {

        private PrintWriter writer;
        private DataOutputStream dataOutput;
        private String toSend = null;

        public Sender(PrintWriter writer, DataOutputStream dataOutput)
        {
            this.writer = writer;
            this.dataOutput = dataOutput;
        }

        @Override
        public void run() {
            try {
            while (true) {
                if (toSend != null) {
                    writer.println(toSend);
                    writer.flush();
                    toSend = null;
                }
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void output(String message){
            sender.toSend = message;
        }

        public void sendFile(String message, String path){

        }
    }

    class Receiver implements Runnable {

        private BufferedReader reader;
        private DataInputStream dataInput;

        public Receiver(BufferedReader reader, DataInputStream dataInput){
            this.reader = reader;
            this.dataInput = dataInput;
        }

        @Override
        public void run() {
            while (true) {
                String line;
                try {
                    line = reader.readLine();
                    input(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void input(String message) throws IOException {
            String data[] = message.split(":");
            try {
                switch (Operation.valueOf(data[0])) {
                    case JOIN_GROUP_OK:
                        System.out.println("CONNECTED TO GROUP");
                        break;

                    case JOIN_GROUP_FAILED:
                        System.out.println("CONNECTING TO GROUP FAILED");
                        break;

                    case FILE_TRANSFER:
                        System.out.println("DATA TRANSFER");
                        //readFile(data[1]);
                        break;

                    case RECEIVE_MESSAGE:
                        System.out.println("RECEIVED MESSAGE -> " + data[1]);
                        break;

                    case FILE_TRANSFER_OK:
                        System.out.println("FILE TRANSFER OK");
                        break;

                    case FILE_TRANSFER_FAILED:
                        System.out.println("FILE TRANSFER FAILED");
                        break;

                    case LOGIN_OK:
                        System.out.println("LOGGED IN");
                        break;

                    case LOGIN_FAILED:
                        System.out.println("LOGIN FAILED");
                        break;
                }
            }
            catch (IllegalArgumentException iae)
            {
                System.out.println("UNDEFINDED RESPONSE -> " + message);
            }
        }

        public void readFile(String path){

        }
    }
}
