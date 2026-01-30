# Touch Handling in Compose

## Basic Click

```kotlin
Button(onClick = { /* action */ }) {
    Text("Click me")
}

// Or on any composable
Box(
    modifier = Modifier.clickable { /* action */ }
) { }
```

## Tap Gestures

```kotlin
Box(
    modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onTap = { offset ->
                // offset.x, offset.y in local coordinates
            },
            onLongPress = { offset ->
                // Long press detected
            },
            onDoubleTap = { offset ->
                // Double tap detected
            }
        )
    }
)
```

## Combined Tap and Long Press

```kotlin
@Composable
fun TapAndHoldItem(
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        // Content
    }
}
```

## Coordinate Mapping (View to Bitmap)

```kotlin
// When displaying scaled image and need to map tap to original coordinates
fun findItemAtPosition(
    tapX: Float,        // Tap X in view coordinates
    tapY: Float,        // Tap Y in view coordinates
    viewWidth: Float,   // View container width
    viewHeight: Float,  // View container height
    bitmapWidth: Int,   // Original bitmap width
    bitmapHeight: Int,  // Original bitmap height
    items: List<Item>   // Items with bitmap coordinates
): Int? {
    // Calculate scale (ContentScale.Fit)
    val scaleX = viewWidth / bitmapWidth
    val scaleY = viewHeight / bitmapHeight
    val scale = minOf(scaleX, scaleY)

    // Calculate offset (centering)
    val scaledWidth = bitmapWidth * scale
    val scaledHeight = bitmapHeight * scale
    val offsetX = (viewWidth - scaledWidth) / 2f
    val offsetY = (viewHeight - scaledHeight) / 2f

    // Convert tap to bitmap coordinates
    val bitmapX = ((tapX - offsetX) / scale).toInt()
    val bitmapY = ((tapY - offsetY) / scale).toInt()

    // Check bounds
    if (bitmapX < 0 || bitmapX >= bitmapWidth ||
        bitmapY < 0 || bitmapY >= bitmapHeight) {
        return null
    }

    // Find item at position
    return items.indexOfFirst { item ->
        item.boundingBox.contains(bitmapX, bitmapY)
    }.takeIf { it >= 0 }
}
```

## Using in Image Preview

```kotlin
@Composable
fun InteractiveImagePreview(
    bitmap: Bitmap,
    items: List<DetectedItem>,
    onItemTapped: (Int) -> Unit
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap.width.toFloat() / bitmap.height)
            .onSizeChanged { viewSize = it }
            .pointerInput(bitmap, items) {
                detectTapGestures { offset ->
                    val index = findItemAtPosition(
                        tapX = offset.x,
                        tapY = offset.y,
                        viewWidth = viewSize.width.toFloat(),
                        viewHeight = viewSize.height.toFloat(),
                        bitmapWidth = bitmap.width,
                        bitmapHeight = bitmap.height,
                        items = items
                    )
                    index?.let { onItemTapped(it) }
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
```

## pointerInput Key Parameter

```kotlin
// Key determines when gesture detector resets
// Use Unit for stable detector
.pointerInput(Unit) { }

// Use changing value to reset when data changes
.pointerInput(bitmap, faces) { }

// Multiple keys
.pointerInput(key1, key2) { }
```

## Drag Gestures

```kotlin
var offsetX by remember { mutableStateOf(0f) }
var offsetY by remember { mutableStateOf(0f) }

Box(
    modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
)
```

## Transform Gestures (Zoom/Rotate)

```kotlin
var scale by remember { mutableStateOf(1f) }
var rotation by remember { mutableStateOf(0f) }
var offset by remember { mutableStateOf(Offset.Zero) }

Box(
    modifier = Modifier
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            rotationZ = rotation,
            translationX = offset.x,
            translationY = offset.y
        )
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, rotate ->
                scale *= zoom
                rotation += rotate
                offset += pan
            }
        }
)
```

## Ripple Effect

```kotlin
// Default ripple
Modifier.clickable { }

// Custom ripple
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = rememberRipple(
        bounded = true,
        radius = 24.dp,
        color = Color.Red
    )
) { }

// No ripple
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null
) { }
```

## Toggle Pattern

```kotlin
// ViewModel
private val _selectedIndices = MutableStateFlow<Set<Int>>(emptySet())

fun toggleItem(index: Int) {
    _selectedIndices.update { current ->
        if (index in current) {
            current - index  // Remove
        } else {
            current + index  // Add
        }
    }
}

// UI
items.forEachIndexed { index, item ->
    val isSelected = index in selectedIndices
    ItemView(
        item = item,
        isSelected = isSelected,
        onClick = { viewModel.toggleItem(index) }
    )
}
```
