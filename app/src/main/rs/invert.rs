# pragma version (1)
# pragma rs java_package_name(com.android.retouchephoto)

uchar4 RS_KERNEL invert( uchar4 in , uint32_t x , uint32_t y ) {
    uchar4 out = in;

    out.r = 255 - in.r;
    out.g = 255 - in.g;
    out.b = 255 - in.b;

    return out;
}

uchar4 RS_KERNEL invert2( uchar4 in , uint32_t x , uint32_t y ) {
    return (0xFFFFFF - in) | 0xFF000000;
}