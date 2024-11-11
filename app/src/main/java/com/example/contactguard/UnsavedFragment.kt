package com.example.contactguard

import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
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
import com.example.contactguard.databinding.FragmentUnsavedBinding
import com.example.contactguard.utility.FireBaseManager


class UnsavedFragment : Fragment(R.layout.fragment_unsaved),OnClickListener, BottomSheetCallBack {
    private lateinit var binding: FragmentUnsavedBinding
    private  val contactsAdapter: ContactsAdapter =  ContactsAdapter()
    private val contactsList = mutableListOf<Contact>()
    private val fireBaseContacts: MutableList<Contact> = mutableListOf()  // fetching from firebase  contacts
    private val localContactsList = mutableListOf<Contact>()               // my local phone contacts
    private val filterList = mutableListOf<Contact>()                       // for search filters
    private lateinit var searchView: SearchView
    private var unSaveContacts : MutableList<Contact> = mutableListOf()
    private val bottomSheetFragment = WarningBottomSheetFragment()



    companion object {
        const val CONTACT_PERMISSION_CODE = 1
        private const val WRITE_CONTACTS_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUnsavedBinding.bind(view)

        val  recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        contactsAdapter.setListener(this)
        recyclerView.adapter = contactsAdapter

        requestContactsPermission()
        requestWriteContactsPermission()

        bottomSheetFragment.setListener(this)


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

        binding.floatingActionButton.setOnClickListener {

            if (unSaveContacts.isNotEmpty()) {
                bottomSheetFragment.arguments = WarningBottomSheetFragment.createBundle("Saving Contacts!", "Are you sure to save those contacts?")
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            } else {
                Toast.makeText(
                    context,
                    "All contacts are already saved on your phone!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    private fun saveToContact(unSaveContacts: MutableList<Contact>) {
        val contentResolver = requireContext().contentResolver

        for (contact in unSaveContacts) {
            val ops = ArrayList<ContentProviderOperation>()

            // Step 1: Insert a new raw contact to get a unique RAW_CONTACT_ID
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Step 2: Insert the display name for the contact
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0) // Use 0 as the reference to the previous operation
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                    .build()
            )

            // Step 3: Insert the phone number for the contact
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            // Apply the batch for each contact to ensure all operations complete individually
            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
//                unSaveContacts.clear()
                Log.d("ContactSave", "Contact saved: ${contact.name}")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save contact: ${contact.name}", Toast.LENGTH_SHORT).show()
            }
        }

        // Notify the user after saving all contacts
        isProgressBarVisible(false)
        showRecycleView(false)
        showSyncMsg(true)
        this.unSaveContacts.clear()
        Toast.makeText(context, "All contacts saved successfully", Toast.LENGTH_SHORT).show()
    }


    //Search Contact
    private fun filterContacts(query: String?) {
        filterList.clear()

        if (query.isNullOrEmpty()) {
            filterList.addAll(unSaveContacts)
        } else {
            val searchQuery = query.lowercase()
            for (contact in unSaveContacts) {
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
            fetchContact{fetchContacts->
                fireBaseContacts.clear()
                fireBaseContacts.addAll(fetchContacts)
                contactsAdapter.submitData(fireBaseContacts)
                Log.wtf("atleast", "$fetchContacts")
                filteringUnSavedContactOnPhone()
            }

        }else{
            Toast.makeText(context, "User Didn't give any permission", Toast.LENGTH_SHORT).show()

        }
    }
    private fun requestWriteContactsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_CONTACTS), WRITE_CONTACTS_PERMISSION_CODE)
        }
    }

    private fun filteringUnSavedContactOnPhone(){
        //jsb server e save nai
        unSaveContacts = fireBaseContacts.filterNot { loadContact ->
            localContactsList.any { serverContact ->
                loadContact.phoneNumber == serverContact.phoneNumber
            }
        }.toMutableList()

        if(unSaveContacts.isEmpty()){
            showSyncMsg(true)
            showRecycleView(false)
        }
        contactsAdapter.submitData(unSaveContacts)
        isProgressBarVisible(false)
        binding.floatingActionButton.isEnabled = true


    }

    private fun fetchContact(onResult: (List<Contact>) -> Unit) {
        val db = FireBaseManager.fireStoreContactDocumentReference()
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


            filterList.addAll(contactsList)
            cursor.close()

        } else {
            Log.wtf("Contact", "No dcontacts found.")
        }
    }
    override fun click(contact: Contact) {
        TODO("Not yet implemented")
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
        if (unSaveContacts.isNotEmpty()) {
            val passToBackend: MutableList<Contact> = mutableListOf()
            passToBackend.addAll(fireBaseContacts)
            passToBackend.addAll(unSaveContacts)
            isProgressBarVisible(true)
            saveToContact(unSaveContacts)
        }
    }

}