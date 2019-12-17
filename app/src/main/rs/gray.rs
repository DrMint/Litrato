# pragma version (1)
# pragma rs java_package_name(com.android.retouchephoto)

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL gray ( uchar4 in ) {
    const float4 pixelf = rsUnpackColor8888 (in);
    const float gray = dot(pixelf, weight);
    return rsPackColorTo8888 (gray, gray, gray, pixelf.a);
}