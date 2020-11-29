package com.networks.arcorefilters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.Toast
import com.google.ar.sceneform.ArSceneView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PhotoSaver (
    private val activity: Activity
){

    private fun getPhotoFile():File{
        val fileName = "IMG_${System.currentTimeMillis()}.png"
        return File(activity.externalCacheDir.toString() + File.separator + fileName)
    }

    private fun saveBitMapAsFile(bitmap: Bitmap, file: File): File?{
        return try {
            file.createNewFile()

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapData = bos.toByteArray()

            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun takePhoto(arSceneView: ArSceneView){
        val bmp = Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopyThread")
        handlerThread.start()

        PixelCopy.request(arSceneView, bmp, {
            if(it == PixelCopy.SUCCESS){
                val result = saveBitMapAsFile(bmp,getPhotoFile())
                activity.startActivity( ResultsActivity.newInstance(activity, result?.absolutePath?:"",true))
            }else{
                activity.runOnUiThread {
                    Toast.makeText(activity, "Unable to take photo", Toast.LENGTH_LONG).show()
                }
            }
        }, Handler(handlerThread.looper))
    }
}