package org.permanent.permanent.ui.legacyPlanning.compose
import DesignateContactOrStewardScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.viewmodels.ArchiveStewardViewModel

@Composable
fun ArchiveStewardScreen(viewModel: ArchiveStewardViewModel,
                         archive: Archive?,
                         openAddEditScreen: () -> Unit,
                         openLegacyScreen: () -> Unit)  {

    val userName = viewModel.contactName.observeAsState()
    val userEmail = viewModel.contactEmail.observeAsState()

    Column(modifier = Modifier.background(Color.White)) {
        Header(archiveName = archive?.fullName,
            accessRoleText = archive?.accessRole?.toTitleCase(),
            iconURL = archive?.thumbURL200)
        DesignateContactOrStewardScreen(
            name = userName,
            email = userEmail,
            title = stringResource(R.string.designate_an_archive_steward),
            subtitle = stringResource(R.string.designate_archive_title),
            cardTitle = stringResource(R.string.a_trusted_archive_steward_title),
            cardSubtitle = stringResource(R.string.a_trusted_archive_steward_description),
            cardButtonName = stringResource(R.string.add_archive_steward),
            openAddEditScreen = openAddEditScreen,
            openLegacyScreen = openLegacyScreen
        )
    }
}

@Composable
private fun Header(
    archiveName: String?,
    accessRoleText: String?,
    iconURL: String?
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val mardiGrasColor = Color(ContextCompat.getColor(context, R.color.mardiGras))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Row(modifier = Modifier.padding(24.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = iconURL,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (archiveName != null) {
                Text(
                    text = archiveName,
                    color = primaryColor,
                    fontFamily = semiboldFont,
                    fontSize = 15.sp
                )
            }

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        mardiGrasColor.copy(alpha = 0.2f)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 2.dp
                    )
            ) {
                if (accessRoleText != null) {
                    Text(text = accessRoleText.uppercase(),
                        color = primaryColor,
                        fontFamily = regularFont,
                        fontSize = 8.sp)
                }
            }
        }
    }
}
