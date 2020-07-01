package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentList:List<CommentModel>)
    fun onCommentloadFailed(message:String)
}