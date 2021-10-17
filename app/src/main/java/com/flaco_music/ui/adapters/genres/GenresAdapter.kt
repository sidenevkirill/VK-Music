package com.flaco_music.ui.adapters.genres

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flaco_music.R
import com.flaco_music.databinding.ItemGenreBinding
import com.flaco_music.utils.constants.ApiConstants
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.setRandomBgColor

class GenresAdapter(
    private val genres: List<ApiConstants.Genres.Genre>,
    private val callback: (genreId: Int, genreName: String, color: Int) -> Unit
) : RecyclerView.Adapter<GenresAdapter.GenresViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenresViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
        val binding = ItemGenreBinding.bind(view)
        return GenresViewHolder(binding)
    }

    override fun getItemCount(): Int = genres.size

    override fun onBindViewHolder(holder: GenresViewHolder, position: Int) {
        val track = genres[position]
        holder.bind(track)
    }

    inner class GenresViewHolder(private val binding: ItemGenreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: ApiConstants.Genres.Genre) {
            binding.nameText.text = genre.name
            val color = itemView.setRandomBgColor()
            itemView.onClick { callback(genre.id, genre.name, color) }
        }
    }
}