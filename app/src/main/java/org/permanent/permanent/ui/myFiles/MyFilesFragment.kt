package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.permanent.databinding.FragmentMainBinding
import org.permanent.permanent.models.File
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.readJsonAsset
import org.permanent.permanent.viewmodels.MainFragmentViewModel
import java.io.IOException


class MyFilesFragment : PermanentBaseFragment(),OnMoreClickListener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainFragmentViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var supportFragmentManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MainFragmentViewModel::class.java)
        binding.viewModel = viewModel
        supportFragmentManager = parentFragmentManager
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        viewAdapter = FilesAdapter(readUserFilesFromAssets(),this)
        recyclerView = binding.rvFiles.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = viewAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL))
        }
    }



    private fun readUserFilesFromAssets(): List<File> {
        try {
            val jsonFileString = activity?.readJsonAsset("files.json")
            val gson = Gson()
            val listPersonType = object : TypeToken<List<File>>() {}.type

            val files: List<File> = gson.fromJson(jsonFileString, listPersonType)
            files.forEachIndexed { idx, file ->
                Log.i("data", "> Item $idx:\n$file")
            }
            return files
        } catch (ex: IOException) {
            ex.message?.let { Log.e("data", it) }
        } catch (ex: JsonSyntaxException) {
            ex.message?.let { Log.e("data", it) }
        }
        return emptyList()
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

    override fun onMoreClick() {
        val bottomNavDrawer = BottomNavigationDrawerFragment()
        bottomNavDrawer.show(supportFragmentManager,bottomNavDrawer.tag)
    }
}