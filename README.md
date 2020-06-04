
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


## INTERFACE

<img src="https://www.r-entries.com/etuliens/img/Litrato/layout.jpg" width="100%">

When the app is first launched, a permission request to access storage is prompted. Then a default image will be displayed. The user can then load an image from his gallery or take a picture by clicking on the top left `File` icon and modify his personal images. What was simply called "Filters" has been divided in three categories : `Presets` which are predefined filters that can be applied just be tapping on its miniature, `Tools` which include all the essential tools to modify an image, and finally `Filters`. This last one is further divided into four categories: `Color`, `Fancy`, `Blur` and `Contour`.

At the top, there is also the `History` button with a clock icon. Using this button, the user can preview and revert to any prior state of the image using a slider. Once the user selected a prior state, he can click on the `Confirm` button to validate their choice.

Further to the right there is the save button, quick access to 90° counterclockwise and clockwise rotations. Lastly, a dropmenu gives access to Settings and EXIF Viewer. The first one allow the user to change some parameters in the app and the second one is usuful to learn more about the loaded image, such as where it has taken, by what camera...

Back to the main window, the user can zoom on the image using two fingers, and then move around with one finger or two fingers. Double tapping the image will zoom on it, and double tapping again will make the image fit on the screen. Warning : sometimes gesture navigation is disabled depending on the situation. Some functions use the touch as an input, such as `Color Picker` or `Crop`.

<img src="https://www.r-entries.com/etuliens/img/Litrato/layout2.jpg" width="100%">

If the user tap on one of a tool or a filter, a new window will appear prompting them to adjust the filter as they wishes. Once the user is happy with the result, they can click on the `Apply` button to validate the modification. The bar at the very bottom also shows the name of the active filter/tool. Clicking on the name can toggle on or off the "Filter menu". The number and the disposition of the controls vary according to the filter. Some use the Color Seek bar to select a tint. In this case, the `Pick a Color` tool also appear on its left. The user can use this tool to select directly on the image the tint they want the work with. At most, a filter can display two seekbars, one colorSeekBar, a switch, and a special menu (currently only used by "Stickers") to display a list of selectable bitmaps.

On the left, two other buttons can appear. The first one is the `Masking` tool. Clicking on it will bring another window where the user can draw where the filter should be applied on the image. By default, the mask is entirely black, meaning that the filter will be apply nowhere. The user can choose the draw in white or black, the size of the brush, and the view opacity of the mask. This last option is purely visual and doesn't change the mask whatsoever.
Let's go back to the Filter menu, the last option at the bottom left is `Histogram`, a button that will toggle the histogram, a visual representation of the repartition of RGB values. It is overprinted on the image to allow the user to still view what is underneath. The histogram uses logarithm scaling on the Y-axis.

## FILTERS

### Original image
<img src="https://www.r-entries.com/etuliens/img/Litrato/default_image.jpg" width="40%">


### Auto
<img src="https://www.r-entries.com/etuliens/img/Litrato/auto.jpg" width="40%">
Provides two ways to increase the contrast without burning values (burning refers to values getting out of the 0-255 scale).
The first is by maximizing the range of luminances values of the image. The second is called Dynamic extension, and tries to make the histogram as flat as possible.

### Luminosity
<img src="https://www.r-entries.com/etuliens/img/Litrato/luminosity.jpg" width="40%">
Changes how bright or dark the image is. It turns up too much, this will burn the image.

- `Seek bar`: the image's brightness (between -100% and 100%).
- `Seek bar 2`: the image's gamma value (between -100% and 100%).

### Saturation
<img src="https://www.r-entries.com/etuliens/img/Litrato/saturation.jpg" width="40%">
Changes the saturation of the image.

- `Seek bar`: the image's saturation (between 0 and 200%)

### Temperature
<img src="https://www.r-entries.com/etuliens/img/Litrato/temperature.jpg" width="40%">
Makes the image look warmer or colder.

- `Seek bar`: the image’s temperature (between -100\% and 100\%).

### Tint
<img src="https://www.r-entries.com/etuliens/img/Litrato/tint.jpg" width="40%">
Changes the tint of an image. Makes the images more magenta or green.

- `Seek bar`: the image’s tint (between -100\% and 100\%).


### Sharpening
<img src="https://www.r-entries.com/etuliens/img/Litrato/sharpness.jpg" width="40%">
Makes the image look sharper or blurrier.

- `Seek bar`: the image’s sharpness (between -100\% and 100\%).


### Colorize
<img src="https://www.r-entries.com/etuliens/img/Litrato/colorize.jpg" width="40%">
Apply one color and saturation to the entire image.

- `Color seek bar`: the color you wish to use.
- `Seek bar`: the color’s saturation (between 0\% and 100\%).


### Change Hue
<img src="https://www.r-entries.com/etuliens/img/Litrato/changehue.jpg" width="40%">
Apply one hue to the entire image but contrary to the Colorize filter, it doesn’t change the saturation of the image.

- `Color seek bar`: the color hue you wish to use.


### Hue Shift
<img src="https://www.r-entries.com/etuliens/img/Litrato/hueshift.jpg" width="40%">
Shift all hues by a certain amount. This can give interesting results when used moderately.

- `Color seek bar`: the color hue you wish to use.
- `Seek bar`: shift amount (between -180 and 180 degrees).

### Threshold
<img src="https://www.r-entries.com/etuliens/img/Litrato/threshold.jpg" width="40%">
If the pixel’s luminosity is bellow the threshold, turns it back. Turns it white otherwise.

- `Color seek bar`: the color hue you wish to use.
- `Seek bar`: shift amount (between -180 and 180 degrees).

### Selective Coloring
<img src="https://www.r-entries.com/etuliens/img/Litrato/selectivecoloring.jpg" width="40%">
Turns everything grayscale except for a specific color. If in remove mode (using the Switch), the user can reverse the effect and only remove that color instead of keeping it.

- `Color seek bar`: the color hue you wish to use.
- `Seek bar`: how far off the color can be (in degrees).
- `Seek bar`: chose between keeping or removing that color.



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

Turns everything grayscale except for a specific color.
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
This class implements all the functions necessary for conversions between RGB and HSV. **As our goal is to eventually remove all the FilterFunctions that doesn't utilize RenderScript, and HSV <> RBG conversions are done directly in Renderscript, this Class will soon be deprecated**. HSV is used by several filters, and because of that, it has been refined a little bit since the first version. First of all, each value (the hue, the saturation, and the luminosity) can now be converted separately. This has improved the performance substantially in some functions such as colorize (-40% in run-time when using rgb2v instead of rgb2hsv). Furthermore, we can reduce the run-time by another 10% by using integers instead of floats in rgb2s. Finally, by using some bit-level trickery such as using (color >> 16) & 0x000000FF instead of Color.red(color), we can expect another 14% improvement.

ColorTools also implements to removeAlpha function. It simply set the alpha value of all pixels to 255. This is useful for function that uses ScriptIntrinsicConvolve3x3 as it also apply the convolution kernel over the alpha values, which result in transparent images.

### RenderScriptTools Class
The RenderScriptTools implements tools useful for functions that use RS. The applyConvolution3x3RS function applies any 3x3 kernel to any image. However, it uses ScriptIntrinsicConvolve3x3. The applyConvolution function uses our own RenderScript convolution and isn't limited to 3x3 kernel. Actually, the kernel can even be rectangular. The cleanRenderScript function can be called after any RS function to destroy the Script, RenderScript, and a list of Allocation for input(s) and output(s).

### ConvolutionTools Class (Deprecated)
This class implements tools used by any filter that uses convolution without RenderScript. **It is now deprecated as all FilterFunctions now use RenderScript based convolution**. The two functions convolution1D and convolution2D are used to apply a 1D and 2D kernel on an image. This kernel can be of any size as long as it is odd. convulution1D also implements an optional correction for the pixels near the edge of the image; when the kernel tries to take the value of a pixel outside the image, it will instead take the closest one on the border of the image. convulution2DUniform can be used when the kernel has uniform weights such as with the Average filter.

After every convolution algorithm, we want to make sure the values are still between 0 and 255. For that, we can use the normalizeOutput function. This function will make sure that even in the worst cases, the resulting values are kept between this range.

There is the convertGreyToColor function which can be to turn a array of integer to a array of android.graphics.Color which is what Bitmap uses. It is only used by functions that also uses ConvolutionTools.

Finally there is the correctedPixelGet which isn’t used right now. This function corrects the pixel coordinates when the kernel is asking for a pixel outside the image. This is essentially what convolution1D is using but in a 2D context. However, I would advise against using it. Some properties can be used to correct pixel coordinates more efficiently when done directly in the loops.



### FilterFunction Class
This class is where all filters are born. A filter function is a static method of this class. It will always takes in parameter a Bitmap (the image to modify) and returns nothing. Most filters can also be tuned by some parameters. Lastly, those that use RenderScript will be given a Context in parameter.


### FilterFunctionIntrinsic Class
This class also implement filters, however, it uses Intrinsic functions. If a filter function has a Intrinsic equivalent (i.e. a function that uses ScriptIntrinsicConvolve3x3), then the name of the function should be the same in both classes. That way, it is easy to switch between them by simply changing the class when calling the function. This is also true for FilterFunctionDeprecated.


### FilterFunctionDeprecated Class (Deprecated)
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
newFilter.setSwitch1(true,"Off", "On");   // It will use the switch, default state, false state displayed name, and true state displayed name

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
This class is used to add new functionality to ImageView objects: the ability to zoom and scroll on the image.
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

|          Filter         | RS | HSV | 0.185 Mpx | 0.750 Mpx | 3.00 Mpx |  %  |  %  |
|:-----------------------:|:--:|:---:|:---------:|:---------:|:--------:|:---:|:---:|
| Old analog              |  ✖ |     |    210    |    302    |    506   | 144 | 168 |
| Night from day          |  ✖ |     |    200    |    642    |   1720   | 321 | 268 |
|                         |    |     |           |           |          |     |     |
| Rotation                |    |     |     23    |     53    |    209   | 230 | 394 |
| Crop                    |    |     |     4     |     6     |    14    | 150 | 233 |
| Flip                    |  ✖ |     |     19    |     20    |    32    | 105 | 160 |
| Stickers                |    |     |     5     |     6     |    16    | 120 | 267 |
| Luminosity              |  ✖ |     |     35    |     33    |    155   |  94 | 470 |
| Contrast                |  ✖ |     |     23    |     22    |    49    |  96 | 223 |
| Sharpness               |  ✖ |     |     22    |     45    |    102   | 205 | 227 |
| Auto                    |  ✖ |     |     35    |    100    |    361   | 286 | 361 |
| Saturation              |  ✖ |     |     15    |     18    |    35    | 120 | 194 |
| Add   noise             |  ✖ |     |    115    |    380    |   1320   | 330 | 347 |
| Temperature             |  ✖ |     |     14    |     16    |    23    | 114 | 144 |
| Tint                    |  ✖ |     |     15    |     16    |    22    | 107 | 138 |
|                         |    |     |           |           |          |     |     |
| Colorize                |  ✖ |  ✖  |     13    |     20    |    37    | 154 | 185 |
| Change   hue            |  ✖ |  ✖  |     12    |     25    |    45    | 208 | 180 |
| Selective coloring      |  ✖ |  ✖  |     19    |     30    |    81    | 158 | 270 |
| Hue shift               |  ✖ |  ✖  |     13    |     30    |    71    | 231 | 237 |
| Threshold               |  ✖ |     |     11    |     17    |    30    | 155 | 176 |
| Posterize               |  ✖ |     |     22    |     20    |    54    |  91 | 270 |
| Average blur (2px)      |  ✖ |     |     21    |     55    |    150   | 262 | 273 |
| Average blur (20px)     |  ✖ |     |    309    |    1034   |   5820   | 335 | 563 |
| Gaussian blur (2px)     |  ✖ |     |     39    |     62    |    165   | 159 | 266 |
| Gaussian blur (20px)    |  ✖ |     |     60    |    168    |    507   | 280 | 302 |
| Directional blur (20px) |  ✖ |     |     38    |     69    |    231   | 182 | 335 |
| Laplacian (2px)         |  ✖ |     |     68    |     98    |    264   | 144 | 269 |
| Sobel (2px)             |  ✖ |     |     53    |    102    |    285   | 192 | 279 |
| Sketch                  |  ✖ |     |    168    |    288    |    566   | 171 | 197 |
| Cartoon                 |  ✖ |     |    221    |    398    |   1120   | 180 | 281 |
|                         |    |     |           |           |          |     |     |
| Mask apply              |    |     |     26    |     62    |    96    | 238 | 155 |
| Histogram               |    |     |     70    |     72    |    79    | 103 | 110 |

Those results show that the program still needs some improvements and optimizations. It makes it clear that filters using RenderScript are way faster than the others. The filters that use convolution kernels are expectedly slower than the rest. The Average blur filter is extremely slow at high kernel size. It is clear that the Gaussian blur being a separable filter makes a huge difference in performance when compared with the Average blur filter. Also, the Add noise filter is particularly slow despite using RenderScript. This is because it’s generating up to three random numbers for each pixel. It would be much faster—but more complicated—to superpose a pre-fetched noisy layer on top of the image.

Our implementation of the Gaussian blur much slower than ScriptIntrinsicBlur. Because of this, Sobel and Laplacian are also significantly slower.

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

- ScriptIntrinsicBlur isn’t able to handle blur radius above 25, this is not a limitation from this program, but from this library.

- The app cannot be used in landscape mode, or else the layout gets terrible. We have lock the app in portrait mode.

- On emulator, seekbars with a negative minimum value cannot go below 0, but it work just fine on most phones we tested.

## New features since last release

Filters:
 - [X] Added a "Cartoon" and "Sketch" filters that limits the number of colors and highlight the contour of the image.
 - [X] Added the ability to flip the image horizontally, and also to change the contrast and gamma.
 - [X] Added rotation of the image at any degrees and crop (also possible to keep the aspect ratio).
 - [X] Added 14 presets (a quick way to apply filter, without parameters).
 
Tools:
 - [X] Added “Color Picker”, a tool to select a hue directly from the image.
 - [X] Added "History" that gives the user the ability to revert to any prior state of the image.
 - [X] Added a setting menu where the user can tweak some parameters. Those values are saved on the phone.
 - [X] Added a menu to view most of the EXIF values of the image, such as the ISO, f-number or where the photo was taken.
 - [X] Ability to only apply a filter to a part of the image. This "mask" is drawn by the user using its finger.
 
UI:
 - [X] Complete overall of the user interface, with menus, separate interfaces, and the preview of each filter.
 - [X] Added dark/light theme.
 - [X] ImageViewZoomScroll has been entirely rewritten. It is now using a matrix to zoom and translate the image.
 - [X] Bug removal: when the histogram was resized, the image was stretching.
 
Load and save:
 - [X] It is now possible to save the image in its original resolution, and have a smaller resolution while using the app.
 - [X] Bug removal: take a picture from the camera at higher resolution than 187px by 250px.
