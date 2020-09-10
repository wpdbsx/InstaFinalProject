package com.example.howlstagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.howlstagram.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_main.*


class AddPhotoActivity : AppCompatActivity() {
    val PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)


        addphoto_image.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)


        }
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) //모든 결과값이 집중되는곳이다. 그래서 필터링을 하는 if를걸어준다.
        {
            if (resultCode == Activity.RESULT_OK)  //만약 사진을 선택하면 이미지를 불러온다.,
            {
                photoUri = data?.data
                addphoto_image.setImageURI(data?.data)
            } else {
                finish() // 카메라에서 사진을 누르면 다시 앨범으로 가거나 뒤로가기를 하면 종료가된다.
            }
        }
    }

    fun contentUpload() {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)
        //child가 폴더를 만들겠다는 의미다. images 에 그 이미지파일 이름의 파일을 만든다.

        storageRef?.putFile(photoUri!!)?.addOnFailureListener {
            // progress_bar.visibility = View.GONE
            Toast.makeText(
                this, getString(R.string.upload_fail),
                Toast.LENGTH_SHORT
            ).show()

        }?.addOnSuccessListener { taskSnapshot ->
            // success
            // downloadUrl을 받아 올수 있음.
            storageRef.downloadUrl.addOnCompleteListener { taskSnapshot ->

                // progress_bar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()

                var uri = taskSnapshot.result.toString()


                val contentDTO = ContentDTO()
                //이미지 주소
                contentDTO.imagerUrl = uri!!.toString()
                // 유저의 UID
                contentDTO.uid = auth?.currentUser?.uid
                //게시물의 설명
                contentDTO.explain = addphoto_edit_explain.text.toString()
                //유저 아이디
                contentDTO.userId = auth?.currentUser?.email
                //게시물 업로드 시간
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document("JPEG_"+contentDTO.timestamp.toString())?.set(contentDTO)
                //컬렉션은 일종의 경로
                setResult(Activity.RESULT_OK)
                finish()
            }
        }  ?.addOnFailureListener {
            progress_bar.visibility = View.GONE

            Toast.makeText(this, getString(R.string.upload_fail),
                Toast.LENGTH_SHORT).show()
        }
    }

}
