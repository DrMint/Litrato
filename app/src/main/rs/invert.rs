#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

uchar4 RS_KERNEL invert(const uchar4 in) {
    uchar4 out = in;
    out.r = 255 - in.r;
    out.g = 255 - in.g;
    out.b = 255 - in.b;
    return out;
}