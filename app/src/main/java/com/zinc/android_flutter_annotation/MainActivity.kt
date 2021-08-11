package com.zinc.android_flutter_annotation

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zinc.android_flutter_annotation.channel.sender.basic.NezaBasicChannelImpl
import com.joyy.neza_api.config.FlutterConfig
import com.zinc.android_flutter_annotation.neza.Flutter
import com.zinc.android_flutter_annotation.utils.AssetsUtils
import io.flutter.embedding.android.FlutterActivity

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/9 6:38 下午
 * @email: 56002982@qq.com
 * @des:
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_goto_flutter_page).setOnClickListener {
            openFlutterPage()
        }

        findViewById<Button>(R.id.btn_say_hello_to_flutter).setOnClickListener {
//            NezaMethodChannelImpl.sayHelloToFlutter()
            Flutter.Channels.nezaMethodChannel.sayHelloToFlutter()
        }

        findViewById<Button>(R.id.btn_send_image_info).setOnClickListener {
            val byteArray = AssetsUtils.getAssetsFile(resources, "sample.png")
//            NezaEventChannelImpl.instance.sendImageInfo(error())
            Flutter.Channels.nezaEventChannel.sendImageInfo(byteArray)
        }

        findViewById<Button>(R.id.btn_send_json_to_flutter).setOnClickListener {
            NezaBasicChannelImpl.sendJsonToFlutter("{\"name\":\"江澎涌\", \"age\":28}")
        }
    }

    private fun openFlutterPage() {
        startActivity(
            FlutterActivity
                .withCachedEngine(FlutterConfig.ENGINE_ID)
                ?.build(this)
        )
    }
}