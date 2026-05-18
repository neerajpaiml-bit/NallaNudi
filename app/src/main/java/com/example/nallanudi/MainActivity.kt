package com.example.nallanudi

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nallanudi.data.Term
import com.example.nallanudi.ui.NallaNudiViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TextToSpeech
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.US
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NallaNudiApp(tts)
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Saved : Screen("saved", "My List", Icons.Default.Favorite)
    object Flashcards : Screen("flashcards", "Flashcards", Icons.Default.Refresh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NallaNudiApp(tts: TextToSpeech?) {
    val navController = rememberNavController()
    val viewModel: NallaNudiViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nalla-Nudi (ನಲ್ಲ-ನುಡಿ)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Search.route) {
                SearchScreen(viewModel, tts)
            }
            composable(Screen.Saved.route) {
                SavedWordsScreen(viewModel, tts)
            }
            composable(Screen.Flashcards.route) {
                val savedTerms by viewModel.savedTerms.collectAsState()
                FlashcardScreen(savedTerms)
            }
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    val items = listOf(Screen.Search, Screen.Saved, Screen.Flashcards)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun SearchScreen(viewModel: NallaNudiViewModel, tts: TextToSpeech?) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredTerms by viewModel.filteredTerms.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Word of the Day Section
        wordOfTheDay?.let { term ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Word of the Day", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(term.englishWord, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(term.kannadaMeaning, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            label = { Text("Search Technical Terms (English/Kannada)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val subjects = listOf("Science", "Math", "Commerce")
            subjects.forEach { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { viewModel.onSubjectFilterChange(subject) },
                    label = { Text(subject) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredTerms) { term ->
                TermCard(term, tts, onToggleSave = { viewModel.toggleSaveTerm(term) })
            }
        }
    }
}

@Composable
fun SavedWordsScreen(viewModel: NallaNudiViewModel, tts: TextToSpeech?) {
    val savedTerms by viewModel.savedTerms.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Difficult Words List", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (savedTerms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No words saved yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(savedTerms) { term ->
                    TermCard(term, tts, onToggleSave = { viewModel.toggleSaveTerm(term) })
                }
            }
        }
    }
}

@Composable
fun TermCard(term: Term, tts: TextToSpeech?, onToggleSave: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = term.englishWord,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = term.kannadaMeaning,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row {
                    IconButton(onClick = { tts?.speak(term.englishWord, TextToSpeech.QUEUE_FLUSH, null, null) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Pronounce")
                    }
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (term.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (term.isSaved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = term.exampleSentence, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            AssistChip(
                onClick = { },
                label = { Text(term.subject, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
fun FlashcardScreen(savedWords: List<Term>) {
    var index by remember { mutableIntStateOf(0) }
    var showMeaning by remember { mutableStateOf(false) }

    if (savedWords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Add words to 'My List' to start revision!", textAlign = TextAlign.Center)
        }
        return
    }

    val currentTerm = savedWords[index % savedWords.size]

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Flashcard Revision", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clickable { showMeaning = !showMeaning },
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (showMeaning) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (showMeaning) currentTerm.kannadaMeaning else currentTerm.englishWord,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (showMeaning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "(${currentTerm.englishWord})", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (showMeaning) "Tap to hide meaning" else "Tap to see meaning",
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    index = if (index > 0) index - 1 else savedWords.size - 1
                    showMeaning = false
                },
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                Text("Previous")
            }
            Button(
                onClick = {
                    index = (index + 1) % savedWords.size
                    showMeaning = false
                },
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                Text("Next")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Word ${ (index % savedWords.size) + 1 } of ${savedWords.size}")
    }
}
