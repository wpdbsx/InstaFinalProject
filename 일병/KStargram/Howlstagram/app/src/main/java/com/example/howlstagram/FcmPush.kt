package com.example.howlstagram

import com.example.howlstagram.model.PushDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.okhttp.*

import java.io.IOException

class FcmPush() {

    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey =
        "AAAAFPpVLVs:APA91bGzeEWT3uIQVwNlZ5zaQIE_ANgOtOcKc7dnBLvKCFlyg2D-lyq25LcIgwmLEpvcITpb06JGqroqVZ4KSD5gUvUnxikOsKrgiIgHjCLxSWJ_JZW_X2TzSJ7qeOpaJm0RKv8XSPDF"

    var okHttpClient: OkHttpClient? = null
    var gson: Gson? = null

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()


    }


    fun sendMessage(destinationUid: String, title: String?, message: String?) {
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get()
            .addOnCompleteListener { task ->

               var uid = FirebaseAuth.getInstance().currentUser!!.uid
                if(!destinationUid.equals(uid))
                if (task.isSuccessful) {

                    var token = task.result["pushToken"].toString()

                    var pushDTO = PushDTO()
                    pushDTO.to = token
                    pushDTO.notification?.title = title
                    pushDTO.notification?.body = message

                    var body = RequestBody.create(JSON, gson?.toJson(pushDTO)!!)
                    var request = Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=" + serverKey)
                        .url(url)
                        .post(body)
                        .build()
                    okHttpClient?.newCall(request)?.enqueue(object : Callback {
                        override fun onFailure(request: Request?, e: IOException?) {
                         }

                        override fun onResponse(response: Response?) {
                            println(response?.body()?.string()) }



                    })

                }
            }


    }

}