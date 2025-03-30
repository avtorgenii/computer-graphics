package c3;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor {
    Color backgroundColor = Color.WHITE;

    public abstract class Figure {
        Color color;
        int startX, startY, endX, endY;

        Figure(Color color) {
            this.color = color;
        }

        public int getCenterX() {
            return startX + Math.abs(startX - endX) / 2;
        }

        public int getCenterY() {
            return startY + Math.abs(startY - endY) / 2;
        }

        public void move(Graphics g, int mouseX, int mouseY) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setXORMode(backgroundColor);
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(color);

            // Erase the figure
            switch (this) {
                case Line line -> g2d.drawLine(startX, startY, endX, endY);
                case Rectangle rectangle -> g2d.fillRect(startX, startY, endX - startX, endY - startY);
                case Circle circle -> g2d.fillOval(startX, startY, endX - startX, endY - startY);
                default -> {
                }
            }

            int centerX = getCenterX();
            int centerY = getCenterY();
            int deltaX = mouseX - centerX;
            int deltaY = mouseY - centerY;

            // Update coordinates
            startX += deltaX;
            startY += deltaY;
            endX += deltaX;
            endY += deltaY;

            // Draw the figure at new position
            if (this instanceof Line) {
                g2d.drawLine(startX, startY, endX, endY);
            } else if (this instanceof Rectangle) {
                g2d.fillRect(startX, startY, endX - startX, endY - startY);
            } else if (this instanceof Circle) {
                g2d.fillOval(startX, startY, endX - startX, endY - startY);
            }
        }


        public void remove(Graphics2D g2d) {
            g2d.setXORMode(backgroundColor);
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(color);

            if (this instanceof Line) {
                g2d.drawLine(startX, startY, endX, endY);
            } else if (this instanceof Rectangle) {
                g2d.fillRect(startX, startY, endX - startX, endY - startY);
            } else if (this instanceof Circle) {
                g2d.fillOval(startX, startY, endX - startX, endY - startY);
            }
        }

        public void startDraw(Graphics2D g2d, int x, int y) {
        }

        public void draw(Graphics2D g2d, int x, int y) {
            g2d.setXORMode(backgroundColor); // mode is not preserved across func calls
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(color);
        }

        public void finishDraw(Graphics2D g2d, int x, int y) {
            g2d.setPaintMode();
            g2d.setStroke(new BasicStroke(5));
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

        public void moveEnd(Graphics g, int mouseX, int mouseY) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setXORMode(backgroundColor);
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(color);

            g2d.drawLine(startX, startY, endX, endY);


            // Calculate squared distances
            int distToStartSq = (mouseX - startX) * (mouseX - startX) + (mouseY - startY) * (mouseY - startY);
            int distToEndSq = (mouseX - endX) * (mouseX - endX) + (mouseY - endY) * (mouseY - endY);

            // Update coordinates based on which endpoint is closer
            if (distToStartSq < distToEndSq) {
                // Mouse is closer to start
                startX = mouseX;
                startY = mouseY;
            } else {
                // Mouse is closer to end
                endX = mouseX;
                endY = mouseY;
            }

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
            g2d.fillRect(startX, startY, endX - startX, endY - startY);

            endX = x;
            endY = y;

            // Draw new
            g2d.fillRect(startX, startY, endX - startX, endY - startY);
        }

        @Override
        public void finishDraw(Graphics2D g2d, int x, int y) {
            // Erase previous
            g2d.fillRect(startX, startY, endX - startX, endY - startY);

            super.finishDraw(g2d, x, y);
            endX = x;
            endY = y;

            // Draw new permanent
            g2d.fillRect(startX, startY, endX - startX, endY - startY);
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
            g2d.fillOval(startX, startY, endX - startX, endY - startY);

            endX = x;
            endY = y;

            // Draw new
            g2d.fillOval(startX, startY, endX - startX, endY - startY);
        }

        @Override
        public void finishDraw(Graphics2D g2d, int x, int y) {
            // Erase previous
            g2d.fillOval(startX, startY, endX - startX, endY - startY);

            super.finishDraw(g2d, x, y);
            endX = x;
            endY = y;

            // Draw new permanent
            g2d.fillOval(startX, startY, endX - startX, endY - startY);
        }
    }

    class EditorPane extends JPanel {
        JRadioButton lineButton, circleButton, squareButton, modifyButton;
        JSpinner redSpinner, greenSpinner, blueSpinner;
        ButtonGroup figureGroup;
        Color currentColor;

        EditorPane() {
            setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            // Radio buttons
            modifyButton = new JRadioButton("Modify");
            lineButton = new JRadioButton("Line");
            lineButton.setSelected(true);
            circleButton = new JRadioButton("Circle");
            squareButton = new JRadioButton("Square");

            figureGroup = new ButtonGroup();
            figureGroup.add(lineButton);
            figureGroup.add(circleButton);
            figureGroup.add(squareButton);
            figureGroup.add(modifyButton);

            // RGB Spinners
            redSpinner = createColorSpinner();
            greenSpinner = createColorSpinner();
            blueSpinner = createColorSpinner();

            ChangeListener colorChangeListener = e -> updateColor();
            redSpinner.addChangeListener(colorChangeListener);
            greenSpinner.addChangeListener(colorChangeListener);
            blueSpinner.addChangeListener(colorChangeListener);

            // Add elements to pane
            buttonPanel.add(modifyButton);
            buttonPanel.add(lineButton);
            buttonPanel.add(circleButton);
            buttonPanel.add(squareButton);
            buttonPanel.add(new JLabel("R:"));
            buttonPanel.add(redSpinner);
            buttonPanel.add(new JLabel("G:"));
            buttonPanel.add(greenSpinner);
            buttonPanel.add(new JLabel("B:"));
            buttonPanel.add(blueSpinner);

            add(buttonPanel, BorderLayout.CENTER);
        }

        private JSpinner createColorSpinner() {
            return new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        }

        private void updateColor() {
            int r = (int) redSpinner.getValue();
            int g = (int) greenSpinner.getValue();
            int b = (int) blueSpinner.getValue();
            setBackground(new Color(r, g, b));
        }

        public Color getCurrentColor() {
            return new Color((int) redSpinner.getValue(), (int) greenSpinner.getValue(), (int) blueSpinner.getValue());
        }

        public Figure createSelectedFigure() {
            if (circleButton.isSelected()) {
                return new Circle(getCurrentColor());
            } else if (squareButton.isSelected()) {
                return new Rectangle(getCurrentColor());
            } else if (lineButton.isSelected()) {
                return new Line(getCurrentColor());
            } else {
                return null;
            }
        }

        public boolean isModifying() {
            return modifyButton.isSelected();
        }
    }

    class DrawingPane extends JPanel {
        Figure figure;
        Line line;
        boolean moving = false;
        boolean movingLineEnd = false;
        int movingTol = 50;
        ArrayList<Figure> figures = new ArrayList<Figure>();

        public boolean coordsInTol(int x1, int y1, int x2, int y2) {
            return Math.abs(x1 - x2) <= movingTol && Math.abs(y1 - y2) <= movingTol;
        }

        DrawingPane(EditorPane editorPane) {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) { // left click
                        if (editorPane.isModifying()) {
                            int mouseX = e.getX();
                            int mouseY = e.getY();
                            for (Figure f : figures) {
                                if (f instanceof Line) {
                                    if (coordsInTol(mouseX, mouseY, f.startX, f.startY) || coordsInTol(mouseX, mouseY, f.endX, f.endY)) {
                                        movingLineEnd = true;
                                        line = (Line) f;
                                    } else if (coordsInTol(mouseX, mouseY, f.getCenterX(), f.getCenterY())) {
                                        moving = true;
                                        figure = f;
                                    }
                                } else {
                                    if (coordsInTol(mouseX, mouseY, f.getCenterX(), f.getCenterY())) {
                                        moving = true;
                                        figure = f;
                                    }
                                }
                            }
                        } else {
                            figure = editorPane.createSelectedFigure();
                            if (figure != null) {
                                Graphics2D g2d = (Graphics2D) getGraphics();

                                figure.startDraw(g2d, e.getX(), e.getY());
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) { // left click
                        Graphics2D g2d = (Graphics2D) getGraphics();
                        if (figure != null) {
                            if (moving || movingLineEnd) {
                                moving = false;
                                movingLineEnd = false;
                            } else {
                                figure.finishDraw(g2d, e.getX(), e.getY());
                                figures.add(figure);
                            }
                            figure = null;
                        }
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    Graphics2D g2d = (Graphics2D) getGraphics();
                    if (e.getButton() == MouseEvent.BUTTON3) { // right click
                        int mouseX = e.getX();
                        int mouseY = e.getY();

                        figures.forEach(f -> {
                            if (coordsInTol(mouseX, mouseY, f.getCenterX(), f.getCenterY())) {
                                f.remove(g2d);
                            }
                        });
                        figures.removeIf(f -> coordsInTol(mouseX, mouseY, f.getCenterX(), f.getCenterY()));
                    }
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Graphics2D g2d = (Graphics2D) getGraphics();
                    if (figure != null) {
                        if (moving) {
                            figure.move(g2d, e.getX(), e.getY());
                        } else {
                            figure.draw(g2d, e.getX(), e.getY());
                        }
                    } else if (line != null) {
                        System.out.println("Moving Line End");
                        line.moveEnd(g2d, e.getX(), e.getY());
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // Redraw all figures
            for (Figure f : figures) {
                g2d.setColor(f.color);
                g2d.setStroke(new BasicStroke(5));
                switch (f) {
                    case Line line1 -> g2d.drawLine(f.startX, f.startY, f.endX, f.endY);
                    case Rectangle rectangle -> g2d.fillRect(f.startX, f.startY, f.endX - f.startX, f.endY - f.startY);
                    case Circle circle -> g2d.fillOval(f.startX, f.startY, f.endX - f.startX, f.endY - f.startY);
                    default -> {
                    }
                }
            }
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
            JMenuItem saveBMPItem = new JMenuItem("Save as .png");

            fileMenu.add(loadItem);
            fileMenu.add(saveItem);
            fileMenu.add(saveBMPItem);
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);

            // Panes
            EditorPane editorPane = new EditorPane();
            DrawingPane drawingPane = new DrawingPane(editorPane);

            add(drawingPane, BorderLayout.CENTER);
            add(editorPane, BorderLayout.SOUTH);

            loadItem.addActionListener(e -> loadFigures(drawingPane));
            saveItem.addActionListener(e -> saveFigures(drawingPane));
            saveBMPItem.addActionListener(e -> saveImage(drawingPane));
        }

        private void loadFigures(DrawingPane drawingPane) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("AvtX Files (*.avtx)", "avtx"));
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                ArrayList<Figure> loadedFigures = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;

                    Pattern pattern = Pattern.compile(
                            "<figure=(\\w+),startX=(\\d+),startY=(\\d+),endX=(\\d+),endY=(\\d+),colorR=(\\d+),colorG=(\\d+),colorB=(\\d+)>"
                    );

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            String type = matcher.group(1);
                            int startX = Integer.parseInt(matcher.group(2));
                            int startY = Integer.parseInt(matcher.group(3));
                            int endX = Integer.parseInt(matcher.group(4));
                            int endY = Integer.parseInt(matcher.group(5));
                            int colorR = Integer.parseInt(matcher.group(6));
                            int colorG = Integer.parseInt(matcher.group(7));
                            int colorB = Integer.parseInt(matcher.group(8));

                            Color color = new Color(colorR, colorG, colorB);
                            Figure figure;

                            switch (type.toLowerCase()) {
                                case "line":
                                    figure = new Line(color);
                                    break;
                                case "circle":
                                    figure = new Circle(color);
                                    break;
                                case "rectangle":
                                    figure = new Rectangle(color);
                                    break;
                                default:
                                    System.out.println("Unknown figure type: " + type);
                                    continue;
                            }

                            figure.startX = startX;
                            figure.startY = startY;
                            figure.endX = endX;
                            figure.endY = endY;

                            loadedFigures.add(figure);

                            drawingPane.figures = loadedFigures;
                            drawingPane.repaint();
                        } else {
                            System.out.println("Invalid line format: " + line);
                        }
                    }


                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                }
            }
        }

        private void saveFigures(DrawingPane drawingPane) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("AvtX Files (*.avtx)", "avtx"));
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();


                if (!file.getName().toLowerCase().endsWith(".avtx")) {
                    file = new File(file.getAbsolutePath() + ".avtx");

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        String figureType;
                        int startX, startY, endX, endY, colorR, colorG, colorB;

                        for (Figure figure : drawingPane.figures) {
                            figureType = switch (figure) {
                                case Line line -> "line";
                                case Rectangle rectangle -> "rectangle";
                                case Circle circle -> "circle";
                                case null, default -> "unknown";
                            };

                            startX = figure.startX;
                            startY = figure.startY;
                            endX = figure.endX;
                            endY = figure.endY;

                            colorR = figure.color.getRed();
                            colorG = figure.color.getGreen();
                            colorB = figure.color.getBlue();

                            writer.write(String.format("<figure=%s,startX=%d,startY=%d,endX=%d,endY=%d,colorR=%d,colorG=%d,colorB=%d>", figureType, startX, startY, endX, endY, colorR, colorG, colorB));
                            writer.newLine();
                        }
                    } catch (IOException e) {
                        System.err.println("Error writing file: " + e.getMessage());
                    }
                }
            }
        }

        private void saveImage(DrawingPane drawingPane) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Files (*.png)", "png"));
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();


                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");


                    BufferedImage image = new BufferedImage(drawingPane.getWidth(), drawingPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = image.createGraphics();

                    // Set the background color
                    g2d.setColor(backgroundColor);
                    g2d.fillRect(0, 0, drawingPane.getWidth(), drawingPane.getHeight());

                    for (Figure figure : drawingPane.figures) {
                        figure.finishDraw(g2d, figure.endX, figure.endY);
                    }

                    try {
                        ImageIO.write(image, "png", file);
                    } catch (Exception e) {
                        System.err.println("Error writing file: " + e.getMessage());
                    }
                }
            }

        }
    }

    public static void main(String[] args) {

        Editor editor = new Editor();
        EditorWindow wnd = editor.new EditorWindow();
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setBounds(270, 170, 700, 600);
        wnd.setTitle("Editor");
        wnd.setVisible(true);

    }
}