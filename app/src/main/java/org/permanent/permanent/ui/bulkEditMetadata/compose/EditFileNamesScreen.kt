package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditFileNamesViewModel

@Composable
fun EditFileNamesScreen(
    viewModel: EditFileNamesViewModel
) {
    val options = listOf(
        EditFilesOptions.REPLACE,
        EditFilesOptions.APPEND,
        EditFilesOptions.SEQUENCE
    )
    var selectedIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val superLightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))

    val uiState by viewModel.uiState.collectAsState()

    Column(
    ) {
        BottomSheetHeader(
            painterResource(id = R.drawable.ic_tag), screenTitle = "Edit file names"
        )
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)) {

            Box(
                modifier = Modifier
                    .border(1.dp, superLightBlueColor, RoundedCornerShape(10))
                    .padding(3.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = Color.White,
                    modifier = Modifier,
                    indicator = {
                        Box {}
                    },
                    divider = {},
                ) {
                    options.forEachIndexed { index, option ->
                        val selected = selectedIndex == index
                        Tab(
                            modifier = if (selected) Modifier
                                .background(superLightBlueColor)
                            else Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Color.White
                                ),
                            selected = selected,
                            onClick = { selectedIndex = index },
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = if (selected) Modifier
                                        .alpha(1.0f)
                                    else
                                        Modifier
                                            .alpha(0.5f)
                                ) {
                                    Image(
                                        painter = painterResource(id = option.resource),
                                        contentDescription = "Replace"
                                    )
                                    Text(
                                        text = option.title.uppercase(),
                                        color = blue900,
                                        fontFamily = boldFont,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Conditional Content based on selectedOption
            when (options[selectedIndex].title) {
                "Replace" -> ReplaceFileNamesScreen(
                    uiState,
                    replace = viewModel::replace
                )
                "Append" -> AppendContent()
                "Sequence" -> PrependContent()
            }
        }
    }
}

private enum class EditFilesOptions(val title: String, val resource: Int) {
    REPLACE("Replace", R.drawable.refresh),
    APPEND("Append", R.drawable.append),
    SEQUENCE("Sequence", R.drawable.sequence)
}

@Composable
fun AppendContent() { /* ... */ }

@Composable
fun PrependContent() { /* ... */ }
//
//@Preview
//@Composable
//fun SimpleComposablePreview() {
//    EditFileNamesScreen(viewModel = EditFileNamesViewModel())
//}