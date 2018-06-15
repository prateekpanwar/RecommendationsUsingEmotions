package pivotslice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TaskPanel implements MouseListener, MouseMotionListener{

	private JLabel questionLabel;
	private JLabel timerDisplay;
	private JTextField answerField;
	private JButton submitButton;
	private JButton start;
	private JButton done;
	private JLabel info;
	private FileWriter f;
	private Task[] t = new Task[6];
	private JPanel task = new JPanel();
	private PivotSlice p;
	public static AutoLogger l = new AutoLogger(true);
	//public static UDPReceive udp = new UDPReceive();
	
	
	public TaskPanel(PivotSlice r) {
		//udp.start();
		p=r;
		try {
			setTask();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		p.addMouseListener(this);
		panelasg(r, p.centerPanel);
		panelasg(r, p.northwest);
		p.infoPanel.addMouseListener(this);
		p.eastPanel.addMouseListener(this);
		p.infoPanel.addMouseMotionListener(this);
		panelasg(r, p.searchPanel);
		panelasg(r, p.facetBrowserX);
		panelasg(r, p.facetBrowserY);
		init();
		
	}
	
	private void init() {
		task.setBorder(BorderFactory.createTitledBorder("Read"));
		task.setLayout(new BorderLayout());
		info = new JLabel("<html>"+"Please write the multiple answers separated by a comma. For example: x, y"+ "<br/>" +"The faster and accurately you perform, the more money you earn. Good luck!"+ "</html>");
		info.setFont(new Font("Serif", Font.PLAIN, 20));
		start = new JButton("Start");
		//Dimension dim = new Dimension(60,35);
		//start.setPreferredSize(dim);
		JPanel g = new JPanel();
		g.add(start);
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//task.setBorder(BorderFactory.createTitledBorder(""));
				//start.setVisible(false);
				l.start();
				task.removeAll();
				try {
					f = new FileWriter("test.txt");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				init2();
			}
		});
		
		//task.add(start, );
		task.add(info, BorderLayout.NORTH);
		task.add(g, BorderLayout.SOUTH);
	}
	int questionCounter = 0;
	private void init2() {
		
		task.setBorder(BorderFactory.createTitledBorder("Tasks "+(questionCounter+1)));
		task.setLayout(new BorderLayout());
		JPanel g = new JPanel();
		questionLabel = new JLabel("<html>"+t[questionCounter].getQuestion()+"</html>");
		timerDisplay = new JLabel(timer());
		answerField = new JTextField();
		submitButton = new JButton("Submit");
		//Dimension dim = new Dimension(70,35);
		
		//answerField.setPreferredSize(dim);
		//submitButton.setPreferredSize(dim);
		
		questionLabel.setFont(new Font("Calibri", Font.PLAIN, 18));
		timerDisplay.setFont(new Font("Calibri", Font.PLAIN, 26));
		g.add(submitButton);
		Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
            	if (questionCounter>1){
            		timerDisplay.setText(timer());
            	}
            	else{
            		timerDisplay.setText(timer());
            		timerDisplay.setText("Practice task");
            		mil =-1;
            		sec = 0;
            		min = 0;
            	}
            }
        }, 0, 100);
		
		submitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (answerField.getText().equals("") == true){
					JOptionPane.showMessageDialog(null, "Please enter your answer");
				}
				else{
					Date date = new Date();
					String answer = answerField.getText();
					t[questionCounter].setAnswer(answer);
					//System.out.println(t[questionCounter].getAnswer());
					try {
						f.write(date.toString()+"\t"+date.getTime()+"\tTask "+(questionCounter+1)+"\t"+ answerField.getText()+"\n");
						l.logAction("Task" +(questionCounter+1)+","+ answerField.getText());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if((questionCounter==1 & answer.equals("2012")) || (questionCounter==0 & answer.equals("2752")) || (questionCounter==2 & answer.equals("6")) || (questionCounter==3 & answer.equals("14")) || (answer.equals("na")) || (questionCounter==4 & answer.contains("78") & answer.toLowerCase().contains("mueller"))){
						if (questionCounter==1)
						{
							JOptionPane.showMessageDialog(null, "Please confirm when you are ready for the main tasks.");
						}
						questionCounter++;
						task.setBorder(BorderFactory.createTitledBorder(""));
						task.setBorder(BorderFactory.createTitledBorder("Tasks "+(questionCounter+1)));
						questionLabel.removeAll();
						answerField.setText("");
						questionLabel.setText("<html>"+t[questionCounter].getQuestion()+"</html>");
					}
					else{
						JOptionPane.showMessageDialog(null, "Incorrect answer. Please try again");
					}
					if (questionCounter == 5){
						done = new JButton("DONE");
						g.remove(submitButton);
						g.add(done);
						
						done.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								Date date = new Date();
								l.logAction("Task" +(questionCounter+1)+","+ answerField.getText());
								try {
									if (answerField.getText().toLowerCase().contains("moller") & answerField.getText().contains("18") || (answer.equals("na"))){
										f.write(date.toString()+"\t"+date.getTime()+"\tTask "+(questionCounter+1)+"\t"+ answerField.getText()+"\n");
										
										f.flush();
										f.close();
										l.stop();
										System.exit(0);
									}
									else{
										JOptionPane.showMessageDialog(null, "Incorrect answer. Please try again");
									}
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								
							}
						});
					}
					//init2();
									
				}
			}
		});
		
		
		task.add(questionLabel, BorderLayout.NORTH);
		task.add(answerField, BorderLayout.CENTER);
		task.add(g, BorderLayout.SOUTH);
		task.add(timerDisplay, BorderLayout.EAST);

	}
	
	
	public void setTask() throws IOException{
		// set new task to the current
		try {
			FileReader f = new FileReader("data/task.txt");
			BufferedReader bufferedReader = new BufferedReader(f);
			String line = null;
			int count=0;
			while((line = bufferedReader.readLine()) != null) {
                t[count]= new Task(line);
                count++;
            }
			
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public JPanel getPanel() {
		return task;
	}
	
	int mil =-1;
	int sec = 0;
	int min = 0;
	public String timer() {
		mil++;
		if (mil==10){
			sec++;
			mil=0;
		}
		if (sec==60){
			min++;
			sec=0;
		}
		
		return ("Timer "+ min+":"+sec+":"+mil);
	}
	
	public void panelasg(PivotSlice r, JPanel j){
		//p=r;
		int max = j.getComponentCount();
		System.out.println(max);
		int i;
		for (i=0;i<max;i++){
			//System.out.println(j.getComponent(i));
			j.getComponent(i).addMouseListener(this);
		}
	}
	
	
	int count=0;
	@Override
	public void mouseClicked(MouseEvent e) {
		PointerInfo a = MouseInfo.getPointerInfo();
		Point b = a.getLocation();
		System.out.println(b.getX()+" "+b.getY());
		l.logAction(b.getX()+","+b.getY());
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//l.logAction("here");
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println("Drag");
		l.logAction("Drag");
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}}
