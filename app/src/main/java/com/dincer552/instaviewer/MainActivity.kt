package com.dincer552.instaviewer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dincer552.instaviewer.util.DiagnosticsLog
import com.dincer552.instaviewer.util.LinkValidator
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DiagnosticsLog.add(this, "APP_START", "LinkViewer başlatıldı; Android ${android.os.Build.VERSION.RELEASE}, SDK ${android.os.Build.VERSION.SDK_INT}")

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
        try {
            if (navController.currentDestination == null) navController.setGraph(R.navigation.nav_graph)
        } catch (e: Exception) {
            DiagnosticsLog.add(this, "NAV_GRAPH_ERROR", "Navigation graph kurulamadı", e)
            navController.setGraph(R.navigation.nav_graph)
        }

        val appBar = AppBarConfiguration(setOf(R.id.home_fragment, R.id.media_fragment, R.id.downloads_fragment, R.id.favorites_fragment, R.id.settings_fragment))
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBar)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == MENU_DIAGNOSTICS) { showDiagnostics(); true } else false
        }
        toolbar.menu.add(Menu.NONE, MENU_DIAGNOSTICS, Menu.NONE, "Loglar")
            .setIcon(android.R.drawable.ic_menu_info_details)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            DiagnosticsLog.add(this, "NAVIGATION", "Ekran: ${resources.getResourceEntryName(destination.id)}")
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DiagnosticsLog.add(this, "NEW_INTENT", "Uygulamaya yeni bağlantı geldi; action=${intent.action}")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data = intent.dataString
        when {
            action == Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                val url = if (!text.isNullOrBlank()) LinkValidator.extractUrl(text) else null
                DiagnosticsLog.add(this, "SHARE_INTENT", "Paylaşım alındı; valid=${url != null}; ${url?.let(DiagnosticsLog::safeUrl) ?: "URL bulunamadı"}")
                if (url != null) navigateToPlayer(url)
            }
            action == Intent.ACTION_VIEW && data != null -> {
                val valid = LinkValidator.isValidLink(data)
                DiagnosticsLog.add(this, "VIEW_INTENT", "VIEW alındı; valid=$valid; ${DiagnosticsLog.safeUrl(data)}")
                if (valid) navigateToPlayer(data)
            }
            else -> DiagnosticsLog.add(this, "INTENT_IGNORED", "action=$action; dataPresent=${data != null}")
        }
    }

    private fun navigateToPlayer(url: String) {
        DiagnosticsLog.add(this, "NAVIGATE_PLAYER", "Medya ekranına geçiş: ${DiagnosticsLog.safeUrl(url)}")
        val bundle = Bundle().apply { putString("url", url) }
        val options = NavOptions.Builder().setLaunchSingleTop(true).setEnterAnim(android.R.anim.fade_in).setExitAnim(android.R.anim.fade_out).build()
        if (navController.currentDestination == null) navController.setGraph(R.navigation.nav_graph)
        try {
            if (navController.currentDestination?.id == R.id.home_fragment) navController.navigate(R.id.action_navigation_home_to_player, bundle, options)
            else navController.navigate(R.id.player_fragment, bundle, options)
            DiagnosticsLog.add(this, "NAVIGATE_PLAYER_OK", "Medya ekranına geçiş komutu verildi")
        } catch (e: Exception) {
            DiagnosticsLog.add(this, "NAVIGATE_PLAYER_ERROR", "Medya ekranına geçiş başarısız", e)
            try {
                navController.navigate(R.id.home_fragment)
                navController.navigate(R.id.player_fragment, bundle, options)
            } catch (fallback: Exception) {
                DiagnosticsLog.add(this, "NAVIGATE_FALLBACK_ERROR", "Yedek geçiş de başarısız", fallback)
            }
        }
    }

    private fun showDiagnostics() {
        val text = TextView(this).apply {
            this.text = DiagnosticsLog.read(this@MainActivity)
            textSize = 11f
            setTextIsSelectable(true)
            setPadding(20, 12, 20, 12)
        }
        val scroll = ScrollView(this).apply { addView(text) }
        AlertDialog.Builder(this).setTitle("Tanılama kayıtları").setView(scroll)
            .setPositiveButton("Paylaş / gönder") { _, _ ->
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "LinkViewer tanılama logları")
                    putExtra(Intent.EXTRA_TEXT, DiagnosticsLog.read(this@MainActivity))
                }
                startActivity(Intent.createChooser(send, "Logları gönder"))
            }
            .setNeutralButton("Temizle") { _, _ ->
                DiagnosticsLog.clear(this)
                DiagnosticsLog.add(this, "LOG_CLEARED", "Kullanıcı logları temizledi")
            }
            .setNegativeButton("Kapat", null).show()
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()

    companion object { private const val MENU_DIAGNOSTICS = 9001 }
}