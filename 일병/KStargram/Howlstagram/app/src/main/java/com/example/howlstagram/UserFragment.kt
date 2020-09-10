package com.example.howlstagram

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.model.AlarmDTO
import com.example.howlstagram.model.ContentDTO
import com.example.howlstagram.model.FollowDTO
import com.example.howlstagram.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var PICK_PROFILE_FROM_ALBUM = 10
    var firestore: FirebaseFirestore? = null
    //현재 나의 uid
    var currentUseruid: String? = null
    //내가 선택한 uid
    var uid: String? = null

    var auth: FirebaseAuth? = null
    var fcmPush: FcmPush? = null

    var followListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null
    var imageprofileListenerRegistration: ListenerRegistration? = null
    var recyclerListenerRegistration: ListenerRegistration? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentUseruid = FirebaseAuth.getInstance().currentUser?.uid
        firestore = FirebaseFirestore.getInstance()
        fragmentView = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_user, container, false)
        fcmPush = FcmPush()
        auth = FirebaseAuth.getInstance()
          chogi()
        if (arguments != null) {

            uid = arguments!!.getString("destinationUid")
            if (uid != null && uid == currentUseruid) {
                //나의 유저 페이지
                fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    activity?.finish()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    auth?.signOut()
                }

            } else {
                //제 3자의 유저 페이지
                fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)

                var mainActivity = (activity as MainActivity)
                mainActivity.toolbar_title_image.visibility = View.GONE
                mainActivity.toolbar_btn_back.visibility = View.VISIBLE
                mainActivity.toolbar_username.visibility = View.VISIBLE
                mainActivity.toolbar_username.text = arguments!!.getString("userId")

                mainActivity.toolbar_btn_back.setOnClickListener {
                    mainActivity.bottom_navigation.selectedItemId = R.id.action_home

                }
                fragmentView?.account_btn_follow_signout?.setOnClickListener {

                    requestFollow()
                }

            }


        }



        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPcikerIntent = Intent(Intent.ACTION_PICK)
            photoPcikerIntent.type = "image/*"
            activity?.startActivityForResult(photoPcikerIntent, PICK_PROFILE_FROM_ALBUM)
        }



        return fragmentView
    }

    fun requestFollow() {
        var tsDocFollowing = firestore!!.collection("users").document(currentUseruid!!)

        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                //아무도 팔로잉 하지 않았을 경우
                followDTO = FollowDTO()
                followDTO?.userId=auth?.currentUser!!.email
                followDTO.followingCount = 1
                followDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if (followDTO.followings.containsKey(uid)) {
                //내 아이디가 제3를 이미 팔로잉 하고 있을 경우 -> 제3자가 나를 팔로워 취소한다.
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings.remove(uid)
            } else {
                //내가 제3를 팔로잉 하지 않았을 경우 -> 제3자가 나를 팔로워 한다.
                followDTO.followingCount = followDTO.followingCount + 1
                followDTO.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore!!.collection("users").document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)

            if (followDTO == null) {
                //아무도 팔로워 하지 않았을 경우
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUseruid!!] = true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction

            }

            if (followDTO?.followers!!.containsKey(currentUseruid!!)) {
                //제3자의 유저를 내가 팔로잉 하고 있을 경우 -> 팔로워 취소 하겠다.

                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUseruid!!)


            } else {
                //제3자를 내가 팔로워 하지 않았을 경우 -> 팔로워 하겠다.

                followDTO?.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUseruid!!] = true
                followerAlarm(uid)


            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction


        }


    }
    fun getProfileImage() {
        imageprofileListenerRegistration = firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null) {
                    var url = documentSnapshot.data!!["image"]
                    var uid2: String = auth?.currentUser?.uid!!
                    var name: String = auth?.currentUser?.email!!
                    var ProfileImageUrl: String = url!!.toString()

                   var profileImageUrl= firestore!!.collection("profileImages")?.document(uid2!!).get().toString()
                    var firestore = FirebaseFirestore.getInstance()




                          if(uid==uid2) {
                              firestore?.collection("profileImages")?.document(uid2!!)?.get()
                                  ?.addOnCompleteListener { task ->
                                      if (task.isSuccessful) {
                                          profileImageUrl = task.result["image"].toString()
                                          var comment = ""

                                              FirebaseDatabase.getInstance().getReference().child("users").child(uid2)
                                                  .addListenerForSingleValueEvent(object : ValueEventListener {
                                                      override fun onDataChange(dataSnapshot: DataSnapshot) {

                                                          writeNewUser(uid2, name, profileImageUrl, comment)

                                                          for (item in dataSnapshot.children) {
                                                              if (item.key == "comment") {

                                                                  comment = item.getValue().toString()

                                                                  writeNewUser(uid2, name, profileImageUrl, comment)
                                                                  passPushTokenToServer()
                                                              }
                                                          }


                                                      }

                                                      override fun onCancelled(databaseError: DatabaseError) {

                                                      }
                                                  })


                                      }
                                  }
                          }

                    Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView!!.account_iv_profile)
                }
            }
    }

    private fun writeNewUser(userId: String, name: String, ProfileImageUrl: String,comment :String) {
        val user = UserModel(name, ProfileImageUrl,uid,comment)

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
    fun followerAlarm(destinationUid: String?) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser!!.email
        alarmDTO.uid = auth?.currentUser!!.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        fcmPush?.sendMessage(destinationUid!!, "알림 메세지 입니다", message)

    }

    override fun onResume() {
        super.onResume()
        getProfileImage()
        getFollowing()
        getFollower()
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

    }
    override fun onStop() {
        super.onStop()
        followListenerRegistration?.remove()
        followingListenerRegistration?.remove()
        imageprofileListenerRegistration?.remove()
        recyclerListenerRegistration?.remove()
    }

    fun getFollower() {

        followListenerRegistration = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount.toString()
            if (followDTO?.followers?.containsKey(currentUseruid)!!) {

                fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                fragmentView?.account_btn_follow_signout
                    ?.background
                    ?.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
            } else {

                if (uid != currentUseruid) {

                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                    fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                }
            }

        }

    }
    fun getFollowing() {
        followingListenerRegistration = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView!!.account_tv_following_count.text = followDTO?.followingCount.toString()
        }
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO>
        val contentDTOs2: ArrayList<ContentDTO> //파이어베이스의문서추가를 담아주는애
        val contentUIdList: ArrayList<String>//파이어베이스의 컨텐트 컬렉션 필드가 여기에있다.
        init {
            contentDTOs = ArrayList()
            recyclerListenerRegistration = firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    account_tv_post_count.text = contentDTOs.size.toString()
                    notifyDataSetChanged() //새로고침 같은 것

                }
            contentDTOs2 = ArrayList()
            contentUIdList = ArrayList()
            Log.v("아이디","여기0")
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            //현재 로그인된 유저의 UID
            firestore?.collection("users")?.document(uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var userDTO = task.result.toObject(FollowDTO::class.java)
                    if(userDTO != null) {
                        Log.v("아이디","여기1")
                        getContents(userDTO.followings)
                    }

                }
                notifyDataSetChanged()
            }




        }
        fun getContents(followers: MutableMap<String, Boolean>) {
            //firestore에 images에 접근한것.
            Log.v("아이디","여기2")
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        //이미지를 하나씩 돌아가면서 uid를 하나씩 넣는 것이다.
                        var item = snapshot.toObject(ContentDTO::class.java)
                        Log.v("아이디1",item?.uid)
                        Log.v("아이디2",uid)
                        if(item?.uid.equals(uid)) {
                            Log.v("아이디들어갔다","그래")

                                Log.v("아이디1",item?.uid)
                                Log.v("아이디2",uid)
                            Log.v("아이디2",uid)
                            Log.v("아이디2",contentDTOs2.toString())
                                contentDTOs2.add(item!!)
                                contentUIdList.add(snapshot.id)


                        }
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)


            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imagerUrl)
                .apply(RequestOptions().centerCrop()).into(imageview)


           if(uid.equals(contentDTOs[position].uid))
            imageview.setOnClickListener { v ->
                Log.v("안녕", position.toString())
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUIdList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
        }


        }

    }


    fun chogi() {
        var tsDocName = firestore!!.collection("users1").document(currentUseruid!!)

        firestore?.runTransaction { transaction ->
            var usermodel = transaction.get(tsDocName).toObject(UserModel::class.java)


            if (usermodel == null) {
                // 아무도 팔로잉 하지 않았을 경우
                usermodel = UserModel()
                usermodel.userName = auth?.currentUser?.email!!
                //usermodel.imageURl=urlReturn()

            }
            else {
                usermodel.ProfileImageUrl=urlReturn()
            }
            transaction.set(tsDocName, usermodel)
            return@runTransaction
        }

    }
    fun urlReturn(): String {
        var surl:String=""
        imageprofileListenerRegistration = firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot?.data != null) {


                    if(uid==currentUseruid) {
                        var url = documentSnapshot!!.data!!["image"]
                        surl=url.toString()
                        Log.v("태그", url.toString())
                        var tsDocName = firestore!!.collection("users1").document(currentUseruid!!)

                        firestore?.runTransaction { transaction ->
                            var usermodel = transaction.get(tsDocName).toObject(UserModel::class.java)
                            usermodel = UserModel()
                            usermodel.userName = auth?.currentUser?.email!!
                            usermodel.ProfileImageUrl=surl
                            transaction.set(tsDocName, usermodel)
                        }
                    }
                }
            }
        return surl
    }


}

