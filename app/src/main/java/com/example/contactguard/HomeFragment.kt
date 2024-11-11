package com.example.contactguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactguard.bottomsheet.BottomSheetCallBack
import com.example.contactguard.bottomsheet.WarningBottomSheetFragment
import com.example.contactguard.databinding.FragmentHomeBinding
import com.example.contactguard.utility.FireBaseManager.fireStoreContactDocumentReference

class HomeFragment : Fragment(R.layout.fragment_home),OnClickListener, BottomSheetCallBack {
    private lateinit var binding: FragmentHomeBinding
    private val contactsList = mutableListOf<Contact>()
    private val fireBaseContacts: MutableList<Contact> = mutableListOf()
    private val localContactsList = mutableListOf<Contact>()
    private val searchingFilterList = mutableListOf<Contact>()
    private lateinit var searchView: SearchView
    private  val contactsAdapter: ContactsAdapter =  ContactsAdapter()
    private var unSyncContacts : MutableList<Contact> = mutableListOf()

    private val bottomSheetFragment = WarningBottomSheetFragment()

    companion object {
        const val CONTACT_PERMISSION_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        val  recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        contactsAdapter.setListener(this)
        recyclerView.adapter = contactsAdapter


        bottomSheetFragment.setListener(this)
        isProgressBarVisible(true)

        searchView = binding.search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchContacts(newText)
                return true
            }

        })

        binding.settings.setOnClickListener {
//            Navigation.
        }



        binding.floatingActionButton.setOnClickListener {
            if (unSyncContacts.isNotEmpty()) {
                bottomSheetFragment.arguments = WarningBottomSheetFragment.createBundle(
                    "Sync Contacts!",
                    "Are you sure to backup those contacts?"
                )
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            } else {
                Toast.makeText(context, "No contact need to Sync", Toast.LENGTH_SHORT).show()

            }
        }

      //  syncContact(emptyList<Contact>())

        requestContactsPermission()
    }

    private fun filteringLocalAndBack(){
        //jsb server e save nai
        unSyncContacts = localContactsList.filterNot { loadContact ->
            fireBaseContacts.any { serverContact ->
                loadContact.phoneNumber == serverContact.phoneNumber
            }
        }.toMutableList()

        if(unSyncContacts.isEmpty()){
            showSyncMsg(true)
            showRecycleView(false)
        }
        contactsAdapter.submitData(unSyncContacts)
//        Log.wtf("Filterore","$result")
        Log.wtf("Filterore","$unSyncContacts")
//        Log.wtf("Filterore","- >>>>>>> $fireBaseContacts")
        isProgressBarVisible(false)
        binding.floatingActionButton.isEnabled = true


    }

    private fun searchContacts(query: String?) {
        searchingFilterList.clear()

        if (query.isNullOrEmpty()) {
            searchingFilterList.addAll(unSyncContacts)
        } else {
            val searchQuery = query.lowercase()
            for (contact in unSyncContacts) {
                if (contact.name.lowercase()
                        .contains(searchQuery) || contact.phoneNumber.lowercase()
                        .contains(searchQuery)
                ) {
                    searchingFilterList.add(contact)
                }
            }
        }
        contactsAdapter.submitData(searchingFilterList)
    }

    private fun  insertFilterData(){

    }

    private fun requestContactsPermission() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS) }
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
            fetchContact{fetchContacts->
                fireBaseContacts.clear()
                fireBaseContacts.addAll(fetchContacts)
                contactsAdapter.submitData(fireBaseContacts)
                Log.wtf("atleast", "$fetchContacts")
                filteringLocalAndBack()
            }

        }else{
            Toast.makeText(context, "User Didn't give any permission", Toast.LENGTH_SHORT).show()

        }
    }

    private fun loadContacts() {

        Toast.makeText(context, "Load Call", Toast.LENGTH_SHORT).show()

        var generateId = 0
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context?.contentResolver?.query(uri, projection, null, null, null)

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                generateId++
                contactsList.add(Contact(generateId, name, phoneNumber))
                localContactsList.add(Contact(generateId, name, phoneNumber))
            }
            Log.wtf("Contact", "$contactsList")
            Log.wtf("Contact", "${contactsList.size}")


            searchingFilterList.addAll(contactsList)
            cursor.close()

        } else {
            Log.wtf("Contact", "No dcontacts found.")
        }
    }

    override fun click(contact: Contact) {
        val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
        }
        startActivity(phoneIntent)
    }

    private fun syncContact(contactsList: List<Contact>) {
        val db = fireStoreContactDocumentReference()

        val contactData = hashMapOf(
            "contacts" to contactsList
        )

        db.set(contactData).addOnCompleteListener{
            contactsAdapter.submitData(listOf())
            isProgressBarVisible(false)
            showRecycleView(false)
            showSyncMsg(true)
            Toast.makeText(context, "Sync contact", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { e->
            Toast.makeText(context, "Sorry contact $e", Toast.LENGTH_SHORT).show()

        }

    }

    private fun fetchContact(onResult: (List<Contact>) -> Unit) {
        val db = fireStoreContactDocumentReference()
        db.get()
            .addOnSuccessListener { document ->

                if(document!=null){
                    val fetchContacts = document.toObject(UserContact::class.java)?.contacts?: emptyList()
                    onResult(fetchContacts)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }

    }
    private fun isProgressBarVisible(show: Boolean)
    {
        binding.progressBar.isVisible = show
    }
    private fun showRecycleView(show: Boolean){
        binding.recyclerView.isVisible = show

    }
    private fun showSyncMsg(show: Boolean){
        binding.syncMsg.isVisible = show
    }

    override fun confirmClick() {
        if(unSyncContacts.isNotEmpty()) {
            val passToBackend: MutableList<Contact> = mutableListOf()
            passToBackend.addAll(fireBaseContacts)
            passToBackend.addAll(unSyncContacts)
            isProgressBarVisible(true)

            syncContact(passToBackend)
        }

    }
}