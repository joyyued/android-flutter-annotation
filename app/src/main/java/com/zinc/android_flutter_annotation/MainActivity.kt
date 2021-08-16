package com.zinc.android_flutter_annotation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.joyy.neza.manager.Flutter
import com.zinc.android_flutter_annotation.config.Config
import com.zinc.android_flutter_annotation.utils.AssetsUtils
import io.flutter.embedding.android.FlutterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            Flutter.Channels.nezaMethodChannel.sayHelloToFlutter()
        }

        findViewById<Button>(R.id.btn_say_hello_to_flutter_with_callback).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val params = HashMap<String, Any?>()
                val result = Flutter.Channels.nezaMethodChannel
                    .sayHelloToFlutterWithCallback(params)
                Log.i("Neza", "method channel callback: $result")
            }

        }

        findViewById<Button>(R.id.btn_send_image_info).setOnClickListener {
            val byteArray = AssetsUtils.getAssetsFile(resources, "sample.png")
            Flutter.Channels.nezaEventChannel.sendImageInfo(byteArray)
        }

        findViewById<Button>(R.id.btn_send_json_to_flutter).setOnClickListener {
            Flutter.Channels.nezaStringBasicChannel.sendJsonToFlutter("{\"name\":\"江澎涌\", \"age\":28}")
        }

        findViewById<Button>(R.id.btn_send_map_to_flutter).setOnClickListener {
            val map = HashMap<String, Any>()
            map["name"] = "Jiang Peng Yong"
            map["age"] = 28
            Flutter.Channels.nezaStandardBasicChannel.sendToFlutter(map)
        }
    }

    private fun openFlutterPage() {
        startActivity(
            FlutterActivity
                .withCachedEngine(Config.ENGINE_ID)
                ?.build(this)
        )
    }
}