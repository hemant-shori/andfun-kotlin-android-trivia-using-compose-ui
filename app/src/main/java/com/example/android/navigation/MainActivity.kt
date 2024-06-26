package com.example.android.navigation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.android.navigation.ui.theme.AndroidTriviaTheme
import com.example.android.navigation.viewmodels.GameViewModel
import kotlinx.coroutines.launch

/*
 * enum class to define the routes.
 */
enum class TriviaAppScreens(@StringRes val title: Int) {
    GameTitleScreen(R.string.app_name),
    GameScreen(R.string.title_android_trivia_question),
    GameWonScreen(R.string.congratulations),
    GameOverScreen(R.string.game_over),
    GameRulesScreen(R.string.rules),
    AboutGameScreen(R.string.about),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTriviaTheme {
                RootNavigationDrawer()
            }
        }
    }

    @Composable
    fun RootNavigationDrawer(
        navigationController: NavHostController = rememberNavController(),
        viewModel: GameViewModel = viewModel()
    ) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val backStackEntry by navigationController.currentBackStackEntryAsState()
        ModalNavigationDrawer(
            drawerContent = {
                DrawerContent(drawerState, navigationController)
            },
            drawerState = drawerState,
            gesturesEnabled = backStackEntry?.destination?.route.equals(
                TriviaAppScreens.GameTitleScreen.name
            )
        ) {
            ScaffoldContent(navigationController, viewModel, drawerState)
        }
    }

    @Composable
    private fun DrawerContent(drawerState: DrawerState, navigationController: NavHostController) {
        val scope = rememberCoroutineScope()
        val backStackEntry by navigationController.currentBackStackEntryAsState()
        ModalDrawerSheet {
            Spacer(modifier = Modifier.height(26.dp))
            Image(
                painter = painterResource(id = R.drawable.about_android_trivia),
                contentDescription = "",
                modifier = Modifier
                    .size(150.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(26.dp))
            data class TriviaNavigationItem(
                val vectorRes: Int, val stringRes: Int, val route: String
            )

            val navItems = listOf(
                TriviaNavigationItem(
                    stringRes = R.string.rules,
                    vectorRes = R.drawable.rules,
                    route = TriviaAppScreens.GameRulesScreen.name
                ),
                TriviaNavigationItem(
                    stringRes = R.string.about,
                    vectorRes = R.drawable.android,
                    route = TriviaAppScreens.AboutGameScreen.name
                )
            )
            navItems.forEach {
                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(id = it.stringRes))
                    },
                    selected = backStackEntry?.destination?.route.equals(it.route),
                    onClick = {
                        navigationController.navigate(it.route)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(it.vectorRes),
                            contentDescription = stringResource(id = it.stringRes)
                        )
                    }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    @Composable
    private fun ScaffoldContent(
        navigationController: NavHostController,
        viewModel: GameViewModel,
        drawerState: DrawerState
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.secondary,
            topBar = {
                val uiState by viewModel.uiState.collectAsState()
                GameTopBar(
                    viewModel.numQuestions,
                    navigationController,
                    uiState.questionIndex,
                    // Add Support for the Up Button
                    navigateUp = { navigateToHomeScreen(viewModel, navigationController) },
                    drawerState
                )
            },
            modifier = Modifier.background(color = Color.Yellow),
            content = { paddingValues ->
                GameNavHost(navigationController, viewModel, paddingValues)
            }
        )
    }

    @Composable
    private fun GameNavHost(
        navigationController: NavHostController,
        viewModel: GameViewModel,
        paddingValues: PaddingValues
    ) {
        val uiState by viewModel.uiState.collectAsState()
        NavHost(
            navController = navigationController,
            startDestination = TriviaAppScreens.GameTitleScreen.name,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Title Screen Route
            composable(route = TriviaAppScreens.GameTitleScreen.name) {
                GameTitleContent(
                    onPlayButtonClicked = {
                        // Shuffles the questions and sets the question index to the first question.
                        viewModel.randomizeQuestions()
                        navigationController.navigate(TriviaAppScreens.GameScreen.name)
                    }
                )
            }
            // Play Game Route
            composable(route = TriviaAppScreens.GameScreen.name,
                enterTransition = { slideInHorizontally() },
                exitTransition = { slideOutHorizontally() },
            ) {
                val gameResultListener: (Boolean) -> Unit = { result ->
                    if (result) {
                        navigationController.navigate(TriviaAppScreens.GameWonScreen.name)
                    } else {
                        navigationController.navigate(TriviaAppScreens.GameOverScreen.name)
                    }
                    // solution for exercise 07: Step.07.Exercise-Adding-Safe-Arguments
                    Toast.makeText(
                        applicationContext,
                        "NumCorrect: ${viewModel.uiState.value.questionIndex}," +
                                " NumQuestions: ${viewModel.numQuestions}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                PlayGameScreen(
                    onOptionSelected = { viewModel.setSelectedAnswer(it) },
                    onSubmitButtonClicked = { viewModel.matchAnswer(gameResultListener) },
                    uiState = uiState
                )
            }
            // Game Win Route
            composable(route = TriviaAppScreens.GameWonScreen.name,
                enterTransition = { slideInHorizontally() },
                exitTransition = { slideOutHorizontally() }
            ) {
                GameWonScreen(
                    // Navigate back to title screen
                    nextMatchListener = { navigateToHomeScreen(viewModel, navigationController) }
                )
            }
            // Game Over Route
            composable(route = TriviaAppScreens.GameOverScreen.name,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                GameOverScreen(
                    // Navigate back to title screen
                    tryAgainListener = { navigateToHomeScreen(viewModel, navigationController) }
                )
            }
            // About Game Route
            composable(route = TriviaAppScreens.AboutGameScreen.name,
                enterTransition = { slideInHorizontally() },
                exitTransition = { slideOutHorizontally() }
            ) {
                AboutGameScreen()
            }
            // Game Rules Route
            composable(route = TriviaAppScreens.GameRulesScreen.name,
                enterTransition = { slideInHorizontally() },
                exitTransition = { slideOutHorizontally() }
            ) {
                GameRulesScreen()
            }
        }
    }

    private fun navigateToHomeScreen(
        viewModel: GameViewModel,
        navigationController: NavHostController
    ) {
        // Hide the share button by shuffling the questions and sets the question
        // index to the first question.
        viewModel.randomizeQuestions()
        // Pop back stack to the game title screen
        navigationController.popBackStack(
            route = TriviaAppScreens.GameTitleScreen.name,
            inclusive = false
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GameTopBar(
        numQuestions: Int,
        navigationController: NavHostController,
        questionNo: Int,
        navigateUp: () -> Unit,
        drawerState: DrawerState
    ) {
        var showMenu by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val backStackEntry by navigationController.currentBackStackEntryAsState()
        val currentScreenTitle = TriviaAppScreens.valueOf(
            backStackEntry?.destination?.route ?: TriviaAppScreens.GameTitleScreen.name
        ).title
        val canNavigateBack = navigationController.previousBackStackEntry != null

        TopAppBar(
            title = {
                Text(
                    text = stringResource(
                        id = currentScreenTitle,
                        formatArgs = arrayOf(questionNo + 1, 3)
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                titleContentColor = MaterialTheme.colorScheme.onSecondary
            ),
            // Add Support for navigation icon that is displayed at the start of the top app bar
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button)
                        )
                    }
                } else {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(id = R.string.app_name)
                        )
                    }
                }
            },
            actions = {
                if (numQuestions == questionNo) {
                    IconButton(onClick = {
                        shareSuccess(numQuestions, questionNo)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
                if (!canNavigateBack) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = TriviaAppScreens.AboutGameScreen.title)
                            )
                        },
                        onClick = {
                            navigationController.navigate(TriviaAppScreens.AboutGameScreen.name)
                            // collapse the DropdownMenu
                            showMenu = !showMenu
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = TriviaAppScreens.GameRulesScreen.title)
                            )
                        },
                        onClick = {
                            navigationController.navigate(TriviaAppScreens.GameRulesScreen.name)
                            // collapse the DropdownMenu
                            showMenu = !showMenu
                        }
                    )
                }
            }
        )
    }

    // Starting an Activity with our new Intent
    private fun shareSuccess(numQuestions: Int, numCorrect: Int) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
            .putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_success_text, numCorrect, numQuestions)
            )
        startActivity(shareIntent)
    }

    @Preview(showBackground = true)
    @Composable
    fun RootGamePreview() {
        AndroidTriviaTheme {
            RootNavigationDrawer()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ScaffoldContentPreview() {
        AndroidTriviaTheme {
            ScaffoldContent(
                navigationController = rememberNavController(),
                viewModel = GameViewModel(),
                rememberDrawerState(initialValue = DrawerValue.Closed)
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DrawerContentPreview() {
        AndroidTriviaTheme {
            DrawerContent(
                rememberDrawerState(initialValue = DrawerValue.Closed),
                rememberNavController()
            )
        }
    }
}
