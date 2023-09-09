package com.lrm.todolist.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lrm.todolist.R
import com.lrm.todolist.ToDoApplication
import com.lrm.todolist.adapter.ToDoListAdapter
import com.lrm.todolist.constants.NOTIFICATION_PERMISSION_CODE
import com.lrm.todolist.databinding.FragmentToDoListBinding
import com.lrm.todolist.viewmodel.ToDoViewModel
import com.lrm.todolist.viewmodel.ToDoViewModelFactory
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class ToDoListFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentToDoListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ToDoViewModel by activityViewModels {
        ToDoViewModelFactory(
            (activity?.application as ToDoApplication).database.toDoDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToDoListBinding.inflate(inflater, container, false)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ToDoListAdapter(requireActivity(), requireContext(), viewModel) {
            Toast.makeText(requireContext(), "Item Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.adapter = adapter
        viewModel.getAll.observe(this.viewLifecycleOwner) { todo ->
            if (todo.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.noTodoFound.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.noTodoFound.visibility = View.GONE
                todo.let { adapter.submitList(it) }
            }
        }

        binding.addItemFab.setOnClickListener {
            val addDialog = AddToDoFragment()
            addDialog.show(requireActivity().supportFragmentManager, "Add ToDo Dialog")
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    private fun hasPermission() =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.POST_NOTIFICATIONS)

    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Permission is required to show notifications",
            NOTIFICATION_PERMISSION_CODE,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //EasyPermissions handles the request result
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}