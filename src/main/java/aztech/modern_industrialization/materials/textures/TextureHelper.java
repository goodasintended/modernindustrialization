/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.materials.textures;

import java.util.function.BiFunction;
import net.minecraft.client.texture.NativeImage;

public class TextureHelper {
    public static void colorize(NativeImage image, int r, int g, int b) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int color = image.getPixelColor(i, j);
                // relative luminance
                double l = (0.2126 * getR(color) + 0.7152 * getG(color) + 0.0722 * getB(color)) / 255;
                image.setPixelColor(i, j, fromArgb(getA(color), l * r, l * g, l * b));
            }
        }
    }

    /**
     * Blend top on top of source.
     */
    public static void blend(NativeImage source, NativeImage top) {
        if (source.getWidth() != top.getWidth()) {
            throw new RuntimeException(
                    "Textures have mismatched widths. Source has width " + source.getWidth() + " and top has width " + top.getWidth());
        }
        if (source.getHeight() != top.getHeight()) {
            throw new RuntimeException(
                    "Textures have mismatched heights. Source has height " + source.getHeight() + " and top has height " + top.getHeight());
        }
        for (int i = 0; i < source.getWidth(); ++i) {
            for (int j = 0; j < source.getHeight(); ++j) {
                int sourceColor = source.getPixelColor(i, j);
                int topColor = top.getPixelColor(i, j);
                double alphaSource = getA(sourceColor) / 255.0;
                double alphaTop = getA(topColor) / 255.0;
                double alphaOut = alphaTop + alphaSource * (1 - alphaTop);
                BiFunction<Integer, Integer, Integer> mergeAlpha = (sourceValue,
                        topValue) -> (int) ((topValue * alphaTop + sourceValue * alphaSource * (1 - alphaTop)) / alphaOut);
                source.setPixelColor(i, j, fromArgb((int) (alphaOut * 255), mergeAlpha.apply(getR(sourceColor), getR(topColor)),
                        mergeAlpha.apply(getG(sourceColor), getG(topColor)), mergeAlpha.apply(getB(sourceColor), getB(topColor))));
            }
        }
    }

    public static void doubleIngot(NativeImage image) {
        // Copy and shift down
        NativeImage lowerIngot = new NativeImage(image.getWidth(), image.getHeight(), true);
        lowerIngot.copyFrom(image);
        int shiftDown = lowerIngot.getHeight() * 2 / 16;
        for (int x = 0; x < lowerIngot.getWidth(); ++x) {
            for (int y = lowerIngot.getHeight(); y-- > 0;) {
                if (y >= shiftDown) {
                    lowerIngot.setPixelColor(x, y, lowerIngot.getPixelColor(x, y - shiftDown));
                } else {
                    lowerIngot.setPixelColor(x, y, 0);
                }
            }
        }
        // Copy and shift up
        NativeImage upperIngot = new NativeImage(image.getWidth(), image.getHeight(), true);
        upperIngot.copyFrom(image);
        int shiftUp = upperIngot.getHeight() * 2 / 16;
        for (int x = 0; x < upperIngot.getWidth(); ++x) {
            for (int y = 0; y < upperIngot.getHeight(); ++y) {
                if (y + shiftUp < upperIngot.getHeight()) {
                    upperIngot.setPixelColor(x, y, upperIngot.getPixelColor(x, y + shiftUp));
                } else {
                    upperIngot.setPixelColor(x, y, 0);
                }
            }
        }
        blend(lowerIngot, upperIngot);
        image.copyFrom(lowerIngot);
        lowerIngot.close();
        upperIngot.close();
    }

    private static int getA(int color) {
        return (color >> 24) & 0xff;
    }

    private static int getR(int color) {
        return color & 0xff;
    }

    private static int getG(int color) {
        return (color >> 8) & 0xff;
    }

    private static int getB(int color) {
        return (color >> 16) & 0xff;
    }

    // double values are from 0 to 255!!!!!
    private static int fromArgb(int a, double r, double g, double b) {
        return fromArgb(a, (int) r, (int) g, (int) b);
    }

    private static int fromArgb(int a, int r, int g, int b) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}