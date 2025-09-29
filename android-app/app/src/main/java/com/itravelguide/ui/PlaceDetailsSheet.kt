package com.itravelguide.ui

import android.content.res.AssetManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itravelguide.R
import com.itravelguide.data.Place
import com.itravelguide.data.PlacesRepository
import com.itravelguide.data.QuestionGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsSheet(
    place: Place,
    isVietnamese: Boolean,
    onDismiss: () -> Unit,
    assetManager: AssetManager,
) {
    val repository = remember { PlacesRepository() }
    val description = remember { mutableStateOf<String?>(null) }
    val novel = remember { mutableStateOf<String?>(null) }
    val selectedTab = remember { mutableStateOf(DetailsTab.INFO) }
    var generatedQuestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val infoScrollState = rememberScrollState()
    val novelScrollState = rememberScrollState()

    LaunchedEffect(place, isVietnamese) {
        infoScrollState.scrollTo(0)
        novelScrollState.scrollTo(0)
        selectedTab.value = DetailsTab.INFO
        val text = withContext(Dispatchers.IO) {
            runCatching {
                repository.loadDescription(assetManager, place.readAssetPath(isVietnamese))
            }.getOrNull()
        }
        description.value = text
        val novelPath = place.novelAssetPath(isVietnamese)
        val novelText = if (novelPath != null) {
            withContext(Dispatchers.IO) {
                runCatching {
                    repository.loadDescription(assetManager, novelPath)
                }.getOrNull()
            }
        } else null
        novel.value = novelText
        if (novelText == null) {
            selectedTab.value = DetailsTab.INFO
        }
        generatedQuestions = emptyList()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        val tabs = buildList {
            add(DetailsTab.INFO)
            if (!novel.value.isNullOrBlank()) add(DetailsTab.NOVEL)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = place.displayName(isVietnamese),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (tabs.size > 1) {
                TabRow(selectedTabIndex = tabs.indexOf(selectedTab.value)) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = selectedTab.value == tab,
                            onClick = { selectedTab.value = tab },
                            text = {
                                Text(
                                    text = when (tab) {
                                        DetailsTab.INFO -> stringResource(id = R.string.place_info_tab)
                                        DetailsTab.NOVEL -> stringResource(id = R.string.place_novel_tab)
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (selectedTab.value) {
                DetailsTab.INFO -> {
                    Column(
                        modifier = Modifier.verticalScroll(infoScrollState)
                    ) {
                        Text(
                            text = description.value ?: place.intro(isVietnamese),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                DetailsTab.NOVEL -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(novelScrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        place.novelDisplayTitle(isVietnamese)?.let { novelTitle ->
                            Text(
                                text = novelTitle,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = novel.value ?: stringResource(id = R.string.novel_not_available),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify
                        )

                        if (!novel.value.isNullOrBlank()) {
                            androidx.compose.material3.Button(
                                onClick = {
                                    val source = novel.value ?: description.value ?: place.intro(isVietnamese)
                                    coroutineScope.launch {
                                        generatedQuestions = QuestionGenerator.generateQuestions(
                                            text = source,
                                            isVietnamese = isVietnamese
                                        )
                                    }
                                }
                            ) {
                                Text(text = stringResource(id = R.string.generate_questions))
                            }

                            if (generatedQuestions.isNotEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.questions_count, generatedQuestions.size),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(modifier = Modifier.size(4.dp))

                            generatedQuestions.forEachIndexed { index, question ->
                                Column(modifier = Modifier.padding(top = if (index == 0) 0.dp else 8.dp)) {
                                    Text(
                                        text = stringResource(id = R.string.question_number, index + 1),
                                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = question,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Justify
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.close))
            }
        }
    }
}

private enum class DetailsTab {
    INFO,
    NOVEL,
}
