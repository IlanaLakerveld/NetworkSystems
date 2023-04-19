package com.nedap.university.Client;

import com.nedap.university.Exeptions.FileNotExistException;
import com.nedap.university.Exeptions.ServerGivesErrorException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Textual User Interface for the client.
 * Via command lines the user can ask for different task to the server.
 * Before asking for these commands the first the InetAddress and Port are asked.
 */

public class ClientTUI {

    static String help = """
            Here some explanations to use the TUI.
            The commands you can use are :
            GET~filename  to get a file from the server
            SEND~filename  to send a file from the server
            DELETE~filename to remove a filename from the server
            REPLACE~filename to replace a file
            LISTFILES to get al list of possible files
            HELP to get this information again
            QUIT stop the program""";


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Ask information about the server you want to connect to.
        InetAddress serverAddress = getInetAddress(scanner);
        int portNumber = getPortNumber(scanner);
        Client client = new Client(portNumber, serverAddress);

        System.out.println(help);
        boolean running = true;
        while (running) {
            System.out.println("Type your input");

            String input = scanner.nextLine();
            String[] splitLine = input.split("~");

            switch (splitLine[0].toUpperCase()) {
                case "GET" -> {
                    if (splitLine.length == 1) {
                        System.out.println("Please add a filename");
                    } else {
                        getRequest(client, splitLine[1]);
                    }
                }
                case "SEND" -> {
                    if (splitLine.length == 1) {
                        System.out.println("Please add a filename");
                    } else {
                        sendRequest(client, splitLine[1]);
                    }
                }
                case "DELETE" -> {
                    if (splitLine.length == 1) {
                        System.out.println("Please add a filename");
                    } else {
                        deleteRequest(client, splitLine[1]);
                    }
                }
                case "REPLACE" -> {
                    if (splitLine.length == 1) {
                        System.out.println("Please add a filename");
                    } else {
                        replaceRequest(client, splitLine[1]);
                    }

                }
                case "LISTFILES" -> getList(client);
                case "HELP" -> System.out.println(help);
                case "QUIT" -> running = false;

                default -> System.out.println("Do not understand this line :" + input);
            }


        }


    }

    private static void getRequest(Client client, String filename) {
        try {
            client.getRequest(filename);
        } catch (IOException e) {
            System.out.println("Something is wrong with the socket so request could be handled ");
        } catch (ServerGivesErrorException e) {
            System.out.println("Error from server get " + filename + "failed");
        }

    }

    private static void replaceRequest(Client client, String filename) {
        try {
            client.replaceRequest(filename);
        } catch (FileNotExistException e) {
            System.out.println("File does not exist on the server ");
        } catch (IOException e) {
            System.out.println("Something is wrong with the socket so request could be handled ");
        }
    }

    private static void sendRequest(Client client, String filename) {
        try {
            client.sendRequest(filename);
        } catch (FileNotExistException e) {
            System.out.println("File does not exist");
        } catch (ServerGivesErrorException e) {
            System.out.println("error from server task not completed");
        } catch (IOException e) {
            System.out.println("Something is wrong with the socket so request could be handled ");
        }
    }

    private static void deleteRequest(Client client, String filename) {
        try {
            client.deleteRequest(filename);
        } catch (FileNotExistException e) {
            System.out.println("The file is not deleted");
        } catch (IOException e) {
            System.out.println("Something is wrong with the socket so request could be handled ");
        }
    }

    private static void getList(Client client) {
        try {
            client.getListRequest();
        } catch (IOException e) {
            System.out.println("Something wrong with the connection, so you can not get the list. ");
        } catch (ServerGivesErrorException e) {
            System.out.println("List not returned");
        }
    }


    /**
     * Get the InetAddress from user input.
     * Checks if the input is am inetAddress.
     *
     * @return InetAddress
     */
    private static InetAddress getInetAddress(Scanner scanner) {
        boolean okeInputAddress = false;
        InetAddress addressServer = null;
        while (!okeInputAddress) {
            System.out.println("What server address do you want? ");
            String inputAddress = scanner.nextLine();

            try {

                addressServer = InetAddress.getByName(inputAddress);
                okeInputAddress = true;
            } catch (UnknownHostException e) {
                System.out.println("Please choose a correct input address");

            }

        }
        return addressServer;
    }

    /**
     * Get the port number from user input and checks it's an  integer.
     *
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
