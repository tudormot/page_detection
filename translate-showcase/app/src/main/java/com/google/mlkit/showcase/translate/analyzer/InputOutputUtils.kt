package com.google.mlkit.showcase.translate.analyzer

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.mlkit.showcase.translate.R
import java.io.IOException


fun getJson(context:Context):Map<String,String> {
    try {
        context.resources.openRawResource(R.raw.book_text).bufferedReader().use {
            val gson = Gson()
            val reader = JsonReader(it)
            return gson.fromJson<HashMap<String, String>?>(reader, hashMapOf<String,String>().javaClass).filter {  entry ->
                entry.value != ""
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return hashMapOf()
}