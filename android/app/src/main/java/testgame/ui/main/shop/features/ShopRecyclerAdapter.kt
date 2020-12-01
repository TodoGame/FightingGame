package testgame.ui.main.shop.features

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testgame.R

class ShopRecyclerAdapter(private val usersMoney: Int, private val dataSet: Array<ShopItem>) :
    RecyclerView.Adapter<ShopRecyclerAdapter.ViewHolder>() {

    var data = dataSet
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, usersMoney)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
                .inflate(R.layout.item_shop_product, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage = itemView.findViewById<ImageView>(R.id.facultyImageView)
        val itemName = itemView.findViewById<TextView>(R.id.facultyNameTextView)

        fun bind(
            item: ShopItem,
            usersMoney: Int
        ) {
            if (item.price > usersMoney) {
                itemName.setTextColor(Color.RED)
            } else {
                itemName.setTextColor(Color.BLACK)
            }
            itemName.text = item.id

            itemName.text = item.name

//            itemImage.setImageResource(
//                when (item.id) {
//                    0 -> R.drawable.ic_sleep_0
//                    1 -> R.drawable.ic_sleep_1
//                    2 -> R.drawable.ic_sleep_2
//                    3 -> R.drawable.ic_sleep_3
//                    4 -> R.drawable.ic_sleep_4
//                    5 -> R.drawable.ic_sleep_5
//                    else -> R.drawable.ic_sleep_active
//                }
//            )
        }
    }
}
