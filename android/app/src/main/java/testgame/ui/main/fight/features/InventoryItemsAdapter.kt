package testgame.ui.main.fight.features

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class InventoryItemsAdapter(context: Context, textViewResourceId: Int) : ArrayAdapter<String>(context, textViewResourceId,
    mContacts
) {
    var mContext: Context = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView: View? = convertView
        var label = convertView as TextView?
        if (convertView == null) {
            convertView = TextView(mContext)
            label = convertView
        }
        label!!.text = mContacts[position]
        return convertView
    }

    // возвращает содержимое выделенного элемента списка
    override fun getItem(position: Int): String {
        return mContacts[position]
    }

    companion object {
        private val mContacts = arrayOf("Банан", "Яд", "Зонтик", "Мыльный пузырь" , "Демидович")
    }
}