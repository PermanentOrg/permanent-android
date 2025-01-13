package org.permanent.permanent.ui.settings.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.viewmodels.TwoStepVerificationViewModel

@Composable
fun TwoStepVerificationEnabledScreen(
    viewModel: TwoStepVerificationViewModel,
    onChangeVerificationMethodClick: () -> Unit
) {
    val twoFAList by viewModel.twoFAList.collectAsState()  // Collect 2FA methods

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.superLightBlue)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Snackbar-style banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.lightGreen))
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_lock_closed_green),
                    contentDescription = "Enabled Lock",
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.two_step_verification_enabled),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = colorResource(id = R.color.textGreen),
                    fontFamily = FontFamily(Font(R.font.usual_regular))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Text
            Text(
                text = stringResource(id = R.string.two_step_verification_description),
                color = colorResource(id = R.color.colorPrimary),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verification Methods List
            if (twoFAList.isNotEmpty()) {
                twoFAList.forEachIndexed { index, item ->
                    VerificationItemCard(
                        item = item,
                        onRemove = {
                            // Update the list to remove the item
                            val updatedList = twoFAList.toMutableList().apply { removeAt(index) }
                            viewModel.updateTwoFAList(updatedList)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (twoFAList.size == 1) {
                // Action Button
                Button(
                    onClick = onChangeVerificationMethodClick,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorPrimary)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.change_verification_method),
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_medium))
                    )
                }
            }
        }

        // Bottom Section
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = colorResource(id = R.color.dividerBlue)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.two_step_verification_help_text),
                color = colorResource(id = R.color.blue),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun VerificationItemCard(
    item: TwoFAVO,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = colorResource(R.color.blue50),
            shape = RoundedCornerShape(size = 12.dp)
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 24.dp, start = 24.dp, end = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Content Column
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(if (item.method == "sms") R.string.text_message_sms else R.string.email),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_medium)),
                        color = colorResource(R.color.blue900)
                    )
                    if (item.method == "sms") { // We show "default" label if it's sms
                        Spacer(modifier = Modifier.width(8.dp)) // Small spacing between title and DEFAULT
                        Text(
                            text = "DEFAULT",
                            color = colorResource(R.color.blue900),
                            fontSize = 10.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 1.6.sp,
                            fontFamily = FontFamily(Font(R.font.usual_medium)),
                            modifier = Modifier
                                .background(
                                    color = colorResource(R.color.blue25),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Value text
                Text(
                    text = item.value,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue600)
                )
            }

            // Align IconButton at the top
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 4.dp) // Optional: Adjust top padding for fine-tuning
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_red),
                    contentDescription = "Remove",
                    tint = colorResource(R.color.error500)
                )
            }
        }
    }
}