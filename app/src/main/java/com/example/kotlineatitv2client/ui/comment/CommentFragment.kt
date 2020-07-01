package com.example.kotlineatitv2client.ui.comment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatitv2client.Adapter.MyCommentAdapter
import com.example.kotlineatitv2client.Callback.ICommentCallBack
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.CommentModel
import com.example.kotlineatitv2client.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog


class CommentFragment : BottomSheetDialogFragment(), ICommentCallBack {
    private var commentViewModel : CommentViewModel?=null

    private var listener:ICommentCallBack

    private var recycler_comment:RecyclerView?=null
    private var dialog:AlertDialog?=null

    init{
        listener = this

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comment_fragment,container,false)
        initViews(itemView)
        loadCommentFromFirebase()
        commentViewModel!!.mutableLiveDataCommentList.observe(this, Observer { commentList ->
            val adapter = MyCommentAdapter(requireContext(),commentList)
            recycler_comment!!.adapter=adapter
        })
        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()

        val commentModels = ArrayList<CommentModel>()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    listener.onCommentloadFailed(error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for(commentSnapShot in snapshot.children)
                    {
                        val commentModel = commentSnapShot.getValue<CommentModel>(CommentModel::class.java)
                        commentModels.add(commentModel!!)

                    }
                    listener.onCommentLoadSuccess(commentModels)
                }

            })
    }

    private fun initViews(itemView: View?) {
        commentViewModel = ViewModelProvider(this).get(CommentViewModel::class.java)
        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()

        recycler_comment = itemView!!.findViewById(R.id.recycler_comment)
        recycler_comment!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,true)
        recycler_comment!!.layoutManager = layoutManager
        recycler_comment!!.addItemDecoration(DividerItemDecoration(requireContext(),layoutManager.orientation))
    }


    override fun onCommentLoadSuccess(commentList: List<CommentModel>) {
        dialog!!.dismiss()
        commentViewModel!!.setCommentList(commentList)
    }

    override fun onCommentloadFailed(message: String) {
        Toast.makeText(requireContext(), ""+message, Toast.LENGTH_SHORT).show()
        dialog!!.dismiss()
    }

    companion object{
        private var instance:CommentFragment?=null

        fun getInstance():CommentFragment
        {
            if(instance == null)
                instance = CommentFragment()
            return instance!!
        }
    }

}