package com.nedap.university.Client;

import com.nedap.university.Exeptions.FileNotExistException;
import com.nedap.university.Exeptions.ServerGivesErrorException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class ClientTUI {

    static String help = "Here some explanations to use the TUI.\nThe commands you can use are :\n" +
            "GET~filename  to get a file from the server\n" +
            "SEND~filename  to send a file from the server\n" +
            "DELETE~filename to remove a filename from the server\n" +
            "LISTFILES to get al list of posible files";


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        //TOdo change that client has input for the server.

        InetAddress serveradress = getInetAddress(scanner);
        int portnumber = getPortNumber(scanner);
        Client client = new Client(portnumber,serveradress);

        System.out.println(help);

        while (true) {
            System.out.println("type your input");

            String input = scanner.nextLine();
            String[] splittedLine = input.split("~");
            switch (splittedLine[0]) {
                case "GET" -> getRequest(client, splittedLine[1]);
                case "SEND" -> sendRequest(client, splittedLine[1]);
                case "DELETE" -> deleteRequest(client,splittedLine[1]);
                case "LISTFILES" -> getList();
                case "HELP" -> System.out.println(help);

                default -> System.out.println("Do not understand this line :" + input);
            }

            System.out.println("If you want to quit the program type : quit.");
            if (scanner.nextLine().equalsIgnoreCase("quit")) {
                break;
            }
        }


    }




    private static void getRequest(Client client, String filename) {
        client.getRequest(filename);
    }

    private static void sendRequest(Client client, String filename) {
        try {
            client.sendRequest(filename);
        } catch (FileNotExistException e) {
            System.out.println("File does not exist");
        }catch (ServerGivesErrorException e){
            System.out.println("error from server task not completed");
        }
    }

    private static void deleteRequest(Client client, String filename) {
        try {
            client.deleteRequest(filename);
        } catch (FileNotExistException e) {
            System.out.println("The file is not deleted");
        }
    }

    private static void getList() {
        System.out.println("not implemented yet");
        //toDO
    }


    /**
     * Get the InetAddress from user input.
     * Checks if the input is am inetAddress.
     * @return InetAddress
     */
    private static InetAddress getInetAddress(Scanner scanner) {
        boolean okeInputAddress = false;
        InetAddress addressServer = null;
        while (!okeInputAddress) {
            System.out.println("what server address do you want? ");
            String inputAddress = scanner.nextLine();

            try {

                addressServer = InetAddress.getByName(inputAddress);
                okeInputAddress = true;
            } catch (UnknownHostException e) {
                System.out.println("please choose a correct input address");

            }

        }
        return addressServer;
    }

    /**
     * Get the port number from user input and checks it's an  integer.
     * @return portNumber
     */
    private static int getPortNumber(Scanner scanner) {

        int port;
        while (true) {
            System.out.println("Import the port number of the server");
            String number = scanner.nextLine();

            try {
                port = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                System.out.println("This is not a number, please enter a number");

                continue;
            }

            if (port < 1 || port > 65535) {
                System.out.println("Port number should be between 1 and 65535");

            } else {
                break;
            }

        }
        return port;
    }

}
