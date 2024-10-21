package com.example.contactguard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.contactguard.navigation.CONTACT
import com.example.contactguard.navigation.UNSAVED
import java.util.ArrayList

class ViewPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {

    private val identifiers = ArrayList<String>()
    override fun getItemCount(): Int {
        return identifiers.size
    }

    override fun createFragment(position: Int): Fragment {

        return when(identifiers[position]){
            CONTACT -> HomeFragment()
            UNSAVED -> UnsavedFragment()
            else-> Fragment()
        }
    }

    fun addIdentifiers(identifiers: List<String>) {
        this.identifiers.clear()
        this.identifiers.addAll(identifiers)
    }
}