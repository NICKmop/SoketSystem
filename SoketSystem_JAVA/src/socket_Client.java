import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;


public class socket_Client extends JFrame{
 
    public static void main(String[] args) {
        try {
            Socket soc = new Socket("localhost", 5056);
            // Socket soc = new Socket("10.10.200.130", 9888);
            //writer
            PrintWriter pw = new PrintWriter(soc.getOutputStream(), true);
            //read
            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));

            //input Data
            Scanner sc =new Scanner(System.in);
            String line = null;
            while (!"exit".equalsIgnoreCase(line)) {
                line = sc.nextLine();

                pw.println(line);
                pw.flush();

                System.out.println("sever respone : " + in.readLine());
            }
            sc.close();
            soc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
}