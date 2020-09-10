package com.example.howlstagram

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_grid.view.*
class GridFragment : Fragment() {

    var mainView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_grid, container, false)
        //var recycleView = mainView?.findViewById<RecyclerView>(R.id.gridfragment_recyclerview)
        mainView?.gridfragment_recyclerview?.adapter = GridFragmentRsecyclerviewAdapter()
        mainView?.gridfragment_recyclerview?.layoutManager = GridLayoutManager(activity, 3)
        return mainView
    }

    inner class GridFragmentRsecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO>

        init {
            contentDTOs = ArrayList()
            FirebaseFirestore.getInstance().collection("images").orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    for (snapshot in querySnapshot!!.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)

                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels /3
            var ImageView = ImageView(parent.context)
            ImageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)


            return CustomViewHolder(ImageView)
        }

        private inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imagerUrl)
                .apply(RequestOptions().centerCrop()).into(imageView)
            imageView.setOnClickListener {
                val fragment = UserFragment()
                val bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)

                fragment.arguments = bundle

                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
        }

    }


}