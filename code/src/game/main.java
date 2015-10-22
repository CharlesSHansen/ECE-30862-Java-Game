package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class main extends JFrame{    
	
	private static BufferedImage bgImage;
	
	public static void main(String[] args) {
	
		
		File file = new File("backgruond.jpg");
		try {
			bgImage = ImageIO.read(file);
		} catch (IOException e) {
		}
		DisplayMode displayMode;
		
		if(args.length == 3) {
			displayMode = new DisplayMode(  
				Integer.parseInt(args[0]),
				Integer.parseInt(args[1]),
				Integer.parseInt(args[2]),
				DisplayMode.REFRESH_RATE_UNKNOWN
					);
		}
		else {
			displayMode = new DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN);
		}
		
		main test = new main();
		test.run(displayMode);
			
		}
	private static final long DEMO_TIME = 5000;
	
	public void run(DisplayMode displayMode) {
		setBackground(Color.blue);
	     setForeground(Color.white);
		 setFont(new Font("Dialog", 0, 24));
		 SimpleScreenManager screen = new SimpleScreenManager();
		 try {
		 screen.setFullScreen(displayMode, this);
		 try {
		 Thread.sleep(DEMO_TIME);
		 }
		 catch (InterruptedException ex) {
			 System.out.println("Exception");
		 }
		 }
		 finally {
		 screen.restoreScreen();
		 }
		 }
		 public void paint(Graphics g) {
		 //g.drawString("Hello World!", 20, 50);
			 g.drawImage(bgImage, 0, 0, null);
		 }
		 
		 
		}
			
