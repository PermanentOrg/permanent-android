package org.permanent.permanent.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun IntroScreen() {
    val context = LocalContext.current
    val backgroundColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))

    Column(
        modifier = Modifier.fillMaxSize().background(color = backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_legacy_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(128.dp)
        )
    }
}