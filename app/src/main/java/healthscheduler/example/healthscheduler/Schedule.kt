package healthscheduler.example.healthscheduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import healthscheduler.example.healthscheduler.databinding.ActivityHomeBinding
import healthscheduler.example.healthscheduler.databinding.ActivityProfileBinding
import healthscheduler.example.healthscheduler.databinding.ActivityScheduleBinding
import healthscheduler.example.healthscheduler.models.ScheduleItem
import healthscheduler.example.healthscheduler.models.UsersItem
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.android.synthetic.main.row_schedule.*
import java.util.ArrayList
import java.util.HashMap

class Schedule : AppCompatActivity() {

    private var listSchedule:       MutableList<ScheduleItem> = ArrayList()
    private var scheduleAdapter:    Schedule.ScheduleAdapter? = null

    private var listUser:           UsersItem? = null

    private val db          = FirebaseFirestore.getInstance()
    private val auth        = Firebase.auth
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityScheduleBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        photoUser()
        buttonsActions(binding)
        getUserData(binding)
    }

    //Funcao para ir buscar a informacao do CURRENTUSER
    private fun getUserData(binding: ActivityScheduleBinding){
        scheduleAdapter = ScheduleAdapter()
        binding.listViewSchedule.adapter = scheduleAdapter

        listSchedule.clear()

        db.collection("consultas").orderBy("date")
            .whereEqualTo("userID", currentUser!!.uid)
            .addSnapshotListener { snapshot, error ->
                snapshot?.let {
                    listSchedule.clear()
                    for (document in snapshot!!) {
                        //O Log.d é só para aparecer no logcat
                        Log.d("exist", "${document.id} => ${document.data}")
                        listSchedule.add(ScheduleItem(
                                document.data.getValue("date").toString(),
                                document.data.getValue("doctorName").toString(),
                                document.data.getValue("hour").toString(),
                                document.data.getValue("local").toString(),
                                document.data.getValue("typeOfConsult").toString()))
                    }
                    scheduleAdapter?.notifyDataSetChanged()
                }
            }
    }

    //Funcao com as acoes dos botoes
    private fun buttonsActions(binding: ActivityScheduleBinding){

        floatingActionButton.setOnClickListener{
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }

    //Funcao que vai buscar a foto do CURRENTUSER
    private fun photoUser(){
        currentUser!!.uid?.let {
            db.collection("users").document(it)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    querySnapshot?.data?.let {
                        listUser = UsersItem.fromHash(querySnapshot.data as HashMap<String, Any?>)
                        listUser?.let { user ->
                            if(user.imagePath != ""){
                                Picasso.get().load(user.imagePath).into(imageViewPhotoUser)
                            }else{
                                Toast.makeText(this@Schedule, "Não tem foto de perfil",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }?: run{
                            Toast.makeText(this@Schedule, "Sem sessão iniciada",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    //Adapter das consultas
    inner class ScheduleAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView = layoutInflater.inflate(R.layout.row_schedule, parent, false)

            val textViewDoctorNameSchedule = rowView.findViewById<TextView>(R.id.textViewDoctorNameSchedule)
            val textViewLocationSchedule = rowView.findViewById<TextView>(R.id.textViewLocationSchedule)
            val textViewTypeOfConsultSchedule = rowView.findViewById<TextView>(R.id.textViewTypeOfConsultSchedule)
            val textViewHourSchedule = rowView.findViewById<TextView>(R.id.textViewHourSchedule)
            val textViewDateSchedule = rowView.findViewById<TextView>(R.id.textViewDateSchedule)

            textViewDateSchedule.text = listSchedule[position].date
            textViewDoctorNameSchedule.text = listSchedule[position].doctorName
            textViewHourSchedule.text = listSchedule[position].hour
            textViewLocationSchedule.text = listSchedule[position].local
            textViewTypeOfConsultSchedule.text = listSchedule[position].typeOfConsult

            return rowView
        }

        override fun getItem(position: Int): Any {
            return listSchedule[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return listSchedule.size
        }
    }

}