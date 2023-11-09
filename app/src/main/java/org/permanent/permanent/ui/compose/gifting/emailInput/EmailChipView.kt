package org.permanent.permanent.ui.compose.gifting.emailInput

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.models.EmailChip
import java.util.UUID

private val INPUTKEY = UUID.randomUUID().toString()

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
fun EmailChipView(emails: MutableList<EmailChip>) {
    val text = remember { mutableStateOf("") }
    val errorText = remember { mutableStateOf("") }
    var isFocused = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val blue50 = Color(ContextCompat.getColor(context, R.color.blue50))
    val error200 = Color(ContextCompat.getColor(context, R.color.error200))
    val error500 = Color(ContextCompat.getColor(context, R.color.error500))
    val italicFont = FontFamily(Font(R.font.open_sans_italic_ttf))

    val hasError: Boolean = errorText.value.isNotEmpty()

    LaunchedEffect(true) {
        emails.add(EmailChip(INPUTKEY))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable {
            if (!isFocused.value) {
                emails.add(EmailChip(INPUTKEY))
            }
        }
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, if (hasError) error200 else blue50, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emails.forEach { chip ->
                    if (chip.text != INPUTKEY) {
                        EmailChipCard(text = chip.text,
                            onDelete = { text ->
                                emails.removeIf { it.text == text }
                        })
                    } else {
                        EmailChipTextField(text, errorText, emails, isFocused)
                    }
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

@Preview
@Composable
fun EmailChipPreview() {
    EmailChipView(emails= mutableListOf(
        EmailChip("flaviu88@gmail.com"), EmailChip("flaviu88@gmail.com"), EmailChip("flaviu88@gmail.com"), EmailChip("flaviu88@gmail.com") ,
        EmailChip("Another Chip")
    ))
}
