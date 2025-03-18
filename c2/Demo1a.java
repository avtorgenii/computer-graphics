package c2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Demo1a {
	public static void main(String[] args) {
		MyFrame wnd = new MyFrame();
		wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		wnd.setVisible(true);
	}
}

class MyPanel extends JPanel {
	MyPanel() {
		super();
		setBackground(new Color(255, 220, 220));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		Dimension size = getSize();
		int w = size.width;
		int h = size.height;
		int r = Math.min(w, h) / 4;

		g2d.drawLine(0, h / 2, w - 1, h / 2);
		g2d.drawLine(w / 2, 0, w / 2, h - 1);

		// Set color for further drawing
		// g2d.setPaint( new Color( 0, 0, 255) );
		Ellipse2D.Double ellipse = new Ellipse2D.Double(w / 2 - r, h / 2 - r,
				2 * r, 2 * r);
		g2d.draw(ellipse);

		// Tu wykonac procedure rysowania
	}
}

class MyFrame extends JFrame {
	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;
	public MyFrame() {
		setSize(WIDTH, HEIGHT);
		setTitle("Automatic redraw");

		setContentPane(new MyPanel());
	}
}
