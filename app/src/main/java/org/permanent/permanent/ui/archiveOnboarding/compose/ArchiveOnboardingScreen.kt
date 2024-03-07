package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CustomProgressIndicator
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun ArchiveOnboardingScreen(
    viewModel: ArchiveOnboardingViewModel
) {
    val context = LocalContext.current
    val horizontalPaddingDp = 32.dp
    val spacerPaddingDp = 8.dp

    val blue900Color = Color(ContextCompat.getColor(context, R.color.blue900))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blue900Color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPaddingDp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Next",
                modifier = Modifier.size(40.dp)
            )

            Box(
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacerPaddingDp)) {
                    OnboardingProgressIndicator(
                        horizontalPaddingDp, spacerPaddingDp, 100
                    )

                    OnboardingProgressIndicator(
                        horizontalPaddingDp, spacerPaddingDp, 0
                    )

                    OnboardingProgressIndicator(
                        horizontalPaddingDp, spacerPaddingDp, 0
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingProgressIndicator(
    horizontalPaddingDp: Dp, spacerPaddingDp: Dp, percent: Int
) {
    val context = LocalContext.current
    val whiteSuperTransparentColor = Color(ContextCompat.getColor(context, R.color.whiteSuperExtraTransparent))
    val purpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val accentColor = Color(ContextCompat.getColor(context, R.color.colorAccent))

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    CustomProgressIndicator(
        Modifier
            .clip(shape = RoundedCornerShape(3.dp))
            .height(2.dp),
        (screenWidthDp - horizontalPaddingDp - horizontalPaddingDp - spacerPaddingDp - spacerPaddingDp) / 3,
        whiteSuperTransparentColor,
        Brush.horizontalGradient(
            listOf(
                purpleColor, accentColor
            )
        ),
        percent
    )
}