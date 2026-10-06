package org.permanent.permanent.ui.myFiles

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.boundsOnScreen

// A picture of the screen under the popup, taken when it opens; [isBlurred] when the
// bitmap was blurred up front (Modifier.blur() does nothing below API 31), which also
// shrinks it, so it is drawn back at [sizeOnScreen].
class PopupBackdrop(
    val bitmap: Bitmap,
    val originOnScreen: IntOffset,
    val sizeOnScreen: IntSize,
    val isBlurred: Boolean
)

// Opens over the sort row: the sort field first, then its direction.
@Composable
fun SortOptionsPopup(
    anchor: Rect,
    currentSort: SortType,
    backdrop: PopupBackdrop?,
    onSortClick: (SortType) -> Unit,
    onDismiss: () -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val margin = with(density) { 8.dp.roundToPx() }
    var origin by remember { mutableStateOf<IntOffset?>(null) }

    Layout(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (origin == null) origin = view.boundsOnScreen().let { IntOffset(it.left, it.top) }
            }
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        content = { SortOptionsCard(currentSort, backdrop, origin, onSortClick) }
    ) { measurables, constraints ->
        val windowOrigin = origin
            ?: return@Layout layout(constraints.maxWidth, constraints.maxHeight) {}
        val placeable = measurables.first().measure(
            Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        )
        // The card starts where the sort row starts.
        val x = (anchor.left - windowOrigin.x)
            .coerceIn(margin, (constraints.maxWidth - margin - placeable.width).coerceAtLeast(margin))
        val y = (anchor.top - windowOrigin.y + margin)
            .coerceIn(margin, (constraints.maxHeight - margin - placeable.height).coerceAtLeast(margin))
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(x, y) }
    }
}

@Composable
private fun SortOptionsCard(
    currentSort: SortType,
    backdrop: PopupBackdrop?,
    windowOrigin: IntOffset?,
    onSortClick: (SortType) -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    var cardInWindow by remember { mutableStateOf<Offset?>(null) }
    Box(
        modifier = Modifier
            .width(240.dp)
            .shadow(24.dp, shape, ambientColor = AmbientShadowColor, spotColor = SpotShadowColor)
            .clip(shape)
            .border(Dp.Hairline, BorderColor, shape)
            .onGloballyPositioned { cardInWindow = it.positionInWindow() }
    ) {
        val cardPosition = cardInWindow
        if (backdrop != null && windowOrigin != null && cardPosition != null) {
            BlurredBackdrop(backdrop, windowOrigin, cardPosition)
        }
        SortOptionsContent(
            currentSort = currentSort,
            cardAlpha = if (backdrop != null) BLURRED_CARD_ALPHA else PLAIN_CARD_ALPHA,
            onSortClick = onSortClick
        )
    }
}

// The screen under the card, blurred, so the card reads as frosted glass (Figma backdrop blur).
@Composable
private fun BoxScope.BlurredBackdrop(
    backdrop: PopupBackdrop,
    windowOrigin: IntOffset,
    cardInWindow: Offset
) {
    val image = remember(backdrop) { backdrop.bitmap.asImageBitmap() }
    val blur = if (backdrop.isBlurred) Modifier else Modifier.blur(BACKDROP_BLUR, BlurredEdgeTreatment.Rectangle)
    Canvas(modifier = Modifier.matchParentSize().then(blur)) {
        drawImage(
            image = image,
            dstOffset = IntOffset(
                (backdrop.originOnScreen.x - windowOrigin.x - cardInWindow.x).toInt(),
                (backdrop.originOnScreen.y - windowOrigin.y - cardInWindow.y).toInt()
            ),
            dstSize = backdrop.sizeOnScreen
        )
    }
}

@Composable
private fun SortOptionsContent(
    currentSort: SortType,
    cardAlpha: Float,
    onSortClick: (SortType) -> Unit
) {
    val blue900 = colorResource(R.color.blue900)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = cardAlpha))
            // Taps inside the card must not dismiss the popup.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down_arrow_up_blue400),
                contentDescription = null,
                tint = blue900,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(text = stringResource(R.string.sort_by), style = titleStyle(blue900))
                Text(
                    text = currentSort.toLabel(LocalContext.current),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colorResource(R.color.blue400)
                    )
                )
            }
        }
        Divider()
        SortField.values().forEach { field ->
            SortOptionRow(
                text = stringResource(field.nameRes),
                isSelected = currentSort.field == field
            ) { onSortClick(if (currentSort.field == field) currentSort else field.defaultSort) }
        }
        Divider()
        currentSort.field.sorts.forEach { sort ->
            SortOptionRow(text = stringResource(sort.directionRes), isSelected = sort == currentSort) {
                onSortClick(sort)
            }
        }
    }
}

@Composable
private fun SortOptionRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val blue900 = colorResource(R.color.blue900)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .semantics { selected = isSelected },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check_primary),
                contentDescription = null,
                tint = blue900,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = blue900
            )
        )
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
}

private fun titleStyle(color: Color) = TextStyle(
    fontFamily = FontFamily(Font(R.font.usual_medium)),
    fontSize = 14.sp,
    lineHeight = 24.sp,
    color = color
)

// Strong enough to lift the card off the list; the backdrop hides it inside the card.
private val AmbientShadowColor = Color.Black.copy(alpha = 0.24f)
private val SpotShadowColor = Color.Black.copy(alpha = 0.44f)
private val BorderColor = Color.Black.copy(alpha = 0.06f)

private val BACKDROP_BLUR = 20.dp

// White over the blurred backdrop; without the backdrop the card needs more.
private const val BLURRED_CARD_ALPHA = 0.48f
private const val PLAIN_CARD_ALPHA = 0.94f
