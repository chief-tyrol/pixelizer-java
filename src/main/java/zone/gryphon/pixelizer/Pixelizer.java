/*
 * Copyright 2019-2019 Gryphon Zone
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zone.gryphon.pixelizer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tyrol
 */
public class Pixelizer {

    public static void main(String... args) {
        new Pixelizer(args).doMain();
    }

    @Option(name = "-h", aliases = {"--help"}, help = true)
    private boolean help = false;

    @Argument(metaVar = "file", usage = "Input image file to process. Must be exactly 8x8 pixels in size")
    private List<File> files = new ArrayList<>();

    public Pixelizer(String... args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            throw new RuntimeException("Invalid arguments", e);
        }

        if (args.length == 0 || help) {
            System.err.print("Usage: pixelizer");
            parser.printSingleLineUsage(System.err);
            System.err.println();
            parser.printUsage(System.err);
            System.exit(1);
        }
    }

    private void doMain() {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < files.size(); i++) {
            out.append(String.format("// pic[%d] = \"%s\"\n", i, files.get(i).getAbsolutePath()));
        }

        out.append("\n");

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

        builder.append("\n    // ").append(file.getAbsolutePath()).append('\n');
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
                builder.append(String.format("{%#2x,%#2x,%#2x}", rgb[2], rgb[1], rgb[0]));

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
