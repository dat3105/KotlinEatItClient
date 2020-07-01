package com.example.kotlineatitv2client.ui.fooddetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Database.CartDataSource
import com.example.kotlineatitv2client.Database.CartDatabase
import com.example.kotlineatitv2client.Database.CartItem
import com.example.kotlineatitv2client.Database.LocalCartDataSource
import com.example.kotlineatitv2client.EventBus.CountCartEvent
import com.example.kotlineatitv2client.Model.CommentModel
import com.example.kotlineatitv2client.Model.FoodModel
import com.example.kotlineatitv2client.R
import com.example.kotlineatitv2client.ui.comment.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder


class FoodDetailFragment : Fragment(), TextWatcher {

    private lateinit var foodDetailViewModel: FoodDetailViewModel
    private lateinit var addonBottomSheetDialog: BottomSheetDialog

    private var img_food: ImageView?=null
    private var btnCart:CounterFab?=null
    private var btnRating:FloatingActionButton?=null
    private var food_name:TextView?=null
    private var food_description:TextView?=null
    private var food_price:TextView?=null
    private var number_button:ElegantNumberButton?=null
    private var ratingBar:RatingBar?=null
    private var btnShowComment: Button?=null
    private var rdi_group_size:RadioGroup?=null
    private var img_add_on:ImageView?=null
    private var chip_group_user_selected_addon:ChipGroup?=null

    private var chip_group_addon: ChipGroup?=null
    private var edt_search_addon:EditText?=null

    private var waitingDialog:AlertDialog?=null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource:CartDataSource

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel = ViewModelProvider(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_detail,container,false)
            initView(root)
        foodDetailViewModel.getMutableLiveDataFood().observe(viewLifecycleOwner, Observer {
            displayinfo(it)
        })
        foodDetailViewModel.getMutableLiveDataComment().observe(viewLifecycleOwner, Observer {
            submitRatingToFirebase(it)
        })
        return root
    }

    private fun submitRatingToFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener{task ->
                if(task.isSuccessful)
                {
                    addRatingToFood(commentModel!!.ratingValue)
                }
                waitingDialog!!.dismiss()
            }
    }

    private fun addRatingToFood(ratingValue: Float) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .child("foods")
            .child(Common.foodSelected!!.key!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(context!!, ""+error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        val foodModel = snapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key
                        val sumRating = foodModel.ratingValue+ ratingValue
                        val ratingCount = foodModel.ratingCount+1
                        val result = sumRating/ratingCount

                        val updateData = HashMap<String,Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue=  result

                        snapshot.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener{task ->
                                waitingDialog!!.dismiss()
                                if(task.isSuccessful)
                                {
                                    Common.foodSelected = foodModel
                                    foodDetailViewModel!!.setFoodModel(foodModel)
                                    Toast.makeText(context!!, "Thank you", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    else
                    {
                        waitingDialog!!.dismiss()
                    }
                }

            })
    }

    private fun displayinfo(it: FoodModel?) {
        Glide.with(requireContext()).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it!!.name!!)
        food_description!!.text = StringBuilder(it!!.description)
        food_price!!.text = StringBuilder(it!!.price.toString())

        ratingBar!!.rating = it!!.ratingValue.toFloat()

        for(sizeModel in it!!.size)
        {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener{compoundButton, b ->
                if(b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f)
            radioButton.layoutParams =params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }
        if(rdi_group_size!!.childCount>0)
        {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price!!.toDouble()
        var displayPrice = 0.0

        if(Common.foodSelected!!.userSelectedAddon!=null && Common.foodSelected!!.userSelectedAddon!!.size>0)
        {
            for(addonModel in Common.foodSelected!!.userSelectedAddon!!)
                totalPrice+= addonModel.price!!.toDouble()
        }

        totalPrice += Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()

        displayPrice = totalPrice * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0)/100.0

        food_price!!.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initView(root: View?) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        addonBottomSheetDialog = BottomSheetDialog(requireContext(),R.style.DialogStyle)
            val layout_user_select_addon = layoutInflater.inflate(R.layout.layout_addon_display,null)
        chip_group_addon = layout_user_select_addon.findViewById(R.id.chip_group_addon)
        edt_search_addon = layout_user_select_addon.findViewById(R.id.edt_search)
        addonBottomSheetDialog.setContentView(layout_user_select_addon)

        addonBottomSheetDialog.setOnDismissListener{dialogInterface ->
            displayUserSelectedAddon()
            calculateTotalPrice()
        }

        waitingDialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        btnCart = root!!.findViewById(R.id.btnCart)
        img_food = root!!.findViewById(R.id.img_food)
        btnRating = root!!.findViewById(R.id.btn_rating)
        food_name = root!!.findViewById(R.id.food_name)
        food_description = root!!.findViewById(R.id.food_description)
        food_price = root!!.findViewById(R.id.food_price)
        number_button = root!!.findViewById(R.id.number_button)
        ratingBar = root!!.findViewById(R.id.ratingBar)
        btnShowComment = root!!.findViewById(R.id.btnShowComment)
        rdi_group_size = root!!.findViewById(R.id.rdi_group_size)
        img_add_on = root!!.findViewById(R.id.img_add_addon)
        chip_group_user_selected_addon = root!!.findViewById(R.id.chip_group_user_selected_addon)

        img_add_on!!.setOnClickListener{
            if(Common.foodSelected!!.addon != null)
            {
                displayAllAddon()
                addonBottomSheetDialog.show()
            }
        }

        btnRating!!.setOnClickListener{
            showDialogRating()
        }

        btnShowComment!!.setOnClickListener{
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager,"CommentFragment")
        }

        btnCart!!.setOnClickListener{
            val cartItem= CartItem()
            cartItem.uid = Common.currentUser!!.uid
            cartItem.userPhone = Common.currentUser!!.phone

            cartItem.foodId = Common.foodSelected!!.id!!
            cartItem.foodName = Common.foodSelected!!.name!!
            cartItem.foodImage = Common.foodSelected!!.image!!
            cartItem.foodPrice = Common.foodSelected!!.price!!.toDouble()
            cartItem.foodQuantity=number_button!!.number.toInt()
            cartItem.foodExtraPrice=Common.calculateExtraPrice(Common.foodSelected!!.userSelectedSize,Common.foodSelected!!.userSelectedAddon)
            if(Common.foodSelected!!.userSelectedAddon !=null)
                cartItem.foodAddon = Gson().toJson(Common.foodSelected!!.userSelectedAddon)
            else
                cartItem.foodAddon="Default"
            if(Common.foodSelected!!.userSelectedSize !=null)
                cartItem.foodSize = Gson().toJson(Common.foodSelected!!.userSelectedSize)
            else
                cartItem.foodSize = "Default"

            cartDataSource.getItemWithAllOptionsInCart(Common.currentUser!!.uid!!,
                cartItem.foodId,
                cartItem.foodSize!!,
                cartItem.foodAddon!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<CartItem> {
                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if(cartItemFromDB.equals(cartItem))
                        {
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice;
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity = cartItemFromDB.foodQuantity+cartItem.foodQuantity

                            cartDataSource.updateCart(cartItemFromDB)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(context, "Update Cart Success", Toast.LENGTH_SHORT).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(context, "[UPDATE CART]"+e.message, Toast.LENGTH_SHORT).show()
                                    }

                                })
                        }
                        else
                        {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                },{
                                        t:Throwable? ->
                                    Toast.makeText(context, "[INSERT CARD]", Toast.LENGTH_SHORT).show()
                                })
                            )
                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        if(e.message!!.contains("empty"))
                        {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                },{
                                        t:Throwable? ->
                                    Toast.makeText(context, "[INSERT CARD]", Toast.LENGTH_SHORT).show()
                                })
                            )
                        }else
                            Toast.makeText(context, "[CART ERROR]"+e.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }

    }

    private fun displayAllAddon() {
        if(Common.foodSelected!!.addon.size>0)
        {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()

            edt_search_addon!!.addTextChangedListener(this)

            for(addonModel in Common.foodSelected!!.addon)
            {

                    val chip = layoutInflater.inflate(R.layout.layout_chip,null,false) as Chip
                    chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price).append(")").toString()
                    chip.setOnCheckedChangeListener{compoundButton, b ->
                        if(b){
                            if(Common.foodSelected!!.userSelectedAddon == null)
                                Common.foodSelected!!.userSelectedAddon = ArrayList()
                            Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                        }
                    }
                    chip_group_addon!!.addView(chip)

            }
        }
    }

    private fun displayUserSelectedAddon() {
        if(Common.foodSelected!!.userSelectedAddon!=null && Common.foodSelected!!.userSelectedAddon!!.size>0)
        {
            chip_group_user_selected_addon!!.removeAllViews()
            for(addonModel in Common.foodSelected!!.userSelectedAddon!!)
            {
                val chip = layoutInflater.inflate(R.layout.layout_chip_with_delete,null,false) as Chip
                chip.text = StringBuilder(addonModel!!.name!!).append("(+$").append(addonModel.price).append(")").toString()
                chip.isClickable = false
                chip.setOnClickListener { view ->
                    chip_group_user_selected_addon!!.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addon!!.addView(chip)
            }
        }else if(Common.foodSelected!!.userSelectedAddon!!.size ==0){
            chip_group_user_selected_addon!!.removeAllViews()
        }
    }

    private fun showDialogRating() {
        var builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rating Food")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment,null)

        val ratingBar = itemView.findViewById<RatingBar>(R.id.rating_bar)
        val edt_comment = itemView.findViewById<EditText>(R.id.edt_comment)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL"){dialogInterface, i -> dialogInterface.dismiss()  }
        builder.setPositiveButton("OK"){dialogInterface, i ->
            val commentModel = CommentModel()
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edt_comment.text.toString()
            commentModel.ratingValue= ratingBar.rating
            val serverTimeStamp =HashMap<String,Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp=(serverTimeStamp)

            foodDetailViewModel!!.setCommentModel(commentModel)
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(charSquence: CharSequence?, p1: Int, p2: Int, p3: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()
        for(addonModel in Common.foodSelected!!.addon)
        {
            if(addonModel.name!!.toLowerCase().contains(charSquence.toString().toLowerCase()))
            {
                val chip = layoutInflater.inflate(R.layout.layout_chip,null,false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price).append(")").toString()
                chip.setOnCheckedChangeListener{compoundButton, b ->
                if(b){
                    if(Common.foodSelected!!.userSelectedAddon == null)
                        Common.foodSelected!!.userSelectedAddon = ArrayList()
                    Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }


}