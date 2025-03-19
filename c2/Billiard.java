package c2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Billiard {
    public static class BilliardPanel extends JPanel {
        // Table stuff
        final int TABLE_EDGE_WIDTH = 20;
        final double FRICTION_COEFFICIENT = 0.000;
        final Color TABLE_EDGE_COLOR = new Color(153, 76, 0);
        final Color TABLE_COLOR = new Color(0, 204, 102);

        public void drawTable(Graphics2D g2d) {
            g2d.setColor(TABLE_EDGE_COLOR);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(TABLE_COLOR);
            g2d.fillRect(TABLE_EDGE_WIDTH, TABLE_EDGE_WIDTH, getWidth() - TABLE_EDGE_WIDTH * 2, getHeight() - TABLE_EDGE_WIDTH * 2);
        }

        // Balls stuff
        final double MAX_START_SPEED = 3.0;
        final double MIN_START_SPEED = -3.0;
        final int BALL_RADIUS = 20;
        final int BALLS_COUNT = 10;
        ArrayList<Ball> balls = new ArrayList<>();
        final int DISTANCE_THRESHOLD = BALL_RADIUS * 2;

        class Ball {
            double x, y;
            double vx, vy;
            Color color;

            public Ball(double x, double y, double vx, double vy, Color color) {
                // x and y represent coordinates of ball's center
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
                this.color = color;
            }

            public void draw(Graphics2D g2d) {
                g2d.setColor(this.color);
                g2d.fillOval((int) x - BALL_RADIUS, (int) y - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
            }

            public void move() {
                x += vx;
                y += vy;
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw table
            drawTable(g2d);


            int frameWidth = getWidth();
            int frameHeight = getHeight();

            // Balls stuff
            for (int i = 0; i < balls.size(); i++) {
                Ball ball = balls.get(i);

                // Detect collision with table edge
                if (ball.x - BALL_RADIUS < TABLE_EDGE_WIDTH) { // with left side
                    ball.vx = -ball.vx;
                } else if (ball.x + BALL_RADIUS > frameWidth - TABLE_EDGE_WIDTH) { // with right side
                    ball.vx = -ball.vx;
                }

                if (ball.y - BALL_RADIUS < TABLE_EDGE_WIDTH) { // with top side
                    ball.vy = -ball.vy;
                } else if (ball.y + BALL_RADIUS > frameHeight - TABLE_EDGE_WIDTH) { // with bottom side
                    ball.vy = -ball.vy;
                }


                // Detect collision with two different balls
                for (int j = i + 1; j < balls.size(); j++) {
                    Ball ball1 = balls.get(i);
                    Ball ball2 = balls.get(j);

                    double dx = ball2.x - ball1.x;
                    double dy = ball2.y - ball1.y;
                    double d = Math.sqrt(dx * dx + dy * dy);

                    if (d <= DISTANCE_THRESHOLD) {
                        // Parts of normal vector pointing from ball2 to ball1
                        double nx = dx / d;
                        double ny = dy / d;

                        // To avoid balls sticking to each other
                        double overlapDepth = DISTANCE_THRESHOLD - d;
                        ball1.x -= overlapDepth * nx / 2;
                        ball1.y -= overlapDepth * ny / 2;
                        ball2.x += overlapDepth * nx / 2;
                        ball2.y += overlapDepth * ny / 2;

                        // t (styczna) is axis perpendicular to n(przechodzi przez centrumy kul)
                        double v1n = ball1.vx * nx + ball1.vy * ny;
                        double v1t = -ball1.vx * ny + ball1.vy * nx;
                        double v2n = ball2.vx * nx + ball2.vy * ny;
                        double v2t = -ball2.vx * ny + ball2.vy * nx;


                        ball1.vx = nx * v2n - ny * v1t;
                        ball1.vy = ny * v2n + nx * v1t;
                        ball2.vx = nx * v1n - ny * v2t;
                        ball2.vy = ny * v1n + nx * v2t;
                    }
                }


                // Apply table friction
                ball.vx *= 1 - FRICTION_COEFFICIENT;
                ball.vy *= 1 - FRICTION_COEFFICIENT;

                // Move balls
                ball.move();

                // Draw balls
                ball.draw(g2d);
            }
        }

        // Init
        BilliardPanel() {
            super();
            setBackground(TABLE_COLOR);

            // Create balls
            Random random = new Random();
            int x, y;
            double startSpeedX, startSpeedY;
            Color ballColor;

            for (int i = 4; i < BALLS_COUNT + 4; i++) {
                // Setup start variables for ball
                x = i * TABLE_EDGE_WIDTH;
                y = i * TABLE_EDGE_WIDTH;
                startSpeedX = MIN_START_SPEED + (Math.random() * (MAX_START_SPEED - MIN_START_SPEED));
                startSpeedY = MIN_START_SPEED + (Math.random() * (MAX_START_SPEED - MIN_START_SPEED));
                ballColor = new Color(random.nextInt(0, 255), random.nextInt(0, 255), random.nextInt(0, 255));

                // Add ball to the table
                balls.add(new Ball(x, y, startSpeedX, startSpeedY, ballColor));
            }
        }
    }


    public static void main(String[] args) {
        final int WIDTH = 800;
        final int HEIGHT = 600;

        JFrame frame = new JFrame("Billiard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Billiard");
        frame.setSize(WIDTH, HEIGHT);
        frame.setContentPane(new BilliardPanel());
        frame.setVisible(true);

        while (true) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
            frame.repaint();
        }
    }
}
