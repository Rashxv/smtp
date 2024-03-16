import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

public class Server {
    // Constants
    private static final int SERVER_PORT = 54;
    private static final String SAVE_DIRECTORY = "C:\\Users\\USER\\Codes\\oop_codes\\smtp\\emails\\";
    private static final HashSet<String> VALID_EMAILS = new HashSet<>(); // Set to store valid email addresses
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE. MMM. d, yyyy HH:mm");
    static {
        // Add valid email addresses to the set
        VALID_EMAILS.add("r@kok.com");
        VALID_EMAILS.add("a@kok.com");
        VALID_EMAILS.add("s@kok.com");
        VALID_EMAILS.add("$a@kok.com");
    }

    public static void main(String[] args) throws IOException {
        // Set up server socket
        DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
        System.out.println("Mail Server Starting at host: " + InetAddress.getLocalHost().getHostName());
        System.out.println("Waiting to be contacted for transferring Mail...");
        while (true) {
            try {
                // Receive email
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String email = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Validate email addresses
                String fromAddress = extractFromAddress(email);
                String toAddress = extractToAddress(email);
                if (isValidEmailAddresses(fromAddress, toAddress)) {
                    if (isValidRecipient(toAddress) && isValidRecipient(fromAddress)) {
                        // Process and save email
                        processEmail(email, receivePacket.getAddress(), receivePacket.getPort(), serverSocket);
                        sendConfirmationMessage(serverSocket, receivePacket.getAddress(), receivePacket.getPort(),
                                true);
                    } else {
                        // Send error message if recipient not found
                        sendConfirmationMessage(serverSocket, receivePacket.getAddress(), receivePacket.getPort(),
                                false, "Recipient email not found.");
                    }
                } else {
                    // Send error message if invalid email format
                    sendConfirmationMessage(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), false,
                            "Invalid email format.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Process received email and save to file
    private static void processEmail(String email, InetAddress clientAddress, int clientPort,
            DatagramSocket serverSocket) throws IOException {
        // Extract header and body (assuming simple format)
        String[] parts = email.split("\n\n", 2);
        String header = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        // Extract information from header
        String fromAddress = extractFromAddress(header);
        String toAddress = extractToAddress(header);

        // Display Mail Header Fields
        displayMailHeader(clientAddress, fromAddress, toAddress, header);

        // Save email to file
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = SAVE_DIRECTORY + "email_" + timestamp + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Save header and body separately
            writer.println("HEADER:");
            writer.println(header);
            writer.println("BODY:");
            writer.println(body);
        }

        System.out.println("Email saved to file: " + fileName);
    }

    // Display Mail Header Fields
    private static void displayMailHeader(InetAddress clientAddress, String fromAddress, String toAddress,
            String header) {
        System.out.println("Mail Received from " + clientAddress.getHostName());
        System.out.println(header); // Display the rest of the header
        System.out.println("TIME: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    // Send confirmation message to the client
    private static void sendConfirmationMessage(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort,
            boolean success, String... errorMessages) throws IOException {
        String message;
        if (success) {
            System.out.println("The Header fields are verified.\nSending \"250 OK\"");
            // Send success message with timestamp
            message = "250 OK\nEmail received successfully at " + LocalDateTime.now().toString();
        } else {
            System.out.println("The Header fields are not valid.\r\nSending \"501 Error\"");
            // Send error message with specific error details
            StringBuilder errorMessageBuilder = new StringBuilder("501 Error\n");
            for (String errorMessage : errorMessages) {
                errorMessageBuilder.append(errorMessage).append("\n");
            }
            message = errorMessageBuilder.toString();
        }
        // Convert message to bytes and send to the client
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        serverSocket.send(sendPacket);
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

    private static String extractFromAddress(String header) {
        // Assuming simple format, extract the line starting with "FROM:"
        String[] lines = header.split("\n");
        for (String line : lines) {
            if (line.startsWith("FROM:")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }

    // Extract recipient email address from the email content
    private static String extractToAddress(String header) {
        // Assuming simple format, extract the line starting with "TO:"
        String[] lines = header.split("\n");
        for (String line : lines) {
            if (line.startsWith("TO:")) {
                return line.substring(3).trim();
            }
        }
        return null;
    }

    // Check if the recipient email is valid
    private static boolean isValidRecipient(String email) {
        return VALID_EMAILS.contains(email);
    }
}