package c4;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Editor extends JPanel {
    public static void addDragDropHandler(JLabel thumbnail) {
        thumbnail.setTransferHandler(new TransferHandler("icon"){
            // Restrict dropping onto image or shape in galleries
            @Override
            public boolean canImport(TransferSupport support) {
                return false;
            }
        });
        thumbnail.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource(); // component from which data is being dragged
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.COPY); // copying the image and not moving it
            }
        });
    }

    // Image Gallery Pane
    static class ImageGalleryPane extends JPanel {
        private final List<BufferedImage> loadedImages = new ArrayList<>();
        private final int PANEL_WIDTH = 150;

        public ImageGalleryPane() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Image Gallery"));

            // Create content panel for thumbnails
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            // Create scroll pane
            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_WIDTH));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            add(scrollPane, BorderLayout.CENTER);

            File imageDir = new File("c4/src/images/");
            if (imageDir.exists() && imageDir.isDirectory()) {
                for (File imgFile : Objects.requireNonNull(imageDir.listFiles())) {
                    try {
                        BufferedImage img = ImageIO.read(imgFile);
                        if (img != null) {
                            loadedImages.add(img);
                            contentPanel.add(createThumbnailLabel(img));
                        }
                    } catch (IOException e) {
                        System.err.println("Could not load: " + imgFile.getName());
                    }
                }
            } else {
                contentPanel.add(new JLabel("No image folder found"));
            }
        }

        private JLabel createThumbnailLabel(BufferedImage img) {
            int originalWidth = img.getWidth();
            int originalHeight = img.getHeight();

            double scale = (double) PANEL_WIDTH / Math.max(originalWidth, originalHeight);
            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            JLabel thumbnail = new JLabel(new ImageIcon(scaled));
            // Add drag'n'drop stuff
            addDragDropHandler(thumbnail);

            return thumbnail;
        }
    }

    // Shape Gallery Pane - Left bottom panel with shapes
    static class ShapeGalleryPane extends JPanel {
        private final List<BufferedImage> loadedShapes = new ArrayList<>();
        private final int PANEL_WIDTH = 150;

        public ShapeGalleryPane() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_WIDTH));
            setBorder(BorderFactory.createTitledBorder("Shapes Gallery"));

            // Create content panel for thumbnails
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            add(contentPanel, BorderLayout.CENTER);

            File imageDir = new File("c4/src/shapes/");
            if (imageDir.exists() && imageDir.isDirectory()) {
                for (File imgFile : Objects.requireNonNull(imageDir.listFiles())) {
                    try {
                        BufferedImage img = ImageIO.read(imgFile);
                        if (img != null) {
                            loadedShapes.add(img);
                            JLabel thumbnail = new JLabel(new ImageIcon(img.getScaledInstance(PANEL_WIDTH, PANEL_WIDTH, Image.SCALE_SMOOTH)));
                            // Add drag'n'drop stuff
                            addDragDropHandler(thumbnail);

                            contentPanel.add(thumbnail);
                        }
                    } catch (IOException e) {
                        System.err.println("Could not load: " + imgFile.getName());
                    }
                }
            } else {
                contentPanel.add(new JLabel("No shapes folder found"));
            }
        }
    }

    // Control Pane - Bottom panel with buttons for adjustments
    static class ControlPane extends JPanel {
        private JButton colorButton;
        private Color currentColor = Color.BLACK;

        public ControlPane() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            setBorder(BorderFactory.createTitledBorder("Controls"));

            // Rotation controls
            JButton rotateLeftButton = new JButton("Rotate -1°");
            JButton rotateRightButton = new JButton("Rotate +1°");

            // Move controls
            JButton moveLeftButton = new JButton("Left 1px");
            JButton moveRightButton = new JButton("Right 1px");
            JButton moveUpButton = new JButton("Up 1px");
            JButton moveDownButton = new JButton("Down 1px");

            // Color chooser button
            colorButton = new JButton("");
            colorButton.setBackground(currentColor);
            colorButton.setPreferredSize(new Dimension(25, 25)); // Make it square
            colorButton.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(
                        this, "Choose Color", currentColor);
                if (newColor != null) {
                    currentColor = newColor;
                    colorButton.setBackground(currentColor);
                }
            });

            // Add buttons to panel
            add(rotateLeftButton);
            add(rotateRightButton);
            add(moveLeftButton);
            add(moveRightButton);
            add(moveUpButton);
            add(moveDownButton);
            add(colorButton);
        }
    }


    // Poster elements classes
    public class PosterElement {
        private boolean canChangeColor;
        private Image img;
        private Point upLeftCorner;
        private int width;
        private int height;

        PosterElement (boolean canChangeColor, Image img, int dropX, int dropY){
            this.canChangeColor = canChangeColor;
            this.img = img;
            this.upLeftCorner = new Point(dropX, dropY);
            this.width = img.getWidth(null);
            this.height = img.getHeight(null);
        }

        public Point getCenter(){
            return new Point(0, 0);
        }

        public void draw(Graphics g) {
            g.drawImage(img, upLeftCorner.x, upLeftCorner.y, width, height, null);
        }
    }

    // Drawing Pane - Right panel for poster composition
    class DrawingPane extends JPanel {
        private final List<PosterElement> elements = new ArrayList<>();

        public DrawingPane() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    return true;
                }
                @Override
                public boolean importData(TransferSupport support) {
                    try {
                        Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);
                        Point dropPoint = support.getDropLocation().getDropPoint();

                        elements.add(new PosterElement(false, img, dropPoint.x, dropPoint.y));
                        repaint();

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (PosterElement element : elements) {
                element.draw(g);
            }
        }
    }

    public class EditorWindow extends JFrame {
        public EditorWindow() {
            setLayout(new BorderLayout());

            // Create menu bar
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem loadItem = new JMenuItem("Load");
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem savePNGItem = new JMenuItem("Save as png");
            fileMenu.add(loadItem);
            fileMenu.add(saveItem);
            fileMenu.add(savePNGItem);
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);

            // Create panels
            ImageGalleryPane imageGalleryPane = new ImageGalleryPane();
            ShapeGalleryPane shapeGalleryPane = new ShapeGalleryPane();
            DrawingPane drawingPane = new DrawingPane();
            ControlPane controlPane = new ControlPane();

            // Left side panel to contain both galleries
            JPanel leftPanel = new JPanel(new GridLayout(2, 1));
            leftPanel.add(imageGalleryPane, BorderLayout.NORTH);
            leftPanel.add(shapeGalleryPane, BorderLayout.SOUTH);

            // Add panels to the frame
            add(leftPanel, BorderLayout.WEST);
            add(drawingPane, BorderLayout.CENTER);
            add(controlPane, BorderLayout.SOUTH);
        }
    }

    public void main(String[] args) {
        Editor.EditorWindow wnd = new EditorWindow();
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setBounds(270, 170, 1000, 800);
        wnd.setTitle("Poster Editor");
        wnd.setVisible(true);
    }
}