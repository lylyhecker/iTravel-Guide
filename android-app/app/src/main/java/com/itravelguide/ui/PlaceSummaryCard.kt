package com.itravelguide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itravelguide.R
import com.itravelguide.data.Place

@Composable
fun PlaceSummaryCard(
    modifier: Modifier = Modifier,
    place: Place?,
    isVietnamese: Boolean,
    onLanguageToggle: () -> Unit,
    onDetailsClick: () -> Unit,
) {
    AnimatedVisibility(visible = place != null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            place?.let { selectedPlace ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedPlace.displayName(isVietnamese),
                            style = MaterialTheme.typography.titleLarge
                        )
                        FilledIconButton(
                            onClick = onLanguageToggle,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_language),
                                contentDescription = stringResource(id = R.string.language_toggle)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = selectedPlace.intro(isVietnamese),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Button(onClick = onDetailsClick) {
                        Text(text = stringResource(id = R.string.view_details))
                    }
                }
            }
        }
    }
}
