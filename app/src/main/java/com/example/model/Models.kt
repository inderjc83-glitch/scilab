package com.example.model

enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN
}

enum class ScienceCategory(val displayName: String, val iconName: String) {
    PHYSICS("Physics & Mechanics", "speed"),
    CHEMISTRY("Chemistry & Reactions", "science"),
    BIOLOGY("Biology & Life Science", "biotech"),
    ENVIRONMENTAL("Environmental & Ecology", "eco")
}

enum class ExperimentStatus(val label: String) {
    ACTIVE("Active Recording"),
    IN_REVIEW("Teacher Review"),
    COMPLETED("Completed & Verified")
}

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@student.edu",
    val institution: String = "Department of Natural Sciences",
    val role: UserRole = UserRole.STUDENT,
    val isMfaEnabled: Boolean = true,
    val isSubscribed: Boolean = true,
    val trialDaysRemaining: Int = 0,
    val subscriptionDaysRemaining: Int = 2, // 2 days before complete
    val expirationEmailSent: Boolean = true,
    val expirationEmailTimestamp: Long = System.currentTimeMillis() - 7200000L,
    val activePlan: String? = "1 Month Access",
    val experimentsCompleted: Int = 8,
    val quizScoreMastery: Int = 94
)

data class SubscriptionAlertEmail(
    val emailId: String = "MAIL_${System.currentTimeMillis()}",
    val recipientEmail: String,
    val subject: String,
    val daysRemaining: Int,
    val body: String,
    val dispatchedAt: Long = System.currentTimeMillis(),
    val isDelivered: Boolean = true
)

data class ExperimentDataPoint(
    val id: Long = System.currentTimeMillis(),
    val experimentId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeOffsetSec: Float = 0f,
    val paramName: String,
    val value: Float,
    val unit: String,
    val notes: String = ""
)

data class ScienceExperiment(
    val id: String,
    val title: String,
    val category: ScienceCategory,
    val hypothesis: String,
    val status: ExperimentStatus = ExperimentStatus.ACTIVE,
    val isEncrypted: Boolean = true,
    val encryptionChecksum: String = "SHA256:7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
    val collaborators: List<String> = listOf("Alex R.", "Jordan K.", "Dr. Maya S."),
    val createdAt: Long = System.currentTimeMillis() - 86400000L,
    val isSyncedToCloud: Boolean = true,
    val offlineFieldMode: Boolean = false,
    val teacherGrade: String? = null,
    val teacherNotes: String? = null,
    val automatedFeedback: String? = null,
    val labNotes: String = "Initial observations: Reagents mixed at standard temperature (22°C). Baseline sensor stability established."
)

data class GroupChatMessage(
    val id: String,
    val experimentId: String,
    val senderName: String,
    val senderRole: UserRole,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)

enum class TaskStatus(val label: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    VERIFIED("Verified")
}

data class ProjectTask(
    val id: String,
    val experimentId: String,
    val title: String,
    val assignee: String,
    val status: TaskStatus,
    val dueDate: String
)

data class ScienceQuizQuestion(
    val id: Int,
    val category: ScienceCategory,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val scientificExplanation: String
)

data class SubscriptionTier(
    val id: String,
    val title: String,
    val durationText: String,
    val priceUsd: Double,
    val isPopular: Boolean = false,
    val featureHighlights: List<String>
)

data class PaymentTransaction(
    val transactionId: String,
    val userId: String,
    val userEmail: String,
    val planName: String,
    val amount: Double,
    val paymentGateway: String, // "Stripe" or "PayPal"
    val merchantPaypal: String = "varinderj83@gmail.com",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED",
    val receiptEmailSent: Boolean = true
)

data class AutomatedFeedbackResult(
    val rSquared: Float,
    val meanValue: Float,
    val variance: Float,
    val trendDescription: String,
    val recommendations: List<String>,
    val scientificVerdict: String
)

enum class AppLanguage(val code: String, val displayName: String, val flagEmoji: String) {
    ENGLISH("en", "English", "🇺🇸"),
    SPANISH("es", "Español", "🇪🇸"),
    FRENCH("fr", "Français", "🇫🇷"),
    GERMAN("de", "Deutsch", "🇩🇪"),
    HINDI("hi", "हिन्दी", "🇮🇳")
}
