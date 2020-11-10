package healthscheduler.example.healthscheduler.Login

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import healthscheduler.example.healthscheduler.Home
import healthscheduler.example.healthscheduler.R
import healthscheduler.example.healthscheduler.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var mGoogleSignInClient: GoogleSignInClient? = null

    internal lateinit var myDialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        binding.buttonLogin.setOnClickListener {
            signInWithEmailAndPassword(binding)
        }

        binding.textViewRecoveryPasswordLogin.setOnClickListener{

            myDialog = Dialog(this)
                myDialog.setContentView(R.layout.popwindow)
                myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

                myDialog.findViewById<Button>(R.id.buttonEnviarPop).setOnClickListener {
                    /* Toast.makeText(this@Login, "Clicou no botao",
                            Toast.LENGTH_SHORT).show()*/

                    val emailAddress = myDialog.findViewById<TextView>(R.id.editTextRecoveryEmailPop).text.toString()

                    Firebase.auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@Login, "Email enviado com sucesso",
                                            Toast.LENGTH_SHORT).show()
                                    myDialog.dismiss()
                                }else{
                                    Toast.makeText(this@Login, "Falha ao enviar o email",
                                            Toast.LENGTH_SHORT).show()
                                }
                            }
                }

            myDialog.show()

            /*val mDialogView = LayoutInflater.from(this).inflate(R.layout.popwindow, null)

            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)

            mBuilder.show()

            mDialogView.buttonEnviarPop.setOnClickListener {

               val emailAddress = mDialogView.editTextRecoveryEmailPop.text.toString()

            }*/
        }
    }

    private fun signInWithEmailAndPassword(binding : ActivityLoginBinding) {
        auth.signInWithEmailAndPassword(binding.editTextEmailLogin.text.toString(), binding.editTextPasswordLogin.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, Home::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@Login, "Falha ao entrar na conta.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}

/* << --------------------------------------- COMENTÁRIOS --------------------------------------- >>

--> Esconder a barra de cima e as setas que estão em baixo

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

*/
