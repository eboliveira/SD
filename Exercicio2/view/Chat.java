package Exercicio2.view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Chat{
    private JPanel JPanelPrincipal;
    private JTextField message;
    private JButton enviarButton;
    private JTextArea messages;
    private DataInputStream in;
    private DataOutputStream out;

    public static void main(String[] args) {
        new Chat();
    }

    public Chat() {
        Socket serverSocket = null; // socket do cliente

        try{
            /* Endereço e porta do servidor */
            int serverPort = 6666;
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");

            /* conecta com o servidor */
            serverSocket = new Socket(serverAddr, serverPort);
            ServerThread server = new ServerThread(serverSocket, this);
            server.start();
            this.in = server.in;
            this.out = server.out;

        } catch (Exception e){
            try {
                serverSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);
            }
        }
        JFrame frame = new JFrame("Chat");
        frame.setContentPane(this.JPanelPrincipal);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(message.getText());
                message.setText("");
            }
        });
        message.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(message.getText());
                message.setText("");

            }
        });
    }

    public void newMessage(String msg){
        messages.setText(messages.getText() + msg + '\n');
    }

    public void sendMessage(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ServerThread extends Thread{
    DataInputStream in;
    DataOutputStream out;
    Socket serverSocket;
    Chat chat;

    public ServerThread(Socket serverSocket, Chat chat) {

        try {
            this.serverSocket = serverSocket;
            this.chat = chat;
            in = new DataInputStream(serverSocket.getInputStream());
            out = new DataOutputStream(serverSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } //catch
    } //construtor

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
            String buffer = "";
            while (true) {
                buffer = in.readUTF();   /* aguarda o envio de dados */
                chat.newMessage(buffer);
            }
        } catch (EOFException eofe) {
            eofe.printStackTrace();
        } catch (IOException ioe) {
        } finally {
            try {
                in.close();
                out.close();
                serverSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("Thread comunicação server finalizada.");
    } //run
}