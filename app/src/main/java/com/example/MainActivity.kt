package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WeightLog
import com.example.data.WorkoutLog
import com.example.ui.DashboardStats
import com.example.ui.FitnessViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftGrayText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FitnessTrackerApp()
            }
        }
    }
}

@Composable
fun FitnessTrackerApp() {
    val viewModel: FitnessViewModel = viewModel()
    
    val workouts by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val weights by viewModel.weightLogs.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Workouts, 1 = Weights

    val context = LocalContext.current
    
    // Auto-generate sample data if both tables are completely empty to instantly showcase visual dashboards
    LaunchedEffect(workouts, weights) {
        if (workouts.isEmpty() && weights.isEmpty()) {
            viewModel.generateSampleData()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        showAddWorkoutDialog = true
                    } else {
                        showAddWeightDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_workout_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (selectedTab == 0) "Log Workout" else "Log Weight",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp) // Support tablets/foldables gracefully
                    .align(Alignment.TopCenter)
            ) {
                // Header section with Greeting & Action Row
                AthleteHeader(
                    onResetAll = {
                        // Safe clean simulation, delete items but we let users add them back manually
                    },
                    onPopulateSample = {
                        viewModel.generateSampleData()
                    }
                )

                // Stats Banner Grid Component showing live calculated state
                StatsGrid(stats = stats)

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Graphic Progress Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Body Weight Journey",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Chronological body mass adaptation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftGrayText
                                )
                            }
                            IconButton(
                                onClick = { showAddWeightDialog = true },
                                modifier = Modifier.testTag("quick_add_weight_chart_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Quick Weight LOG",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom drawn line trend graphics
                        WeightTrendChart(weightLogs = weights)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Content Console
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Workout Tab Button
                    val isWorkoutActive = selectedTab == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isWorkoutActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 12.dp)
                            .testTag("workout_tab_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Workouts Feed",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isWorkoutActive) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    }

                    // Weight Tab Button
                    val isWeightActive = selectedTab == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isWeightActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 12.dp)
                            .testTag("weight_tab_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Weigh-in History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeightActive) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Feed List
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        if (workouts.isEmpty()) {
                            EmptyStateFiller(
                                title = "No Workouts Logged Yet",
                                desc = "Establish a routine by adding resistance lifts, stretching or running intervals.",
                                actionText = "Record Workout",
                                onAction = { showAddWorkoutDialog = true }
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(workouts) { log ->
                                    WorkoutLogCard(
                                        log = log,
                                        onDelete = { viewModel.deleteWorkout(log.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        if (weights.isEmpty()) {
                            EmptyStateFiller(
                                title = "No Weight Logs Found",
                                desc = "Track body weight adaptations over time to build visual timeline insights.",
                                actionText = "Log Initial Weight",
                                onAction = { showAddWeightDialog = true }
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(weights) { log ->
                                    WeightLogCard(
                                        log = log,
                                        onDelete = { viewModel.deleteWeight(log.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog overlays
    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onSubmit = { type, name, sets, reps, weight, duration, calories, notes ->
                viewModel.insertWorkout(type, name, sets, reps, weight, duration, calories, notes)
                showAddWorkoutDialog = false
            }
        )
    }

    if (showAddWeightDialog) {
        AddWeightDialog(
            onDismiss = { showAddWeightDialog = false },
            onSubmit = { weight, notes ->
                viewModel.insertWeight(weight, notes)
                showAddWeightDialog = false
            }
        )
    }
}

@Composable
fun AthleteHeader(
    onResetAll: () -> Unit,
    onPopulateSample: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ATHLETIC STATS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Welcome, Athlete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = SoftGrayText
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onPopulateSample,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .testTag("populate_sample_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Demo Logs",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun StatsGrid(stats: DashboardStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Current Weight",
                value = "${"%.1f".format(stats.currentWeightKg)} kg",
                subtext = if (stats.weightChangeKg == 0f) "Baseline" else "${if (stats.weightChangeKg > 0) "+" else ""}${"%.1f".format(stats.weightChangeKg)} kg",
                icon = Icons.Default.Info, // custom represent scale
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Training Streak",
                value = "${stats.streakDays} Days",
                subtext = if (stats.streakDays > 0) "Stay consistent!" else "Log workout today",
                icon = Icons.Default.Star, // Fire flame alternative
                iconColor = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Total Minutes",
                value = "${stats.totalActiveMinutes} min",
                subtext = "${stats.totalWorkouts} total logs",
                icon = Icons.Default.DateRange, // clock timer alternative
                iconColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Calories Burned",
                value = "${stats.totalCaloriesBurned} kcal",
                subtext = "Estimated metabolic effort",
                icon = Icons.Default.Favorite, // Flame burn alternative
                iconColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SoftGrayText,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = SoftGrayText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WeightTrendChart(
    weightLogs: List<WeightLog>,
    modifier: Modifier = Modifier
) {
    if (weightLogs.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .drawBehind {
                    val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                        strokeWidth = 2f,
                        pathEffect = pathEffect
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "No Trend Graphic",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track at least 2 weigh-ins to plot smooth progress curves.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftGrayText,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Oldest logs first for chronological mapping
        val sortedLogs = weightLogs.sortedBy { it.timestamp }
        val weights = sortedLogs.map { it.weightKg }
        val minW = weights.minOrNull() ?: 0f
        val maxW = weights.maxOrNull() ?: 100f
        
        val range = maxW - minW
        val displayMin = if (range == 0f) minW - 3f else minW - (range * 0.15f)
        val displayMax = if (range == 0f) maxW + 3f else maxW + (range * 0.15f)
        val displayRange = displayMax - displayMin

        val primaryColor = MaterialTheme.colorScheme.primary

        Column {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointsCount = sortedLogs.size
                
                // Draw target background horizontal bars (dashed grids)
                val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val lines = 3
                for (i in 0..lines) {
                    val yLine = height * (i.toFloat() / lines)
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = androidx.compose.ui.geometry.Offset(0f, yLine),
                        end = androidx.compose.ui.geometry.Offset(width, yLine),
                        strokeWidth = 1f,
                        pathEffect = pathEffect
                    )
                }

                // Plotting scales
                val stepX = width / (pointsCount - 1)
                val coordinates = sortedLogs.mapIndexed { index, weightLog ->
                    val x = index * stepX
                    val yRatio = (weightLog.weightKg - displayMin) / displayRange
                    val y = height - (yRatio * height)
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                // Smooth gradient fill below line
                val fillPath = Path().apply {
                    moveTo(0f, height)
                    coordinates.forEach { offset ->
                        lineTo(offset.x, offset.y)
                    }
                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )

                // Render vector trend line path
                val linePath = Path().apply {
                    coordinates.firstOrNull()?.let { moveTo(it.x, it.y) }
                    for (i in 1 until coordinates.size) {
                        lineTo(coordinates[i].x, coordinates[i].y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                // Primary nodes with custom rings
                coordinates.forEach { offset ->
                    drawCircle(
                        color = primaryColor,
                        radius = 8f,
                        center = offset
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 4f,
                        center = offset
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Graph X axis data timelines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cal = Calendar.getInstance()
                val formatShortDate = { ms: Long ->
                    cal.timeInMillis = ms
                    val month = cal.get(Calendar.MONTH) + 1
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    "$month/$day"
                }

                Text(
                    text = "${"%.1f".format(sortedLogs.first().weightKg)} kg (${formatShortDate(sortedLogs.first().timestamp)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGrayText
                )

                Text(
                    text = "Overall range: ${"%.1f".format(displayMin)} - ${"%.1f".format(displayMax)} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGrayText,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${"%.1f".format(sortedLogs.last().weightKg)} kg (Today)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkoutLogCard(
    log: WorkoutLog,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.timestamp))
    
    // Choose specific colors/borders based on training types
    val (accentColor, typeIcon) = when (log.workoutType) {
        "Strength" -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.Info) // representation
        "Cardio" -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.DateRange)
        "Mindfulness" -> Pair(MaterialTheme.colorScheme.tertiary, Icons.Default.Favorite)
        else -> Pair(Color.White, Icons.Default.Star)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stylized category column
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Central details column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGrayText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Inline dynamic statistics metrics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.workoutType == "Strength") {
                        if (log.sets != null && log.reps != null) {
                            Text(
                                text = "${log.sets} sets × ${log.reps} reps",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        if (log.weightKg != null && log.weightKg > 0) {
                            Text(
                                text = "@ ${"%.1f".format(log.weightKg)} kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        if (log.durationMinutes != null && log.durationMinutes > 0) {
                            Text(
                                text = "Duration: ${log.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }

                    if (log.caloriesBurned != null && log.caloriesBurned > 0) {
                        Text(
                            text = "🔥 ${log.caloriesBurned} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (log.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notes: ${log.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftGrayText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trash action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_workout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Log",
                    tint = SoftGrayText.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun WeightLogCard(
    log: WeightLog,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault()).format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info, // Scale surrogate
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${"%.1f".format(log.weightKg)} kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGrayText
                    )
                }

                if (log.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notes: ${log.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftGrayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_weight_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove weighing",
                    tint = SoftGrayText.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateFiller(
    title: String,
    desc: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SoftGrayText,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = SoftGrayText,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = actionText, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        type: String,
        name: String,
        sets: Int?,
        reps: Int?,
        weight: Float?,
        duration: Int?,
        calories: Int?,
        notes: String
    ) -> Unit
) {
    var type by remember { mutableStateOf("Strength") } // Strength, Cardio, Mindfulness
    var exerciseName by remember { mutableStateOf("") }
    
    var setsText by remember { mutableStateOf("") }
    var repsText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    
    var durationText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Fitness Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Segment Selection
                Text(
                    text = "Activity Classification",
                    style = MaterialTheme.typography.labelMedium,
                    color = SoftGrayText,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("Strength", "Cardio", "Mindfulness")
                    categories.forEach { cat ->
                        val isSelected = type == cat
                        Button(
                            onClick = { type = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Exercise Title field
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = {
                        exerciseName = it
                        hasError = false
                    },
                    label = { Text("Exercise / Routine Name") },
                    placeholder = { Text("e.g. Bench Press, 5K Jog") },
                    isError = hasError && exerciseName.isBlank(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_name_input")
                )

                if (type == "Strength") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = setsText,
                            onValueChange = { setsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Sets") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { repsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Weight used (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { c -> c.isDigit() } },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Est. Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Felt how?") },
                    placeholder = { Text("Smooth reps, slight fatigue, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Text(
                        text = "Please enter an exercise or routine name.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (exerciseName.trim().isBlank()) {
                        hasError = true
                    } else {
                        val sets = setsText.toIntOrNull()
                        val reps = repsText.toIntOrNull()
                        val weight = weightText.toFloatOrNull()
                        val duration = durationText.toIntOrNull() ?: if (type == "Strength") 30 else 15
                        val calories = caloriesText.toIntOrNull() ?: if (type == "Strength") 200 else 120
                        
                        onSubmit(type, exerciseName.trim(), sets, reps, weight, duration, calories, notesText.trim())
                    }
                },
                modifier = Modifier.testTag("submit_workout_button")
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SoftGrayText)
            }
        }
    )
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSubmit: (weight: Float, notes: String) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Body Weight",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Logging consistent morning body weight establishes clean trend diagnostics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftGrayText
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it.filter { c -> c.isDigit() || c == '.' }
                        hasError = false
                    },
                    label = { Text("Weight reading (kg)") },
                    placeholder = { Text("e.g. 74.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_input_field")
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Weigh-in Notes") },
                    placeholder = { Text("Fasted weight, post workout, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Text(
                        text = "Please enter a valid numeric weight reading (e.g. 72.8)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightText.toFloatOrNull()
                    if (weight == null || weight <= 0f) {
                        hasError = true
                    } else {
                        onSubmit(weight, notesText.trim())
                    }
                },
                modifier = Modifier.testTag("submit_weight_button")
            ) {
                Text("Record Reading")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SoftGrayText)
            }
        }
    )
}
