package com.aquaflow.waterreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AquaBlue = Color(0xFF087EBC)
private val DeepBlue = Color(0xFF075985)
private val PaleBlue = Color(0xFFE7F6FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AquaFlowTheme { AquaFlowHome() } }
    }
}

@Composable
private fun AquaFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AquaBlue,
            secondary = Color(0xFF43BFEA),
            background = Color(0xFFF5FBFF),
            surface = Color.White
        ),
        content = content
    )
}

@Composable
private fun AquaFlowHome() {
    var consumed by remember { mutableIntStateOf(0) }
    val goal = 2000
    val progress = (consumed.toFloat() / goal).coerceIn(0f, 1f)

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5FBFF)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("AquaFlow", color = DeepBlue, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("Every sip matters", color = Color(0xFF668397), fontSize = 14.sp)
                }
                Text("💧", fontSize = 30.sp)
            }
            Spacer(Modifier.height(34.dp))
            Text("Today's hydration", color = Color(0xFF466477), fontSize = 17.sp)
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFDDF5FF), Color(0xFFB8E9FA))),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💧", fontSize = 72.sp)
                    Text("$consumed ml", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Text("of $goal ml daily goal", color = Color(0xFF466477))
                    Spacer(Modifier.height(12.dp))
                    Text("${(progress * 100).toInt()}% completed", color = DeepBlue, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(26.dp))
            Text("Add a drink", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = DeepBlue)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(150, 200, 250, 500).forEach { amount ->
                    Button(
                        onClick = { consumed += amount },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AquaBlue)
                    ) { Text("+${amount}", fontSize = 12.sp) }
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("Small sips make a big difference.", color = Color(0xFF668397), fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text("Your daily hydration companion", color = Color(0xFF8AA2B1), fontSize = 12.sp)
        }
    }
}
