#pragma version(1)
#pragma rs java_package_name(com.android.retouchephoto)
#pragma rs_fp_relaxed

const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

float saturationValue = 0.0f;

uchar4 __attribute__((kernel)) saturation(uchar4 in)
{
    float4 f4 = rsUnpackColor8888(in);
    float3 out = dot(f4.rgb, gMonoMult);
    out = mix(out, f4.rgb, saturationValue);

    return rsPackColorTo8888(out);
}