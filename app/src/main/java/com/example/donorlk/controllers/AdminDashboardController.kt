package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.CreateAdminActivity
import com.example.donorlk.R
import com.example.donorlk.utils.LoginManager
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardController : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initialize Firebase Auth and LoginManager
        auth = FirebaseAuth.getInstance()
        loginManager = LoginManager(this)

        val createAdminCard = findViewById<LinearLayout>(R.id.createAdminCard)
        val manageAdminCard = findViewById<LinearLayout>(R.id.manageAdminCard)
        val logoutContainer = findViewById<LinearLayout>(R.id.logoutContainer)

        createAdminCard.setOnClickListener {
            val intent = Intent(this, CreateAdminActivity::class.java)
            startActivity(intent)
        }

        manageAdminCard.setOnClickListener {
            val intent = Intent(this, EditAdminActivity::class.java)
            startActivity(intent)
        }

        logoutContainer.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun performLogout() {
        try {
            // Use LoginManager for complete logout
            loginManager.performLogout()

            // Show logout success message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login screen using LoginManager
            loginManager.navigateToLogin()
            finish()

        } catch (e: Exception) {
            // Handle any logout errors
            Toast.makeText(this, "Error during logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
