
/**
 * TCPClient: Cliente para conexao TCP
 * Descricao: Envia uma informacao ao servidor e recebe confirmações ECHO
 * Ao enviar "PARAR", a conexão é finalizada.
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {
	public static void main (String args[]) {
	        Socket clientSocket = null; // socket do cliente
            Scanner reader = new Scanner(System.in); // ler mensagens via teclado

            try{
                /* Endereço e porta do servidor */
                int serverPort = 6666;
                InetAddress serverAddr = InetAddress.getByName("127.0.0.1");

                /* conecta com o servidor */
                clientSocket = new Socket(serverAddr, serverPort);
                ServerThread server = new ServerThread(clientSocket);
                server.start();

	    } catch (Exception e){
                try {
                    clientSocket.close();
                } catch (IOException ioe) {
                    System.out.println("IO: " + ioe);
                }
            }
     } //main
} //class

class ServerThread extends Thread{
    DataInputStream in;
    DataOutputStream out;
    Socket serverSocket;
    Scanner reader = new Scanner(System.in); // ler mensagens via teclado
    boolean flag = false;
    String shared_path = System.getProperty("user.dir") + "/shared2/";

    public ServerThread(Socket serverSocket) {

        try {
            this.serverSocket = serverSocket;
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
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if(flag){
                            break;
                        }
                        String buffer = "";
                        buffer = reader.nextLine();
                        try {
                            out.writeUTF(buffer);
                            if(buffer.equals("EXIT")){
                                serverSocket.close();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            t1.setDaemon(true);
            t1.start();
            while (true) {
                buffer = in.readUTF();   /* aguarda o envio de dados */
                if (buffer.equals("EXIT")) {
                    flag = true;
                    break;
                }
                else if(buffer.equals("DOWNLOAD")){
                    buffer = in.readUTF();
                    if(!buffer.equals("ARQUIVO NAO EXISTE")){
                        File path = new File(shared_path + buffer);
                        path.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(path);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        int file_len = in.readInt();
                        byte[] bytes = new byte[file_len];
                        int bytes_read = in.read(bytes, 0, bytes.length);
                        int current = bytes_read;
                        do{
                            bytes_read = in.read(bytes, current, (bytes.length-current));
                            if(bytes_read>=0){
                                current+=bytes_read;
                            }
                        }while(bytes_read>0);
                        bufferedOutputStream.write(bytes,0,current);
                        bufferedOutputStream.flush();
                        System.out.println("Arquivo concluído");
                    }
                }
                System.out.println("Server disse: " + buffer);

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

