#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};
static float level = 0;
static uchar4 black;
static uchar4 white;

void setLevel(float v) {
    level = v;
    black = rsPackColorTo8888(0 , 0 , 0, 255);
    white = rsPackColorTo8888(255 , 255 , 255, 255);
}

uchar4 RS_KERNEL threshold(const uchar4 in) {
    const float4 pixelf = rsUnpackColor8888(in);
    const float gray = dot(pixelf , weight);
    return (gray >= level) ? white : black;
}