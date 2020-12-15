package testgame.ui.main.shop

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ShopPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int  = 2

    override fun getItem(i: Int): Fragment {
        return if (i == 0) {
            ShopPageFragment()
        } else {
            InventoryPageFragment()
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
