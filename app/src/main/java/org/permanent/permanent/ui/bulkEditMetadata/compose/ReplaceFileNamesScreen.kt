package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun ReplaceFileNamesScreen() {
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val middleGrey = Color(ContextCompat.getColor(context, R.color.middleGrey))
    val superLightBlue = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Find".uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = semiBoldFont,
                    color = middleGrey)
            )
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = superLightBlue, shape = RoundedCornerShape(size = 2.dp))
                    .width(342.dp)
                    .height(48.dp)
                    .background(color = superLightBlue.copy(0.5f), shape = RoundedCornerShape(size = 2.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Make TextField fill parent's width
                    textStyle = TextStyle(
                        fontFamily = regularFont,
                        fontSize = 13.sp,
                        color = blue900,
                        lineHeight = 16.sp // Set line height as before
                    ),
                    singleLine = true
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Replace".uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = semiBoldFont,
                    color = middleGrey)
            )
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = superLightBlue, shape = RoundedCornerShape(size = 2.dp))
                    .width(342.dp)
                    .height(48.dp)
                    .background(color = superLightBlue.copy(0.5f), shape = RoundedCornerShape(size = 2.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Make TextField fill parent's width
                    textStyle = TextStyle(
                        fontFamily = regularFont,
                        fontSize = 13.sp,
                        color = blue900,
                        lineHeight = 16.sp // Set line height as before
                    ),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
        EditFileNamesFooter()
    }
}

@Preview
@Composable
fun ReplaceFileNamesPreview() {
    ReplaceFileNamesScreen()
}