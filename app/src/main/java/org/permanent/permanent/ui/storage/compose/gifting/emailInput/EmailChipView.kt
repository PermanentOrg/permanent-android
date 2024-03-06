package org.permanent.permanent.ui.storage.compose.gifting.emailInput

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.models.EmailChip

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
fun EmailChipView(
    emails: MutableList<EmailChip>,
    onAddEmailChip: (EmailChip) -> Unit,
    onRemoveEmailChip: (EmailChip) -> Unit
) {
    val errorText = remember { mutableStateOf("") }
    var isFocused = remember { mutableStateOf(false) }
    val showTextField = remember { mutableStateOf(true) }

    val context = LocalContext.current

    val blue50 = Color(ContextCompat.getColor(context, R.color.blue50))
    val error200 = Color(ContextCompat.getColor(context, R.color.error200))
    val error500 = Color(ContextCompat.getColor(context, R.color.error500))
    val italicFont = FontFamily(Font(R.font.open_sans_italic_ttf))

    val hasError: Boolean = errorText.value.isNotEmpty()

    LaunchedEffect(true) {
        showTextField.value = true
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable {
                if (!isFocused.value) {
                    showTextField.value = true
                }
            }
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, if (hasError) error200 else blue50, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emails.forEach { chip ->
                    EmailChipCard(text = chip.text,
                        onDelete = { text ->
                            val email = emails.firstOrNull { it.text == text }
                            if (email != null) {
                                onRemoveEmailChip(email)
                            }
                        })
                }

                if (showTextField.value) {
                    EmailChipTextField(
                        errorText,
                        emails,
                        isFocused,
                        showTextField,
                        onAddEmailChip = onAddEmailChip,
                        onRemoveEmailChip = onRemoveEmailChip
                    )
                }
            }
        }
        if (hasError) {
            Text(
                text = "${errorText.value} is invalid!",
                color = error500,
                fontFamily = italicFont,
                fontSize = 10.sp
            )
        }
    }
}