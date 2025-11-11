package com.example.subsentry

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.subsentry.databinding.ActivityMainBinding
import com.example.subsentry.utils.SessionManager
import com.example.subsentry.work.NotificationWorker

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if (sessionManager.isLoggedIn()) {
//            NotificationWorker.scheduleWork(this) // Статический вызов
//        }


        sessionManager = SessionManager(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // ЭТО САМАЯ ВАЖНАЯ СТРОКА - без нее навигация не работает
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id == R.id.authFragment ||
                    destination.id == R.id.registerFragment

            binding.bottomNavigation.visibility = if (isAuthScreen) View.GONE else View.VISIBLE

            if (sessionManager.isLoggedIn() && isAuthScreen) {
                navController.navigate(R.id.mainFragment)
            }
        }

        if (sessionManager.isLoggedIn() &&
            (navController.currentDestination?.id == R.id.authFragment ||
                    navController.currentDestination?.id == R.id.registerFragment)) {
            navController.navigate(R.id.mainFragment)
        }
    }

    fun logout() {
        sessionManager.logout()
        navController.navigate(R.id.authFragment)
    }
}