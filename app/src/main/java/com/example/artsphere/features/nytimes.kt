package com.example.artsphere.features

import com.example.artsphere.BuildConfig
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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


object ApiKeys {
    val apiKey: String = BuildConfig.NYT_API_KEY
}

data class Article(
    val title: String,
    val url: String,
    val imageUrl: String? = null
)
data class NewsUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null
)

class NewsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState(isLoading = true))
    val uiState: StateFlow<NewsUiState> = _uiState

    init {
        loadNews()
    }

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
@Composable
fun ArtNewsScreen(viewModel: NewsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    context.startActivity(intent)
                                }
                        ) {
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

                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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