package com.example.sos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sos.data.SosRepository
import com.example.sos.feature.auth.choose.ChooseScreen
import com.example.sos.feature.auth.login.LoginScreen
import com.example.sos.feature.auth.regis.InterestScreen
import com.example.sos.feature.auth.regis.RegisScreen
import com.example.sos.feature.auth.regis.StudentProfileScreen
import com.example.sos.feature.auth.regis.VerificationScreen
import com.example.sos.feature.welcome.WelcomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onStartClick = {
                    navController.navigate("choose") {
                        popUpTo("welcome") {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable("choose") {
            ChooseScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onCreateAccountClick = {
                    navController.navigate("registration")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    // TODO: Ganti dengan login ke backend dan tampilkan error jika gagal.
                    if (SosRepository.login(email, password)) {
                        // Role ADMIN → workspace admin; selain itu → area client.
                        val destination = if (SosRepository.isAdminSession) "admin" else "main"
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onSignUpClick = {
                    navController.navigate("registration")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Alur registrasi: registration → verification → student_profile → interest
        composable("registration") {
            RegisScreen(
                onSignUpClick = {
                    navController.navigate("verification")
                },
                onLoginClick = {
                    navController.navigate("login")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("verification") {
            VerificationScreen(
                onContinueClick = {
                    navController.navigate("student_profile")
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onResendClick = {
                    // TODO: Kirim ulang kode OTP.
                }
            )
        }

        composable("student_profile") {
            StudentProfileScreen(
                onNextClick = {
                    navController.navigate("interest")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("interest") {
            InterestScreen(
                onCompleteClick = { _ ->
                    // TODO: Simpan bidang minat ke profil lewat backend.
                    // Registrasi selesai → langsung masuk ke area client.
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Workspace Admin (verifikasi, moderasi, dispute, transaksi).
        composable("admin") {
            AdminNavigation(
                onLogout = {
                    SosRepository.logout()
                    navController.navigate("choose") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Area client (user biasa) dengan bottom navigation.
        composable("main") {
            MainScreen(
                onLogout = {
                    SosRepository.logout()
                    navController.navigate("choose") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
