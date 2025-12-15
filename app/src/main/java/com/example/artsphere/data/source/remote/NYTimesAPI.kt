package com.example.artsphere.data.source.remote

import com.example.artsphere.BuildConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

//NYTimes key retrieved from BuildConfig
object ApiKeys {
    val apiKey: String = BuildConfig.NYT_API_KEY
}

/**
 * Data class representing a news article from the New York Times API.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * @property title The headline or title of the article.
 * @property url The web URL where the full article can be accessed.
 * @property imageUrl Optional URL of the article's main image. Null if no image is available.
 */
data class Article(
    val title: String,
    val url: String,
    val imageUrl: String? = null
)
/**
 * UI state data class for the news screen.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This class represents the current state of the news feed, including loading status,
 * article data, and any error messages that may occur during data fetching.
 *
 * @property isLoading Indicates whether news data is currently being fetched.
 * @property articles List of article objects to display. Empty list when loading or on error.
 * @property error Error message string if an exception occurred during fetching. Null if no error.
 */
data class NewsUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for managing art news data and UI state.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This ViewModel handles fetching news articles from the New York Times API and
 * exposes the UI state to composables. It automatically loads news on initialization
 * and provides a method to manually refresh the data.
 */
class NewsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState(isLoading = true))
    val uiState: StateFlow<NewsUiState> = _uiState

    //Loads news when ViewModel is created
    init {
        loadNews()
    }

    /**
     * Loads or reloads art news articles from the New York Times API.
     *
     * KDoc generated with AI; reviewed and modified for accuracy.
     *
     * This method launches a coroutine in the viewModelScope to fetch news data asynchronously.
     * It updates the UI state to reflect loading, success, or error states appropriately.
     * Can be called manually to refresh the news feed.
     */
    fun loadNews() {
        viewModelScope.launch {
            _uiState.value = NewsUiState(isLoading = true, error = null)
            try {
                val news = fetchArtNews()
                _uiState.value = NewsUiState(articles = news)
            } catch (e: Exception) {
                _uiState.value = NewsUiState(error = e.message)
            }
        }
    }
}

/**
 * Composable function that displays a screen with the latest art news from NYT.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This screen shows a list of art-related news articles with images and titles.
 * It handles loading states, error states, and provides a refresh button.
 *
 * @param viewModel The NewsViewModel instance that manages the news data and state.
 *                  Defaults to a new instance created by viewModel().
 * @param onArticleClick Callback function invoked when a user clicks on an article.
 *                       Receives the clicked Article object as a parameter.
 */
@Composable
fun ArtNewsScreen(
    viewModel: NewsViewModel = viewModel(),
    onArticleClick: (Article) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Latest Art News", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(text = "Error: ${uiState.error}", modifier = Modifier.padding(16.dp))
            }
            else -> {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                ) {
                    items(uiState.articles) { article ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    onArticleClick(article)
                                }
                        ) {
                            // Display article image if available
                            article.imageUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = article.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Display article title
                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Refresh button to reload articles
                Button(
                    onClick = { viewModel.loadNews() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh")
                }
            }
        }
    }
}

/**
 * Fetches art-related news articles from the New York Times Top Stories API.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This suspend function makes a network call to the NYT API's arts section,
 * parses the JSON response, and returns a list of Article objects.
 * It runs on the IO dispatcher to avoid blocking the main thread.
 *
 * @return List of Article objects containing title, URL, and optional image URL.
 * @throws Exception if the network request fails or JSON parsing encounters an error.
 */
suspend fun fetchArtNews(): List<Article> = withContext(Dispatchers.IO) {
    val url = "https://api.nytimes.com/svc/topstories/v2/arts.json?api-key=${ApiKeys.apiKey}"
    val response = URL(url).readText()
    val json = JSONObject(response)
    val results = json.getJSONArray("results")

    List(results.length()) { i ->
        val item = results.getJSONObject(i)

        val multimedia = item.optJSONArray("multimedia")
        val imageUrl = multimedia?.let { arr ->
            if (arr.length() > 0) arr.getJSONObject(0).getString("url") else null
        }

        Article(
            title = item.getString("title"),
            url = item.getString("url"),
            imageUrl = imageUrl
        )
    }
}