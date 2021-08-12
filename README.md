# bubble


A flutter plugin to enable you launch a bubble while putting your application to background and upon clicking the bubble brings your application back to foreground
$~~~~~~~~~~~$

## Getting Started
### Add dependency

```yaml
    dependencies: 
        float_bubble_head: ^0.0.1
```
$~~~~~~~~~~~$

### Add in android-manifest file (**../main/AndroidManifest.xml**)

If you are unsure on where to do this, you can reference the example project AndroidManifest.xml file [here](example/android/app/src/main/AndroidManifest.xml)
$~~~~~~~~~~~$

Add `SYSTEM_ALERT_WINDOW` permission in manifest
```xml
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```

NOTE: For best UX practices, you should request for `SYSTEM_ALERT_WINDOW` permission on your application launch (if permission `status` is not granted)
To request for permission, we advise the use of this [package](https://pub.dev/packages/permission_handler)


Add `intent-filter` in activity tag

```xml
    <intent-filter>
        <action android:name="intent.bring.app.to.foreground" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
```

Add `intent-filter` in application tag
```xml
    <service
        android:name="com.dsaved.bubblehead.bubble.BubbleHeadService"
        android:enabled="true"
        android:exported="false"/>
```
$~~~~~~~~~~~$

### Note:  To set bubble icon, create `assets/images` folder path and add your png icon with name `icon.png` to the directory (ensure to import assets in your `pubspec.yaml` file)
$~~~~~~~~~~~$
### Examples

**To start bubble**
[This puts your app in background and can be re-launched (brought to foreground) on tap of the bubble]

```dart
    Bubble _bubble = new Bubble();

    Future<void> startBubbleHead() async {
    
        try {
            await _bubble.startBubbleHead();
        } on PlatformException {
            print('Failed to call startBubbleHead');
        }
    }
```

**To stop bubble**

```dart
    Bubble _bubble = new Bubble();

    Future<void> stopBubbleHead() async {
    
        try {
            await _bubble.stopBubbleHead();
        } on PlatformException {
            print('Failed to call stopBubbleHead');
        }
    }
```

**Other parameters**
(You can choose to tweak **optional** parameters when initializing bubble)


```dart
    Bubble({
        this.shouldBounce = true,
        this.allowDragToClose = true,
        this.showCloseButton = false,
    });
```

**Parameter Definition**
- shouldBounce - Defaults to `True`
(Adds animation to bubble-head)
- allowDragToClose - Defaults to `True`
(Enables dragging bubble to bottom screen to exit)
- showCloseButton - Defaults to `False`
(Adds a close button icon to the bubble-head)
  



