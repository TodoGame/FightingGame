package testgame.ui.main.featuresInventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testgame.databinding.ItemInventoryThingBinding
import item.ItemData
import testgame.data.GameApp

class InventoryRecyclerAdapter(val clickListener: InventoryItemListener) :
        ListAdapter<ItemData, InventoryRecyclerAdapter.ViewHolder>(InventoryItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemInventoryThingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
                item: ItemData,
                clickListener: InventoryItemListener,
        ) {
            binding.inventoryItem = item
            binding.clickListener = clickListener
            binding.inventoryItemImageView.setImageResource(GameApp().getItemImageIdByItemId(item.id))
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemInventoryThingBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class InventoryItemDiffCallback :
        DiffUtil.ItemCallback<ItemData>() {
    override fun areItemsTheSame(oldItem: ItemData, newItem: ItemData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ItemData, newItem: ItemData): Boolean {
        return oldItem == newItem
    }
}

class InventoryItemListener(val listener: (item: ItemData) -> Unit) {
    fun onClick(item: ItemData) = listener(item)
}
