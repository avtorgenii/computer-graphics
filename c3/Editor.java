package c3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Editor {
    Color backgroundColor = Color.WHITE;

    public abstract class Figure {
        Color color;
        int startX, startY, endX, endY;

        Figure(Color color) {
            this.color = color;
        }

        public void save() {
        }

        public void startDraw(Graphics2D g2d, int x, int y) {
        }

        public void draw(Graphics2D g2d, int x, int y) {
            g2d.setXORMode(backgroundColor); // somehow this mode is not preserved across func calls
            g2d.setStroke(new BasicStroke(5));
        }

        public void finishDraw(Graphics2D g2d, int x, int y) {
            g2d.setPaintMode();
            g2d.setColor(color);
        }
    }

    public class Line extends Figure {
        Line(Color color) {
            super(color);
        }

        @Override
        public void startDraw(Graphics2D g2d, int x, int y) {
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }

        @Override
        public void draw(Graphics2D g2d, int x, int y) {
            super.draw(g2d, x, y);
            // Erase previous
            g2d.drawLine(startX, startY, endX, endY);

            endX = x;
            endY = y;

            // Draw new
            g2d.drawLine(startX, startY, endX, endY);
        }

        @Override
        public void finishDraw(Graphics2D g2d, int x, int y) {
            // Erase previous
            g2d.drawLine(startX, startY, endX, endY);

            super.finishDraw(g2d, x, y);
            endX = x;
            endY = y;

            // Draw new permanent
            g2d.drawLine(startX, startY, endX, endY);
        }
    }

    public class Rectangle extends Figure {
        Rectangle(Color color) {
            super(color);
        }

        @Override
        public void startDraw(Graphics2D g2d, int x, int y) {
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }

        @Override
        public void draw(Graphics2D g2d, int x, int y) {
            super.draw(g2d, x, y);

            // Erase previous
            g2d.drawRect(startX, startY, endX- startX, endY - startY);

            endX = x;
            endY = y;

            // Draw new
            g2d.drawRect(startX, startY, endX- startX, endY - startY);
        }

        @Override
        public void finishDraw(Graphics2D g2d, int x, int y) {
            // Erase previous
            g2d.drawRect(startX, startY, endX- startX, endY - startY);

            super.finishDraw(g2d, x, y);
            endX = x;
            endY = y;

            // Draw new permanent
            g2d.drawRect(startX, startY, endX- startX, endY - startY);
        }
    }

    public class Circle extends Figure {
        Circle(Color color) {
            super(color);
        }

        @Override
        public void startDraw(Graphics2D g2d, int x, int y) {
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }

        @Override
        public void draw(Graphics2D g2d, int x, int y) {
            super.draw(g2d, x, y);

            // Erase previous
            g2d.drawOval(startX, startY, endX- startX, endY - startY);

            endX = x;
            endY = y;

            // Draw new
            g2d.drawOval(startX, startY, endX- startX, endY - startY);
        }

        @Override
        public void finishDraw(Graphics2D g2d, int x, int y) {
            // Erase previous
            g2d.drawOval(startX, startY, endX- startX, endY - startY);

            super.finishDraw(g2d, x, y);
            endX = x;
            endY = y;

            // Draw new permanent
            g2d.drawOval(startX, startY, endX- startX, endY - startY);
        }
    }

    class EditorPane extends JPanel {
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

        public Figure createSelectedFigure() {
            if (circleButton.isSelected()) {
                return new Circle(getCurrentColor());
            } else if (squareButton.isSelected()) {
                return new Rectangle(getCurrentColor());
            } else {
                return new Line(getCurrentColor());
            }
        }
    }

    class DrawingPane extends JPanel {
        Figure figure;

        DrawingPane(EditorPane editorPane) {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    figure = editorPane.createSelectedFigure();
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
            EditorPane editorPane = new EditorPane();
            DrawingPane drawingPane = new DrawingPane(editorPane);

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