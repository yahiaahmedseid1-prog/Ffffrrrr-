package com.example

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.ads.UnityAdsManager
import com.example.model.Anime
import com.example.model.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Realtime Database programmatically to ensure instant functionality
        val options = FirebaseOptions.Builder()
            .setApiKey("AIzaSyDfGLNhCwLr_gAxV9LRz0Ppm7Feb-iZYt4")
            .setApplicationId("1:1737295542:android:abcdef")
            .setDatabaseUrl("https://studio-1737295542-906cd-default-rtdb.firebaseio.com")
            .setProjectId("studio-1737295542-906cd")
            .build()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options)
        }

        // Initialize Unity Ads SDK
        UnityAdsManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            // Apply customized elegant dark movie-streaming colors
            val customColorScheme = darkColorScheme(
                primary = Color(0xFFF43F5E),      // Premium Rose 500
                background = Color(0xFF0B0C10),   // Elegant Pure Dark Canvas
                surface = Color(0xFF16171D),      // Deep graphite card slate
                onPrimary = Color.White,
                onBackground = Color(0xFFF1F5F9), // Slate 100
                onSurface = Color(0xFFF1F5F9),
                secondary = Color(0xFFE11D48),    // Premium Rose 600
                tertiary = Color(0xFF94A3B8)       // Slate 400
            )
            
            MaterialTheme(
                colorScheme = customColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Global App Navigation Host
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AnimeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("home") {
            HomeScreen(navController, viewModel)
        }
        composable(
            route = "detail/{animeId}",
            arguments = listOf(navArgument("animeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
            DetailScreen(navController, animeId, viewModel)
        }
        composable(
            route = "player/{videoUrl}",
            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val videoUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            PlayerScreen(navController, videoUrl)
        }
    }
}

// -------------------------------------------------------------
// 1. Splash Screen Component
// -------------------------------------------------------------
@Composable
fun SplashScreen(navController: NavController) {
    var logoScale by remember { mutableStateOf(0.7f) }
    var logoRotate by remember { mutableStateOf(0f) }

    // Navigation and Animation triggers
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            logoScale = value
        }
        animate(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        ) { value, _ ->
            logoRotate = value
        }
        
        delay(1000) // Total Splash Wait Time
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B0C10), Color(0xFF16171D)) // Elegant Pure Dark Gradient
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Vector Streaming Logo Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .rotate(logoRotate)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFF43F5E), Color(0xFFE11D48)) // Premium Rose Gradient
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "App Logo Icon",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANIME ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF43F5E),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "WATCHER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "بوابة الأنمي المفضلة لديك",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFFF43F5E),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// 2. Home Screen Component
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: AnimeViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val animesState by viewModel.animes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Dynamically derive distinct categories from full lists
    val categoriesList = remember(animesState) {
        val list = mutableSetOf("الكل")
        animesState.forEach {
            if (it.category.isNotBlank()) {
                list.add(it.category.trim())
            }
        }
        list.toList()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C10))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ANIME",
                            color = Color(0xFFF43F5E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "WATCHER",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "PREMIUM STREAMING",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16171D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Movie,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        bottomBar = {
            // Static Unity Banner Ad at the bottom of the Home screen
            if (activity != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(Color(0xFF0B0C10)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                val adContainer = UnityAdsManager.createBannerView(activity)
                                addView(adContainer)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B0C10))
                .padding(horizontal = 16.dp)
        ) {
            // Clean Styled Search Field
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("البحث عن الأنمي...", color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search Icon",
                        tint = Color(0xFF94A3B8)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF16171D),
                    unfocusedContainerColor = Color(0xFF16171D),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("search_bar")
            )

            // Featured Hero (Material 3 Style) - Only visible when not searching
            val featuredAnime = animesState.firstOrNull()
            if (searchQuery.isEmpty() && featuredAnime != null) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(bottom = 16.dp)
                        .clickable { navController.navigate("detail/${featuredAnime.id}") },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16171D))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = featuredAnime.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient black overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE11D48))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "شائع الآن • TRENDING NOW",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = featuredAnime.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${featuredAnime.category} • ${featuredAnime.status}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Horizontal Categories Scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                items(categoriesList) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50)) // Fully rounded elegant design chips
                            .background(if (isSelected) Color(0xFFF43F5E) else Color(0xFF16171D))
                            .clickable { viewModel.setSelectedCategory(cat) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Anime Grid (LazyColumn representation with beautiful items)
            if (animesState.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = "No anime",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لم يتم العثور على أي أنميات مضافة",
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("anime_list")
                ) {
                    items(animesState) { anime ->
                        AnimeCardItem(anime) {
                            navController.navigate("detail/${anime.id}")
                        }
                    }
                }
            }
        }
    }
}

// Anime Card item visualizer
@Composable
fun AnimeCardItem(anime: Anime, onClick: () -> Unit) {
    val episodeCount = anime.episodes.size
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16171D)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("anime_card_${anime.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Photo Thumbnail
            AsyncImage(
                model = anime.image,
                contentDescription = "Cover Image for ${anime.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp, 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category badge + Status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF43F5E).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = anime.category.ifBlank { "عام" },
                            color = Color(0xFFF43F5E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    val statusColor = if (anime.status == "مستمر") Color(0xFFF59E0B) else Color(0xFF10B981)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = anime.status,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = anime.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = anime.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SlowMotionVideo,
                        contentDescription = "Episodes",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$episodeCount حلقة",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (anime.year.isNotBlank()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = anime.year,
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Detail Screen Component
// -------------------------------------------------------------
@Composable
fun DetailScreen(navController: NavController, animeId: String, viewModel: AnimeViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val animesState by viewModel.animes.collectAsState()
    val anime = remember(animesState, animeId) {
        animesState.find { it.id == animeId }
    }

    if (anime == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0B0C10)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFF43F5E))
        }
        return
    }

    val episodesSortedList = remember(anime.episodes) {
        anime.episodes.values.toList().sortedBy { ep ->
            ep.number.toDoubleOrNull() ?: 9999.0
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C10))
    ) {
        // Hero Backdrop image & header details
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Large Cover backdrop
                AsyncImage(
                    model = anime.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
                
                // Dark bottom mask overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0B0C10))
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White
                    )
                }
            }
        }

        // Info details block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF43F5E).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = anime.category.ifBlank { "عام" },
                            color = Color(0xFFF43F5E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = anime.status,
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (anime.year.isNotBlank()) {
                        Text(
                            text = "سنة: ${anime.year}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = anime.title,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description card
                Text(
                    text = "القصة",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = anime.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "قائمة الحلقات (${episodesSortedList.size})",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Episodes directory list
        if (episodesSortedList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد حلقات مضافة لهذا الأنمي حالياً.",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            items(episodesSortedList) { ep ->
                EpisodeItemRow(ep) {
                    if (activity != null) {
                        // Play direct Rewarded Ad before watching. Fallback included
                        UnityAdsManager.showRewardedAd(activity) {
                            val encodedUrl = URLEncoder.encode(ep.video, StandardCharsets.UTF_8.toString())
                            navController.navigate("player/$encodedUrl")
                        }
                    } else {
                        val encodedUrl = URLEncoder.encode(ep.video, StandardCharsets.UTF_8.toString())
                        navController.navigate("player/$encodedUrl")
                    }
                }
            }
        }

        // Bottom Spacer padding
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Single Episode card layout
@Composable
fun EpisodeItemRow(ep: Episode, onPlayClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16171D)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("episode_row_${ep.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Badge visual
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF43F5E).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play Episode",
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                val epTitle = if (ep.title.isNotBlank()) " - ${ep.title}" else ""
                Text(
                    text = "الحلقة ${ep.number}$epTitle",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (ep.duration.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المدة: ${ep.duration}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF43F5E)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("مشاهدة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Player Screen Component (Media3 ExoPlayer Screen)
// -------------------------------------------------------------
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(navController: NavController, videoUrl: String) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Auto lock to Landscape on entry for true full-screen, reset back on exit
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Configure and remember ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // Properly release ExoPlayer resource to avoid leaks or continuing audio
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Embed Android PlayerView inside compose
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    // Stretch or maintain ratio correctly
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Overlaid Back action for ergonomics
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close Player",
                tint = Color.White
            )
        }
    }
}

// -------------------------------------------------------------
// View Model Implementation
// -------------------------------------------------------------
class AnimeViewModel : ViewModel() {
    private val viewModelScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()
    )

    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    private val repository = com.example.repository.FirebaseRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory = _selectedCategory.asStateFlow()

    val animes = repository.getAnimesFlow()
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) list else list.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedCategory) { list, category ->
            if (category == "الكل") list else list.filter {
                it.category.contains(category, ignoreCase = true)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }
}
