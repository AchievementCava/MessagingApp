/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sai.messagingapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author saile
 */
public class Server implements Runnable {
    
    private ArrayList<ConnHandle> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    
    public Server() {
        connections = new ArrayList<>();
        done = false;
    }
    
    @Override
    //When a new connection is initiated, create a socket
    public void run() {
        try {
            
            ServerSocket server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
                
            while (!done) {
                Socket client = server.accept();
                ConnHandle handler = new ConnHandle(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception ex) {
            //pass
        }
    }
    
    public void broadcast(String message) {
        for (ConnHandle ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }
    
    
    class ConnHandle implements Runnable {
        
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nick;
        
        public ConnHandle(Socket client) {
            this.client = client;
        }
        
        @Override
        public void run() {
            try {
                
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                
                //while (!(functions.isValid(nick))) {
                    out.println("Please enter a nickname: ");
                    nick = in.readLine();
                //}
                
                String joinMsg = nick + " joined the chat!";
                System.out.println(joinMsg);
                broadcast(joinMsg);
                
                
                //user joined chat
                
                String message;
                while ((message = in.readLine()) != null) {
                    
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        
                        if (messageSplit.length == 2) {
                            
                            String msg = nick + " renamed themselves to " + messageSplit[1];
                            nick = messageSplit[1];
                            broadcast(msg);
                            System.out.println(msg);
                            
                        } else {
                            
                            out.println("Server: Please provide a nickname with no spaces");
                            
                        }
                    } else if (message.startsWith("/quit")) {
                        
                        broadcast(nick + " " + "left the chat...");
                        shutdown();
                        
                    } else broadcast(nick + ": " + message);
                }
            } catch (IOException e) {
                shutdown();
            }
        }
        
        public void sendMessage(String message) {
            out.println(message);
        }
        
        public void shutdown() {
            try {
                done = true;
                pool.shutdown();
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException ex) {
                //ignore
            }
        }
        
    }
    
    class functions {
        
        //checks if nickname is acceptable
        static boolean isValid(String nick) {
            
            if (nick.equals(" ")) return false;
            
            return true;
            
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Start");
        Server server = new Server();
        server.run();
        System.out.println("Thread start");
    }
}
