package com.example.kotlineatitv2client.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import com.example.kotlineatitv2client.Adapter.MyPopularCategoriesAdapter
import com.example.kotlineatitv2client.Adapter.MyBestDealsAdapter
import com.example.kotlineatitv2client.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel


    var recycler_popular: RecyclerView?=null
    var viewPager:LoopingViewPager?=null
    var layoutAnimationController:LayoutAnimationController?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)


        initView(root)
        homeViewModel.popularList.observe(viewLifecycleOwner, Observer {
            val listData=it
            val adapter = MyPopularCategoriesAdapter(requireContext(),listData)
            recycler_popular!!.adapter=adapter
            recycler_popular!!.layoutAnimation = layoutAnimationController
        })

        homeViewModel.bestDealList.observe(viewLifecycleOwner, Observer {
            val adapter =
                MyBestDealsAdapter(
                    requireContext(),
                    it,
                    false
                )
            viewPager!!.adapter = adapter
        })

        return root
    }

    private fun initView(root:View) {
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
        viewPager = root.findViewById(R.id.viewpager)
        recycler_popular=root.findViewById(R.id.recycler_popular)
        recycler_popular!!.setHasFixedSize(true)
        recycler_popular!!.layoutManager = LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
    }

    override fun onResume() {
        super.onResume()
        viewPager!!.resumeAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        viewPager!!.pauseAutoScroll()
    }
}