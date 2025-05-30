package org.permanent.permanent.ui.bulkEditMetadata.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditFileNamesViewModel

@Composable
fun EditFileNamesScreen(
    viewModel: EditFileNamesViewModel,
    cancel: () -> Unit
) {
    val options = listOf(
        EditFilesOptions.REPLACE,
        EditFilesOptions.APPEND,
        EditFilesOptions.SEQUENCE
    )
    var selectedIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val superLightBlueColor = Color(ContextCompat.getColor(context, R.color.blue25))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        it
        Column() {
            BottomSheetHeader(
                painterResource(id = R.drawable.ic_edit_name),
                screenTitle = stringResource(id = R.string.edit_file_names)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(26.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
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
                                            text = option.getLabel(context).uppercase(),
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
                when (options[selectedIndex]) {
                    EditFilesOptions.REPLACE -> ReplaceFileNamesScreen(
                        uiState,
                        replace = viewModel::replace,
                        applyChanges = viewModel::applyChanges,
                        cancel = cancel
                    )

                    EditFilesOptions.APPEND -> AppendFileNamesScreen(
                        uiState = uiState,
                        append = viewModel::append,
                        applyChanges = viewModel::applyChanges,
                        cancel = cancel
                    )

                    EditFilesOptions.SEQUENCE -> SequenceFileNamesScreen(
                        uiState = uiState,
                        formatDate = viewModel::formatDate,
                        formatCount = viewModel::formatCount,
                        applyDateChanges = viewModel::applyChanges,
                        applyCountChanges = viewModel::applyChanges,
                        cancel = cancel
                    )
                }
            }

            LaunchedEffect(key1 = uiState.errorMessage) {
                uiState.errorMessage?.let {
                    scope.launch {
                        snackbarHostState.showSnackbar(it)
                        viewModel.updateError(null)
                    }
                }
            }

            LaunchedEffect(key1 = uiState.shouldClose, block = {
                if (uiState.shouldClose) {
                    cancel()
                }
            })
        }
    }
}

private enum class EditFilesOptions(val titleID: Int, val resource: Int) {
    REPLACE(R.string.replace, R.drawable.refresh),
    APPEND(R.string.append, R.drawable.append),
    SEQUENCE(R.string.sequence, R.drawable.sequence);

    fun getLabel(context: Context) = context.getString(titleID)
}