package c4;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        private List<PosterElement> elements;
        private Runnable repaintCallback;


        public void setElementsAndRepaint(List<PosterElement> elements, Runnable repaintCallback) {
            this.elements = elements;
            this.repaintCallback = repaintCallback;
        }

        public PosterElement getSelectedElement() {
            PosterElement selectedElement = elements.stream().filter(e -> e.selected).findFirst().orElse(null);
            return selectedElement;
        }

        public ControlPane() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            setBorder(BorderFactory.createTitledBorder("Controls"));

            // Rotation controls
            JButton rotateLeftButton = new JButton("Rotate -1°");
            JButton rotateRightButton = new JButton("Rotate +1°");

            rotateLeftButton.addActionListener(e -> {
                DrawingPane.rotate1Degree(-1, getSelectedElement());
                repaintCallback.run();
            });
            rotateRightButton.addActionListener(e -> {
                DrawingPane.rotate1Degree(1, getSelectedElement());
                repaintCallback.run();
            });

            // Move controls
            JButton moveLeftButton = new JButton("Left 1px");
            JButton moveRightButton = new JButton("Right 1px");
            JButton moveUpButton = new JButton("Up 1px");
            JButton moveDownButton = new JButton("Down 1px");

            moveLeftButton.addActionListener(e -> {
                DrawingPane.move(-1, 0, getSelectedElement());
                repaintCallback.run();
            });

            moveRightButton.addActionListener(e -> {
                DrawingPane.move(1, 0, getSelectedElement());
                repaintCallback.run();
            });

            moveUpButton.addActionListener(e -> {
                DrawingPane.move(0, -1, getSelectedElement());
                repaintCallback.run();
            });

            moveDownButton.addActionListener(e -> {
                DrawingPane.move(0, 1, getSelectedElement());
                repaintCallback.run();
            });

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
        protected Color color;
        protected int absoluteWidth;
        protected int absoluteHeight;
        protected int initialWidth;
        protected int initialHeight;
        protected boolean selected = false;
        protected int baseRotationAngleDegree = 5;
        protected AffineTransform transformation;
        protected final int vertexTolPx = 30;

        // For transformations - absolute values
        protected double posX, posY;
        protected double scaleX = 1.0, scaleY = 1.0;
        protected double rotationAngle = 0.0;


        PosterElement(int dropX, int dropY, Color color) {
            this.posX = dropX;
            this.posY = dropY;
            this.color = color;

            transformation = new AffineTransform();
            updateTransformation();
        }

        PosterElement(double posX, double posY, double scaleX, double scaleY, double rotationAngle, int initialWidth, int initialHeight, Color color) {
            this.posX = posX;
            this.posY = posY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.rotationAngle = rotationAngle;
            this.color = color;
            this.initialWidth = initialWidth;
            this.initialHeight = initialHeight;
            this.absoluteWidth = initialWidth; // Initialize absoluteWidth
            this.absoluteHeight = initialHeight; // Initialize absoluteHeight

            transformation = new AffineTransform();
            updateTransformation();
        }

        protected void updateTransformation() {
            transformation = new AffineTransform();

            // Apply in order: translate -> rotate -> scale
            transformation.translate(posX, posY);

            // Rotate around center
            double centerX = absoluteWidth / 2.0;
            double centerY = absoluteHeight / 2.0;
            transformation.rotate(rotationAngle, centerX, centerY);

            // Apply scale
            transformation.scale(scaleX, scaleY);
        }


        public void bringToFront(List<PosterElement> elements) {
            if (elements.contains(this)) {
                elements.remove(this);
                elements.add(this); // Adding to the end places it on top
            }
        }

        public Graphics2D draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            // Helps to deal with resizing of window and drawing for image saving
            AffineTransform existingTransform = g2.getTransform();
            AffineTransform combinedTransform = new AffineTransform(existingTransform);
            combinedTransform.concatenate(this.transformation);

            g2.setTransform(combinedTransform);

            g2.setColor(this.color);
            if (selected) {
                drawBorder(g2);
            }

            return g2;
        }

        public void rotate(int notch, boolean oneDegree) {
            double angleDelta = Math.toRadians((-1) * notch * (oneDegree ? 1 : baseRotationAngleDegree));
            rotationAngle += angleDelta;
            updateTransformation();
        }

        public void move(int dx, int dy) {
            posX += dx;
            posY += dy;
            updateTransformation();
        }

        public void scale(int dx, int dy, Point mouseLoc) {
            try {
                int newWidth = absoluteWidth + dx;
                int newHeight = absoluteHeight + dy;

                // Ensuring values don't turn 0 so image won't disappear
                if (newWidth == 0) newWidth = (dx > 0) ? 1 : -1;
                if (newHeight == 0) newHeight = (dy > 0) ? 1 : -1;

                scaleX = (double) newWidth / initialWidth;
                scaleY = (double) newHeight / initialHeight;

                absoluteWidth = newWidth;
                absoluteHeight = newHeight;

                updateTransformation();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void setSelected(boolean selected, List<PosterElement> elements) {
            this.selected = selected;
            if (elements != null) {
                bringToFront(elements);
            }
        }

        public boolean trySelect(int mouseX, int mouseY) {
            try {
                Point2D mousePoint = new Point2D.Double(mouseX, mouseY);
                Point2D inversePoint = new Point2D.Double();
                AffineTransform inverse = transformation.createInverse();
                inverse.transform(mousePoint, inversePoint);

                boolean inside = inversePoint.getX() >= 0 && inversePoint.getX() <= initialWidth &&
                        inversePoint.getY() >= 0 && inversePoint.getY() <= initialHeight;

                this.selected = inside || nearVertex(mouseX, mouseY); // do not deselect if scaling
                return inside;
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean nearVertex(int mouseX, int mouseY) {
            try {
                Point2D mousePoint = new Point2D.Double(mouseX, mouseY);
                Point2D inversePoint = new Point2D.Double();
                AffineTransform inverse = transformation.createInverse();
                inverse.transform(mousePoint, inversePoint);

                boolean nearVertex;

                nearVertex = (inversePoint.distance(0, 0) <= vertexTolPx || inversePoint.distance(initialWidth, 0) <= vertexTolPx
                        || inversePoint.distance(0, initialHeight) <= vertexTolPx || inversePoint.distance(initialWidth, initialHeight) <= vertexTolPx);

                return nearVertex;
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }
        }

        public void drawBorder(Graphics2D g2) {
            Graphics2D g2Copy = (Graphics2D) g2.create(); // create copy to avoid side effects
            g2Copy.setColor(Color.RED);

            // Create a dashed stroke
            float[] dashPattern = {5.0f, 5.0f};
            g2Copy.setStroke(new BasicStroke(
                    2,                     // thickness
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,                 // miter limit
                    dashPattern,          // dash pattern
                    0.0f                  // dash phase
            ));

            // Draw the border slightly outside the element for visibility
            g2Copy.drawRect(-2, -2, initialWidth + 4, initialHeight + 4);
            g2Copy.dispose();
        }


        public List<Integer> getEdgeCoordinates() {
            int minX, minY, maxX, maxY;

            Point2D[] corners = new Point2D[]{
                    new Point2D.Double(0, 0),                 // Top-left
                    new Point2D.Double(initialWidth, 0),         // Top-right
                    new Point2D.Double(0, initialHeight),        // Bottom-left
                    new Point2D.Double(initialWidth, initialHeight) // Bottom-right
            };

            // Transform all corners using the current transformation
            Point2D[] transformedCorners = new Point2D[4];
            for (int i = 0; i < 4; i++) {
                transformedCorners[i] = new Point2D.Double();
                transformation.transform(corners[i], transformedCorners[i]);
            }

            minX = (int) transformedCorners[0].getX();
            minY = (int) transformedCorners[0].getY();
            maxX = (int) transformedCorners[0].getX();
            maxY = (int) transformedCorners[0].getY();

            for (int i = 1; i < 4; i++) {
                minX = Math.min(minX, (int) transformedCorners[i].getX());
                minY = Math.min(minY, (int) transformedCorners[i].getY());
                maxX = Math.max(maxX, (int) transformedCorners[i].getX());
                maxY = Math.max(maxY, (int) transformedCorners[i].getY());
            }

            return new ArrayList<>(List.of(minX, minY, maxX, maxY));
        }
    }

    public class ImageElement extends PosterElement {
        Image img;

        ImageElement(int dropX, int dropY, Image img) {
            super(dropX, dropY, null);
            this.img = img;
            this.initialWidth = img.getWidth(null);
            this.initialHeight = img.getHeight(null);
            this.absoluteWidth = initialWidth;
            this.absoluteHeight = initialHeight;
        }

        ImageElement(double posX, double posY, double scaleX, double scaleY, double rotationAngle, int initialWidth, int initialHeight, Color color, Image img) {
            super(posX, posY, scaleX, scaleY, rotationAngle, initialWidth, initialHeight, null);
            this.img = img;
            this.absoluteWidth = initialWidth;
            this.absoluteHeight = initialHeight;
        }

        public Graphics2D draw(Graphics g) {
            Graphics2D g2 = super.draw(g);
            g2.drawImage(img, 0, 0, null);
            return g2;
        }
    }

    public class ShapeElement extends PosterElement {
        private final Shape shape;
        private final boolean isCircle;

        ShapeElement(int dropX, int dropY, Color color, boolean isCircle) {
            super(dropX, dropY, color);
            this.initialWidth = 150;
            this.initialHeight = 150;
            this.absoluteWidth = initialWidth;
            this.absoluteHeight = initialHeight;
            this.isCircle = isCircle;
            if (isCircle) {
                shape = new Ellipse2D.Double(0, 0, absoluteWidth, absoluteHeight);
            } else {
                shape = new Rectangle2D.Double(0, 0, absoluteWidth, absoluteHeight);
            }
        }

        ShapeElement(double posX, double posY, double scaleX, double scaleY, double rotationAngle, int initialWidth, int initialHeight, Color color, boolean isCircle) {
            super(posX, posY, scaleX, scaleY, rotationAngle, initialWidth, initialHeight, color);
            this.absoluteWidth = initialWidth;
            this.absoluteHeight = initialHeight;
            this.isCircle = isCircle;
            if (isCircle) {
                shape = new Ellipse2D.Double(0, 0, absoluteWidth, absoluteHeight);
            } else {
                shape = new Rectangle2D.Double(0, 0, absoluteWidth, absoluteHeight);
            }
        }

        public Graphics2D draw(Graphics g) {
            Graphics2D g2 = super.draw(g);
            g2.fill(shape);
            g2.dispose();
            return g2;
        }

        public void drawBorder(Graphics2D g2) {
            Graphics2D g2Copy = (Graphics2D) g2.create();
            g2Copy.setColor(Color.RED);

            // Create a dashed stroke
            float[] dashPattern = {5.0f, 5.0f};
            g2Copy.setStroke(new BasicStroke(
                    2,                   // thickness
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,               // miter limit
                    dashPattern,         // dash pattern
                    0.0f                // dash phase
            ));

            // Draw at origin - transformation handles position
            if (isCircle) {
                g2Copy.drawOval(-2, -2, initialWidth + 4, initialHeight + 4);
            } else {
                g2Copy.drawRect(-2, -2, initialWidth + 4, initialHeight + 4);
            }
            g2Copy.dispose();
        }

        public boolean trySelect(int mouseX, int mouseY) {
            if (isCircle) {
                try {
                    // Transform the mouse point to object space
                    Point2D mousePoint = new Point2D.Double(mouseX, mouseY);
                    Point2D inversePoint = new Point2D.Double();
                    AffineTransform inverse = transformation.createInverse();
                    inverse.transform(mousePoint, inversePoint);

                    // For circle, check if within radius
                    double dx = inversePoint.getX() - (double) initialWidth / 2;
                    double dy = inversePoint.getY() - (double) initialHeight / 2;
                    boolean inside = dx * dx + dy * dy <= ((double) initialWidth / 2) * ((double) initialHeight / 2);

                    this.selected = inside;
                    return inside;
                } catch (NoninvertibleTransformException e) {
                    return false;
                }
            } else {
                return super.trySelect(mouseX, mouseY);
            }
        }

        public boolean nearVertex(int mouseX, int mouseY) {
            if (isCircle) {
                try {
                    Point2D mousePoint = new Point2D.Double(mouseX, mouseY);
                    Point2D inversePoint = new Point2D.Double();
                    AffineTransform inverse = transformation.createInverse();
                    inverse.transform(mousePoint, inversePoint);

                    double radius = Math.min(initialWidth / 2.0, initialHeight / 2.0);

                    double dx = inversePoint.getX();
                    double dy = inversePoint.getY();
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    return Math.abs(distance - radius) <= vertexTolPx;
                } catch (NoninvertibleTransformException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return super.nearVertex(mouseX, mouseY);
            }
        }
    }

    // Drawing Pane - Right panel for poster
    public class DrawingPane extends JPanel {
        public final List<PosterElement> elements = new ArrayList<>();
        private final ControlPane controlPane;

        public static void rotate1Degree(int notch, PosterElement element) {
            element.rotate(notch, false);
        }

        public static void move(int dx, int dy, PosterElement element) {
            element.move(dx, dy);
        }

        public List<Integer> getEdgePoints() {
            if (elements.isEmpty())
                return null;

            // First point represent minimal x and y coordinates that Poster has, second max x and y
            int minX = elements.getFirst().getEdgeCoordinates().getFirst();
            int minY = elements.getFirst().getEdgeCoordinates().get(1);
            int maxX = elements.getFirst().getEdgeCoordinates().get(2);
            int maxY = elements.getFirst().getEdgeCoordinates().get(3);


            for (int i = 1; i < elements.size(); i++) {
                int newMinX = elements.get(i).getEdgeCoordinates().get(0);
                int newMinY = elements.get(i).getEdgeCoordinates().get(1);
                int newMaxX = elements.get(i).getEdgeCoordinates().get(2);
                int newMaxY = elements.get(i).getEdgeCoordinates().get(3);

                minX = Math.min(minX, newMinX);
                minY = Math.min(minY, newMinY);
                maxX = Math.max(maxX, newMaxX);
                maxY = Math.max(maxY, newMaxY);
            }

            return new ArrayList<>(List.of(minX, minY, maxX, maxY));
        }

        public BufferedImage exportAsBufferedImage() {
            List<Integer> edgePoints = getEdgePoints(); // coordinates are local to viewport, can go to negative values
            if (edgePoints == null)
                return null;


            Rectangle bounds = getBounds(); // Minimal values for coords are 0, 0

            int minX = Math.min(edgePoints.getFirst(), bounds.x);
            int minY = Math.min(edgePoints.get(1), bounds.y);
            int maxX = Math.max(edgePoints.get(2), bounds.x + bounds.width);
            int maxY = Math.max(edgePoints.get(3), bounds.y + bounds.height);

            int width = maxX - minX;
            int height = maxY - minY;

            // For images below or to the right of viewport - canvas stretches out to the right and/or down
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();


            // For images above or to the left of viewport - transformation is applied to move images to the right and/or down so they would be drawn on the canvas
            AffineTransform offsetTransform = AffineTransform.getTranslateInstance(-minX, -minY);
            System.out.println(minX + " " + minY);
            g2d.transform(offsetTransform);


            // Set the background color
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);


            // Draw all elements with their original transformations
            for (PosterElement e : elements) {
                // Temporarily disable selection borders for export
                boolean wasSelected = e.selected;
                e.selected = false;
                e.draw(g2d);
                e.selected = wasSelected;
            }

            g2d.dispose();
            return image;
        }

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
                                elements.add(new ShapeElement(dropPoint.x, dropPoint.y, controlPane.currentColor, true));
                                repaint();
                                return true;
                            } else if (shapeInfo.contains("square")) {
                                // Create a square shape
                                elements.add(new ShapeElement(dropPoint.x, dropPoint.y, controlPane.currentColor, false));
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
            addMouseWheelListener(adapter);
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
        public void saveAsPNG(BufferedImage image) {
            if (image == null)
                return;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Files (*.png)", "png"));
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();


                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");

                    try {
                        ImageIO.write(image, "png", file);
                    } catch (Exception e) {
                        System.err.println("Error writing file: " + e.getMessage());
                    }
                }
            }
        }

        public void saveAsAVT(DrawingPane drawingPane) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            // Filter for your custom extension
            fileChooser.setFileFilter(new FileNameExtensionFilter("AvtX Files (*.avtx2)", "avtx2"));
            int returnValue = fileChooser.showSaveDialog(this);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Ensure the file has the correct extension
                if (!file.getName().toLowerCase().endsWith(".avtx2")) {
                    file = new File(file.getAbsolutePath() + ".avtx2");
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    List<PosterElement> elements = drawingPane.elements;

                    int canvasWidth = drawingPane.getWidth();
                    int canvasHeight = drawingPane.getHeight();

                    writer.write(String.format("<object=canvas width=%d height=%d>", canvasWidth, canvasHeight));
                    writer.newLine();

                    for (PosterElement element : elements) {
                        int rgb;
                        if (element.color != null) {
                            rgb = element.color.getRGB();
                        } else {
                            rgb = -1;
                        }

                        double angleDegrees = element.rotationAngle;

                        String commonProperties = String.format(
                                "x=%f y=%f scaleX=%f scaleY=%f angle=%f color=%s width=%d height=%d",
                                element.posX, element.posY,
                                element.scaleX, element.scaleY,
                                angleDegrees,
                                rgb,
                                element.initialWidth,
                                element.initialHeight
                        );

                        if (element instanceof ImageElement imgElement) {
                            String pixelsBase64 = "";
                            try {
                                Image img = imgElement.img;
                                BufferedImage bufferedImage = new BufferedImage(
                                        img.getWidth(null),
                                        img.getHeight(null),
                                        BufferedImage.TYPE_INT_ARGB
                                );

                                // Draw the original image onto the buffered image
                                Graphics2D g2d = bufferedImage.createGraphics();
                                g2d.drawImage(img, 0, 0, null);
                                g2d.dispose();

                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                ImageIO.write(bufferedImage, "png", outputStream);
                                byte[] imageBytes = outputStream.toByteArray();
                                pixelsBase64 = Base64.getEncoder().encodeToString(imageBytes);
                            } catch (IOException e) {
                                System.err.println("Warning: Could not encode image data for saving: " + e.getMessage());
                                e.printStackTrace();
                                pixelsBase64 = "ENCODING_ERROR";
                            }

                            writer.write(String.format("<object=image %s pixels=%s>", commonProperties, pixelsBase64));
                            writer.newLine();
                        } else if (element instanceof ShapeElement shapeElement) {
                            String type = shapeElement.isCircle ? "circle" : "rectangle";

                            writer.write(String.format("<object=%s %s isCircle=%b>",
                                    type, commonProperties, shapeElement.isCircle));
                            writer.newLine();
                        }
                    }
                    System.out.println("Poster saved successfully to " + file.getName());

                } catch (IOException e) {
                    System.err.println("Error writing AVT file: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Could not save the file.\nError: " + e.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    System.err.println("An unexpected error occurred during saving: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "An unexpected error occurred while saving.\nError: " + e.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void loadFromAVT(DrawingPane drawingPane) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("AvtX Files (*.avtx2)", "avtx2"));
            int returnValue = fileChooser.showOpenDialog(this);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Clear existing elements
                    drawingPane.elements.clear();

                    String line;
                    Pattern canvasPattern = Pattern.compile("<object=canvas width=(\\d+) height=(\\d+)>");
                    Pattern commonPropsPattern = Pattern.compile("x=([\\d.\\-]+) y=([\\d.\\-]+) scaleX=([\\d.\\-]+) scaleY=([\\d.\\-]+) angle=([\\d.\\-]+) color=(-?\\d+) width=(\\d+) height=(\\d+)");
                    Pattern imagePattern = Pattern.compile("<object=image (.*?) pixels=(.+)>");
                    Pattern shapePattern = Pattern.compile("<object=(circle|rectangle) (.*?) isCircle=(true|false)>");

                    // Read the canvas size first
                    line = reader.readLine();
                    if (line != null) {
                        Matcher canvasMatcher = canvasPattern.matcher(line);
                        if (canvasMatcher.find()) {
                            int width = Integer.parseInt(canvasMatcher.group(1));
                            int height = Integer.parseInt(canvasMatcher.group(2));
                            drawingPane.setPreferredSize(new Dimension(width, height));
                        }
                    }

                    // Read all elements
                    while ((line = reader.readLine()) != null) {
                        // Handle image elements
                        Matcher imageMatcher = imagePattern.matcher(line);
                        if (imageMatcher.find()) {
                            String properties = imageMatcher.group(1).replace(",", ".");
                            String base64Pixels = imageMatcher.group(2);

                            Matcher propsMatcher = commonPropsPattern.matcher(properties);
                            if (propsMatcher.find()) {
                                drawingPane.elements.add(createImageElement(propsMatcher, base64Pixels));
                                System.out.println(drawingPane.elements.size());
                            }
                            continue;
                        }

                        // Handle shape elements
                        Matcher shapeMatcher = shapePattern.matcher(line);
                        if (shapeMatcher.find()) {
                            String shapeType = shapeMatcher.group(1);
                            String properties = shapeMatcher.group(2).replace(",", ".");
                            boolean isCircle = Boolean.parseBoolean(shapeMatcher.group(3));

                            Matcher propsMatcher = commonPropsPattern.matcher(properties);
                            if (propsMatcher.find()) {
                                drawingPane.elements.add(createShapeElement(propsMatcher, isCircle));
                                System.out.println(drawingPane.elements.size());
                            }
                        }
                    }

                    // Repaint to show loaded elements
                    drawingPane.repaint();
                    System.out.println("Poster loaded successfully from " + file.getName());

                } catch (IOException e) {
                    System.err.println("Error reading AVT file: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Could not load the file.\nError: " + e.getMessage(),
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    System.err.println("An unexpected error occurred during loading: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "An unexpected error occurred while loading.\nError: " + e.getMessage(),
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private ImageElement createImageElement(Matcher matcher, String base64Pixels) throws IOException {
            double posX = Double.parseDouble(matcher.group(1));
            double posY = Double.parseDouble(matcher.group(2));
            double scaleX = Double.parseDouble(matcher.group(3));
            double scaleY = Double.parseDouble(matcher.group(4));
            double angle = Double.parseDouble(matcher.group(5));
            int colorValue = Integer.parseInt(matcher.group(6));
            int width = Integer.parseInt(matcher.group(7));
            int height = Integer.parseInt(matcher.group(8));

            if (base64Pixels.equals("ENCODING_ERROR")) {
                throw new IOException("Cannot load image with encoding error");
            }

            // Decode the Base64 string back to an image
            byte[] imageBytes = Base64.getDecoder().decode(base64Pixels);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);

            // Create the image element
            Color elementColor = colorValue != -1 ? new Color(colorValue) : null;
            ImageElement imgElement = new ImageElement(posX, posY, scaleX, scaleY, angle, width, height, elementColor, bufferedImage);
            return imgElement;
        }

        private ShapeElement createShapeElement(Matcher matcher, boolean isCircle) {
            double posX = Double.parseDouble(matcher.group(1));
            double posY = Double.parseDouble(matcher.group(2));
            double scaleX = Double.parseDouble(matcher.group(3));
            double scaleY = Double.parseDouble(matcher.group(4));
            double angle = Double.parseDouble(matcher.group(5));
            int colorValue = Integer.parseInt(matcher.group(6));
            int width = Integer.parseInt(matcher.group(7));
            int height = Integer.parseInt(matcher.group(8));

            Color color = new Color(colorValue);

            // Create the shape element
            ShapeElement shapeElement = new ShapeElement(posX, posY, scaleX, scaleY, angle, width, height, color, isCircle);
            return shapeElement;
        }

        public EditorWindow() {
            setLayout(new BorderLayout());

            // Create panels
            ImageGalleryPane imageGalleryPane = new ImageGalleryPane();
            ShapeGalleryPane shapeGalleryPane = new ShapeGalleryPane();
            ControlPane controlPane = new ControlPane();
            DrawingPane drawingPane = new DrawingPane(controlPane);
            controlPane.setElementsAndRepaint(drawingPane.elements, drawingPane::repaint);

            // Create menu bar
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem loadItem = new JMenuItem("Load");
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem savePNGItem = new JMenuItem("Save as png");
            savePNGItem.addActionListener(e -> saveAsPNG(drawingPane.exportAsBufferedImage()));
            saveItem.addActionListener(e -> saveAsAVT(drawingPane));
            loadItem.addActionListener(e -> loadFromAVT(drawingPane));

            fileMenu.add(loadItem);
            fileMenu.add(saveItem);
            fileMenu.add(savePNGItem);
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);

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