package jp.techacademy.kinugawa.mikako.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.support.design.widget.Snackbar
import android.view.View

class MainActivity : AppCompatActivity() {

    //パーミッション用
    private val PERMISSIONS_REQUEST_CODE = 100

    //画像のURIを格納するためのリスト
    var image = mutableListOf<Uri>()

    //画像のインデックス番号用
    var numbers = 0

    //タイマー
    private var mTimer: Timer? = null

    //Handlerインスタンス作成 スレッドを超えて依頼をする用
    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //アプリを立ち上げたタイミングで外部ストレージにアクセスする許可をとる。
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


        //進むボタン
        next_button.setOnClickListener {
            if (image.size.toInt()-1 > numbers) {
                //添字に1を足して画像を表示
                numbers += 1
                imageView.setImageURI(image[numbers])
            } else if (image.size.toInt()-1 == numbers ){
                //添字を0に戻して画像を表示
                numbers = 0
                imageView.setImageURI(image[numbers])
            }

            Log.d("ANDROID", "URI : " + image[numbers].toString())

        }

        //戻るボタン　iが0の時だけ特別扱いで書き直し予定
        back_button.setOnClickListener {
            if (image.size.toInt()-1 >= numbers && numbers != 0) {
                //添字に1を引いて画像を表示
                numbers -= 1
                imageView.setImageURI(image[numbers])
            } else if (numbers == 0){
                numbers = image.size.toInt()-1
                imageView.setImageURI(image[numbers])
            }

            Log.d("ANDROID", "URI : " + image.size.toString())

        }



        //自動送りボタン
        slide_button.setOnClickListener {

            if (mTimer == null){

                // タイマーの作成
                mTimer = Timer()

                //自動送りはじめる
                slid_start()

                //進む・戻るボタンをタップ無効にする　+ 文字のグレーアウト
                next_button.isEnabled = false
                back_button.isEnabled = false

                //自動送りボタン表記を「停止」にしておく
                slide_button.text = "停止"


            } else if (mTimer != null){

                //止める
                mTimer!!.cancel()

                //nullに戻しておく
                mTimer = null

                //進む・戻るボタンをタップできるようにする
                next_button.isEnabled = true
                back_button.isEnabled = true

                //自動送りボタン表記を「再生」にしておく
                slide_button.text = "再生"

            }

        }

    }


    //2秒毎に自動送りをはじめる関数をまとめておく　slid_start()
    fun slid_start() {
        // タイマーの始動
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {

                mHandler.post {
                    if (image.size.toInt()-1 > numbers) {
                        //添字に1を足して画像を表示
                        numbers += 1
                        imageView.setImageURI(image[numbers])
                    } else if (image.size.toInt()-1 == numbers ){
                        //添字を0に戻して画像を表示
                        numbers = 0
                        imageView.setImageURI(image[numbers])
                    }
                }
            }
        }, 100, 2000) // 最初に始動させるまで 秒、ループの間隔を 秒 に設定

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された")
                    getContentsInfo()
                } else {
                    Log.d("ANDROID", "許可されなかった")

                    //進む・戻る・自動送りボタンをタップ無効にする　+ 文字のグレーアウト
                    next_button.isEnabled = false
                    back_button.isEnabled = false
                    slide_button.isEnabled = false

                    Snackbar.make(this.imageView, "画像を表示できませんでした", Snackbar.LENGTH_SHORT)
                            //.setAction("Action", null)  //いらない？
                            .show()
                    return

                }
        }

    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        )

        var imageUri :Uri?

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("ANDROID", "URI : " + imageUri!!.toString())

                //imageリストに格納していく
                image.add(imageUri)

            } while (cursor.moveToNext())

            //何枚画像があるか確認する
            Log.d("ANDROID", "URI : " + image.size.toString())

            imageView.setImageURI(image[0]) //インデックス番号で1枚目を表示
        }
        cursor.close()
    }
}

