#pragma version (1)
#pragma rs java_package_name(com.android.retouchephoto)
#pragma rs_fp_relaxed

int width;
int height;
const uchar4 *pixels;
const float* kernel;
int kernelWidth;
int kernelHeight;
float kernelWeight = 1;

uchar4 __attribute__((kernel)) toConvolution(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=255;
    float4 color = rsUnpackColor8888(in);

    if (x >= kernelWidth && x <= width - kernelWidth && y >= kernelHeight && y <= height - kernelHeight) {
        int index = 0;
        for (int convX = -kernelWidth; convX <= kernelWidth; convX++){
            for(int convY = -kernelHeight; convY <= kernelHeight; convY++){
                color += kernel[index] * rsUnpackColor8888(pixels[x + convX + ((y + convY) * width)]) / kernelWeight;
                index++;
            }
        }
    }
    return rsPackColorTo8888(color);
}

uchar4 __attribute__((kernel)) toConvolutionUniform(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=255;
    float4 color = rsUnpackColor8888(in);

    if (x >= kernelWidth && x <= width - kernelWidth && y >= kernelHeight && y <= height - kernelHeight) {
        for (int convX = -kernelWidth; convX <= kernelWidth; convX++){
            for(int convY = -kernelHeight; convY <= kernelHeight; convY++){
                color += rsUnpackColor8888(pixels[x + convX + ((y + convY) * width)]) / kernelWeight;
            }
        }
    }
    return rsPackColorTo8888(color);
}

// Doesn't work right now, give strange artefacts over the places covered by the correction
uchar4 __attribute__((kernel)) toConvolutionHorizontalWithCorrection(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=0;

    float4 color = rsUnpackColor8888(in);

    if (x < kernelWidth) {
        for (int convX = -kernelWidth; convX <= 0; convX++) {
            color += rsUnpackColor8888(pixels[0 + y * width]) * kernel[convX + kernelWidth] / kernelWeight;
        }
        for (int convX = 1; convX <= kernelWidth; convX++) {
            color += rsUnpackColor8888(pixels[x + convX + y * width]) * kernel[convX + kernelWidth] / kernelWeight;
        }
    } else if (x >= width - kernelWidth) {
        for (int convX = 0; convX <= kernelWidth; convX++) {
            color += rsUnpackColor8888(pixels[width - 1 + y * width]) * kernel[convX + kernelWidth] / kernelWeight;
        }
        for (int convX = -kernelWidth; convX < 0; convX++) {
            color += rsUnpackColor8888(pixels[x + convX + y * width]) * kernel[convX + kernelWidth] / kernelWeight;
        }

    } else {
        for (int convX = -kernelWidth; convX <= kernelWidth; convX++) {
            color += rsUnpackColor8888(pixels[x + convX + y * width]) * kernel[convX + kernelWidth] / kernelWeight;
        }
    }
    return rsPackColorTo8888(color);
}

uchar4 __attribute__((kernel)) toConvolutionHorizontal(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=0;

    float4 color = rsUnpackColor8888(in);

    if (x < kernelWidth || x >= height - kernelWidth) {
        return rsPackColorTo8888(color);
    }

    for (int convX = -kernelWidth; convX <= kernelWidth; convX++) {
        color += rsUnpackColor8888(pixels[x + convX + y * width]) * kernel[convX + kernelWidth] / kernelWeight;
    }

    return rsPackColorTo8888(color);
}

uchar4 __attribute__((kernel)) toConvolutionVertical(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=0;

    float4 color = rsUnpackColor8888(in);

    if (y < kernelHeight || y >= height - kernelHeight) {
        return rsPackColorTo8888(color);
    }

    for (int convY = -kernelHeight; convY <= kernelHeight; convY++) {
        color += rsUnpackColor8888(pixels[x + (y + convY) * width]) * kernel[convY + kernelHeight] / kernelWeight;
    }

    return rsPackColorTo8888(color);
}

