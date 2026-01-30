# Custom Dialogs in Compose

## Basic Dialog

```kotlin
@Composable
fun SimpleDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dialog Title")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dialog content goes here")
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
```

## Controlled Dialog Pattern

```kotlin
// In ViewModel
data class AppState(
    val showDialog: Boolean = false,
    val dialogData: String? = null
)

fun showDialog(data: String) {
    _state.value = _state.value.copy(
        showDialog = true,
        dialogData = data
    )
}

fun hideDialog() {
    _state.value = _state.value.copy(
        showDialog = false,
        dialogData = null
    )
}

// In Composable
if (state.showDialog) {
    MyDialog(
        data = state.dialogData,
        onDismiss = { viewModel.hideDialog() }
    )
}
```

## Emoji Picker Dialog

```kotlin
@Composable
fun EmojiPickerDialog(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Emoji",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8F8F8))
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Red)
                }
            }
        }
    }
}
```

## Confirmation Dialog

```kotlin
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

## Dialog with Input

```kotlin
@Composable
fun InputDialog(
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enter Value", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Value") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = { onSubmit(text) },
                        enabled = text.isNotBlank()
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}
```

## Dialog Properties

```kotlin
Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
        dismissOnBackPress = true,      // Dismiss when back pressed
        dismissOnClickOutside = true,   // Dismiss when clicking outside
        usePlatformDefaultWidth = false // Control width manually
    )
) {
    // Content with custom width
    Card(modifier = Modifier.fillMaxWidth(0.9f)) { }
}
```

## Bottom Sheet Dialog

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetExample() {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Bottom Sheet Content")
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
```

## Dialog with State for Editing Specific Item

```kotlin
// ViewModel
data class State(
    val showPicker: Boolean = false,
    val editingIndex: Int? = null  // Track which item is being edited
)

fun openPickerForItem(index: Int) {
    _state.value = _state.value.copy(
        showPicker = true,
        editingIndex = index
    )
}

fun selectItem(item: String) {
    val index = _state.value.editingIndex ?: return
    val items = _state.value.items.toMutableList()
    items[index] = item
    _state.value = _state.value.copy(
        items = items,
        showPicker = false,
        editingIndex = null
    )
}

fun closePicker() {
    _state.value = _state.value.copy(
        showPicker = false,
        editingIndex = null
    )
}

// UI
items.forEachIndexed { index, item ->
    ItemView(
        item = item,
        onLongClick = { viewModel.openPickerForItem(index) }
    )
}

if (state.showPicker) {
    PickerDialog(
        onItemSelected = { viewModel.selectItem(it) },
        onDismiss = { viewModel.closePicker() }
    )
}
```

## Loading Dialog

```kotlin
@Composable
fun LoadingDialog() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(16.dp))
                Text("Loading...")
            }
        }
    }
}
```
