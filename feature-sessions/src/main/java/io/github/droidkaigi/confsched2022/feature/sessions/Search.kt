package io.github.droidkaigi.confsched2022.feature.sessions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.github.droidkaigi.confsched2022.designsystem.components.KaigiScaffold
import io.github.droidkaigi.confsched2022.model.DroidKaigi2022Day
import io.github.droidkaigi.confsched2022.model.DroidKaigiSchedule
import io.github.droidkaigi.confsched2022.model.Filters
import io.github.droidkaigi.confsched2022.model.TimetableItem.Session
import io.github.droidkaigi.confsched2022.model.TimetableItemId
import io.github.droidkaigi.confsched2022.model.TimetableItemWithFavorite
import io.github.droidkaigi.confsched2022.model.fake
import io.github.droidkaigi.confsched2022.ui.UiLoadState.Error
import io.github.droidkaigi.confsched2022.ui.UiLoadState.Loading
import io.github.droidkaigi.confsched2022.ui.UiLoadState.Success

@Composable
fun SearchRoot(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onItemClick: (TimetableItemId) -> Unit,
) {
    val state: SearchUiModel by viewModel.uiModel
    SearchScreen(
        modifier = modifier,
        uiModel = state,
        onItemClick = onItemClick,
        onBookMarkClick = { sessionId, currentIsFavorite ->
            viewModel.onFavoriteToggle(sessionId, currentIsFavorite)
        },
    )
}

@Composable
private fun SearchScreen(
    uiModel: SearchUiModel,
    onItemClick: (TimetableItemId) -> Unit,
    onBookMarkClick: (sessionId: TimetableItemId, currentIsFavorite: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchWord = rememberSaveable { mutableStateOf("") }
    KaigiScaffold(
        modifier = modifier,
        topBar = {},
        content = {
            Column(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
                )
            ) {
                when (uiModel.state) {
                    is Error -> TODO()
                    is Success -> {
                        SearchTextField(searchWord = searchWord.value) {
                            searchWord.value = it
                        }
                        SearchedItemListField(
                            schedule = uiModel.state.value,
                            searchWord = searchWord.value,
                            onItemClick = onItemClick,
                            onBookMarkClick = onBookMarkClick,
                        )
                    }
                    Loading -> {
                        FullScreenLoading()
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun SearchTextField(
    modifier: Modifier = Modifier,
    searchWord: String,
    onSearchWordChange: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = searchWord,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.9F)
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .focusRequester(focusRequester),
            placeholder = { Text("Search Session") },
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "search_icon",
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "search_word_delete_icon",
                    modifier = Modifier.clickable {
                        onSearchWordChange("")
                    }
                )
            },
            onValueChange = {
                onSearchWordChange(it)
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchedItemListField(
    schedule: DroidKaigiSchedule,
    searchWord: String,
    onItemClick: (TimetableItemId) -> Unit,
    onBookMarkClick: (sessionId: TimetableItemId, currentIsFavorite: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        schedule.dayToTimetable.forEach { (dayToTimeTable, timeTable) ->
            val sessions =
                timeTable.filtered(Filters(filterSession = true, searchWord = searchWord)).contents
            if (sessions.isEmpty()) return@forEach
            stickyHeader {
                SearchedHeader(day = dayToTimeTable)
            }
            items(sessions) {
                SearchedItem(
                    timetableItemWithFavorite = it,
                    searchWord = searchWord,
                    onItemClick = onItemClick,
                    onBookMarkClick = onBookMarkClick,
                )
            }
        }
    }
}

@Composable
private fun SearchedHeader(modifier: Modifier = Modifier, day: DroidKaigi2022Day) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.onPrimary)
    ) {
        Text(
            text = day.name,
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchedItem(
    modifier: Modifier = Modifier,
    timetableItemWithFavorite: TimetableItemWithFavorite,
    searchWord: String,
    onItemClick: (TimetableItemId) -> Unit,
    onBookMarkClick: (sessionId: TimetableItemId, currentIsFavorite: Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .wrapContentHeight()
            .clickable { onItemClick.invoke(timetableItemWithFavorite.timetableItem.id) }
    ) {
        Column(modifier = Modifier.padding(start = 15.dp, end = 10.dp, top = 15.dp)) {
            Row {
                val timeTable = timetableItemWithFavorite.timetableItem
                if (timeTable is Session) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .padding(top = 10.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        AsyncImage(
                            model = timeTable.speakers.firstOrNull()?.iconUrl,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentDescription = "Speaker Icon",
                        )
                    }

                    val bookMarkIconResource = if (timetableItemWithFavorite.isFavorited) {
                        R.drawable.ic_bookmark_filled
                    } else {
                        R.drawable.ic_bookmark
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        HighlightedText(
                            text = timeTable.title.currentLangTitle,
                            keyword = searchWord
                        )
                        Text(text = "${timeTable.startsTimeString} 〜")
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(5f)
                                    .height(40.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Badge {
                                    Text(text = timeTable.category.title.currentLangTitle)
                                }
                                Badge(
                                    modifier = Modifier.offset(x = 10.dp),
                                    containerColor = MaterialTheme.colorScheme.onTertiary
                                ) {
                                    Text(text = timeTable.room.name.currentLangTitle)
                                }
                            }
                            Icon(
                                painter = painterResource(id = bookMarkIconResource),
                                contentDescription = "book_mark_icon",
                                modifier = Modifier
                                    .size(30.dp)
                                    .weight(1f)
                                    .clickable {
                                        onBookMarkClick(
                                            timeTable.id,
                                            timetableItemWithFavorite.isFavorited
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HighlightedText(
    text: String,
    keyword: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondary,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary
) {
    val highlightStyle = SpanStyle(color = color, background = backgroundColor)
    val annotatedText = remember(text, keyword) {
        buildAnnotatedString {
            append(text)
            if (keyword.isNotEmpty()) {
                var index = text.indexOf(keyword)
                while (index >= 0) {
                    addStyle(highlightStyle, index, index + keyword.length)
                    index = text.indexOf(keyword, index + 1)
                }
            }
        }
    }
    Text(text = annotatedText, modifier = modifier)
}

@Preview(showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(
        uiModel = SearchUiModel(
            state = Success(DroidKaigiSchedule.fake())
        ),
        onItemClick = {},
        onBookMarkClick = { _, _ -> },
    )
}
