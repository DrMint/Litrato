#pragma  version (1)
#pragma rs java_package_name(com.android.retouchephoto)
#include "hsvtorgb.rs"
float t = 0;
float saturation = -1;

uchar4  RS_KERNEL  colorize(uchar4  in) {
    float4  pixelf = rsUnpackColor8888(in);
    float maxRGB= max(pixelf.r,(max(pixelf.g,pixelf.b)));
    float minRGB = min(pixelf.r,(min(pixelf.b,pixelf.g)));
    float s;

    if (saturation >= 0) {
        s = saturation;
    } else {
        if(maxRGB == 0){
            s = 0;
        }else{
            s = 1 - (minRGB/maxRGB);
        }
    }
    return HSVtoRGB(t, s, maxRGB, pixelf);
}