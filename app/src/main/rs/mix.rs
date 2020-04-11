#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)

float luminositySaturation = 0.0;
float overlayTransparency = 0.0;

rs_allocation pixels;

uchar4 RS_KERNEL multiply(const uchar4 in, uint32_t x, uint32_t y) {
    float4 dst = rsUnpackColor8888(in);
    float4 src = rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x, y));
    dst = src * dst;
    return rsPackColorTo8888(dst);
}

uchar4 RS_KERNEL add(const uchar4 in, uint32_t x, uint32_t y) {
    float4 dst = rsUnpackColor8888(in);
    float4 src = rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x, y));
    dst = fmin(src + dst, (float) 1.0);
    return rsPackColorTo8888(dst);
}

uchar4 RS_KERNEL overlay(const uchar4 in, uint32_t x, uint32_t y) {
    float4 dst = rsUnpackColor8888(in);
    float4 src = rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x, y));
    dst = src * (1 - overlayTransparency) + dst * overlayTransparency;
    return rsPackColorTo8888(dst);
}


uchar4 RS_KERNEL luminosity(const uchar4 in, uint32_t x, uint32_t y) {

    float4 pixelf = rsUnpackColor8888(rsGetElementAt_uchar4(pixels, x, y));
    float4 inF = rsUnpackColor8888(in);

    float maxRGB = max(pixelf.r,(max(pixelf.g,pixelf.b)));
    float minRGB = min(pixelf.r,(min(pixelf.b,pixelf.g)));
    float s;
    float t;

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

    float tI=(int)(t/60)%6;
    float f = t/60 -tI;


    //float v = maxRGB;
    // Value is the value of "in" and not "pixels"
    float v = max(inF.r,(max(inF.g, inF.b)));
    s = clamp(s - luminositySaturation, 0.0, 1.0);

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


