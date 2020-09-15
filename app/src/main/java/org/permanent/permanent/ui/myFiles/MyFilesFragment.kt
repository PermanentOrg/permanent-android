package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.Constants
import org.permanent.permanent.models.File
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.readJsonAsset
import org.permanent.permanent.viewmodels.MyFilesViewModel
import java.io.IOException


class MyFilesFragment :
    PermanentBaseFragment(),
    PermanentTextWatcher,
    FileOptionsClickListener,
    View.OnClickListener {

    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: FilesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyFilesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MyFilesViewModel::class.java)
        binding.viewModel = viewModel
        setupRecyclerView()
        binding.etSearchQuery.addTextChangedListener(this)
        binding.clFolderDropdown.setOnClickListener(this)

        return binding.root
    }

    private fun setupRecyclerView() {
        viewAdapter = FilesAdapter(readUserFilesFromAssets(), this)
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

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        viewAdapter.filter.filter(charSequence)
        binding.ivSearchIcon.visibility =
            if (charSequence.toString().isEmpty()) View.VISIBLE else View.GONE
    }

    private fun readUserFilesFromAssets(): ArrayList<File> {
        try {
            val jsonFileString = activity?.readJsonAsset("files.json")
            val gson = Gson()
            val listPersonType = object : TypeToken<List<File>>() {}.type

            val files: ArrayList<File> = gson.fromJson(jsonFileString, listPersonType)
            files.forEachIndexed { idx, file -> Log.i("data", "> Item $idx:\n$file") }
            return files
        } catch (ex: IOException) {
            ex.message?.let { Log.e("data", it) }
        } catch (ex: JsonSyntaxException) {
            ex.message?.let { Log.e("data", it) }
        }
        return emptyList<File>() as ArrayList<File>
    }

    override fun onFileOptionsClick(file: File) {
        val bottomDrawerFragment = FileOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FILE_NAME, file.name)
        bottomDrawerFragment.arguments = bundle
        bottomDrawerFragment.show((context as AppCompatActivity).supportFragmentManager, bottomDrawerFragment.tag)
    }

    override fun onClick(view: View) {
        val bottomDrawerFragment = FolderOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FOLDER_NAME, "My Files")
        bottomDrawerFragment.arguments = bundle
        bottomDrawerFragment.show((context as AppCompatActivity).supportFragmentManager, bottomDrawerFragment.tag)
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}