package healthscheduler.example.healthscheduler.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import healthscheduler.example.healthscheduler.R
import healthscheduler.example.healthscheduler.databinding.ActivityHomeBinding
import healthscheduler.example.healthscheduler.models.DoctorsItem
import healthscheduler.example.healthscheduler.models.MessageItem
import healthscheduler.example.healthscheduler.models.UsersItem
import kotlinx.android.synthetic.main.popwindow_alertinternet.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private val REQUEST_CODE        =  0
    private val db                  = FirebaseFirestore.getInstance()
    private val storageRef          = Firebase.storage.reference
    private val auth                = Firebase.auth
    private val imagesRef           = storageRef.child("images/${UUID.randomUUID()}.jpg")
    private val currentUser         = auth.currentUser

    private var currentUserName:    String?           = null
    private var currentUserAddress: String?           = null
    private var downUrl:            String?           = null
    private var user:               UsersItem?        = null
    private var curFile:            Uri?              = null

    private var message:            MessageItem?      = null
    private var user1:              DoctorsItem?      = null
    private var user2:              DoctorsItem?      = null
    private var user3:              DoctorsItem?      = null

    private var refLatestMessages   = db.collection("latest_messages")
    private var referenceUsers      = db.collection("users")
    private var referenceUsersMedic = db.collection("users_medic")

    private var users:          MutableList<DoctorsItem> = arrayListOf()
    private var latestMessages: MutableList<MessageItem> = arrayListOf()

    private lateinit var myDialog:  Dialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToPhoneCall()
        }

        //Inicializações das funções
        checkConnection()
        textViewStyle(binding)
        textViewActions(binding)
        userData(binding)
        getUser()
        getUsersDoctors()
        getCountNotification()
        buttonsActions(binding)
    }

    private fun checkConnection() {
        val manager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo

        if (null != networkInfo){
            if(networkInfo.type == ConnectivityManager.TYPE_WIFI) {
            }else if(networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                //Toast.makeText(this, "Mobile Data Connected", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            val dialog = Dialog(this)
                dialog.setContentView(R.layout.popwindow_alertinternet)
                //USAR ISTO NOS OUTROS DIALOGS
                dialog.setCanceledOnTouchOutside(false)
                dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dialog.buttonTryAgainPopWindowAlert.setOnClickListener {
                    recreate()
                }
            dialog.show()

        }
    }

    private fun getUsersDoctors() {

        referenceUsersMedic.addSnapshotListener { snapshot, error ->
            users.clear()
            if (snapshot != null) {
                for (doc in snapshot) {
                    val user = DoctorsItem.fromHash(doc.data as HashMap<String, Any?>)
                    users.add(user)
                }
            }
        }
    }

    //Funcao que ve se o user tem dados, se nao tiver faz com que insira e os ultimos 3 users que falou
    private fun userData(binding: ActivityHomeBinding){
        currentUser?.uid.let {
            if (it != null) {
                db.collection("users")
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        if (querySnapshot != null) {
                            //users.clear()
                            for (doc in querySnapshot) {
                                if (doc.id == it) {
                                    user = UsersItem.fromHash(doc.data as HashMap<String, Any?>)
                                }
                            }
                            user?.let { item ->
                                if (item.imagePath == "") {
                                    item.userID = it
                                    binding.textViewUserNameHome.text = item.username
                                    binding.textViewUserNumberPhoneHome.text = item.phoneNumberEmail
                                    binding.textViewUserAddressHome.text = item.address
                                }
                                else {
                                    item.userID = it
                                    binding.textViewUserNameHome.text = item.username
                                    binding.textViewUserNumberPhoneHome.text = item.phoneNumberEmail
                                    binding.textViewUserAddressHome.text = item.address
                                    Picasso.get().load(item.imagePath).into(binding.imageViewUserPhotoHome)
                                }
                                refLatestMessages
                                    .document(it)
                                    .collection("latest_message")
                                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                                    .addSnapshotListener { snapshot, error ->
                                        latestMessages.clear()
                                        if (snapshot != null) {
                                            for (doc in snapshot) {
                                                message = MessageItem.fromHash(doc.data as HashMap<String, Any?>)
                                                latestMessages.add(message!!)
                                            }
                                            latestMessages.let {
                                                for ((i, message) in latestMessages.withIndex()) {
                                                    /*if (latestMessages.size == 0) {
                                                        binding.imageViewFavoriteUserPhoto1Home.visibility = View.GONE
                                                        binding.imageViewFavoriteUserPhoto2Home.visibility = View.GONE
                                                        binding.imageViewFavoriteUserPhoto3Home.visibility = View.GONE

                                                        binding.textViewFavoriteName1Home.visibility = View.GONE
                                                        binding.textViewFavoriteName2Home.visibility = View.GONE
                                                        binding.textViewFavoriteName3Home.visibility = View.GONE
                                                    }*/
                                                    if (latestMessages.size == 1) {
                                                        /*binding.imageViewFavoriteUserPhoto2Home.visibility = View.GONE
                                                        binding.imageViewFavoriteUserPhoto3Home.visibility = View.GONE

                                                        binding.textViewFavoriteName2Home.visibility = View.GONE
                                                        binding.textViewFavoriteName3Home.visibility = View.GONE*/
                                                        binding.imageViewFavoriteUserPhoto1Home.visibility = View.VISIBLE
                                                        binding.textViewFavoriteName1Home.visibility = View.VISIBLE

                                                        for (item1 in users) {
                                                            if (message.fromId == item1.medicID || message.toId == item1.medicID) {
                                                                when (i) {
                                                                    0 -> {
                                                                        user1 = item1
                                                                        binding.textViewFavoriteName1Home.text = "Dr. " + item1.username
                                                                        if (item1.imagePath != "") {
                                                                            Picasso.get().load(item1.imagePath).into(binding.imageViewFavoriteUserPhoto1Home)
                                                                        } else {
                                                                            binding.imageViewFavoriteUserPhoto1Home.setBackgroundResource(R.drawable.imageviewfotofavorito1)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    else if (latestMessages.size == 2) {

                                                        binding.imageViewFavoriteUserPhoto1Home.visibility = View.VISIBLE
                                                        binding.textViewFavoriteName1Home.visibility = View.VISIBLE

                                                        binding.imageViewFavoriteUserPhoto2Home.visibility = View.VISIBLE
                                                        binding.textViewFavoriteName2Home.visibility = View.VISIBLE
                                                        /*binding.imageViewFavoriteUserPhoto3Home.visibility = View.GONE

                                                        binding.textViewFavoriteName3Home.visibility = View.GONE*/

                                                        for (item1 in users) {
                                                            if (message.fromId == item1.medicID || message.toId == item1.medicID) {
                                                                when (i) {
                                                                    0 -> {
                                                                        user1 = item1
                                                                        binding.textViewFavoriteName1Home.text = "Dr. " + item1.username
                                                                        if (item1.imagePath != "") {
                                                                            Picasso.get().load(item1.imagePath).into(binding.imageViewFavoriteUserPhoto1Home)
                                                                        } else {
                                                                            binding.imageViewFavoriteUserPhoto1Home.setBackgroundResource(R.drawable.imageviewfotofavorito1)
                                                                        }
                                                                    }
                                                                    1 -> {
                                                                        user2 = item1
                                                                        binding.textViewFavoriteName2Home.text = "Dr. " + item1.username
                                                                        if (item1.imagePath != "") {
                                                                            Picasso.get().load(item1.imagePath).into(binding.imageViewFavoriteUserPhoto2Home)
                                                                        } else {
                                                                            binding.imageViewFavoriteUserPhoto1Home.setBackgroundResource(R.drawable.imageviewfotofavorito1)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    else if (latestMessages.size >= 3) {

                                                        binding.imageViewFavoriteUserPhoto1Home.visibility = View.VISIBLE
                                                        binding.imageViewFavoriteUserPhoto2Home.visibility = View.VISIBLE
                                                        binding.imageViewFavoriteUserPhoto3Home.visibility = View.VISIBLE

                                                        binding.textViewFavoriteName1Home.visibility = View.VISIBLE
                                                        binding.textViewFavoriteName2Home.visibility = View.VISIBLE
                                                        binding.textViewFavoriteName3Home.visibility = View.VISIBLE

                                                        for (item1 in users) {
                                                            if (message.fromId == item1.medicID || message.toId == item1.medicID) {
                                                                when (i) {
                                                                    0 -> {
                                                                        user1 = item1
                                                                        binding.textViewFavoriteName1Home.text = "Dr. " + item1.username
                                                                        if (item1.imagePath != "") {
                                                                            Picasso.get().load(item1.imagePath).into(binding.imageViewFavoriteUserPhoto1Home)
                                                                        } else {
                                                                            binding.imageViewFavoriteUserPhoto1Home.setBackgroundResource(R.drawable.imageviewfotofavorito1)
                                                                        }
                                                                    }
                                                                    1 -> {
                                                                        user2 = item1
                                                                        binding.textViewFavoriteName2Home.text = "Dr. " + item1.username
                                                                        if (item1.imagePath != "") {
                                                                            Picasso.get().load(item1.imagePath).into(binding.imageViewFavoriteUserPhoto2Home)
                                                                        } else {
                                                                            binding.imageViewFavoriteUserPhoto1Home.setBackgroundResource(R.drawable.imageviewfotofavorito1)
                                                                        }
                                                                    }
                                                                    2 -> {
                                                                        user3 = item1
                                                                        binding.textViewFavoriteName3Home.text = "Dr. " + item1.username
                                                                        if (item1.imagePath != "") {
                                                                            Picasso.get().load(item1.imagePath).into(binding.imageViewFavoriteUserPhoto3Home)
                                                                        } else {
                                                                            binding.imageViewFavoriteUserPhoto1Home.setBackgroundResource(R.drawable.imageviewfotofavorito1)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }
                            } ?: run {
                                binding.imageViewUserPhotoHome.setOnClickListener(null)
                                binding.buttonScheduleHome.setOnClickListener(null)
                                binding.buttonChatHome.setOnClickListener(null)
                                binding.floatingActionButton.setOnClickListener(null)

                                myDialog = Dialog(this, R.style.AnimateDialog)
                                myDialog.setContentView(R.layout.popwindow_register_continue)
                                myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                myDialog.setCanceledOnTouchOutside(false)
                                myDialog.findViewById<Button>(R.id.buttonRegisterContinuePopWindow).setOnClickListener {
                                    val username = myDialog.findViewById<EditText>(R.id.editTextNomeRegisterContinuePopWindow)
                                    val address = myDialog.findViewById<EditText>(R.id.editTextMoradaRegisterContinuePopWindow)
                                    val phone = myDialog.findViewById<EditText>(R.id.editTextTelemovelRegisterContinuePopWindow)
                                    val birthday = myDialog.findViewById<EditText>(R.id.editTextDataDeNascimentoContinuePopWindow)
                                    val hospitalNumber = myDialog.findViewById<EditText>(R.id.editTextNProcessoHospitalarRegisterContinuePopWindow)
                                    //Mudar para timestamp
                                    val healthNumber = myDialog.findViewById<EditText>(R.id.editTextNDeSaudeRegisterContinuePopWindow)
                                    val image = "https://i.ibb.co/XD9cD4C/profile-user-1.png"
                                    if (username.text.toString() == "" || address.text.toString() == "" || phone.text.toString() == ""|| birthday.text.toString() == ""|| hospitalNumber.text.toString() == "" || healthNumber.text.toString() == "") {
                                        Toast.makeText(
                                                this@HomeActivity, "Verifique os seus dados",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else {
                                        val db = FirebaseFirestore.getInstance()
                                        //Colocar "imageRef.name" no imagemPath me baixo
                                        val user = UsersItem(username.text.toString(), currentUser?.email, address.text.toString(), image, currentUser?.uid, phone.text.toString(), birthday.text.toString(), hospitalNumber.text.toString(), healthNumber.text.toString())
                                        db.collection("users").document(currentUser!!.uid)
                                            .set(user.toHashMap())
                                            .addOnSuccessListener {
                                                textViewActions(binding)
                                                userData(binding)
                                                getUser()
                                                getUsersDoctors()
                                                getCountNotification()
                                                buttonsActions(binding)
                                                Log.d("writeBD", "DocumentSnapshot successfully written!")
                                                myDialog.dismiss()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("writeBD", "Error writing document", e)
                                            }
                                    }
                                }
                                myDialog.show()
                            }
                        }
                    }
            }
        }
    }

    //Funcao com as acoes das textViews
    private fun textViewActions(binding: ActivityHomeBinding){

        //ImageViewUserPhoto ao clicar vai para o perfil
        binding.imageViewUserPhotoHome.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        //TextViewUserName ao clicar vai para o perfil
        binding.textViewUserNameHome.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    //Funcao com estilo das textViews
    private fun textViewStyle(binding: ActivityHomeBinding){
        binding.textViewUserNameHome.text = ""
        binding.textViewUserNumberPhoneHome.text = ""
        binding.textViewUserAddressHome.text = ""
    }

    //Funcao com as acoes dos botoes
    private fun buttonsActions(binding: ActivityHomeBinding){
        //Botao SOS
        binding.floatingActionButton.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:808242424")
            startActivity(callIntent)
        }

        //Botao para ir as consultas
        binding.buttonScheduleHome.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }

        //Botao para ir ao Chat
        binding.buttonChatHome.setOnClickListener {
            val intent = Intent(this, LatestMessagesActivity::class.java)
            intent.putExtra(ContactsActivity.USER_KEY, user)
            startActivity(intent)
        }

        //Botao para ultimas mensagens User1
        binding.imageViewFavoriteUserPhoto1Home.setOnClickListener {

            if (user1 != null) {

                val intent = Intent(this, ChatMessagesActivity::class.java)
                intent.putExtra(ContactsActivity.USER_KEY, user1)
                startActivity(intent)
            }
        }

        //Botao para ultimas mensagens User2
        binding.imageViewFavoriteUserPhoto2Home.setOnClickListener {

            if (user2 != null) {

                val intent = Intent(this, ChatMessagesActivity::class.java)
                intent.putExtra(ContactsActivity.USER_KEY, user2)
                startActivity(intent)
            }
        }

        //Botao para ultimas mensagens User3
        binding.imageViewFavoriteUserPhoto3Home.setOnClickListener {

            if (user3 != null) {

                val intent = Intent(this, ChatMessagesActivity::class.java)
                intent.putExtra(ContactsActivity.USER_KEY, user3)
                startActivity(intent)
            }
        }

        //Botao para editar, ja nao existe.
//        binding.buttonEditHome.setOnClickListener {
//            myDialog = Dialog(this)
//            myDialog.setContentView(R.layout.popwindow_edit)
//            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//
//            //Botao para escolher foto
//            myDialog.findViewById<ImageView>(R.id.imageViewUserPhotoEdit).setOnClickListener {
//                Intent(Intent.ACTION_GET_CONTENT).also {
//                    it.type = "image/*"
//                    startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)
//                }
//            }
//
//            //Botao para submeter a atualizacao
//            myDialog.findViewById<Button>(R.id.buttonEditarEdit).setOnClickListener {
//                uploadImageToFirebaseStorage()
//                updateUser()
//            }
//            myDialog.show()
//        }
    }

    //Funcao para buscar quantas consultas tem o CURRENTUSER
    private fun getCountNotification() {
        val imageViewScheduleNotification = findViewById<ImageView>(R.id.imageViewScheduleNotification)
        val textViewScheduleNotification = findViewById<TextView>(R.id.textViewScheduleNotification)

        /*val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val date : String

        if (month >= 10 && day >= 10) {
            date = "$year-$month-$day"
        }
        else if (month >= 10 && day < 10) {
            date = "$year-$month-0$day"
        }
        else if (month < 10 && day >= 10) {
            date = "$year-0$month-$day"
        }
        else {
            date = "$year-0$month-0$day"
        }*/

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())

        currentUser?.let{ it ->
            db.collection("consultas")
                .whereEqualTo("userID", it.uid)
                //.whereEqualTo("date", date)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.let {
                        var count = 0
                        for (doc in snapshot) {
                            if (doc.data.getValue("date") == currentDate) {
                                count++
                            }
                        }

                        if (count > 0) {
                            imageViewScheduleNotification.visibility = View.VISIBLE
                            textViewScheduleNotification.visibility = View.VISIBLE
                            textViewScheduleNotification.text = count.toString()
                        }
                        else {
                            imageViewScheduleNotification.visibility = View.INVISIBLE
                            textViewScheduleNotification.visibility = View.INVISIBLE
                        }
                }
            }
        }

    }

    //Funcao para ir buscar a informacao do CURRENTUSER
    private fun getUser() {

        db.collection("users").document(currentUser!!.uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                querySnapshot?.data?.let {
                    user = UsersItem.fromHash(querySnapshot.data as HashMap<String, Any?>)
                    user?.let { user ->
                        currentUserName = user.username.toString()
                        currentUserAddress = user.address.toString()
                    }
                } ?: run {
                    Toast.makeText(
                            this@HomeActivity, "Sem utilizador",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    //Funcao para fazer upload da imagem para o FireStorage
    /*private fun uploadImageToFirebaseStorage() {

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        curFile?.let{
            ref.putFile(curFile!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        downUrl = it.toString()
                    }
                }
                .addOnFailureListener {

                }
        }
    }*/

    //Funcao para fazer update do utilizador
    /*private fun updateUser(){

        val address = myDialog.findViewById<EditText>(R.id.editTextUserAddressEdit)

        if(address.text.toString() == "") {
            val user = UsersItem(currentUserName, currentUser!!.email, currentUserAddress, downUrl, currentUser.uid)
            db.collection("users").document(currentUser.uid)
                .set(user.toHashMap())
                .addOnSuccessListener {
                    Log.d("writeBD", "DocumentSnapshot successfully written!")
                    myDialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.w("writeBD", "Error writing document", e)
                }
        } else {
            val user = UsersItem(currentUserName, currentUser!!.email, address.text.toString(), downUrl, currentUser.uid)
            db.collection("users").document(currentUser.uid)
                .set(user.toHashMap())
                .addOnSuccessListener {
                    Log.d("writeBD", "DocumentSnapshot successfully written!")
                    myDialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.w("writeBD", "Error writing document", e)
                }
        }
    }*/

    //Funcao para buscar permissao para fazer chamada
    private fun getPermissionToPhoneCall() {
        if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(
                        Manifest.permission.CALL_PHONE), REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
         if (requestCode == REQUEST_CODE) {

             if (grantResults.size == 1
                     && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

             } else {

                 Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show()
                 finishAffinity()
             }
         }
     }

    //Abrir a janela para escolher a foto
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageViewUserPhotoEdit = findViewById<ImageView>(R.id.imageViewUserPhotoEdit)

        if (resultCode === Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMAGE_PICK) {
                data?.data?.let {

                    curFile = it
                    myDialog.findViewById<ImageView>(R.id.imageViewUserPhotoEdit).setImageURI(curFile)
                }
            }
        }
    }*/

    companion object {

        const val REQUEST_CODE_PHOTO = 23524
        const val REQUEST_CODE_IMAGE_PICK = 0
        const val ONE_MEGABYTE : Long = 1024 * 1024 * 5
    }
}