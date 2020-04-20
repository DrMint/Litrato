package com.example.litrato.filters.tools;

/**
 * This class implements tools used by any filter that uses convolution without RenderScript.
 * This class will be deprecated as soon as all convolution is done through RS.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2019-01-08
 */
@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
@Deprecated public class ConvolutionTools {

    /**
     *  Apply the convolution kernel to the image.
     *  This function is only for uniform kernels (where all the weights are the same).
     *  Convolution isn't apply on the borders.
     *  @param pixels the image's pixels
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     *  @param kernel the kernel used for convolution.
     *  @param kernelWidth the kernel width
     *  @param kernelHeight the kernel height
     */
    @Deprecated public static void convolution2D(final int[] pixels, final int imageWidth, final int imageHeight, final int[] kernel, final int kernelWidth, final int kernelHeight) {
        int[] output = new int[imageWidth * imageHeight];

        final int sizeX = (kernelWidth - 1) / 2;
        final int sizeY = (kernelHeight - 1) / 2;
        int index;

        for (int x = sizeX; x < imageWidth - sizeX; x++) {
            for (int y = sizeY; y < imageHeight - sizeY; y++) {
                index = x + y * imageWidth;

                for (int convX = -sizeX; convX <= sizeX; convX++) {
                    for (int convY = -sizeY; convY <= sizeY; convY++) {
                        output[index] += pixels[x + convX + (y + convY) * imageWidth] * kernel[convX + sizeX + (convY + sizeY) * kernelWidth];
                    }
                }
            }
        }
        normalizeOutput(pixels, output, kernel);
    }

    /**
     *  Apply the convolution kernel to the image.
     *  This function is only for uniform kernels (where all the weights are the same).
     *  Convolution isn't apply on the borders.
     *  @param pixels the image's pixels
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     *  @param kernelWidth the kernel width
     *  @param kernelHeight the kernel height
     */
    @Deprecated public static void convolution2DUniform(final int[] pixels, final int imageWidth, final int imageHeight, final int kernelWidth, final int kernelHeight) {
        int[] output = new int[imageWidth * imageHeight];

        final int sizeX = (kernelWidth - 1) / 2;
        final int sizeY = (kernelHeight - 1) / 2;
        int index;

        for (int x = sizeX; x < imageWidth - sizeX; x++) {
            for (int y = sizeY; y < imageHeight - sizeY; y++) {
                index = x + y * imageWidth;

                for (int convX = -sizeX; convX <= sizeX; convX++) {
                    for (int convY = -sizeY; convY <= sizeY; convY++) {
                        output[index] += pixels[x + convX + (y + convY) * imageWidth];
                    }
                }
            }
        }
        normalizeOutput(pixels, output, 0, kernelWidth * kernelHeight);
    }

    /**
     *  Apply the convolution kernel to the image. It only does it in one direction. If correctBorders is true,
     *  when the kernel will be partially outside the image, all coordinates will be remapped to the nearest valid pixel.
     *  @param pixels the image's pixels
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     *  @param horizontal will apply the convolution horizontally if true, otherwise vertically.
     *  @param correctBorders will apply the convolution even on the borders, otherwise not.
     */
    @Deprecated public static void convolution1D(final int[] pixels, final int imageWidth, final int imageHeight, final int[] kernel, final boolean horizontal, final boolean correctBorders) {
        int[] output = new int[imageWidth * imageHeight];

        final int size = (kernel.length - 1) / 2;
        int index;

        if (horizontal) {

            // Apply the gaussian filter for each row over the work already done
            for (int y = 0; y < imageHeight; y++) {

                if (correctBorders) {
                    int correctedX;

                    // For all pixels less than size pixel away from the left side of the image
                    for (int x = 0; x < size; x++) {
                        index = x + y * imageWidth;
                        for (int convX = -size; convX <= size; convX++) {
                            correctedX = Math.max(x + convX, 0);
                            output[index] += pixels[correctedX + y * imageWidth] * kernel[convX + size];
                        }
                    }

                    // For all pixels less than size pixel away from the right side of the image
                    for (int x = imageWidth - size; x < imageWidth; x++) {
                        index = x + y * imageWidth;
                        for (int convX = -size; convX <= size; convX++) {
                            correctedX = Math.min(x + convX, imageWidth - 1);
                            output[index] += pixels[correctedX + y * imageWidth] * kernel[convX + size];
                        }
                    }
                }

                // For all pixels in between
                for (int x = size; x < imageWidth - size; x++) {
                    index = x + y * imageWidth;
                    for (int convX = -size; convX <= size; convX++) {
                        output[index] += pixels[x + convX + y * imageWidth] * kernel[convX + size];
                    }
                }
            }

        } else {

            // Apply the gaussian filter for each column
            for (int x = 0; x < imageWidth; x++) {

                if (correctBorders) {
                    int correctedY;

                    // For all pixels less than size pixel away from the top side of the image
                    for (int y = 0; y < size; y++) {
                        index = x + y * imageWidth;
                        for (int convY = -size; convY <= size; convY++) {
                            correctedY = Math.max(y + convY, 0);
                            output[index] += pixels[x + correctedY * imageWidth] * kernel[convY + size];
                        }
                    }

                    // For all pixels less than size pixel away from the bottom side of the image
                    for (int y = imageHeight - size; y < imageHeight; y++) {
                        index = x + y * imageWidth;
                        for (int convY = -size; convY <= size; convY++) {
                            correctedY = Math.min(y + convY, imageHeight - 1);
                            output[index] += pixels[x + correctedY * imageWidth] * kernel[convY + size];
                        }
                    }
                }

                // For all pixels in between
                for (int y = size; y < imageHeight - size; y++) {
                    index = x + y * imageWidth;
                    for (int convY = -size; convY <= size; convY++) {
                        output[index] += pixels[x + (convY + y) * imageWidth] * kernel[convY + size];
                    }
                }
            }
        }
        normalizeOutput(pixels, output, kernel);
    }

    /**
     *  Takes the values in input and copies them to output after normalizing them to 0-255.
     *  Calculates sumNegativeKernel and sumPositiveKernel before calling normalizeOutput.
     *  @param input the pixel to be normalized
     *  @param output where the result will be stored.
     *  @param kernel the kernel used during convolution.
     */
    @Deprecated private static void normalizeOutput(final int[] input, final int[] output, final int[] kernel) {
        // We calculate the sum of our kernel to normalize the values.
        int sumNegativeKernel = 0;
        int sumPositiveKernel = 0;

        for (int i:kernel) {
            if (i < 0) {
                sumNegativeKernel -= i;
            } else {
                sumPositiveKernel += i;
            }
        }
        normalizeOutput(input, output, sumNegativeKernel, sumPositiveKernel);
    }

    /**
     *  Takes the values in output and copies them to pixels after normalizing them to 0-255.
     *  The values in output should be in between sumNegativeKernel * 255 and sumPositiveKernel * 255.
     *  @param pixels where the result will be stored.
     *  @param output the pixel to be normalized.
     *  @param sumNegativeKernel sum of all negative values in the kernel.
     *  @param sumPositiveKernel sum of all positive values in the kernel.
     */
    @Deprecated private static void normalizeOutput(final int[] pixels, final int[] output, final int sumNegativeKernel, final int sumPositiveKernel) {
        // Save the new values to pixels
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (output[i] + 255 * sumNegativeKernel) / (sumNegativeKernel + sumPositiveKernel);
        }
    }

    /**
     *  Takes an array of greyscale values (between 0 and 255) and convert
     *  that array in a pixel array.
     *  @param pixels the pixel to be converted to color
     */
    @Deprecated public static void convertGreyToColor(final int[] pixels) {
        // Saves the new values as colors
        int outputGrey;
        int pixelLength = pixels.length;
        for (int i = 0; i < pixelLength; i++) {
            outputGrey = pixels[i];
            pixels[i] = android.graphics.Color.rgb(outputGrey, outputGrey, outputGrey);
        }
    }

    /**
     *  If the coordinates given in parameter are outside the image, returns the closest pixel.
     *  It will be much more efficient to hardcode this "fix" directly in each
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     *  @param x the x coordinates of the pixel
     *  @param y the y coordinates of the pixel
     *  @return the pixels at (x, y) if the coordinates are inside the image, otherwise the closest pixel.
     */
    @Deprecated public static int correctedPixelGet(final int[] pixels, final int imageWidth, final int imageHeight, final int x, final int y) {
        int newX = x;
        int newY = y;

        if (x < 0) {
            newX = 0;
        } else if (x >= imageWidth) {
            newX = imageWidth - 1;
        }

        if (y < 0) {
            newY = 0;
        } else if (y >= imageHeight) {
            newY = imageHeight - 1;
        }

        return pixels[newX + newY * imageWidth];
    }


}
