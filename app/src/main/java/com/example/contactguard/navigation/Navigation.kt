package com.example.chatapplication.ui.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavInflater
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.contactguard.R

object Navigation {


    //this method helps in fragment to fragment navigation
    fun navigate(fragment: Fragment, popBackFragment: Int? = null, to: Int, bundle: Bundle? = null) {
        val navController = fragment.findNavController()
        val navOptionsBuilder = NavOptions.Builder()

        if (popBackFragment != null) {
            navOptionsBuilder.setPopUpTo(popBackFragment, true)
        }

        val navOptions = navOptionsBuilder.build()
        navController.navigate(to, bundle, navOptions)
    }

    //this method actually setup the activities navgraph destination
    //just pass the starting fragment reference
    fun setupNavGraph(activity: FragmentActivity,containerViewId: Int,startFragment: Int){
        val navHostFragment =
            activity.supportFragmentManager.findFragmentById(containerViewId) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        val navInflater: NavInflater = navController.navInflater
        val navGraph: NavGraph = navInflater.inflate(R.navigation.nav_graph)

        navGraph.setStartDestination(startFragment)

        navController.graph = navGraph
    }
}
