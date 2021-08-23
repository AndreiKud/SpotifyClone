package ru.andreikud.spotifyclone.data

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

class SongAdapter @Inject constructor(
    private val glide: RequestManager,
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPrimary: TextView = itemView.findViewById(R.id.tvPrimary)
        private val tvSecondary: TextView = itemView.findViewById(R.id.tvSecondary)
        private val ivItemImage: ImageView = itemView.findViewById(R.id.ivItemImage)

        fun bind(position: Int) {
            val song = songs[position]
            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
            glide.load(song.logoUrl).into(ivItemImage)

            onItemClickListener?.let {
                it(song)
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.mediaId == newItem.mediaId

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
        SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = songs.size

    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener(listener: ((Song) -> Unit)?) {
        onItemClickListener = listener
    }
}