#pragma version (1)
#pragma rs java_package_name(com.android.retouchephoto)
#pragma rs_fp_relaxed

int width;
int height;
const uchar4 *pixels;
int sizeConvolution;
float* filtre;
int kernelWeight = 1;

uchar4 __attribute__((kernel)) toConvolution(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=255;
    float4 color = rsUnpackColor8888(in);

    if (x >= sizeConvolution && x <= width - sizeConvolution && y >= sizeConvolution && y <= height - sizeConvolution) {
        int index = 0;
        for (int convX = -sizeConvolution; convX <= sizeConvolution; convX++){
            for(int convY = -sizeConvolution; convY <= sizeConvolution; convY++){
                color += filtre[index] * rsUnpackColor8888(pixels[x + convX + ((y + convY) * width)]) / kernelWeight;
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

    if (x >= sizeConvolution && x <= width - sizeConvolution && y >= sizeConvolution && y <= height - sizeConvolution) {
        for (int convX = -sizeConvolution; convX <= sizeConvolution; convX++){
            for(int convY = -sizeConvolution; convY <= sizeConvolution; convY++){
                color += rsUnpackColor8888(pixels[x + convX + ((y + convY) * width)]) / kernelWeight;
            }
        }
    }
    return rsPackColorTo8888(color);
}


uchar4 __attribute__((kernel)) toConvolutionHorizontalWithCorrection(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=0;

    float4 color = rsUnpackColor8888(in);

    if (x < sizeConvolution) {
        for (int convX = -sizeConvolution; convX <= 0; convX++) {
            color += rsUnpackColor8888(pixels[0 + y * width]) * filtre[convX + sizeConvolution] / kernelWeight;
        }
        for (int convX = 1; convX <= sizeConvolution; convX++) {
            color += rsUnpackColor8888(pixels[x + convX + y * width]) * filtre[convX + sizeConvolution] / kernelWeight;
        }
    } else if (x >= width - sizeConvolution) {
        for (int convX = 0; convX <= sizeConvolution; convX++) {
            color += rsUnpackColor8888(pixels[width - 1 + y * width]) * filtre[convX + sizeConvolution] / kernelWeight;
        }
        for (int convX = -sizeConvolution; convX < 0; convX++) {
            color += rsUnpackColor8888(pixels[x + convX + y * width]) * filtre[convX + sizeConvolution] / kernelWeight;
        }

    } else {
        for (int convX = -sizeConvolution; convX <= sizeConvolution; convX++) {
            color += rsUnpackColor8888(pixels[x + convX + y * width]) * filtre[convX + sizeConvolution] / kernelWeight;
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

    if (x < sizeConvolution || x >= height - sizeConvolution) {
        return rsPackColorTo8888(color);
    }

    for (int convX = -sizeConvolution; convX <= sizeConvolution; convX++) {
        color += rsUnpackColor8888(pixels[x + convX + y * width]) * filtre[convX + sizeConvolution] / kernelWeight;
    }

    return rsPackColorTo8888(color);
}

uchar4 __attribute__((kernel)) toConvolutionVertical(uchar4 in,uint32_t x,uint32_t y) {

    in.r=0;
    in.b=0;
    in.g=0;
    in.a=0;

    float4 color = rsUnpackColor8888(in);

    if (y < sizeConvolution || y >= height - sizeConvolution) {
        return rsPackColorTo8888(color);
    }

    for (int convY = -sizeConvolution; convY <= sizeConvolution; convY++) {
        color += rsUnpackColor8888(pixels[x + (y + convY) * width]) * filtre[convY + sizeConvolution] / kernelWeight;
    }

    return rsPackColorTo8888(color);
}

