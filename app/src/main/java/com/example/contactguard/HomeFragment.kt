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
    private val filterList = mutableListOf<Contact>()
    private lateinit var searchView: SearchView
    private  val contactsAdapter: ContactsAdapter =  ContactsAdapter()

    companion object {
        const val CONTACT_PERMISSION_CODE = 1
        val currentFragment = R.id.homeFragment

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

    private fun requestContactsPermission() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS) }
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()

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
                //   filterList.add(Contact(generateId, name, phoneNumber))
            }
            Log.wtf("Contact", "$contactsList")
            Log.wtf("Contact", "${contactsList.size}")

            filterList.addAll(contactsList)
            contactsAdapter.submitData(filterList)
            syncContact(contactsList)
            cursor.close()

        } else {
            Log.wtf("Contact", "No contacts found.")
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
            Toast.makeText(context, "Sorry contact ${e}", Toast.LENGTH_SHORT).show()

        }

    }


}