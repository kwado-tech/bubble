import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class Bubble {
  static const MethodChannel _channel =
      const MethodChannel('com.dsaved.bubble.head');

  bool shouldBounce;
  bool showCloseButton;
  bool allowDragToClose;

  Bubble({
    this.shouldBounce = true,
    this.allowDragToClose = true,
    this.showCloseButton = false,
  });

  /// puts app in background and shows floaty-bubble head
  Future<void> startBubbleHead() async {
    ByteData bytes = await rootBundle.load('assets/images/icon.png');
    var buffer = bytes.buffer;
    var encodedImage = base64.encode(Uint8List.view(buffer));
    await _channel.invokeMethod('startBubbleHead', {
      "image": encodedImage,
      "bounce": shouldBounce,
      "showClose": showCloseButton,
      "dragToClose": allowDragToClose,
    });
  }

  /// closes floaty-bubble head
  Future<void> stopBubbleHead() async {
    await _channel.invokeMethod('stopBubbleHead');
  }
}
