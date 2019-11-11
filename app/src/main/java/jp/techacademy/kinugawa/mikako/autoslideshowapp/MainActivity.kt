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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //パーミッション用
    private val PERMISSIONS_REQUEST_CODE = 100

    var image = mutableListOf<Uri>()

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

        //添字用
        var sum = image.indices.toString()
        var number = sum.toInt()

        //進むボタン
        next_button.setOnClickListener {
            if (image.size > number) {
                //添字に1を足して画像を表示
                number += 1
                imageView.setImageURI(image[number])
            } else if (image.size == number){
                //添字を0に戻して画像を表示
                number = 0
                imageView.setImageURI(image[number])
            }

            Log.d("ANDROID", "URI : " + image[number].toString())

        }

        //戻るボタン
        back_button.setOnClickListener {
            if (image.size < number) {
                //添字に1を引いて画像を表示
                number -= 1
                imageView.setImageURI(image[number])
            } else if (image.size == 0){
                number = image.size
                imageView.setImageURI(image[number])
            }

            Log.d("ANDROID", "URI : " + image[number].toString())

        }

        //スライドショーボタン
        slide_button.setOnClickListener {

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された")
                    getContentsInfo()
                } else {
                    Log.d("ANDROID", "許可されなかった")
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

        //var image = mutableListOf<Uri>()

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("ANDROID", "URI : " + imageUri!!.toString())

                //取得したimageUriを書き込み可能なMutableListに代入 。インデックス番号使いたいから。
                //image = mutableListOf(imageUri)

                image.add(imageUri)

            } while (cursor.moveToNext())

            //何枚画像があるか確認する
            Log.d("ANDROID", "URI : " + image.size.toString())

            imageView.setImageURI(image[0]) //インデックス番号で1枚目を表示
        }
        cursor.close()
    }
}

