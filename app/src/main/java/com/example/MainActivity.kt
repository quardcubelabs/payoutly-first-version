package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddTransactionDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.LimeBrand

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FinanceViewModel = viewModel()
                val isLocked by viewModel.isAppLocked.collectAsState()
                val session by viewModel.userSession.collectAsState()

                when {
                    isLocked -> {
                        LockScreen(viewModel)
                    }
                    session == null -> {
                        LoginScreen(viewModel)
                    }
                    else -> {
                        MainLayout(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: FinanceViewModel) {
    var selectedTab by remember { mutableStateOf("HOME") } // "HOME", "STAT", "GOALS", "SETTINGS"
    var showAddDialog by remember { mutableStateOf(false) }
    var initialTypeForDialog by remember { mutableStateOf("EXPENSE") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0C0D0E),
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onFabClick = {
                    initialTypeForDialog = "EXPENSE"
                    showAddDialog = true
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding() // Ensures no notch clipping or bottom overlap
        ) {
            when (selectedTab) {
                "HOME" -> DashboardScreen(
                    viewModel = viewModel,
                    onQuickActionClick = { action ->
                        when (action) {
                            "SEND" -> {
                                initialTypeForDialog = "EXPENSE"
                                showAddDialog = true
                            }
                            "RECEIVE" -> {
                                initialTypeForDialog = "INCOME"
                                showAddDialog = true
                            }
                            "GOAL" -> {
                                selectedTab = "GOALS"
                            }
                            "SETTINGS" -> {
                                selectedTab = "SETTINGS"
                            }
                        }
                    }
                )
                "STAT" -> StatisticsScreen(viewModel)
                "GOALS" -> GoalsScreen(viewModel)
                "SETTINGS" -> SettingsScreen(viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            initialType = initialTypeForDialog,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun BottomNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(Color(0xFF0E0F11))
            .navigationBarsPadding(), // Ensures gesture navigation bar spacing safety
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left tabs
            NavBarItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = selectedTab == "HOME",
                onClick = { onTabSelected("HOME") }
            )

            NavBarItem(
                label = "Statistic",
                icon = Icons.Default.BarChart,
                isSelected = selectedTab == "STAT",
                onClick = { onTabSelected("STAT") }
            )

            // Central FAB spacer
            Spacer(modifier = Modifier.width(56.dp))

            // Right tabs
            NavBarItem(
                label = "Goals",
                icon = Icons.Default.TrackChanges,
                isSelected = selectedTab == "GOALS",
                onClick = { onTabSelected("GOALS") }
            )

            NavBarItem(
                label = "Preference",
                icon = Icons.Default.Settings,
                isSelected = selectedTab == "SETTINGS",
                onClick = { onTabSelected("SETTINGS") }
            )
        }

        // Floating Action Button centered
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(56.dp)
                .background(LimeBrand, shape = CircleShape)
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Expense Entry",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) LimeBrand else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) LimeBrand else Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
