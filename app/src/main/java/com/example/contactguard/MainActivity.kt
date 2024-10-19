package com.example.contactguard

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), OnClickListener {
    private val contactsList = mutableListOf<Contact>()
    private val filterList = mutableListOf<Contact>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var contactsAdapter: ContactsAdapter

    companion object {
        const val CONTACT_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        contactsAdapter = ContactsAdapter()
        contactsAdapter.setListener(this)
        recyclerView.adapter = contactsAdapter


        searchView = findViewById(R.id.search)
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
    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration(res.configuration)
        config.fontScale = 1.0f // Prevent font scaling
        res.updateConfiguration(config, res.displayMetrics)
        return res
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACT_PERMISSION_CODE
            )
        } else {
            loadContacts()
        }
    }

    override fun onPause() {
        super.onPause()
      //  loadContacts()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACT_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
        }
    }

    private fun loadContacts() {

        var generateId = 0
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)

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
}
