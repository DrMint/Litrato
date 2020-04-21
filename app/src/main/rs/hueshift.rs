#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.android.retouchephoto)
#include "hsvtorgb.rs"
int32_t lut[360];
float shift;

uchar4 __attribute__((kernel)) hueshift(uchar4 in)
{   float4  pixelf = rsUnpackColor8888(in);
     float maxRGB= max(pixelf.r,(max(pixelf.g,pixelf.b)));
     float minRGB = min(pixelf.r,(min(pixelf.b,pixelf.g)));
     float s;
     float t;
    if(maxRGB == 0){
        s = 0;
     }else{
     s = 1 - (minRGB/maxRGB);

    if (maxRGB==minRGB){
        t=0;
    }else if(maxRGB==pixelf.r){
        t=fmod(60*((pixelf.g-pixelf.b)/(maxRGB-minRGB))+360,360);
    }
    else if(maxRGB==pixelf.g){
        t=60*((pixelf.b-pixelf.r)/(maxRGB-minRGB))+120;
    }
    else {
            t=60*((pixelf.r-pixelf.g)/(maxRGB-minRGB))+240;
        }
    t=lut[(int)t];
    return HSVtoRGB(t, s, maxRGB, pixelf);
}
}
void init(){
for(int i = 0; i < 360; i++) {
    lut[i]=0;
}
}

void calculateLUT(){
for (int i = 0; i < 360; i++) {
    lut[i] = i + shift;
    if (lut[i] < 0) {
        lut[i] += 360;
    } else if (lut[i] >= 360) {
        lut[i] -= 360;
    }
}
}
