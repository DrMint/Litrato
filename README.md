
# IMAGE PROCESSING ON ANDROID

## DESCRIPTION
The goal of this project is to create an Android app capable of editing photos and saving those changes to the gallery.

Filters and effects:
- 14 presets to quickly apply a filter (without any parameters).
- Transformations such as rotation, crop, flip...
- Essentials tools such as luminosity, contrast, gamma, saturation...
- More advanced filters that use convolution or blending two or more images.
- Almost all filters are using RenderScript, which accelerates the calculations.

UI:
- The UI is simple and intuitive, with menus, separate interfaces, and the preview of each filter.
- Dark and light theme.
- Zoom and scroll: it is possible to zoom on the image using two fingers or with a double tap, and then move around with one finger.

Tools:
- “Color Picker”, a tool to select a hue directly from the image.
- "History" gives the user the ability to revert to any prior state of the image.
- A setting menu where the user can tweak some parameters. Those values are saved on the phone.
- An menu to view most EXIF values of the image, such as the ISO, f-number or where the photo was taken.
- Ability to only apply a filter to a part of the image. This "mask" is drawn by the user using its finger.

Load and save:
- Take or select an image: images can be obtained from the gallery, or directly from the camera.
- It is possible to save the image in its original resolution, and have a smaller resolution while using the app.


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
Provides two ways to increase the contrast without burning values (burning refers to values getting out of the 0-255 range).
The first is by maximizing the range of luminances values of the image. The second is called Dynamic extension, and tries to make the histogram as flat as possible.

- `Switch`: choose which way to increase contrast.

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
- `Switch`: chose between keeping or removing that color.


### Add noise
<img src="https://www.r-entries.com/etuliens/img/Litrato/addnoise.jpg" width="40%">
Adds some random noise to the image.

- `Seek bar`: the amount of noise (between 0 and 255).
- `Switch`: turns the noise greyscale or colored.


### Posterize
<img src="https://www.r-entries.com/etuliens/img/Litrato/posterize.jpg" width="40%">
Reduces the number of distinct colors, also called color quantization.

- `Seek bar`: how many possible colors should be kept in each channel (in steps between 2 and 32).
- `Switch`: also turns the image greyscale.

### Average Blur
<img src="https://www.r-entries.com/etuliens/img/Litrato/averageblur.jpg" width="40%">
Blurs the image by averaging equally all pixels with their neighbors.

- `Seek bar`: blur amount (between 1 and 19 pixels).

### Gaussian Blur
<img src="https://www.r-entries.com/etuliens/img/Litrato/gaussianblur.jpg" width="40%">
Blurs the image by using a Gaussian function.

- `Seek bar`: blur amount (between 1 and 25 pixels).

### Laplacian
<img src="https://www.r-entries.com/etuliens/img/Litrato/laplacian.jpg" width="40%">
Used to highlight all the image’s contours.

- `Seek bar`: how much details should be kept(between 1 and 14 pixels).


### Directional Blur
<img src="https://www.r-entries.com/etuliens/img/Litrato/directionalblur.jpg" width="40%">
Blurs the image horizontally or vertically.

- `Seek bar`: blur amount (between 2 and 30 pixels).
- `Switch`: horizontal or vertical.


### Cartoon
<img src="https://www.r-entries.com/etuliens/img/Litrato/cartoon.jpg" width="40%">
Applies a cartoon effect. This is achieve by reducing the number of color values and highlighting the contours.

- `Seek bar`: the black value of the "shading" (everything expect the contours).
- `Seek bar 2`: the number of colors.

### Sketch
<img src="https://www.r-entries.com/etuliens/img/Litrato/sketch.jpg" width="40%">
Sketch effect.

- `Seek bar`: how thick or narrow the contours should be.
- `Seek bar 2`: how much colors should remain.

### Crop
<img src="https://www.r-entries.com/etuliens/img/Litrato/crop.jpg" width="40%">
Crop the image at the desired size. The area is selected using touch.

- `Switch`: how thick or narrow the contours should be.

### Flip
<img src="https://www.r-entries.com/etuliens/img/Litrato/flip.jpg" width="40%">
Applies a horizontal flip of the image.

### Rotation
<img src="https://www.r-entries.com/etuliens/img/Litrato/rotation.jpg" width="40%">
Applies a rotation to the image.

- `Seek bar`: rotation in degrees (between -180 and 180).

### Stickers
<img src="https://www.r-entries.com/etuliens/img/Litrato/sticker.jpg" width="40%">
Allows to put stickers on the image. Touching the screen applies a sticker at that location.

- `Seek bar`: size of the sticker in percent (between 10% and 290%)
- `Seek bar`: rotation of the sticker in degrees (between -180 and 180)



## PACKAGES, CLASSES, AND FUNCTIONS

### 1 - Packages

At the root folder there are three packages:

- activities: containing everything related to the activities, the tools to managed the UI, menus etc...
- filters: all the Classes and Functions that deals with images.
- tools: other Classes to help with any part of the app.

Each package also as a sub-package named tools. Tools are packages filled with Classes useful for the parent package they're included in.

### 2 - Activities Package

#### 2.1 - MainActivity Class
This is the core of the app. This Class initializes a lot of variables for other classes such as calling Settings.setDPValuesInPixel, or generating the listeners such as menuButtonListener or menuItemListener. This Class and all the other Activities implements a method named `applyColorTheme()`. This method is usually called by onCreate but because this activities has an ActionBar, it needs to be called by onCreateOptionsMenu.

#### 2.2 - ExifActivity Class
An activity to view the image EXIF data. EXIF is a meta-data format used by a lot of image formats and even sounds files. It contains most notably the camera model and manufacturer, the exposure, ISO, focal length... Also the GPS coordinates where the image was taken. To display the coordinates, we used the Google Maps API. This API key can be found in res/google\_maps\_api.xml. When dark theme is enabled, the map uses raw/style\_gmap\_night.json

#### 2.3 - FiltersActivity Class
An activity used to prompt the user to tweak the filter parameter. This activity can also start a new instance of itself, most notably to create a mask.

#### 2.4 - PreferencesActivity Class
This class is where the user can view and change app's preferences.

#### 2.5 - Tools Package
#### 2.5.1 - History Class
This Class allows to manage the history. It takes an ArrayList of AppliedFilter as attribute. When a filter is applied to an image (and confirmed by the user), it is added to the ArrayList. Then by using `goUntilFilter` we can get back the image to any prior stage. To achieve this result, this method takes the image and apply all the filters until that state is achieved. This way, we don't have to save a bitmap for each step, and you can reapply the same actions to the image in its original resolution (compared to its loaded resolution which is usually lower to increase reactivity of the UI). `removeUntil` allows to remove all states following the given state, reverting the history to that state.

#### 2.5.2 - Preference Enum
It is used to ensure the names used are the same throughout the code.

#### 2.5.3 - PreferenceManager Class
This Class is used to save and load preferences on the phone. It also store the default values when the app is first installed. This is where the default values such as
- `DARK_MODE`: defines if the color theme should be dark or light.
- `IMPORTED_BMP_SIZE`: the maximum size of a loaded image. If the image is rectangular, the longest dimension will be resized to `IMPORTED_BMP_SIZE` and the otherwill be smaller than `IMPORTED_BMP_SIZE`. Default: `1000`
- `MINIATURE_BMP_SIZE`: the size of the miniature used in the filter and preset menu.
- `SAVE_ORIGINAL_RESOLUTION`: if true, the history is reapplied to the original image (the image as it was before reducing it to its `IMPORTED_BMP_SIZE`.)
- `OPEN_HISTOGRAM_BY_DEFAULT`: if true, make the histogram visible by default when using a filter.

#### 2.5.4 - Settings Class
This class is where constants and magic numbers are stored. It gives easy access to some settings.
- `MAX_ZOOM_LEVEL`: how much the user can zoom on the image. For example: 5f means we can zoom until only 1/5 of the image is displayed. Default `5f`.
- `DOUBLE_TAP_ZOOM`: how much it zooms on the image when double tapping it. Default `3f`.
- `OUTPUT_JPG_QUALITY`: The quality of the saved image. 100 means no compression, the lower you go, the higher the compression.
- `SAVE_PATH`: the path to Litrato's folder. Photo are not saved there if using the Android MediaStore.
- `SAVE_PATH_ORIGINAL`: the path to the subfolder of Litrato where the captured image are saved.
- Layout related constant such as `ITEMS_MARGIN_IN_MENU`, `PADDING_BETWEEN_MINIATURE_AND_LABEL`...
- `FILTER_MASK_NAME` and `FILTER_ROTATION`: because some filter are used in the code, they must have a peculiar name. To ensure this name is the same throughout the code, they are stored there.
- `ACTIVITY_EXTRA_CALLER`: when adding extras to a StartActivity's Intent, we must used a string key to transfer information. To ensure this key is the same throughout the code, it is stored there.

#### 2.6 - Ui Package
Those menus are used in the bottom part of the UI, in the MainActivity and also the Stickers filter.

#### 2.6.1 - BottomMenu Class
Those menus are used in the bottom part of the UI, in the MainActivity and also the Stickers filter.


#### 2.6.2 - ColorTheme Class
This class is used to change the color and style of UI elements to reflect the global style. The Dark Mode can be disabled in the Settings to change the style of the app.

#### 2.6.3 - DisplayedFilter Class
A displayed filter is a filter paired with its visual representation.

#### 2.6.4 - ImageViewZoomScroll Class
This class is used to add new functionality to ImageView objects: the ability to zoom and scroll on the image. Zoom and scroll events are handle by the MainActivity class, this class is used to calculate which portion of the image should be displayed. The more we zoom, the smaller this surface. When we scroll, we are moving this surface around.

This surface is a rectangle defined by `newHeight`, `newWidth`, and `center`. If we want to change zoom level, we use the following line, we can use `setZoom(float zoom)` and we can move the image by using `translate(int x, int y)`.

Using those values, the Class created a transformation Matrix for the displayed image.

Another very useful function in imageViewTouchPointToBmpCoordinates which convert the pixel touched on the imageView to the coordinates of that pixel in image (regardless of zoom and center position).

#### 2.6.5 - ViewTools Class
This Class contains multiple tools useful with views such as the ability to know if a View is visible, or transform DP units into pixels.

### 3 - Filters Package
#### 3.1 - Filter Class

A Filter is an object that describes which input (colorSeekBar, seekBars, switches etc...) the user has access to. Each Filter instance could be created anywhere in the code, but we decided to do it outside MainActivity, which was already quite full.

At first, there is no link between a Filter instance and its corresponding FilterFunction. In order to create that connection, each Filter instance is given a new FilterInterface object. This interface is used to declare which FilterFunction should be called when applying the filter.

Here's an example to showcase how easily a new filter can be created. Please keep in mind that most filters doesn't use that many options, we have purposely used all of them: 


```java
// Create a filter with a name, a category and we can
// declare which sub-function is available to the user.
newFilter = new Filter("Name", Category.CATEGORY_NAME);
newFilter.allowMasking = false;     // true by default
newFilter.allowHistogram = false;   // true by default
// allowScrollZoom is true by default, must be false to use the coordinates of touch events.
newFilter.allowScrollZoom = false;  

// Now, let's define which interface to use and their parameters.
// If we don't call set..., this UI element wont be available.
newFilter.setColorSeekBar();
newFilter.setSeekBar1(seekBar1Min, seekBar1Current, seekBar1Max, "SeekBar 1 Label", "Unit");
newFilter.setSeekBar2(seekBar2Min, seekBar2Current, seekBar2Max, "SeekBar 2 Label", "Unit");
newFilter.setSwitch1(defaultBooleanValue, "Label if true", "Label if false");

// Then we can specify if changing the UI element automatically refresh the image.
newFilter.seekBar1AutoRefresh = false;
newFilter.seekBar2AutoRefresh = false;
newFilter.switch1AutoRefresh = false;

// We can now set two different function, the first one is the one used when using the FilterActivity
// (while tweaking the parameters). The second one is only called when the user click "Apply".
// Filters such as Crop is using this distinction.
newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
    @Override
    public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp, int selectedMenuItem, Filter filter) {
        FilterFunction.sobel(bmp, seekBar, switch1);
        return null;
    }
});

// If no apply function is provided, the preview function is called instead.
newFilter.setFilterApplyFunction(new FilterApplyInterface() {
    @Override
    public Bitmap apply(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp, int selectedMenuItem, Filter filter) {
        return maskBmp;
    }
});

// We can create any outside variable that could be used in between the Preview and Apply function.
// Those variables needs to be final.
```

#### 3.2 - FilterFunction Class
A filter function is a static method of this class. It will always takes in parameter a Bitmap (the image to modify). Most filters can also be tuned by some parameters. Lastly, those that use RenderScript will be given a Context in parameter.

`keepOrRemoveAColor` is the filter function for the Keep a color and Remove a color filters. It takes a target hue as a parameter. Then, for each pixel, a pixel turns progressively greyer depending on the distance in degrees between its hue and the target hue. In order the accelerate the process, a lookup table (abbreviated to LUT from now on) has been used. Other functions also use LUTs such as `linearContrastStretching`, `histogramEqualization`, and `hueShift`.

`gaussianBlur` was a difficult function to write. The Gaussian blur operation "can be applied to a two-dimensional image as two independent one-dimensional calculations" (taken from Wikipedia). Thanks to this property, we will be using a one-dimensional kernel. I chose to scale the sigma with the size of the kernel. That way, the Gaussian kernel with always "look the same'' but its resolution will increase with its size. In fact, the kernel will always have values between 1 and 90.

#### 3.3 - FilterFunctionDeprecated Class (Deprecated)
This class is the legacy versions of currently used filters. Functions that uses non-RS convolution are in this class.

#### 3.4 - FilterFunctionIntrinsic Class
This class is the Intrinsic versions of currently used filters. It has been left there to compare our implementations from Android's library's.

#### 3.5 - AppliedFilter Class
This Class was made to implement the history. It is essentially a Filter and all the parameters to apply it (the states of all seekBars, a mask, the state of the switch, the points touchUp and touchDown). It can be used as a recipe to exactly recreate the effect applied by the user. Then, the `apply` function allows us to apply this AppliedFilter to a bitmap.

#### 3.6 - FilterApply and FilterPreview Interfaces
This FilterApply interface is used to dynamically change the Filter instances apply method. Each filter will call a different FilterFunction static method and using this interface, it is possible for MainActivity to change which one to use.
The FilterPreview interface is used in the same way but targets the Preview method instead.

#### 3.7 - Category and BlendType Enums
Category is used to define where one Filter object should be displayed (as a Filter, a Tool, or a Filter). The Blending type defines how two images can be blend together using the applyTexture FilterFunction.

#### 3.8 - Tools Package

#### 3.8.1 - ColorTools Class (Deprecated)
This class implemented all the functions necessary for conversions between RGB and HSV. It is now deprecated as those conversions are done in RenderScript directly.

#### 3.8.2 - ConvolutionTools Class (Deprecated)
This class implemented tools used by any filter that uses convolution without RenderScript. It is now deprecated as all FilterFunctions now use RenderScript based convolution.

#### 3.8.3 - RenderScriptTools Class
The RenderScriptTools implements tools useful for functions that use RS. The applyConvolution3x3RS function applies any 3x3 kernel to any image. However, it uses ScriptIntrinsicConvolve3x3. The applyConvolution function uses our own RenderScript convolution and isn't limited to 3x3 kernel. Actually, the kernel can even be rectangular. The  cleanRenderScript function can be called after any RS function to destroy the Script, RenderScript, and a list of Allocation for input(s) and output(s).

### 4 - RenderScriptTools Class

#### 4.1 - FileInputOutput Class
This class is managing input and output. Loading files, loading resources, creating folders...

#### 4.2 - ImageTools Class
This Class contains useful tools to manipulate images, here are some of them. First to create a bitmap satisfying our expectations (modifying its size or not) with `cloneBitmap`, `createScaledBitmap` or `toSquare`. Then to create the histogram of the bitmap with `generateHistogram(Bitmap bmp)`. It contains also tools to draw on a bitmap `drawCircle(final Bitmap bmp, Point center, int radius, int color)` to create a mask and `drawRectangle(final Bitmap bmp, Point a, Point b, int color, int thickness)` to show the cropping area.


#### 4.3 - Point and PointPercentage Classes
The Point Class allows to create a two-dimensional point (two integers) and contains multiple methods to manipulate them : we can copy a point, translate it, and test if two points are equals. Points are mainly used for when the user is interacting with the screen, for example to draw the rectangle when cropping, we need the position of the finger. Then the PointPercentage Class is basically the same but instead of having two integers to make a point, we are having two floats because they are representing the coordinates on their respective axis in percent. PointPercentage is used to apply filters to the full size image. Indeed having the coordinate of the touched pixel in percentage allows to have this touched pixel for any size of the image.



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


**RS** means that the function uses RenderScript. **HSV** means it uses RGB->HSV->RGB convertions. The durations are in milliseconds. The last two columns are the ratio between 0.185 Mpx and 0.750 Mpx, and the ratio between 0.750 Mpx and 3.00 Mpx respectively. Those values would be 400\% if the performance scaled linearly. Also, it is clear that there is a constant time spent on initializing the filter, copying the image, displaying the result, which isn't always dependant on the image size. This is why the first ratio is almost always lower then the second one. In conclusion, at 0.750 Mpx on this phone, for most filters, there is little intrestess in lowering the internal image resolution.
The filters that use convolution kernels are expectantly slower than the rest. The **Average blur** filter is very slow at high kernel size. It is clear that the **Gaussian Blur** being a separable filter makes a huge difference in performance when compared with the **Average blur** filter. Also, the Add noise filter is particularly slow despite using RenderScript. This is because it’s generating up to three random numbers for each pixel. It would be much faster—but more complicated—to superpose a pre-fetched noisy layer on top of the image, simmilarly to what we do with the **Old analog** preset.

Our implementation of the Gaussian blur much slower than ScriptIntrinsicBlur. Because of this, Sobel and Laplacian are also significantly slower.

Furthermore, the images used in a photography app such as this one would probably be those taken by the phone. The Samsung A10 takes pictures with a resolution of 13 Mpx which would make virtually all the filter unusable in real-time. This is why have an interface that uses a smaller version of the images to priorities reactivity, and only applies the filters to the original non-resized image when saving.

## MEMORY USAGE
The following test has been performed on the same phone as before. In order to better highlight some behavior, we used a 3 Mpx image.

<img src="https://www.r-entries.com/etuliens/img/Litrato/memory.jpg" width="100%">

The program memory usage starts around 75 MB and after one minute of standby. When we load the 3 Mpx image, the memory consumption skyrocketed to about 201 MB. After a little while, the memory usage dropped to 107. The image internally is stored in RGBA-4444 which means that we use 4 bytes to store each pixel. 3 millions * 4 is equals to 12MB. In practice, loading this image resulted in about 3 times this amount. This can be explained because our program stores three copies of the image: the original image, the filtered image, and internally, the imageView stores another copy. When applying a filter (we choose Colorize), the value stay constant at about 300MB after Applying the filter and returning to the Main activity, the memory consumption stays at around 210MB. The second wave was using the Rotate tool. After all those operations, the program stays at 260MB.

The orange portion is the memory allocated to "Graphics". Strangely enough, this amount seems to never go down. We would like to remind the reader that our History doesn't save each step as a bitmap, but as the "recipe" to make recreate the image from the previous state.

Loading a new image reset this "Graphics" portion and also "Native", the blue portion bellow.

## BUGS AND LIMITATIONS

- Landscape mode is not available. We have created a layout\_land of Filter, but because of time contraint, we decided to focus on other, more important elements (History was one of them at the time).
- Convolutions do not correct the pixel values for the border of the image. As such, we can see black borders around the image.
- When rotating the image, we could have a Crop function that automatically keeps the largest rectangle that fit inside the image.
- When switching between Light and Dark mode, the icons in Tools aren't recolorize by the theme. It is difficult to say what is causing it. The problem appeared quite recently while creating the BottomMenu Class. As it isn't that much of a problem, we left it as it is for now.
- Images saved my our program seems to erase most EXIF meta-data.
- The EXIF value for flash activation seems to not follow the ExifInterface given by Android's library. Some phones and other cameras use seems to use many different values, some of which are not even categories by the library. This result in a lot of false positive by the EXIF Viewer.
- The Google API key has been made public on our GitHub after committing the file. It is highly discouraged by GitHub and Google Developer's Guide.
- MainActivity is sharing its AppContext in static manner with BottomMenu Class. This can lead to memory leaks according the Android Studio.
- We looked a little bit into AsyncTasks as it could greatly improve the speed of the app on older and newer phones.
- The app is really slow when first launched. From what we understood, this is because RenderScript is "compiling" / "caching" its functions.
- The app memory consumption seems way higher than its expected consumption. We have to look into that.

