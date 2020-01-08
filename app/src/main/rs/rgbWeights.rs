#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

static float rWeight = 0.f;
static float gWeight = 0.f;
static float bWeight = 0.f;

void setWeights(float redWeight, float greenWeight,  float blueWeight) {
    rWeight = redWeight;
    gWeight = greenWeight;
    bWeight = blueWeight;
}

uchar4 RS_KERNEL applyWeights(const uchar4 in) {
    uchar4 out = in;
    out.r = clamp((int)(rWeight + in.r), 0, 255);
    out.g = clamp((int)(gWeight + in.g), 0, 255);
    out.b = clamp((int)(bWeight + in.b), 0, 255);
    return out;
}