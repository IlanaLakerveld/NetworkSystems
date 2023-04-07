package com.nedap.university.Client;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Scanner;


public class ClientTUI {

    static String help = "Here some explanations to use the TUI. The commands you can use are : " +
            "GET~filename  to get a file from the server " +
            "SEND~filename  to send a file from the server" +
            "DELETE~filename to remove a filename from the server+ " +
            "LISTFILES";


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client=new Client();;
        System.out.println(help);

        System.out.println("type your input");
        // je mag niet eerst een data connectie hebben om dit te kunnen doen moet je dan specifiek iets neerzetten wat dan ook de port enzo weergeeft?


        String input = scanner.nextLine();
        String[] splittedLine = input.split("~");
        switch (splittedLine[0]) {
            case "GET" -> getRequest(client,splittedLine[1]);
            case "SEND" -> sendRequest(splittedLine[1]);
            case "DELETE" -> deleteRequest(splittedLine[1]);
            case "LISTFILES" -> getList();

            default -> System.out.println("Do not understand this line :" + input);
        }


    }


    private static void getRequest(Client client,String filename) {
        try {
            client.getRequest(filename);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        //toDO
    }

    private static void sendRequest(String filename) {
        //toDO
    }

    private static void deleteRequest(String filename) {
        //toDO
    }

    private static void getList(){
        //toDO
    }
}
