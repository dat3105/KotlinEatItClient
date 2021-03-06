package com.example.kotlineatitv2client.Common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import com.example.kotlineatitv2client.Model.*
import kotlinx.android.synthetic.main.fragment_food_detail.view.*
import java.lang.StringBuilder
import java.math.RoundingMode
import java.text.DecimalFormat

object Common {
    fun formatPrice(price: Double): String {
        if(price!=0.toDouble())
        {
            val df=DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".",",")
        }else{
            return "0,00"
        }
    }

    fun calculateExtraPrice(userSelectedSize: SizeModel?,
                            userSelectedAddon: MutableList<AddonModel>?
    ): Double {
        var result:Double=0.0
        if(userSelectedSize == null && userSelectedAddon == null)
            return 0.0
        else if(userSelectedSize ==null)
        {
            for(addonModel in userSelectedAddon!!)
                result += addonModel.price!!.toDouble()
            return result
        }
        else if(userSelectedAddon == null)
        {
            result = userSelectedSize!!.price.toDouble()
            return result
        }
        else
        {
            result = userSelectedSize!!.price.toDouble()
            for(addonModel in userSelectedAddon!!)
                result += addonModel.price!!.toDouble()
            return result
        }
    }

    fun setSpanString(wellcome: String, name: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(wellcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan,0,name!!.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder,TextView.BufferType.SPANNABLE)
    }

    val COMMENT_REF: String = "Comments"
    var foodSelected: FoodModel?=null
    var categorySelected: CategoryModel?=null
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int =1
    val DEFAULT_COLUMN_COUNT: Int=0
    val POPULAR_REF:String="MostPopular"
    val USER_REFERENCE="Users"
    var currentUser:UserModel?=null
    val BEST_DEALS_REF:String = "BestDeals"
}