package com.flaco_music.ui.adapters.options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flaco_music.R
import com.flaco_music.databinding.ItemOptionBinding
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.options.OptionsUtils

class OptionsAdapter(private val options: List<OptionsUtils.Option>) :
    RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false)
        val binding = ItemOptionBinding.bind(view)
        return OptionsViewHolder(binding)
    }

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        val option = options[position]
        holder.bind(option)
    }

    inner class OptionsViewHolder(private val binding: ItemOptionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: OptionsUtils.Option) {
            binding.title.text = option.title
            binding.icon.setImageResource(option.iconRes)
            itemView.onClick { option.onClick() }
        }
    }
}