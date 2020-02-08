#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.android.retouchephoto)


int32_t *histogramR;
int32_t *histogramG;
int32_t *histogramB;
int32_t *histogramR2;
int32_t *histogramG2;
int32_t *histogramB2;
int32_t *LUTr;
int32_t *LUTg;
int32_t *LUTb;
int32_t *LUTr2;
int32_t *LUTg2;
int32_t *LUTb2;
void __attribute__((kernel)) compute_histogram(uchar4 in)
{
volatile int32_t *addrR = &histogramR[in.r];
rsAtomicInc(addrR);
volatile int32_t *addrG = &histogramG[in.g];
rsAtomicInc(addrG);
volatile int32_t *addrB = &histogramB[in.b];
rsAtomicInc(addrB);
}

uchar4 __attribute__((kernel)) apply_histogram(uchar4 in)
{
uchar valR = LUTr[in.r];
uchar valG = LUTg[in.g];
uchar valB = LUTb[in.b];
return (uchar4) {valR,valG,valB,in.a};
}

uchar4 __attribute__((kernel)) apply_dim(uchar4 in)
{
uchar valR = LUTr2[in.r];
uchar valG = LUTg2[in.g];
uchar valB = LUTb2[in.b];
return (uchar4) {valR,valG,valB,in.a};
}
uchar4 __attribute__((kernel)) apply_egal(uchar4 in)
{
uchar valR = (histogramR2[in.r]*255)/histogramR2[255];
uchar valG = (histogramG2[in.g]*255)/histogramG2[255];
uchar valB = (histogramB2[in.b]*255)/histogramB2[255];
return (uchar4) {valR,valG,valB,in.a};
}