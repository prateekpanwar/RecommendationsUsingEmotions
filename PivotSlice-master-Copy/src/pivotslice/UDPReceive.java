package pivotslice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.image.ColorModel;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class UDPReceive extends Thread{
	
	private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    public static PivotSlice p;
    private static int trueCount = 0;
    
    
	public UDPReceive(PivotSlice r) {
		// TODO Auto-generated constructor stub
		try {
			socket = new DatagramSocket(5005);
			//System.out.println("df");
//			run();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p = r;
		
	}
	
	public void run() {
        running = true;
        float clr = 0.2f;
        while (running) {
           DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            try {
				socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());
            //System.out.println(received);          
            if (received.contains("T")){
            	trueCount++;
            	clr = clr - 0.04f;
            	//p.eastPanel.setBackground(Color.getHSBColor(clr, 1.0f, 1.0f));
            	System.out.println(received);
            	if(received.contains("a")){
            		//p.test.setText("Problem with the search panel");
            		p.dialog.setLocation(p.searchPanel.getWidth(), p.searchPanel.getHeight());
            		p.helpStr.setText("Problem with the search panel");
            		}
            	else if(received.contains("b")){
            		//p.test.setText("Problem with the task panel");
            		p.dialog.setLocation(p.searchPanel.getWidth()+1000, p.searchPanel.getHeight());
            		p.helpStr.setText("Problem with the task panel");
            	}
            	else if(received.contains("c")){
            		//p.test.setText("Problem with the info panel");
            		p.dialog.setLocation(p.searchPanel.getWidth()+1100, p.searchPanel.getHeight()+500);
            		p.helpStr.setText("Problem with the info panel");
            	}
            	else if(received.contains("d")){
            		//p.test.setText("Problem with the help panel");
            		p.dialog.setLocation(p.searchPanel.getWidth()+1100, p.searchPanel.getHeight()+700);
            		p.helpStr.setText("Problem with the help panel");
            	}
            	else if(received.contains("e")){
            		//p.test.setText("Problem with the filterX panel");
            		p.dialog.setLocation(p.searchPanel.getWidth()+1000, p.searchPanel.getHeight()+900);
            		p.helpStr.setText("Problem with the filterX panel");
            	}
            	else{
            		//p.test.setText("Problem with the Dataset panel");
            		p.helpStr.setPreferredSize(new Dimension(400, 100));
            		p.helpStr.setText("<html><body><h3 'text-align:justify;'>"
            				+ "You can get common information using the toggle button in the search panel"
            				+ "</h3></body></html>");
            		p.dialog.setPreferredSize(new Dimension(400, 100));
            		p.dialog.setLocationRelativeTo(p.centerPanel);
            	}
            	
        		
        		try {
        			p.dialog.getContentPane().setBackground(Color.getHSBColor(clr, 1.0f, 1.0f));
        			p.dialog.setVisible(true);
					Thread.sleep(3000);
					p.dialog.setVisible(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		p.test.setText("");
        		p.eastPanel.setBackground(null);
        		
            }
            else{
            	p.test.setText("");
            	trueCount=0;
            	clr = 0.2f;
            	p.eastPanel.setBackground(null);
            }
            
            if (received.equals("end")) {
                running = false;
                continue;
            }
            try {
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        socket.close();
        
    }

}
