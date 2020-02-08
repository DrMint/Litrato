#pragma  version (1)
#pragma rs java_package_name(com.android.retouchephoto)

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