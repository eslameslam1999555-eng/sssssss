package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.BossData
import com.example.ChatMessage
import com.example.GameViewModel
import com.example.MatchmakingState
import com.example.SecurityTransaction
import com.example.database.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Representation of 3D point for projection math
data class Point3D(val x: Float, val y: Float, val z: Float)

// Vertices representing a futuristic space crystal / boss starship shape
val crystal3DVertices = listOf(
    Point3D(0f, -1.8f, 0f),       // Top Vertex
    Point3D(-1f, 0f, -1f),       // Middle Base vertices
    Point3D(1f, 0f, -1f),
    Point3D(1f, 0f, 1f),
    Point3D(-1f, 0f, 1f),
    Point3D(0f, 1.8f, 0f)        // Bottom Vertex
)

val crystal3DEdges = listOf(
    Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4), // Top cone
    Pair(1, 2), Pair(2, 3), Pair(3, 4), Pair(4, 1), // Middle ring
    Pair(5, 1), Pair(5, 2), Pair(5, 3), Pair(5, 4)  // Bottom cone
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(viewModel: GameViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isVRMode by viewModel.isVRModeOn.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    var activeTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Security,
                            contentDescription = "Shield",
                            tint = BioGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedLanguage == "ar") "بوابة ڤورتكس الفضائية" else "VIRTEX VR DASHBOARD",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyberCyan
                        )
                    }
                },
                actions = {
                    // Language Switch Button
                    TextButton(
                        onClick = {
                            viewModel.setLanguage(if (selectedLanguage == "ar") "en" else "ar")
                        },
                        modifier = Modifier.testTag("lang_toggle")
                    ) {
                        Text(
                            text = if (selectedLanguage == "ar") "English" else "العربية",
                            color = BioGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // VR Quick Toggle
                    IconButton(
                        onClick = { viewModel.toggleVRMode() },
                        modifier = Modifier.testTag("vr_mode_ico")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideogameAsset,
                            contentDescription = "VR Mode",
                            tint = if (isVRMode) RubyRedText() else CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpaceSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SpaceSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { 
                        Text(
                            if (selectedLanguage == "ar") "الرئيسية" else "Home", 
                            fontSize = 10.sp, 
                            fontFamily = FontFamily.Monospace
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlack,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    ),
                    modifier = Modifier.testTag("tab_home")
                )

                NavigationBarItem(
                    selected = activeTab == "combat",
                    onClick = { activeTab = "combat" },
                    icon = { Icon(Icons.Filled.VideogameAsset, contentDescription = "Combat") },
                    label = { 
                        Text(
                            if (selectedLanguage == "ar") "المحاكاة" else "Combat", 
                            fontSize = 10.sp, 
                            fontFamily = FontFamily.Monospace
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlack,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    ),
                    modifier = Modifier.testTag("tab_combat")
                )

                NavigationBarItem(
                    selected = activeTab == "arsenal",
                    onClick = { activeTab = "arsenal" },
                    icon = { Icon(Icons.Filled.Carpenter, contentDescription = "Arsenal") },
                    label = { 
                        Text(
                            if (selectedLanguage == "ar") "الأسلحة" else "Arsenal", 
                            fontSize = 10.sp, 
                            fontFamily = FontFamily.Monospace
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlack,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    ),
                    modifier = Modifier.testTag("tab_arsenal")
                )

                NavigationBarItem(
                    selected = activeTab == "shop",
                    onClick = { activeTab = "shop" },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Store") },
                    label = { 
                        Text(
                            if (selectedLanguage == "ar") "المتجر" else "Store", 
                            fontSize = 10.sp, 
                            fontFamily = FontFamily.Monospace
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlack,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    ),
                    modifier = Modifier.testTag("tab_shop")
                )

                NavigationBarItem(
                    selected = activeTab == "support",
                    onClick = { activeTab = "support" },
                    icon = { Icon(Icons.Filled.Android, contentDescription = "Support AI") },
                    label = { 
                        Text(
                            if (selectedLanguage == "ar") "دعم الذكاء" else "Support AI", 
                            fontSize = 10.sp, 
                            fontFamily = FontFamily.Monospace
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlack,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    ),
                    modifier = Modifier.testTag("tab_support")
                )
            }
        },
        containerColor = CyberBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBlack)
        ) {
            // Apply gradient ambient background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radialBrush = Brush.radialGradient(
                    colors = listOf(SpaceSurface.copy(alpha = 0.45f), CyberBlack),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width
                )
                drawRect(brush = radialBrush)
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "MainTabsAnim"
            ) { tab ->
                when (tab) {
                    "home" -> HomeScreenView(viewModel)
                    "combat" -> CombatSimulatorView(viewModel)
                    "arsenal" -> ArsenalCustomizerView(viewModel)
                    "shop" -> StellarShopView(viewModel)
                    "support" -> LiveSupportChatView(viewModel)
                }
            }
        }
    }
}

// ----------------------------------------------------
// UI COLORS HELPERS
// ----------------------------------------------------
fun RubyRedText() = LaserRed
fun PulseCyanText() = CyberCyan

// ----------------------------------------------------
// HOME VIEW
// ----------------------------------------------------
@Composable
fun HomeScreenView(viewModel: GameViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val rProfile by viewModel.playerProfile.collectAsState()
    val isVRActive by viewModel.isVRModeOn.collectAsState()

    val profile = rProfile ?: PlayerProfile()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Credits
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("welcome_card"),
                colors = CardDefaults.cardColors(containerColor = SpaceSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (selectedLanguage == "ar") "🚀 القيادة العليا للنظام" else "🚀 SYSTEM COMMAND HUB",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (selectedLanguage == "ar") {
                            "أهلاً بك القائد الأسطوري Eslam Sobhy في اللعبة الثلاثية الأبعاد الفائقة الحسم المتوافقة مع نظارات الواقع الافتراضي VR!"
                        } else {
                            "Welcome Commander Eslam Sobhy to the ultimate high-fidelity 3D Virtual Reality Space Combat Simulator!"
                        },
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = SpaceWhite,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Credits
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardSurface)
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (selectedLanguage == "ar") "إعتماد تطوير فريد" else "EXCLUSIVE CREDITS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenSun,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ESLAM SOBHY (إسلام صبحي)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BioGreen,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (selectedLanguage == "ar") "المصمم والقائد التنفيذي الأقصى" else "Chief Game Architect & Executive Overlord",
                                fontSize = 10.sp,
                                color = MutedSlate
                            )
                        }
                    }
                }
            }
        }

        // Stats Box
        item {
            Text(
                text = if (selectedLanguage == "ar") "🎖️ الملف الشخصي وسوابق الخدمة" else "🎖️ SERVICE RECORDS & STATISTICS",
                color = MutedSlate,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First Row (Level and Coins)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (selectedLanguage == "ar") "المستوي النشط" else "ACTIVE TIER", fontSize = 10.sp, color = MutedSlate)
                            Text("LEVEL ${profile.currentLevel}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BioGreen)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (selectedLanguage == "ar") "العملات المتوفرة" else "CREDITS BAL", fontSize = 10.sp, color = MutedSlate)
                            Text("${profile.coins} كوينز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldenSun)
                        }
                    }
                }

                // Second Row (Wins and Bosses Defeated)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (selectedLanguage == "ar") "إجمالي الانتصارات" else "TOTAL WINS", fontSize = 10.sp, color = MutedSlate)
                            Text("${profile.totalWins} ⚔️", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (selectedLanguage == "ar") "الزعماء المقاتلين" else "BOSSES SLAIN", fontSize = 10.sp, color = MutedSlate)
                            Text("${profile.bossCompletedLevel} / 4", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LaserRed)
                        }
                    }
                }
            }
        }

        // Fast Quick-Action VR Simulator Toggle and Lobby search
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceSurface),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, MutedSlate.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (selectedLanguage == "ar") "نظارة الواقع الافتراضي VR" else "VR HEADSET COMPATIBILITY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpaceWhite,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (selectedLanguage == "ar") "تفعيل تناصف منظور العينين" else "Enable side-by-side stereoscopy",
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }
                        Switch(
                            checked = isVRActive,
                            onCheckedChange = { viewModel.toggleVRMode() },
                            modifier = Modifier.testTag("vr_toggle_switch")
                        )
                    }
                }
            }
        }

        // Active Game State Logs
        item {
            Text(
                text = if (selectedLanguage == "ar") "🛰️ مراقب الاتصال وتقارير النشاط" else "🛰️ COMBAT NETWORK MONITOR (REALTIME-FEED)",
                color = MutedSlate,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = SpaceSurface)
            ) {
                val logs by viewModel.combatLogs.collectAsState()
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedLanguage == "ar") "المستشعر هادئ، لا توجد تقارير حالية." else "Sensors quiet, no activity registered.",
                            fontSize = 12.sp,
                            color = MutedSlate,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        items(logs) { log ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "> $log",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (log.contains("نجاح") || log.contains("انتصار") || log.contains("Armed") || log.contains("unlocked")) BioGreen else SpaceWhite,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Reset & Admin Config
        item {
            OutlinedButton(
                onClick = { viewModel.scoreReset() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LaserRed),
                border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.5f))
            ) {
                Text(
                    text = if (selectedLanguage == "ar") "🔄 إعادة تصفير تقدم المحارب" else "🔄 HARD REBOOT PROFILE PROGRESS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ----------------------------------------------------
// COMBAT VIEW WITH INTERACTIVE 3D COMPASS WIREFRAME & VR STEREOSCOPIC MODE
// ----------------------------------------------------
@Composable
fun CombatSimulatorView(viewModel: GameViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isVRActive by viewModel.isVRModeOn.collectAsState()
    val bossHp by viewModel.bossHealth.collectAsState()
    val playerHp by viewModel.playerHealth.collectAsState()
    val gameLvl by viewModel.gameLevel.collectAsState()

    val rx by viewModel.rotationX.collectAsState()
    val ry by viewModel.rotationY.collectAsState()

    val matchmakingState by viewModel.matchmaking.collectAsState()

    val activeBoss = viewModel.bosses.firstOrNull { it.level == gameLvl } ?: viewModel.bosses[0]
    val activeWeapon = viewModel.getActiveWeapon()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Boss info badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(activeBoss.primaryColor)))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedLanguage == "ar") "الزعيم الحالي م${activeBoss.level}" else "STAGE ${activeBoss.level} DETECTED",
                        color = MutedSlate,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(android.graphics.Color.parseColor(activeBoss.primaryColor)).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == "ar") "نشط" else "LIVE TARGET",
                            color = Color(android.graphics.Color.parseColor(activeBoss.primaryColor)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (selectedLanguage == "ar") activeBoss.nameAr else activeBoss.nameEn,
                    color = SpaceWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedLanguage == "ar") activeBoss.descriptionAr else activeBoss.descriptionEn,
                    color = MutedSlate,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Levels Select Button Array
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (selectedLanguage == "ar") "اختيار حلقة المواجهة الفضائية" else "SELECT ARENA NODE",
                    fontSize = 10.sp,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..4) {
                        val isCurrent = gameLvl == i
                        Button(
                            onClick = { viewModel.changeActiveLevel(i) },
                            modifier = Modifier.weight(1f).testTag("select_level_$i"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) CyberCyan else SpaceSurface,
                                contentColor = if (isCurrent) CyberBlack else SpaceWhite
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("L$i", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Simulated Multitasking Virtual Reality Dual Split screen viewport
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = CyberBlack),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVRActive) {
                        if (selectedLanguage == "ar") "🕶️ منظار VRStereoscopy ثنائي العينين" else "🕶️ stereoscopic vr cardboard splitscreen"
                    } else {
                        if (selectedLanguage == "ar") "📺 شاشة عرض أحادية المنظور ثلاثية الأبعاد" else "📺 STANDARD STATIC MONOCULAR SCREEN"
                    },
                    fontSize = 11.sp,
                    color = BioGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBlack)
                        .border(1.dp, MutedSlate.copy(alpha = 0.4f))
                ) {
                    val w = maxWidth
                    val h = maxHeight

                    if (isVRActive) {
                        // Drawing stereoscopic side-by-side with parallax displacement
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left Eye Viewport
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                CanvasViewport(
                                    rx = rx,
                                    ry = ry,
                                    eyeOffset = -0.1f, // Parallax Offset
                                    bossHpRatio = bossHp / activeBoss.maxHealth,
                                    playerHpRatio = playerHp / 100f,
                                    laserColor = activeWeapon?.crystalColor ?: "#00DDFF"
                                )
                                Text("L", modifier = Modifier.align(Alignment.TopStart).padding(4.dp), color = MutedSlate, fontSize = 10.sp)
                            }

                            VerticalDivider(
                                color = CyberCyan.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxHeight().width(1.dp)
                            )

                            // Right Eye Viewport
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                CanvasViewport(
                                    rx = rx,
                                    ry = ry,
                                    eyeOffset = 0.1f, // Opposite Parallax offset
                                    bossHpRatio = bossHp / activeBoss.maxHealth,
                                    playerHpRatio = playerHp / 100f,
                                    laserColor = activeWeapon?.crystalColor ?: "#00DDFF"
                                )
                                Text("R", modifier = Modifier.align(Alignment.TopEnd).padding(4.dp), color = MutedSlate, fontSize = 10.sp)
                            }
                        }
                    } else {
                        // Standard Single Larger Viewport
                        CanvasViewport(
                            rx = rx,
                            ry = ry,
                            eyeOffset = 0.0f,
                            bossHpRatio = bossHp / activeBoss.maxHealth,
                            playerHpRatio = playerHp / 100f,
                            laserColor = activeWeapon?.crystalColor ?: "#00DDFF"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Look Slider Simulator to emulate accelerometer look rotation
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (selectedLanguage == "ar") "محاكاة توجيه الرأس (الجيروسكوب): ${rx.toInt()}°" else "Head Orientation Tilt Pitch: ${rx.toInt()}°",
                        fontSize = 11.sp,
                        color = SpaceWhite,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = rx,
                        onValueChange = { viewModel.updateRotation(it - rx, 0f) },
                        valueRange = -90f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = SpaceSurface
                        ),
                        modifier = Modifier.testTag("look_tilt_slider")
                    )
                }
            }
        }

        // Live Health and Target lock indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Armor Ring
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SpaceSurface)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (selectedLanguage == "ar") "درع سفينتك" else "ARMOR INTEGRITY", fontSize = 10.sp, color = MutedSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${playerHp.toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (playerHp > 40) BioGreen else LaserRed)
                    LinearProgressIndicator(
                        progress = { playerHp / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp),
                        color = if (playerHp > 40) BioGreen else LaserRed,
                        trackColor = CyberBlack
                    )
                }
            }

            // Boss Health Ring
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SpaceSurface)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (selectedLanguage == "ar") "صحة الزعيم" else "BOSS HEAT CAPACITY", fontSize = 10.sp, color = MutedSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (bossHp <= 0) (if (selectedLanguage == "ar") "مُسحق!" else "MUTILATED") else "${bossHp.toInt()} HP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(activeBoss.primaryColor))
                    )
                    LinearProgressIndicator(
                        progress = { bossHp / activeBoss.maxHealth },
                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp),
                        color = Color(android.graphics.Color.parseColor(activeBoss.primaryColor)),
                        trackColor = CyberBlack
                    )
                }
            }
        }

        // Active Firing Laser Action Block
        Button(
            onClick = { viewModel.fireActiveWeapon() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("fire_laser_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.FlashOn, "Laser firing")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (bossHp <= 0) {
                    if (selectedLanguage == "ar") "إعادة استدعاء الزعيم للتحدي!" else "RESPAWN CHOSEN EVIL BOSS!"
                } else {
                    if (selectedLanguage == "ar") "إطلاق الليزر بالسلاح النشط!" else "FIRE ACTIVE ENERGY WEAPON!"
                },
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Multiplayer Matching Lobby Screen Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (selectedLanguage == "ar") "📡 بهو اللعب الجماعي التنافسي (عبر الإنترنت)" else "📡 COMPETITIVE MATCHMAKING ARENA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpaceWhite,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedLanguage == "ar") {
                        "خوادمنا السحابية السريعة (سعة 60Hz تيك رايت) تقدم معدل اتصال فائق السرعة وبدون أي تأخير."
                    } else {
                        "Our global high-frequency cloud stack processes packet-relays under a dedicated 120Hz tickrate."
                    },
                    fontSize = 11.sp,
                    color = MutedSlate
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(matchmakingState, label = "MatchmakingAnim") { state ->
                    when (state) {
                        is MatchmakingState.Idle -> {
                            Button(
                                onClick = { viewModel.startFastMatchmaking() },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaceSurface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("find_match_btn"),
                                border = BorderStroke(1.dp, CyberCyan)
                            ) {
                                Text(
                                    text = if (selectedLanguage == "ar") "🔍 ابحث عن مباراة ند لند فورية" else "🔍 INITIALIZE LOW-LATENCY MATCHMAKING",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberCyan
                                )
                            }
                        }

                        is MatchmakingState.Searching -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (selectedLanguage == "ar") "يتم مسح الخادم وترشيح الأنداد..." else "Pinging active secure gateways...",
                                    fontSize = 12.sp,
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { viewModel.cancelMatchmaking() },
                                    modifier = Modifier.testTag("cancel_match_btn")
                                ) {
                                    Text(if (selectedLanguage == "ar") "إلغاء البحث" else "ABORT SEARCH", color = LaserRed, fontSize = 11.sp)
                                }
                            }
                        }

                        is MatchmakingState.Found -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberBlack)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (selectedLanguage == "ar") "✅ تم العثور على خصم!" else "✅ INTERCONNECT LOBBY ESTABLISHED!",
                                    color = BioGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${if (selectedLanguage == "ar") "الخصم القتالي" else "Opponent Fighter"}: ${state.opponent}",
                                    color = SpaceWhite,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "IP Address: ${state.serverIp} | Ping: ${state.latencyMs}ms (Stable)",
                                    color = MutedSlate,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.cancelMatchmaking() },
                                    colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("leave_match_btn")
                                ) {
                                    Text(
                                        text = if (selectedLanguage == "ar") "مغادرة المباراة المقترحة" else "ABORT CONFLICT (LEAVE ARENA)",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        
                        else -> {}
                    }
                }
            }
        }
    }
}

// Custom 3D Projection Canvas Drawing
@Composable
fun CanvasViewport(
    rx: Float,
    ry: Float,
    eyeOffset: Float,
    bossHpRatio: Float,
    playerHpRatio: Float,
    laserColor: String
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val viewportScale = size.width * 0.28f

        // Draw HUD background circular radar lines
        drawCircle(
            color = MutedSlate.copy(alpha = 0.2f),
            radius = size.width * 0.4f,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        )

        drawCircle(
            color = MutedSlate.copy(alpha = 0.15f),
            radius = size.width * 0.25f,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )

        // Draw crosshair axes
        drawLine(
            color = CyberCyan.copy(alpha = 0.35f),
            start = Offset(cx - 40f, cy),
            end = Offset(cx + 40f, cy),
            strokeWidth = 2f
        )
        drawLine(
            color = CyberCyan.copy(alpha = 0.35f),
            start = Offset(cx, cy - 40f),
            end = Offset(cx, cy + 40f),
            strokeWidth = 2f
        )

        // Math projection loop for our 3D polygon crystal representing the boss
        val projected2DPoints = mutableListOf<Offset>()
        
        // Translate angles into radians
        val yawRad = Math.toRadians((ry + eyeOffset * 40f).toDouble())
        val pitchRad = Math.toRadians(rx.toDouble())

        // Calculate trigonometry
        val cosY = cos(yawRad).toFloat()
        val sinY = sin(yawRad).toFloat()
        val cosP = cos(pitchRad).toFloat()
        val sinP = sin(pitchRad).toFloat()

        for (vertex in crystal3DVertices) {
            // Apply scale 
            val scaledX = vertex.x * 1.5f
            val scaledY = vertex.y * 1.5f
            val scaledZ = vertex.z * 1.5f

            // 3D Matrix Rotation (Yaw & Pitch)
            // Yaw (Rotation around Y coordinate)
            val rx1 = scaledX * cosY - scaledZ * sinY
            val rz1 = scaledX * sinY + scaledZ * cosY

            // Pitch (Rotation around X coordinate)
            val ry2 = scaledY * cosP - rz1 * sinP
            val rz2 = scaledY * sinP + rz1 * cosP

            // Add Depth Offset (Shift camera backward)
            val camZ = rz2 + 4.2f

            // Perspective Division screen projection
            val px = cx + (rx1 / camZ) * viewportScale
            val py = cy + (ry2 / camZ) * viewportScale

            projected2DPoints.add(Offset(px, py))
        }

        // Draw 3D wireframe edges
        for (edge in crystal3DEdges) {
            val pt1 = projected2DPoints.getOrNull(edge.first)
            val pt2 = projected2DPoints.getOrNull(edge.second)
            if (pt1 != null && pt2 != null) {
                drawLine(
                    color = Color(android.graphics.Color.parseColor(laserColor)).copy(alpha = 0.8f),
                    start = pt1,
                    end = pt2,
                    strokeWidth = 4f
                )
            }
        }

        // Draw vertex points
        for (pt in projected2DPoints) {
            drawCircle(
                color = CyberCyan,
                radius = 6f,
                center = pt
            )
        }

        // Firing indicator visual if boss has taken damage
        if (bossHpRatio > 0 && bossHpRatio < 1f) {
            val bossColorVal = Color(android.graphics.Color.parseColor(laserColor))
            drawLine(
                color = bossColorVal.copy(alpha = 0.7f),
                start = Offset(0f, size.height),
                end = projected2DPoints.getOrNull(5) ?: Offset(cx, cy),
                strokeWidth = 6f
            )
            drawLine(
                color = bossColorVal.copy(alpha = 0.7f),
                start = Offset(size.width, size.height),
                end = projected2DPoints.getOrNull(5) ?: Offset(cx, cy),
                strokeWidth = 6f
            )
        }
    }
}

// ----------------------------------------------------
// WEAPONS ARSENAL CUSTOMIZER VIEW WITH ROOM SYNC
// ----------------------------------------------------
@Composable
fun ArsenalCustomizerView(viewModel: GameViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val weapons by viewModel.allWeapons.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    val currentWeapon = viewModel.getActiveWeapon()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (selectedLanguage == "ar") "🎒 مستودع تخصيص الأسلحة والقدرات" else "🎒 WEAPONS CUSTOMIZATION DEPOT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CyberCyan
            )
        }

        if (currentWeapon != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceSurface),
                    border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(currentWeapon.crystalColor)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (selectedLanguage == "ar") "⚡ السلاح النشط المجهز حالياً" else "⚡ PRIMARY ARMED WEAPON CORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedSlate,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(currentWeapon.crystalColor)))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = currentWeapon.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpaceWhite,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Weapon stats stats
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(if (selectedLanguage == "ar") "ضرر الطاقة" else "DAMAGE CAPACITY", fontSize = 10.sp, color = MutedSlate)
                                Text("${currentWeapon.damage.toInt()} DP", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LaserRed)
                            }

                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(if (selectedLanguage == "ar") "معدل الإطلاق" else "DISCHARGE SPEED", fontSize = 10.sp, color = MutedSlate)
                                Text("LVL ${currentWeapon.speed.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BioGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Upgrade Button
                        Button(
                            onClick = { viewModel.upgradeWeaponDamage(currentWeapon) },
                            modifier = Modifier.fillMaxWidth().testTag("upgrade_energy_weapon_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(android.graphics.Color.parseColor(currentWeapon.crystalColor))),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Bolt, "upgrade bolt")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedLanguage == "ar") "ترقية الضرر بقوة (+8.5 DP) - تكلفة 450 كوينز" else "OVERCLOCK INTENSITY (+8.5 DP) - 450 CR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyberBlack
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (selectedLanguage == "ar") "ترسانة الأسلحة المتوفرة بالمركبة" else "AVAILABLE SPACECRAFT ARSENAL",
                color = MutedSlate,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(weapons) { weapon ->
            val isEquipped = currentWeapon?.id == weapon.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (weapon.isUnlocked) {
                            viewModel.selectActiveWeapon(weapon)
                        }
                    }
                    .testTag("weapon_item_${weapon.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEquipped) CardSurface else SpaceSurface
                ),
                border = if (isEquipped) BorderStroke(1.dp, BioGreen) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(weapon.crystalColor)))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = weapon.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (weapon.isUnlocked) SpaceWhite else MutedSlate,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${weapon.type} • ${weapon.attackType}",
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }
                    }

                    Box {
                        if (weapon.isUnlocked) {
                            if (isEquipped) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Check, null, tint = BioGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (selectedLanguage == "ar") "مجهز" else "EQUIP", fontSize = 11.sp, color = BioGreen, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(if (selectedLanguage == "ar") "اضغط للتجهيز" else "TOUCH TO ARM", fontSize = 11.sp, color = CyberCyan)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lock, null, tint = LaserRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${weapon.cost} CR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenSun
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// EXCLUSIVE STORE & OUTSHOP WITH AES-SECURED LOGS
// ----------------------------------------------------
@Composable
fun StellarShopView(viewModel: GameViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val weapons by viewModel.allWeapons.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val coins = profile?.coins ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedLanguage == "ar") "رصيد الائتمان الكوني" else "COSMIC TRUST WALLET",
                            fontSize = 11.sp,
                            color = MutedSlate,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$coins كوينز",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldenSun,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = GoldenSun, modifier = Modifier.size(36.dp))
                }
            }
        }

        item {
            Text(
                text = if (selectedLanguage == "ar") "🛒 المعدات والترقيات الحصرية المعروضة" else "🛒 EXCLUSIVE GEAR & SKINS STOCK",
                color = MutedSlate,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val lockedWeapons = weapons.filter { !it.isUnlocked }
        if (lockedWeapons.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceSurface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedLanguage == "ar") "🎉 أحسنت! قمت بشراء كافة الأسلحة المتاحة بالتكامل التام." else "🎉 Mastery! You have fully decrypted all available custom equipment.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = BioGreen
                        )
                    }
                }
            }
        } else {
            items(lockedWeapons) { weapon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(weapon.name, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = SpaceWhite)
                                Text("${weapon.type} (Damage: ${weapon.damage.toInt()})", fontSize = 11.sp, color = MutedSlate)
                            }
                            Text("${weapon.cost} كوينز", color = GoldenSun, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.purchaseWeapon(weapon) },
                            modifier = Modifier.fillMaxWidth().testTag("buy_weapon_${weapon.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Icon(Icons.Filled.ShoppingCart, "Buy store item", tint = CyberBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedLanguage == "ar") "شراء آمن خالي من الثغرات" else "AUTHORIZE COMPILER SECURE PAY",
                                color = CyberBlack,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Transactions digital receipt logs representing "ضمان أمان تام لكافة المعاملات المالية"
        item {
            Text(
                text = if (selectedLanguage == "ar") "🛡️ سجل إثبات المعاملات المشفر آمن" else "🛡️ VERIFIED CRYPTO-AUTHENTICATION RECORD RECEIPTS",
                color = MutedSlate,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MutedSlate.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedLanguage == "ar") "لا توجد مدفوعات مسجلة في الجلسة الحالية." else "No verified transaction logs recorded within this session.",
                        fontSize = 11.sp,
                        color = MutedSlate,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceSurface),
                    border = BorderStroke(1.dp, BioGreen.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tx.transactionId, color = BioGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Text(
                                if (selectedLanguage == "ar") "مؤمن بالكامل" else "SECURED_GATEWAY",
                                color = BioGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("${if (selectedLanguage == "ar") "المعدات" else "Acquired Gear"}: ${tx.itemName}", fontSize = 12.sp, color = SpaceWhite)
                        Text("Deducted: ${tx.price} Credits", fontSize = 11.sp, color = GoldenSun)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "SHA-Hash: ${tx.hash}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MutedSlate
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// GEMINI INTELLIGENT ADVISOR CHAT (LORE & TACTICS SUPPORT)
// ----------------------------------------------------
@Composable
fun LiveSupportChatView(viewModel: GameViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var userQueryText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BioGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (selectedLanguage == "ar") "مستشار الدعم الفني الذكي 24/7" else "24/7 EXECUTIVE ADVISOR CHATBOT",
                        fontSize = 11.sp,
                        color = MutedSlate,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (selectedLanguage == "ar") "تواصل مباشر مع عقل الهيكل الأقصى Eslam Sobhy AI" else "Linked directly to Eslam Sobhy AI Command Structure",
                        fontSize = 11.sp,
                        color = SpaceWhite
                    )
                }
            }
        }

        // Suggestions buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = if (selectedLanguage == "ar") {
                listOf(
                    "كيف أهزم الزعيم الأخير إسلام صبحي؟" to "كيف اهزم الزعيم النهائي؟",
                    "ما هي ترقيات السلاح الموصى بها؟" to "ما هي افضل ترقيات للاسلحة؟",
                    "كيف أشغل نظارة الواقع الافتراضي VR؟" to "طريقة تشغيل نظارات الواقع الافتراضي"
                )
            } else {
                listOf(
                    "How to beat Overlord Eslam Sobhy?" to "How to defeat the final boss?",
                    "Best damage weapon customization?" to "Best weapon customizations?",
                    "How to load VR on my headset?" to "VR configurations?"
                )
            }

            for (sug in suggestions) {
                Card(
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(sug.second)
                    },
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = sug.first,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Bubble list
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SpaceSurface)
                .border(1.dp, MutedSlate.copy(alpha = 0.2f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatMessages) { msg ->
                    val isPlayer = msg.sender == "Player"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isPlayer) 12.dp else 0.dp,
                                        bottomEnd = if (isPlayer) 0.dp else 12.dp
                                    )
                                )
                                .background(if (isPlayer) CyberCyan else CardSurface)
                                .border(
                                    1.dp,
                                    if (isPlayer) CyberCyan else BioGreen.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .widthIn(max = 240.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isPlayer) (if (selectedLanguage == "ar") "المحارب" else "Warrior") else "ESLAM_SOBHY_AI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isPlayer) CyberBlack else BioGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isPlayer) CyberBlack else SpaceWhite,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.Start) {
                            Card(colors = CardDefaults.cardColors(containerColor = CardSurface)) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BioGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedLanguage == "ar") "الذكاء الاصطناعي يخوض معالجة البيانات..." else "Analyzing lore databases...",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = BioGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Text query entry box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = userQueryText,
                onValueChange = { userQueryText = it },
                modifier = Modifier.weight(1.0f).testTag("chat_input_field"),
                placeholder = {
                    Text(
                        if (selectedLanguage == "ar") "اسأل المطور إسلام صبي الذكاء الاصطناعي..." else "Message Eslam Sobhy AI adviser...",
                        fontSize = 11.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpaceWhite,
                    unfocusedTextColor = SpaceWhite,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = SpaceSurface,
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = MutedSlate
                ),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (userQueryText.trim().isNotEmpty()) {
                            viewModel.sendChatMessage(userQueryText)
                            userQueryText = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (userQueryText.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(userQueryText)
                        userQueryText = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberCyan)
                    .testTag("send_chat_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = CyberBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
