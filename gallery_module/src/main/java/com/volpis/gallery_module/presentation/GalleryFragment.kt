package com.volpis.gallery_module.presentation

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.volpis.gallery_module.R
import com.volpis.gallery_module.data.repository.*
import com.volpis.gallery_module.domain.model.CloudProviderType
import com.volpis.gallery_module.domain.model.GalleryConfig
import com.volpis.gallery_module.domain.model.ViewMode
import com.volpis.gallery_module.domain.model.media.MediaFilter
import com.volpis.gallery_module.domain.repository.CompositeMediaRepository
import com.volpis.gallery_module.domain.repository.PermissionHandler
import com.volpis.gallery_module.presentation.adapters.AlbumAdapter
import com.volpis.gallery_module.presentation.adapters.MediaAdapter
import com.volpis.gallery_module.presentation.utils.GlideUtils
import com.volpis.gallery_module.presentation.utils.preloadItemImages
import com.volpis.gallery_module.presentation.viewmodel.GalleryUiState
import com.volpis.gallery_module.presentation.viewmodel.GalleryViewModel
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    companion object {
        private const val ARG_CONFIG = "gallery_config"
        fun newInstance(config: GalleryConfig): GalleryFragment {
            val fragment = GalleryFragment()
            val args = Bundle()
            args.putParcelable(ARG_CONFIG, config)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: GalleryViewModel
    private lateinit var config: GalleryConfig
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var albumAdapter: AlbumAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.loadInitialData()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            GlideUtils.clearMemoryCache(requireContext())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = arguments?.getParcelable(ARG_CONFIG) ?: GalleryConfig()

        val permissionHandler = FragmentPermissionHandler(this, requestPermissionLauncher)
        val deviceRepository = DeviceMediaRepositoryImpl(requireContext(), permissionHandler)

        val mediaRepository =
            if (config.enableCloudIntegration && config.cloudProviders.isNotEmpty()) {
                val cloudRepos = config.cloudProviders.associateWith { providerType ->
                    when (providerType) {
                        CloudProviderType.GOOGLE_DRIVE -> GoogleDriveRepository()
                        CloudProviderType.DROPBOX -> DropboxRepository()
                        CloudProviderType.ONE_DRIVE -> OneDriveRepository()
                        CloudProviderType.CUSTOM -> CustomCloudRepository()
                    }
                }
                CompositeMediaRepository(deviceRepository, cloudRepos)
            } else {
                deviceRepository
            }

        val factory = GalleryViewModel.Factory(mediaRepository, config)
        viewModel = ViewModelProvider(this, factory)[GalleryViewModel::class.java]

        lifecycle.addObserver(lifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.gallery_menu, menu)

                if (config.enableSearch) {
                    val searchItem = menu.findItem(R.id.action_search)
                    val searchView = searchItem.actionView as SearchView

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            if (!query.isNullOrBlank()) {
                                val filter = viewModel.currentFilter.value.copy(searchQuery = query)
                                viewModel.updateFilter(filter)
                            }
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            if (newText.isNullOrBlank()) {
                                val filter = viewModel.currentFilter.value.copy(searchQuery = null)
                                viewModel.updateFilter(filter)
                            }
                            return true
                        }
                    })
                } else {
                    menu.findItem(R.id.action_search)?.isVisible = false
                }

                menu.findItem(R.id.action_filter)?.isVisible = config.enableFiltering
                val toggleViewItem = menu.findItem(R.id.action_toggle_view)
                toggleViewItem.isVisible = config.allowViewModeToggle
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_toggle_view -> {
                        viewModel.toggleViewMode()
                        true
                    }

                    R.id.action_filter -> {
                        showFilterDialog()
                        true
                    }

                    android.R.id.home -> {
                        viewModel.onBackPressed()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupToolbar()
        setupAdapters()
        setupRecyclerViews()
        setupPreloading()
        setupButtons()
        observeViewModel()
        applyCustomizations()

        binding.grantPermissionButton.setOnClickListener {
            val permissionHandler = FragmentPermissionHandler(this, requestPermissionLauncher)
            lifecycleScope.launch {
                permissionHandler.requestStoragePermissions()
            }
        }

        lifecycleScope.launch {
            val permissionHandler =
                FragmentPermissionHandler(this@GalleryFragment, requestPermissionLauncher)
            if (permissionHandler.hasStoragePermissions()) {
                viewModel.loadInitialData()
            } else {
                viewModel.onPermissionDenied()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycle.removeObserver(lifecycleObserver)
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        GlideUtils.clearMemoryCache(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mediaRecyclerView.clearOnScrollListeners()
    }

    private fun setupToolbar() {
        val activity = requireActivity() as AppCompatActivity
        val toolbar: Toolbar = binding.toolbar

        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            config.titleText?.let { titleResId ->
                title = getString(titleResId)
            } ?: run {
                title = getString(R.string.default_gallery_title)
            }
        }
    }

    private fun setupAdapters() {
        mediaAdapter = MediaAdapter(config = config, onItemClick = { item ->
            viewModel.toggleItemSelection(item)
        }, onItemLongClick = { item ->
            viewModel.onMediaClicked(item)
        })

        albumAdapter = AlbumAdapter(
            config = config, onAlbumClick = { album ->
                viewModel.openAlbum(album.id)
            })
    }

    private fun setupRecyclerViews() {
        binding.mediaRecyclerView.apply {
            adapter = mediaAdapter
            layoutManager = when (config.defaultViewMode) {
                ViewMode.GRID -> GridLayoutManager(requireContext(), config.defaultGridColumns)
                ViewMode.LIST -> LinearLayoutManager(requireContext())
            }
        }

        binding.albumsRecyclerView.apply {
            adapter = albumAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupButtons() {
        config.doneButtonText?.let { textResId ->
            binding.doneButton.text = getString(textResId)
        }

        binding.doneButton.setOnClickListener {
            viewModel.confirmSelection()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUiState(state)
            }
        }
    }

    private fun updateUiState(state: GalleryUiState) {
        when (state) {
            is GalleryUiState.Loading -> {
                binding.loadingProgress.isVisible = true
                binding.mediaRecyclerView.isVisible = false
                binding.albumsRecyclerView.isVisible = false
                binding.emptyView.isVisible = false
                binding.permissionView.isVisible = false
            }

            is GalleryUiState.Success -> {
                binding.loadingProgress.isVisible = false
                binding.permissionView.isVisible = false
                binding.emptyView.isVisible = false

                val selectedCount = state.selectedItems.size
                binding.selectionCountTextView.text = if (selectedCount > 0) {
                    config.selectionCounterText?.let { textResId ->
                        getString(textResId, selectedCount)
                    } ?: getString(R.string.default_selection_counter, selectedCount)
                } else {
                    ""
                }

                binding.doneButton.isVisible = selectedCount > 0

                if (state.currentAlbumId == null && config.groupByAlbum && state.albums.isNotEmpty()) {
                    binding.albumsRecyclerView.isVisible = true
                    binding.mediaRecyclerView.isVisible = false
                    albumAdapter.submitList(state.albums)
                } else {
                    binding.albumsRecyclerView.isVisible = false
                    binding.mediaRecyclerView.isVisible = true

                    val recyclerView = binding.mediaRecyclerView
                    val currentLayoutManager = recyclerView.layoutManager

                    when (state.viewMode) {
                        ViewMode.GRID -> {
                            if (currentLayoutManager !is GridLayoutManager || currentLayoutManager.spanCount != state.columnCount) {
                                recyclerView.layoutManager =
                                    GridLayoutManager(requireContext(), state.columnCount)
                            }
                        }

                        ViewMode.LIST -> {
                            if (currentLayoutManager !is LinearLayoutManager || currentLayoutManager is GridLayoutManager) {
                                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                            }
                        }
                    }

                    mediaAdapter.setCurrentAlbumId(state.currentAlbumId)
                    mediaAdapter.setViewMode(state.viewMode)
                    mediaAdapter.setSelectedItems(state.selectedItems)

                    if (state.mediaItems.isNotEmpty()) {
                        binding.loadingProgress.isVisible = true

                        recyclerView.post {
                            mediaAdapter.submitList(state.mediaItems) {
                                binding.loadingProgress.isVisible = false
                            }
                        }
                    } else {
                        mediaAdapter.submitList(state.mediaItems)
                    }

                    if (state.currentAlbumId != null) {
                        val album = state.albums.find { it.id == state.currentAlbumId }
                        album?.let {
                            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                                it.name
                        }
                    }
                }
            }

            is GalleryUiState.Empty -> {
                binding.loadingProgress.isVisible = false
                binding.mediaRecyclerView.isVisible = false
                binding.albumsRecyclerView.isVisible = false
                binding.permissionView.isVisible = false
                binding.emptyView.isVisible = true

                config.emptyStateText?.let { textResId ->
                    binding.emptyTextView.text = getString(textResId)
                }
            }

            is GalleryUiState.Error -> {
                binding.loadingProgress.isVisible = false
                binding.mediaRecyclerView.isVisible = false
                binding.albumsRecyclerView.isVisible = false
                binding.permissionView.isVisible = false
                binding.emptyView.isVisible = true
                binding.emptyTextView.text = state.message

                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }

            is GalleryUiState.NoPermission -> {
                binding.loadingProgress.isVisible = false
                binding.mediaRecyclerView.isVisible = false
                binding.albumsRecyclerView.isVisible = false
                binding.emptyView.isVisible = false
                binding.permissionView.isVisible = true

                binding.grantPermissionButton.setOnClickListener {
                    val permissionHandler =
                        FragmentPermissionHandler(this, requestPermissionLauncher)
                    lifecycleScope.launch {
                        permissionHandler.requestStoragePermissions()
                    }
                }
            }
        }
    }

    private fun setupPreloading() {
        binding.mediaRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mediaAdapter.preloadItemImages(requireContext())
                }
            }
        })
    }

    private fun applyCustomizations() {
        config.backgroundColor?.let { colorResId ->
            binding.root.setBackgroundColor(requireContext().getColor(colorResId))
        }

        config.titleTypeface?.let { typeface ->
            binding.emptyTextView.typeface = typeface
            binding.selectionCountTextView.typeface = typeface
        }

        config.subtitleTypeface?.let { typeface ->
            binding.doneButton.typeface = typeface
            binding.grantPermissionButton.typeface = typeface
        }
    }

    private fun showFilterDialog() {
        if (!config.enableFiltering) {
            Toast.makeText(requireContext(), R.string.filtering_disabled, Toast.LENGTH_SHORT).show()
            return
        }

        val currentFilter = viewModel.currentFilter.value

        val dialog = MediaFilterDialog.newInstance(currentFilter)

        dialog.setOnFilterAppliedListener(object : MediaFilterDialog.OnFilterAppliedListener {
            override fun onFilterApplied(filter: MediaFilter) {
                viewModel.updateFilter(filter)
            }
        })

        dialog.show(childFragmentManager, "FILTER_DIALOG")
    }

    private class FragmentPermissionHandler(
        private val fragment: Fragment,
        private val permissionLauncher: ActivityResultLauncher<Array<String>>? = null,
    ) : PermissionHandler {
        override fun hasStoragePermissions(): Boolean {
            val requiredPermissions = getRequiredPermissions()
            return requiredPermissions.all { permission ->
                val result = fragment.requireContext().checkSelfPermission(permission)
                result == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }

        override suspend fun requestStoragePermissions(): Boolean {
            if (hasStoragePermissions()) {
                return true
            }

            val requiredPermissions = getRequiredPermissions()

            permissionLauncher?.launch(requiredPermissions.toTypedArray())
            // Return true to indicate we've started the permission request process
            // The actual result will be handled by the permission launcher callback
            return false
        }

        private fun getRequiredPermissions(): List<String> {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    listOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                else -> {
                    listOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }
}

class FragmentGalleryBinding private constructor(
    val root: View,
    val toolbar: Toolbar,
    val selectionCountTextView: TextView,
    val doneButton: Button,
    val albumsRecyclerView: RecyclerView,
    val mediaRecyclerView: RecyclerView,
    val loadingProgress: ProgressBar,
    val emptyView: LinearLayout,
    val emptyTextView: TextView,
    val permissionView: LinearLayout,
    val grantPermissionButton: Button,
) {
    companion object {
        fun inflate(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            attachToParent: Boolean,
        ): FragmentGalleryBinding {
            val root = inflater.inflate(R.layout.fragment_gallery, parent, attachToParent)

            return FragmentGalleryBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                selectionCountTextView = root.findViewById(R.id.selection_count_text_view),
                doneButton = root.findViewById(R.id.done_button),
                albumsRecyclerView = root.findViewById(R.id.albums_recycler_view),
                mediaRecyclerView = root.findViewById(R.id.media_recycler_view),
                loadingProgress = root.findViewById(R.id.loading_progress),
                emptyView = root.findViewById(R.id.empty_view),
                emptyTextView = root.findViewById(R.id.empty_text_view),
                permissionView = root.findViewById(R.id.permission_view),
                grantPermissionButton = root.findViewById(R.id.grant_permission_button)
            )
        }
    }
}