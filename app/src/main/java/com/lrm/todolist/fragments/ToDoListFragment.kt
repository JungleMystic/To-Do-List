package com.lrm.todolist.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.lrm.todolist.R
import com.lrm.todolist.ToDoApplication
import com.lrm.todolist.adapter.ToDoListAdapter
import com.lrm.todolist.constants.NOTIFICATION_PERMISSION_CODE
import com.lrm.todolist.constants.TAG
import com.lrm.todolist.databinding.FragmentToDoListBinding
import com.lrm.todolist.viewmodel.ToDoViewModel
import com.lrm.todolist.viewmodel.ToDoViewModelFactory
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import de.hdodenhof.circleimageview.CircleImageView

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission()) requestPermission()
        }

        val adapter = ToDoListAdapter(requireActivity(), requireContext(), viewModel) {
            val addDialog = AddToDoFragment(it.id)
            addDialog.show(childFragmentManager, "Edit ToDo Dialog")
        }

        binding.recyclerView.adapter = adapter
        viewModel.getAll.observe(this.viewLifecycleOwner) { todos ->
            Log.i(TAG, "getAllToDos -> $todos")
            if (todos.isEmpty()) {
                Log.i(TAG, "Nothing ToDo is shown")
                binding.recyclerView.visibility = View.GONE
                binding.noTodoFound.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.noTodoFound.visibility = View.GONE
                todos.let { adapter.submitList(it) }
            }
        }

        binding.addItemFab.setOnClickListener {
            Log.i(TAG, "AddToDo Dialog is shown")
            val addDialog = AddToDoFragment()
            addDialog.show(childFragmentManager, "Add ToDo Dialog")
        }

        binding.appIcon.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }

        binding.appName.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // Show a dialog to go to settings, if the permission is permanently denied.
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsGranted -> Notification permission is called ")
        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    // Easy permissions checks whether the Notification permission is granted or not.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasPermission() =
            EasyPermissions.hasPermissions(requireContext(), Manifest.permission.POST_NOTIFICATIONS)

    // If notification permission is not granted, Easy Permission will request the user to grant.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        // EasyPermissions handles the request result
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showDeveloperInfoDialog() {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.custom_developer_info, null)
        val imageLink = "https://firebasestorage.googleapis.com/v0/b/gdg-vizag-f9bf0.appspot.com/o/gdg_vizag%2Fdeveloper%2FRammohan_L_pic.png?alt=media&token=6e55ba28-e0ca-45c6-b50b-be1955da2566"
        val devImage = dialogView.findViewById<CircleImageView>(R.id.dev_image)
        Glide.with(requireContext()).load(imageLink).placeholder(R.drawable.loading_icon_anim).into(devImage)

        val devGithubLink = dialogView.findViewById<CircleImageView>(R.id.dev_github_link)
        val devYoutubeLink = dialogView.findViewById<CircleImageView>(R.id.dev_youtube_link)

        devGithubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JungleMystic"))
            startActivity(intent)
        }

        devYoutubeLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@junglemystic"))
            startActivity(intent)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setCancelable(true)

        val developerDialog = builder.create()
        developerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        developerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView is called")
        _binding = null
    }
}