import java.io.*;
import java.util.*;
import java.net.*;

public class RClient{ //Client (Email writter)
    public static void main(String[] args) {

        DatagramSocket clientSocket = null; //create an empty socket
        Scanner console = new Scanner(System.in); //for user input
        String receiverMail;

        int sequenceNum = 0;

        try { //error handler to catch io errors
            InetAddress IP = InetAddress.getLocalHost();
            String hostname = IP.getHostName();
            System.out.println("Mail Client Starting at host: "+ hostname); //prints the hostname (DESKTOP-XXXX)
            
            InetAddress serverAddress = null;
            while(true)
            {
                System.out.print("Enter your email address: ");
                receiverMail = console.nextLine();
                if (!isValidEmail(receiverMail))
                {
                    System.out.println("invalid email");
                }
                else
                    break;
            }
            while (true) 
            { //loop for server hostname input
                try {
                    System.out.print("Type name of Mail servers: ");
                    String mailServer = console.nextLine(); //get user input for server hostname
                    serverAddress = InetAddress.getByName(mailServer); //save mail server ip to server address
                    break ; //mail server is valid, exit loop
                } catch (UnknownHostException ex) { //catch invalid hostname in InetAddress
                    System.out.println("Unknown host name");
                }
            }
            clientSocket = new DatagramSocket(); //create empty socket object
            int serverPort = 1111;
            System.out.println("Sending SYN...");
            send_message("SYN-Receiver" + receiverMail, serverAddress, serverPort, clientSocket);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String ackMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (ackMessage.contains("ACK"))
            {
                System.out.println("ACK received");
                System.out.println("Sending ACK ACK...");
                send_message("ACK ACK", serverAddress, serverPort, clientSocket);
            }
            else
            {
                System.out.println("ACK Error");
            }

            while(true) //infinite loop until break
            {
                clientSocket.receive(receivePacket);
                //System.out.println(mail); //mail add later
                String mail = new String(receivePacket.getData(), 0, receivePacket.getLength());
                
                String a1[] = mail.split("TO:");
                String a2[] = a1[1].split("FROM:");
                String to = a2[0];
                String a3[] = a2[1].split("SUBJECT:");
                String from = a3[0];
                String a4[] = a3[1].split("SEQ:");
                String subject = a4[0];
                String a5[] = a4[1].split("BODY:");
                sequenceNum = Integer.parseInt(a5[0]);
                String a6[] = a5[1].split("HOST:");
                String body = a6[0];

                System.out.println("Mail Received from " + from); //print the hostname of the address
                String directoryPath = "./localMails/";
                String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-");
                String filename = subject + "_" + timestamp + ".txt";
                String relativeFilePath = directoryPath + filename;
                File directory = new File(directoryPath);
                directory.mkdirs();

                System.out.println("FROM: " + from);
                System.out.println("TO: " + to);
                System.out.println("SUBJECT: " + subject);
                System.out.println("TIME: " + timestamp);
                System.out.println(body);

                System.out.println("Sending ACK:" + (sequenceNum + receivePacket.getLength()));
                send_message("ACK:" + (sequenceNum + receivePacket.getLength()), serverAddress, serverPort, clientSocket);

                File f = new File(relativeFilePath);

                PrintWriter fout = new PrintWriter(f);
                fout.println("FROM: " + from);
                fout.println("TO: " + to);
                fout.println("SUBJECT: " + subject);
                fout.println("TIME: " + timestamp);
                fout.println(body);
                fout.close();
                
            }
        } catch (IOException e) { //catch IOException (Inputs, files error)
            e.printStackTrace(); //print where the error was
        } finally {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close(); //close socket after code ends
            }
            console.close();
        }
    }
    
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

    static void send_message(String message, InetAddress serverAddress, int portNumber, DatagramSocket currentSocket)
    {
        try{
            byte[] sendData = message.getBytes(); //convert message to bytes
            int messageBytes = message.getBytes().length; //byte length of request message
            String byteSize = Integer.toString(messageBytes); //parsing int to string
            System.out.println("Message is sending " + byteSize + " Bytes"); //print bytes length
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, portNumber); //create a packet
            currentSocket.send(sendPacket); //send packet(Bytes, Bytes length, address to send, port number)
        } catch(IOException e) { //catch IOException
            e.printStackTrace();
        }
    }
}