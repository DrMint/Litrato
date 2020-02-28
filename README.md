# IMAGE PROCESSING ON ANDROID

## DESCRIPTION
The goal of this project is to create an Android app capable of editing photos and saving those changes to the gallery.
Here a list of the features:
- Take or select an image: images can be obtained from the gallery, or directly from the camera.
- Zoom and scroll: it is possible to zoom on it using two fingers, and then move around with one finger.
- Numerous filters: the app allows the user to apply a vast array of filters.
- Highly optimized: all filters are using the latest technologies (such as RenderScript) to offer the best user experience.
- Reset and save: the user can always undo all filters applied by clicking the Original button. They can also save the image.
- User friendly interface: the UI is simple and intuitive.

[//]: # (_Features in **bold** are not yet implemented, or are not yet refined enough._)

This app utilizes [divyanshub024's](https://github.com/divyanshub024) [ColorSeekBar](https://github.com/divyanshub024/ColorSeekBar) (Copyright 2018 Divyanshu Bhargava, licensed under the Apache License, Version 2.0).


## INTERFACE

![](https://www.r-entries.com/etuliens/img/PT/interface.jpg)

When no filter has yet been applied, the `ORIGINAL` button will display`LOAD`. Clicking on it will ask to user if he wants to load an photo from the library, or take a photo.
When no filter has yet been applied, the `APPLY` button will also change and display `SAVE`. The user will have to grant the app the right to write on the internal storage.

When the user click on the histogram, it will reduce its size and make the image information disappear. Click on the histogram again will revert back to its prior state.

The user can zoom on the image using two fingers, and then move around with one finger or two fingers. Double tapping the image will zoom on it, and double tapping again will make the image fit on the screen.

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

Makes the image look sharper or blurrier.
- `Seek bar`: the image’s sharpness (between -100% and 100%).

### Colorize
![](https://www.r-entries.com/etuliens/img/PT/6.jpg)

Apply one color and saturation to the entire image.
- `Color seek bar`: the color you which to use.
- `Seek bar`: the color’s saturation (between 0% and 100%).

### Change hue
![](https://www.r-entries.com/etuliens/img/PT/9.jpg)

Apply one hue to the entire image but contrary to the Colorize filter, it doesn’t change the saturation of the image.
- `Color seek bar`: the color hue you which to use.

[//]: # (Add Hue shift)

### Hue shift
![](https://www.r-entries.com/etuliens/img/PT/8.jpg)

Shift all hues by a certain amount. This can give interesting results when used moderately.
- `Seek bar`: shift amount (in degrees).


### Invert
![](https://www.r-entries.com/etuliens/img/PT/7.jpg)

The luminance and colors are inverted: whites become blacks and reds become blues.

### Keep a color
![](https://www.r-entries.com/etuliens/img/PT/10.jpg)

Turns everything grayscales except for a specific color.
- `Color seek bar`: the color you which to keep.
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
- `Switch`: also turns the image greyscale.

### Threshold
![](https://www.r-entries.com/etuliens/img/PT/13.jpg)

If the pixel’s luminosity is bellowing the threshold, turns it back. Turns it white otherwise.
- `Seek bar`: set the threshold (between 0 and 255).

### Add noise
![](https://www.r-entries.com/etuliens/img/PT/14.jpg)

Adds some random noise to the image.
- `Seek bar`: the amount of noise (between 0 and 255).
- `Switch`: turns the noise greyscale or colored.

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

[//]: # (Add directional blur)

### Laplacian
![](https://www.r-entries.com/etuliens/img/PT/19.jpg)

Used to highlight all the image’s contours.
- `Seek bar`: how much detail should be kept (in pixels).

[//]: # (Add sobel)

## CLASSES AND FUNCTIONS
### ColorTools Class
This class implements all the functions necessary for conversions between RGB and HSV. **As our goal is to eventually remove all the FilterFunctions that doesn't utilize RenderScript, and HSV <> RBG convertions are done directly in Renderscript, this Class will soon be deprecated**. HSV is used by several filters, and because of that, it has been refined a little bit since the first version. First of all, each value (the hue, the saturation, and the luminosity) can now be converted separately. This has improved the performance substantially in some functions such as colorize (-40% in run-time when using rgb2v instead of rgb2hsv). Furthermore, we can reduce the run-time by another 10% by using integers instead of floats in rgb2s. Finally, by using some bit-level trickery such as using (color >> 16) & 0x000000FF instead of Color.red(color), we can expect another 14% improvement.

ColorTools also implements to removeAlpha function. It simply set the alpha value of all pixels to 255. This is useful for function that uses ScriptIntrinsicConvolve3x3 as it also apply the convolution kernel over the alpha values, which result in transparent images.

### RenderScriptTools Class
The RenderScriptTools implements tools useful for functions that use RS. The applyConvolution3x3RS function applies any 3x3 kernel to any image. However, it uses ScriptIntrinsicConvolve3x3. The applyConvolution function uses our own RenderScript convolution and isn't limited to 3x3 kernel. Actually, the kernel can even be rectangular. The cleanRenderScript function can be called after any RS function to destroy the Script, RenderScript, and a list of Allocation for input(s) and output(s).

### ConvolutionTools Class
This class implements tools used by any filter that uses convolution without RenderScript. **As our goal is to eventually remove all the FilterFunctions that doesn't utilize RenderScript this Class will soon be deprecated**. The two functions convulution1D and convulution2D are used to apply a 1D and 2D kernel on an image. This kernel can be of any size as long as it is odd. convulution1D also implements an optional correction for the pixels near the edge of the image; when the kernel tries to take the value of a pixel outside the image, it will instead take the closest one on the border of the image. convulution2DUniform can be used when the kernel has uniform weights such as with the Average filter.

After every convolution algorithm, we want to make sure the values are still between 0 and 255. For that, we can use the normalizeOutput function. This function will make sure that even in the worst cases, the resulting values are kept between this range.

There is the convertGreyToColor function which can be to turn a array of integer to a array of android.graphics.Color which is what Bitmap uses. It is only used by functions that also uses ConvolutionTools.

Finally there is the correctedPixelGet which isn’t used right now. This function corrects the pixel coordinates when the kernel is asking for a pixel outside the image. This is essentially what convulution1D is using but in a 2D context. However, I would advise against using it. Some properties can be used to correct pixel coordinates more efficiently when done directly in the loops.



### FilterFunction Class
This class is where all filters are born. A filter function is a static method of this class. It will always takes in parameter a Bitmap (the image to modify) and returns nothing. Most filters can also be tuned by some parameters. Lastly, those that use RenderScript will be given a Context in parameter.


### FilterFunctionIntrinsic Class
This class also implement filters, however, it uses Intrinsic functions. If a filter function has a Intrinsic equivalent (i.e. a function that uses ScriptIntrinsicConvolve3x3), then the name of the function should be the same in both classes. That way, it is easy to switch between them by simply changing the class when calling the function. This is also true for FilterFunctionDeprecated.


### FilterFunctionDeprecated Class
This class is the legacy versions of currently used filters. Functions that uses non-RS convolution are in this class.

keepOrRemoveAColor is the filter function for the Keep a color and Remove a color filters. It takes a target hue as a parameter. Then, for each pixel, a pixel turns progressively greyer depending on the distance in degrees between its hue and the target hue. In order the accelerate the process, a lookup table (abbreviated to LUT from now on) has been used.
Other functions also use LUTs such as linearContrastStretching, histogramEqualization, and hueShift.

gaussianBlur was a difficult function to write. The Gaussian blur operation “can be applied to a two-dimensional image as two independent one-dimensional calculations” (taken from Wikipedia). Thanks to this property, we will be using a one-dimensional kernel. I chose to scale the sigma with the size of the kernel. That way, the Gaussian kernel with always “look the same” but its resolution will increase with its size. In fact, the kernel will always have values between 1 and 90.

Not too much to talk about the other function, except that my implementation of histogramEqualization has to call rgb2v and rgb2hsv over all the pixels. I could have used a float array to store all the values but decided to preserve some memory instead.

### Filter Class
A Filter is an object that describes which input (colorSeekBar, seekBars, switches etc...) the user has access to. Each Filter instance is created in the MainActivity when the program launches. At first, there is no link between a Filter instance and its corresponding FilterFunction. In order to create that connection, each Filter instance is given a new FilterInterface object. This interface is used to declare which FilterFunction should be called when applying the filter.

A Filter instance has various instance variables:

- A name which is then displayed in the spinner.
- Does this filter use ColorSeekBar?
- Does this filter use the first SeekBar? If so, what its minimum, set, and maximum values should be, along with the unit displayed?
- Same thing for the second SeekBar.
- Does this filter use the first Switch? If so, what is its default state, and what should be displayed when it is on or off.
- An interface used to launch the right FilterFunction.

### MainActivity Class
This is the core of the app.
Here is how a filter is declared:

```java
Filter newFilter = new Filter("Name");       // We starts by creating a new Filter object with a given name.
newFilter.setColorSeekBar();                 // This filter will use the ColorSeekBar
newFilter.setSeekBar1(0, 100, 100, "%");     // It will also use the first SeekBar and the minimum, set, maximum value and unit is given in parameter.
newFilter.setSeekBar2(0, 100, 100, "%");     // It will also use the second SeekBar
newFilter.setSwitch1(true,"Off", "On");   // It will use the switch, default state, false state displayed name, and true state dispayed name

// We override its apply method to redirect towards the appropriate FilterFunction.
newFilter.setFilterFunction(new FilterInterface() {
    @Override                               
    public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
        FilterFunctions.filterFunctionName(bmp, colorSeekHue, seekBar, seekBar2, switch1);
    }
});
filters.add(newFilter);                     // Finally, we add this new Filter to filters (an array of Filter instances).
```

Then we populate the spinner with the names of filters in our array of filter. The order they are displayed in the spinner follows the order they were added.


### ImageViewZoomScroll Class
This class is used to add new functionalities to ImageView objects: the ability to zoom and scroll on the image.
Zoom and scroll events are handle by the MainActivity class, this class is used to calculate which portion of the image should be displayed. The more we zoom, the smaller this surface. When we scroll, we are moving this surface around.

This surface is a rectangle defined by `newHeight`, `newWidth`, and `center`.
If we want to change zoom level, we use the following line, we can use `setZoom(float zoom)` and we can move the image by using `translate(int x, int y)`.

Finally, when we want to refresh the image displayed by the our ImageView, we can use to following line of code:
```Java
Bitmap displayedBmp = Bitmap.createBitmap(  
        fullSizeBmp,   
	myImageView.getX(),   
	myImageView.getY(),   
	myImageView.getNewWidth(),   
	myImageView.getNewHeight());
```

Another very useful function in imageViewTouchPointToBmpCoordinates which convert the pixel touched on the imageView to the coordinates of that pixel in image (regardless of zoom and center position).


### Settings Class
This class is where constants and magic numbers are stored. It gives easy access to some settings.
- `IMPORTED_BMP_SIZE`: the maximum size of a loaded image. If the image is rectangular, the longest dimension will be resized to `IMPORTED_BMP_SIZE` and the other will be smaller than `IMPORTED_BMP_SIZE`. Default: `1000`
- `MAX_ZOOM_LEVEL`: how much the user can zoom on the image. For example: 5f means we can zoom until only 1/5 of the image is displayed. Default `5f`.
- `DOUBLE_TAP_ZOOM`: how much it zooms on the image when double tapping it. Default `3f`.
- `IMAGE_RATIO_PRECISION`: A magic number used by ImageViewZoomScroll when comparing the imageView ratio and the image ratio. Because those two values will never be exactly the same,  this value is how far off is still considered equal.

### Point Class
A point is an object with two integers. It is possible to create a point, copy a point, translate it, and test if two points are equals. It is used by ImageViewZoomScroll.


## PERFORMANCES
The following test has been performed on a Samsung SM-A105FN, a low spec phone from released in February 2019. This phone has an AnTuTu score of 88.710, 2 GB of RAM, and uses a Samsung Exynos 7 Octa 7884 processor.
According to Device Atlas, France still most used phone in 2019 is the iPhone 7 (with 6.89% share) which has an AnTuTu score of 237.890 (+268% compared to the A10).

The last column is the ratio between the processing time for 1 Mpx and 3.6 Mpx. If the process is linear, is should be 360%.

| Filter                                          | Use RS | HSV RGB | 1 Mpx (ms) | 3.6 Mpx (ms) |  %  |
|-------------------------------------------------|:------:|:-------:|:----------:|:------------:|:---:|
| Exposure                                        |    ✖   |         |     20     |      64      | 320 |
| Saturation                                      |    ✖   |         |     42     |      161     | 383 |
| Temperature                                     |    ✖   |         |     15     |      36      | 240 |
| Tint                                            |    ✖   |         |     13     |      43      | 331 |
| Sharpening                                      |    ✖   |         |     20     |      61      | 305 |
| Colorize                                        |        |    ✖    |     81     |      328     | 405 |
| Colorize                                        |    ✖   |    ✖    |          |           |  |
| Change hue                                      |        |    ✖    |     92     |      349     | 379 |
| Change hue                                      |    ✖   |    ✖    |          |           |  |
| Hue shift                                       |        |    ✖    |     208    |      592     | 285 |
| Invert                                          |    ✖   |         |     38     |      132     | 347 |
| Keep a color                                    |        |    ✖    |     246    |      762     | 310 |
| Keep a color                                    |    ✖   |    ✖    |          |           |  |
| Remove a color                                  |        |    ✖    |     258    |      730     | 283 |
| Remove a color                                  |    ✖   |    ✖    |          |           |  |
| Posterize                                       |    ✖   |         |     16     |      72      | 450 |
| Threshold                                       |    ✖   |         |     12     |      56      | 466 |
| Add noise                                       |    ✖   |         |     544    |     1650     | 303 |
| Linear contrast stretching                      |        |    ✖    |     272    |      979     | 360 |
| Linear contrast stretching                      |    ✖   |         |     |      |       |
| Histogram equalization                          |        |    ✖    |     321    |      974     | 303 |
| Histogram equalization                          |    ✖   |         |          |           |  |
| Average blur (2px)                              |        |         |     538    |     3030     | 563 |
| Average blur (20px)                             |        |         |    16010   |       ?      |  ?  |
| Average blur (20px)                             |    ✖   |         |    |             |    |
| Gaussian blur (25px)                            |        |         |    1400    |     7390     | 527 |
| Gaussian blur (50px)                            |        |         |    3080    |     16300    | 529 |
| Gaussian blur (50px) (without border correction) |        |         |    2630    |     15400    | 586 |
| Gaussian blur (25px)                            |    ✖   |         |     |      | |
| Gaussian blur (INTRINSIC) (25px)                |    ✖   |         |     39     |      188     | 482 |
| Laplacian (2px)                                 |        |         |     839    |     6040     | 720 |
| Laplacian (2px)                                 |    ✖    |         |     |     | |
| Laplacian (INTRINSIC) (2px)                     |    ✖   |         |     19     |      56      | 294 |
| Sobel (2px)                                     |    ✖   |         |     |      | |
| Histogram                                       |        |         |     31     |      110     | 355 |

Those results show that the program still needs some improvements and optimizations. It makes it clear that filters using RenderScript are way faster than the others. The filters that use convolution kernels are expectedly slower than the rest. The Average blur filter is extremely slow at high kernel size. It is clear that the Gaussian blur being a separable filter makes a huge difference in performance when compared with the Average blur filter. Also, the Add noise filter is particularly slow despite using RenderScript. This is because it’s generating up to three random numbers for each pixel. It would be much faster—but more complicated—to superpose a pre-fetched noisy layer on top of the image.
Furthermore, the images used in a photography app such as this one would probably be those taken by the phone. The Samsung A10 takes pictures with a resolution of 13 Mpx which would make virtually all the filter unusable in real-time. It is clear that the interface should use a smaller version of the images to priorities interactivity, and only apply the filter to the original image when saving.

## MEMORY USAGE
The following test has been performed on the same phone as before. In order to better highlight some behavior, we used a 13 Mpx image.

![](https://www.r-entries.com/etuliens/img/PT/memory.jpg)

The program memory usage starts around 77 MB and after one minute of standby, it has descended to about 53 MB. When we load the 13 Mpx image, the memory consumption skyrocketed to about 183 MB. When applying some filters, we can expect different results depending on which filter we use: the highest peak (at 407 MB) was the saturation filter which runs much faster than any other. However, because we were moving the seek bar, this led to many calls of this function in a short period of time. We suspect the garbage collector to not be able to perform its task in time which leads to this jump in memory consumption. Other filters with much longer calculation time will produce the constant rises that follow. Finally, when the app is left in standby, the memory stays constant at where it started which is good.

## BUGS AND LIMITATIONS
Most bugs/limitations have been fixed already. A few subsisted:

- When the histogram is resize, the image can get stretch because the imageView gets bigger or smaller.  
Refreshing the image doesn't seem to work. we suspect this is because requestLayout is asynchronous, and  
when the image refresh, it utilizes the imageView's aspect ratio before it actually changed.  
Thus, refreshing the image will actually make the problem worse.

- Right now, we only load a miniature of the image when it is taken from the camera.  We have to ask the system to create a temporary file in order to store the full image. Until then, the image is only 187px by 250px.

- The implementation of Laplacian edge detection using ScriptIntrinsicConvolve3x3 has a problem when using an amount parameter above 14. The image turns very bright. I suspect the problem to be caused by kernel weights above 128 (the center weight is equal to 8 * (amount + 1) which is superior or equal to 128 when amount is above 14). As using this filter with that much blur isn’t very useful, I decided to simply limit the user seek bar to values between 0 and 14. However, for this particular release, I left the possibility to go up to 20 for testing purposes.

- ScriptIntrinsicBlur isn’t able to handle blur radius above 25, this is not a limitation from this program, but from this library.

- When loading images, the resulting image is sometimes misoriented (turned 90 degrees in one direction). This is probably due to the fact that some system saves the image rotation has a property and not directly apply it on the image. A rotation filter will be added eventually, and therefore, this problem will be fixed.

- The app cannot be used in landscape mode, or else the layout gets terrible. We have lock the app in portrait mode.

- [Talk about how RenderScript does random shit on different phones / emulators]

## FUTURE FEATURES

- When taking an image, we have to store it to get it at full resolution.  

- Rotation of the image (at first 90, -90, 180 then any degrees).  

- Crop an image, possibly merging the rotation and crop function UI wise.  

- Makes sure that all filter functions are using RenderScript.  

- An idea to keep the UI interactive while saving the image at high resolution would be to make all the modification on a smaller size image, save all the filter applied, and then apply them again to the full size image when saving. It is okay for the user to wait a few seconds when saving, but not while using a seekBar.


