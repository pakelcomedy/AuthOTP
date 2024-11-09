package com.pakelcomedy.authenotp.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    // Configure email and password for SMTP authentication
    private const val SMTP_EMAIL = "kontolbaladojoemama@gmail.com"  // Use your email
    private const val SMTP_PASSWORD = "vfzr alik piph qkry"  // Use your app-specific password

    // Function to send OTP email
    suspend fun sendOtpEmail(recipientEmail: String, otp: String, callback: (Boolean, String?) -> Unit) {
        // Run network operation in background thread using Dispatchers.IO
        withContext(Dispatchers.IO) {
            // SMTP server settings
            val properties = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")  // Gmail's SMTP server
                put("mail.smtp.port", "587")  // TLS port
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD)
                }
            })

            try {
                // Prepare email message
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SMTP_EMAIL))
                    addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                    subject = "Your OTP Code"
                    setText("Your OTP code is: $otp")
                }

                // Send email
                Transport.send(message)
                callback(true, "Email sent successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false, "Error sending email: ${e.message}")
            }
        }
    }
}