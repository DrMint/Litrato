 #pragma  version (1)
 #pragma rs java_package_name(com.android.retouchephoto)

static uchar4 HSVtoRGB(float t, float s, float maxRGB, float4 pixelf) {
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
        return rsPackColorTo8888(l,v,n, pixelf.a);
    }else if(tI==3){
        return rsPackColorTo8888(l,m,v, pixelf.a);
    }else if(tI==4){
        return rsPackColorTo8888(n,l,v, pixelf.a);
    }else if(tI==5){
        return rsPackColorTo8888(v,l,m, pixelf.a);
    }
}
