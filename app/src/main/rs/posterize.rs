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
     out.r = (in.r / steps) * range;
     out.g = (in.g / steps) * range;
     out.b = (in.b / steps) * range;
     return out;
}