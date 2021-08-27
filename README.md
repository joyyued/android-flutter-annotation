# Android-Flutter-Annotation

用于 Android 端项目，通过使用注解自动生成与 Flutter 通信的代码。

可生成的三种通信渠道有:

* MethodChannel
* EventChannel
* BasicMessageChannel

## 集成

在项目的 `build.gradle` 添加 jitpack 仓库的依赖

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

在 module 的 `build.gradle` 中添加

```gradle
apply plugin: 'kotlin-kapt'

dependencies {
    implementation "com.github.joyyued.android-flutter-annotation:annotation:1.0.0"
    kapt "com.github.joyyued.android-flutter-annotation:compiler:1.0.0"
}
```

## 使用

### Flutter 引擎

用于标记 Flutter 引擎的 id，会生成创建 Flutter 引擎 id 的代。并注解生成的 channel 会默认使用该 engineId。

```kotlin
@FlutterEngine(engineId = "引擎 id")
class MyFlutterEngine {}
```

使用此注解之后，可以进行以下操作

```kotlin
// 引擎初始化
Flutter.init(context)

// 引擎释放
Flutter.release()

// 获取默认 flutter 引擎
Flutter.Engine.DEFAULT_ENGINE

// engineId 可以不传，默认则为注解的 engineId
Flutter.Engine.getEngine("引擎 id")
```

### MethodChannel

由 MethodChannel 特征决定了，可以通过该渠道在 Native 接收来自 Flutter 的信息，也可以在 Native 发送信息给 Flutter，使用中请注意 **接收** 和 **发送** 的类型。

#### @FlutterMethodChannel

用于生成 Android 端中，与 Flutter 通信的 MethodChannel 代码

**接收者 [Flutter -> Native]**

```kotlin
@FlutterMethodChannel(
    type = ChannelType.RECEIVER,
    channelName = ChannelConfig.METHOD_CHANNEL
)
class NezaMethodChannel {

    @HandleMessage
    fun sayHelloToNative() {
        sayHello()
    }

    fun sayHello() {
        Log.e("NezaMethodChannel", "[Flutter -> Native] sayHelloToNative")
    }

}
```

在需要接收信息的方法加上 `@HandleMessage` 注解，则当 Flutter 端调用渠道名称为 `ChannelConfig.METHOD_CHANNEL` 的渠道，并且方法为 `sayHelloToNative` 时，则框架则会调用至该方法。

**发送者 [Native -> Flutter]**

```kotlin
@FlutterMethodChannel(
    type = ChannelType.SENDER,
    channelName = ChannelConfig.METHOD_CHANNEL
)
interface NezaMethodChannel {
    fun sayHelloToFlutter()
}
```

使用此注解后，可以通过以下代码进行发送信息给 Flutter，发送的方法名为所定义的方法名称（例子中则为 `sayHelloToFlutter`）

```kotlin
Flutter.channels?.nezaMethodChannel?.sayHelloToFlutter()

// 如果需要返回值，则只需要将其包裹至协程，进行同步即可
CoroutineScope(Dispatchers.Main).launch {
    val result = Flutter.channels
        ?.nezaMethodChannel
        ?.sayHelloToFlutter()
        ?.await()
        // do something
}
```

协程返回的内容结构如下，可以根据 `resultType` 进行区分结果类型，再获取相应的数据。

```kotlin
data class MethodChannelResult(
    val resultType: MethodChannelResultType,
    val successResult: SuccessResult? = null,
    val errorResult: ErrorResult? = null,
)
```

#### @ParseData

用于在接收者中，框架会以方法的参数名作为 key 值获取从 Flutter 传过来的 value，如下代码所示

```kotlin
@FlutterMethodChannel(
    type = ChannelType.RECEIVER,
    channelName = Config.METHOD_CHANNEL
)
class NezaMethodChannel {
    @ParseData
    fun sayHelloToNativeWithParam(name: String?, age: Int?) {
        // do something
    }
}
```

- 使用 `@ParseData` 注解的参数类型都需要为可空，即需要为 `String?` ,不能为 `String`
- 不使用 `@ParseData` 注解，则参数个数必须为 0 或 1 个，如果 1 个参数，该参数类型必须为 `Any`

#### @MessageHandler

用于接收者，被标记的方法会接收 Flutter 传递过来的同渠道同方法名的信息

- 可以与 `@ParseData` 同时使用，此时框架会尝试着进行解析参数，但参数类型必须为可空，即需要为 `String?`，不能为 `String`
- 如果只是单独使用，则参数个数必须为 0 或 1 个，如果 1 个参数，该参数类型必须为 `Any`

#### @Callback

用于标记回调的参数，在 Method Channel 中，必须为 `io.flutter.plugin.common.MethodChannel.Result` 类型的属性。

框架会在每次接收到 Flutter 传递的信息时，将回调设置到该参数，方法中可以进行逻辑处理后传递返回值给 Flutter 端

代码使用如下

```kotlin
@FlutterMethodChannel(
    type = ChannelType.RECEIVER,
    channelName = Config.METHOD_CHANNEL
)
class NezaMethodChannel {

    @HandleMessage
    fun sayHelloToNativeWithRaw(
        map: Any,
        @Callback result: MethodChannel.Result
    ) {
        //  your logic

        // success
        result.success("")

        // error
        result.error("code", "message", "detail")

        // notImplemented
        result.notImplemented()
    }

}
```

### EventChannel

由于 EventChannel 特征决定了，只能单向从 Native 发送信息给 Flutter，所以相较于 MethodChannel 渠道，只有发送者。

#### @FlutterEventChannel

用于标记需要生成 Flutter Event Method 通信的代码

```kotlin
@FlutterEventChannel(channelName = ChannelConfig.EVENT_CHANNEL)
interface NezaEventChannel {
    @Param
    fun sendImageInfo(byteArray: ByteArray)
}
```

可以通过以下代码进行调用

```kotlin
Flutter.channels?.nezaEventChannel?.sendImageInfo(byteArray)
```

### BasicMessageChannel

由于 BasicMessageChannel 特征决定了，可以通过该渠道在 Native 接收来自 Flutter 的信息，也可以在 Native 发送信息给 Flutter，使用中请注意 **接收** 和 **发送** 的类型。

同时还需要一个 解析器（注解中的 codeccClass）进行对数据的解析，谷歌官方提供了四种解析器：

1. io.flutter.plugin.common.BinaryCodec
2. io.flutter.plugin.common.JSONMessageCodec
3. io.flutter.plugin.common.StandardMessageCodec
4. io.flutter.plugin.common.StringCodec

当然你也可以进行自定义适合自己的解析器，但值得注意的是，你必须像官方代码一样提供一个 `INSTANCE` 的静态公开的实例属性。

#### @FlutterBasicChannel

用于生成 Android 端中，与 Flutter 通信的 Basic Message Channel 代码

**接收者 [Flutter -> Native]**

```kotlin
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = ChannelConfig.STANDER_BASIC_CHANNEL,
    type = ChannelType.RECEIVER
)
object NezaBasicChannel {
    @MessageHandler
    fun receiverJsonFromFlutter(json: String) {
        Log.e("NezaBasicChannel", "[Flutter -> Native]$json")
    }
}
```

当 Flutter 端调用渠道名称为 `ChannelConfig.STANDER_BASIC_CHANNEL` 的渠道时，框架则会调用标记了 `@MessageHandler` 注解的方法。

**发送者 [Native -> Flutter]**

```kotlin
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = ChannelConfig.EVENT_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaBasicChannel {
    @Param
    fun sendJsonToFlutter(json: String)
}
```

可以通过以下代码进行发送信息给 Flutter

```kotlin
Flutter.channels?.nezaStringBasicChannel?.sendJsonToFlutter("{\"name\":\"江澎涌\"}")

// 如果需要返回值，则只需要将其包裹至协程，进行同步即可
CoroutineScope(Dispatchers.Main).launch {
    Flutter.channels
        ?.nezaStringBasicChannel
        ?.sendJsonToFlutter("{\"name\":\"江澎涌\", \"age\":28}")
        ?.await()

    // do something
}
```

#### @MessageHandler

用于在接收者中，标记需要处理接收到信息的方法，切记标记的方法参数类型要和 codec 的泛型一致

- 如果存在多个 `@MessageHandler` ，则只有最后一个方法会被调用
- 如果没有 `@MessageHandler`，则没有回调触发

#### @Callback

在接收者中，用于标记回调 Flutter 的参数，属性的类型必须为 `io.flutter.plugin.common.BasicMessageChannel.Reply<T>`

框架会在每次接收到 Flutter 传递的信息时，将回调设置到该参数，方法中可以进行逻辑处理后传递返回值给 Flutter 端

代码使用如下

```kotlin
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = Config.STRING_BASIC_CHANNEL,
    type = ChannelType.RECEIVER
)
class NezaStringBasicChannel {

    @HandleMessage
    fun receiverJsonFromFlutter(
        json: String?,
        @Callback reply: BasicMessageChannel.Reply<String>
    ) {
        Log.e("NezaBasicChannel", "[Flutter -> Native] $json")
        reply.reply("Reply something......")
    }

}
```

### 添加参数

三种 channel 发送者的方法都可以携带参数，可以使用 `@Param` 或 `@ParamMap` 对方法标注

* `@Param`: 代表只使用第一个参数的 值 和 类型作为传递值和传递类型。
* `@ParamMap`: 代表会将参数组织为一个 HashMap 进行传递，参数名称为 key，参数值为 value。

**值得注意：**

1. 同时使用两个注解，框架会抛出错误
2. 不使用注解，则框架会通过参数个数进行默认行为：1 个参数则以标注了 `@Param` 处理，其余情况以标注了 `@ParamMap` 处理
3. 当多参数的方法，使用了 `@Param` 注解，框架不会报错，内部逻辑只会使用第一个参数作为传递参数，传递类型也是第一个参数类型，其余的参数将形同虚设。

## LICENSE

[MIT](LICENSE)
