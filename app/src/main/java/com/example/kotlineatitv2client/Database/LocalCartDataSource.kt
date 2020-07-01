package com.example.kotlineatitv2client.Database

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalCartDataSource(private val cartDao:CartDAO):CartDataSource {
    override fun getAllCart(uid: String): Flowable<List<CartItem>> {
        return cartDao.getAllCart(uid)
    }

    override fun countItemInCart(uid: String): Single<Int> {
        return cartDao.countItemInCart(uid)
    }

    override fun sumPrice(uid: String): Single<Double> {
        return cartDao.sumPrice(uid)
    }

    override fun getItemInCart(foodId: String, uid: String): Single<CartItem> {
        return cartDao.getItemInCart(foodId,uid)
    }

    override fun insertOrReplaceAll(vararg cartItem: CartItem): Completable {
        return  cartDao.insertOrReplaceAll(*cartItem)
    }

    override fun updateCart(cart: CartItem): Single<Int> {
       return cartDao.updateCart(cart)
    }

    override fun deleteCart(cart: CartItem): Single<Int> {
        return cartDao.deleteCart(cart)
    }

    override fun clearCart(uid: String): Single<Int> {
        return cartDao.clearCart(uid)
    }

    override fun getItemWithAllOptionsInCart(
        uid: String,
        foodId: String,
        foodSize: String,
        foodAddon: String
    ): Single<CartItem> {
        return cartDao.getItemWithAllOptionsInCart(uid,foodId,foodSize,foodAddon)
    }
}