#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

static float gammaCorrection = 0;

void setGamma(float value) {
    gammaCorrection = 1.0 / value;
}

uchar4 RS_KERNEL gamma(const uchar4 in) {
     uchar4 out = in;
     out.r = 255 * pow(in.r / 255.0, gammaCorrection);
     out.g = 255 * pow(in.g / 255.0, gammaCorrection);
     out.b = 255 * pow(in.b / 255.0, gammaCorrection);
     return out;
}