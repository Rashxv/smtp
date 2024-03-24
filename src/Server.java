import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Scanner;

public class Server{ //server
    private static final HashSet<String> VALID_EMAILS = new HashSet<>(); // Set to store valid email addresses

    static {
        // Add valid email addresses to the set
        VALID_EMAILS.add("r@kok.com");
        VALID_EMAILS.add("a@kok.com");
        VALID_EMAILS.add("s@kok.com");
        VALID_EMAILS.add("$a@kok.com");
    }
    public static void main(String[] args) throws FileNotFoundException{
        // Initializing DatagramSocket
        DatagramSocket serverSocket = null;

        try {
            // Creating DatagramSocket to listen on port 1111
            serverSocket = new DatagramSocket(1111);
            byte[] receiveData = new byte[1024];
            String receiverMail = "";
            boolean senderActive = false;
            boolean receiverActive = false;

            InetAddress senderAddress = null;
            InetAddress receiverAddress = null;
            int senderPort = 0;
            int receiverPort = 0;

            int senderNum = 1;
            int receiverNum = 1;

            InetAddress IP = InetAddress.getLocalHost();
            System.out.println("Mail Server Starting at host: "+ IP.getHostName()); //prints server hostname
            System.out.println("Server is listening on port 1111...");
            System.out.println("Waiting to be contacted by sender and receiver for transferring Mails...");

            while (true) { //infinite loop, Server stays on
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //empty packet

                serverSocket.receive(receivePacket); //receive first SYN message
                InetAddress clientAddress = receivePacket.getAddress(); //take address from packet
                //int clientPort = receivePacket.getPort(); //take the port number from packet
                
                while (!senderActive || !receiverActive)
                {
                    String syn = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (syn.contains("SYN-Sender") && !senderActive)
                    {
                        
                        System.out.println("SYN received");
                        System.out.println("Sending ACK");
                        senderAddress = receivePacket.getAddress();
                        senderPort = receivePacket.getPort();
                        System.out.println("sender port: " + senderPort);
                        send_message("ACK", senderAddress, senderPort, serverSocket);
                        serverSocket.receive(receivePacket);
                        String ackack = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (ackack.contains("ACK ACK"))
                        {
                            System.out.println("ACK ACK received");
                            senderActive = true;
                        }
                    }
                    else if (syn.contains("SYN-Receiver") && !receiverActive)
                    {
                        System.out.println("SYN received");
                        System.out.println("Sending ACK");
                        receiverAddress = receivePacket.getAddress();
                        receiverPort = receivePacket.getPort();
                        String s1[] = syn.split("SYN-Receiver");
                        receiverMail = s1[1];
                        System.out.println("receiver port: " + receiverPort);
                        System.out.println("Client receiver mail: " + receiverMail);
                        send_message("ACK", receiverAddress, receiverPort, serverSocket);
                        serverSocket.receive(receivePacket);
                        String ackack = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (ackack.contains("ACK ACK"))
                        {
                            System.out.println("ACK ACK received");
                            receiverActive = true;
                        }
                    }
                    serverSocket.receive(receivePacket);
                }
                
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (message.equals("FIN"))
                {
                    System.out.println("FIN received");
                    System.out.println("Sending ACK");
                    send_message("ACK", senderAddress, senderPort, serverSocket);
                    serverSocket.receive(receivePacket);
                    String termString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (termString.equals("ACK ACK"))
                    {
                        System.out.println("ACK ACK received");
                        System.out.println("Terminating");
                        senderActive = false;
                        continue ;
                    }
                }

                System.out.println("Email received");
                String a1[] = message.split("TO:");
                String a2[] = a1[1].split("FROM:");
                String to = a2[0];
                String a3[] = a2[1].split("SUBJECT:");
                String from = a3[0];
                String a4[] = a3[1].split("SEQ:");
                String subject = a4[0];
                String a5[] = a4[1].split("BODY:");
                senderNum = Integer.parseInt(a5[0]);
                String a6[] = a5[1].split("HOST:");
                String body = a6[0];
                String hostname = a6[1];

                System.out.println("(Sender) Sending ACK:" + (senderNum + receivePacket.getLength()));
                send_message("ACK:" + (senderNum + receivePacket.getLength()), senderAddress, senderPort, serverSocket);
                System.out.println("Mail Received from " + hostname); //print the hostname of the address
                String directoryPath = "./mails/";
                String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-");
                String filename = timestamp + ".txt";
                String relativeFilePath = directoryPath + filename;
                File directory = new File(directoryPath);
                directory.mkdirs();

                System.out.println("FROM: " + from);
                System.out.println("TO: " + to);
                System.out.println("SUBJECT: " + subject);
                System.out.println("TIME: " + timestamp);
                System.out.println("SEQ: " + senderNum);
                System.out.println(body);

                boolean found = false;
                for (String s: VALID_EMAILS) //for each loop, checks if "TO" is an email in server
                {
                    if (s.equalsIgnoreCase(to))
                    {
                        found = true;
                        break ;
                    }
                }

                if (isValidEmailAddresses(to,from) && (found || to.equalsIgnoreCase(receiverMail)))
                {
                    System.out.println("The Header fields are verified.");
                    System.out.println("Sending mail to client receiver...");
                    if (to.equalsIgnoreCase(receiverMail))
                    {
                        String receiverMessage = "TO:" + to + "FROM:" + from + "SUBJECT:" + subject + "SEQ:" + receiverNum + "BODY:" + body + "HOST:" + hostname;
                        send_message(receiverMessage, receiverAddress, receiverPort, serverSocket);
                        serverSocket.receive(receivePacket);
                        String ackString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (ackString.contains("ACK"))
                        {
                            String tempStr[] = ackString.split("ACK:");
                            receiverNum = Integer.parseInt(tempStr[1]);
                            System.out.println("(Receiver) ACK:" + receiverNum + " received");
                        }
                        else
                            System.out.println("ACK error");
                    }
                    
                    System.out.println("Sending 250 Ok");
                    String confirmation = "250 OK:" + timestamp;

                    send_message(confirmation, senderAddress, senderPort, serverSocket);
                    serverSocket.receive(receivePacket);
                    String ackString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (ackString.contains("ACK"))
                    {
                        System.out.println("ACK received");
                    }
                    else
                    {
                        System.out.println("ACK error");
                    }

                    File f = new File(relativeFilePath);

                    PrintWriter fout = new PrintWriter(f);
                    fout.println("FROM: " + from);
                    fout.println("TO: " + to);
                    fout.println("SUBJECT: " + subject);
                    fout.println("TIME: " + timestamp);
                    fout.println(body);
                    fout.close();

                    System.out.println("mail sent to client at " + hostname + ":" + clientAddress);
                }
                else if (!isValidEmailAddresses(to,from)){
                    System.out.println("The Header fields are not valid.");
                    System.out.println("Sending 501 Error");

                    String confirmation = "501 Error";

                    send_message(confirmation, senderAddress, senderPort, serverSocket);
                    serverSocket.receive(receivePacket);
                    String ackString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (ackString.contains("ACK"))
                    {
                        System.out.println("ACK received");
                    }
                    else
                    {
                        System.out.println("ACK error");
                    }
                }
                else if (!to.equals(receiverMail) || !found)
                {
                    System.out.println("Email address does not exist");
                    System.out.println("Sending 505 Error");

                    String confirmation = "505 Error";

                    send_message(confirmation, senderAddress, senderPort, serverSocket);
                    serverSocket.receive(receivePacket);
                    String ackString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (ackString.contains("ACK"))
                    {
                        System.out.println("ACK received");
                    }
                    else
                    {
                        System.out.println("ACK error");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
    // Check if both "From" and "To" email addresses are valid
    private static boolean isValidEmailAddresses(String fromAddress, String toAddress) {
        return isValidEmail(fromAddress) && isValidEmail(toAddress);
    }
     // Check if the provided email address is valid
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Split using "@" symbol to separate username and domain parts
        String[] parts = email.split("@", 2);
        if (parts.length != 2) {
            return false; // Must have exactly one "@" symbol
        }
        // Check username validity (allow letters, numbers, periods, hyphens, and
        // underscores)
        String username = parts[0];
        if (!username.matches("^[\\w\\.-]+$")) {
            return false;
        }
        // Check domain validity (allow letters, numbers, hyphens, and periods)
        String domain = parts[1];
        if (!domain.matches("^[\\w\\.-]+$")) {
            return false;
        }
        return true;
    }
    static void send_message(String message, InetAddress serverAddress, int portNumber, DatagramSocket clientSocket)
    {
        try{
            byte[] sendData = message.getBytes(); //convert message to bytes
            int messageBytes = message.getBytes().length; //byte length of request message
            String byteSize = Integer.toString(messageBytes); //parsing int to string
            System.out.println("Message is sending " + byteSize + " Bytes"); //print bytes length
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, portNumber); //create a packet
            clientSocket.send(sendPacket); //send packet(Bytes, Bytes length, address to send, port number)
        } catch(IOException e) { //catch IOException
            e.printStackTrace();
        }
    }
}

