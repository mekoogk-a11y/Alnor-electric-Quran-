package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppStrings
import com.example.data.local.AyahEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.SurahEntity
import com.example.data.local.UserPreferences
import com.example.ui.components.MushafOrnamentalBorderBox
import com.example.ui.components.SurahHeaderCartouche
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MushafPaperCream
import com.example.ui.theme.MushafPaperInner
import com.example.ui.theme.NightMushafPaper
import com.example.ui.viewmodel.MushafPageUiState
import com.example.ui.viewmodel.MushafViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MushafScreen(
    strings: AppStrings,
    prefs: UserPreferences,
    surahs: List<SurahEntity>,
    bookmarks: List<BookmarkEntity>,
    initialPage: Int,
    mushafViewModel: MushafViewModel,
    onToggleMode: (Boolean) -> Unit,
    onChangeFontSize: (Float) -> Unit,
    onToggleTranslation: (Boolean) -> Unit,
    onToggleBookmark: (pageNumber: Int, surahId: Int, surahNameArabic: String, isCurrentlyBookmarked: Boolean) -> Unit,
    onOpenTafsirForVerse: (verseKey: String) -> Unit,
    onPlaySurahAudio: (surahId: Int) -> Unit
) {
    val pageStates by mushafViewModel.pageStates.collectAsStateWithLifecycle()
    val chapterVerses by mushafViewModel.chapterVerses.collectAsStateWithLifecycle()
    val chapterTranslations by mushafViewModel.chapterTranslations.collectAsStateWithLifecycle()
    val isChapterLoading by mushafViewModel.isChapterLoading.collectAsStateWithLifecycle()
    val chapterError by mushafViewModel.chapterError.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = (initialPage.coerceIn(1, 604) - 1),
        pageCount = { 604 }
    )

    var activeScrollSurahId by remember(initialPage, surahs) {
        val matched = surahs.lastOrNull { it.startPage <= initialPage } ?: surahs.firstOrNull()
        mutableIntStateOf(matched?.id ?: 1)
    }
    var showJumpDialog by remember { mutableStateOf(false) }
    var selectedVerseForSheet by remember { mutableStateOf<AyahEntity?>(null) }

    val currentPageNumber = pagerState.currentPage + 1
    val currentSurahOnPage = remember(surahs, currentPageNumber, pageStates) {
        val versesOnPage = pageStates[currentPageNumber]?.verses
        val firstCh = versesOnPage?.firstOrNull()?.chapterId
        if (firstCh != null) surahs.find { it.id == firstCh }
        else surahs.lastOrNull { it.startPage <= currentPageNumber }
    }
    val isCurrentBookmarked = remember(bookmarks, currentPageNumber) {
        bookmarks.any { it.pageNumber == currentPageNumber }
    }

    LaunchedEffect(initialPage) {
        val targetIdx = (initialPage.coerceIn(1, 604) - 1)
        if (pagerState.currentPage != targetIdx) {
            pagerState.scrollToPage(targetIdx)
        }
    }

    LaunchedEffect(currentPageNumber, prefs.isFlipMode) {
        if (prefs.isFlipMode) {
            mushafViewModel.loadPage(currentPageNumber)
            val sId = currentSurahOnPage?.id ?: 1
            mushafViewModel.saveReadingPosition(currentPageNumber, sId, "$sId:1")
        }
    }

    LaunchedEffect(activeScrollSurahId, prefs.isFlipMode, prefs.showTranslationInScroll) {
        if (!prefs.isFlipMode) {
            val trId = if (prefs.showTranslationInScroll) prefs.selectedTranslationId else null
            mushafViewModel.loadChapter(activeScrollSurahId, trId)
            val surah = surahs.find { it.id == activeScrollSurahId }
            if (surah != null) {
                mushafViewModel.saveReadingPosition(surah.startPage, surah.id, "${surah.id}:1")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mushaf_screen")
    ) {
        // Top Reading Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = prefs.isFlipMode,
                        onClick = { onToggleMode(true) },
                        label = { Text(strings.flipMode, maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_flip_chip")
                    )
                    FilterChip(
                        selected = !prefs.isFlipMode,
                        onClick = { onToggleMode(false) },
                        label = { Text(strings.scrollMode, maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.ViewDay,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_scroll_chip")
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onChangeFontSize(prefs.mushafFontSizeSp - 2f) },
                        modifier = Modifier.testTag("font_decrease_btn")
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease font size")
                    }
                    IconButton(
                        onClick = { onChangeFontSize(prefs.mushafFontSizeSp + 2f) },
                        modifier = Modifier.testTag("font_increase_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase font size")
                    }
                    IconButton(
                        onClick = {
                            val surah = currentSurahOnPage
                            onToggleBookmark(
                                currentPageNumber,
                                surah?.id ?: 1,
                                surah?.nameArabic ?: "الفاتحة",
                                isCurrentBookmarked
                            )
                        },
                        modifier = Modifier.testTag("mushaf_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (isCurrentBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = strings.bookmarksTitle,
                            tint = if (isCurrentBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (prefs.isFlipMode) {
            // MODE A: REALISTIC 3D PAGE FLIP MODE
            val paperColor = if (prefs.isDarkTheme) NightMushafPaper else MushafPaperInner
            val outerBg = if (prefs.isDarkTheme) MaterialTheme.colorScheme.background else MushafPaperCream

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(outerBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { pageIndex ->
                    val pageNum = pageIndex + 1
                    val pageUi = pageStates[pageNum] ?: MushafPageUiState(isLoading = true)
                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)

                    MushafOrnamentalBorderBox(
                        paperColor = paperColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 18f * density
                                rotationY = pageOffset * -42f
                                transformOrigin = if (pageOffset >= 0f) {
                                    TransformOrigin(0f, 0.5f)
                                } else {
                                    TransformOrigin(1f, 0.5f)
                                }
                                val scale = 1f - (pageOffset.absoluteValue * 0.08f).coerceAtMost(0.15f)
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - (pageOffset.absoluteValue * 0.25f).coerceAtMost(0.6f)
                            }
                    ) {
                        MushafPageContent(
                            pageNumber = pageNum,
                            pageUi = pageUi,
                            surahs = surahs,
                            strings = strings,
                            fontSizeSp = prefs.mushafFontSizeSp,
                            onRetry = { mushafViewModel.loadPage(pageNum) },
                            onVerseTap = { selectedVerseForSheet = it }
                        )
                    }
                }
            }

            // Bottom Page Flip Controls
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0,
                        modifier = Modifier.testTag("mushaf_prev_page_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        Text(strings.previousPage)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clickable { showJumpDialog = true }
                            .testTag("mushaf_page_indicator")
                    ) {
                        Text(
                            text = if (strings.isArabic) {
                                "صفحة ${currentPageNumber.toArabicIndicDigits()} / ٦٠٤"
                            } else {
                                "Page $currentPageNumber / 604"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (pagerState.currentPage < 603) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage < 603,
                        modifier = Modifier.testTag("mushaf_next_page_btn")
                    ) {
                        Text(strings.nextPage)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                }
            }
        } else {
            // MODE B: CONTINUOUS SCROLL MODE
            val activeSurah = surahs.find { it.id == activeScrollSurahId }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (activeScrollSurahId > 1) activeScrollSurahId -= 1
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Previous Surah")
                        }
                        Text(
                            text = if (strings.isArabic) {
                                "سورة ${activeSurah?.nameArabic ?: ""}"
                            } else {
                                "${activeSurah?.nameSimple ?: ""}"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = {
                                if (activeScrollSurahId < 114) activeScrollSurahId += 1
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Next Surah")
                        }
                    }

                    FilterChip(
                        selected = prefs.showTranslationInScroll,
                        onClick = { onToggleTranslation(!prefs.showTranslationInScroll) },
                        label = { Text(if (strings.isArabic) "الترجمة" else "Translation") },
                        leadingIcon = {
                            Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            if (isChapterLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (chapterError != null && chapterVerses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(strings.syncPagePrompt, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val trId = if (prefs.showTranslationInScroll) prefs.selectedTranslationId else null
                        mushafViewModel.loadChapter(activeScrollSurahId, trId)
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.retry)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeSurah?.let { surah ->
                        item {
                            SurahHeaderCartouche(
                                surahNameArabic = surah.nameArabic,
                                revelationLabel = if (surah.revelationPlace == "makkah") strings.makki else strings.madani,
                                versesCountLabel = "${surah.versesCount.toArabicIndicDigits()} ${strings.ayahsLabel}",
                                showBismillah = surah.bismillahPre
                            )
                        }
                    }
                    items(chapterVerses, key = { it.verseKey }) { ayah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVerseForSheet = ayah },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (prefs.isDarkTheme) NightMushafPaper else MushafPaperInner
                            ),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${ayah.textUthmani} ﴿${ayah.verseNumber.toArabicIndicDigits()}﴾",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = AmiriQuranFontFamily,
                                        fontSize = prefs.mushafFontSizeSp.sp,
                                        lineHeight = (prefs.mushafFontSizeSp * 1.85f).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                val translation = chapterTranslations[ayah.verseKey]
                                if (prefs.showTranslationInScroll && !translation.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = translation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpDialog) {
        var pageInput by remember { mutableStateOf(currentPageNumber.toString()) }
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text(if (strings.isArabic) "الانتقال إلى صفحة" else "Go to Mushaf Page") },
            text = {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { input -> pageInput = input.filter { it.isDigit() } },
                    label = { Text("1 - 604") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = pageInput.toIntOrNull()?.coerceIn(1, 604) ?: currentPageNumber
                    showJumpDialog = false
                    coroutineScope.launch {
                        pagerState.scrollToPage(target - 1)
                    }
                }) {
                    Text(if (strings.isArabic) "انتقال" else "Go")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text(if (strings.isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Verse Quick Action Dialog (Tafsir / Audio / Info)
    selectedVerseForSheet?.let { ayah ->
        val surah = surahs.find { it.id == ayah.chapterId }
        AlertDialog(
            onDismissRequest = { selectedVerseForSheet = null },
            title = {
                Text(
                    text = if (strings.isArabic) {
                        "سورة ${surah?.nameArabic ?: ""} • آية ${ayah.verseNumber.toArabicIndicDigits()}"
                    } else {
                        "Surah ${surah?.nameSimple ?: ""} • Verse ${ayah.verseNumber}"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = ayah.textUthmani,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AmiriQuranFontFamily,
                            fontSize = 22.sp,
                            lineHeight = 38.sp
                        )
                    )
                    Text(
                        text = "${strings.pageLabel} ${ayah.pageNumber} • ${strings.juzLabel} ${ayah.juzNumber} • ${strings.hizbLabel} ${ayah.hizbNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val key = ayah.verseKey
                    selectedVerseForSheet = null
                    onOpenTafsirForVerse(key)
                }) {
                    Icon(Icons.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.tafsirTitle)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val ch = ayah.chapterId
                    selectedVerseForSheet = null
                    onPlaySurahAudio(ch)
                }) {
                    Text(strings.playSurah)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MushafPageContent(
    pageNumber: Int,
    pageUi: MushafPageUiState,
    surahs: List<SurahEntity>,
    strings: AppStrings,
    fontSizeSp: Float,
    onRetry: () -> Unit,
    onVerseTap: (AyahEntity) -> Unit
) {
    val firstVerse = pageUi.verses.firstOrNull()
    val headerSurah = surahs.find { it.id == (firstVerse?.chapterId ?: 1) }
        ?: surahs.lastOrNull { it.startPage <= pageNumber }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Traditional Mushaf Top Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${strings.juzLabel} ${(firstVerse?.juzNumber ?: headerSurah?.juzStart ?: 1).toArabicIndicDigits()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "سورة ${headerSurah?.nameArabic ?: ""}",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = AmiriQuranFontFamily),
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = GoldAccent.copy(alpha = 0.45f)
        )

        when {
            pageUi.isLoading && pageUi.verses.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(strings.loading, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            pageUi.errorMessage != null && pageUi.verses.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = strings.syncPagePrompt,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onRetry) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.retry)
                        }
                    }
                }
            }

            else -> {
                val groupedByChapter = remember(pageUi.verses) {
                    pageUi.verses.groupBy { it.chapterId }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedByChapter.forEach { (chapterId, verses) ->
                        val surah = surahs.find { it.id == chapterId }
                        val startsHere = verses.firstOrNull()?.verseNumber == 1
                        if (startsHere && surah != null) {
                            SurahHeaderCartouche(
                                surahNameArabic = surah.nameArabic,
                                revelationLabel = if (surah.revelationPlace == "makkah") strings.makki else strings.madani,
                                versesCountLabel = "${surah.versesCount.toArabicIndicDigits()} ${strings.ayahsLabel}",
                                showBismillah = surah.bismillahPre
                            )
                        }

                        // Continuous Quranic flow for authentic Mushaf reading
                        val annotated = buildAnnotatedString {
                            verses.forEachIndexed { idx, ayah ->
                                append(ayah.textUthmani)
                                append(" ")
                                withStyle(
                                    SpanStyle(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("﴿${ayah.verseNumber.toArabicIndicDigits()}﴾")
                                }
                                if (idx < verses.lastIndex) append(" ")
                            }
                        }

                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = AmiriQuranFontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.95f).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Interactive verse chips for Tafsir & Inspection
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            verses.forEach { ayah ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    modifier = Modifier.clickable { onVerseTap(ayah) }
                                ) {
                                    Text(
                                        text = "${ayah.verseNumber.toArabicIndicDigits()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = GoldAccent.copy(alpha = 0.35f)
        )

        Text(
            text = "— ${pageNumber.toArabicIndicDigits()} —",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
