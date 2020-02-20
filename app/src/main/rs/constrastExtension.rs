#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.android.retouchephoto)


int32_t histogramR[256];
int32_t histogramG[256];
int32_t histogramB[256];
int r;
int g;
int b;
int minr;
int ming;
int minb;
int maxr;
int maxg;
int maxb;


void __attribute__((kernel)) compute_histogramb(uchar4 in)
{
rsAtomicInc(&histogramB[in.b]);
}
void __attribute__((kernel)) compute_histogramr(uchar4 in)
{
rsAtomicInc(&histogramR[in.r]);
}
void __attribute__((kernel)) compute_histogramg(uchar4 in)
{
rsAtomicInc(&histogramG[in.g]);
}

uchar4 __attribute__((kernel)) apply_histogram(uchar4 in)
{
uchar valR = histogramR[in.r];
uchar valG = histogramG[in.g];
uchar valB = histogramB[in.b];
return (uchar4) {valR,valG,valB,in.a};
}

void createRemapArray() {
//create map for y
r=maxr-minr;
g=maxg-ming;
b=maxb-minb;
for (int i = 0; i < 256; i++) {
histogramR[i]=(255*(i-minr))/r;
histogramG[i]=(255*(i-ming))/g;
histogramB[i]=(255*(i-minb))/b;
}
}

void init() {
for (int i = 0; i < 256; i++) {
histogramR[i] = 0;
histogramG[i] = 0;
histogramB[i] = 0;
}
}
void minArrayR() {
        int i = 0;
        while (histogramR[i] == 0) {
            i++;
        }
        minr=i;
    }

void maxArrayR() {
        int i = 255;
        while (histogramR[i] == 0) {
            i--;
        }
        maxr=i;
 }
void minArrayG() {
        int i = 0;
        while (histogramG[i] == 0) {
            i++;
        }
        ming=i;
    }

void maxArrayG() {
        int i = 255;
        while (histogramG[i] == 0) {
            i--;
        }
        maxg=i;
 }
void minArrayB() {
        int i = 0;
        while (histogramB[i] == 0) {
            i++;
        }
        minb=i;
    }

void maxArrayB() {
        int i = 255;
        while (histogramB[i] == 0) {
            i--;
        }
        maxb=i;
 }
