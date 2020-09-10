package com.example.howlstagram

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.model.AlarmDTO
import com.example.howlstagram.model.ContentDTO
import com.example.howlstagram.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailviewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var user: FirebaseAuth? = null
    var fcmPush: FcmPush? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()
        fcmPush = FcmPush()
        var view = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_detail, container, false)
        view.detailviewfragment_recycleryview.adapter = DetailRecycleryviewAdapter()
        view.detailviewfragment_recycleryview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailRecycleryviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDTOs: ArrayList<ContentDTO> //파이어베이스의문서추가를 담아주는애
        val contentUIdList: ArrayList<String>//파이어베이스의 컨텐트 컬렉션 필드가 여기에있다.

        init {
            contentDTOs = ArrayList()
            contentUIdList = ArrayList()

            var uid = FirebaseAuth.getInstance().currentUser?.uid
            //현재 로그인된 유저의 UID
            firestore?.collection("users")?.document(uid!!)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var userDTO = task.result.toObject(FollowDTO::class.java)
                    if (userDTO != null) {
                        getContents(userDTO.followings)
                    }

                }
            }


        }

        fun getContents(followers: MutableMap<String, Boolean>) {
            //firestore에 images에 접근한것.
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener
                    contentDTOs.clear()
                    contentUIdList.clear()
                    for (snapshot in querySnapshot!!.documents) {
                        //이미지를 하나씩 돌아가면서 uid를 하나씩 넣는 것이다.
                        var item = snapshot.toObject(ContentDTO::class.java)
                        if (followers.keys.contains(item?.uid)) {
                            contentDTOs.add(item!!)
                            contentUIdList.add(snapshot.id)
                        }
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //var view =LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val viewHolder = (holder as CustomViewHolder).itemView


            firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)?.get()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var url = task.result["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop())
                            .into(viewHolder.detailviewitem_profile_image)
                    }
                }
            //유저 아이디
            viewHolder.detailviewitem_profile_textview.text = contentDTOs[position].userId
            //이미지
            Glide.with(holder.itemView.context).load(contentDTOs[position].imagerUrl)
                .into(viewHolder.detailviewitem_imageview_content)
            //설명 텍스트

            viewHolder.detailviewitem_explain_textview.text = contentDTOs[position].explain
            //좋아요 카운터 설정

            viewHolder.detailviewitem_favoritescounter_textview.text =
                "좋아요 " + contentDTOs[position].favoriteCount + "개"
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }
            //좋아요 클릭했을경우
            if (contentDTOs[position].favorites.containsKey(uid)) {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }
            //좋아요를 클릭하지 않았을 경우
            else {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            viewHolder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
            }
            viewHolder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUIdList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }
        }

        private fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUIdList[position])
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser!!.uid
                var contentDTO = transaction.get(tsDoc!!).toObject((ContentDTO::class.java))

                //좋아요를 누른상태 - > 누르지 않은 상태
                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)
                }
                //좋아요를 누르지않은상태 ->  누른 상태
                else {
                    contentDTO.favorites[uid] = true
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                //만든값(contentDTO)를 set해주겠다 tsDoc에다가
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = user?.currentUser?.email
            alarmDTO.uid = user?.currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = user?.currentUser?.email + getString(R.string.alarm_favorite)
            fcmPush?.sendMessage(destinationUid, "알림 메세지 입니다", message)


        }
    }

}