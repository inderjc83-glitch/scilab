package com.example.data

import com.example.crypto.CryptoManager
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object SciLabRepository {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // App Preferences
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isOfflineFieldMode = MutableStateFlow(false)
    val isOfflineFieldMode: StateFlow<Boolean> = _isOfflineFieldMode.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Current User Profile
    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "usr_99182",
            name = "Alex Rivera",
            email = "alex.rivera@science.edu",
            role = UserRole.STUDENT,
            isMfaEnabled = true,
            isSubscribed = true,
            trialDaysRemaining = 0,
            subscriptionDaysRemaining = 2,
            expirationEmailSent = true,
            activePlan = "1 Month Access",
            experimentsCompleted = 5,
            quizScoreMastery = 88
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun login(name: String, email: String, role: UserRole) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            email = email,
            role = role
        )
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    // Experiments
    private val _experiments = MutableStateFlow<List<ScienceExperiment>>(emptyList())
    val experiments: StateFlow<List<ScienceExperiment>> = _experiments.asStateFlow()

    // Selected Experiment
    private val _selectedExperimentId = MutableStateFlow<String?>("exp_physics_01")
    val selectedExperimentId: StateFlow<String?> = _selectedExperimentId.asStateFlow()

    // Data Points (by experimentId)
    private val _dataPoints = MutableStateFlow<Map<String, List<ExperimentDataPoint>>>(emptyMap())
    val dataPoints: StateFlow<Map<String, List<ExperimentDataPoint>>> = _dataPoints.asStateFlow()

    // Chat Messages (by experimentId)
    private val _chatMessages = MutableStateFlow<Map<String, List<GroupChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<String, List<GroupChatMessage>>> = _chatMessages.asStateFlow()

    // Project Tasks (by experimentId)
    private val _projectTasks = MutableStateFlow<Map<String, List<ProjectTask>>>(emptyMap())
    val projectTasks: StateFlow<Map<String, List<ProjectTask>>> = _projectTasks.asStateFlow()

    // Quizzes
    private val _quizzes = MutableStateFlow<List<ScienceQuizQuestion>>(emptyList())
    val quizzes: StateFlow<List<ScienceQuizQuestion>> = _quizzes.asStateFlow()

    // Payment Transactions (Admin Log)
    private val _transactions = MutableStateFlow<List<PaymentTransaction>>(emptyList())
    val transactions: StateFlow<List<PaymentTransaction>> = _transactions.asStateFlow()

    // Subscription Expiration Alert Emails (Automated dispatch 2 days prior)
    private val _expirationEmails = MutableStateFlow<List<SubscriptionAlertEmail>>(emptyList())
    val expirationEmails: StateFlow<List<SubscriptionAlertEmail>> = _expirationEmails.asStateFlow()

    // Live Sensor Streaming Job
    private var streamingJob: Job? = null
    private val _isLiveStreaming = MutableStateFlow(false)
    val isLiveStreaming: StateFlow<Boolean> = _isLiveStreaming.asStateFlow()

    init {
        initializeSeedData()
    }

    private fun initializeSeedData() {
        val seedExperiments = listOf(
            ScienceExperiment(
                id = "exp_physics_01",
                title = "Gravitational Acceleration & Pendulum Period",
                category = ScienceCategory.PHYSICS,
                hypothesis = "The oscillation period T squared is strictly proportional to string length L, yielding g ≈ 9.81 m/s².",
                status = ExperimentStatus.ACTIVE,
                isEncrypted = true,
                encryptionChecksum = "SHA256:4a89fb13cc2817d2",
                collaborators = listOf("Alex Rivera", "Elena Rostova", "Dr. Marcus Vance"),
                teacherGrade = "96/100 (A)",
                teacherNotes = "Exceptional data fidelity. Harmonic oscillator resonance was captured accurately.",
                labNotes = "Trial 1-5 used 0.2m to 1.0m pendulums with high-precision optical photogate sensors. Minor aerodynamic dampening detected."
            ),
            ScienceExperiment(
                id = "exp_chem_02",
                title = "Enzyme Catalase Kinetics: H2O2 Decomposition",
                category = ScienceCategory.CHEMISTRY,
                hypothesis = "Reaction velocity v follows Michaelis-Menten kinetics with substrate saturation at [S] > 50mM.",
                status = ExperimentStatus.ACTIVE,
                isEncrypted = true,
                encryptionChecksum = "SHA256:8891d4ffbc02099b",
                collaborators = listOf("Alex Rivera", "Sophia Chen", "Liam Patel"),
                teacherGrade = null,
                teacherNotes = null,
                labNotes = "Catalase enzyme buffered at pH 7.4. Pressure sensor tracked O2 gas evolution at 1-second intervals."
            ),
            ScienceExperiment(
                id = "exp_bio_03",
                title = "Aquatic Plant Photosynthetic Oxygen Generation",
                category = ScienceCategory.BIOLOGY,
                hypothesis = "Dissolved Oxygen (DO) production increases with photon flux density up to light saturation point.",
                status = ExperimentStatus.IN_REVIEW,
                isEncrypted = true,
                encryptionChecksum = "SHA256:09c124a87ef5a021",
                collaborators = listOf("Alex Rivera", "Maya Lin"),
                teacherGrade = "88/100 (B+)",
                teacherNotes = "Good baseline analysis. Recommend repeating with calibrated carbon dioxide buffer.",
                labNotes = "Elodea densa submerged in 1% sodium bicarbonate solution under spectrum LED illumination."
            ),
            ScienceExperiment(
                id = "exp_env_04",
                title = "Urban Heat Island & Microclimate Telemetry",
                category = ScienceCategory.ENVIRONMENTAL,
                hypothesis = "Canopy-covered urban corridors exhibit 4.2°C lower ambient surface temperature versus asphalt surfaces.",
                status = ExperimentStatus.COMPLETED,
                isEncrypted = true,
                encryptionChecksum = "SHA256:2fe901c1839211aa",
                collaborators = listOf("Alex Rivera", "Sam Wilson", "Prof. Higgins"),
                teacherGrade = "98/100 (A+)",
                teacherNotes = "Outstanding field research methodology and geospatial telemetry.",
                labNotes = "Remote field study across 4 city transects using calibrated infrared pyrometers."
            )
        )
        _experiments.value = seedExperiments

        // Seed Data points for Physics
        val physicsPoints = mutableListOf<ExperimentDataPoint>()
        val initialLengths = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f, 1.6f)
        initialLengths.forEachIndexed { idx, length ->
            val period = (2f * Math.PI.toFloat() * kotlin.math.sqrt(length / 9.81f)) + (Random.nextFloat() * 0.04f - 0.02f)
            physicsPoints.add(
                ExperimentDataPoint(
                    id = 1000L + idx,
                    experimentId = "exp_physics_01",
                    timeOffsetSec = (idx * 10).toFloat(),
                    paramName = "Oscillation Period (T)",
                    value = period,
                    unit = "seconds",
                    notes = "Length = ${length}m (Trial #${idx + 1})"
                )
            )
        }

        // Seed Data points for Chemistry
        val chemPoints = mutableListOf<ExperimentDataPoint>()
        var currentPressure = 101.3f
        for (i in 0..10) {
            currentPressure += (12f / (i + 1f)) + (Random.nextFloat() * 0.8f)
            chemPoints.add(
                ExperimentDataPoint(
                    id = 2000L + i,
                    experimentId = "exp_chem_02",
                    timeOffsetSec = (i * 5).toFloat(),
                    paramName = "O2 Gas Pressure",
                    value = currentPressure,
                    unit = "kPa",
                    notes = "Reaction progress t=${i * 5}s"
                )
            )
        }

        _dataPoints.value = mapOf(
            "exp_physics_01" to physicsPoints,
            "exp_chem_02" to chemPoints
        )

        // Seed Chat
        _chatMessages.value = mapOf(
            "exp_physics_01" to listOf(
                GroupChatMessage(
                    id = "msg_1",
                    experimentId = "exp_physics_01",
                    senderName = "Dr. Marcus Vance",
                    senderRole = UserRole.TEACHER,
                    messageText = "Great setup! Make sure to align the optical photogate exactly with the pendulum equilibrium point.",
                    timestamp = System.currentTimeMillis() - 7200000L
                ),
                GroupChatMessage(
                    id = "msg_2",
                    experimentId = "exp_physics_01",
                    senderName = "Alex Rivera",
                    senderRole = UserRole.STUDENT,
                    messageText = "Done! We just calibrated the optical laser sensor and gathered 8 length trials. R² is looking like 0.992.",
                    timestamp = System.currentTimeMillis() - 3600000L
                ),
                GroupChatMessage(
                    id = "msg_3",
                    experimentId = "exp_physics_01",
                    senderName = "Elena Rostova",
                    senderRole = UserRole.STUDENT,
                    messageText = "I also recorded the video telemetry for angular displacement verification under 10 degrees small angle approx.",
                    timestamp = System.currentTimeMillis() - 1800000L
                )
            )
        )

        // Seed Tasks
        _projectTasks.value = mapOf(
            "exp_physics_01" to listOf(
                ProjectTask(
                    id = "task_1",
                    experimentId = "exp_physics_01",
                    title = "Photogate Laser Alignment & Sensor Zeroing",
                    assignee = "Alex Rivera",
                    status = TaskStatus.VERIFIED,
                    dueDate = "Oct 10"
                ),
                ProjectTask(
                    id = "task_2",
                    experimentId = "exp_physics_01",
                    title = "Collect T vs L Harmonic Oscillations (10 trials)",
                    assignee = "Elena Rostova",
                    status = TaskStatus.IN_PROGRESS,
                    dueDate = "Oct 14"
                ),
                ProjectTask(
                    id = "task_3",
                    experimentId = "exp_physics_01",
                    title = "Compile Automated Statistical Error Propagation",
                    assignee = "Alex Rivera",
                    status = TaskStatus.TODO,
                    dueDate = "Oct 16"
                ),
                ProjectTask(
                    id = "task_4",
                    experimentId = "exp_physics_01",
                    title = "Teacher Rubric Submission & Peer Review",
                    assignee = "All Team Members",
                    status = TaskStatus.TODO,
                    dueDate = "Oct 18"
                )
            )
        )

        // Seed Science Quizzes
        _quizzes.value = listOf(
            ScienceQuizQuestion(
                id = 1,
                category = ScienceCategory.PHYSICS,
                question = "In simple pendulum motion under small angles, what happens to oscillation period T if length L is quadrupled (4x)?",
                options = listOf(
                    "Period remains unchanged",
                    "Period doubles (2x)",
                    "Period quadruples (4x)",
                    "Period halves (1/2x)"
                ),
                correctIndex = 1,
                scientificExplanation = "Because T = 2π√(L/g), period T scales with the square root of string length. √4 = 2, so the period doubles."
            ),
            ScienceQuizQuestion(
                id = 2,
                category = ScienceCategory.CHEMISTRY,
                question = "What is the primary effect of an enzyme catalyst on an exothermic biochemical reaction?",
                options = listOf(
                    "Lowers the activation energy (Ea) without altering ΔG",
                    "Increases overall Gibbs free energy change (ΔG)",
                    "Raises system temperature continuously",
                    "Shifts chemical equilibrium to 100% products"
                ),
                correctIndex = 0,
                scientificExplanation = "Catalysts lower the activation energy barrier for both forward and reverse reactions, increasing rate velocity without changing ΔG or equilibrium."
            ),
            ScienceQuizQuestion(
                id = 3,
                category = ScienceCategory.BIOLOGY,
                question = "During the light reactions of photosynthesis, what process directly releases O2 gas into the atmosphere?",
                options = listOf(
                    "Calvin cycle CO2 fixation",
                    "Photolysis of water molecules at Photosystem II",
                    "Glycolysis in cellular cytoplasm",
                    "Oxidative phosphorylation in mitochondria"
                ),
                correctIndex = 1,
                scientificExplanation = "Photolysis splits H2O molecules at the oxygen-evolving complex of Photosystem II (2H2O -> 4H+ + 4e- + O2)."
            ),
            ScienceQuizQuestion(
                id = 4,
                category = ScienceCategory.ENVIRONMENTAL,
                question = "Which ecological phenomenon explains why urban centers experience significantly higher ambient heat than rural outskirts?",
                options = listOf(
                    "Coriolis acceleration deflection",
                    "Urban Heat Island effect due to low albedo impervious surfaces",
                    "Atmospheric ozone depletion over metropolitan zones",
                    "Geothermal magma convection"
                ),
                correctIndex = 1,
                scientificExplanation = "Dark pavements and asphalt absorb solar radiation (low albedo) while lack of vegetative evapotranspiration prevents natural evaporative cooling."
            )
        )

        // Initial Seed Transaction
        _transactions.value = listOf(
            PaymentTransaction(
                transactionId = "TXN_7741092_STRIPE",
                userId = "usr_99182",
                userEmail = "alex.rivera@science.edu",
                planName = "1 Month Premium - $1",
                amount = 1.00,
                paymentGateway = "Stripe",
                merchantPaypal = "varinderj83@gmail.com",
                timestamp = System.currentTimeMillis() - 259200000L,
                status = "COMPLETED"
            )
        )

        // Seed 2-Days Prior Subscription Expiration Notice Email
        _expirationEmails.value = listOf(
            SubscriptionAlertEmail(
                emailId = "ALERT_EXP_2DAYS_001",
                recipientEmail = "alex.rivera@student.edu",
                subject = "⚠️ SciLab Alert: Subscription Completes in 2 Days",
                daysRemaining = 2,
                body = "Dear Alex,\n\nYour SciLab subscription will expire in 2 days. To prevent disruption to your real-time telemetry graphs, collaborative lab notes, and team projects, renew now for $1/month or $9/year.\n\nAutomated notification from SciLab Security.",
                dispatchedAt = System.currentTimeMillis() - 3600000L,
                isDelivered = true
            )
        )
    }

    // Dispatch automated email notice 2 days prior to expiration
    fun sendSubscriptionExpirationEmail(daysRemaining: Int = 2) {
        val user = _userProfile.value
        val alert = SubscriptionAlertEmail(
            emailId = "ALERT_EXP_${System.currentTimeMillis()}",
            recipientEmail = user.email,
            subject = "⚠️ SciLab Alert: Subscription Completes in $daysRemaining Days",
            daysRemaining = daysRemaining,
            body = "Dear ${user.name},\n\nYour current subscription (${user.activePlan ?: "Premium Tier"}) will expire in $daysRemaining days. Renew early to retain access to cloud-synced science experiments, quiz games, and team workspaces.\n\nThank you for exploring science with SciLab!",
            dispatchedAt = System.currentTimeMillis(),
            isDelivered = true
        )
        _expirationEmails.value = listOf(alert) + _expirationEmails.value
        _userProfile.value = user.copy(
            subscriptionDaysRemaining = daysRemaining,
            expirationEmailSent = true,
            expirationEmailTimestamp = System.currentTimeMillis()
        )
    }

    // Set subscription days left (e.g. testing 2 days trigger)
    fun setSubscriptionDaysRemaining(days: Int) {
        val current = _userProfile.value
        if (days <= 2 && (!current.expirationEmailSent || current.subscriptionDaysRemaining > 2)) {
            sendSubscriptionExpirationEmail(days)
        } else {
            _userProfile.value = current.copy(subscriptionDaysRemaining = days)
        }
    }

    // Set App Language
    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    // Toggle Dark Mode
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Switch User Role
    fun switchUserRole(role: UserRole) {
        _userProfile.value = _userProfile.value.copy(role = role)
    }

    // Toggle MFA
    fun toggleMfa(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(isMfaEnabled = enabled)
    }

    // Select Experiment
    fun selectExperiment(id: String) {
        _selectedExperimentId.value = id
    }

    // Add New Experiment
    fun createExperiment(
        title: String,
        category: ScienceCategory,
        hypothesis: String,
        isFieldMode: Boolean
    ): String {
        val newId = "exp_${System.currentTimeMillis()}"
        val checksum = CryptoManager.calculateSha256Checksum("$title:$hypothesis:${System.currentTimeMillis()}")
        val newExp = ScienceExperiment(
            id = newId,
            title = title,
            category = category,
            hypothesis = hypothesis,
            status = ExperimentStatus.ACTIVE,
            isEncrypted = true,
            encryptionChecksum = checksum,
            collaborators = listOf(_userProfile.value.name, "Dr. Maya S."),
            createdAt = System.currentTimeMillis(),
            isSyncedToCloud = !isFieldMode,
            offlineFieldMode = isFieldMode
        )
        _experiments.value = listOf(newExp) + _experiments.value
        _selectedExperimentId.value = newId
        if (isFieldMode) {
            _pendingSyncCount.value += 1
        }
        return newId
    }

    // Add Data Point
    fun addDataPoint(
        experimentId: String,
        paramName: String,
        value: Float,
        unit: String,
        notes: String = ""
    ) {
        val existing = _dataPoints.value[experimentId] ?: emptyList()
        val nextTimeOffset = if (existing.isNotEmpty()) existing.last().timeOffsetSec + 5f else 0f
        val newPoint = ExperimentDataPoint(
            id = System.currentTimeMillis(),
            experimentId = experimentId,
            timeOffsetSec = nextTimeOffset,
            paramName = paramName,
            value = value,
            unit = unit,
            notes = notes
        )
        val updated = existing + newPoint
        _dataPoints.value = _dataPoints.value + (experimentId to updated)

        if (_isOfflineFieldMode.value) {
            _pendingSyncCount.value += 1
        }
    }

    // 1-Tap Quick Sample Generator
    fun quickAddSample(experimentId: String): ExperimentDataPoint {
        val existing = _dataPoints.value[experimentId] ?: emptyList()
        val last = existing.lastOrNull()
        val paramName = last?.paramName ?: "Primary Telemetry"
        val unit = last?.unit ?: "units"
        val lastVal = last?.value ?: 12.0f
        val delta = (Random.nextFloat() - 0.45f) * (if (lastVal > 50) 3.5f else 0.4f)
        val newVal = (lastVal + delta).coerceAtLeast(0.1f)
        val nextTimeOffset = if (existing.isNotEmpty()) existing.last().timeOffsetSec + 10f else 0f
        val newPoint = ExperimentDataPoint(
            id = System.currentTimeMillis(),
            experimentId = experimentId,
            timeOffsetSec = nextTimeOffset,
            paramName = paramName,
            value = newVal,
            unit = unit,
            notes = "Quick 1-tap telemetry reading @ t=${nextTimeOffset.toInt()}s"
        )
        val updated = existing + newPoint
        _dataPoints.value = _dataPoints.value + (experimentId to updated)
        if (_isOfflineFieldMode.value) {
            _pendingSyncCount.value += 1
        }
        return newPoint
    }

    // Clear data points for fresh experiment run
    fun clearDataPoints(experimentId: String) {
        _dataPoints.value = _dataPoints.value + (experimentId to emptyList())
    }

    // 1-Tap Quick Grade
    fun quickGrade(experimentId: String, grade: String) {
        val feedbackNotes = when {
            grade.contains("95") || grade.contains("A") -> "Exceptional empirical methodology, rigorous data logging, and well-supported scientific conclusion."
            grade.contains("85") || grade.contains("B") -> "Good scientific control and solid telemetry curve. Consider adding additional baseline trials."
            else -> "Initial data submitted. Please repeat calibration and re-evaluate experimental variance."
        }
        submitTeacherReview(experimentId, grade, feedbackNotes)
    }

    // Live Streaming Telemetry (1Hz auto simulator)
    fun toggleLiveStreaming(experimentId: String, paramName: String, unit: String) {
        if (_isLiveStreaming.value) {
            streamingJob?.cancel()
            _isLiveStreaming.value = false
        } else {
            _isLiveStreaming.value = true
            streamingJob = scope.launch {
                var baseline = when {
                    paramName.contains("Temp", true) -> 24.5f
                    paramName.contains("pH", true) -> 7.2f
                    paramName.contains("Press", true) -> 101.3f
                    paramName.contains("Volt", true) -> 3.3f
                    else -> 15.0f
                }
                while (isActive) {
                    delay(1500L)
                    val noise = (Random.nextFloat() - 0.48f) * 0.6f
                    baseline += noise
                    addDataPoint(
                        experimentId = experimentId,
                        paramName = paramName,
                        value = baseline,
                        unit = unit,
                        notes = "Auto-telemetry sampled @ ${System.currentTimeMillis() % 10000}ms"
                    )
                }
            }
        }
    }

    // Send Group Chat Message
    fun sendChatMessage(experimentId: String, text: String) {
        if (text.isBlank()) return
        val current = _chatMessages.value[experimentId] ?: emptyList()
        val user = _userProfile.value
        val msg = GroupChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            experimentId = experimentId,
            senderName = user.name,
            senderRole = user.role,
            messageText = text.trim(),
            timestamp = System.currentTimeMillis(),
            isEncrypted = true
        )
        _chatMessages.value = _chatMessages.value + (experimentId to (current + msg))
    }

    // Update Project Task Status
    fun updateTaskStatus(experimentId: String, taskId: String, nextStatus: TaskStatus) {
        val tasks = _projectTasks.value[experimentId] ?: return
        val updated = tasks.map {
            if (it.id == taskId) it.copy(status = nextStatus) else it
        }
        _projectTasks.value = _projectTasks.value + (experimentId to updated)
    }

    // Add Task
    fun addTask(experimentId: String, title: String, assignee: String, dueDate: String) {
        val tasks = _projectTasks.value[experimentId] ?: emptyList()
        val newTask = ProjectTask(
            id = "task_${System.currentTimeMillis()}",
            experimentId = experimentId,
            title = title,
            assignee = assignee,
            status = TaskStatus.TODO,
            dueDate = dueDate
        )
        _projectTasks.value = _projectTasks.value + (experimentId to (tasks + newTask))
    }

    // Update Lab Notes
    fun updateLabNotes(experimentId: String, newNotes: String) {
        val updatedList = _experiments.value.map { exp ->
            if (exp.id == experimentId) {
                exp.copy(
                    labNotes = newNotes,
                    encryptionChecksum = CryptoManager.calculateSha256Checksum(newNotes)
                )
            } else exp
        }
        _experiments.value = updatedList
    }

    // Teacher Grades and Automated Feedback
    fun submitTeacherReview(experimentId: String, grade: String, feedbackNotes: String) {
        val updated = _experiments.value.map { exp ->
            if (exp.id == experimentId) {
                exp.copy(
                    teacherGrade = grade,
                    teacherNotes = feedbackNotes,
                    status = ExperimentStatus.COMPLETED
                )
            } else exp
        }
        _experiments.value = updated
    }

    fun applyAutomatedFeedback(experimentId: String, feedbackSummary: String) {
        val updated = _experiments.value.map { exp ->
            if (exp.id == experimentId) {
                exp.copy(automatedFeedback = feedbackSummary)
            } else exp
        }
        _experiments.value = updated
    }

    // Toggle Offline Field Research Mode
    fun toggleOfflineFieldMode() {
        val next = !_isOfflineFieldMode.value
        _isOfflineFieldMode.value = next
    }

    // Trigger Cloud Sync
    fun syncWithCloud() {
        scope.launch {
            _isCloudSyncing.value = true
            delay(1200L) // Simulate network cloud sync
            _pendingSyncCount.value = 0
            _experiments.value = _experiments.value.map { it.copy(isSyncedToCloud = true) }
            _isCloudSyncing.value = false
        }
    }

    // Process Subscription Payment (Stripe or PayPal)
    fun processSubscriptionPayment(
        planTitle: String,
        amount: Double,
        gateway: String, // "Stripe" or "PayPal"
        email: String
    ): PaymentTransaction {
        val txnId = "TXN_${System.currentTimeMillis()}_${gateway.uppercase()}"
        val transaction = PaymentTransaction(
            transactionId = txnId,
            userId = _userProfile.value.id,
            userEmail = email,
            planName = planTitle,
            amount = amount,
            paymentGateway = gateway,
            merchantPaypal = "varinderj83@gmail.com",
            timestamp = System.currentTimeMillis(),
            status = "COMPLETED",
            receiptEmailSent = true
        )
        _transactions.value = listOf(transaction) + _transactions.value
        _userProfile.value = _userProfile.value.copy(
            isSubscribed = true,
            activePlan = planTitle,
            trialDaysRemaining = 0,
            subscriptionDaysRemaining = 30, // Extended subscription renewed
            expirationEmailSent = false
        )
        return transaction
    }
}
