package com.example.medisync.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

import com.example.medisync.ui.navigation.UserTopBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip

import androidx.compose.runtime.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.horizontalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.IconButton

@Composable
fun UserReportsScreen(
    onNavigateToReportDetail: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Newest First") }
    
    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            searchQuery = ""
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        UserTopBar(
            title = "Reports",
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchActiveChange = { active ->
                isSearchActive = active
                if (active) selectedCategory = "All"
                if (!active) searchQuery = ""
            },
            onSearchQueryChange = { searchQuery = it }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Category & Filter Row (Hide if typing search query)
            if (searchQuery.isEmpty()) {
                val categories = listOf("All", "Lab", "Blood", "Others")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scrolling categories
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory
                            
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) colorScheme.secondary else colorScheme.surface)
                                    .border(
                                        1.dp, 
                                        if (isSelected) Color.Transparent else colorScheme.outlineVariant, 
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category, 
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Filter Menu
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                tint = colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            listOf("Newest First", "Oldest First").forEach { sortOption ->
                                DropdownMenuItem(
                                    text = { Text(sortOption, color = colorScheme.onSurface) },
                                    onClick = {
                                        selectedSort = sortOption
                                        showSortMenu = false
                                    },
                                    trailingIcon = {
                                        if (selectedSort == sortOption) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = colorScheme.secondary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Reports List Grouped by Year
            val reports = listOf(
                Triple("Comprehensive Metabolic Panel", "Oct 24, 2026 • Lab Results", Icons.Default.Science),
                Triple("Chest X-Ray", "Oct 12, 2026 • Imaging", Icons.Default.MedicalInformation),
                Triple("Prescription Renewal", "Sep 30, 2026 • Clinical Notes", Icons.Default.Science),
                Triple("Complete Blood Count", "Jul 15, 2025 • Lab Results", Icons.Default.Science),
                Triple("MRI Scan", "Feb 10, 2025 • Imaging", Icons.Default.MedicalInformation)
            )
            
            val groupedReports = reports.groupBy { 
                it.second.substringAfterLast(", ").substringBefore(" ") 
            }
            
            groupedReports.forEach { (year, yearReports) ->
                // Section Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = year,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${yearReports.size} reports",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                
                // List Items
                yearReports.forEach { report ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReportDetail() }
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(report.third, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    report.first, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                                    color = colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    report.second, fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
            
            // Add padding at the bottom for NavBar
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}