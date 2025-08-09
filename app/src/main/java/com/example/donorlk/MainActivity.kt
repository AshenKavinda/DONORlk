package com.example.donorlk

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.controllers.AdminDashboardController
import com.example.donorlk.controllers.HomePageController
import com.example.donorlk.controllers.LoginController
import com.example.donorlk.controllers.OperatorDashboardController
import com.example.donorlk.controllers.SubAdminHomeController
import com.example.donorlk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth, Firestore and SharedPreferences
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("DONORlk_prefs", MODE_PRIVATE)

        // Add a small delay to ensure Firebase Auth is fully initialized
        // This is especially important when app is killed and restarted
        Handler(Looper.getMainLooper()).postDelayed({
            checkExistingLogin()
        }, 500) // 500ms delay to ensure Firebase Auth state is restored
    }

    private fun checkExistingLogin() {
        val currentUser = auth.currentUser
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        val savedUserRole = sharedPreferences.getString("user_role", "")

        Log.d("MainActivity", "Checking login state:")
        Log.d("MainActivity", "Firebase user: ${currentUser?.email}")
        Log.d("MainActivity", "SharedPrefs logged in: $isLoggedIn")
        Log.d("MainActivity", "Saved role: $savedUserRole")

        if (currentUser != null && isLoggedIn && !savedUserRole.isNullOrEmpty()) {
            // User is logged in with Firebase and we have saved role
            Log.d("MainActivity", "User already logged in, redirecting to ${savedUserRole}")
            redirectBasedOnRole(savedUserRole)
        } else if (currentUser != null) {
            // User is logged in with Firebase but no saved login state
            // This can happen after app is killed from recent apps
            Log.d("MainActivity", "Firebase user found but no saved state, fetching role from Firestore")
            fetchUserRoleAndRedirect(currentUser.uid)
        } else if (isLoggedIn) {
            // SharedPrefs says logged in but no Firebase user - clear invalid state
            Log.d("MainActivity", "Invalid login state detected, clearing and redirecting to login")
            clearLoginState()
            redirectToLogin()
        } else {
            // User not logged in, go to login screen
            Log.d("MainActivity", "User not logged in, redirecting to login")
            redirectToLogin()
        }
    }

    private fun fetchUserRoleAndRedirect(uid: String) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    val role = user?.role?.lowercase() ?: "donator"

                    // Save the role for future use
                    saveLoginState(true, role, uid, auth.currentUser?.email, auth.currentUser?.displayName)
                    redirectBasedOnRole(role)
                } else {
                    // User doc doesn't exist, default to donator
                    saveLoginState(true, "donator", uid, auth.currentUser?.email, auth.currentUser?.displayName)
                    redirectBasedOnRole("donator")
                }
            }
            .addOnFailureListener { e ->
                Log.w("MainActivity", "Error getting user document", e)
                // On error, clear login state and go to login
                clearLoginState()
                redirectToLogin()
            }
    }

    private fun redirectBasedOnRole(role: String) {
        val intent = when (role.lowercase()) {
            "donator" -> Intent(this, HomePageController::class.java)
            "sub admin" -> Intent(this, SubAdminHomeController::class.java)
            "super admin" -> Intent(this, AdminDashboardController::class.java)
            "operator" -> Intent(this, OperatorDashboardController::class.java)
            else -> Intent(this, HomePageController::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginController::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveLoginState(isLoggedIn: Boolean, userRole: String, uid: String? = null, email: String? = null, name: String? = null) {
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", isLoggedIn)
            putString("user_role", userRole.lowercase())
            uid?.let { putString("user_uid", it) }
            email?.let { putString("user_email", it) }
            name?.let { putString("user_name", it) }
            apply()
        }
        Log.d("MainActivity", "Login state saved: $isLoggedIn, role: $userRole")
    }

    private fun clearLoginState() {
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", false)
            remove("user_role")
            remove("user_uid")
            remove("user_email")
            remove("user_name")
            apply()
        }
        Log.d("MainActivity", "Login state cleared")
    }
}