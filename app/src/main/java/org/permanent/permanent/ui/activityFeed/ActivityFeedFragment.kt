package org.permanent.permanent.ui.activityFeed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentActivityFeedBinding
import org.permanent.permanent.models.Notification
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.ActivityFeedViewModel

class ActivityFeedFragment : PermanentBaseFragment(), NotificationListener {

    private lateinit var binding: FragmentActivityFeedBinding
    private lateinit var viewModel: ActivityFeedViewModel
    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ActivityFeedViewModel::class.java)
        binding = FragmentActivityFeedBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initNotificationsRecyclerView(binding.rvNotifications)

        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onNotificationsRetrieved = Observer<MutableList<Notification>> {
        notificationsAdapter.set(it)
    }

    private fun initNotificationsRecyclerView(rvNotifications: RecyclerView) {
        notificationsRecyclerView = rvNotifications
        notificationsAdapter = NotificationsAdapter(this)
        notificationsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = notificationsAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onNotificationClick(notification: Notification) {
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnNotificationsRetrieved().observe(this, onNotificationsRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnNotificationsRetrieved().removeObserver(onNotificationsRetrieved)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}
