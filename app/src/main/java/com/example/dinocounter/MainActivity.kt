package com.example.dinocounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dinocounter.ui.theme.DinoCounterTheme
import org.json.JSONArray
import java.io.File


class MainActivity : ComponentActivity() {

    private val fileName = "cookie_data.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read and calculate total counts from the JSON file
        val cookiesFromFile = calculateTotalCounts()

        setContent {
            DinoCounterTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CookieApp(
                        cookiesFromFile,
                        ::appendCookieData,
                        onRecalculate = { calculateTotalCounts() })
                }
            }
        }
    }

    // Calculate total counts from the existing JSON file
    private fun calculateTotalCounts(): List<CookieData> {
        val file = File(filesDir, fileName)
        val totalCounts = IntArray(6) { 0 }

        if (file.exists()) {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val cookieArray = jsonArray.getJSONArray(i)
                for (j in 0 until cookieArray.length()) {
                    val cookieId = cookieArray.getInt(j)
                    totalCounts[cookieId - 1]++ // Increase total count for the cookie ID
                }
            }
        }

        // Return the total count list with partial set to 0
        return (1..6).map { CookieData(it, totalCounts[it - 1],0) }
    }

    // Append partial counts to the file and reset the partial counters
    private fun appendCookieData(cookies: List<CookieData>, selectionOrder: List<Int?>) {
        val file = File(filesDir, fileName)
        val jsonArray = if (file.exists()) {
            JSONArray(file.readText())
        } else {
            JSONArray()
        }

        // Create an array for storing the partial counts (IDs)
        val cookieEntry = JSONArray()

        selectionOrder.forEach { selectedId ->
            if (selectedId != null) {
                val cookie = cookies.find { it.id == selectedId }
                cookieEntry.put(cookie?.id)
            }
        }

        // Append the new entry to the JSON array
        if (cookieEntry.length() > 0) {
            jsonArray.put(cookieEntry)
        }

        // Write the updated JSON array back to the file
        file.writeText(jsonArray.toString())
    }
}

// Data class to hold cookie info
data class CookieData(
    val id: Int,
    var total: Int,
    var partial: Int
)

@Composable
fun CookieApp(
    initialCookies: List<CookieData>,
    onStoreData: (List<CookieData>, List<Int?>) -> Unit,
    onRecalculate: () -> List<CookieData>
) {
    // List of cookie image resources
    val cookieImages = listOf(
        R.drawable.cookie1,
        R.drawable.cookie2,
        R.drawable.cookie3,
        R.drawable.cookie4,
        R.drawable.cookie5,
        R.drawable.cookie6
    )

    // Convert initial cookies list into a mutable state list that will be tracked by Compose
    val cookies = remember { mutableStateListOf(*initialCookies.toTypedArray()) }

    val selectionOrderId = remember { mutableIntStateOf(0) }
    val selectionOrder = remember { mutableStateListOf<Int?>(null, null, null, null) }

    // Helper function to calculate the total sum of all partial counters
    val sumOfAllPartialCounters: () -> Int = {
        cookies.sumOf { it.partial }
    }

    val context = LocalContext.current
    val isEditing = remember { mutableStateOf(false) }

    if (isEditing.value) {
        val file = File(context.filesDir, "cookie_data.json")
        val jsonContent = if (file.exists()) file.readText() else "[]"

        EditView(
            jsonContent = jsonContent,
            onSave = { updatedJson ->
                file.writeText(updatedJson)
                // Recalculate total counts and update cookies
                val updatedCookies = onRecalculate()
                cookies.clear()
                cookies.addAll(updatedCookies)

                isEditing.value = false
            },
            onCancel = {
                isEditing.value = false
            }
        )
    } else {
        // Scrollable grid with auto-arrangement
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .padding(2.dp)
        ) {
            items(cookies.size) { index ->
                // Pass the corresponding cookie object and image resource to CookieBlock
                CookieBlock(
                    cookie = cookies[index],
                    imageRes = cookieImages[index],
                    canAdd = sumOfAllPartialCounters() < 4, // Check if adding is allowed
                    onIncrementOrder = { updatedCookie ->
                        selectionOrder[selectionOrderId.intValue] = cookies[index].id
                        selectionOrderId.intValue++
                        cookies[index] = updatedCookie.copy(
                            partial = updatedCookie.partial + 1,
                            total = updatedCookie.total + 1
                        )
                    },
                    onDecrementOrder = { updatedCookie ->
                        if ((selectionOrderId.intValue > 0) && (updatedCookie.partial > 0)) {
                            selectionOrderId.intValue--
                            selectionOrder[selectionOrderId.intValue] = null
                            cookies[index] = updatedCookie.copy(
                                partial = updatedCookie.partial - 1,
                                total = updatedCookie.total - 1
                            )
                        }
                    }
                )
            }
        }
        // Store button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sumOfAllPartial = sumOfAllPartialCounters();
            Button(
                onClick = {
                    if (sumOfAllPartial == 4) {
                        // Store data logic
                        onStoreData(cookies, selectionOrder)

                        // Reset the partial counters after storing
                        cookies.forEachIndexed { index, cookie ->
                            cookies[index] = cookie.copy(partial = 0)  // Reset variables
                        }
                        selectionOrderId.intValue = 0
                        selectionOrder.fill(null)
                    } else if (sumOfAllPartial == 0){
                        // Navigate to edit view
                        isEditing.value = true
                    }
                },
                modifier = Modifier.padding(2.dp),
                enabled = (sumOfAllPartial == 4) or (sumOfAllPartial == 0),
            ) {
                Text(if (sumOfAllPartial == 4) "Guardar" else if (sumOfAllPartial == 0) "Editar" else "---")
            }
        }
    }
}

@Composable
fun CookieBlock(
    cookie: CookieData,
    imageRes: Int,
    canAdd: Boolean,  // New parameter to control if "+" is allowed
    onIncrementOrder: (CookieData) -> Unit,
    onDecrementOrder: (CookieData) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(8.dp)  // Outer padding for spacing
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)) // Border with rounded corners
            .clip(RoundedCornerShape(8.dp))  // Clip content to rounded corners
            .padding(8.dp) // Inner padding for content spacing
    ) {
        Column(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Image of the cookie with automatic resizing
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Cookie ${cookie.id}",
                modifier = Modifier
                    .size(150.dp)
                    .alpha(if (cookie.partial > 0) 1f else 0.5f),
                contentScale = ContentScale.Fit // Adjust the image to fit the block
            )
            // Total Counter, Partial Counter
            Text(text = "Total: ${cookie.total}, Partial: ${cookie.partial}", fontSize = 11.sp)
            Row(
                modifier = Modifier.padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // "-" button
                Button(
                    onClick = {
                        onDecrementOrder(cookie)
                    },
                    modifier = Modifier
                        .width(80.dp)  // Make the button wider
                        .height(50.dp),
                    enabled = cookie.partial != 0,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text("-")
                }
                // "+" button
                Button(
                    onClick = {
                        if (canAdd) {
                            onIncrementOrder(cookie)
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)  // Make the button wider
                        .height(50.dp),
                    enabled = canAdd,  // Disable the button if canAdd is false
                    shape = RoundedCornerShape(3.dp),
                ) {
                    Text("+")
                }
            }
        }
    }
}

@Composable
fun EditView(
    jsonContent: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val textState = remember { mutableStateOf(jsonContent) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Save button
            Button(onClick = { onSave(textState.value) }) {
                Text("Guardar")
            }

            // Cancel button
            Button(onClick = { onCancel() }) {
                Text("Cancel·lar")
            }
        }
        // Editable TextField for JSON content
        TextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("JSON Data") }
        )
    }
}
