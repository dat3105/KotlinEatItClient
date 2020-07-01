package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.CategoryModel

interface ICategoryCallbackListener {
    fun onCategoryLoadSuccess(popularModelList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}