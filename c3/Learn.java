package c3;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.*;

import javax.swing.*;

// This class implements mouse and keyboard listeners MouseListener
// and MouseMotionListener interfaces need to be implemented in order
// to receive notification on mouse events
class DrawWndPane extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    DrawWndPane() {
        super();

        // Make this object an event listener i.e. register "this"
        // object as a listener of mouse and keyboard events.
        // Only events occurring in the areas of the component for
        // which the add...Listener method was called will be
        // reported to event handlers.
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    // Mouse event handlers:
    // ==================================================

    // This handler is called when mouse button is clicked
    public void mouseClicked(MouseEvent arg0) {
        System.out.println("mouseClicked at " + arg0.getX() + " " + arg0.getY());
    }

    // This handler is called when the mouse enters the window
    // pane area
    public void mouseEntered(MouseEvent arg0) {
        System.out.println("mouseEntered at " + arg0.getX() + " " + arg0.getY());
    }

    // This handler is called when the mouse exits the window
    // pane area
    public void mouseExited(MouseEvent arg0) {
        System.out.println("mouseExited at " + arg0.getX() + " " + arg0.getY());
    }

    // This handler is called when mouse button is pressed
    public void mousePressed(MouseEvent arg0) {
        String which;

        // Get information which mouse button was clicked
        if (arg0.getButton() == MouseEvent.BUTTON1)
            which = " Button 1";
        else if (arg0.getButton() == MouseEvent.BUTTON2)
            which = " Button 2";
        else
            which = " Button 3";

        System.out.println("mousePressed at " + arg0.getX() + " " + arg0.getY() + " " + which + " was pressed");
    }

    // This handler is called when the button is released
    public void mouseReleased(MouseEvent arg0) {
        System.out.println("mouseReleased at " + arg0.getX() + " " + arg0.getY());
    }

    // This handler is called if the cursor is moved
    // in the pane area with a button pressed down
    public void mouseDragged(MouseEvent arg0) {
        System.out.println("mouseDragged at " + arg0.getX() + " " + arg0.getY());
    }

    // This handler is called if the cursor is moved
    // in the pane area with no button pressed down
    public void mouseMoved(MouseEvent arg0) {
        System.out.println("mouseMoved to " + arg0.getX() + " " + arg0.getY());
    }

    // Keyboard event handlers:
    // ==================================================

    // This handler is called when the keyboard key is pressed
    public void keyPressed(KeyEvent e) {
        // Use getKeyCode to get the symbol of the pressed key.
        // It can be used to distinguish virtual keys.
        // getKeyChar() returns the character associated
        // with the key. Use it for "ordinary" character keys only.
        System.out.println("keyPressed " + "Key code: " + e.getKeyCode() + " Char: " + e.getKeyChar());
    }

    // This handler is called when a key is released
    public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased");
    }

    // This handler notifies about the event consisting in
    // typing a character. The modifier keys (Shift, Alt) are not
    // reported with this handler.
    public void keyTyped(KeyEvent e) {
        // Use getKeyChar() to get the character associated
        // with the key. Use it for "ordinary" character keys.
        System.out.println("keyTyped " + "Char: " + e.getKeyChar());
    }

    public static void main(String[] args) {
        c3.Window wnd = new c3.Window();

        // Closing window terminates the program
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setBounds(70, 70, 300, 300);
        wnd.setVisible(true);

        while (true) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
            wnd.repaint();
        }
    }
}


class Window extends JFrame
{
    public Window()
    {
        Container contents = getContentPane();
        contents.add( new c3.DrawWndPane() );
        setTitle( "Solar system");
    }
}