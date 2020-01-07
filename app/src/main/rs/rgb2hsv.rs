#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)
#pragma rs_fp_relaxed

uchar4 RS_KERNEL hsv(uchar4 in){
    // ARGB
    //float4 rgba = rsUnpackColor8888(in);
    uchar4 out;
    uchar minRGB = min( in.r, min( in.g, in.b ) );
    uchar maxRGB = max( in.r, max( in.g, in.b ) );
    uchar deltaRGB = maxRGB - minRGB;

    if ( deltaRGB <= 0) {

        out.s0 = 0;
        out.s1 = 0;

    } else { // deltaRGB > 0 -> maxRGB > 0

        out.s1 = (255 * deltaRGB) / maxRGB;

        if (in.r >= maxRGB) {
            if( in.g > in.b ) {
                out.s0 = (30 * (in.g - in.b)) / deltaRGB;        // between yellow & magenta
            } else {
                out.s0 = 180 + (30 * (in.g - in.b)) / deltaRGB;
            }
        } else if (in.g >= maxRGB) {
            out.s0 = 60 + (30 * (in.b - in.r)) / deltaRGB;  // between cyan & yellow
        } else {
            out.s0 = 120 + (30 * (in.r - in.g)) / deltaRGB;  // between magenta & cyan
        }
    }

    out.s2 = maxRGB;
    out.s3 = in.a;

    return out;
}