package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.getAppStrings
import com.example.ui.components.MiniAudioPlayerBar
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemorizationScreen
import com.example.ui.screens.MushafScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SurahIndexScreen
import com.example.ui.screens.TafsirScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.theme.AlNoorTheme
import com.example.ui.viewmodel.ExploreViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MushafViewModel

object AlNoorRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val INDEX = "index"
    const val MUSHAF = "mushaf"
    const val AUDIO = "audio"
    const val SETTINGS = "settings"
    const val TAFSIR = "tafsir"
    const val SEARCH = "search"
    const val MEMORIZATION = "memorization"
    const val ADHKAR = "adhkar"
    const val TASBIH = "tasbih"
    const val PRAYER_TIMES = "prayer_times"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlNoorAppRoot(
    mainViewModel: MainViewModel,
    mushafViewModel: MushafViewModel,
    exploreViewModel: ExploreViewModel
) {
    val prefs by mainViewModel.preferences.collectAsStateWithLifecycle()
    val surahs by mainViewModel.surahs.collectAsStateWithLifecycle()
    val bookmarks by mainViewModel.bookmarks.collectAsStateWithLifecycle()
    val memorizationList by mainViewModel.memorizationList.collectAsStateWithLifecycle()
    val tasbihSessions by mainViewModel.tasbihSessions.collectAsStateWithLifecycle()
    val downloadedAudio by mainViewModel.downloadedAudio.collectAsStateWithLifecycle()
    val cachedVersesCount by mainViewModel.cachedVersesCount.collectAsStateWithLifecycle()
    val cachedPagesCount by mainViewModel.cachedPagesCount.collectAsStateWithLifecycle()
    val downloadProgress by mainViewModel.downloadProgress.collectAsStateWithLifecycle()
    val audioState by mainViewModel.audioState.collectAsStateWithLifecycle()
    val verificationReport by mainViewModel.verificationReport.collectAsStateWithLifecycle()

    val strings = remember(prefs.isArabic) { getAppStrings(prefs.isArabic) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AlNoorRoutes.SPLASH

    var targetMushafPage by remember { mutableIntStateOf(prefs.lastReadPage) }
    var targetTafsirVerse by remember { mutableStateOf<String?>(null) }

    val isSplash = currentRoute == AlNoorRoutes.SPLASH
    val topLevelRoutes = setOf(
        AlNoorRoutes.HOME,
        AlNoorRoutes.INDEX,
        AlNoorRoutes.MUSHAF,
        AlNoorRoutes.AUDIO,
        AlNoorRoutes.SETTINGS
    )
    val baseRoute = currentRoute.substringBefore("?").substringBefore("/")
    val showBottomBar = !isSplash && baseRoute in topLevelRoutes
    val showTopBar = !isSplash && baseRoute !in setOf(AlNoorRoutes.HOME)

    val screenTitle = when (baseRoute) {
        AlNoorRoutes.INDEX -> strings.quranIndex
        AlNoorRoutes.MUSHAF -> strings.mushaf
        AlNoorRoutes.AUDIO -> strings.audio
        AlNoorRoutes.SETTINGS -> strings.settingsTitle
        AlNoorRoutes.TAFSIR -> strings.tafsirTitle
        AlNoorRoutes.SEARCH -> strings.searchQuran
        AlNoorRoutes.MEMORIZATION -> strings.memorizationTitle
        AlNoorRoutes.ADHKAR -> strings.adhkarTitle
        AlNoorRoutes.TASBIH -> strings.tasbihTitle
        AlNoorRoutes.PRAYER_TIMES -> strings.prayerTimesTitle
        else -> strings.appTitle
    }

    val isExpandedWidth = LocalConfiguration.current.screenWidthDp >= 720

    val navItems = listOf(
        Triple(AlNoorRoutes.HOME, strings.home, Icons.Filled.Home),
        Triple(AlNoorRoutes.INDEX, strings.quranIndex, Icons.Filled.FormatListNumbered),
        Triple(AlNoorRoutes.MUSHAF, strings.mushaf, Icons.AutoMirrored.Filled.MenuBook),
        Triple(AlNoorRoutes.AUDIO, strings.audio, Icons.Filled.Headphones),
        Triple(AlNoorRoutes.SETTINGS, strings.more, Icons.Filled.Settings)
    )

    AlNoorTheme(
        darkTheme = prefs.isDarkTheme,
        isArabic = prefs.isArabic
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(screenTitle, maxLines = 1) },
                        navigationIcon = {
                            if (baseRoute !in topLevelRoutes) {
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.testTag("top_back_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomBar && !isExpandedWidth) {
                    Column {
                        if (baseRoute != AlNoorRoutes.AUDIO) {
                            MiniAudioPlayerBar(
                                audioState = audioState,
                                onTogglePlayPause = { mainViewModel.toggleAudioPlayPause() },
                                onOpenAudioScreen = {
                                    navController.navigate(AlNoorRoutes.AUDIO) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        NavigationBar {
                            navItems.forEach { (route, label, icon) ->
                                NavigationBarItem(
                                    selected = baseRoute == route,
                                    onClick = {
                                        if (route == AlNoorRoutes.MUSHAF) {
                                            targetMushafPage = prefs.lastReadPage
                                        }
                                        navController.navigate(route) {
                                            popUpTo(AlNoorRoutes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_$route")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showBottomBar && isExpandedWidth) {
                    NavigationRail {
                        navItems.forEach { (route, label, icon) ->
                            NavigationRailItem(
                                selected = baseRoute == route,
                                onClick = {
                                    if (route == AlNoorRoutes.MUSHAF) {
                                        targetMushafPage = prefs.lastReadPage
                                    }
                                    navController.navigate(route) {
                                        popUpTo(AlNoorRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = if (baseRoute == AlNoorRoutes.MUSHAF) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                    }

                    Box(modifier = contentModifier) {
                        NavHost(
                            navController = navController,
                            startDestination = AlNoorRoutes.SPLASH
                        ) {
                            composable(AlNoorRoutes.SPLASH) {
                                SplashScreen(
                                    strings = strings,
                                    onFinished = {
                                        navController.navigate(AlNoorRoutes.HOME) {
                                            popUpTo(AlNoorRoutes.SPLASH) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(AlNoorRoutes.HOME) {
                                HomeScreen(
                                    strings = strings,
                                    prefs = prefs,
                                    surahs = surahs,
                                    cachedPagesCount = cachedPagesCount,
                                    downloadProgress = downloadProgress,
                                    onToggleLanguage = { mainViewModel.setArabic(!prefs.isArabic) },
                                    onToggleTheme = { mainViewModel.setDarkTheme(!prefs.isDarkTheme) },
                                    onOpenMushaf = { page ->
                                        targetMushafPage = page
                                        navController.navigate(AlNoorRoutes.MUSHAF)
                                    },
                                    onOpenIndex = { navController.navigate(AlNoorRoutes.INDEX) },
                                    onOpenAudio = { navController.navigate(AlNoorRoutes.AUDIO) },
                                    onOpenTafsir = {
                                        targetTafsirVerse = prefs.lastReadVerseKey
                                        navController.navigate(AlNoorRoutes.TAFSIR)
                                    },
                                    onOpenSearch = { navController.navigate(AlNoorRoutes.SEARCH) },
                                    onOpenMemorization = { navController.navigate(AlNoorRoutes.MEMORIZATION) },
                                    onOpenAdhkar = { navController.navigate(AlNoorRoutes.ADHKAR) },
                                    onOpenTasbih = { navController.navigate(AlNoorRoutes.TASBIH) },
                                    onOpenPrayerTimes = { navController.navigate(AlNoorRoutes.PRAYER_TIMES) },
                                    onOpenSettings = { navController.navigate(AlNoorRoutes.SETTINGS) },
                                    onStartOfflineDownload = { mainViewModel.startFullQuranDownload() }
                                )
                            }

                            composable(AlNoorRoutes.INDEX) {
                                SurahIndexScreen(
                                    strings = strings,
                                    surahs = surahs,
                                    bookmarks = bookmarks,
                                    onOpenPage = { page ->
                                        targetMushafPage = page
                                        navController.navigate(AlNoorRoutes.MUSHAF)
                                    },
                                    onDeleteBookmark = { id -> mainViewModel.removeBookmarkById(id) }
                                )
                            }

                            composable(AlNoorRoutes.MUSHAF) {
                                MushafScreen(
                                    strings = strings,
                                    prefs = prefs,
                                    surahs = surahs,
                                    bookmarks = bookmarks,
                                    initialPage = targetMushafPage,
                                    mushafViewModel = mushafViewModel,
                                    onToggleMode = { mainViewModel.setFlipMode(it) },
                                    onChangeFontSize = { mainViewModel.setFontSize(it) },
                                    onToggleTranslation = { mainViewModel.setShowTranslationInScroll(it) },
                                    onToggleBookmark = { page, surahId, surahName, isBookmarked ->
                                        if (isBookmarked) {
                                            mainViewModel.removeBookmarkByPage(page)
                                        } else {
                                            mainViewModel.addBookmark(page, surahId, surahName, "$surahId:1")
                                        }
                                    },
                                    onOpenTafsirForVerse = { verseKey ->
                                        targetTafsirVerse = verseKey
                                        navController.navigate(AlNoorRoutes.TAFSIR)
                                    },
                                    onPlaySurahAudio = { surahId ->
                                        val surah = surahs.find { it.id == surahId }
                                        exploreViewModel.playSurah(
                                            reciterId = prefs.selectedReciterId,
                                            reciterName = "مشاري راشد العفاسي",
                                            surahId = surahId,
                                            surahNameArabic = surah?.nameArabic ?: "",
                                            surahNameEnglish = surah?.nameSimple ?: ""
                                        )
                                        navController.navigate(AlNoorRoutes.AUDIO)
                                    }
                                )
                            }

                            composable(AlNoorRoutes.AUDIO) {
                                AudioScreen(
                                    strings = strings,
                                    prefs = prefs,
                                    surahs = surahs,
                                    downloadedAudio = downloadedAudio,
                                    audioState = audioState,
                                    exploreViewModel = exploreViewModel,
                                    onTogglePlayPause = { mainViewModel.toggleAudioPlayPause() },
                                    onSeek = { mainViewModel.seekAudio(it) },
                                    onChangeSpeed = { mainViewModel.setAudioSpeed(it) },
                                    onCycleRepeat = { mainViewModel.cycleAudioRepeat() }
                                )
                            }

                            composable(AlNoorRoutes.TAFSIR) {
                                TafsirScreen(
                                    strings = strings,
                                    prefs = prefs,
                                    surahs = surahs,
                                    initialVerseKey = targetTafsirVerse,
                                    exploreViewModel = exploreViewModel
                                )
                            }

                            composable(AlNoorRoutes.SEARCH) {
                                SearchScreen(
                                    strings = strings,
                                    surahs = surahs,
                                    exploreViewModel = exploreViewModel,
                                    onOpenPage = { page ->
                                        targetMushafPage = page
                                        navController.navigate(AlNoorRoutes.MUSHAF)
                                    },
                                    onOpenTafsir = { verseKey ->
                                        targetTafsirVerse = verseKey
                                        navController.navigate(AlNoorRoutes.TAFSIR)
                                    }
                                )
                            }

                            composable(AlNoorRoutes.MEMORIZATION) {
                                MemorizationScreen(
                                    strings = strings,
                                    surahs = surahs,
                                    memorizationList = memorizationList,
                                    reminderEnabled = prefs.memorizationReminderEnabled,
                                    onToggleReminder = { mainViewModel.setMemorizationReminder(it) },
                                    onUpdateMemorization = { mainViewModel.updateMemorization(it) },
                                    onOpenSurahPage = { page ->
                                        targetMushafPage = page
                                        navController.navigate(AlNoorRoutes.MUSHAF)
                                    }
                                )
                            }

                            composable(AlNoorRoutes.ADHKAR) {
                                AdhkarScreen(
                                    strings = strings,
                                    onOpenMushafPage = { page ->
                                        targetMushafPage = page
                                        navController.navigate(AlNoorRoutes.MUSHAF)
                                    }
                                )
                            }

                            composable(AlNoorRoutes.TASBIH) {
                                TasbihScreen(
                                    strings = strings,
                                    sessions = tasbihSessions,
                                    onIncrement = { mainViewModel.incrementTasbih(it) },
                                    onDecrement = { mainViewModel.decrementTasbih(it) },
                                    onReset = { mainViewModel.resetTasbih(it) },
                                    onUpdateTarget = { s, t -> mainViewModel.updateTasbihTarget(s, t) }
                                )
                            }

                            composable(AlNoorRoutes.PRAYER_TIMES) {
                                PrayerTimesScreen(
                                    strings = strings,
                                    prefs = prefs,
                                    onSelectCity = { mainViewModel.setPrayerCity(it) },
                                    onCustomLocation = { lat, lon, label ->
                                        mainViewModel.setCustomLocation(lat, lon, label)
                                    },
                                    onSelectMethod = { mainViewModel.setCalculationMethod(it) }
                                )
                            }

                            composable(AlNoorRoutes.SETTINGS) {
                                SettingsScreen(
                                    strings = strings,
                                    prefs = prefs,
                                    cachedVersesCount = cachedVersesCount,
                                    cachedPagesCount = cachedPagesCount,
                                    downloadProgress = downloadProgress,
                                    verificationReport = verificationReport,
                                    onSetArabic = { mainViewModel.setArabic(it) },
                                    onSetDarkTheme = { mainViewModel.setDarkTheme(it) },
                                    onSetFontSize = { mainViewModel.setFontSize(it) },
                                    onSetFlipMode = { mainViewModel.setFlipMode(it) },
                                    onStartFullDownload = { mainViewModel.startFullQuranDownload() },
                                    onRunVerification = { mainViewModel.runVerification() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
