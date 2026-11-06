package com.dn0ne.player.app.presentation.components.topbar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.fastFirstOrNull
import com.dn0ne.player.app.presentation.components.isSystemInLandscapeOrientation
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridWithCollapsibleTabsTopBar(
    topBarTabs: List<Tab>,
    gridStates: List<LazyGridState>,
    defaultSelectedTab: Tab = Tab.Tracks,
    onTabChange: (tab: Tab) -> Unit = {},
    tabTitleTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    tabRowTitleTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    topBarButtons: @Composable BoxScope.(tab: Tab) -> Unit = {},
    minTopBarHeight: Dp = 60.dp,
    maxTopBarHeight: Dp = 250.dp,
    maxTopBarHeightLandscape: Dp = 150.dp,
    collapseFraction: (Float) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentHorizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.Top,
    enableScrollbar: Boolean = true,
    modifier: Modifier = Modifier,
    gridCells: (tab: Tab) -> GridCells = { GridCells.Fixed(1) },
    tabContent: LazyGridScope.(tab: Tab) -> Unit
) {
    if (topBarTabs.isEmpty()) return
    val initialPage = topBarTabs.indexOf(defaultSelectedTab).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { topBarTabs.size }
    val settledTab = topBarTabs[pagerState.settledPage.coerceIn(topBarTabs.indices)]
    val activeGridState = gridStates[pagerState.currentPage.coerceIn(gridStates.indices)]
    val isScrolledFromTop by remember(activeGridState) {
        derivedStateOf {
            activeGridState.firstVisibleItemIndex > 0 ||
                    activeGridState.firstVisibleItemScrollOffset > 24
        }
    }
    val collapseTarget by animateFloatAsState(
        targetValue = if (isScrolledFromTop) 1f else 0f,
        label = "tabs-header-collapse"
    )
    val maxHeight = if (isSystemInLandscapeOrientation()) maxTopBarHeightLandscape else maxTopBarHeight
    val currentHeight = lerp(maxHeight, minTopBarHeight, collapseTarget)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(collapseTarget) {
        collapseFraction(1f - collapseTarget)
    }
    LaunchedEffect(pagerState.settledPage) {
        onTabChange(settledTab)
    }
    if (settledTab != defaultSelectedTab) {
        BackHandler {
            coroutineScope.launch {
                pagerState.animateScrollToPage(initialPage)
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentHeight)
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                TabHeaderLabels(
                    tabs = topBarTabs,
                    selectedPage = pagerState.currentPage,
                    pagerState = pagerState,
                    collapseFraction = collapseTarget,
                    style = tabRowTitleTextStyle,
                    modifier = Modifier.fillMaxSize()
                )
            }
            topBarButtons(settledTab)
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val tab = topBarTabs[page]
            val gridState = gridStates[page]
            LazyVerticalGridScrollbar(
                state = gridState,
                settings = ScrollbarSettings(
                    enabled = enableScrollbar,
                    thumbUnselectedColor = MaterialTheme.colorScheme.surfaceContainer,
                    thumbSelectedColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                LazyVerticalGrid(
                    columns = gridCells(tab),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    horizontalArrangement = contentHorizontalArrangement,
                    verticalArrangement = contentVerticalArrangement
                ) {
                    tabContent(tab)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.fillMaxWidth().height(200.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabHeaderLabels(
    tabs: List<Tab>,
    selectedPage: Int,
    pagerState: PagerState,
    collapseFraction: Float,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagePosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    val slotDistance = 112.dp

    Box(modifier = modifier.clip(ShapeDefaults.ExtraLarge)) {
        tabs.forEachIndexed { index, tab ->
            val distance = index - pagePosition
            val sideVisibility = (1f - collapseFraction).coerceIn(0f, 1f)
            val centerFraction = (1f - kotlin.math.abs(distance)).coerceIn(0f, 1f)
            val alpha = centerFraction +
                    (0.55f * (1f - centerFraction) * sideVisibility)
            if (kotlin.math.abs(distance) <= 1.25f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(IntrinsicSize.Min)
                        .offset(x = slotDistance * distance)
                        .graphicsLayer { this.alpha = alpha }
                        .clickable(
                            enabled = index != selectedPage && alpha > 0.2f,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = context.resources.getString(tab.titleResId),
                        style = if (index == selectedPage) {
                            style.copy(
                                fontSize = lerp(
                                    MaterialTheme.typography.titleLarge.fontSize,
                                    MaterialTheme.typography.headlineSmall.fontSize,
                                    1f - collapseFraction
                                ),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else style.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(ShapeDefaults.ExtraLarge)
                            .background(
                                if (index == selectedPage) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}
