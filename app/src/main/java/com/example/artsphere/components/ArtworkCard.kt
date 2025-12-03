package com.example.artsphere.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.artsphere.R

@Composable
fun ArtworkCard(
    index: Int, height: Int,
    navController: NavController,
//    artwork: Artwork
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clickable {
                navController.navigate("artwork/$index")
            },
        shape = RoundedCornerShape(16.dp),
    ) {
//        AsyncImage(
//            model = artwork.imageUrl,
//            contentDescription = artwork.name,
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
//            contentScale = ContentScale.Crop
//        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Artwork ${index + 1}", color = Color.Gray)
        }
    }
}

@Composable
fun DetailScreen(navController: NavController, index: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Sky Hopinka", fontWeight = FontWeight.Bold)
                Text(
                    text = "@Sky Hopinka",
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Share action */ }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Share")
            }
        }

        Image(
            painter = painterResource(R.drawable.dickson),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dickson Mounds",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "2022",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = {  }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Like",
                        )
                    }
                    IconButton(onClick = {  }) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Save"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Price: $250", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Medium: inkjet print with hand-etched text", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Dimensions:", style = MaterialTheme.typography.bodyMedium)
            Text(text = "image: 101.6 × 101.6 cm (40 × 40 in.)", style = MaterialTheme.typography.bodyMedium)
            Text(text = "framed: 105.41 × 105.41 × 4.76 cm (41 1/2 × 41 1/2 × 1 7/8 in.)", style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
