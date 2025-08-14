package com.example.elmnassri

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.elmnassri.databinding.FragmentStorageBinding
import kotlinx.coroutines.launch
import java.io.File
import com.example.elmnassri.BuildConfig // <-- THE FIX: This import was missing


class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory((requireActivity().application as InventoryApplication).repository)
    }

    private var selectedImageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            dialog?.findViewById<ImageView>(R.id.image_preview)?.apply {
                setImageURI(selectedImageUri)
                visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ItemAdapter()
        binding.recyclerView.adapter = adapter
        setupSearchView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collect { items ->
                adapter.submitList(items)
            }
        }

        binding.fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return false }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun showAddItemDialog() {
        selectedImageUri = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.edit_text_item_name)
        val priceEditText = dialogView.findViewById<EditText>(R.id.edit_text_item_price)
        val barcodeEditText = dialogView.findViewById<EditText>(R.id.edit_text_item_barcode)
        val takePictureButton = dialogView.findViewById<Button>(R.id.btn_select_image)

        takePictureButton.setOnClickListener {
            getTmpFileUri().let { uri ->
                selectedImageUri = uri
                takePictureLauncher.launch(uri)
            }
        }

        dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Item")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString()
                val price = priceEditText.text.toString().toDoubleOrNull()
                val barcode = barcodeEditText.text.toString()

                if (name.isNotBlank() && price != null) {
                    val newItem = Item(
                        name = name,
                        price = price,
                        barcode = barcode.ifEmpty { "N/A" },
                        imageUri = selectedImageUri?.toString()
                    )
                    viewModel.upsertItem(newItem)
                } else {
                    Toast.makeText(requireContext(), "Name and Price cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}