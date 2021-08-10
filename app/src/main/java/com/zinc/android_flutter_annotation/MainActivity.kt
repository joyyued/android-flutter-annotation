package com.zinc.android_flutter_annotation

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zinc.android_flutter_annotation.channel.receiver.basic.NezaBasicChannelProxy
import com.zinc.android_flutter_annotation.channel.sender.basic.NezaBasicChannelImpl
import com.zinc.android_flutter_annotation.channel.sender.event.NezaEventChannelImpl
import com.zinc.android_flutter_annotation.channel.sender.method.NezaMethodChannelImpl
import com.zinc.android_flutter_annotation.neza.config.FlutterConfig
import com.zinc.android_flutter_annotation.utils.AssetsUtils
import io.flutter.embedding.android.FlutterActivity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

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
            NezaMethodChannelImpl.sayHelloToFlutter()
        }

        findViewById<Button>(R.id.btn_send_image_info).setOnClickListener {
            val byteArray = AssetsUtils.getAssetsFile(resources, "sample.png")
            NezaEventChannelImpl.instance.sendImageInfo(byteArray)
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