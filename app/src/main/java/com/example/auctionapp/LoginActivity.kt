package com.example.auctionapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AWSMobileClient.getInstance().initialize(applicationContext, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails) {
                // Initialization successful
            }

            override fun onError(e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Initialization error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        val usernameInput = findViewById<TextInputEditText>(R.id.username_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input)
        val mfaInput = findViewById<TextInputEditText>(R.id.mfa_input)
        val signInButton = findViewById<MaterialButton>(R.id.sign_in_button)

        signInButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val mfaCode = mfaInput.text.toString()

            AWSMobileClient.getInstance().signIn(username, password, null, object : Callback<SignInResult> {
                override fun onResult(result: SignInResult) {
                    when (result.signInState) {
                        SignInState.DONE -> {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Sign in complete", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                        // SOFTWARE_TOKEN_MFA is not available in the version of the AWS SDK we are
                        // using. Handling SMS_MFA here covers MFA challenges without referencing the
                        // missing enum value and allows the project to compile.
                        SignInState.SMS_MFA -> {
                            if (mfaCode.isNotEmpty()) {
                                AWSMobileClient.getInstance().confirmSignIn(mfaCode, object : Callback<SignInResult> {
                                    override fun onResult(confirmResult: SignInResult) {
                                        if (confirmResult.signInState == SignInState.DONE) {
                                            runOnUiThread {
                                                Toast.makeText(this@LoginActivity, "Sign in complete", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                                finish()
                                            }
                                        }
                                    }

                                    override fun onError(e: Exception) {
                                        runOnUiThread {
                                            Toast.makeText(this@LoginActivity, "MFA error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                })
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@LoginActivity, "Please enter MFA code", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        else -> {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Sign in state: ${result.signInState}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                override fun onError(e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Sign in error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}

