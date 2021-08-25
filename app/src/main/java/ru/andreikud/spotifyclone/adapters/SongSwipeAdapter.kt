package ru.andreikud.spotifyclone.adapters

import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import ru.andreikud.spotifyclone.R

class SongSwipeAdapter : BaseSongAdapter(R.layout.swipe_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {
            val song = songs[position]
            val text = "${song.title} - ${song.subtitle}"
            val tvPrimary: TextView = findViewById(R.id.tvPrimary)
            tvPrimary.text = text

            tvPrimary.setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }
}