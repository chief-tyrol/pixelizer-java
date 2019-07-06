package zone.gryphon.pixelizer;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.Option;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author galen
 */
@Slf4j
public class Pixelizer {

    public static void main(String... args) throws Exception {
        new Pixelizer(args).doMain();
    }

    private final List<File> files;

    public Pixelizer(String...args) throws Exception {
        if(args.length == 0) {
            usage();
            throw new RuntimeException("Will never reach this");
        }

        files = Collections.unmodifiableList(Arrays.stream(args)
                .map(File::new)
                .collect(Collectors.toList()));
    }

    private void usage() {
        System.err.println("Usage: java -jar pixelizer inputFile [outputFile]");
        System.exit(1);
    }

    public void doMain() throws Exception {
        Thread.sleep(50);
        StringBuilder out = new StringBuilder();


        for (int i = 0; i < files.size(); i++) {
            out.append(String.format("// pic[%d] = \"%s\"\n", i, files.get(i).getAbsolutePath()));
        }

        out.append(String.format("const unsigned char pic[%d][8][8][3] PROGMEM = {\n", files.size()));


        for (int i = 0; i < files.size(); i++) {
            out.append(process(files.get(i)));

            if (i < files.size() - 1) {
                out.append(',');
            }

            out.append('\n').append('\n');

        }
        


        out.append("};\n");


        System.out.println(out.toString());
    }

    private String process(File file) {
        StringBuilder builder = new StringBuilder();
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read in " + file.toString(), e);
        }

        Raster raster = img.getRaster();

        final int w = img.getWidth();
        final int h = img.getHeight();

        if (w != 8 || h != 8) {
            throw new IllegalArgumentException("Input image must be exactly 8x8 pixels (actual: " + w + "x" + h + ")");
        }

        long pixels = w * h;


        builder.append("    // ").append(file.getAbsolutePath()).append('\n');
        for (int i = 0; i < w; i++) {

            builder.append("    ");

            if (i == 0) {
                builder.append('{');
            } else {
                builder.append(' ');
            }


            builder.append('{');
            for (int j = 0; j < h; j++) {
                int[] rgb = new int[3];

                for (int channel = 0; channel < 3; channel++) {
                    rgb[channel] = raster.getSample(i, j, channel);
                }
                builder.append(String.format("{%3d,%3d,%3d}", rgb[2], rgb[1], rgb[0]));

                if (j < h - 1) {
                    builder.append(", ");
                }
            }
            builder.append('}');

            if (i < w - 1) {
                builder.append(",\n");
            } else {
                builder.append('}');
            }
        }
        return builder.toString();
    }


}
