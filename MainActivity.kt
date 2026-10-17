package com.example.wallpro

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class Wallpaper(
    val id: Int,
    val title: String,
    val category: String,
    val imageUrl: String,
    val isPremium: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFFD700),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E)
                )
            ) {
                WallpaperAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperAppScreen() {
    val context = LocalContext.current
    var isUserPremium by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    val categories = listOf("All", "Dark & Amoled", "Cinema", "Football", "Minimal")
    var selectedCategory by remember { mutableStateOf("All") }

    val wallpapers = remember {
        listOf(
            Wallpaper(1, "Neon Cyber", "Dark & Amoled", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1080&q=80", false),
            Wallpaper(2, "Dark Knight", "Cinema", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1080&q=80", true),
            Wallpaper(3, "Stadium Lights", "Football", "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=1080&q=80", true),
            Wallpaper(4, "Clean Desert", "Minimal", "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=1080&q=80", false),
            Wallpaper(5, "Black Abyss", "Dark & Amoled", "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1080&q=80", true),
            Wallpaper(6, "Retro Vibes", "Cinema", "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1080&q=80", false)
        )
    }

    val filteredWallpapers = if (selectedCategory == "All") {
        wallpapers
    } else {
        wallpapers.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WallCraft 4K", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    if (!isUserPremium) {
                        Button(
                            onClick = { showPaywallDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PRO", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Badge(containerColor = Color(0xFF4CAF50), modifier = Modifier.padding(end = 16.dp)) {
                            Text("VIP ACTIVE", color = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700),
                            selectedLabelColor = Color.Black,
                            labelColor = Color.White
                        )
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredWallpapers) { wallpaper ->
                    WallpaperItemCard(
                        wallpaper = wallpaper,
                        isUnlocked = !wallpaper.isPremium || isUserPremium,
                        onClick = {
                            if (wallpaper.isPremium && !isUserPremium) {
                                showPaywallDialog = true
                            } else {
                                selectedWallpaper = wallpaper
                            }
                        }
                    )
                }
            }
        }
    }

    selectedWallpaper?.let { wallpaper ->
        FullScreenWallpaperDialog(
            wallpaper = wallpaper,
            onDismiss = { selectedWallpaper = null },
            onSetWallpaper = { flag ->
                setDeviceWallpaper(context, wallpaper.imageUrl, flag)
            }
        )
    }

    if (showPaywallDialog) {
        PaywallDialog(
            onDismiss = { showPaywallDialog = false },
            onPurchaseSuccess = {
                isUserPremium = true
                showPaywallDialog = false
                Toast.makeText(context, "نسخه پریمیوم با موفقیت فعال شد! 🎉", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun WallpaperItemCard(wallpaper: Wallpaper, isUnlocked: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 300f
                    )
                )
        )

        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xFFFFD700), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PRO", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = wallpaper.title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

@Composable
fun FullScreenWallpaperDialog(
    wallpaper: Wallpaper,
    onDismiss: () -> Unit,
    onSetWallpaper: (Int) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = wallpaper.imageUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showOptions) {
                    Button(
                        onClick = { showOptions = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Set as Wallpaper", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Text("Apply to:", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onSetWallpaper(WallpaperManager.FLAG_SYSTEM); onDismiss() }) {
                            Text("Home", color = Color.White)
                        }
                        OutlinedButton(onClick = { onSetWallpaper(WallpaperManager.FLAG_LOCK); onDismiss() }) {
                            Text("Lock", color = Color.White)
                        }
                        Button(
                            onClick = {
                                onSetWallpaper(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("Both", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun PaywallDialog(onDismiss: () -> Unit, onPurchaseSuccess: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upgrade to WallCraft PRO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Unlock all exclusive 4K wallpapers and remove all limits:", color = Color.LightGray)
                Row { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)); Spacer(Modifier.width(8.dp)); Text("Access to 500+ Exclusive PRO Wallpapers", color = Color.White) }
                Row { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)); Spacer(Modifier.width(8.dp)); Text("Ultra HD 4K & AMOLED Quality", color = Color.White) }
                Row { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)); Spacer(Modifier.width(8.dp)); Text("One-time payment (Lifetime Access)", color = Color.White) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPurchaseSuccess() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("Unlock Lifetime - $2.99", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later", color = Color.Gray)
            }
        }
    )
}

fun setDeviceWallpaper(context: Context, imageUrl: String, flag: Int) {
    Toast.makeText(context, "Setting wallpaper, please wait...", Toast.LENGTH_SHORT).show()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = URL(imageUrl).openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap, null, true, flag)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Wallpaper applied successfully! ✨", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
