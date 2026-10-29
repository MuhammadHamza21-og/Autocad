package com.example.ui.screens.wizard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.engine.GeometryEngine
import com.example.engine.LayoutCandidate
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWizardScreen(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.wizardStep.collectAsState()
    val wizardRoomReqs by viewModel.wizardRoomRequirements.collectAsState()
    val alternatives by viewModel.wizardAlternatives.collectAsState()
    val selectedAltIndex by viewModel.wizardSelectedAltIndex.collectAsState()

    BackHandler {
        if (currentStep > 1) {
            viewModel.setWizardStep(currentStep - 1)
        } else {
            viewModel.navigateTo(Screen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0F1D),
        topBar = {
            Surface(
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentStep > 1) viewModel.setWizardStep(currentStep - 1)
                                else viewModel.navigateTo(Screen.DASHBOARD)
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFE2E8F0))
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(
                                text = "Project Creation Wizard",
                                color = Color(0xFFF8FAFC),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Step $currentStep of 5: ${getStepTitle(currentStep)}",
                                color = Color(0xFF00B4D8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Step Indicator Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..5) {
                            val isActive = i <= currentStep
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        if (isActive) Color(0xFF00B4D8) else Color(0xFF334155),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.setWizardStep(currentStep - 1) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(Modifier.width(10.dp))
                    }

                    if (currentStep < 5) {
                        Button(
                            onClick = { viewModel.setWizardStep(currentStep + 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                        ) {
                            Text("Next Step", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A0F1D))
                        }
                    } else {
                        Button(
                            onClick = { viewModel.applyWizardAlternative() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0A0F1D))
                            Spacer(Modifier.width(6.dp))
                            Text("Open in CAD Editor", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (currentStep) {
                1 -> WizardStep1Details(viewModel)
                2 -> WizardStep2ShapeAndUnits(viewModel)
                3 -> WizardStep3Measurements(viewModel)
                4 -> WizardStep4RoomSchedule(viewModel, wizardRoomReqs)
                5 -> WizardStep5Layouts(viewModel, alternatives, selectedAltIndex)
            }
        }
    }
}

private fun getStepTitle(step: Int): String = when (step) {
    1 -> "Project & Client Information"
    2 -> "Plot Geometry & Area Conventions"
    3 -> "Site Measurements & Setbacks"
    4 -> "Building Requirements & Room Schedule"
    5 -> "AI Layout Alternatives & Comparison"
    else -> ""
}

// STEP 1: Details
@Composable
private fun WizardStep1Details(viewModel: CadViewModel) {
    val pName by viewModel.wizardProjectName.collectAsState()
    val cName by viewModel.wizardClientName.collectAsState()
    val sLoc by viewModel.wizardSiteLocation.collectAsState()
    val dTitle by viewModel.wizardDrawingTitle.collectAsState()
    val notes by viewModel.wizardNotes.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "Enter project metadata for drawing title block and documentation.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        }

        item {
            WizardTextField(
                value = pName,
                onValueChange = { viewModel.wizardProjectName.value = it },
                label = "Project Name",
                placeholder = "e.g. 10 Marla Modern Villa"
            )
        }

        item {
            WizardTextField(
                value = cName,
                onValueChange = { viewModel.wizardClientName.value = it },
                label = "Client Name (Optional)",
                placeholder = "e.g. Mr. Hamza Tariq"
            )
        }

        item {
            WizardTextField(
                value = sLoc,
                onValueChange = { viewModel.wizardSiteLocation.value = it },
                label = "Site Location",
                placeholder = "e.g. DHA Phase 6, Sector C, Lahore"
            )
        }

        item {
            WizardTextField(
                value = dTitle,
                onValueChange = { viewModel.wizardDrawingTitle.value = it },
                label = "Drawing Title",
                placeholder = "e.g. ARCHITECTURAL WORKING PLAN"
            )
        }

        item {
            WizardTextField(
                value = notes,
                onValueChange = { viewModel.wizardNotes.value = it },
                label = "Draftsman Notes",
                placeholder = "e.g. All internal walls are 4.5\" unless noted.",
                maxLines = 3
            )
        }
    }
}

// STEP 2: Shape & Units
@Composable
private fun WizardStep2ShapeAndUnits(viewModel: CadViewModel) {
    val plotShape by viewModel.wizardPlotShape.collectAsState()
    val unitSystem by viewModel.wizardUnitSystem.collectAsState()
    val marlaConvention by viewModel.wizardMarlaConvention.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "SELECT PLOT SHAPE",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(PlotShapeType.values()) { shape ->
            val isSelected = shape == plotShape
            SelectableCard(
                title = shape.label,
                subtitle = when (shape) {
                    PlotShapeType.RECTANGLE -> "Standard 90° corners, parallel sides."
                    PlotShapeType.TRAPEZOID -> "Front and rear widths differ or non-parallel sides."
                    PlotShapeType.IRREGULAR_4_SIDED -> "Corner plot with diagonal survey measurement."
                    else -> "Survey coordinate vertices."
                },
                isSelected = isSelected,
                onClick = { viewModel.wizardPlotShape.value = shape }
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "REGIONAL AREA CONVENTION",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(MarlaConvention.values()) { conv ->
            val isSelected = conv == marlaConvention
            SelectableCard(
                title = conv.label,
                subtitle = conv.description,
                isSelected = isSelected,
                onClick = { viewModel.wizardMarlaConvention.value = conv }
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "DRAWING MEASUREMENT UNITS",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(UnitSystem.values()) { u ->
            val isSelected = u == unitSystem
            SelectableCard(
                title = u.label,
                subtitle = "Architectural CAD units in ${u.shortLabel}",
                isSelected = isSelected,
                onClick = { viewModel.wizardUnitSystem.value = u }
            )
        }
    }
}

// STEP 3: Measurements & Setbacks
@Composable
private fun WizardStep3Measurements(viewModel: CadViewModel) {
    val fWidth by viewModel.wizardFrontWidth.collectAsState()
    val bWidth by viewModel.wizardBackWidth.collectAsState()
    val lLen by viewModel.wizardLeftLength.collectAsState()
    val rLen by viewModel.wizardRightLength.collectAsState()
    val diag by viewModel.wizardDiagonal.collectAsState()
    val roadFacing by viewModel.wizardRoadFacing.collectAsState()
    val fSet by viewModel.wizardFrontSetback.collectAsState()
    val rSet by viewModel.wizardRearSetback.collectAsState()
    val shape by viewModel.wizardPlotShape.collectAsState()
    val conv by viewModel.wizardMarlaConvention.collectAsState()

    val fw = fWidth.toFloatOrNull() ?: 30f
    val bw = bWidth.toFloatOrNull() ?: fw
    val ll = lLen.toFloatOrNull() ?: 60f
    val rl = rLen.toFloatOrNull() ?: ll
    val dg = diag.toFloatOrNull()

    val validation = GeometryEngine.validateQuadrilateral(fw, bw, ll, rl, dg)
    val areaReport = UnitConverter.calculateAreaReport(validation.calculatedAreaSqFt, conv)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Live Area Calculation & Survey Validation Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (validation.isValid) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, if (validation.isValid) Color(0xFF10B981) else Color(0xFFEF4444))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (validation.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (validation.isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (validation.isValid) "Survey Validated: ${areaReport.formattedSummary}" else "Invalid Geometry",
                            color = Color(0xFFF8FAFC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = validation.message,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Text(
                "PLOT BOUNDARY DIMENSIONS (FEET)",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WizardTextField(
                    value = fWidth,
                    onValueChange = {
                        viewModel.wizardFrontWidth.value = it
                        if (shape == PlotShapeType.RECTANGLE || shape == PlotShapeType.SQUARE) {
                            viewModel.wizardBackWidth.value = it
                        }
                    },
                    label = "Front Width (ft)",
                    modifier = Modifier.weight(1f)
                )
                WizardTextField(
                    value = bWidth,
                    onValueChange = { viewModel.wizardBackWidth.value = it },
                    label = "Back Width (ft)",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WizardTextField(
                    value = lLen,
                    onValueChange = {
                        viewModel.wizardLeftLength.value = it
                        if (shape == PlotShapeType.RECTANGLE || shape == PlotShapeType.SQUARE) {
                            viewModel.wizardRightLength.value = it
                        }
                    },
                    label = "Left Length (ft)",
                    modifier = Modifier.weight(1f)
                )
                WizardTextField(
                    value = rLen,
                    onValueChange = { viewModel.wizardRightLength.value = it },
                    label = "Right Length (ft)",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (shape == PlotShapeType.IRREGULAR_4_SIDED || shape == PlotShapeType.TRAPEZOID) {
            item {
                WizardTextField(
                    value = diag,
                    onValueChange = { viewModel.wizardDiagonal.value = it },
                    label = "Diagonal Survey Length (ft)",
                    placeholder = "Uniquely constrains quadrilateral area"
                )
            }
        }

        item {
            Text(
                "BYE-LAWS SETBACKS (FEET)",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WizardTextField(
                    value = fSet,
                    onValueChange = { viewModel.wizardFrontSetback.value = it },
                    label = "Front Setback (ft)",
                    modifier = Modifier.weight(1f)
                )
                WizardTextField(
                    value = rSet,
                    onValueChange = { viewModel.wizardRearSetback.value = it },
                    label = "Rear Setback (ft)",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            WizardTextField(
                value = roadFacing,
                onValueChange = { viewModel.wizardRoadFacing.value = it },
                label = "Road-Facing Orientation",
                placeholder = "e.g. Front (South), 40 ft Road"
            )
        }
    }
}

// STEP 4: Room Schedule
@Composable
private fun WizardStep4RoomSchedule(
    viewModel: CadViewModel,
    rooms: List<RoomRequirement>
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "ROOM REQUIREMENTS SCHEDULE",
                        color = Color(0xFF00B4D8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${rooms.size} Spaces configured for architectural placement",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showAddRoomDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A0F1D))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Room", color = Color(0xFF0A0F1D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(rooms) { room ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.name,
                            color = Color(0xFFF8FAFC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: ${room.targetWidthFt.toInt()}'-0\" × ${room.targetLengthFt.toInt()}'-0\" (${(room.targetWidthFt * room.targetLengthFt).toInt()} sq ft)",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        )
                        if (room.requiresAttachedBath) {
                            Text(
                                text = "• Includes attached bathroom",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.removeWizardRoom(room.id) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove")
                    }
                }
            }
        }
    }

    if (showAddRoomDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("Add Room to Schedule", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RoomType.values()) { type ->
                        Surface(
                            onClick = {
                                viewModel.addWizardRoom(type)
                                showAddRoomDialog = false
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF131B2E),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = type.displayName,
                                color = Color(0xFFF8FAFC),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

// STEP 5: Layout Alternatives
@Composable
private fun WizardStep5Layouts(
    viewModel: CadViewModel,
    alternatives: List<LayoutCandidate>,
    selectedIndex: Int
) {
    val archStyle by viewModel.wizardArchStyle.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "PREFERRED ARCHITECTURAL STYLE",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArchitecturalStyle.values().take(4).forEach { style ->
                    val isSelected = style == archStyle
                    SelectableCard(
                        title = style.title,
                        subtitle = style.description,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.wizardArchStyle.value = style
                            viewModel.generateWizardAlternatives()
                        }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "GENERATED LAYOUT CANDIDATES",
                    color = Color(0xFF00B4D8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.generateWizardAlternatives() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                    Spacer(Modifier.width(4.dp))
                    Text("Regenerate", fontSize = 11.sp, color = Color(0xFFF8FAFC))
                }
            }
        }

        items(alternatives.indices.toList()) { index ->
            val alt = alternatives[index]
            val isSelected = index == selectedIndex

            Surface(
                onClick = { viewModel.selectWizardAlternative(index) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF131B2E),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) Color(0xFF00B4D8) else Color(0xFF334155)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = alt.title,
                                color = Color(0xFFF8FAFC),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = alt.conceptSummary,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${alt.score}/100",
                                color = Color(0xFF10B981),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AltStatChip("Covered", "${alt.coveredAreaSqFt.toInt()} sq ft", Modifier.weight(1f))
                        AltStatChip("Open Space", "${alt.openAreaSqFt.toInt()} sq ft", Modifier.weight(1f))
                        AltStatChip("Efficiency", "${alt.efficiencyPercent.toInt()}%", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        alt.satisfiedConstraints.take(2).forEach { sc ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Text(sc, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AltStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF0F172A),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color(0xFF64748B), fontSize = 10.sp)
            Text(value, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SelectableCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.12f) else Color(0xFF131B2E),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) Color(0xFF00B4D8) else Color(0xFF334155)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isSelected) Color(0xFF00B4D8) else Color(0xFFF8FAFC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    maxLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color(0xFF64748B)) },
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFFF8FAFC),
            unfocusedTextColor = Color(0xFFF8FAFC),
            focusedBorderColor = Color(0xFF00B4D8),
            unfocusedBorderColor = Color(0xFF475569)
        ),
        modifier = modifier
    )
}
