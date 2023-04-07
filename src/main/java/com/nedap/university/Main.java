package com.nedap.university;

import com.nedap.university.MakeAck;
import com.nedap.university.Reveiver;
import com.nedap.university.Sending;
import com.nedap.university.Server.fileDoesNotExistError;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    private byte[] buffer = new byte[512];
//    public QuoteServer(int port) throws SocketException {
//        socket = new DatagramSocket(port);
//        random = new Random();
//    }
    private Main() {}

    public static void main(String[] args) {

        running = true;
        System.out.println("Hello, Nedap University! ilana ");
        int port = 0;
        DatagramSocket socket ;
        try {
             socket = new DatagramSocket(port) ;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("local port is : " + socket.getLocalPort())  ;
        initShutdownHook();

        while (keepAlive) {
            try {
                byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
                DatagramPacket request = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
                socket.receive(request) ;
                System.out.println(request.getPort());
                System.out.println(request.getAddress());
                System.out.println(request.getData());
                //byte[] slice = Arrays.copyOfRange(request.getData(), 0,request.getLength() ) ;
                String a= new String(buffer,0, request.getLength());


                // kijk of je iets binnen krijgt
                // als je iets binnen krijgt handel het af
                    // opties  :
                    // Een nieuwe aanvraag : GET, SEND, REMOVE, LIST
                            // BIj GET or REMOVE moet je checken of bestand uberhoud wel bestaat
                                // als bestaat die speciefieke dingen uitvoeren
                                // als bestand niet bestaat, geef een fout melding terug.
                            // als send is en bestaat als dan kan vragen of je wil replacen of moet echt replace command zijn
                            // List moet de server een lijst maken met alle files, en die sturen als txt?


                    // als bij iets hoort wat al wel bestaat??
                            // zorg dat bij juiste tread komt
                    // als onzin is ? negeer deze onzin

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Stopped");
        running = false;
    }

    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }


    private void GETAnswers(String filename) throws fileDoesNotExistError {
        File file = new File(filename);
        if(!file.exists()){
            throw new fileDoesNotExistError() ;
        }
        else{
            Sending.Sending();
        }
    }

    private void Send(String filename){
        File file = new File(filename);
        if(file.exists()){
            // todo maak hier iets dat zegt met een vraag wil je dit overschijven?
        }
        else{
            // todo make an acknowlegdement  before go into receiving mode.
            MakeAck.MakeAck(1);
            Reveiver.receiver(filename);
        }
    }
}
