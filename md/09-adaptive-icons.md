# Adaptive Icons (Android 8.0+)

## Structure

```
Adaptive icons consist of two layers:
- Background: 108dp x 108dp
- Foreground: 108dp x 108dp

The system applies a mask (circle, square, rounded square, etc.)
to create the final icon shape.

Safe zone: Inner 72dp x 72dp (outer 18dp may be cropped)
```

## File Structure

```
res/
├── drawable/
│   ├── ic_launcher_foreground.xml  (Vector drawable)
│   └── ic_launcher_background.xml  (Vector or solid color)
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml             (Adaptive icon definition)
│   └── ic_launcher_round.xml       (Round variant)
├── values/
│   └── ic_launcher_background.xml  (Optional: background color)
```

## Adaptive Icon Definition

```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

## Vector Foreground (Emoji Example)

```xml
<!-- res/drawable/ic_launcher_foreground.xml -->
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Yellow circle (face) -->
    <path
        android:fillColor="#FFDD67"
        android:pathData="M54,22
            C71.67,22 86,36.33 86,54
            C86,71.67 71.67,86 54,86
            C36.33,86 22,71.67 22,54
            C22,36.33 36.33,22 54,22Z"/>

    <!-- Left eye -->
    <path
        android:fillColor="#664E27"
        android:pathData="M40,48
            C40,44.69 42.69,42 46,42
            C49.31,42 52,44.69 52,48
            C52,51.31 49.31,54 46,54
            C42.69,54 40,51.31 40,48Z"/>

    <!-- Right eye -->
    <path
        android:fillColor="#664E27"
        android:pathData="M56,48
            C56,44.69 58.69,42 62,42
            C65.31,42 68,44.69 68,48
            C68,51.31 65.31,54 62,54
            C58.69,54 56,51.31 56,48Z"/>

    <!-- Smile -->
    <path
        android:fillColor="#664E27"
        android:pathData="M38,58
            Q54,76 70,58"
        android:strokeWidth="3"
        android:strokeColor="#664E27"
        android:fillColor="@android:color/transparent"/>
</vector>
```

## Solid Color Background

```xml
<!-- res/drawable/ic_launcher_background.xml -->
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFECB3"
        android:pathData="M0,0h108v108H0z"/>
</vector>
```

## Alternative: Color Resource Background

```xml
<!-- res/values/ic_launcher_background.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#FFECB3</color>
</resources>

<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

## Splash Screen (Android 12+)

```xml
<!-- res/values-v31/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.App.Splash" parent="Theme.SplashScreen">
        <!-- Background color -->
        <item name="windowSplashScreenBackground">#FFECB3</item>

        <!-- Animated or static icon -->
        <item name="windowSplashScreenAnimatedIcon">
            @drawable/ic_launcher_foreground
        </item>

        <!-- Optional: Icon background circle color -->
        <item name="windowSplashScreenIconBackgroundColor">#FFDD67</item>

        <!-- Theme to switch to after splash -->
        <item name="postSplashScreenTheme">@style/Theme.FaceMoji</item>
    </style>
</resources>
```

## AndroidManifest.xml Setup

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:theme="@style/Theme.App.Splash">
    <!-- Activities -->
</application>
```

## MainActivity Splash Handling

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen (before super.onCreate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)
        // ...
    }
}
```

## Vector Path Basics

```xml
<!-- Path commands -->
M x,y    - Move to
L x,y    - Line to
H x      - Horizontal line to
V y      - Vertical line to
C x1,y1 x2,y2 x,y  - Cubic bezier
Q x1,y1 x,y        - Quadratic bezier
A rx,ry rotation large-arc sweep x,y  - Arc
Z        - Close path

<!-- Circle shorthand using arc -->
<path android:pathData="
    M centerX-radius,centerY
    A radius,radius 0 1,1 centerX+radius,centerY
    A radius,radius 0 1,1 centerX-radius,centerY
    Z"/>

<!-- Rectangle -->
<path android:pathData="M left,top h width v height h -width Z"/>
```

## Design Guidelines

```
1. Keep important content in the inner 72dp
2. Don't put text in icons (gets too small)
3. Use simple shapes that scale well
4. Test on multiple launcher shapes:
   - Circle
   - Rounded square
   - Squircle
   - Teardrop

5. Layer order:
   - Background: flat color or subtle pattern
   - Foreground: main icon content

6. Colors:
   - Use brand colors
   - Ensure contrast between layers
   - Avoid pure white (can disappear on light themes)
```

## Testing Icons

```bash
# Generate icon previews
# Android Studio: File > New > Image Asset

# View on device with different launchers:
# - Pixel Launcher (circle)
# - Samsung One UI (rounded square)
# - Nova Launcher (customizable)
```

## Legacy Icon Fallbacks

```
For Android 7.1 and below, provide PNG icons:
res/
├── mipmap-mdpi/ic_launcher.png      (48x48)
├── mipmap-hdpi/ic_launcher.png      (72x72)
├── mipmap-xhdpi/ic_launcher.png     (96x96)
├── mipmap-xxhdpi/ic_launcher.png    (144x144)
├── mipmap-xxxhdpi/ic_launcher.png   (192x192)

These are only used if adaptive icon is not supported.
The mipmap-anydpi-v26 takes precedence on Android 8.0+.
```
