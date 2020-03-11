#pragma version (1)
#pragma rs java_package_name(com.android.retouchephoto)
#pragma rs_fp_relaxed

int width;
int height;
//const uchar4 *pixels;
const float* kernel;
int kernelWidth;
int kernelHeight;
float kernelWeight = 1;
rs_allocation pixels;

uchar4 __attribute__((kernel)) toConvolution(uchar4 in,uint32_t x,uint32_t y) {
    in.r=0;
    in.b=0;
    in.g=0;
    in.a=255;
    float4 color;

    if (x >= kernelWidth && x <= width - kernelWidth && y >= kernelHeight && y <= height - kernelHeight) {
        color = rsUnpackColor8888(in);
        int index = 0;
        for (int convX = -kernelWidth; convX <= kernelWidth; convX++){
            for(int convY = -kernelHeight; convY <= kernelHeight; convY++){
                //color += kernel[index] * rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + convX + ((y + convY) * width))) / kernelWeight;
                color += kernel[index] * rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + convX, y + convY));
                index++;
            }
        }
        color /= kernelWeight;
        return rsPackColorTo8888(color);
    }
    return in;
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
                //color += rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + convX + ((y + convY) * width))) / kernelWeight;
                color += rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + convX, y + convY));
            }
        }
        color /= kernelWeight;
        return rsPackColorTo8888(color);
    }
    return in;
}

uchar4 __attribute__((kernel)) toConvolutionHorizontal(uchar4 in,uint32_t x,uint32_t y) {
    in.r=0;
    in.b=0;
    in.g=0;
    in.a=255;
    float4 color = rsUnpackColor8888(in);

    if (x >= kernelWidth && x <= width - kernelWidth) {
        for (int convX = -kernelWidth; convX <= kernelWidth; convX++) {
            //color += kernel[convX + kernelWidth] * rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + convX + y * width));
            color += kernel[convX + kernelWidth] * rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + convX, y));
        }
        color /= kernelWeight;
        return rsPackColorTo8888(color);
    }
    return in;

}

uchar4 __attribute__((kernel)) toConvolutionVertical(uchar4 in,uint32_t x,uint32_t y) {
    in.r=0;
    in.b=0;
    in.g=0;
    in.a=255;
    float4 color = rsUnpackColor8888(in);

    if (y >= kernelHeight && y <= height - kernelHeight) {
        for (int convY = -kernelHeight; convY <= kernelHeight; convY++) {
            //color += kernel[convY + kernelHeight] * rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x + ((y + convY) * width))) / kernelWeight;
            color += kernel[convY + kernelHeight] * rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x, y + convY));
        }
        color /= kernelWeight;
        return rsPackColorTo8888(color);
    }
    return in;
}

