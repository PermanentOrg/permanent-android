package org.permanent.permanent.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.R
import org.permanent.databinding.FragmentOnboardingBinding
import org.permanent.permanent.ui.MainActivity

class OnboardingFragment : Fragment() {
    private lateinit var onboardingPagesAdapter: OnboardingPagesAdapter
    private var _binding: FragmentOnboardingBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onboardingPagesAdapter = OnboardingPagesAdapter(this)
        viewPager = binding.viewPager
        viewPager.adapter = onboardingPagesAdapter

        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
        }.attach()

        binding.button.setOnClickListener {
            val currentItem = viewPager.currentItem

            if(currentItem == onboardingPagesAdapter.itemCount - 1) {
                startActivity(Intent(activity, MainActivity::class.java))
            } else {
                viewPager.currentItem = currentItem + 1
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    onboardingPagesAdapter.itemCount - 1 -> {
                        binding.button.text = getString(R.string.get_started)

                        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                        with (sharedPref.edit()) {
                            putBoolean(getString(R.string.onboarding_completed), true)
                            apply()
                        }
                    }
                    else -> binding.button.text = getString(R.string.next)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class OnboardingPagesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(Page.getByPosition(position)) {
            Page.ONE -> PageOneFragment()
            Page.TWO -> PageTwoFragment()
            Page.THREE -> PageThreeFragment()
        }
    }
}

enum class Page(private val position: Int) {
    ONE(0),
    TWO(1),
    THREE(2);

    companion object {
        fun getByPosition(position: Int): Page {
            for (shelve in values()) {
                if (shelve.position == position) {
                    return shelve
                }
            }

            return ONE
        }
    }
}