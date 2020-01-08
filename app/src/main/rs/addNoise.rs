#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

static int intensity = 0.f;

void setIntensity(int value) {
    intensity = value;
}

uchar4 RS_KERNEL applyNoise(const uchar4 in) {
    uchar4 out = in;
    out.r = clamp(rsRand(-intensity, intensity) + in.r, 0, 255);
    out.g = clamp(rsRand(-intensity, intensity) + in.g, 0, 255);
    out.b = clamp(rsRand(-intensity, intensity) + in.b, 0, 255);
    return out;
}

uchar4 RS_KERNEL applyNoiseBW(const uchar4 in) {
    uchar4 out = in;
    int randomInt = rsRand(-intensity, intensity);
    out.r = clamp(randomInt + in.r, 0, 255);
    out.g = clamp(randomInt + in.g, 0, 255);
    out.b = clamp(randomInt + in.b, 0, 255);
    return out;
}