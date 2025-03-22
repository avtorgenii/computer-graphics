package c3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Editor {
    public abstract class Figure {
        Color color;

        Figure(Color color) {
            this.color = color;
        }

        public void save() {}

        public void startDraw(Graphics2D g2d, int x, int y) {
            g2d.setXORMode(color);
        }

        public void draw(Graphics2D g2d, int x, int y) {}

        public void finishDraw(Graphics2D g2d, int x, int y) {
            g2d.setPaintMode();
        }
    }

    public class Line extends Figure {
        int startX, startY, endX, endY;

        Line(Color color) {
            super(color);
        }

        @Override
        public void startDraw(Graphics2D g2d, int x, int y) {
            super.startDraw(g2d, x, y);
            startX = x;
            startY = y;
        }

        @Override
        public void draw(Graphics2D g2d, int x, int y) {
            super.draw(g2d, x, y);
            g2d.drawLine(startX, startY, x, y);
        }

        @Override
        public void finishDraw(Graphics2D g2d, int x, int y) {
            super.finishDraw(g2d, x, y);
            endX = x;
            endY = y;
        }
    }

    public class Rectangle extends Figure {
        Rectangle(Color color) {
            super(color);
        }
    }

    public class Circle extends Figure {
        Circle(Color color) {
            super(color);
        }
    }

    static class EditorPane extends JPanel {
        JRadioButton lineButton, circleButton, squareButton;
        JButton colorButton;
        ButtonGroup figureGroup;
        Color currentColor;

        EditorPane() {
            setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            // Radio buttons
            lineButton = new JRadioButton("Line");
            lineButton.setSelected(true);
            circleButton = new JRadioButton("Circle");
            squareButton = new JRadioButton("Square");

            figureGroup = new ButtonGroup();
            figureGroup.add(lineButton);
            figureGroup.add(circleButton);
            figureGroup.add(squareButton);

            // Color stuff
            currentColor = Color.BLACK;
            colorButton = new JButton();
            colorButton.setPreferredSize(new Dimension(20, 20));
            colorButton.setBackground(currentColor);
            colorButton.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(this, "Choose a Color", currentColor);
                if (newColor != null) {
                    currentColor = newColor;
                    colorButton.setBackground(currentColor);
                }
            });

            // Add elements to pane
            buttonPanel.add(lineButton);
            buttonPanel.add(circleButton);
            buttonPanel.add(squareButton);
            buttonPanel.add(colorButton);
            add(buttonPanel, BorderLayout.CENTER);
        }

        public Color getCurrentColor() {
            return currentColor;
        }
    }

    class DrawingPane extends JPanel {
        Figure figure;

        DrawingPane() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    figure = new Line(Color.BLUE);
                    Graphics2D g2d = (Graphics2D) getGraphics();

                    figure.startDraw(g2d, e.getX(), e.getY());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Graphics2D g2d = (Graphics2D) getGraphics();

                    figure.finishDraw(g2d, e.getX(), e.getY());
                    figure = null;
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Graphics2D g2d = (Graphics2D) getGraphics();
                    figure.draw(g2d, e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
        }
    }

    public class EditorWindow extends JFrame {
        public EditorWindow() {
            setLayout(new BorderLayout());

            // File menu
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem loadItem = new JMenuItem("Load");
            JMenuItem saveItem = new JMenuItem("Save");

            fileMenu.add(loadItem);
            fileMenu.add(saveItem);
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);

            // Panes
            DrawingPane drawingPane = new DrawingPane();
            EditorPane editorPane = new EditorPane();

            add(drawingPane, BorderLayout.CENTER);
            add(editorPane, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Editor editor = new Editor();
            Editor.EditorWindow wnd = editor.new EditorWindow();
            wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            wnd.setBounds(270, 170, 700, 600);
            wnd.setTitle("Editor");
            wnd.setVisible(true);
        });
    }
}