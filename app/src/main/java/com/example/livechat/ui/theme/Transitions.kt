package com.example.livechat.ui.theme

import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

fun slideInFromRight(animationSpec: FiniteAnimationSpec<IntOffset> = tween(300)): EnterTransition {
    return slideInHorizontally(
        animationSpec = animationSpec,
        initialOffsetX = { fullWidth -> fullWidth }
    )
}

fun slideOutToRight(animationSpec: FiniteAnimationSpec<IntOffset> = tween(300)): ExitTransition {
    return slideOutHorizontally(
        animationSpec = animationSpec,
        targetOffsetX = { fullWidth -> fullWidth }
    )
}