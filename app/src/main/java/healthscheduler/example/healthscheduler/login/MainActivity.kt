package healthscheduler.example.healthscheduler.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import healthscheduler.example.healthscheduler.R
import healthscheduler.example.healthscheduler.activity.HomeActivity
import healthscheduler.example.healthscheduler.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val auth                                     = Firebase.auth
    private val currentUser                              = auth.currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        buttonsActions(binding)
        imageViewActions(binding)

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, options)

    }

    //Funcao com as acoes dos botoes
    private fun buttonsActions(binding: ActivityMainBinding){
        binding.buttonContinueWithEmailMain.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.imageViewRegistarMain.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    //Funcao com as acoes das imageViews
    private fun imageViewActions(binding: ActivityMainBinding){
        binding.imageViewGoogleMain.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("Google", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                Log.w("Google", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("", "loginSuccess!")
                        val user = auth.currentUser
                        val intent = Intent(this, HomeActivity::class.java)
                        //intent.putExtra("emailOrPhone", user?.email)
                        startActivity(intent)
                    } else {
                        Log.w("", "loginFailed! Info = ", task.exception)

                        Toast.makeText(baseContext, "Falha ao entrar na conta.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    companion object {
        val TAG = "MainActivity"
        private const val REQUEST_CODE_SIGN_IN = 9001
    }
}
