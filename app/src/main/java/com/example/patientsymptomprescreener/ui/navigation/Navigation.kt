package com.prescreener.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prescreener.ui.SharedViewModel
import com.prescreener.ui.screens.*

sealed class Screen(val route: String) {
    object Welcome       : Screen("welcome")
    object PatientInfo   : Screen("patient_info")
    object Symptoms      : Screen("symptoms")
    object Results       : Screen("results")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Single shared ViewModel across all screens
    val sharedViewModel: SharedViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Welcome.route) {

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.PatientInfo.route) }
            )
        }

        composable(Screen.PatientInfo.route) {
            PatientInfoScreen(
                viewModel = sharedViewModel,
                onNext = { navController.navigate(Screen.Symptoms.route) }
            )
        }

        composable(Screen.Symptoms.route) {
            SymptomsScreen(
                viewModel = sharedViewModel,
                onAnalyzeComplete = { navController.navigate(Screen.Results.route) }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(
                viewModel = sharedViewModel,
                onNewAssessment = {
                    sharedViewModel.resetAll()
                    navController.popBackStack(Screen.Welcome.route, inclusive = false)
                }
            )
        }
    }
}


