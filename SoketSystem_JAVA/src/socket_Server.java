import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 * socket_Server
 */
public class socket_Server {
    private final static Logger logger = Logger.getLogger(socket_Client.class.getName());
    private static FileHandler filehandler;

    public static void main(String[] args) throws IOException {
        ServerSocket serversocket = null;
        filehandler = new FileHandler("./socketServer.log", true);
        try {
            
            serversocket = new ServerSocket(5056);
            serversocket.setReuseAddress(true);

            while (true) {
                Socket client = serversocket.accept();
                
                System.out.println("New client Connected : " + client.getInetAddress().getHostAddress());

                ClientHandler ch = new ClientHandler(client);

                new Thread(ch).start();
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if (serversocket != null) {
                try {
                    serversocket.close();    
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }

    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        
        //constructor
        public ClientHandler(Socket soc){
            this.clientSocket = soc;
        }

        @Override
        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    
                    System.out.printf("client : %s\n", line);
                    out.println(line);
                    
                    logger.info(line+"\n");
                    logger.addHandler(filehandler);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally{
                try {
                    if (out != null){
                        out.close();
                    }
                    if (in != null){
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
