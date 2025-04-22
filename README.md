# Android Gallery Module

A highly configurable image and video gallery module for Android applications, built with Kotlin.
This module provides a comprehensive solution for displaying and selecting media from the device storage and cloud services.

## Features

- **Flexible Selection**: Support for both single and multiple media selection
- **Viewing Options**: List view and grid view with configurable column count
- **Media Organization**: View all media items or categorize by albums
- **UI Customization**: Extensive customization for colors, fonts, icons, and more
- **Video Support**: Display duration for videos and autoplay options
- **Cloud Integration**: Support for Google Drive, with extensible architecture for custom providers
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

# Google Drive Integration Guide

This guide explains how to configure Google Drive integration into your app.

## Configuring Google Drive

To enable Google Drive integration in your app:

1. **Create a Google Cloud Project**:
    - Go to the [Google Cloud Console](https://console.cloud.google.com/)
    - Create a new project or select an existing one

2. **Enable the Google Drive API**:
    - Navigate to "APIs & Services" > "Library"
    - Search for "Google Drive API" and enable it

3. **Configure OAuth Consent Screen**:
    - Go to "APIs & Services" > "OAuth consent screen"
    - Set User Type to "External"
    - Fill in required app information (name, support email, developer contact)
    - Add required scopes: `https://www.googleapis.com/auth/drive.readonly` and `https://www.googleapis.com/auth/drive.photos.readonly`
    - Add your email as a test user

4. **Create OAuth Credentials**:
    - Go to "APIs & Services" > "Credentials"
    - Click "Create Credentials" > "OAuth client ID"
    - Select "Android" as the application type
    - Enter your app's package name
    - Add your app's SHA-1 signing certificate fingerprint
        - To get your SHA-1, run: `./gradlew signingReport` in your project directory

5. **Add Google Services to your project**:
    - Download the `google-services.json` file
    - Place it in your app's root directory
    - Add the Google Services plugin to your project-level build.gradle:
      ```groovy
      buildscript {
          dependencies {
              classpath 'com.google.gms:google-services:version'
          }
      }
      ```
    - Apply the plugin in your app-level build.gradle:
      ```groovy
      apply plugin: 'com.google.gms.google-services'
      ```

6. **Add required dependencies**:
    - Ensure these dependencies are in your app's build.gradle:
      ```groovy
      implementation 'com.google.android.gms:play-services-auth:version'
      implementation 'com.google.api-client:google-api-client-android:version'
      implementation 'com.google.apis:google-api-services-drive:version'
      ```

## Using Google Drive in Your App

```kotlin
GalleryBuilder(this)
    .setSelectionMode(SelectionMode.MULTIPLE)
    .setDefaultViewMode(ViewMode.GRID)
    .setEnableCloudIntegration(true)
    .addCloudProvider(CloudProviderType.GOOGLE_DRIVE, authenticate = true)
    .setOnMediaSelected { selectedItems ->
        // Handle selected media items
    }
    .launch(this, R.id.fragment_container)

// Handle authentication result
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (CloudMediaManager.handleAuthResult(requestCode, resultCode, data)) {
        // Authentication successful, relaunch gallery if needed
        GalleryBuilder.createCloudGallery(this) { selectedItems ->
            // Handle selected media items
        }.launch(this, R.id.fragment_container)
    }
}
```

## Google Drive Authentication Workflow

1. When you call `addCloudProvider(CloudProviderType.GOOGLE_DRIVE, authenticate = true)`, the module will check if you're already authenticated.
2. If not authenticated, it will launch the Google Sign-In UI.
3. The user will be prompted to sign in and grant permissions to their Google Drive.
4. The result is delivered to your Activity's `onActivityResult()` method.
5. Use `CloudMediaManager.handleAuthResult()` to process the authentication result.
6. If successful, you can relaunch the gallery to show Google Drive content.

## Troubleshooting

### Common Issues:

1. **"App not verified" warning**:
    - This is normal during development
    - Ensure you've added yourself as a test user
    - Click "Continue" to proceed during testing

2. **Authentication failures**:
    - Verify your SHA-1 fingerprint matches what's in Google Cloud Console
    - Check that your package name is correctly configured
    - Ensure you've enabled the correct scopes

3. **No files showing from Google Drive**:
    - Check if the user has granted the necessary permissions
    - Verify the user has media files in their Google Drive
    - Confirm that authentication was successful

## Extending with Custom Cloud Providers

The module is designed to be extensible. To implement your own provider:

1. Create a class that implements the `CloudRepository` interface
2. Register your provider using the `CloudProviderType.CUSTOM` type
3. Implement the necessary authentication and media retrieval methods

Example skeleton for a custom provider:

```kotlin
class CustomCloudProvider(private val context: Context) : BaseCloudRepository(), MediaRepository {
    override val providerType: CloudProviderType = CloudProviderType.CUSTOM
    
    override fun isAuthenticated(): Boolean {
        // Check if user is authenticated with your cloud service
    }
    
    // Other required methods
}

```

Provide that implementation into GalleryFragment's onCreate method:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    config = arguments?.getParcelable(ARG_CONFIG) ?: GalleryConfig()

    val permissionHandler = FragmentPermissionHandler(this, requestPermissionLauncher)
    val deviceRepository = DeviceMediaRepositoryImpl(requireContext(), permissionHandler)

    val mediaRepository =
        if (config.enableCloudIntegration && config.cloudProviders.isNotEmpty()) {
            val cloudRepos = config.cloudProviders.associateWith { providerType ->
                when (providerType) {
                    CloudProviderType.GOOGLE_DRIVE -> GoogleDriveRepository(requireContext())
                    // change to your implementation
                    CloudProviderType.CUSTOM -> throw NotImplementedError("Please provide implementation for $providerType")
                }
            }
            CompositeMediaRepository(deviceRepository, cloudRepos)
        } else {
            deviceRepository
        }

    val factory = GalleryViewModel.Factory(mediaRepository, config)
    viewModel = ViewModelProvider(this, factory)[GalleryViewModel::class.java]

    lifecycle.addObserver(lifecycleObserver)
}
```

Then register your provider:

```kotlin
GalleryBuilder(context)
    .setEnableCloudIntegration(true)
    .addCloudProvider(CloudProviderType.CUSTOM)
    .launch(activity, containerId)
```