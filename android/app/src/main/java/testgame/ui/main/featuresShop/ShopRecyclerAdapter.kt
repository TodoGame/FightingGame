package testgame.ui.main.featuresShop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testgame.databinding.ItemShopProductBinding
import item.ItemData
import testgame.data.GameApp

class ShopRecyclerAdapter(private val clickListener: ShopItemListener) :
        ListAdapter<ItemData, ShopRecyclerAdapter.ViewHolder>(ShopNewsItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemShopProductBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                item: ItemData,
                clickListener: ShopItemListener,
        ) {
            binding.shopItem = item
            binding.clickListener = clickListener
            binding.facultyImageView.setImageResource(GameApp().getItemImageIdFromItemId(item.id))
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemShopProductBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class ShopNewsItemDiffCallback : DiffUtil.ItemCallback<ItemData>() {
    override fun areItemsTheSame(oldItem: ItemData, newItem: ItemData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ItemData, newItem: ItemData): Boolean {
        return oldItem == newItem
    }
}

class ShopItemListener(val listener: (itemId: Int) -> Unit) {
    fun onClick(item: ItemData) = listener(item.id)
}
