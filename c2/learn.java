package c2;

import javax.swing.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class learn {
    public static void main(String[] args) throws IOException {
//        JFrame wnd = new JFrame("learn");
//        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        wnd.setSize(400, 400);
//        wnd.setVisible(true);
//
//        JPanel panel = new JPanel();
//        panel.paintComponents(wnd.getGraphics());
//        wnd.setContentPane(panel);

        Date time;
        GregorianCalendar calendar;
        int hour, minute, second;
        // Acquire the time stamp in milliseconds
        time = new Date();
        // Create the calendar and set it to current time
        calendar = new GregorianCalendar();
        calendar.setTime( time );
        // Extract hour, minute and second from the calendar object
        minute = calendar.get( Calendar.MINUTE );
        hour = calendar.get( Calendar.HOUR );
        second = calendar.get( Calendar.SECOND );

    }
}
