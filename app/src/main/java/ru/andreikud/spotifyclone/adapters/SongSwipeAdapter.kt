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

class SongSwipeAdapter : BaseSongAdapter(R.layout.swipe_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {
            val song = songs[position]
            val text = "${song.title} - ${song.subtitle}"
            val tvPrimary: TextView = findViewById(R.id.tvPrimary)
            tvPrimary.text = song.title

            setItemClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }
}