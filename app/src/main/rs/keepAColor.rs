#pragma  version (1)
#pragma rs java_package_name(com.android.retouchephoto)

int choosedColor;
int margin;
bool keep;
int lut[360];
int increment=0;

uchar4 RS_KERNEL keepAColor(uchar4 in) {
    if (choosedColor < 0) choosedColor = 0;
    if (choosedColor >= 360) choosedColor = 0;

    if (margin < 0) margin = 0;
    if (margin > 360) margin = 360;
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
    if( keep == true){
        s = min(s, 1.0f - 1.0f / margin * lut[(int) t]);
    }

    else{
        s = min(s, 1.0f / margin * lut[(int) t]);
    }
        float tI=(int)(t/60)%6;
        float f = t/60 -tI;
        float v = maxRGB;
        float l = v*(1-s);
        float m = v * (1-(f*s));
        float n = v*(1-(1-f)*s);
        if(tI==0){
            return rsPackColorTo8888(v,n,l, pixelf.a);
        }else if(tI==1){
            return rsPackColorTo8888(m,v,l, pixelf.a);
        }else if(tI==2){
            return rsPackColorTo8888(l,v,n,pixelf.a);
        }else if(tI==3){
            return rsPackColorTo8888(l,m,v,pixelf.a);
        }else if(tI==4){
            return rsPackColorTo8888(n,l,v,pixelf.a);
        }else {
            return rsPackColorTo8888(v,l,m,pixelf.a);
        }
}

void init(){
    for(int i=0;i<360;i++){
        lut[i]=0;
    }
}
void calculateLUT(){
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
}