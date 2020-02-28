#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.android.retouchephoto)

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
     }else if(tI==5){
         return rsPackColorTo8888(v,l,m,pixelf.a);
     }
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
