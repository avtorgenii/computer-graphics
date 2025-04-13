package c4;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Editor extends JPanel {
    public static void addDragDropHandler(JLabel thumbnail, boolean isImage) {
        thumbnail.setTransferHandler(new TransferHandler() {
            // Restrict dropping onto image or shape in galleries
            @Override
            public boolean canImport(TransferSupport support) {
                return false;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                final ImageIcon icon = (ImageIcon) ((JLabel) c).getIcon();

                // Create a Transferable that supports both flavors
                return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return isImage ?
                                new DataFlavor[]{DataFlavor.imageFlavor} :
                                new DataFlavor[]{DataFlavor.stringFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return isImage ?
                                flavor.equals(DataFlavor.imageFlavor) :
                                flavor.equals(DataFlavor.stringFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor)
                            throws UnsupportedFlavorException {
                        if (isImage && flavor.equals(DataFlavor.imageFlavor)) {
                            return icon.getImage();
                        } else if (!isImage && flavor.equals(DataFlavor.stringFlavor)) {
                            return icon.getDescription();
                        } else {
                            throw new UnsupportedFlavorException(flavor);
                        }
                    }
                };
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
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
            addDragDropHandler(thumbnail, true);

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
                            ImageIcon icon = new ImageIcon(img.getScaledInstance(PANEL_WIDTH, PANEL_WIDTH, Image.SCALE_SMOOTH));
                            // Set a description to identify the shape type
                            icon.setDescription(imgFile.getName());
                            JLabel thumbnail = new JLabel(icon);
                            // Add drag'n'drop stuff
                            addDragDropHandler(thumbnail, false);

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
        protected Point upLeftCorner;
        protected int width;
        protected int height;
        protected boolean selected = false;

        PosterElement(int dropX, int dropY) {
            this.upLeftCorner = new Point(dropX, dropY);
        }

        public void bringToFront(List<PosterElement> elements) {
            if (elements.contains(this)) {
                elements.remove(this);
                elements.add(this); // Adding to the end places it on top
            }
        }

        public Point getCenter() {
            return new Point(0, 0);
        }

        public void draw(Graphics g) {
            if (selected) {
                drawBorder(g);
            }
        }

        public void move(int x, int y) {
            // Calculate the center offset
            int centerOffsetX = width / 2;
            int centerOffsetY = height / 2;

            // Create a translation that positions the element so its center is at the cursor
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(x - centerOffsetX, y - centerOffsetY);

            // Apply the transform to create a new point at the desired position
            transform.transform(new Point(0, 0), upLeftCorner);
        }

        public void setSelected(boolean selected, List<PosterElement> elements) {
            this.selected = selected;
            if (elements != null) {
                bringToFront(elements);
            }
        }

        public boolean trySelect(int mouseX, int mouseY) {
            // Works for rectangular shapes
            boolean inside = mouseX >= upLeftCorner.x && mouseX <= upLeftCorner.x + width &&
                    mouseY >= upLeftCorner.y && mouseY <= upLeftCorner.y + height;

            this.selected = inside;
            return inside;
        }

        public void drawBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); // create copy to avoid side effects
            g2.setColor(Color.RED);

            // Create a dashed stroke
            float[] dashPattern = {5.0f, 5.0f};
            g2.setStroke(new BasicStroke(
                    2,                     // thickness
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,                 // miter limit
                    dashPattern,          // dash pattern
                    0.0f                  // dash phase
            ));

            // Draw the border slightly outside the element for visibility
            g2.drawRect(upLeftCorner.x - 2, upLeftCorner.y - 2, width + 4, height + 4);
        }
    }

    public class ImageElement extends PosterElement {
        Image img;

        ImageElement(int dropX, int dropY, Image img) {
            super(dropX, dropY);
            this.img = img;
            this.width = img.getWidth(null);
            this.height = img.getHeight(null);
        }

        public void draw(Graphics g) {
            super.draw(g);
            g.drawImage(img, upLeftCorner.x, upLeftCorner.y, null);
        }
    }

    public class ShapeElement extends PosterElement {
        boolean isCircle;

        ShapeElement(int dropX, int dropY, boolean isCircle) {
            super(dropX, dropY);
            this.isCircle = isCircle;
            this.width = 150;
            this.height = 150;
        }

        public void draw(Graphics g) {
            super.draw(g);
            if (isCircle) {
                g.fillOval(upLeftCorner.x, upLeftCorner.y, width, height);
            } else {
                g.fillRect(upLeftCorner.x, upLeftCorner.y, width, height);
            }
        }

        public boolean trySelect(int mouseX, int mouseY) {
            if (isCircle) {
                int centerX = upLeftCorner.x + width / 2;
                int centerY = upLeftCorner.y + height / 2;
                int radius = width / 2;

                double dx = mouseX - centerX;
                double dy = mouseY - centerY;

                boolean inside = dx * dx + dy * dy <= radius * radius;
                this.selected = inside;
                return inside;
            } else {
                return super.trySelect(mouseX, mouseY);
            }
        }
    }

    // Drawing Pane - Right panel for poster composition
    class DrawingPane extends JPanel {
        private final List<PosterElement> elements = new ArrayList<>();
        private final ControlPane controlPane;

        public DrawingPane(ControlPane controlPane) {
            this.controlPane = controlPane;
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
                        Point dropPoint = support.getDropLocation().getDropPoint();

                        // Check if the data is an image
                        if (support.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                            Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);
                            elements.add(new ImageElement(dropPoint.x, dropPoint.y, img));
                            repaint();
                            return true;
                        }
                        // Check if the data is text (for shapes)
                        else if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            String shapeInfo = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);

                            if (shapeInfo.contains("circle")) {
                                // Create a circle shape
                                elements.add(new ShapeElement(dropPoint.x, dropPoint.y, true));
                                repaint();
                                return true;
                            } else if (shapeInfo.contains("square")) {
                                // Create a square shape
                                elements.add(new ShapeElement(dropPoint.x, dropPoint.y, false));
                                repaint();
                                return true;
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });

            // Mouse events listeners
            MyMouseAdapter adapter = new MyMouseAdapter(elements, this::repaint);

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(controlPane.currentColor);

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
            ControlPane controlPane = new ControlPane();
            DrawingPane drawingPane = new DrawingPane(controlPane);


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