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
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.elmnassri.databinding.FragmentStorageBinding
import kotlinx.coroutines.launch
import java.io.File
import com.example.elmnassri.BuildConfig

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

        // Pass the click handler lambda to the adapter
        val adapter = ItemAdapter { item ->
            // When an item is clicked, open the dialog in "edit mode"
            showAddItemDialog(item)
        }
        binding.recyclerView.adapter = adapter
        setupSearchView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collect { items ->
                adapter.submitList(items)
            }
        }

        binding.fabAddItem.setOnClickListener {
            // When FAB is clicked, open the dialog in "add mode"
            showAddItemDialog(null)
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

    // The function now accepts an optional Item to edit
    // In StorageFragment.kt

    private fun showAddItemDialog(itemToEdit: Item?) {
        selectedImageUri = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.edit_text_item_name)
        val priceEditText = dialogView.findViewById<EditText>(R.id.edit_text_item_price)
        val barcodeEditText = dialogView.findViewById<EditText>(R.id.edit_text_item_barcode)
        val takePictureButton = dialogView.findViewById<Button>(R.id.btn_select_image)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.image_preview)

        val isEditMode = itemToEdit != null
        val dialogTitle = if (isEditMode) "Edit Item" else "Add New Item"

        if (isEditMode) {
            nameEditText.setText(itemToEdit?.name)
            priceEditText.setText(itemToEdit?.price.toString())
            barcodeEditText.setText(itemToEdit?.barcode)
            itemToEdit?.imageUri?.let {
                selectedImageUri = it.toUri()
                imagePreview.load(it)
                imagePreview.visibility = View.VISIBLE
            }
        }

        takePictureButton.setOnClickListener {
            getTmpFileUri().let { uri ->
                selectedImageUri = uri
                takePictureLauncher.launch(uri)
            }
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set to null initially to override
            .setNegativeButton("Cancel", null)

        if (isEditMode) {
            builder.setNeutralButton("Delete") { _, _ ->
                itemToEdit?.let { viewModel.deleteItem(it) }
                Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
            }
        }

        dialog = builder.create()
        dialog?.show()

        // Override the "Save" button's click listener to allow for async validation
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val name = nameEditText.text.toString()
            val price = priceEditText.text.toString().toDoubleOrNull()
            val barcode = barcodeEditText.text.toString()

            // The save logic is now inside a coroutine
            lifecycleScope.launch {
                // Don't check for existing barcode if we are in edit mode
                if (isEditMode || viewModel.findItemByBarcode(barcode) == null) {
                    if (name.isNotBlank() && price != null) {
                        val itemId = itemToEdit?.id ?: 0
                        val updatedItem = Item(
                            id = itemId,
                            name = name,
                            price = price,
                            barcode = barcode.ifEmpty { "N/A" },
                            imageUri = selectedImageUri?.toString() ?: itemToEdit?.imageUri
                        )
                        viewModel.upsertItem(updatedItem, selectedImageUri)
                        dialog?.dismiss() // Close the dialog on success
                    } else {
                        Toast.makeText(requireContext(), "Name and Price cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If the item exists, show an error
                    Toast.makeText(requireContext(), "An item with this barcode already exists", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}