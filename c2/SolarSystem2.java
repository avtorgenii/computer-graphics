package c2;

import javax.swing.*;
import java.awt.*;


class SolarDrawWndPane2 extends JPanel {
    SolarDrawWndPane2() {
        super();
        setBackground(new Color(0, 0, 0));
    }

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
            2300 * 2    // Pluto
    };

    final double sunDiameterKm = 1392000 / 100.0;
    final double moonOrbitRadius = 20;
    final double moonDiameterKm = 3474;
    final double moonOrbitalSpeedKmPerS = 1.02;

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
    final Color moonColor = new Color(169, 169, 169);
    final Color sunColor = new Color(255, 255, 0);


    int center_x, center_y;
    final long startSeconds = System.currentTimeMillis() / 1000;


    public void DrawPlanets(double seconds, double sizeFactor, Graphics2D g2d, Graphics g, double[] orbitRadius) {
        g2d.setColor(sunColor);
        int sunSize = (int) (sunDiameterKm * sizeFactor);
        g.fillOval(center_x - sunSize / 2, center_y - sunSize / 2, sunSize, sunSize);

        int earth_x = 0, earth_y = 0;

        for (int i = 0; i < planetDiametersKm.length; i++) {
            double r = orbitRadius[i];
            double w = orbitalSpeedsKmPerS[i] / r;
            double theta = w * seconds;
            // Coordinates of planet's center
            int x = (int) (center_x + r * Math.cos(theta));
            int y = (int) (center_y + r * Math.sin(theta));

            if (i == 2) { // Earth
                earth_x = x;
                earth_y = y;
            }

            int planetSize = (int) (planetDiametersKm[i] * sizeFactor);
            g2d.setColor(planetColors[i]);
            g.fillOval(x - planetSize / 2, y - planetSize / 2, planetSize, planetSize);
        }

        // Moon
        double moon_w = moonOrbitalSpeedKmPerS / moonOrbitRadius;
        double moonTheta = moon_w * seconds * 50.0;
        int moon_x = (int) (earth_x + moonOrbitRadius * Math.cos(moonTheta));
        int moon_y = (int) (earth_y + moonOrbitRadius * Math.sin(moonTheta));

        int moonSize = (int) (moonDiameterKm * sizeFactor);
        g2d.setColor(moonColor);
        g.fillOval(moon_x - moonSize / 2, moon_y - moonSize / 2, moonSize, moonSize);
    }

    public void DrawOrbits(Graphics2D g2d, double[] orbitRadius) {
        g2d.setColor(Color.WHITE);
        for (double radius : orbitRadius) {
            int r = (int) radius;
            g2d.drawOval(center_x - r, center_y - r, 2 * r, 2 * r);
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Dimension size = getSize();
        center_x = size.width / 2;
        center_y = size.height / 2;

        // Size factor for scaling planets
        int minSize = Math.min(size.width, size.height);
        double sizeFactor = (double) minSize / 35 / sunDiameterKm;

        // Orbits
        double[] orbitRadius = new double[planetDiametersKm.length];
        for (int i = 0; i < orbitRadius.length; i++) {
            orbitRadius[i] = (i + 1) * (minSize / (2.0 * orbitRadius.length));
        }

        double elapsedSeconds = ((double) System.currentTimeMillis() / 1000 - startSeconds) * 1.0;
        DrawOrbits(g2d, orbitRadius);
        DrawPlanets(elapsedSeconds, sizeFactor, g2d, g, orbitRadius);
    }
}

class SmpWindow2 extends JFrame {
    public SmpWindow2() {
        Container contents = getContentPane();
        contents.add(new SolarDrawWndPane2());
        setTitle("Solar system 2");
    }
}

public class SolarSystem2 {

    public static void main(String[] args) {
        SmpWindow2 wnd = new SmpWindow2();

        // Closing window terminates the programs
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setBounds(70, 70, 700, 700);
        wnd.setVisible(true);


        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
            wnd.repaint();
        }
    }

}