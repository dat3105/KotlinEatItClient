package com.example.kotlineatitv2client.ui.foodlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.FoodModel

class FoodListViewModel : ViewModel() {

    private var mutableFoodModelListData: MutableLiveData<List<FoodModel>>?=null

    fun getMutableFoddModelListData():MutableLiveData<List<FoodModel>>{
        if(mutableFoodModelListData == null)
            mutableFoodModelListData = MutableLiveData()
        mutableFoodModelListData!!.value = Common.categorySelected!!.foods
        return mutableFoodModelListData!!
    }
}