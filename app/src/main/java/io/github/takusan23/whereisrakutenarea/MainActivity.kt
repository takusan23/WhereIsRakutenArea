package io.github.takusan23.whereisrakutenarea

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.takusan23.whereisrakutenarea.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    /** 権限コールバック */
    private val permissionCallBack = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // 権限確保
            checkRakutenNetwork()
        }
    }

    /** ViewBinding */
    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        start()

        // ボタンを押したとき
        viewBinding.button.setOnClickListener {
            start()
        }

    }

    /** 権限があれば回線チェックを行う */
    private fun start() {
        // 権限の確認
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 権限がある
            checkRakutenNetwork()
        } else {
            // 無い
            Toast.makeText(this, "接続状態へのアクセスに権限が必要ですので付与をお願いします。", Toast.LENGTH_SHORT).show()
            permissionCallBack.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /** 楽天の自社回線に繋がっているか確認する関数 */
    private fun checkRakutenNetwork() {

        /** 関数内に関数を書く。TextViewに回線名を入れる */
        fun setTextViewOperatorName(cellInfo: CellInfo) {
            viewBinding.textView.text = when (cellInfo) {
                // 3G
                is CellInfoWcdma -> {
                    cellInfo.cellIdentity.operatorAlphaLong
                }
                // LTE
                is CellInfoLte -> {
                    cellInfo.cellIdentity.earfcn
                    cellInfo.cellIdentity.operatorAlphaShort
                }
                // 5G (NR)
                is CellInfoNr -> {
                    cellInfo.cellIdentity.operatorAlphaShort
                }
                else -> "不明"
            }
        }

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Android 10以降は前取得したデータを使い回す仕様のため、新しく取ってきてもらうために分岐
                telephonyManager.requestCellInfoUpdate(mainExecutor, object : TelephonyManager.CellInfoCallback() {
                    override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                        setTextViewOperatorName(cellInfo[0])
                    }
                })
            }
        } else {
            val cellInfoList = telephonyManager.allCellInfo
            setTextViewOperatorName(cellInfoList[0])
        }
    }

}