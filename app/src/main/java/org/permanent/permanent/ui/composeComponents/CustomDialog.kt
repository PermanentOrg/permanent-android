import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun CustomDialog(title: String,
                 subtitle: String?,
                 okButtonText: String,
                 cancelButtonText: String,
                 onConfirm: () -> Unit, 
                 onDismiss: () -> Unit) {
    val context = LocalContext.current
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontFamily = boldFont,
                    color = primaryColor
                )
                subtitle?.let {
                    Text(
                        text = it,
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontFamily = semiboldFont,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                    onClick = { onConfirm() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                    Text(
                        text = okButtonText,
                        fontFamily = semiboldFont,
                        fontSize = 15.sp
                    )
                }
                Button(modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                    Text(
                        text = cancelButtonText,
                        fontFamily = semiboldFont,
                        fontSize = 15.sp,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    CustomDialog(
        title = "Are you sure you’d like to gift storage?\nThis can't be undone!",
        subtitle = "hello",
        okButtonText = "Yes, gift storage",
        cancelButtonText = "Cancel",
        onConfirm = { /*TODO*/ }) {
    }
}
