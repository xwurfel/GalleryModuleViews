# Android Gallery Module

A highly configurable image and video gallery module for Android applications, built with Kotlin. 
This module provides a comprehensive solution for displaying and selecting media from the device storage and cloud services.

## Features

- **Flexible Selection**: Support for both single and multiple media selection
- **Viewing Options**: List view and grid view with configurable column count
- **Media Organization**: View all media items or categorize by albums
- **UI Customization**: Extensive customization for colors, fonts, icons, and more
- **Video Support**: Display duration for videos and autoplay options
- **Cloud Integration**: Support for Google Drive, Dropbox, and OneDrive
- **Search & Filter**: Find media by name, date, size, and media type
- **Zoom Support**: Pinch-to-zoom for images and videos
- **Material Design**: Modern UI following Material Design guidelines

## Installation

1. Copy and paste gallery_module into your project.
2. Add the following dependency to your `build.gradle` file:

```groovy
dependencies {
    implementation(project(":gallery_module"))
}
```

## Permissions

Add these permissions to your AndroidManifest.xml:

```xml
 <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

## Basic Usage

### Simple Gallery

```kotlin
GalleryBuilder.createSimpleGallery(this) { selectedItems ->
    // Handle selected media items
}.launch(this, R.id.fragment_container)
```

### Single Selection Gallery

```kotlin
GalleryBuilder.createSingleSelectionGallery(this) { selectedItem ->
    // Handle selected media item
}.launch(this, R.id.fragment_container)
```

### Custom Gallery

```kotlin
GalleryBuilder(context)
    .setSelectionMode(SelectionMode.MULTIPLE)
    .setMaxSelectionCount(10)
    .setDefaultViewMode(ViewMode.GRID)
    .setDefaultGridColumns(3)
    .setOnMediaSelected { selectedItems ->
        // Handle selected media items
    }
    .launch(activity, R.id.fragment_container)
```

## Advanced Customization

### Appearance

```kotlin
GalleryBuilder(context)
    .setBackgroundColor(R.color.background)
    .setSelectedIndicatorColor(R.color.colorAccent)
    .setAlbumTitleTextColor(R.color.textPrimary)
    .setAlbumSubtitleTextColor(R.color.textSecondary)
    .setThumbnailCornerRadius(R.dimen.corner_radius)
    .setTitleTypeface(typeface)
    .setSubtitleTypeface(subtitleTypeface)
    // ...more customizations
```

### Text

```kotlin
GalleryBuilder(context)
    .setTitleText(R.string.gallery_title)
    .setEmptyStateText(R.string.no_media)
    .setSelectionCounterText(R.string.selection_count)
    .setDoneButtonText(R.string.done)
```

### Video Options

```kotlin
GalleryBuilder(context)
    .setShowVideoDuration(true)
    .setAutoPlayVideos(true)
    .setMuteAutoPlayVideos(true)
    .setLoopAutoPlayVideos(false)
```

### Feature Toggles

```kotlin
GalleryBuilder(context)
    .setEnableZoom(true)
    .setEnableSearch(true)
    .setEnableFiltering(true)
    .setEnableCloudIntegration(true)
    .setCloudProviders(listOf(
        CloudProviderType.GOOGLE_DRIVE,
        CloudProviderType.DROPBOX
    ))
```

## Configuration Reference

| Method | Description |
|--------|-------------|
| `setSelectionMode(mode)` | Sets selection mode (SINGLE or MULTIPLE) |
| `setMaxSelectionCount(count)` | Maximum number of items that can be selected |
| `setDefaultViewMode(mode)` | Default view mode (GRID or LIST) |
| `setDefaultGridColumns(count)` | Number of columns in grid view (1-5) |
| `setAllowViewModeToggle(allow)` | Allow toggling between view modes |
| `setGroupByAlbum(group)` | Group media by albums |
| `setShowAlbumTitles(show)` | Show album titles |
| `setDefaultOpenAlbum(albumId)` | Open specific album by default |
| `setBackgroundColor(colorResId)` | Background color |
| `setSelectedIndicatorColor(colorResId)` | Selected item indicator color |
| ... | ... |

See `GalleryConfig` class for all available configuration options.

## Callbacks

```kotlin
GalleryBuilder(context)
    .setOnMediaSelected { items ->
        // Called when selection is confirmed
    }
    .setOnMediaClicked { item ->
        // Called when an item is clicked (long press)
    }
    .setOnBackPressed {
        // Called when back button is pressed
    }
```