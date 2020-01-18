# IMAGE PROCESSING ON ANDROID
## FILTERS
### Brightness
![](https://www.r-entries.com/etuliens/img/PT/1.jpg)

Changes how bright or dark the image is. It turns up too much, this will burn the image.
- `Seek bar`: the image’s brightness (between -100% and 100%).

### Saturation
![](https://www.r-entries.com/etuliens/img/PT/2.jpg)

Changes the saturation of the image colors.
- `Seek bar`: the image’s saturation (between 0 and 200%).

### Temperature
![](https://www.r-entries.com/etuliens/img/PT/3.jpg)

Makes the image look warmer or colder.
- `Seek bar`: the image’s temperature (between -100% and 100%).

### Tint
![](https://www.r-entries.com/etuliens/img/PT/4.jpg)

Changes the tint of an image. Makes the images more magenta or green.
- `Seek bar`: the image’s tint (between -100% and 100%).

### Sharpening
![](https://www.r-entries.com/etuliens/img/PT/5.jpg)

Makes the image look sharpen or blurrier.
- `Seek bar`: the image’s sharpness (between -100% and 100%).

### Colorize
![](https://www.r-entries.com/etuliens/img/PT/6.jpg)

Makes the image look sharpen or blurrier.
- `Color seek bar`: the color you which to use.
- `Seek bar`: the color’s saturation (between 0% and 100%).

### Change hue
![](https://www.r-entries.com/etuliens/img/PT/9.jpg)

Apply one hue to the entire image but contrary to the Colorize filter, it doesn’t change the saturation of the image.
- `Color seek bar`: the color hue you which to use.

### Invert
![](https://www.r-entries.com/etuliens/img/PT/7.jpg)

The luminance and colors are inverted: whites become blacks and reds become blues.

### Keep a color
![](https://www.r-entries.com/etuliens/img/PT/10.jpg)

Turns everything grayscale except for a color.
- `Color seek bar`: the color you which to kept.
- `Seek bar`: how far off the color can be before turning grey (in degrees).

### Remove a color
![](https://www.r-entries.com/etuliens/img/PT/11.jpg)

Same as the Keep a color filter but only turns that color to greyscale.
- `Color seek bar`: the color you which to remove.
- `Seek bar`: how far off the color can be before turning grey (in degrees).

### Posterize
![](https://www.r-entries.com/etuliens/img/PT/12.jpg)

Same as the Keep a color filter but only turns that color to greyscale.
- `Seek bar`: how many possible colors should be kept in each channel (in steps between 2 and 32).
- `Second seek bar`: also turns the image greyscale.

### Threshold
![](https://www.r-entries.com/etuliens/img/PT/13.jpg)

If the pixel’s luminosity is bellowing the threshold, turns it back. Turns it white otherwise.
- `Seek bar`: set the threshold (between 0 and 255).

### Add noise
![](https://www.r-entries.com/etuliens/img/PT/14.jpg)

Adds some random noise to the image.
- `Seek bar`: the amount of noise (between 0 and 255).
- `Second seek bar`: turns the noise greyscale or colored.

### Linear contrast stretching
![](https://www.r-entries.com/etuliens/img/PT/17.jpg)

Modifies the luminance range of an image. This can be used to increase or compress the image contrast.
- `Seek bar`: the lower range of luminance.
- `Second seek bar`: the higher range of luminance.

### Histogram equalization
![](https://www.r-entries.com/etuliens/img/PT/16.jpg)

Spread the luminance values evenly between 0 and 255. This can look surreal on some images, but it usually extends the contrast substantially.

### Average blur
![](https://www.r-entries.com/etuliens/img/PT/15.jpg)

Blur the image by averaging all pixels with their neighbors. This filter is quite inefficient and the Gaussian blur should be prioritized. 
- `Seek bar`: blur amount (in pixels).

### Gaussian blur
![](https://www.r-entries.com/etuliens/img/PT/18.jpg)

Blur the image by using a gaussian function.
- `Seek bar`: blur amount (in pixels).

### Laplacian
![](https://www.r-entries.com/etuliens/img/PT/19.jpg)

Used to highlight all the image’s contours.
- `Seek bar`: how much detail should be kept (in pixels).

## FUNCTIONS
### ColorTools Class
This class implements all the functions necessary for conversions between RGB and HSV. None of those functions currently utilize RenderScript (abbreviated to RS from now on), but this should come along eventually. HSV is used by many filters, and because of that, it has been refined a little bit since the first version. First of all, each value (the hue, the saturation and the luminosity) can now be converted separately. This has improved the performance substantially in some functions such as colorize (-40% in runtime when using rgb2v instead of rgb2hsv). Furthermore, we can reduce the runtime by another 10% by using integers instead of floats in rgb2s. Finally, by using some bit-level trickery such as using (color >> 16) & 0x000000FF instead of Color.red(color), we can expect another 14% improvement. Also being able to call hsv2rgb with H, S, and V as three separate parameters is a huge improvement. This doesn’t apply when you call rgb2hsv and keep the HSV values in their float array.

### RenderScriptTools Class
The RenderScriptTools implements tools useful for functions that use RS. The applyConvolution3x3RS function applies any 3x3 kernel to any image. It uses ScriptIntrinsicConvolve3x3. The cleanRenderScript function can be called after any RS function to destroy the Script, RenderScript, and the two Allocation for input and output.

### ConvolutionTools Class
This class implements tools used by any filter that uses convolution without RenderScript. This class will be deprecated as soon as all convolution is done through RS. The two functions convulution1D and convulution2D are used to apply a 1D and 2D kernel on an image. This kernel can be of any size as long as it is odd. convulution1D also implements an optional correction for the pixels near the edge of the image; when the kernel tries to take the value of a pixel outside the image, it will instead take the closest one on the border of the image. convulution2DUniform can be used when the kernel has uniform weights such as with the Average filter. 

After every convolution algorithm, we want to make sure the values are still between 0 and 255. For that, we can use the normalizeOutput function. This function will make sure that even in the worst cases, the resulting values are kept between this range.
This is also the correctedPixelGet which isn’t used right now. This function corrects the pixel coordinates when the kernel is asking for a pixel outside the image. This is essentially what convulution1D is using but in a 2D context. However, I would advise against using it. Some properties can be used to correct pixel coordinates more efficiently when done directly in the loops.

### Filter Class
The filter class is where all filters are born. A filter as few properties: 
Each filter instance is created in the MainActivity when the program launches. There is no link between a Filter instance and its corresponding filter function. In order to create that connection, each Filter instance is created with a unique ID which is then used by its apply method. The apply method will read the ID and call the appropriate filter function. If there is a way to set a different apply function for each Filter instance (such as when we set a listener) then we could get rid of this ID.

The filters that use RenderScript are given images in the form of Bitmaps, whereas the other filters are using pixels arrays. Why not using only one kind of parameter for all filters? It comes without saying that we want to limit the number of unnecessary conversions between the two types of data structure. Because some filter calls each other (i.e. Gaussian Blur which call the toGray filter), I wanted for all filters to utilize pixels array. That way, we have one conversion happening before applying any filter, and one at the end of the process regardless of the number of filters called in between. However, I have been unable to make RS works with pixels array, only with Bitmap. This is why this hybrid system is currently used. Fortunately, this hasn’t increased the number of conversions, however, this means that non-RS filter cannot call RS ones. When all filters will be rewritten in RS, we will be able to use only Bitmaps. 

A Filter instance has various instance variables:

- A unique ID (an integer between 100 and 999).
- A name which is then displayed in the spinner.
- A redirection. Some filters are actually just redirection towards another—more general—filter. If this is the case, redirection is set to be the ID of that target filter.
- A Boolean that indicates if the filter uses RS.
- Does this filter use ColorSeekBar?
- Does this filter use the first SeekBar? If so, what its minimum, set, and maximum values should be, along with the unit displayed?
- Same thing for the second SeekBar.

### Details on some filter function
keepOrRemoveAColor is the filter function for the Keep a color and Remove a color filters. It takes a target hue as a parameter. Then, for each pixel, a pixel turns progressively greyer depending on the distance in degrees between its hue and the target hue. In order the accelerate the process, a lookup table (abbreviated to LUT from now on) has been used.
Other functions also use LUTs such as linearContrastStretching, histogramEqualization, and hueShift.
gaussianBlur was a difficult function to write. The Gaussian blur operation “can be applied to a two-dimensional image as two independent one-dimensional calculations” (taken from Wikipedia). Thanks to this property, we will be using a one-dimensional kernel.
I chose to scale the sigma with the size of the kernel. That way, the Gaussian kernel with always “look the same” but its resolution will increase with its size. In fact, the kernel will always have values between 1 and 90.

Not too much to talk about the other function, except that my implementation of histogramEqualization has to call rgb2v and rgb2hsv over all the pixels. I could have used a float array to store all the values but decided to preserve some memory instead.

## PERFORMANCES
The following test has been performed on a Samsung SM-A105FN, a low spec phone from released in February 2019. This phone has an AnTuTu score of 88.710, 2 GB of RAM, and uses a Samsung Exynos 7 Octa 7884 processor.
According to Device Atlas, France still most used phone in 2019 is the iPhone 7 (with 6.89% share) which has an AnTuTu score of 237.890 (+268% compared to the A10).

| Filter                                          | Use RS | HSV RGB | 1 Mpx (ms) | 3.6 Mpx (ms) |  %  |
|-------------------------------------------------|:------:|:-------:|:----------:|:------------:|:---:|
| Exposure                                        |    ✖   |         |     20     |      64      | 320 |
| Saturation                                      |    ✖   |         |     42     |      161     | 383 |
| Temperature                                     |    ✖   |         |     15     |      36      | 240 |
| Tint                                            |    ✖   |         |     13     |      43      | 331 |
| Sharpening                                      |    ✖   |         |     20     |      61      | 305 |
| Colorize                                        |        |    ✖    |     81     |      328     | 405 |
| Change hue                                      |        |    ✖    |     92     |      349     | 379 |
| Hue shift                                       |        |    ✖    |     208    |      592     | 285 |
| Invert                                          |    ✖   |         |     38     |      132     | 347 |
| Keep a color                                    |        |    ✖    |     246    |      762     | 310 |
| Remove a color                                  |        |    ✖    |     258    |      730     | 283 |
| Posterize                                       |    ✖   |         |     16     |      72      | 450 |
| Threshold                                       |    ✖   |         |     12     |      56      | 466 |
| Add noise                                       |    ✖   |         |     544    |     1650     | 303 |
| Linear contrast stretching                      |        |    ✖    |     272    |      979     | 360 |
| Histogram equalization                          |        |    ✖    |     321    |      974     | 303 |
| Average blur (2px)                              |        |         |     538    |     3030     | 563 |
| Average blur (20px)                             |        |         |    16010   |       ?      |  ?  |
| Gaussian blur (25px)                            |        |         |    1400    |     7390     | 527 |
| Gaussian blur (50px)                            |        |         |    3080    |     16300    | 529 |
| Gaussian blur (50px)(without border correction) |        |         |    2630    |     15400    | 586 |
| Gaussian blur (25px)                            |    ✖   |         |     39     |      188     | 482 |
| Laplacian (2px)                                 |        |         |     839    |     6040     | 720 |
| Laplacian (2px)                                 |    ✖   |         |     19     |      56      | 294 |
| Histogram                                       |        |         |     31     |      110     | 355 |

Those results show that the program still needs some improvements and optimizations. It makes it clear that filters using RenderScript are way faster than the others. The filters that use convolution kernels are expectedly slower than the rest. The Average blur filter is extremely slow at high kernel size. It is clear that the Gaussian blur being a separable filter makes a huge difference in performance when compared with the Average blur filter. Also, the Add noise filter is particularly slow despite using RenderScript. This is because it’s generating up to three random numbers for each pixel. It would be much faster—but more complicated—to superpose a pre-fetched noisy layer on top of the image.
Furthermore, the images used in a photography app such as this one would probably be those taken by the phone. The Samsung A10 takes pictures with a resolution of 13 Mpx which would make virtually all the filter unusable in real-time. It is clear that the interface should use a smaller version of the images to priorities interactivity, and only apply the filter to the original image when saving.

## MEMORY USAGE
The following test has been performed on the same phone as before. In order to better highlight some behavior, we used a 13 Mpx image.

The program memory usage starts around 77 MB and after one minute of standby, it has descended to about 53 MB. When we load the 13 Mpx image, the memory consumption skyrocketed to about 183 MB. When applying some filters, we can expect different results depending on which filter we use: the highest peak (at 407 MB) was the saturation filter which runs much faster than any other. However, because we were moving the seek bar, this led to many calls of this function in a short period of time. We suspect the garbage collector to not be able to perform its task in time which leads to this jump in memory consumption. Other filters with much longer calculation time will produce the constant rises that follow. Finally, when the app is left in standby, the memory stays constant at where it started which is good.

## BUGS AND LIMITATIONS
Most bugs/limitations have been fixed already. A few subsisted:

- The implementation of Laplacian edge detection using ScriptIntrinsicConvolve3x3 has a problem when using an amount parameter above 14. The image turns very bright. I suspect the problem to be caused by kernel weights above 128 (the center weight is equal to 8 * (amount + 1) which is superior or equal to 128 when amount is above 14). As using this filter with that much blur isn’t very useful, I decided to simply limit the user seek bar to values between 0 and 14. However, for this particular release, I left the possibility to go up to 20 for testing purposes.

- ScriptIntrinsicBlur isn’t able to handle blur radius above 25, this is not a limitation from this program, but from this library.

- In order to create a RenderScript instance, you need to provide a Context instance. I may have missed how to create one, but right now the program is using the Context from MainActivity. Because of this, it was necessary to make the Context static and accessible through a static method called getAppContext. This is a memory leak according to Android Studio. There is surely a better way of doing this, but I am not currently aware of it.

- When loading images, the resulting image is sometimes misoriented (turned 90 degrees in one direction). This is probably due to the fact that some system saves the image rotation has a property and not directly apply it on the image. A rotation filter will be added eventually, and therefore, this problem will be fixed.
