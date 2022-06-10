import java.io.*;
import java.net.*;

/**
 * socket_Server
 */
public class socket_Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serversocket = null;
        try {
            
            serversocket = new ServerSocket(5056);
            serversocket.setReuseAddress(true);

            System.out.println("server open : "+ serversocket.getInetAddress());
            System.out.println("server socket " + serversocket);

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
