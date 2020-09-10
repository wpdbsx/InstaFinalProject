package com.example.howlstagram


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    var PICK_PROFILE_FROM_ALBUM = 10
    var fragmentView: View? = null
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        setToolbarDefault()
        when (item.itemId) {
            R.id.action_home -> {
                var detailViewFragment = DetailviewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()

                return true
            }
            R.id.action_search -> {
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, gridFragment).commit()

                return true
            }
            R.id.action_add_photo -> {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }

                return true
            }
            R.id.action_favorite_alarm -> {
                var alertFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alertFragment).commit()

                return true
            }
            R.id.action_account -> {
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var userFragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()

                return true
            }


        }

        return false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.action_home


        //this 는class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
        //  를 가르키고 BottomNavigationView.OnNavigationItemSelectedListener 로 넘어와서
        //onNavigationItemSelected 로 간다.
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        //사진을 받아오는권한을 얻는 코드
        registerPushToken()
        action_message.setOnClickListener {

            val intent = Intent(this,MainActivity2::class.java)

            startActivity(intent)

    }
        action_food.setOnClickListener {

            var fragment1 = Fragment1()
            supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment1).commit()

        }


    }

    fun registerPushToken() {
        var pushToken = FirebaseInstanceId.getInstance().token
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var map = mutableMapOf<String, Any>()

        map["pushToken"] = pushToken!!
        FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
    }

    fun setToolbarDefault() {
        toolbar_btn_back.visibility = View.GONE
        toolbar_username.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid).putFile(imageUri!!)
                .addOnCompleteListener { task ->
                    var url = task.result.downloadUrl.toString()
                    var map = HashMap<String, Any>()
                    map["image"] = url
                    FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)

                }
        }
    }
}