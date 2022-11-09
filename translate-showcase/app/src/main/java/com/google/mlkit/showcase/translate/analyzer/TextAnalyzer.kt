/*
 * Copyright 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.google.mlkit.showcase.translate.analyzer

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.mlkit.common.MlKitException
import com.google.mlkit.showcase.translate.util.ImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import info.debatty.java.stringsimilarity.Cosine
import info.debatty.java.stringsimilarity.Jaccard
import java.util.concurrent.Executor

/**
 * Analyzes the frames passed in from the camera and returns any detected text within the requested
 * crop region.
 */
class TextAnalyzer(
    private val context: Context,
    lifecycle: Lifecycle,
    executor: Executor,
    private val result: MutableLiveData<String>,
) : ImageAnalysis.Analyzer {
    private val detector =
        TextRecognition.getClient(TextRecognizerOptions.Builder().setExecutor(executor).build())
//    private val textData: Map<String,String> =

    private val cosineComputer: Cosine = Cosine(GRAM_SIZE)
    private val jaccardComputer: Jaccard = Jaccard(GRAM_SIZE)
    private val textPrecomputedCosines: Map<String,Map<String,Int>> =
        getJson(context).mapValues {
            cosineComputer.getProfile(it.value)
        }

    private val textPrecomputedJaccard: Map<String,Map<String,Int>> =
        getJson(context).mapValues {
            jaccardComputer.getProfile(it.value)
        }

    companion object{
        const val GRAM_SIZE = 6
        private const val TAG = "TextAnalyzer"
    }


    init {
        lifecycle.addObserver(detector)
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        recognizeTextOnDevice(image).addOnCompleteListener {
            imageProxy.close()
        }

    }

    private fun recognizeTextOnDevice(
        image: InputImage
    ): Task<Text> {
        return detector.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
//                result.value = visionText.text
                result.value = calculateSimilarities(visionText.text)
                Log.d(TAG, "result.value = ${result.value}")

            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                Log.e(TAG, "Text recognition error", exception)
                val message = getErrorMessage(exception)
                message?.let {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun calculateSimilarities(detectedText: String):String {
        val cosineMeasurementsString =
            sequence {
                val detectedTextCosProfile = cosineComputer.getProfile(detectedText)
                textPrecomputedCosines.forEach {
                    yield(Pair(it.key, cosineComputer.similarity(detectedTextCosProfile, it.value)))
                }
            }.sortedByDescending { it.second }.take(3).map {
                "Page ${it.first}, similarity: ${it.second}"
            }.toList().joinToString(separator = "\n", prefix = "Cosine sim:\n")

//        val jaccardMeasurementsString =
//            sequence {
//                val detectedTextJaccardProfile = jaccardComputer.getProfile(detectedText)
//                textPrecomputedJaccard.forEach {
//                    yield(Pair(it.key, jaccardComputer.similarity(detectedTextJaccardProfile, it.value)))
//                }
//            }.sortedByDescending { it.second }.take(3).map {
//                "Page ${it.first}, similarity: ${it.second}"
//            }.toList().joinToString(separator = "\n", prefix = "Cosine sim:\n")
        return cosineMeasurementsString



    }
    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

}