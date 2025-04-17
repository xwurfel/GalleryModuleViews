package com.volpis.gallery_module.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.util.Pair
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.RangeSlider
import com.volpis.gallery_module.R
import com.volpis.gallery_module.domain.model.media.MediaFilter
import com.volpis.gallery_module.domain.model.media.MediaSortOption
import com.volpis.gallery_module.domain.model.media.MediaType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MediaFilterDialog : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_CURRENT_FILTER = "current_filter"
        private const val MAX_FILE_SIZE_MB = 100

        fun newInstance(currentFilter: MediaFilter?): MediaFilterDialog {
            val fragment = MediaFilterDialog()
            val args = Bundle()
            args.putParcelable(ARG_CURRENT_FILTER, currentFilter)
            fragment.arguments = args
            return fragment
        }
    }

    private var listener: OnFilterAppliedListener? = null
    private var currentFilter: MediaFilter = MediaFilter()

    private lateinit var dateRangeTextView: TextView
    private lateinit var fileSizeRangeSlider: RangeSlider
    private lateinit var fileSizeTextView: TextView
    private lateinit var imagesCheckbox: CheckBox
    private lateinit var videosCheckbox: CheckBox
    private lateinit var cloudItemsCheckbox: CheckBox
    private lateinit var sortByRadioGroup: RadioGroup

    interface OnFilterAppliedListener {
        fun onFilterApplied(filter: MediaFilter)
    }

    fun setOnFilterAppliedListener(listener: OnFilterAppliedListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<MediaFilter>(ARG_CURRENT_FILTER)?.let {
            currentFilter = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_media_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateRangeTextView = view.findViewById(R.id.date_range_text_view)
        fileSizeRangeSlider = view.findViewById(R.id.file_size_range_slider)
        fileSizeTextView = view.findViewById(R.id.file_size_text_view)
        imagesCheckbox = view.findViewById(R.id.images_checkbox)
        videosCheckbox = view.findViewById(R.id.videos_checkbox)
        cloudItemsCheckbox = view.findViewById(R.id.cloud_items_checkbox)
        sortByRadioGroup = view.findViewById(R.id.sort_by_radio_group)

        view.findViewById<View>(R.id.date_range_layout).setOnClickListener {
            showDateRangePicker()
        }

        fileSizeRangeSlider.valueFrom = 0f
        fileSizeRangeSlider.valueTo = MAX_FILE_SIZE_MB.toFloat()
        fileSizeRangeSlider.values = listOf(0f, MAX_FILE_SIZE_MB.toFloat())

        fileSizeRangeSlider.addOnChangeListener { slider, _, _ ->
            updateFileSizeText(slider.values[0].toInt(), slider.values[1].toInt())
        }

        view.findViewById<Button>(R.id.apply_button).setOnClickListener {
            applyFilter()
        }

        view.findViewById<Button>(R.id.reset_button).setOnClickListener {
            resetFilter()
        }

        applyCurrentFilterToViews()
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.date_range)).setSelection(
                Pair(
                    currentFilter.dateRange?.first?.time
                        ?: MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    currentFilter.dateRange?.second?.time
                        ?: MaterialDatePicker.todayInUtcMilliseconds()
                )
            ).build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Date(selection.first)
            val endDate = Date(selection.second)
            currentFilter = currentFilter.copy(dateRange = startDate to endDate)
            updateDateRangeText(startDate, endDate)
        }

        dateRangePicker.addOnNegativeButtonClickListener {
            currentFilter = currentFilter.copy(dateRange = null)
            updateDateRangeText(null, null)
        }

        dateRangePicker.addOnCancelListener {}

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun updateDateRangeText(startDate: Date?, endDate: Date?) {
        if (startDate != null && endDate != null) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedStart = dateFormat.format(startDate)
            val formattedEnd = dateFormat.format(endDate)
            dateRangeTextView.text = "$formattedStart - $formattedEnd"
        } else {
            dateRangeTextView.text = getString(R.string.date_range)
        }
    }

    private fun updateFileSizeText(minSize: Int, maxSize: Int) {
        val formattedMinSize = if (minSize == 0) "0" else "$minSize MB"
        val formattedMaxSize = if (maxSize == MAX_FILE_SIZE_MB) "∞" else "$maxSize MB"
        fileSizeTextView.text = "$formattedMinSize - $formattedMaxSize"
    }

    private fun applyCurrentFilterToViews() {
        updateDateRangeText(
            currentFilter.dateRange?.first, currentFilter.dateRange?.second
        )

        val minSizeMB = (currentFilter.minSize ?: 0) / (1024 * 1024)
        val maxSizeMB = currentFilter.maxSize?.let { it / (1024 * 1024) } ?: MAX_FILE_SIZE_MB
        fileSizeRangeSlider.values = listOf(minSizeMB.toFloat(), maxSizeMB.toFloat())
        updateFileSizeText(minSizeMB.toInt(), maxSizeMB.toInt())

        imagesCheckbox.isChecked = currentFilter.mediaTypes.contains(MediaType.IMAGE)
        videosCheckbox.isChecked = currentFilter.mediaTypes.contains(MediaType.VIDEO)

        cloudItemsCheckbox.isChecked = currentFilter.includeCloudItems

        val sortByButtonId = when (currentFilter.sortBy) {
            MediaSortOption.NAME_ASC -> R.id.sort_name_asc
            MediaSortOption.NAME_DESC -> R.id.sort_name_desc
            MediaSortOption.DATE_CREATED_ASC -> R.id.sort_date_created_asc
            MediaSortOption.DATE_CREATED_DESC -> R.id.sort_date_created_desc
            MediaSortOption.DATE_MODIFIED_ASC -> R.id.sort_date_modified_asc
            MediaSortOption.DATE_MODIFIED_DESC -> R.id.sort_date_modified_desc
            MediaSortOption.SIZE_ASC -> R.id.sort_size_asc
            MediaSortOption.SIZE_DESC -> R.id.sort_size_desc
        }
        sortByRadioGroup.check(sortByButtonId)
    }

    private fun applyFilter() {
        val mediaTypes = mutableSetOf<MediaType>().apply {
            if (imagesCheckbox.isChecked) add(MediaType.IMAGE)
            if (videosCheckbox.isChecked) add(MediaType.VIDEO)
        }

        val minSizeMB = fileSizeRangeSlider.values[0].toLong()
        val maxSizeMB = fileSizeRangeSlider.values[1].toLong()
        val minSize = if (minSizeMB > 0) minSizeMB * 1024 * 1024 else null
        val maxSize = if (maxSizeMB < MAX_FILE_SIZE_MB) maxSizeMB * 1024 * 1024 else null

        val sortBy = when (sortByRadioGroup.checkedRadioButtonId) {
            R.id.sort_name_asc -> MediaSortOption.NAME_ASC
            R.id.sort_name_desc -> MediaSortOption.NAME_DESC
            R.id.sort_date_created_asc -> MediaSortOption.DATE_CREATED_ASC
            R.id.sort_date_created_desc -> MediaSortOption.DATE_CREATED_DESC
            R.id.sort_date_modified_asc -> MediaSortOption.DATE_MODIFIED_ASC
            R.id.sort_date_modified_desc -> MediaSortOption.DATE_MODIFIED_DESC
            R.id.sort_size_asc -> MediaSortOption.SIZE_ASC
            R.id.sort_size_desc -> MediaSortOption.SIZE_DESC
            else -> MediaSortOption.DATE_MODIFIED_DESC
        }

        val filter = MediaFilter(
            mediaTypes = mediaTypes,
            dateRange = currentFilter.dateRange,
            minSize = minSize,
            maxSize = maxSize,
            includeCloudItems = cloudItemsCheckbox.isChecked,
            sortBy = sortBy,
            searchQuery = currentFilter.searchQuery,
            albumIds = currentFilter.albumIds,
            cloudProviders = currentFilter.cloudProviders
        )

        listener?.onFilterApplied(filter)
        dismiss()
    }

    private fun resetFilter() {
        currentFilter = MediaFilter()
        applyCurrentFilterToViews()
    }
}