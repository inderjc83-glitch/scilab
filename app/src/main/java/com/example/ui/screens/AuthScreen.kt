package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SciLabRepository
import com.example.model.UserRole
import com.example.ui.components.HowToUseDialog
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    // Sign In Fields
    var signInEmail by remember { mutableStateOf("alex.rivera@science.edu") }
    var signInPassword by remember { mutableStateOf("••••••••") }
    var signInPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Sign Up Fields
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpInstitution by remember { mutableStateOf("Department of Natural Sciences") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpConfirmPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var agreeToGuidelines by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successToast by remember { mutableStateOf<String?>(null) }
    var showHowToUseDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showAdminDataSheet by remember { mutableStateOf(false) }

    if (showAdminDataSheet) {
        AdminDataSheetScreen(onBack = { showAdminDataSheet = false })
        return
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Top Brand Emblem & Title
            Surface(
                color = SciCyan.copy(alpha = 0.15f),
                shape = CircleShape,
                border = BorderStroke(2.dp, SciCyan),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = "SciLab Logo",
                        tint = SciCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SciLab",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (authMode == AuthMode.SIGN_IN)
                        "Sign in to continue your research & labs"
                    else
                        "Create your academic account to get started",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick 1-Tap Demo Shortcuts (for effortless instant login in 1 click)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = SciCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Instant 1-Tap Demo Access:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                SciLabRepository.login(
                                    name = "Alex Rivera",
                                    email = "alex.rivera@science.edu",
                                    role = UserRole.STUDENT,
                                    institution = "Department of Natural Sciences"
                                )
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SciCyan.copy(alpha = 0.18f), contentColor = SciCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("quick_login_student")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Student", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                SciLabRepository.login(
                                    name = "Dr. Marcus Vance",
                                    email = "marcus.vance@faculty.edu",
                                    role = UserRole.TEACHER,
                                    institution = "Faculty of Applied Sciences"
                                )
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SciAmber.copy(alpha = 0.18f), contentColor = SciAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("quick_login_teacher")
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Teacher", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Navigation Tabs: [ Sign In ] vs [ Create Account (Sign Up) ]
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    // Sign In Tab
                    Surface(
                        color = if (authMode == AuthMode.SIGN_IN) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                authMode = AuthMode.SIGN_IN
                                errorMessage = null
                            }
                            .testTag("tab_sign_in")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = if (authMode == AuthMode.SIGN_IN) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (authMode == AuthMode.SIGN_IN)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Sign Up Tab
                    Surface(
                        color = if (authMode == AuthMode.SIGN_UP) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                authMode = AuthMode.SIGN_UP
                                errorMessage = null
                            }
                            .testTag("tab_sign_up")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Sign Up",
                                fontWeight = if (authMode == AuthMode.SIGN_UP) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (authMode == AuthMode.SIGN_UP)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Success banner if any
            if (successToast != null) {
                Surface(
                    color = SciEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SciEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successToast ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // =========================================================================
            // TAB 1: SIGN IN FORM
            // =========================================================================
            if (authMode == AuthMode.SIGN_IN) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Role Selector for Sign In
                    Text("Select Your Account Role:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isStudent = selectedRole == UserRole.STUDENT
                        Surface(
                            color = if (isStudent) SciCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(if (isStudent) 2.dp else 1.dp, if (isStudent) SciCyan else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRole = UserRole.STUDENT
                                    signInEmail = "alex.rivera@science.edu"
                                }
                                .testTag("signin_role_student")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Student", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Record labs", fontSize = 10.sp, color = SciCyan)
                                }
                            }
                        }

                        val isTeacher = selectedRole == UserRole.TEACHER
                        Surface(
                            color = if (isTeacher) SciAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(if (isTeacher) 2.dp else 1.dp, if (isTeacher) SciAmber else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRole = UserRole.TEACHER
                                    signInEmail = "marcus.vance@faculty.edu"
                                }
                                .testTag("signin_role_teacher")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SciAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Teacher", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Grade & Review", fontSize = 10.sp, color = SciAmber)
                                }
                            }
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = signInEmail,
                        onValueChange = {
                            signInEmail = it
                            errorMessage = null
                        },
                        label = { Text("Academic Email") },
                        placeholder = { Text("your.name@science.edu") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signin_email_field")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = signInPassword,
                        onValueChange = {
                            signInPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber)
                        },
                        trailingIcon = {
                            IconButton(onClick = { signInPasswordVisible = !signInPasswordVisible }) {
                                Icon(
                                    if (signInPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (signInPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signin_password_field")
                    )

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remember me", fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = {
                                successToast = "Password reset instructions sent to $signInEmail"
                            }
                        ) {
                            Text("Forgot password?", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Sign In Submit Button
                    Button(
                        onClick = {
                            if (signInEmail.isBlank()) {
                                errorMessage = "Please enter your academic email"
                                return@Button
                            }
                            val displayName = if (selectedRole == UserRole.STUDENT) "Alex Rivera" else "Dr. Marcus Vance"
                            SciLabRepository.login(
                                name = displayName,
                                email = signInEmail.trim(),
                                role = selectedRole
                            )
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber,
                            contentColor = if (selectedRole == UserRole.STUDENT) Color(0xFF0A192F) else Color(0xFF332B00)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signin_submit_button")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign In as ${if (selectedRole == UserRole.STUDENT) "Student" else "Teacher"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Switch to Sign Up
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {
                            authMode = AuthMode.SIGN_UP
                            errorMessage = null
                        }) {
                            Text("Create Account", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2: SIGN UP FORM
            // =========================================================================
            if (authMode == AuthMode.SIGN_UP) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Select Account Type & Access Level:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    // Student Account Card
                    val isStudent = selectedRole == UserRole.STUDENT
                    Surface(
                        color = if (isStudent) SciCyan.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(if (isStudent) 2.dp else 1.dp, if (isStudent) SciCyan else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = UserRole.STUDENT }
                            .testTag("signup_role_student")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            RadioButton(
                                selected = isStudent,
                                onClick = { selectedRole = UserRole.STUDENT },
                                colors = RadioButtonDefaults.colors(selectedColor = SciCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Student Account (Learner Access)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Record live experiments, inspect telemetry, complete milestones, chat with peers & take quizzes.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Teacher Account Card
                    val isTeacher = selectedRole == UserRole.TEACHER
                    Surface(
                        color = if (isTeacher) SciAmber.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(if (isTeacher) 2.dp else 1.dp, if (isTeacher) SciAmber else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = UserRole.TEACHER }
                            .testTag("signup_role_teacher")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            RadioButton(
                                selected = isTeacher,
                                onClick = { selectedRole = UserRole.TEACHER },
                                colors = RadioButtonDefaults.colors(selectedColor = SciAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Teacher / Faculty (Elevated Access)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Official grading, cohort oversight, rubrics & endorsement of AI telemetry analytics.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Full Name Field
                    OutlinedTextField(
                        value = signUpName,
                        onValueChange = {
                            signUpName = it
                            errorMessage = null
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text(if (selectedRole == UserRole.STUDENT) "e.g. Alex Rivera" else "e.g. Dr. Marcus Vance") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name_field")
                    )

                    // Email Field
                    OutlinedTextField(
                        value = signUpEmail,
                        onValueChange = {
                            signUpEmail = it
                            errorMessage = null
                        },
                        label = { Text("Academic Email") },
                        placeholder = { Text(if (selectedRole == UserRole.STUDENT) "alex@student.edu" else "marcus@faculty.edu") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_email_field")
                    )

                    // Institution Field
                    OutlinedTextField(
                        value = signUpInstitution,
                        onValueChange = {
                            signUpInstitution = it
                            errorMessage = null
                        },
                        label = { Text("School / Institution") },
                        placeholder = { Text("Department of Natural Sciences") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_institution_field")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = {
                            signUpPassword = it
                            errorMessage = null
                        },
                        label = { Text("Create Password") },
                        placeholder = { Text("At least 6 characters") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber) },
                        trailingIcon = {
                            IconButton(onClick = { signUpPasswordVisible = !signUpPasswordVisible }) {
                                Icon(
                                    if (signUpPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_password_field")
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = signUpConfirmPassword,
                        onValueChange = {
                            signUpConfirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Repeat your password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber) },
                        visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_confirm_password_field")
                    )

                    // Agreement checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreeToGuidelines = !agreeToGuidelines },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreeToGuidelines,
                            onCheckedChange = { agreeToGuidelines = it },
                            colors = CheckboxDefaults.colors(checkedColor = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "I agree to lab safety rules and academic data ethics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Sign Up Submit Button
                    Button(
                        onClick = {
                            val finalName = if (signUpName.isNotBlank()) signUpName.trim() else (if (selectedRole == UserRole.STUDENT) "Alex Rivera" else "Dr. Marcus Vance")
                            val finalEmail = if (signUpEmail.isNotBlank()) signUpEmail.trim() else (if (selectedRole == UserRole.STUDENT) "alex.rivera@student.edu" else "marcus.vance@faculty.edu")

                            if (signUpPassword.isNotEmpty() && signUpConfirmPassword.isNotEmpty() && signUpPassword != signUpConfirmPassword) {
                                errorMessage = "Passwords do not match. Please re-enter."
                                return@Button
                            }
                            if (!agreeToGuidelines) {
                                errorMessage = "Please accept the academic data & safety guidelines"
                                return@Button
                            }

                            SciLabRepository.signUp(
                                name = finalName,
                                email = finalEmail,
                                role = selectedRole,
                                institution = if (signUpInstitution.isNotBlank()) signUpInstitution.trim() else "Department of Natural Sciences"
                            )
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == UserRole.STUDENT) SciCyan else SciAmber,
                            contentColor = if (selectedRole == UserRole.STUDENT) Color(0xFF0A192F) else Color(0xFF332B00)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_submit_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create ${if (selectedRole == UserRole.STUDENT) "Student" else "Teacher"} Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Switch to Sign In
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {
                            authMode = AuthMode.SIGN_IN
                            errorMessage = null
                        }) {
                            Text("Sign In", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                        }
                    }
                }
            }

            // Bottom Help & Storage Info Button
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHowToUseDialog = true }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = SciCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "How SciLab Works & Where Data is Saved",
                    fontSize = 12.sp,
                    color = SciCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Discrete Owner Master PIN Access to Background Data Sheet
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdminPinDialog = true }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Owner Access: Background Auth Data Sheet",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showHowToUseDialog) {
        HowToUseDialog(onDismiss = { showHowToUseDialog = false })
    }

    if (showAdminPinDialog) {
        com.example.ui.components.AdminPinDialog(
            onPinCorrect = {
                showAdminPinDialog = false
                showAdminDataSheet = true
            },
            onDismiss = { showAdminPinDialog = false }
        )
    }
}
