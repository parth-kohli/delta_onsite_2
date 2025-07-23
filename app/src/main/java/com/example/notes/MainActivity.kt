package com.example.notes

import android.R
import android.R.attr.enabled
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notes.ui.theme.NotesTheme
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecurePrefs.init(this)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
val api = ApiFunctions()
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val token = remember { mutableStateOf("") }
    val navController = rememberNavController()
    val selectedDestination = remember { mutableStateOf("signup") }
    val selectedTabDestination = remember { mutableStateOf(0) }
    var density = LocalDensity.current
    val id = remember { mutableStateOf(0) }
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }

    var tileWidth = 450.dp / 16
    var tileHeight = 900.dp / (16 * 900.dp / 450.dp)
    NavHost(navController, startDestination = "signup") {
        composable("signup") {
            signinscreen(navController,tileWidth, tileHeight, modifier)
        }
        composable("userpage") {
            notesScreen(navController,modifier=modifier, onBack = {}, selectedTabDestination = selectedTabDestination, title=title, description = description, id= id)
        }
        composable("addnotes") {
            addNotesScreen(navController, onBack = {},modifier=modifier, selectedTabDestination = selectedTabDestination)
        }
        composable("editnotes") {
            addNotesScreen(navController, onBack = {},modifier=modifier, selectedTabDestination = selectedTabDestination, tit = title.value, des = description.value, id=id.value)
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun notesScreen(navController: NavController, onBack: ()-> Unit, modifier: Modifier, selectedTabDestination: MutableState<Int>, title: MutableState<String>, description: MutableState<String>, id: MutableState<Int>){
    BackHandler {
        selectedTabDestination.value=1
        navController.navigate("addnotes")
    }
    val notes = remember { mutableStateListOf<Notes>() }
    var currentPage by remember { mutableStateOf(0) }
    val refresh=remember { mutableStateOf(false) }
    var prevsearch = 0
    val listState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(Unit, refresh.value) {
        currentPage=0
        notes.clear()
        loadMoreNotes(currentPage,list=notes) {currentPage++; prevsearch=notes.size }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { index ->
                if (index != null && index >= notes.size - 3 && !isLoading && prevsearch>=(currentPage-1)*10 && currentPage!=0) {
                    isLoading = true
                    prevsearch=notes.size
                    loadMoreNotes(currentPage, list=notes) {
                        currentPage++
                        isLoading = false
                    }

                }
            }

    }
    Scaffold( modifier = modifier
        .fillMaxSize()
        .background(Color.Black),
        topBar = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton({navController.navigate("signup"); SecurePrefs.clearToken()}){

                    Icon(Icons.Default.ExitToApp, contentDescription = "Close", modifier = Modifier.size(60.dp))
                }
            }
        },
        bottomBar = {PrimaryTabRow(
            selectedTabIndex = selectedTabDestination.value,
            containerColor = Color.Transparent,
            contentColor = Color.White

        ){
            Tab(
                selected =selectedTabDestination.value==0,
                onClick = {
                    selectedTabDestination.value=0
                    navController.navigate("userpage") {


                    }
                },

                icon = {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null, modifier = Modifier.size(45.dp)
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray
            )
            Tab(
                selected =selectedTabDestination.value==1,
                onClick = {
                    selectedTabDestination.value=1
                    navController.navigate("addnotes") {

                    }
                },

                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null, modifier = Modifier.size(45.dp)
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray
            )


        }}
        ) {innerPadding->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize().padding(innerPadding)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (notes.size == 0) {
                item {
                    Text("No Questions Found", color = Color.LightGray)
                }
            }
            items(notes.size) { note ->
                val notecontent = notes[note]
                NoteCard(notecontent,title, description,navController, id, refresh)
            }

            if (isLoading && prevsearch >= (currentPage - 1) * 10) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard(
    note: Notes,title: MutableState<String>, description: MutableState<String>, navController: NavController, id: MutableState<Int>, refresh: MutableState<Boolean>
    ) {
    val showDialog = remember { mutableStateOf(false) }
    val expanded = remember { mutableStateOf(false) }
    val instant = Instant.parse(note.created_at+'Z')
    val localZonedDateTime = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    Box(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(onClick = {
                expanded.value = !expanded.value
                showDialog.value=false
            },
                onLongClick = {
                    showDialog.value=true
                })
            .fillMaxWidth(0.90f)

    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(23,23,63,255), contentColor = Color(255, 255, 255, 200)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {


                    Text(
                        text = localZonedDateTime.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right
                    )
                }
                Row {
                    Text(text = note.title, style = MaterialTheme.typography.titleMedium)

                }
                if (!expanded.value) {
                    Text(text = note.note, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                else{
                    Text(text = note.note)
                }
            }
        }
        DropdownMenu(containerColor = Color.White.copy(alpha=0.9f),
            expanded = showDialog.value,
            onDismissRequest = { showDialog.value = false },
        ) {

            DropdownMenuItem(
                text = { Text("Edit", color = Color.Black) },
                onClick = {
                    showDialog.value = false
                    id.value=note.id
                    title.value=note.title
                    description.value=note.note
                    navController.navigate("editnotes")

                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint =  Color.Black
                    )
                }
            )
            Box(modifier = Modifier.height(2.dp).background(Color(0xFF0D47A1)).fillMaxWidth())
            val context = LocalContext.current
            DropdownMenuItem(
                text = { Text("Delete", color =  Color.Black) },
                onClick = {
                    showDialog.value = false
                    api.deleteNote(note.id, SecurePrefs.getToken().toString()){
                        Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
                        refresh.value=!refresh.value
                    }
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Edit",
                        tint = Color.Black
                    )
                }
            )

        }
    }
}
fun loadMoreNotes(page: Int, list: SnapshotStateList<Notes>, onDone: () -> Unit) {
    api.getnotes(SecurePrefs.getToken().toString(),page * 10 ) { newNotes ->
        if(!newNotes.isNullOrEmpty()) list.addAll(newNotes)
        onDone()
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addNotesScreen(navController: NavController, modifier: Modifier,onBack: ()-> Unit, selectedTabDestination: MutableState<Int>, tit: String="", des: String="", id: Int=0){
    BackHandler {
        selectedTabDestination.value=0
        navController.navigate("userpage")
    }
    var title = remember { mutableStateOf(tit) }
    var description = remember { mutableStateOf(des) }
    var isLoading = remember { mutableStateOf(false) }
    var show = remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition()
    val hueShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing)
        )
    )
    val context= LocalContext.current
    val scrollState = rememberScrollState()
    val scrollState1 = rememberScrollState()
    val scrollState2 = rememberScrollState()
    val hue = (hueShift + 2 * (360f / 16)) % 360f
    val color = Color.hsv(hue, 0.9f,0.9f)
    Scaffold( modifier = modifier
        .fillMaxSize()
        .background(Color.Black),
        topBar = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton({navController.navigate("signup"); SecurePrefs.clearToken()}){
                    Column() {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Close", modifier = Modifier.size(60.dp))

                    }
                }
            }
        },
        bottomBar = {PrimaryTabRow(
            selectedTabIndex = selectedTabDestination.value,
            containerColor = Color.Transparent,
            contentColor = Color.White

        ){
            Tab(
                selected =selectedTabDestination.value==0,
                onClick = {
                    selectedTabDestination.value=0
                    navController.navigate("userpage") {

                    }
                },

                icon = {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null, modifier = Modifier.size(45.dp)
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray
            )
            Tab(
                selected =selectedTabDestination.value==1,
                onClick = {
                    selectedTabDestination.value=1
                    navController.navigate("addnotes") {

                    }
                },

                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null, modifier = Modifier.size(45.dp)
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray
            )


        }}
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                .background(Color(23,23,63,255))
                .padding(16.dp)
                .verticalScroll(scrollState1), verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0,1,80,255)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = title.value,
                        onValueChange = {
                            if (it.length <= 255) title.value = it
                        },
                        label = { Text("Title") },
                        placeholder = { Text("What's your question about?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(Color.LightGray)
                    )
                    Text(
                        "${title.value.length}/255",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp), color = Color.LightGray
                    )
                    OutlinedTextField(
                        value = description.value,
                        onValueChange = { description.value = it },
                        label = { Text("Description") },
                        placeholder = { Text("Explain your question in detail...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 10, textStyle = TextStyle(Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isLoading.value = true
                            if (tit!=""){
                                api.editNotes(id, title.value, description.value, SecurePrefs.getToken().toString()){
                                    title.value = ""
                                    description.value = ""
                                    isLoading.value = false
                                    val msg = it.message ?: "Note updated successfully"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    navController.navigate("userpage")

                                }
                            }
                            else {
                                api.sendnotes(
                                    title.value,
                                    description.value,
                                    SecurePrefs.getToken().toString()
                                ) {
                                    title.value = ""
                                    description.value = ""
                                    isLoading.value = false
                                    if (it.message.contains("You have inserted 5 notes in the last hour already")) {
                                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                }
                            }
                            },
                enabled = title.value.isNotBlank() && !isLoading.value,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(color)
                 ){
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Post Notes", color= Color.Black)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun signinscreen(navController: NavController, tileWidth: Dp, tileHeight: Dp, modifier: Modifier){
    BackHandler {  }
    val isLoading=remember { mutableStateOf(true) }
    var errorMsg = remember { mutableStateOf<String?>(null) }
    isLoading.value = false
    LaunchedEffect(Unit) {
        try {

            if (SecurePrefs.getToken() != null) {
                api.loginWithToken(
                    SecurePrefs.getToken().toString(), navController
                ) {
                    isLoading.value=false
                }
            } else {
                println("token")
                isLoading.value = false
            }
        } catch (e: Exception) {
            isLoading.value = false

        }
    }
    var username by remember { mutableStateOf("") }
    var emailid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("Done") }
    Column(
        modifier = Modifier
            .fillMaxSize().background(Color(0xFF121212))
            , verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    Box(
        modifier = Modifier.background(Color(0xFF121212)))
    {
        var mode by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier

                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(modifier = Modifier.clip(RoundedCornerShape(tileHeight))) {
                IconButton(
                    onClick = { mode = 0 },
                    modifier = Modifier
                        .size(tileWidth * 5, tileHeight * 1.5f)
                        .background(if (mode == 0) Color.White else Color.Gray)
                ) {
                    Text(
                        "SIGN IN",
                        color = if (mode == 0) Color.Black else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
                IconButton(
                    onClick = { mode = 1 },
                    modifier = Modifier
                        .size(tileWidth * 5, tileHeight * 1.5f)
                        .background(if (mode == 1) Color.White else Color.Gray)
                ) {

                    Text(
                        "SIGN UP",
                        color = if (mode == 1) Color.Black else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text( "Username") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black
                )
            )
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {

            }
            val context = LocalContext.current
            errorMsg.value?.let {
                if (it == "Unauthorized") Text(
                    "Invalid Username or Password",
                    color = Color.Red
                ) else Text(it, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (mode == 1) {

                        api.signupUser(user = UserSignup(username, password)) {
                            api.loginUser(user = UserSignup(username, password)){
                                navController.navigate("userpage")
                            }
                        }
                    } else {
                        api.loginUser(user = UserSignup(username, password)){
                            navController.navigate("userpage")
                        }
                    }
                },
                modifier = Modifier
                    .size(tileWidth * 5, tileHeight * 2),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(tileHeight)
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(color = Color.Cyan, strokeCap = StrokeCap.Round)
                } else {
                    Text(if (mode == 0) "SIGN IN" else "SIGN UP")
                }
            }
        }
    }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotesTheme {
        Greeting("Android")
    }
}