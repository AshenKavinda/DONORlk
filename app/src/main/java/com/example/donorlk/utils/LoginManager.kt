package com.example.donorlk.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.donorlk.controllers.AdminDashboardController
import com.example.donorlk.controllers.HomePageController
import com.example.donorlk.controllers.LoginController
import com.example.donorlk.controllers.OperatorDashboardController
import com.example.donorlk.controllers.SubAdminHomeController
import com.google.firebase.auth.FirebaseAuth

class LoginManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("DONORlk_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val PREF_IS_LOGGED_IN = "is_logged_in"
        private const val PREF_USER_ROLE = "user_role"
        private const val PREF_USER_UID = "user_uid"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_NAME = "user_name"
    }

    /**
     * Save user login state and information
     */
    fun saveLoginState(
        isLoggedIn: Boolean,
        userRole: String,
        uid: String? = null,
        email: String? = null,
        name: String? = null
    ) {
        sharedPreferences.edit().apply {
            putBoolean(PREF_IS_LOGGED_IN, isLoggedIn)
            putString(PREF_USER_ROLE, userRole.lowercase())
            uid?.let { putString(PREF_USER_UID, it) }
            email?.let { putString(PREF_USER_EMAIL, it) }
            name?.let { putString(PREF_USER_NAME, it) }
            apply()
        }
        Log.d("LoginManager", "Login state saved: $isLoggedIn, role: $userRole")
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        val sharedPrefLoggedIn = sharedPreferences.getBoolean(PREF_IS_LOGGED_IN, false)
        val firebaseLoggedIn = auth.currentUser != null
        return sharedPrefLoggedIn && firebaseLoggedIn
    }

    /**
     * Get saved user role
     */
    fun getUserRole(): String? {
        return sharedPreferences.getString(PREF_USER_ROLE, null)
    }

    /**
     * Get saved user information
     */
    fun getUserInfo(): Map<String, String?> {
        return mapOf(
            "uid" to sharedPreferences.getString(PREF_USER_UID, null),
            "email" to sharedPreferences.getString(PREF_USER_EMAIL, null),
            "name" to sharedPreferences.getString(PREF_USER_NAME, null),
            "role" to sharedPreferences.getString(PREF_USER_ROLE, null)
        )
    }

    /**
     * Clear all login state
     */
    fun clearLoginState() {
        sharedPreferences.edit().apply {
            putBoolean(PREF_IS_LOGGED_IN, false)
            remove(PREF_USER_ROLE)
            remove(PREF_USER_UID)
            remove(PREF_USER_EMAIL)
            remove(PREF_USER_NAME)
            apply()
        }
        Log.d("LoginManager", "Login state cleared")
    }

    /**
     * Perform complete logout
     */
    fun performLogout() {
        try {
            // Clear SharedPreferences first
            clearLoginState()

            // Sign out from Firebase Auth
            auth.signOut()

            Log.d("LoginManager", "Logout completed successfully")
        } catch (e: Exception) {
            Log.e("LoginManager", "Error during logout", e)
            throw e
        }
    }

    /**
     * Navigate to appropriate screen based on user role
     */
    fun navigateBasedOnRole(role: String) {
        val intent = when (role.lowercase()) {
            "donator" -> Intent(context, HomePageController::class.java)
            "sub admin" -> Intent(context, SubAdminHomeController::class.java)
            "super admin" -> Intent(context, AdminDashboardController::class.java)
            "operator" -> Intent(context, OperatorDashboardController::class.java)
            else -> Intent(context, HomePageController::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Navigate to login screen
     */
    fun navigateToLogin() {
        val intent = Intent(context, LoginController::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
