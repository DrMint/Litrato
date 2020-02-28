#pragma  version (1)
#pragma rs java_package_name(com.android.retouchephoto)

float choosedColor;
bool keep;

uchar4 RS_KERNEL keepAColor(uchar4 in) {

    uchar mymax = max(in.r,max(in.g,in.b));
    uchar mymin = min(in.r,min(in.g,in.b));
    uchar diff = mymax - mymin;
    float t = 0;
    float s = 0;

    if (mymax == mymin){
        t = 0;
    } else if (mymax == in.r){
        t = (60 * (in.g - in.b) / diff);
    } else if(mymax == in.g){
        t = (60 * (in.b - in.r) / diff) + 120;
    } else if(mymax == in.b){
        t = (60 * (in.r - in.g) / diff) + 240;
    }

    if(mymax == 0){
        s = 0;
    } else {
        s = 1 - (mymax - mymin);
    }
    if( keep == true){

    if ((t - choosedColor) >= -25 && (t - choosedColor) <= 25 ){
        return in;
    } else {
        const uchar gray = (30 * in.r + 59 * in.g + 11 * in.b) / 100;
        return (uchar4) {gray, gray, gray,in.a };
    }
    }

    else{
        if (t - choosedColor >= -25 && t - choosedColor <= 25) {
            const uchar gray = (30 * in.r + 59 * in.g + 11 * in.b) / 100;
            return (uchar4) {gray, gray, gray,in.a };
        } else {
            return in;
        }
    }
}