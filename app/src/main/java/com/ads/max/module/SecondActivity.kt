package com.ads.max.module

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        val tabTitles = arrayOf("Tab 1", "Tab 2", "Tab 3")

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabTitles.size

            override fun createFragment(position: Int): Fragment {
                return DemoTabFragment.newInstance(position + 1, tabTitles[position])
            }
        }

        // Bật offscreenPageLimit = 2 để ViewPager2 preload cùng lúc cả 3 Fragment
        // Kiểm tra tính độc lập và khả năng click của Native Ad trên cả 3 Fragment
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}
