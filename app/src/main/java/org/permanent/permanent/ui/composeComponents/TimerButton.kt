package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.permanent.permanent.R

@Composable
fun TimerButton(
    text: String, startImmediately: Boolean = true, // Parameter to trigger countdown on load
    onResendClick: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(60) } // Countdown in seconds
    var isCountingDown by remember { mutableStateOf(startImmediately) } // Initialize based on parameter

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            while (timeLeft > 0) {
                delay(1000L) // Wait 1 second
                timeLeft-- // Decrease time
            }
            isCountingDown = false // Reset state
        }
    }

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(
            1.dp, colorResource(id = R.color.blue25), RoundedCornerShape(12.dp)
        ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = if (isCountingDown) R.color.blue15 else R.color.blue25)),
        onClick = {
            if (!isCountingDown) {
                timeLeft = 60 // Start countdown from 60 sec
                isCountingDown = true
                onResendClick() // Call resend action
            }
        }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = colorResource(id = if (isCountingDown) R.color.blueGreyLight else R.color.blue900),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
            )
            Spacer(modifier = Modifier.width(10.dp))
            if (isCountingDown) {
                // Show countdown timer when active
                Text(
                    text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                    color = colorResource(id = R.color.blue900),
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                )
            }
        }
    }
}