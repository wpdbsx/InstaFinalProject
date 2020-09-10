package com.example.howlstagram

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.howlstagram.model.UserModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null

    var database: FirebaseDatabase? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001 //구글 로그인 코드
    var callbackManger: CallbackManager? = null //페이스북 관련 로그인 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {

            createAndLoginEmail()
        }
        google_sign_in_button.setOnClickListener {

            googleLogin()

        } //구글 로그인 버튼누르면 작동한다.
        facebook_login_button.setOnClickListener {
            facebookLogin()
        }//페이스북 로그인버튼을 누르면작동한다.
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            //구글 로그인에 접근할수있도록 허가해주는 id키값
            //default_web_client_id 즉 인증키
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        printHashKey(this)
        callbackManger = CallbackManager.Factory.create() //초기화 시키는것
    }

    //페이스북 키 받는것
    fun printHashKey(pContext: Context) {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("Howl", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Howl", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("Howl", "printHashKey()", e)
        }

    }

    fun createAndLoginEmail() {
        if (email_edittext.text.length == 0 || password_edittext.text.length == 0) {
            Toast.makeText(this, "올바르지 않은 형식입니다.", Toast.LENGTH_LONG).show()
            return;
        }
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->


                if (task.isSuccessful) {

                    moveMainPage(auth?.currentUser)
                    // Toast.makeText(this, "아이디 생성 성공", Toast.LENGTH_LONG).show()
                } else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

                } else {
                    signinEmail()
                }


            }
    }

    fun signinEmail() {

        auth?.signInWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                var uid: String
                var name: String

                Log.v("ㅎㅇ", task.getResult().user.photoUrl.toString())
                name = email_edittext.text.toString()
                uid = task.getResult().user.uid

                var firestore = FirebaseFirestore.getInstance()

                var profileImageUrl = ""



                firestore?.collection("profileImages")?.document(uid!!)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        profileImageUrl = task.result["image"].toString()
                        var comment = ""

                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                    writeNewUser(uid, name, profileImageUrl, comment)

                                    for (item in dataSnapshot.children) {
                                        if (item.key == "comment") {
                                            comment = item.getValue().toString()
                                            writeNewUser(uid, name, profileImageUrl, comment)
                                            passPushTokenToServer()
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })


                    }
                }










                moveMainPage(auth?.currentUser)
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

            }

        }


    }

    private fun writeNewUser(userId: String, name: String, ProfileImageUrl: String, comment: String) {
        val user = UserModel(name, ProfileImageUrl, userId, comment)

        // user?.userName = email_edittext.text.toString()
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(user)
    }

    internal fun passPushTokenToServer() {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val token = FirebaseInstanceId.getInstance().token
        val map = HashMap<String, Any>()
        map.put("pushToken", token!!)

        FirebaseDatabase.getInstance().reference.child("users").child(uid).updateChildren(map)


    }
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) { //로그인 되었을경우 스타트 액티비티를 활성화
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {

        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    //파이어베이스에 구글이 로그인아이디정보를 자동으로 넘겨주지 않아서 넘겨주는 코드를 넣어야한다.
    //이것이 구글에서 파이어베이스로 코드를 넘겨주는것이다.
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        //구글아이디를 credential에 담는것
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {


                var uid: String
                var name: String
                var profileImageUrl: String
                var comment = ""

                name = account.email!!
                uid = task.getResult().user.uid
                profileImageUrl = ""
                var firestore = FirebaseFirestore.getInstance()
                firestore?.collection("profileImages")?.document(uid!!)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        profileImageUrl = task.result["image"].toString()
                        var comment = ""



                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                    writeNewUser(uid, name, profileImageUrl, comment)

                                    for (item in dataSnapshot.children) {
                                        if (item.key == "comment") {

                                            comment = item.getValue().toString()
                                            writeNewUser(uid, name, profileImageUrl, comment)
                                            passPushTokenToServer()
                                        }
                                    }


                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })


                    }
                }

                Log.v("안녕", "ㅎㅇ2")
                moveMainPage(auth?.currentUser)
            }
        }
    }

    fun facebookLogin() { //로그인이 성공하면 callbackManger 가 호출되면서 부른다
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager
            .getInstance()
            .registerCallback(callbackManger, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {

                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {
                    println("loginCancel")
                }

                override fun onError(error: FacebookException?) {
                    println("loginError")
                }

            })

    }

    fun handleFacebookAccessToken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { //auth가 파이어베이스를 관리한다.
                task ->
            if (task.isSuccessful) {  //facebook로그인 완료되면 메인페이지로 넘어간다.
                var uid: String
                var name: String
                var profileImageUrl: String

                name = ""
                uid = task.getResult().user.uid
                profileImageUrl = ""
                var firestore = FirebaseFirestore.getInstance()
                firestore?.collection("profileImages")?.document(uid!!)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        profileImageUrl = task.result["image"].toString()
                        var comment = ""



                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                    writeNewUser(uid, name, profileImageUrl, comment)

                                    for (item in dataSnapshot.children) {
                                        if (item.key == "comment") {

                                            comment = item.getValue().toString()
                                            writeNewUser(uid, name, profileImageUrl, comment)
                                            passPushTokenToServer()
                                        }
                                    }


                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })


                    }
                }


                moveMainPage(auth?.currentUser)

            }

        }
    }

    override fun onResume() {
        super.onResume()


        moveMainPage(auth?.currentUser)
    }

    //이전의 화면의 데이터를 넘겨줄떄 사용하는것
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManger?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }

    }


}
