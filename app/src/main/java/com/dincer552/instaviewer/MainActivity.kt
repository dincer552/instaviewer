package com.dincer552.instaviewer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dincer552.instaviewer.util.LinkValidator
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Defensive: ensure graph is attached
        try {
            if (navController.currentDestination == null) {
                navController.setGraph(R.navigation.nav_graph)
            }
        } catch (_: Exception) {
            navController.setGraph(R.navigation.nav_graph)
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.home_fragment,
                R.id.media_fragment,
                R.id.downloads_fragment,
                R.id.favorites_fragment,
                R.id.settings_fragment
            )
        )

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Official helper keeps selected state, back-stack and singleTop behaviour correct
        bottomNavigation.setupWithNavController(navController)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val data = intent.dataString

        when {
            action == Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrBlank()) {
                    val url = LinkValidator.extractUrl(text)
                    if (url != null) {
                        navigateToPlayer(url)
                    }
                }
            }

            action == Intent.ACTION_VIEW && data != null -> {
                if (LinkValidator.isValidLink(data)) {
                    navigateToPlayer(data)
                }
            }
        }
    }

    private fun navigateToPlayer(url: String) {
        val bundle = Bundle().apply { putString("url", url) }
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(android.R.anim.fade_in)
            .setExitAnim(android.R.anim.fade_out)
            .build()

        // Ensure we have a current destination before navigating
        if (navController.currentDestination == null) {
            navController.setGraph(R.navigation.nav_graph)
        }

        try {
            when (navController.currentDestination?.id) {
                R.id.home_fragment -> {
                    navController.navigate(R.id.action_navigation_home_to_player, bundle, options)
                }
                else -> {
                    // From any other tab: go to player directly
                    navController.navigate(R.id.player_fragment, bundle, options)
                }
            }
        } catch (e: IllegalStateException) {
            // Last resort – graph may still be initializing
            navController.navigate(R.id.home_fragment)
            navController.navigate(R.id.player_fragment, bundle, options)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
