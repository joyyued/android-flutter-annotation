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

        // =========================== Method ===========================
        findViewById<Button>(R.id.btn_method_none).setOnClickListener {
            Flutter.Channels
                .nezaMethodChannel
                .sayHelloToFlutter()
        }

        findViewById<Button>(R.id.btn_method_none_async).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = Flutter.Channels
                    .nezaMethodChannel
                    .sayHelloToFlutterAsync()
                show("none param : $result")
            }
        }

        findViewById<Button>(R.id.btn_method_single).setOnClickListener {
            Flutter.Channels
                .nezaMethodChannel
                .sayHelloToFlutter("Jiang PengYong [ single param ]")
        }

        findViewById<Button>(R.id.btn_method_single_async).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = Flutter.Channels
                    .nezaMethodChannel
                    .sayHelloToFlutterAsync("Jiang PengYong [ single param ]")
                show("single param : $result")
            }
        }

        findViewById<Button>(R.id.btn_method_multi).setOnClickListener {
            Flutter.Channels
                .nezaMethodChannel
                .sayHelloToFlutter(
                    name = "Jiang PengYong [ multi param ]",
                    age = 28,
                    height = 170,
                )
        }

        findViewById<Button>(R.id.btn_method_multi_async).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = Flutter.Channels
                    .nezaMethodChannel
                    .sayHelloToFlutterAsync(
                        name = "Jiang PengYong [ multi param ]",
                        age = 28,
                        height = 170,
                    )
                show("multi param : $result")
            }
        }

        // =========================== Event ===========================
        findViewById<Button>(R.id.btn_send_image_info).setOnClickListener {
            val byteArray = AssetsUtils.getAssetsFile(resources, "sample.png")
            Flutter.Channels.nezaEventChannel.sendImageInfo(byteArray)
            Flutter.Channels.nezaEventChannel.sendImageInfo(
                errorCode = "",
                errorDetails = "",
                errorMessage = ""
            )
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

    private fun show(msg: String) {
        Log.e("Neza Project", "[MainActivity] $msg")
    }
}