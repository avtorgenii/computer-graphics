package c6;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Phong {
    public static class Scene {
        public List<LightSource> lightSources;
        public double[] ambientIntensities;

        public double[]diffuseReflection;
        public double[] specularReflection;
        public double[] ambientLightDiffuseReflection;
        public double[] selfLuminance;
        public double[] transparency;

        public double glossiness;

        public int[] imageResolution;
        public String fileName;

        public static class LightSource {
            public double[] location;
            public double[] intensities;
        }
    }

    static class Sphere {
        double radius;
        double[] center;

        public Sphere(double radius, double[] center) {
            this.radius = radius;
            this.center = center;
        }

        public double[] intersection(double x, double y) {
            double r2 = radius * radius;
            double xy2 = x * x + y * y;

            if (xy2 >= r2) {
                return null;
            }

            double z = Math.sqrt(r2 - xy2);
            return new double[] {x, y, z};
        }

        // Calculate the normal vector at a point on the sphere
        public double[] normalAt(double[] point) {
            double[] normal = subtractVectors(point, center);
            return normalize(normal);
        }
    }

    // Vector calculations stuff
    private static double[] addVectors(double[] a, double[] b) {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double[] subtractVectors(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[] multiplyVector(double[] v, double scalar) {
        return new double[] {v[0] * scalar, v[1] * scalar, v[2] * scalar};
    }

    private static double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double vectorLength(double[] v) {
        return Math.sqrt(dotProduct(v, v));
    }

    private static double[] normalize(double[] v) {
        double length = vectorLength(v);
        if (length == 0) return v;
        return multiplyVector(v, 1.0 / length);
    }

    private static Color vectorToColor(double[] v) {
        // Clamp color values to the range [0, 1]
        int r = (int) Math.min(255, Math.max(0, v[0] * 255));
        int g = (int) Math.min(255, Math.max(0, v[1] * 255));
        int b = (int) Math.min(255, Math.max(0, v[2] * 255));
        return new Color(r, g, b);
    }

    // Rendering stuff
    public static double attenuation(double r) {
        double c2, c1, c0;
        c2 = 1.0;
        c1 = 1.0;
        c0 = 1.0;

        return Math.min(1.0, 1.0 / ((c2 * r * r) + (c1 * r) + c0));
    }

    public static double[] applyPhongForHitPoint(Scene scene, double [] hitPoint, double[] normal) {
        double [] res = new double[3]; //rgb


        for(int i = 0;i < 3;i++) {
            double diffuseIntermediate = 0;
            double specularIntermediate = 0;

            // Own illumination
            res[i] += scene.selfLuminance[i];

            // Ambient
            res[i] += scene.ambientLightDiffuseReflection[i];


            // Light sources stuff
            for (int j = 0;j < scene.lightSources.size();j++) {
                double lightSourceIntensity = scene.lightSources.get(j).intensities[i];
                double [] lightSourceLoc = scene.lightSources.get(j).location;

                // Distance to light source
                double r = vectorLength(subtractVectors(lightSourceLoc, hitPoint));

                // Unit vector to light source from point
                double[] I = normalize(subtractVectors(lightSourceLoc, hitPoint));

                diffuseIntermediate = dotProduct(normal, I);
                diffuseIntermediate *= attenuation(r);
                diffuseIntermediate *= lightSourceIntensity;






                // Attenuation


                //




                // Diffuse reflection



                // Specular reflection
            }


            res[i] += diffuseIntermediate * scene.diffuseReflection[i];
            res[i] += specularIntermediate * scene.specularReflection[i];

            // Transparency


        }


        return res;
    }


    public static void renderSceneAndSave(Scene scene) throws IOException {
        int width = scene.imageResolution[0];
        int height = scene.imageResolution[1];

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Sphere sphere = new Sphere(0.5, new double[] {0, 0, 0});

        double ratio = (double) width / height;
        double viewWindowSize = 1.0; // so change of sphere radius is actually visible
        Color pixelColor = Color.BLACK; // defaults to background color

        // Render the scene
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                double x = viewWindowSize * ratio * ((double) (2 * j) / (width - 1) - 1);
                double y = -viewWindowSize * ((double) (2 * i) / (height - 1) - 1);

                double[] hitPoint = sphere.intersection(x, y);


                if (hitPoint != null) {
                    double[] normal = sphere.normalAt(hitPoint);

                    // Visualize normals as RGB
                    double[] colorVector = applyPhongForHitPoint(scene, hitPoint, normal);

                    pixelColor = vectorToColor(colorVector);
                }

                image.setRGB(j, i, pixelColor.getRGB());
            }
        }

        // Save the image to file
        File outputFile = new File(scene.fileName);
        ImageIO.write(image, "png", outputFile);
    }

    public static void showImage(String fileName, int width, int height) {
        JFrame frame = new JFrame("Sphere render");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocation(600, 200);

        ImageIcon imageIcon = new ImageIcon(fileName);

        JLabel imageLabel = new JLabel(imageIcon);
        frame.add(imageLabel);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        // Read data from json
        ObjectMapper mapper = new ObjectMapper();
        Scene scene = mapper.readValue(new File("c6/scene.json"), Scene.class);

        renderSceneAndSave(scene);
        showImage(scene.fileName, scene.imageResolution[0], scene.imageResolution[1]);
    }
}