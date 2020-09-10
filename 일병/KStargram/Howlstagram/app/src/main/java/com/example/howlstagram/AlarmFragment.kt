package com.example.howlstagram

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.model.AlarmDTO
import com.example.howlstagram.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
class AlarmFragment : Fragment() {

    var alarmSnapshot: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerViewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val alarmDTOList = ArrayList<AlarmDTO>()
        var contentDTOs: ArrayList<ContentDTO>
        init {

            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            println(uid)
            FirebaseFirestore.getInstance()
                .collection("alarms")
                .whereEqualTo("destinationUid", uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmDTOList.clear()
                    if(querySnapshot == null)return@addSnapshotListener
                    for (snapshot in querySnapshot?.documents!!) {
                        alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                    }
                    alarmDTOList.sortByDescending { it.timestamp }
                    notifyDataSetChanged()
                }
            contentDTOs = ArrayList()
            FirebaseFirestore.getInstance().collection("images").orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    for (snapshot in querySnapshot!!.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)

                    }
                    notifyDataSetChanged()
                }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val profileImage = holder.itemView.commentviewItem_Imageview_profile
            val commentTextView = holder.itemView.commentviewItem_textview_comment
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            if (!alarmDTOList[position].uid!!.equals(uid)) {
                FirebaseFirestore.getInstance().collection("profileImages")
                    .document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->

                        Log.v("실행", "실행")

                        if (task.isSuccessful) {
                            val url = task.result["image"]
                            if (!alarmDTOList[position].uid!!.equals(uid)) {
                                Glide.with(activity!!)
                                    .load(url)
                                    .apply(RequestOptions().circleCrop())
                                    .into(profileImage)
                                Log.v("실행", "실행2")
                            }
                        }
                            profileImage.setOnClickListener {
                                val fragment = UserFragment()
                                val bundle = Bundle()
                                bundle.putString("destinationUid", contentDTOs[position].uid)
                                bundle.putString("userId", contentDTOs[position].userId)

                                fragment.arguments = bundle

                                activity?.supportFragmentManager?.beginTransaction()
                                    ?.replace(R.id.main_content, fragment)?.commit()
                            }

                    }

                    when (alarmDTOList[position].kind) {
                        0 -> {
                            val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                            commentTextView.text = str_0
                        }

                        1 -> {
                            val str_1 =
                                alarmDTOList[position].userId + getString(R.string.alarm_who) + alarmDTOList[position].message + getString(
                                    R.string.alarm_comment
                                )
                            commentTextView.text = str_1
                        }

                        2 -> {
                            val str_2 = alarmDTOList[position].userId + getString(R.string.alarm_follow)
                            commentTextView.text = str_2
                        }
                    }
            }
        }
        override fun getItemCount(): Int {

            return alarmDTOList.size
        }
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    }
}