/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */
import org.omg.SendingContext.RunTime;

import java.net.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Scanner;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            /* cria um socket e mapeia a porta para aguardar conexao */
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Servidor aberto...");

                /* aguarda conexoes */
                Socket clientSocket = listenSocket.accept();

                System.out.println("Cliente conectado ... Criando thread ...");

                /* cria um thread para atender a conexao */
                ClientThread c = new ClientThread(clientSocket);

                /* inicializa a thread */
                c.start();
            } //while

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } //catch
    } //main
} //class

/**
 * Classe ClientThread: Thread responsavel pela comunicacao
 * Descricao: Rebebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */
class ClientThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    String shared_path = System.getProperty("user.dir") + "/shared/";

    public ClientThread(Socket clientSocket) {

        try {
            this.clientSocket = clientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } //catch
    } //construtor

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
            DateTimeFormatterBuilder formatter = new DateTimeFormatterBuilder();
            String buffer = "";
            String resp = "";
            while (true) {
                buffer = in.readUTF();   /* aguarda o envio de dados */
                if (buffer.equals("EXIT")) {
                    break;
                }
                else if(buffer.equals("TIME")){

                    DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
                    resp = format.format(LocalDateTime.now());
                    out.writeUTF(resp);
                }
                else if(buffer.equals("DATE")){
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/YYYY");
                    resp = format.format(LocalDateTime.now());
                    out.writeUTF(resp);
                }
                else if(buffer.equals("FILES")){
                    ProcessBuilder p = new ProcessBuilder("ls", shared_path);
                    Process process = p.start();

                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(process.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(process.getErrorStream()));

                    // read the output from the command
                    System.out.println("Standard output of the command:\n");
                    int file_count = 0;
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        out.writeUTF(s);
                        file_count+=1;
                    }
                    out.writeUTF("Quantidade de arquivos: " + String.valueOf(file_count));

                }
                else if(buffer.length() > 5){
                    if(buffer.substring(0,4).equals("DOWN")){
                        out.writeUTF("DOWNLOAD");
                        String filename = buffer.substring(5);
                        File file = new File(shared_path + filename);
                        if(file.length()>0){
                            out.writeUTF(filename);
                            byte[] bytes = new byte[(int)file.length()];
                            out.writeInt((int)file.length());
                            FileInputStream fileInputStream = new FileInputStream(file);
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                            bufferedInputStream.read(bytes,0,bytes.length);
                            out.write(bytes, 0, bytes.length);
                            out.flush();
                            out.writeUTF("FIM ARQUIVO");
                        }else{
                            out.writeUTF("ARQUIVO NAO EXISTE");
                        }

                    }
                }
                else{
                    out.writeUTF("Comando desconhecido");
                }
                System.out.println("Cliente disse: " + buffer);

            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("Thread comunicação cliente finalizada.");
    } //run
} //class

