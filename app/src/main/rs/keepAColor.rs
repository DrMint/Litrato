#pragma  version (1)
#pragma rs java_package_name(com.android.retouchephoto)
#include "hsvtorgb.rs"

int choosedColor;
float margin;
bool keep;
float lut[360];
int increment = 0;

uchar4 RS_KERNEL keepAColor(uchar4 in) {

    float4  pixelf = rsUnpackColor8888(in);
    float maxRGB= max(pixelf.r,(max(pixelf.g,pixelf.b)));
    float minRGB = min(pixelf.r,(min(pixelf.b,pixelf.g)));
    float diff = maxRGB - minRGB;
    float t = 0;
    float s = 0;
    if(maxRGB == 0){
        s = 0;
     }else{
     s = 1 - (minRGB/maxRGB);
       }
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

    s *= lut[(int) t];
    return HSVtoRGB(t, s, maxRGB, pixelf);
}

void calculateLUT(){

    if (choosedColor < 0) choosedColor = 0;
    if (choosedColor >= 360) choosedColor = 0;

    if (margin < 0) margin = 0;
    if (margin > 360) margin = 360;

    if (choosedColor > 180) {
        lut[0] = 360 - choosedColor;
        increment = 1;
    } else if (choosedColor == 0) {
        lut[0] = 0;
        increment = 1;
    } else {
        lut[0] = choosedColor;
        increment = -1;
    }
    for (int i = 1; i < 360; i++) {
        lut[i] = lut[i - 1] + increment;
        if (lut[i] == 180 || lut[i] == 0) {
             increment = -increment;
        }
    }

    for (int i = 0; i < 360; i++) {
        if( keep == true){
            lut[i] = 1.0f - 1.0f / margin * lut[i];
        } else{
            lut[i] = 1.0f / margin * lut[i];
        }
        if (lut[i] < 0) lut[i] = 0;
        if (lut[i] > 1) lut[i] = 1;
    }

}