package com.example

import com.example.analytics.ScienceFeedbackEngine
import com.example.crypto.CryptoManager
import com.example.data.SciLabRepository
import com.example.model.ExperimentDataPoint
import com.example.model.UserRole
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCryptoManagerEncryptionAndSha256() {
        val rawData = "Temperature: 24.5C, pH: 7.21, Calibration: Valid"
        val checksum = CryptoManager.calculateSha256Checksum(rawData)
        assertTrue(checksum.startsWith("SHA256:"))

        val encrypted = CryptoManager.encryptPayload(rawData)
        assertNotNull(encrypted)
        val decrypted = CryptoManager.decryptPayload(encrypted)
        assertEquals(rawData, decrypted)
    }

    @Test
    fun testScienceFeedbackEngineRegression() {
        val samplePoints = listOf(
            ExperimentDataPoint(id = 1L, experimentId = "test", timeOffsetSec = 0f, paramName = "T", value = 1.0f, unit = "s"),
            ExperimentDataPoint(id = 2L, experimentId = "test", timeOffsetSec = 10f, paramName = "T", value = 2.0f, unit = "s"),
            ExperimentDataPoint(id = 3L, experimentId = "test", timeOffsetSec = 20f, paramName = "T", value = 3.0f, unit = "s"),
            ExperimentDataPoint(id = 4L, experimentId = "test", timeOffsetSec = 30f, paramName = "T", value = 4.0f, unit = "s")
        )
        val result = ScienceFeedbackEngine.analyzeExperimentData(samplePoints, "Linear growth expected")
        assertTrue("R² should be close to 1.0 for linear data", result.rSquared >= 0.99f)
        assertTrue(result.scientificVerdict.contains("Confirmed"))
    }

    @Test
    fun testTwoDaysPriorSubscriptionEmailDispatch() {
        SciLabRepository.sendSubscriptionExpirationEmail(2)
        val user = SciLabRepository.userProfile.value
        assertEquals(2, user.subscriptionDaysRemaining)
        assertTrue(user.expirationEmailSent)

        val emails = SciLabRepository.expirationEmails.value
        assertTrue("Alert email should be registered in dispatch logs", emails.isNotEmpty())
        val latest = emails.first()
        assertEquals(user.email, latest.recipientEmail)
        assertTrue(latest.subject.contains("2 Days"))
    }

    @Test
    fun testRoleBasedAccessPermissions() {
        // Test switching to Student
        SciLabRepository.switchUserRole(UserRole.STUDENT)
        assertEquals(UserRole.STUDENT, SciLabRepository.userProfile.value.role)

        // Test switching to Teacher
        SciLabRepository.switchUserRole(UserRole.TEACHER)
        assertEquals(UserRole.TEACHER, SciLabRepository.userProfile.value.role)
    }

    @Test
    fun testLoginAndLogoutRoleAssignment() {
        // Test Student login
        SciLabRepository.login("Alex Rivera", "alex@student.edu", UserRole.STUDENT)
        assertTrue(SciLabRepository.isLoggedIn.value)
        assertEquals(UserRole.STUDENT, SciLabRepository.userProfile.value.role)
        assertEquals("Alex Rivera", SciLabRepository.userProfile.value.name)

        // Test Logout
        SciLabRepository.logout()
        assertFalse(SciLabRepository.isLoggedIn.value)

        // Test Teacher login
        SciLabRepository.login("Dr. Marcus Vance", "marcus@faculty.edu", UserRole.TEACHER)
        assertTrue(SciLabRepository.isLoggedIn.value)
        assertEquals(UserRole.TEACHER, SciLabRepository.userProfile.value.role)
        assertEquals("Dr. Marcus Vance", SciLabRepository.userProfile.value.name)
    }

    @Test
    fun testSciLabRepositoryPaymentAndSubscription() {
        val txn = SciLabRepository.processSubscriptionPayment(
            planTitle = "1 Month Access",
            amount = 1.00,
            gateway = "PayPal",
            email = "student@university.edu"
        )
        assertEquals("PayPal", txn.paymentGateway)
        assertEquals(1.00, txn.amount, 0.001)
        assertEquals("COMPLETED", txn.status)
        assertTrue(SciLabRepository.userProfile.value.isSubscribed)
        assertEquals(30, SciLabRepository.userProfile.value.subscriptionDaysRemaining)
    }
}
