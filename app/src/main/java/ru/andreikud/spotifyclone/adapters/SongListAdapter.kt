package ru.andreikud.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import ru.andreikud.spotifyclone.R
import ru.andreikud.spotifyclone.data.entities.Song
import javax.inject.Inject

class SongListAdapter @Inject constructor(
    private val glide: RequestManager,
) : BaseSongAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        with(holder.itemView) {
            val tvPrimary: TextView = findViewById(R.id.tvPrimary)
            val tvSecondary: TextView = findViewById(R.id.tvSecondary)
            val ivItemImage: ImageView = findViewById(R.id.ivItemImage)
            val song = songs[position]

            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
            glide.load(song.logoUrl).into(ivItemImage)

            val clickListener = View.OnClickListener {
                onItemClickListener?.let { listener ->
                    listener(song)
                }
            }
            tvPrimary.setOnClickListener(clickListener)
            tvSecondary.setOnClickListener(clickListener)
            ivItemImage.setOnClickListener(clickListener)
        }
    }
}