package com.networks.arcorefilters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

class FacePropRecyclerAdapter(
    private val filters: List<FaceProp>,
    private val actions: ViewHolderActions,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<FacePropRecyclerAdapter.FacePropViewHolder>() {

    val selectedModel = MutableLiveData<FaceProp>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacePropViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.adapter_face_props, parent, false)
        return FacePropViewHolder(view, actions)
    }

    override fun onBindViewHolder(holder: FacePropViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount(): Int {
        return filters.size
    }


    inner class FacePropViewHolder(
        view: View,
        private val actions: ViewHolderActions
    ) : RecyclerView.ViewHolder(view){

        private val propContainer = view.findViewById<ConstraintLayout>(R.id.prop_container)
        private val propImageView = view.findViewById<ImageView>(R.id.prop_img)
        private var prop: FaceProp? = null

        init {
            selectedModel.observe(lifecycleOwner,{
                if(prop?.title == it.title){
                    propContainer?.setBackgroundResource(R.drawable.selected_prop)
                }else{
                    propContainer?.setBackgroundResource(R.drawable.unselected_prop)
                }
            })
        }

        fun bind(prop: FaceProp){
            this.prop = prop
            propImageView.setImageResource(prop.icon)
            propImageView.setOnClickListener {
                actions.applyFaceProp(prop)
                selectedModel.postValue(prop)
            }
        }


    }

    interface ViewHolderActions{
        fun applyFaceProp(props: FaceProp)
    }

}