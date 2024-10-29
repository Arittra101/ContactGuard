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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactguard.databinding.FragmentHomeBinding
import com.example.contactguard.utility.FireBaseManager.fireStoreContactDocumentReference
import com.example.contactguard.utility.FireBaseManager.getFireStoreInstance

class HomeFragment : Fragment(R.layout.fragment_home),OnClickListener {
    private lateinit var binding: FragmentHomeBinding
    private val contactsList = mutableListOf<Contact>()
    private val fireBaseContacts: MutableList<Contact> = mutableListOf()
    private val localContactsList = mutableListOf<Contact>()
    private val filterList = mutableListOf<Contact>()
    private lateinit var searchView: SearchView
    private  val contactsAdapter: ContactsAdapter =  ContactsAdapter()

    companion object {
        const val CONTACT_PERMISSION_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
//    avishek51775@gmail.com


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        val  recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        contactsAdapter.setListener(this)
        recyclerView.adapter = contactsAdapter

        var isShow = true
        var count =0;

      /*  recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && isShow) {
                    count++
                    if(count==1){
                        searchView.isVisible = false
                        count=0
                    }
                    // Scrolling down, hide the search view
                    searchView.animate().translationY(-searchView.height.toFloat()).setDuration(300)
                    searchView.isVisible = false
                    isShow = false
                } else if (dy < 0 && !isShow) {
                    // Scrolling up, show the search view
                    count++
                    if(count==1){
                        searchView.isVisible = true
                        count=0
                    }
                    isShow = true
                    searchView.animate().translationY(0f).setDuration(300)
                }
            }
        })*/
        searchView = binding.search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterContacts(newText)
                return true
            }

        })

        requestContactsPermission()
    }

    private fun filteringLocalAndBack(){
        //filter mismatched mobile no local.number != firestore.number
        // after that set to the recyleview


        //make a list of merge local and firestore contact which is shown to first fragment
        //in second fragment the one which is not available in firestore

        val result  = fireBaseContacts.filter{serverContact->
            localContactsList.any{loadContacts-> loadContacts.phoneNumber == serverContact.phoneNumber}

        }
        Log.wtf("Filterore","$result")

    }

    private fun filterContacts(query: String?) {
        filterList.clear()

        if (query.isNullOrEmpty()) {
            filterList.addAll(contactsList)
        } else {
            val searchQuery = query.lowercase()
            for (contact in contactsList) {
                if (contact.name.lowercase()
                        .contains(searchQuery) || contact.phoneNumber.lowercase()
                        .contains(searchQuery)
                ) {
                    filterList.add(contact)
                }
            }
        }
        contactsAdapter.submitData(filterList)
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
                Log.wtf("atleast", fetchContacts[0].name)
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
                //   filterList.add(Contact(generateId, name, phoneNumber))
            }
            Log.wtf("Contact", "$contactsList")
            Log.wtf("Contact", "${contactsList.size}")

           /* contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))
            contactsList.add(Contact(1, "name", "phoneNumber"))*/

            filterList.addAll(contactsList)
            //contactsAdapter.submitData(filterList)
            //syncContact(contactsList)
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

    private fun syncContact(contactsList: MutableList<Contact>) {
        val db = fireStoreContactDocumentReference()

        val contactData = hashMapOf(
            "contacts" to contactsList
        )

        db.set(contactData).addOnCompleteListener{
            Toast.makeText(context, "Save contact", Toast.LENGTH_SHORT).show()

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
//        Log.wtf("Arittra", "$data")

    }
}