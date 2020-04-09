#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

int32_t lut[256];

void setBurnBlackIntensity(float intensity) {
    float v = intensity + 1.0;
    float correction = 255 * v - 255;
    for (int i = 0; i < 256; i++) {
        lut[i]= max((int) (i * v - correction), 0);
    }
}

void setBurnWhiteIntensity(float intensity) {
    float v = intensity + 1.0;
    for (int i = 0; i < 256; i++) {
        lut[i]= min((int) (i * v), 255);
    }
}

void setBurnIntensity(float intensity) {
    float v = intensity + 1.0;
    float v2 = intensity * 2 + 1.0;
    float correction = 255 * v - 255;
    for (int i = 0; i < 256; i++) {
        lut[i]= clamp((int) (i * v2 - correction), 0, 255);
    }
}

uchar4 RS_KERNEL burn(const uchar4 in) {
    uchar4 out = in;
    out.r = lut[in.r];
    out.g = lut[in.g];
    out.b = lut[in.b];
    return out;
}