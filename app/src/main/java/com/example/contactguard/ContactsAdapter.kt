package com.example.contactguard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    private val dataSet = ArrayList<Contact>()
    private var preViousNum = -1
    var newNumber = -1
    private lateinit var callBack : OnClickListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_layout, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.userTitle.text = dataSet[position].name
        holder.phoneNo.text = dataSet[position].phoneNumber
        holder.phoneCall.setOnClickListener {
            callBack.click(dataSet[position])
        }
        holder.perImage.text = dataSet[position].name.uppercase().take(1)
        val context = holder.itemView.context

        var inRand = false
        while (preViousNum == newNumber) {
            preViousNum = newNumber
            newNumber = Random.nextInt(1, 7)
            inRand =true

        }

        if(!inRand) {
            preViousNum = newNumber
            newNumber = Random.nextInt(1, 7)
        }



        val randColorSelection = intMap[newNumber]
        val color =
            randColorSelection?.let {
                ContextCompat.getColor(
                    context,
                    it
                )
            } // Replace with your color resource or hex color
        holder.perImage.backgroundTintList = color?.let { ColorStateList.valueOf(it) }

    }

    fun submitData(people: List<Contact>) {

        notifyDataSetChanged()
        val oldData = ArrayList(dataSet)  //creates a copy only
        dataSet.clear()
        dataSet.addAll(people)

        val diffUtilCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldData.size
            }

            override fun getNewListSize(): Int {
                return dataSet.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldData[oldItemPosition].id == dataSet[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldData[oldItemPosition] == dataSet[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    class ContactViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userTitle: TextView = itemView.findViewById(R.id.userTitle)
        val phoneNo: TextView = itemView.findViewById(R.id.phoneNo)
        val phoneCall: ImageView = itemView.findViewById(R.id.phoneCall)
        val perImage: TextView = itemView.findViewById(R.id.perImage)

    }

    fun setListener(callBack: OnClickListener) {
        this.callBack = callBack
    }

}
interface OnClickListener {
    fun click(contact: Contact)
}