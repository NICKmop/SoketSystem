import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
 
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
 
public class socket_Client_copy extends JFrame {
    private JTextField textField;
    JTextArea textArea;
    JLabel lblStatus;
    private Socket socket;
 
    private DataInputStream dis;
    private DataOutputStream dos;
 
    private boolean connectStatus;// 클라이언트 접속 여부 저장
    private boolean stopSignal;// 쓰레드 종료 신호 저장
 
    public socket_Client_copy() {
        showFrame();
    }
 
    public void showFrame() {
        setTitle("1:1 채팅 클라이언트");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(1200, 400, 500, 300);
        getContentPane().setLayout(null);
 
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.LIGHT_GRAY);
        getContentPane().add(textArea);
 
        textField = new JTextField();
        textField.setBounds(0, 230, 390, 31);
        getContentPane().add(textField);
        textField.setColumns(10);
 
        JButton btnInput = new JButton("입력");
        btnInput.setForeground(Color.BLUE);
        btnInput.setBackground(Color.YELLOW);
        btnInput.setBounds(387, 230, 97, 31);
        getContentPane().add(btnInput);
 
        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 484, 22);
        getContentPane().add(panel);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(0, 23, 484, 208);
        getContentPane().add(scrollPane);
        scrollPane.setViewportView(textArea);
        
 
        lblStatus = new JLabel("서버 연결 상태 : 미연결");
        lblStatus.setForeground(Color.RED);
        panel.add(lblStatus);
 
        textField.addActionListener(new ActionListener() {
 
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
 
        setVisible(true);
        
 
        startService();
        
        textArea.requestFocus();// 텍스트필드에 커서요청
 
    }
    public void startService() {
        // 서버접속시도
        String password= prepareConnect();
        boolean connectResult=connectServer(password);
        
        //서버접속 결과 판별하여 패스워드 일치할 때까지 패스워드 입력 후 접속 시도
        while(!connectResult) {
            password=prepareConnect();
            connectResult=connectServer(password);
            
        }
        textArea.append("서버접속 패스워드 일치\n");
        
        //멀티쓰레딩 구현하여 receiveMessage() 메서드 호출
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                receiveMessage();
            }
        }).start();
        
        
    }
    
 
    public String prepareConnect() {
        // 서버접속을 위한 패스워드 입력
        // 아무입력없이 확인을 눌렀을 경우("" 입력시)
        // 다이얼로그를 사용하여 "패스워드 입력 필수!" 출력 후 다시 입력 받기
        // 단, password가 null이 아닐 때만 판별 수행
        String password = JOptionPane.showInputDialog(this, "패스워드 입력");
 
        while (password != null && password.equals("")) {
            JOptionPane.showMessageDialog(this, "패스워드 입력 필수!", "경고", JOptionPane.ERROR_MESSAGE);
            password = JOptionPane.showInputDialog(this,"패스워드 입력");
            
        }
 
        // 취소 버튼 눌렀을 경우 (null 입력 시)
        // ConfurmDialog를 사용하여 "종료하시겠습니까?" 질문에 예/아니오 선택받기
        if (password == null) {
            int confirm = JOptionPane.showConfirmDialog(this, "종료하시겠습니까?", "종료 확인", JOptionPane.YES_NO_OPTION);
//                    System.out.println(confirm);
            // 선택된 버튼의 값을 JOptionPane.XXX_OPTION 상수와 비교
            if (confirm == JOptionPane.YES_OPTION) {// 예(Y) 선택 시 현재 프로그램 종료
                System.exit(0);// 프로그램 강제 종료(정상적인 강제 종료)
                
            }
            
            return null;
        }
        //----------------------------------------
 
        return password;
    }
 
    public boolean connectServer(String password) {
 
        try {
            textArea.append("서버에 접속을 시도 중입니다....\n");
 
            // socket 객체를 생성하여 IP 주소와 포트번호 전달->서버 접속시도
            socket = new Socket("localhost", 5056);
 
            // DataOutputStream 객체 생성 후 입력되는 패스워드 넘겨주기
 
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(password);
 
            textArea.append("서버 접속 완료\n");
 
            textArea.append("패스워드 확인중...\n");
 
            // DataInputStream 객체 생성 후 전달받은 접속요청 결과 출력
            dis = new DataInputStream(socket.getInputStream());
            boolean result = dis.readBoolean();
            
            //전달받은 접속요청 결과 판별
            if(!result) {
                textArea.append("패스워드 불일치로 연결 실패\n");
                socket.close();//소켓 반환
                return false;
            }else {
                lblStatus.setText("서버 연결 상태 : 연결됨\n");
                connectStatus=true;
            }
 
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public void receiveMessage() {
        //멀티 쓰레딩으로 메세지 수신 처리 작업 수행
        //boolean 타입 멤버변수 stopSignal 이 false 일 동안 반복해서 메세지 수신
        
        try {
            while(!stopSignal) {
                //클라이언트가 writeUTF() 메서드로 전송한 메세지를 입력받기
                textArea.append("서버 : "+dis.readUTF()+"\n");
            }
            //stopSignal 이 true 가 되면 메세지 수신 종료되므로 dis와 socket 반환
            dis.close();
            socket.close();
        }catch(EOFException e){
            //상대방이 접속 해제할 경우 소켓이 제거되면서 호출되는 예외
            textArea.append("서버 접속이 해제되었습니다.\n");
            lblStatus.setText("서버 연결 상태 : 미연결");
            connectStatus=true;
        }catch(SocketException e) {
            textArea.append("서버 접속이 해제되었습니다.\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        
        
    }
    
 
    public void sendMessage() {
        try {
            String text = textField.getText();
            textArea.append(text + "\n");
            
            //입력된 메세지가 "/exit" 일 경우
            if(text.equals("/exit")) {
                //textArea 에 "bye" 출력 후
                //stopSignal을 true로 설정 , 스트림 반환, 소켓 반환
                stopSignal=true;
                dos.close();
                socket.close();
                
                //프로그램 종료
                System.exit(0);
            }else {
                //입력된 메세지가 "/exit"가 아닐 경우( 전송할 메세지인 경우)
                //클라이언트에게 메세지 전송
                dos.writeUTF(text);
                
                //초기화 및 커서요청
                textField.setText("");
                textField.requestFocus();
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        new socket_Client_copy();
    }
}