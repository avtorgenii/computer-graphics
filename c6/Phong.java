package c6;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Phong {
    public static class Scene {
        public List<LightSource> lightSources;
        public double[] ambientIntensities;

        public List<double[]> diffuseReflection;
        public List<double[]> specularReflection;
        public List<double[]> ambientLightDiffuseReflection;
        public List<double[]> selfLuminance;

        public double glossiness;

        public int[] imageResolution;
        public String fileName;

        public static class LightSource {
            public double[] location;
            public double[] intensities;
        }
    }

    public static void main(String[] args) throws IOException {
        // Read data from json
        ObjectMapper mapper = new ObjectMapper();
        Scene scene = mapper.readValue(new File("c6/scene.json"), Scene.class);



    }
}
