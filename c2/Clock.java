package c2;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.awt.*;
//import java.awt.Color;
//import java.awt.BasicStroke;
//import java.awt.Container;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Polygon;

import java.lang.Thread;
import java.lang.InterruptedException;

import javax.swing.*;

public class Clock {

	public static void main(String[] args) 
   {
       Window wnd = new Window();
      
      // Closing window terminates the program      
      wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      wnd.setBounds(70, 70, 300, 300);
      wnd.setVisible(true);
      
      while (true)
      {
         try
         {
            Thread.sleep(5);
         }
         catch (InterruptedException e)
         {
            System.out.println("Interrupted");
         }
         wnd.repaint();
      }
   }
        
}

class DrawWndPane extends JPanel
{

	final int GAUGE_LEN = 10;
    final int pendulum_period = 2;
    final int amplitude = 45;
	int center_x, clock_center_y;
	int  r_outer, r_inner, r_weight, l_pendulum;
	GregorianCalendar calendar;
	
	
   DrawWndPane()
   {
	   super();	   
	   setBackground( new Color( 200, 200, 255) );
	   calendar = new GregorianCalendar();
   }
   
   public void DrawGauge( double angle, Graphics g )
   {
       int xw, yw, xz, yz;
       
       angle = 3.1415 * angle / 180.0;
       xw = (int)(center_x + r_inner * Math.sin( angle ));
       yw = (int)(clock_center_y - r_inner * Math.cos( angle ));
       xz = (int)(center_x + r_outer * Math.sin( angle ));
       yz = (int)(clock_center_y - r_outer * Math.cos( angle ));
       
       g.drawLine( xw, yw, xz, yz );
   }
   
   public void DrawHand( double angle, int length, Graphics g )
   {
       int xw, yw, xz, yz;
       
       angle = 3.1415 * angle / 180.0;
       xw = (int)(center_x + length * Math.sin( angle ));
       yw = (int)(clock_center_y - length * Math.cos( angle ));
       
       angle += 3.1415;
       xz = (int)(center_x + GAUGE_LEN * Math.sin( angle ));
       yz = (int)(clock_center_y - GAUGE_LEN * Math.cos( angle ));
       
       g.drawLine( xw, yw, xz, yz );
   }
   
   public void DrawDial( Graphics g )
   {
	   g.drawOval(  center_x - r_outer,
			        clock_center_y - r_outer,
			        2*r_outer, 2*r_outer );
	   
	   for ( int i = 0; i <= 11; i++ )
		   DrawGauge( i * 30.0, g );
   }

   public void DrawPendulum(double seconds, double l_pendulum, double r_weight, Graphics g){
       int xw, yw, xz, yz;

       xw = center_x;
       yw = clock_center_y + r_outer;

       double w = 2 * 3.1415 / pendulum_period;
       double new_angle = Math.toRadians(amplitude) * Math.cos(w * seconds);

       xz = xw + (int) (l_pendulum * Math.sin(new_angle));
       yz = yw + (int) (l_pendulum * Math.cos(new_angle));


       g.drawLine( xw, yw, xz, yz );
       g.fillOval(xz - (int)r_weight, yz - (int)r_weight, (int)(2 * r_weight), (int)(2 * r_weight));
   }
   
   public void paint( Graphics g )
   {
      paintComponent( g );
   }

   public void paintComponent( Graphics g )
   {  
	    int  minute, second, hour;
	   
	    super.paintComponent(g);	   
       Graphics2D  g2d = (Graphics2D)g;     

       Dimension size = getSize();

       center_x = size.width/2;
       clock_center_y = (int) (size.height/6.5);
       r_outer = (int) (Math.min( size.width, size.height)/6.5);
       r_inner = r_outer - GAUGE_LEN;

       l_pendulum = r_outer * 4;
       r_weight = l_pendulum / 20;
       
       Date time = new Date();
       calendar.setTime( time );
       
       minute = calendar.get( Calendar.MINUTE );
       hour   = calendar.get( Calendar.HOUR );
       if ( hour > 11 )
    	    hour = hour - 12;
       second = calendar.get( Calendar.SECOND );
       
       DrawDial( g );
       
	   g2d.setColor( new Color( 255, 0, 0 ) );        
	   g2d.setStroke( new BasicStroke( 5 ) );  
      DrawHand( 360.0 * (hour * 60 + minute) / ( 60.0 * 12 ) , (int)(0.75 * r_inner), g);
    
	   g2d.setColor( new Color( 255, 0, 0 ) );        
	   g2d.setStroke( new BasicStroke( 3 ) );  
      DrawHand( 360.0 * (minute * 60 + second )/( 3600.0), (int)(0.97 * r_outer), g);
       
	   g2d.setColor( new Color( 0, 0, 0 ) );        
	   g2d.setStroke( new BasicStroke( 1 ) );  
      DrawHand( second * 6.0, (int)(0.97 * r_inner), g);

      DrawPendulum(System.currentTimeMillis() / 1000.0, l_pendulum, r_weight, g);
   }
}

class Window extends JFrame
{
    public Window()
    {
    	Container  contents = getContentPane(); 
    	contents.add( new DrawWndPane() );
    	setTitle( "Solar system");
    }
}