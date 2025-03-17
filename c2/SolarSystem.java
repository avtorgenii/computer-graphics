package c2;

import java.awt.*;
import java.lang.Thread;
import java.lang.InterruptedException;

import javax.swing.*;

public class SolarSystem {

    public static void main(String[] args)
    {
        SmpWindow wnd = new SmpWindow();

        // Closing window terminates the programs
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setBounds(70, 70, 700, 700);
        wnd.setVisible(true);

        while (true)
        {
            try
            {
                Thread.sleep(0);
            }
            catch (InterruptedException e)
            {
                System.out.println("Interrupted");
            }
            wnd.repaint();
        }
    }

}

class SolarDrawWndPane extends JPanel
{
    // In AU
    final double[] orbitRadius = {
            0.38709830982 * 10,  // Mercury
            0.72332981996 * 10,  // Venus
            1.00000101778 * 10,  // Earth
            1.52367934191 * 10,  // Mars
            5.20260319132 * 5,  // Jupiter
            9.55490959574 * 5,  // Saturn
            19.21844606178 * 2, // Uranus
            30.11038686942 * 2, // Neptune
            39.5181761979 * 1.7   // Pluto
    };

    final double[] orbitalSpeedsKmPerS = {
            47.36,  // Mercury
            35.02,  // Venus
            29.78,  // Earth
            24.13,  // Mars
            13.07,  // Jupiter
            9.69,   // Saturn
            6.81,   // Uranus
            5.43,   // Neptune
            4.66    // Pluto
    };

    final double[] planetDiametersKm = {
            4879,   // Mercury
            12104,  // Venus
            12742,  // Earth
            6792,   // Mars
            13982, // Jupiter
            11646, // Saturn
            50724,  // Uranus
            49528,  // Neptune
            5000    // Pluto
    };

    final double moonOrbitRadius = 2.57;
    final double moonDiameterKm = 3474;
    final double moonOrbitalSpeedKmPerS = 1.02;
    final Color moonColor = new Color(169, 169, 169);
    final double sunDiameterKm = (double) 1392000 / 100;

    final Color[] planetColors = {
            new Color(169, 169, 169),  // Mercury
            new Color(255, 223, 186),  // Venus
            new Color(0, 0, 255),      // Earth
            new Color(255, 69, 0),     // Mars
            new Color(255, 215, 0),    // Jupiter
            new Color(255, 223, 186),  // Saturn
            new Color(0, 255, 255),    // Uranus
            new Color(0, 0, 139),      // Neptune
            new Color(139, 69, 19)     // Pluto
    };


    int center_x, center_y;

    SolarDrawWndPane()
    {
        super();
        setBackground( new Color( 0, 0, 0) );
    }


    public void DrawPlanets(double seconds, double sizeFactor, double orbitFactor, Graphics2D g2d, Graphics g){
        // Sun
        g2d.setColor( new Color( 255, 255, 0 ) );
        int sunSize = (int) (sunDiameterKm * sizeFactor);
        g.fillOval(center_x - sunSize / 2, center_y - sunSize / 2, sunSize, sunSize);

        // Earth data
        int earth_x = 0;
        int earth_y = 0;

        // Planets
        for(int i = 0;i < planetDiametersKm.length;i++){
            double r = orbitRadius[i] * orbitFactor;
            double w = orbitalSpeedsKmPerS[i] / 500 / r;
            double teta = w * seconds;
            int x = (int) (center_x + r * Math.cos(teta));
            int y = (int) (center_y + r * Math.sin(teta));

            if(i == 2){
                earth_x = x;
                earth_y = y;
            }

            int planetSize = (int) (planetDiametersKm[i] * sizeFactor);

            g2d.setColor(planetColors[i]);
            g.fillOval(x - planetSize / 2, y - planetSize / 2, planetSize, planetSize);
        }

        // Moon
        double r = moonOrbitRadius * orbitFactor;
        double w = moonOrbitalSpeedKmPerS / 500 / r;
        double teta = w * seconds;
        int x = (int) (earth_x + r * Math.cos(teta));
        int y = (int) (earth_y + r * Math.sin(teta));

        int planetSize = (int) (moonDiameterKm * sizeFactor);

        g2d.setColor(moonColor);
        g.fillOval(x - planetSize / 2, y - planetSize / 2, planetSize, planetSize);
    }

    public void DrawOrbits(double orbitFactor, Graphics2D g2d, Graphics g){
        for(int i = 0; i < orbitRadius.length; i++){
            int major_semi_axis = (int) (orbitFactor * orbitRadius[i]);

            g2d.setColor(planetColors[i]);
            g.drawOval(center_x - major_semi_axis, center_y - major_semi_axis, major_semi_axis * 2, major_semi_axis * 2);
        }
    }

    public void paint( Graphics g )
    {
        paintComponent( g );
    }

    public void paintComponent( Graphics g )
    {
        double seconds = System.currentTimeMillis();

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        Dimension size = getSize();

        center_x = size.width/2;
        center_y = size.height/2;

        int minSize = Math.min(size.width, size.height);
        double orbitFactor = minSize / (orbitRadius[orbitRadius.length - 1] * 2);
        // Sun can take only n percent of screen width
        double sizeFactor = (double) minSize / 35 / sunDiameterKm;


        DrawPlanets(seconds, sizeFactor, orbitFactor, g2d, g);
        DrawOrbits(orbitFactor, g2d, g);
    }
}

class SmpWindow extends JFrame
{
    public SmpWindow()
    {
        Container  contents = getContentPane();
        contents.add( new SolarDrawWndPane() );
        setTitle("Solar sytem");
    }
}