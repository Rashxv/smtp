import java.io.*;
import java.util.*;
import java.net.*;

public class Client{ //Client (Email writter)
    public static void main(String[] args) {
        DatagramSocket clientSocket = null; //create an empty socket
        Scanner console = new Scanner(System.in); //for user input

        int sequenceNum = 1;

        try { //error handler to catch io errors
            InetAddress IP = InetAddress.getLocalHost();
            String hostname = IP.getHostName();
            System.out.println("Mail Client Starting at host: "+ hostname); //prints the hostname (DESKTOP-XXXX)
            
            InetAddress serverAddress = null;
            while (true) { //loop for server hostname input
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
            send_message("SYN-Sender" , serverAddress, serverPort, clientSocket);
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

            while (true) {
                // Create email
                System.out.println("Creating New Email.."); //mail inputs
                System.out.print("To: ");
                String to = console.nextLine();
                System.out.print("From: ");
                String from = console.nextLine();
                System.out.print("Subject: ");
                String subject = console.nextLine();
                System.out.print("Body: ");
                String body = console.nextLine();

                String request = "TO:" + to + "FROM:" + from + "SUBJECT:" + subject + "SEQ:" + sequenceNum + "BODY:" + body + "HOST:" + hostname; //request message

                send_message(request, serverAddress, serverPort, clientSocket); //calls send_message function (bottom)

                System.out.println("Mail Sent to Server, waiting...");
                clientSocket.receive(receivePacket); //save received packet (ACK)
                String ackString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (ackString.contains("ACK"))
                {
                    String tempStr[] = ackString.split("ACK:");
                    sequenceNum = Integer.parseInt(tempStr[1]);
                    System.out.println("ACK:" + sequenceNum + " received");
                }
                else 
                {
                    System.out.println("ACK error");
                }

                clientSocket.receive(receivePacket);
                String confirmation = new String(receivePacket.getData(), 0, receivePacket.getLength()); //convert packet (bytes) to string
                if (confirmation.contains("250 OK")) //packet received successfully
                {
                    String timestamp[] = confirmation.split("250 OK:"); //split the message and take whats after "250 OK:" (the timestamp)
                    System.out.println("Email received successfully at " + timestamp[1]);
                    System.out.println("Sending ACK");
                    send_message("ACK", serverAddress, serverPort, clientSocket);
                    
                }
                else if (confirmation.contains("501 Error")) //packet failed
                {
                    System.out.println("501 Error");
                    System.out.println("Header files are invalid"); //add option to quit or continue
                    System.out.println("Sending ACK");
                    send_message("ACK", serverAddress, serverPort, clientSocket);
                }
                else if (confirmation.contains("505 Error"))
                {
                    System.out.println("505 Error");
                    System.out.println("Email does not exist");
                    System.out.println("Sending ACK");
                    send_message("ACK", serverAddress, serverPort, clientSocket);
                }
                else //unkown error
                { 
                    System.out.println("Unknown Error");
                    System.out.println("Sending ACK");
                    send_message("ACK", serverAddress, serverPort, clientSocket);
                    break ; //quit loop
                }
                System.out.println("Do you want to quit? (quit/no): ");
                boolean quitvalid = false;
                while(true)
                {
                    String quitter = console.nextLine();
                    if (quitter.contains("quit")) //quit the loop and end program
                    {
                        System.out.println("Quiting");
                        quitvalid = true;
                        break ;
                    }
                    else if (quitter.contains("no")) //continue the loop
                    {
                        quitvalid = false;
                        break ;
                    }
                    else
                    {
                        System.out.print("Invalid input, re-enter: "); //quit
                    }
                }
                if (quitvalid)
                {
                    //send terminate, get ack, send ack ack
                    System.out.println("Sending FIN");
                    send_message("FIN", serverAddress, serverPort, clientSocket);
                    clientSocket.receive(receivePacket);
                    String ackTerm = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (ackTerm.contains("ACK"))
                    {
                        System.out.println("ACK received");
                        System.out.println("Sending ACK ACK");
                        send_message("ACK ACK", serverAddress, serverPort, clientSocket);
                        break ;
                    }
                    else
                    {
                        System.out.println("ACK Error");
                    }

                }
                else
                    continue ;
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