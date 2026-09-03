package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SavedPage
import com.example.data.SavedPageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val DEFAULT_HOME_URL = "https://educationhills.netlify.app/"

data class WebUiState(
    val currentUrl: String = DEFAULT_HOME_URL,
    val pageTitle: String = "The Education Hills",
    val isLoading: Boolean = false,
    val loadProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val errorMessage: String? = null,
    val isSecure: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class WebViewModel(private val repository: SavedPageRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WebUiState())
    val uiState: StateFlow<WebUiState> = _uiState.asStateFlow()

    // Navigation command events (e.g., triggered by buttons for the WebView)
    private val _webCommand = MutableStateFlow<WebCommand?>(null)
    val webCommand: StateFlow<WebCommand?> = _webCommand.asStateFlow()

    // Search and filter in Saved Pages
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Active drawer / dialog state
    private val _showSavedSheet = MutableStateFlow(false)
    val showSavedSheet: StateFlow<Boolean> = _showSavedSheet.asStateFlow()

    private val _showAddBookmarkDialog = MutableStateFlow(false)
    val showAddBookmarkDialog: StateFlow<Boolean> = _showAddBookmarkDialog.asStateFlow()

    private val _editingBookmark = MutableStateFlow<SavedPage?>(null)
    val editingBookmark: StateFlow<SavedPage?> = _editingBookmark.asStateFlow()

    // Saved pages stream
    val savedPages: StateFlow<List<SavedPage>> = combine(
        _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) repository.allSavedPages
            else repository.searchPages(query.trim())
        },
        _selectedCategory
    ) { pages, category ->
        if (category == "All") pages
        else pages.filter { it.category.equals(category, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current page bookmark status
    val currentPageSaved: StateFlow<SavedPage?> = _uiState.flatMapLatest { state ->
        repository.observeByUrl(state.currentUrl)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onPageStarted(url: String) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            isLoading = true,
            errorMessage = null,
            isSecure = url.startsWith("https://")
        )
    }

    fun onPageFinished(url: String, title: String?) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            pageTitle = title?.takeIf { it.isNotBlank() } ?: "Education Hills",
            isLoading = false,
            loadProgress = 100,
            errorMessage = null,
            isSecure = url.startsWith("https://")
        )
    }

    fun onProgressChanged(progress: Int) {
        _uiState.value = _uiState.value.copy(
            loadProgress = progress,
            isLoading = progress in 1..99
        )
    }

    fun onReceivedError(description: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = description
        )
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.value = _uiState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    fun loadUrl(url: String) {
        val target = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        _webCommand.value = WebCommand.LoadUrl(target)
    }

    fun reload() {
        _webCommand.value = WebCommand.Reload
    }

    fun stopLoading() {
        _webCommand.value = WebCommand.StopLoading
    }

    fun goBack() {
        _webCommand.value = WebCommand.GoBack
    }

    fun goForward() {
        _webCommand.value = WebCommand.GoForward
    }

    fun goHome() {
        loadUrl(DEFAULT_HOME_URL)
    }

    fun clearWebCommand() {
        _webCommand.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openSavedSheet() {
        _showSavedSheet.value = true
    }

    fun closeSavedSheet() {
        _showSavedSheet.value = false
    }

    fun openAddBookmarkDialog(bookmark: SavedPage? = null) {
        _editingBookmark.value = bookmark
        _showAddBookmarkDialog.value = true
    }

    fun closeAddBookmarkDialog() {
        _editingBookmark.value = null
        _showAddBookmarkDialog.value = false
    }

    fun toggleSaveCurrentPage() {
        val currentSaved = currentPageSaved.value
        if (currentSaved != null) {
            deleteSavedPage(currentSaved)
        } else {
            val state = _uiState.value
            saveBookmark(
                title = state.pageTitle,
                url = state.currentUrl,
                category = "General",
                notes = ""
            )
        }
    }

    fun saveBookmark(title: String, url: String, category: String, notes: String, isPinned: Boolean = false) {
        viewModelScope.launch {
            val page = SavedPage(
                title = title.ifBlank { "Untitled Page" },
                url = url.trim(),
                category = category.ifBlank { "General" },
                notes = notes.trim(),
                savedAt = System.currentTimeMillis(),
                isPinned = isPinned
            )
            repository.insert(page)
            closeAddBookmarkDialog()
        }
    }

    fun updateSavedPage(page: SavedPage) {
        viewModelScope.launch {
            repository.update(page)
            closeAddBookmarkDialog()
        }
    }

    fun deleteSavedPage(page: SavedPage) {
        viewModelScope.launch {
            repository.delete(page)
        }
    }

    fun togglePin(page: SavedPage) {
        viewModelScope.launch {
            repository.update(page.copy(isPinned = !page.isPinned))
        }
    }
}

sealed interface WebCommand {
    data class LoadUrl(val url: String) : WebCommand
    data object Reload : WebCommand
    data object StopLoading : WebCommand
    data object GoBack : WebCommand
    data object GoForward : WebCommand
}

class WebViewModelFactory(private val repository: SavedPageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WebViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
