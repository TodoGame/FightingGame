package testgame.ui.main.shop.features

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter

class ShopPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int  = 2

    override fun getItem(i: Int): Fragment {
        if (i == 0) {
            return ShopBuyFragment()
        } else {
            return InventoryFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> {
                "shop"
            }
            1 -> {
                "inventory"
            }
            else -> {
                "error"
            }
        }
    }

}
