#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

int width;
rs_allocation pixels;

uchar4 RS_KERNEL mirror(const uchar4 in, uint32_t x, uint32_t y) {
    return rsGetElementAt_uchar4(pixels, width - 1 - x, y);
}
