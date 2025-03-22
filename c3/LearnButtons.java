package c3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LearnButtons {
    public static void main(String[] args) {
        SmpWindow wnd = new SmpWindow();
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setVisible(true);
        wnd.setBounds(70, 70, 450, 300);
        wnd.setTitle("Mouse event handling demo");
    }
}

// Implement event handling functions within the same class
// which implements the window content pane
class ButtonPane extends JPanel implements ActionListener {
    // Declare references to three buttons located on the pane
    JButton button1;
    JButton button2;
    JButton button3;

    // The message string to be displayed on the pane
    // after each click of a button.
    String message;

    // Initialize UI elements and set up event listeners
    // within the constructor of the content pane object
    ButtonPane() {
        super();

        // Switch off automatic components positioning
        setLayout(null);

        // Create button objects. The button label
        // is specified at the constructor parameter
        button1 = new JButton("1");
        button2 = new JButton("2");
        button3 = new JButton("3");

        // Set fixed position and size of each button
        button1.setBounds(100, 100, 70, 30);
        button2.setBounds(190, 100, 70, 30);
        button3.setBounds(280, 100, 70, 30);

        // Add buttons to the content pane in order to assure
        // correct display
        add(button1);
        add(button2);
        add(button3);

        // Add listener to allow reacting to clicking -
        // The same listener is used by all buttons
        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);

        message = "No button has been pressed yet";
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Just draw the message string indicating
        // which button was just clicked
        g2d.drawString(message, 130, 150);
    }

    // The button event handler. It implements the method of
    // ActionListener interface. It will be called each time one of
    // buttons is clicked
    public void actionPerformed(ActionEvent event) {
        // Acquire reference to the object being the event source
        Object source = event.getSource();

        // Distinguish which button has been clicked
        if (source == button1)
            message = "Button 1 clicked";
        else if (source == button2)
            message = "Button 2 clicked";
        else
            message = "Button 3 clicked";

        // Force window redraw to see the result immediately
        repaint();
    }
}

class SmpWindow extends JFrame {
    public SmpWindow() {
        // Acquire drawing surface and add own panel to it
        // and add the panel containing buttons
        Container contents = getContentPane();
        contents.add(new ButtonPane());
    }
}

