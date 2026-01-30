# Jetpack Compose UI Patterns

## Basic Screen Structure

```kotlin
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Content
    }
}
```

## State Collection

```kotlin
// In Composable
val state by viewModel.state.collectAsState()

// Access state properties
state.isLoading
state.data
state.errorMessage
```

## Material 3 Components

```kotlin
// Card
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Content
    }
}

// Button
Button(
    onClick = { /* action */ },
    modifier = Modifier.height(56.dp),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
    enabled = state.isEnabled
) {
    Text("Click me")
}

// Text Button
TextButton(onClick = { /* action */ }) {
    Text("Cancel", color = Color.Red)
}

// Circular Progress
CircularProgressIndicator(color = TealColor)
```

## Layout Patterns

```kotlin
// Column - vertical stack
Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
) { }

// Row - horizontal stack
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
) { }

// Box - overlapping/positioning
Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) { }

// Weight distribution
Row {
    Button(modifier = Modifier.weight(1f)) { }
    Spacer(modifier = Modifier.width(16.dp))
    Button(modifier = Modifier.weight(1f)) { }
}
```

## LazyVerticalGrid

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),  // or GridCells.Adaptive(minSize = 64.dp)
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.height(200.dp)
) {
    items(itemList) { item ->
        ItemComposable(item = item)
    }
}
```

## Image Display

```kotlin
// From Bitmap
Image(
    bitmap = bitmap.asImageBitmap(),
    contentDescription = "Photo",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Fit
)

// ContentScale options
ContentScale.Fit       // Scale to fit, maintain aspect
ContentScale.Crop      // Scale to fill, crop excess
ContentScale.FillBounds // Stretch to fill
ContentScale.Inside    // Scale down only if needed
```

## Modifier Chains

```kotlin
Modifier
    .fillMaxWidth()
    .height(56.dp)
    .padding(16.dp)
    .clip(RoundedCornerShape(12.dp))
    .background(Color.White)
    .border(2.dp, Color.Red, RoundedCornerShape(12.dp))
    .clickable { onClick() }
```

## Conditional UI

```kotlin
// if-else in Compose
when {
    state.isLoading -> LoadingView()
    state.error != null -> ErrorView(state.error)
    state.data != null -> ContentView(state.data)
    else -> EmptyView()
}

// Conditional modifier
Modifier
    .background(if (isSelected) Color.Red else Color.Gray)
    .border(
        width = if (isSelected) 2.dp else 0.dp,
        color = if (isSelected) Color.Red else Color.Transparent
    )
```

## Side Effects

```kotlin
// Run once on first composition
LaunchedEffect(Unit) {
    viewModel.loadData()
}

// Run when key changes
LaunchedEffect(state.userId) {
    viewModel.loadUserData(state.userId)
}

// Observe state changes
LaunchedEffect(state.saveSuccess) {
    if (state.saveSuccess) {
        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
        viewModel.clearSaveSuccess()
    }
}
```

## Remember State

```kotlin
// Simple state
var isExpanded by remember { mutableStateOf(false) }

// Derived from other state
val displayText by remember(state.count) {
    derivedStateOf { "Count: ${state.count}" }
}

// Survive rotation (in ViewModel instead)
// Don't use rememberSaveable for complex objects
```

## Context Access

```kotlin
@Composable
fun MyComposable() {
    val context = LocalContext.current

    Button(onClick = {
        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
    }) {
        Text("Show Toast")
    }
}
```

## Theming

```kotlin
// Define colors
val TealColor = Color(0xFF009688)
val CoralColor = Color(0xFFEF5350)

// Use Material theme
MaterialTheme(
    colorScheme = lightColorScheme(
        primary = TealColor,
        secondary = CoralColor,
        background = Color(0xFFF5F5F5)
    )
) {
    // Content
}

// Access theme colors
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onPrimary
```

## Scaffold Structure

```kotlin
Scaffold(
    topBar = { TopAppBar(title = { Text("Title") }) },
    bottomBar = { BottomNavigation { } },
    floatingActionButton = {
        FloatingActionButton(onClick = { }) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
) { paddingValues ->
    Content(modifier = Modifier.padding(paddingValues))
}
```
