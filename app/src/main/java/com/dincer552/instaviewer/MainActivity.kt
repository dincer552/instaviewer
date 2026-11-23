package com.dincer552.instaviewer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dincer552.instaviewer.util.LinkValidator

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.home_fragment,
            R.id.media_fragment,
            R.id.downloads_fragment,
            R.id.favorites_fragment,
            R.id.settings_fragment
        ))

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottom_navigation
        )
        bottomNavigation.setOnMenuItemSelectedListener { item ->
            val destinationId = item.itemId
            if (destinationId != navController.currentDestination?.id) {
                navController.navigate(destinationId)
            }
            true
        }

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
        navController.navigate(R.id.action_navigation_home_to_player, bundle)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}