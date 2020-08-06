package com.ersiver.newsster.ui.list

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ersiver.newsster.model.Article
import com.ersiver.newsster.repository.NewssterRepository
import com.ersiver.newsster.util.*
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel for the [ArticleListFragment] screen.
 * The ViewModel works with the [NewssterRepository]
 * to get the list of articles.
 */
@ExperimentalPagingApi
class ArticleListViewModel @ViewModelInject constructor(
    private val repository: NewssterRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _navigateToArticleEvent = MutableLiveData<SingleEvent<Article>>()
    val navigateToArticleEvent: LiveData<SingleEvent<Article>>
        get() = _navigateToArticleEvent

    private val _categoryLiveData = MutableLiveData<String>()
    val categoryLiveData: LiveData<String> get() = _categoryLiveData

    private val _languageLiveData = MutableLiveData<String>()
    val languageLiveData: LiveData<String> get() = _languageLiveData

    init {
        _categoryLiveData.value = getLastSavedCategory()
        _languageLiveData.value = getLastSavedLanguage()
    }

    private var currentNews: Flow<PagingData<Article>>? = null

    fun loadNews(
    ): Flow<PagingData<Article>> {

        val category = _categoryLiveData.value!!
        val language = _languageLiveData.value!!

        val lastResult = currentNews
        if (lastResult != null && !shouldFetch(language, category))
            return lastResult

        val newNews =
            repository.fetchArticles(language, category).cachedIn(viewModelScope)

        currentNews = newNews
        saveCategoryFiltering(category)
        saveLanguageFiltering(language)
        return newNews
    }

    private fun shouldFetch(language: String, category: String): Boolean {
        return category != getLastSavedCategory()
                || language != getLastSavedLanguage()
    }

    private fun getLastSavedCategory() = savedStateHandle
        .getLiveData<String>(
            SAVED_STATE_CATEGORY
        ).value ?: DEFAULT_STATE_CATEGORY

    private fun getLastSavedLanguage() = savedStateHandle
        .getLiveData<String>(
            SAVED_STATE_LANGUAGE
        ).value ?: DEFAULT_STATE_LANGUAGE

    fun openArticle(article: Article) {
        _navigateToArticleEvent.value = SingleEvent(article)
    }

    /**
     * Sets the current list filtering type.
     */
    private fun saveCategoryFiltering(category: String) {
        savedStateHandle.set(SAVED_STATE_CATEGORY, category)
    }

    /**
     * Sets the current language.
     */
    private fun saveLanguageFiltering(language: String) {
        savedStateHandle.set(SAVED_STATE_LANGUAGE, language)
    }

    fun updateCategory(category: String) {
        _categoryLiveData.value = category
    }

    fun updateLanguage(language: String) {
        _languageLiveData.value = language
    }
}