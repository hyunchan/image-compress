package com.hcpark.image_compress.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

class MainViewModel : ViewModel() {
    private val decimalFormat = DecimalFormat("0.00")
    val choose = MutableLiveData<Uri?>()
    val info = MutableLiveData("null")
    val quality = MutableLiveData("")
    val compress = MutableLiveData<File?>()
    val compressInfo = compress.map {
        it ?: return@map "null"
        val size = it.length()
        val mSize = size.div(1024 * 1024 * 1f)
        "${it.name}\n${decimalFormat.format(mSize)} MB, ${decimalFormat.format(size)} Bytes"
    }

    fun put(context: Context, uri: Uri?) = viewModelScope.launch {
        choose.value = uri
        if (uri != null) context.contentResolver.query(
            uri, null, null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)
                val mSize = size.div(1024 * 1024 * 1f)
                Log.d("hc", "name::$name size::$size")
                info.value =
                    "${name}\n${decimalFormat.format(mSize)} MB, ${decimalFormat.format(size)} Bytes"
            }
        } ?: Log.e("hc", "cursor is null")

        if (uri != null)
            compress(context, uri, quality.value ?: "")
    }

    fun compress(context: Context, uri: Uri, qualityString: String) = viewModelScope.launch {
        val qualityValue = try {
            Integer.parseInt(qualityString)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

        if (qualityValue < 1 || 100 < qualityValue) {
            compress.value = null
            return@launch
        }
        val file = createFile(context)
        if (file != null) {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }?.compress(
                Bitmap.CompressFormat.JPEG, qualityValue, file.outputStream()
            )
            compress.value = file

        } else
            Log.e("hc", "file is null")
    }

    private fun createFile(context: Context): File? {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.also {
                if (it.exists().not()) it.mkdirs()
            }
            File.createTempFile("temp", ".jpg", dir)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }
}