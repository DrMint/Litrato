#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

static uchar steps = 0;
static uchar range = 0;

void setSteps(uchar v) {
    steps = 256 / v;
    range = 255 / (v - 1);
}

uchar4 RS_KERNEL posterize(const uchar4 in) {
     uchar4 out = in;
     out.r = clamp((in.r / steps) * range, 0, 255);
     out.g = clamp((in.g / steps) * range, 0, 255);
     out.b = clamp((in.b / steps) * range, 0, 255);
     return out;
}