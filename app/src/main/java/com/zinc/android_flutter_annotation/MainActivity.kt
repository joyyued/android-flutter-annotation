package com.zinc.android_flutter_annotation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.joyy.ued.android_flutter_annotation.annotation.model.EventChannelSenderEOSType
import com.joyy.ued.android_flutter_annotation.engine.EngineUtils
import com.joyy.ued.android_flutter_annotation.engine.Flutter
import com.zinc.android_flutter_annotation.config.Config
import com.zinc.android_flutter_annotation.utils.AssetsUtils
import io.flutter.embedding.android.FlutterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: Jiang PengYong
 * @date: 2021/8/9 6:38 下午
 * @email: 56002982@qq.com
 * @des:
 */
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_init_engine).setOnClickListener {
            Flutter.init(this)
        }

        findViewById<Button>(R.id.btn_release_engine).setOnClickListener {
            Flutter.release()
        }

        findViewById<Button>(R.id.btn_goto_flutter_page).setOnClickListener {
            openFlutterPage()
        }

        // =========================== Method ===========================
        findViewById<Button>(R.id.btn_method_none).setOnClickListener {
            Flutter.channels?.nezaMethodChannel?.sayHelloToFlutter()
        }

        findViewById<Button>(R.id.btn_method_none_async).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = Flutter.channels
                    ?.nezaMethodChannel
                    ?.sayHelloToFlutter()
                    ?.await()
                show("none param result: $result")
            }
        }

        findViewById<Button>(R.id.btn_method_single).setOnClickListener {
            Flutter.channels
                ?.nezaMethodChannel
                ?.sayHelloToFlutter("Jiang PengYong [ single param ]")
        }

        findViewById<Button>(R.id.btn_method_single_async).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = Flutter.channels
                    ?.nezaMethodChannel
                    ?.sayHelloToFlutter("Jiang PengYong [ single param ]")
                    ?.await()
                show("single param result: $result")
            }
        }

        findViewById<Button>(R.id.btn_method_multi).setOnClickListener {
            Flutter.channels
                ?.nezaMethodChannel
                ?.sayHelloToFlutter(
                    name = "Jiang PengYong [ multi param ]",
                    age = 28,
                    height = 170
                )
        }

        findViewById<Button>(R.id.btn_method_multi_async).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = Flutter.channels
                    ?.nezaMethodChannel
                    ?.sayHelloToFlutter(
                        name = "Jiang PengYong [ multi param ]",
                        age = 28,
                        height = 170
                    )
                    ?.await()
                show("multi param result: $result")
            }
        }

        findViewById<Button>(R.id.btn_method_change_name).setOnClickListener {
            Flutter.channels
                ?.nezaMethodChannel
                ?.changeName()
        }

        // =========================== Event ===========================
        findViewById<Button>(R.id.event_send_image).setOnClickListener {
            val byteArray = AssetsUtils.getAssetsFile(resources, "sample.png")
            Flutter.channels
                ?.nezaEventChannel
                ?.sendImageInfo(
                    byteArray = byteArray
                )
        }

        findViewById<Button>(R.id.event_send_info).setOnClickListener {
            val byteArray = AssetsUtils.getAssetsFile(resources, "sample.png")
            Flutter.channels
                ?.nezaEventChannel
                ?.sendImageInfo(
                    id = 5,
                    byteArray = byteArray
                )
        }

        findViewById<Button>(R.id.event_send_error).setOnClickListener {
            Flutter.channels
                ?.nezaEventChannel
                ?.sendImageInfo(
                    errorCode = "404",
                    errorMessage = "Error Message",
                    errorDetails = "Error Details"
                )
        }

        findViewById<Button>(R.id.event_send_end_of_stream).setOnClickListener {
            Flutter.channels
                ?.nezaEventChannel
                ?.sendImageInfo(type = EventChannelSenderEOSType.EOS)
        }
        // =========================== Basic ===========================

        findViewById<Button>(R.id.btn_send_json_to_flutter).setOnClickListener {
            Flutter.channels
                ?.nezaStringBasicChannel
                ?.sendJsonToFlutter("{\"name\":\"江澎涌\", \"age\":28}")
        }

        findViewById<Button>(R.id.btn_send_map_to_flutter).setOnClickListener {
            val map = HashMap<String, String>()
            map["name"] = "Jiang Peng Yong"
            map["age"] = "28"
            Flutter.channels?.nezaStandardBasicChannel?.sendToFlutter(map)
        }

        findViewById<Button>(R.id.btn_send_map_to_flutter_customer).setOnClickListener {
            val map = HashMap<String, String>()
            map["name"] = "Jiang Peng Yong"
            map["age"] = "28"
            Flutter.channels?.nezaCustomerBasicChannel?.sendMapToFlutter(map)
        }
    }

    private fun openFlutterPage() {
        if (EngineUtils.getEngine(Config.ENGINE_ID) != null) {
            startActivity(
                FlutterActivity
                    .withCachedEngine(Config.ENGINE_ID)
                    .build(this@MainActivity)
            )
        } else {
            Toast.makeText(this, "请先初始化 Engine", Toast.LENGTH_LONG).show()
        }
    }

    private fun show(msg: String) {
        Log.i("Neza", "[MainActivity] $msg")
    }
}