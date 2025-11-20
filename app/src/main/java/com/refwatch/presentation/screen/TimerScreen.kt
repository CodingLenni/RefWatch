package com.refwatch.presentation.screen

import TimerViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Replay
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonColors
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import java.util.Locale
import kotlin.time.Duration

const val TIMER_SCREEN_PATH = "Timer_Screen"

@Composable
fun TimerScreen(viewModel: TimerViewModel, navController: NavController) {
    val halftime by viewModel.halftime.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.playClockTimer.collectAsStateWithLifecycle()
    val timeLeft by viewModel.timeLeftDuration.collectAsStateWithLifecycle()
    val additionalTime by viewModel.additionalTimeDuration.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val isAdditionalTimeRunning by viewModel.isAdditionalTimeRunning.collectAsStateWithLifecycle()
    val timerInStartPosition by viewModel.timerInStartPosition.collectAsStateWithLifecycle()


    AppScaffold() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 25.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(halftime.label)
            PlayClockDisplay(elapsedTime, viewModel, isRunning, timerInStartPosition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RemainingTimeDisplay(timeLeft)
                OverTimeDisplay(additionalTime, isAdditionalTimeRunning)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row {
                // play / pause button
                FilledIconButton(
                    modifier = Modifier.size(IconButtonDefaults.LargeButtonSize),
                    onClick = {
                        if (isRunning) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    colors = IconButtonColors(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.onPrimary,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.onPrimary,
                    )
                ) {
                    val icon = if (isRunning) Icons.Sharp.Pause else Icons.Sharp.PlayArrow
                    val contentDesc = if (isRunning) "Pause timer" else "Start timer"
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDesc,
                        modifier = Modifier.size(IconButtonDefaults.LargeIconSize)
                    )
                }
                Spacer(modifier = Modifier.size(20.dp))
                // settings / reset button
                val buttonColor = IconButtonColors(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                if (timerInStartPosition || isRunning) {
                    // settings button
                    FilledIconButton(
                        modifier = Modifier.size(IconButtonDefaults.LargeButtonSize),
                        onClick = {
                            navController.navigate(GAME_SETTINGS_SCREEN_PATH)
                        },
                        enabled = !isRunning,
                        colors = buttonColor
                    ) {
                        val icon =
                            Icons.Sharp.Settings
                        val contentDesc = "Game Settings"
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDesc,
                            modifier = Modifier.size(IconButtonDefaults.LargeIconSize)
                        )
                    }
                } else {
                    // reset button
                    FilledIconButton(
                        modifier = Modifier.size(IconButtonDefaults.LargeButtonSize),
                        onClick = {
                            viewModel.resetTimer()
                        },
                        colors = buttonColor
                    ) {
                        val icon = Icons.Sharp.Replay
                        val contentDesc = "Reset timer"
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDesc,
                            modifier = Modifier.size(IconButtonDefaults.LargeIconSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayClockDisplay(
    elapsedTime: Duration,
    viewModel: TimerViewModel,
    isRunning: Boolean,
    timerInStartPosition: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val totalSeconds = elapsedTime.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timerText = String.format(Locale.GERMAN, "%02d:%02d", minutes, seconds)
    FixedWidthDurationText(
        timerText,
        style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.clickable(
            enabled = !isRunning && timerInStartPosition,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                viewModel.switchPeriod()
            },
        )
    )
}

@Composable
fun RemainingTimeDisplay(timeLeft: Duration) {
    val totalSeconds = (timeLeft.inWholeMilliseconds + 999) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timerText = String.format(Locale.GERMAN, "%02d:%02d", minutes, seconds)

    FixedWidthDurationText(timerText)
}

@Composable
fun OverTimeDisplay(additionalTime: Duration, isRunning: Boolean) {
    val totalSeconds = additionalTime.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timerText = String.format(Locale.GERMAN, "%02d:%02d", minutes, seconds)

    if (isRunning) {
        FixedWidthDurationText(
            timerText,
            color = MaterialTheme.colorScheme.onSecondary,
            bgColor = MaterialTheme.colorScheme.secondary
        )
    } else {
        FixedWidthDurationText(
            timerText,
        )
    }
}

/*
* A Composable that displays a Kotlin Duration in MM:SS format with a fixed width,
* preventing layout shifting when the numbers change.
*
* @param duration The [Duration] to display.
* @param modifier The [Modifier] to be applied to this composable.
* @param style The [TextStyle] to apply to the text, allowing for size and color customization.
*/
@Composable
fun FixedWidthDurationText(
    time: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displaySmall.copy(
        // Use a monospaced font family for extra certainty,
        // though the technique below relies more on fixed-digit formatting.
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
    ),
    color: Color = MaterialTheme.colorScheme.onBackground,
    bgColor: Color = MaterialTheme.colorScheme.background
) {
    Text(
        text = time,
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(25)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        style = style,
        color = color
    )
}

@Composable
@WearPreviewDevices
fun PreviewTimerScreen() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel = TimerViewModel();
    TimerScreen(viewModel, navController)
}
