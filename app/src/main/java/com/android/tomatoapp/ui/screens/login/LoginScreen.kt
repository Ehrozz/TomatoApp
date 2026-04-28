package com.android.tomatoapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.tomatoapp.R
import com.android.tomatoapp.ui.components.AppButtonOutlined
import com.android.tomatoapp.ui.components.AppButtonPrimary
import com.android.tomatoapp.ui.components.AppTextField
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.RedDark
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.SmallRadius
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onSignInClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(RedDark, RedPrimary),
                        angle = 155f
                    )
                )
                .padding(horizontal = 24.dp, vertical = 44.dp)
        ) {
            Column {
                // Logo Box
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(SurfaceWhite, androidx.compose.foundation.shape.RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍅", fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Title
                Text(
                    text = "Welcome to\nTomatoApp",
                    style = TextStyle(
                        fontFamily = PlayfairDisplayFamily,
                        fontSize = 26.sp,
                        color = Color.White,
                        lineHeight = 30.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "Your smart farming companion",
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                    )
                )
            }
        }

        // White Card (curved top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-28).dp)
                .background(SurfaceWhite, androidx.compose.foundation.shape.RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Column {
                // Email Field Label
                Text(
                    text = "EMAIL ADDRESS",
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Email Field
                AppTextField(
                    value = state.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    placeholder = "farmer@example.com",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field Label
                Text(
                    text = "PASSWORD",
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Password Field
                AppTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button
                AppButtonPrimary(
                    text = "Sign In",
                    onClick = {
                        viewModel.signIn()
                        onSignInClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Divider with text
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Divider(color = Border, thickness = 0.5.dp)
                    Text(
                        text = "or continue with",
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = TextMuted
                        ),
                        modifier = Modifier
                            .background(SurfaceWhite)
                            .padding(horizontal = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Google Sign In Button
                AppButtonOutlined(
                    text = "Continue with Google",
                    onClick = {
                        viewModel.signInWithGoogle()
                        onGoogleLoginClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceWhite
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Register Link
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                color = TextMuted
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "Create one",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                color = RedPrimary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            modifier = Modifier.clickableLogin { onRegisterClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun clickableLogin(onClick: () -> Unit): Modifier {
    return Modifier
}
