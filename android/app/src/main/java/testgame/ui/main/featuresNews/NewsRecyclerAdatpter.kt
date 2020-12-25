package testgame.ui.main.featuresNews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testgame.databinding.ItemFacultyNewsBinding

class NewsRecyclerAdapter(private val clickListener: NewsItemListener) :
        ListAdapter<NewsItem, NewsRecyclerAdapter.ViewHolder>(NewsItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemFacultyNewsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                item: NewsItem,
                clickListener: NewsItemListener,
        ) {
            binding.newsItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemFacultyNewsBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class NewsItemDiffCallback : DiffUtil.ItemCallback<NewsItem>() {
    override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
        return oldItem == newItem
    }
}

class NewsItemListener(val listener: (itemId: Int) -> Unit) {
    fun onClick(item: NewsItem) = listener(item.id)
}