package com.example.contactguard.utility

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FireBaseManager {

    fun getFirebaseUserId(): String{
        return FirebaseAuth.getInstance().uid.toString()
    }

    fun fireStoreContactDocumentReference(): DocumentReference {
        return FirebaseFirestore.getInstance().collection("users").document(getFirebaseUserId())
    }

    fun fireStoreWholeCollectionReference():CollectionReference{
        return FirebaseFirestore.getInstance().collection("users")
    }
    fun getFireStoreInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}