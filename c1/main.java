package c1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static javax.imageio.ImageIO.read;
import static javax.imageio.ImageIO.write;


public class main {
    static int int2RGB(int red, int green, int blue) {
        // Make sure that color intensities are in 0..255 range
        red = red & 0x000000FF;
        green = green & 0x000000FF;
        blue = blue & 0x000000FF;

        // Assemble packed RGB using bit shift operations
        return (red << 16) + (green << 8) + blue;
    }

    public static void ringsMask(BufferedImage image, String outputImage) {
        System.out.println("Ring mask synthesis");

        int x_res, y_res;

        int x_c, y_c;

        int black;

        int i, j;

        final int w = 10;

        x_res = image.getWidth();
        y_res = image.getHeight();


        black = int2RGB(0, 0, 0);


        x_c = x_res / 2;
        y_c = y_res / 2;

        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                double d;
                int r;

                // Calculate distance to the image center
                d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                // Find the ring index
                r = (int) d / w;

                // Make decision on the pixel color
                // based on the ring index
                if (r % 2 == 0)
                    // Even ring - set black color
                    image.setRGB(j, i, black);
            }

        try {
            ImageIO.write(image, "bmp", new File(outputImage));
            System.out.println("Ring mask image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void ringsOverlay(String imagePath1, String imagePath2, String outputImagePath) {
        System.out.println("Ring overlay synthesis");

        BufferedImage image1, image2, outputImage;

        try {
            image1 = ImageIO.read(new File(imagePath1));
            image2 = ImageIO.read(new File(imagePath2));
        } catch (Exception e) {
            System.out.println("The image cannot be stored");
            return;
        }


        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            System.out.println("Images have different dimensions, aborting...");
            return;
        }

        int x_res, y_res;

        int x_c, y_c;

        int i, j;

        final int w = 10;

        x_res = image1.getWidth();
        y_res = image1.getHeight();

        outputImage = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);


        x_c = x_res / 2;
        y_c = y_res / 2;

        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                double d;
                int r;

                // Calculate distance to the image center
                d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                // Find the ring index
                r = (int) d / w;

                // Make decision on the pixel color
                // based on the ring index
                if (r % 2 == 0)
                    // Even ring - set black color
                    outputImage.setRGB(j, i, image1.getRGB(j, i));
                else
                    outputImage.setRGB(j, i, image2.getRGB(j, i));
            }

        try {
            ImageIO.write(outputImage, "bmp", new File(outputImagePath));
            System.out.println("Ring overlay image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }

    }

    public static void rings(int x_res, int y_res, int width, String path) {
        System.out.println("Ring pattern synthesis");

        BufferedImage image;

        // Initialize the image with the specified resolution and RGB format
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int x_c, y_c;

        int black;
        int white;

        int i, j;


        black = int2RGB(0, 0, 0);
        white = int2RGB(255, 255, 255);


        x_c = x_res / 2;
        y_c = y_res / 2;

        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                double d;
                int r;

                // Calculate distance to the image center
                d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                // Find the ring index
                r = (int) d / width;

                // Make decision on the pixel color
                // based on the ring index
                if (r % 2 == 0)
                    // Even ring - set black color
                    image.setRGB(j, i, black);
                else
                    // Odd ring - set white color
                    image.setRGB(j, i, white);
            }

        try {
            ImageIO.write(image, "bmp", new File(path));
            System.out.println("Ring mask image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void fuzzyRings(int x_res, int y_res, String path) {
        System.out.println("Ring pattern synthesis");

        // BufferedImage object to hold the image
        BufferedImage image;

        // Coordinates of the image center
        int x_c, y_c;

        // RGB values for black and white colors
        int black, white;

        // Loop variables for iterating over pixels
        int i, j;

        // Fixed ring width
        final int w = 50;

        // Initialize the image with the specified resolution and RGB format
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        // Define black and white colors in RGB format
        black = int2RGB(0, 0, 0);
        white = int2RGB(255, 255, 255);

        // Calculate the center coordinates of the image
        x_c = x_res / 2;
        y_c = y_res / 2;

        // Iterate through each pixel in the image
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                double d;
                int r;
                int l;
                int color;

                // Compute the Euclidean distance from the current pixel to the center
                d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                // Determine the ring index based on the distance
                r = (int) d / w;

                // Calculate grey level based on distance from center
                // Usage of Math.PI instead of 3.14 causes black dots and white rings
                l = (int) (128 * (Math.sin((3.14 * d) / w) + 1));

                color = int2RGB(l, l, l);

                // Assign black or white color based on the ring index
                image.setRGB(j, i, color);
            }
        }

        // Save the generated image to the specified file path
        try {
            ImageIO.write(image, "bmp", new File(path));
            System.out.println("Ring image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void gridMask(BufferedImage image, String outputImage, boolean overrideBg, int l_width, int d_x, int d_y, int c_grid, Integer c_bg) {
        System.out.println("Grid mask synthesis");

        int x_res = image.getWidth();
        int y_res = image.getHeight();
        int i, j, k;

        // Background
        if (overrideBg) {
            for (i = 0; i < y_res; i++) {
                for (j = 0; j < x_res; j++) {
                    image.setRGB(j, i, c_bg);
                }
            }
        }

        // Horizontal lines
        for (k = d_y / 2; k < y_res; k += l_width + d_y) {
            for (i = k; i < k + l_width && i < y_res; i++) {
                for (j = 0; j < x_res; j++) {
                    image.setRGB(j, i, c_grid);
                }
            }
        }

        // Vertical lines
        for (k = d_x / 2; k < x_res; k += l_width + d_x) {
            for (i = k; i < k + l_width && i < x_res; i++) {
                for (j = 0; j < y_res; j++) {
                    image.setRGB(i, j, c_grid);
                }
            }
        }


        try {
            ImageIO.write(image, "bmp", new File(outputImage));
            System.out.println("Grid image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void gridOverlay(String imagePath1, String imagePath2, String outputImagePath, int l_width, int d_x, int d_y) {
        System.out.println("Grid overlay synthesis");

        BufferedImage image1, image2, outputImage;

        try {
            image1 = ImageIO.read(new File(imagePath1));
            image2 = ImageIO.read(new File(imagePath2));
        } catch (Exception e) {
            System.out.println("The image cannot be stored");
            return;
        }


        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            System.out.println("Images have different dimensions, aborting...");
            return;
        }

        int x_res = image1.getWidth();
        int y_res = image1.getHeight();

        outputImage = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int i, j, k;

        // Background
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                outputImage.setRGB(j, i, image1.getRGB(j, i));
            }
        }

        // Horizontal lines
        for (k = d_y / 2; k < y_res; k += l_width + d_y) {
            for (i = k; i < k + l_width && i < y_res; i++) {
                for (j = 0; j < x_res; j++) {
                    outputImage.setRGB(j, i, image2.getRGB(j, i));
                }
            }
        }

        // Vertical lines
        for (k = d_x / 2; k < x_res; k += l_width + d_x) {
            for (i = k; i < k + l_width && i < x_res; i++) {
                for (j = 0; j < y_res; j++) {
                    outputImage.setRGB(i, j, image2.getRGB(i, j));
                }
            }
        }


        try {
            ImageIO.write(outputImage, "bmp", new File(outputImagePath));
            System.out.println("Grid overlay image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void colorGrid(int x_res, int y_res, int l_width, int d_x, int d_y, int c_grid, int c_bg, String path) {
        System.out.println("Grid pattern synthesis");

        // Initialize the image with the specified resolution and RGB format
        BufferedImage image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);


        gridMask(image, path, true, l_width, d_x, d_y, c_grid, c_bg);
    }

    public static void chessMask(BufferedImage image, String outputImage, boolean overrideBg, int c_size, int c_color1, int c_color2) {
        System.out.println("Chess mask synthesis");

        // Loop variables for iterating
        int i, j, k, l;

        int x_res = image.getWidth();
        int y_res = image.getHeight();

        int c_id = 0;

        // First type cells
        if (overrideBg) {
            for (i = 0; i < y_res; i++) {
                for (j = 0; j < x_res; j++) {
                    image.setRGB(j, i, c_color1);
                }
            }
        }

        // Second type cells
        for (k = 0; k < y_res; k += c_size) {
            for (l = (c_id % 2) * c_size; l < x_res; l += 2 * c_size) {
                for (j = k; j < c_size + k && j < x_res; j++) {
                    for (i = l; i < c_size + l && i < y_res; i++) {
                        image.setRGB(i, j, c_color2);
                    }
                }
            }
            c_id++;
        }


        try {
            ImageIO.write(image, "bmp", new File(outputImage));
            System.out.println("Grid image created successfully");
        } catch (
                IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void chessOverlay(String imagePath1, String imagePath2, String outputImagePath, int c_size) {
        System.out.println("Chess overlay synthesis");

        BufferedImage image1, image2, outputImage;

        try {
            image1 = ImageIO.read(new File(imagePath1));
            image2 = ImageIO.read(new File(imagePath2));
        } catch (Exception e) {
            System.out.println("The image cannot be stored");
            return;
        }


        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            System.out.println("Images have different dimensions, aborting...");
            return;
        }

        int x_res = image1.getWidth();
        int y_res = image1.getHeight();

        outputImage = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        // Loop variables for iterating
        int i, j, k, l;

        int c_id = 0;

        // First type cells
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                outputImage.setRGB(j, i, image1.getRGB(j, i));
            }
        }


        // Second type cells
        for (k = 0; k < y_res; k += c_size) {
            for (l = (c_id % 2) * c_size; l < x_res; l += 2 * c_size) {
                for (j = k; j < c_size + k && j < x_res; j++) {
                    for (i = l; i < c_size + l && i < y_res; i++) {
                        outputImage.setRGB(j, i, image2.getRGB(j, i));
                    }
                }
            }
            c_id++;
        }


        try {
            ImageIO.write(outputImage, "bmp", new File(outputImagePath));
            System.out.println("Chess overlay image created successfully");
        } catch (
                IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void chessGrid(int x_res, int y_res, int c_size, int c_color1, int c_color2, String path) {
        System.out.println("Chess pattern synthesis");

        // Initialize the image with the specified resolution and RGB format
        BufferedImage image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        chessMask(image, path, true, c_size, c_color1, c_color2);
    }

    public static void circlesWithBg(int x_res, int y_res, int b_width, int d_xy, int c_circle, int c_bg, String path) {
        System.out.println("Circles with background pattern synthesis");

        BufferedImage image;
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int x_c, y_c;


        int i, j, k, l;


        // Fill out background
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                image.setRGB(j, i, c_bg);
            }
        }

        // Create circles
        for (k = -b_width / 2; k < y_res; k += (b_width + d_xy)) {
            for (l = -b_width / 2; l < x_res; l += (b_width + d_xy)) {
                y_c = k + (b_width / 2);
                x_c = l + (b_width / 2);

                for (i = k; i < k + b_width && i < y_res; i++) {
                    for (j = l; j < l + b_width && j < x_res; j++) {
                        double d;

                        // Calculate distance to the ring center
                        d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                        // Fill circle only if distance from pixel to center is smaller than radius and if coordinates are in bound
                        if ((d <= (double) (b_width / 2)) && i > 0 && j > 0)
                            image.setRGB(j, i, c_circle);
                    }
                }
            }
        }


        try {
            ImageIO.write(image, "bmp", new File(path));
            System.out.println("Circles with background pattern image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void manyRings(int x_res, int y_res, int b_width, int r_width, String path) {
        System.out.println("Many Rings pattern synthesis");

        BufferedImage image;
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int x_c, y_c;

        int black;
        int white;

        int i, j, k, l;


        black = int2RGB(0, 0, 0);
        white = int2RGB(255, 255, 255);


        for (k = 0; k < y_res; k += b_width) {
            for (l = 0; l < x_res; l += b_width) {
                y_c = k + (b_width / 2);
                x_c = l + (b_width / 2);

                for (i = k; i < k + b_width; i++) {
                    for (j = l; j < l + b_width; j++) {
                        double d;
                        int r;

                        // Calculate distance to the ring center
                        d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                        // Find the ring index
                        r = (int) d / r_width;

                        // Make decision on the pixel color
                        // based on the ring index
                        if (r % 2 == 0)
                            // Even ring - set black color
                            image.setRGB(j, i, black);
                        else
                            // Odd ring - set white color
                            image.setRGB(j, i, white);
                    }
                }
            }
        }


        try {
            ImageIO.write(image, "bmp", new File(path));
            System.out.println("Many rings pattern image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void diamonds(int x_res, int y_res, int diagonal, int c_color1, int c_color2, String path){
        System.out.println("Diamonds pattern synthesis");

        BufferedImage image;
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int x_c, y_c;

        int i, j, k, l;

        // Fill background
        for(i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                image.setRGB(j, i, c_color1);
            }
        }

        // Fill diamonds
        for(k = 0;k < y_res; k += diagonal) {
            for(l = 0; l < x_res; l += diagonal) {
                y_c = k + (diagonal / 2);
                x_c = l + (diagonal / 2);
                for(i = k; i < k + diagonal && i < y_res; i++) {
                    for(j = l; j < l + diagonal && j < x_res; j++) {
                        double d = Math.abs(i - y_c) + Math.abs(j - x_c);

                        if (d <= (double) diagonal / 2) {
                            image.setRGB(j, i, c_color2);
                        }
                    }
                }
            }
        }



        try {
            ImageIO.write(image, "bmp", new File(path));
            System.out.println("Diamonds pattern image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void convergingPaths(int x_res, int y_res, int angle, int c_color1, int c_color2, String path){
        System.out.println("Converging paths pattern synthesis");

        BufferedImage image;
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int x_c = x_res / 2;
        int y_c = y_res / 2;

        int i, j;

        int smth;


        for(i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                double oppositeSide = Math.abs(i - y_c);
                double side = Math.abs(j - x_c);
                double hypotenuse = Math.sqrt(oppositeSide * oppositeSide + side * side);

                double angleRads = Math.asin(oppositeSide / hypotenuse);
                double angleDegrees = Math.toDegrees(angleRads);

                int lowerBound = (int) (Math.floor(angleDegrees / angle) * angle);


                boolean isAboveCenter = i - y_c < 0;
                boolean isRightOfCenter = j - x_c >= 0;

                if (isRightOfCenter && isAboveCenter) {
                    smth = 0;
                } else if (!isRightOfCenter && isAboveCenter) {
                    smth = 1;
                } else if (!isRightOfCenter && !isAboveCenter) {
                    smth = 0;
                } else {
                    smth = 1;
                }

                if((lowerBound / angle) % 2 == smth) {
                    image.setRGB(j, i, c_color1);
                }
                else {
                    image.setRGB(j, i, c_color2);
                }

            }
        }



        try {
            ImageIO.write(image, "bmp", new File(path));
            System.out.println("Converging paths pattern image created successfully");
        } catch (IOException e) {
            System.out.println("The image cannot be stored");
        }
    }

    public static void main(String[] args) {
        // 1
        fuzzyRings(500, 550, "c1/rings.bmp");

        int c_grid = int2RGB(0, 0, 0);
        int c_bg = int2RGB(100, 255, 255);
        colorGrid(500, 500, 20, 30, 30, c_grid, c_bg, "c1/grid.bmp");

        chessGrid(500, 500, 50, c_grid, c_bg, "c1/chess.bmp");

        //2
        int patternColor = int2RGB(0, 0, 0);

        try {
            BufferedImage sonic = read(new File("c1/sonic.jpg"));
            gridMask(sonic, "c1/grid_sonic.bmp", false, 20, 30, 30, patternColor, null);

            sonic = read(new File("c1/sonic.jpg"));
            ringsMask(sonic, "c1/ring_sonic.bmp");

            sonic = read(new File("c1/sonic.jpg"));
            chessMask(sonic, "c1/chess_sonic.bmp", false, 50, patternColor, patternColor);
        } catch (IOException e) {
            System.out.println(e);
        }

        // 3
        rings(500, 500, 3, "c1/rings.bmp");

        int c_1 = int2RGB(0, 0, 0);
        int c_2 = int2RGB(255, 255, 255);
        circlesWithBg(500, 500, 50, 10, c_1, c_2, "c1/circles_bg.bmp");

        manyRings(500, 500, 100, 10, "c1/many_rings_bg.bmp");

        diamonds(500, 500, 50, c_1, c_2, "c1/diamonds.bmp");
        convergingPaths(500, 500, 15, c_1, c_2, "c1/converging_paths.bmp");


        // 4
        ringsOverlay("c1/sonic.jpg", "c1/eggman.jpg", "c1/rings_overlay.bmp");
        gridOverlay("c1/sonic.jpg", "c1/eggman.jpg", "c1/grid_overlay.bmp", 20, 50, 70);
        chessOverlay("c1/sonic.jpg", "c1/eggman.jpg", "c1/chess_overlay.bmp", 50);
    }
}