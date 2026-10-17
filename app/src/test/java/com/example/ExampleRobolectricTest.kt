package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SciLab", appName)
  }

  @Test
  fun `verify master pin and background auth recording`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.AuthDataSheetManager.init(context)

    // Master PIN verification
    org.junit.Assert.assertTrue(com.example.data.AuthDataSheetManager.verifyPin("8383"))
    org.junit.Assert.assertFalse(com.example.data.AuthDataSheetManager.verifyPin("0000"))

    // Log a new sign up event
    val initialCount = com.example.data.AuthDataSheetManager.records.value.size
    com.example.data.AuthDataSheetManager.logEvent(
      context = context,
      action = com.example.data.AuthActionType.SIGN_UP,
      fullName = "Elena Rostova",
      email = "elena.rostova@lab.edu",
      role = com.example.model.UserRole.STUDENT,
      institution = "Biochemistry Dept"
    )

    val updatedRecords = com.example.data.AuthDataSheetManager.records.value
    assertEquals(initialCount + 1, updatedRecords.size)
    assertEquals("Elena Rostova", updatedRecords.first().fullName)
    assertEquals(com.example.data.AuthActionType.SIGN_UP, updatedRecords.first().action)

    // Raw CSV check
    val rawCsv = com.example.data.AuthDataSheetManager.getRawCsvContent(context)
    org.junit.Assert.assertTrue(rawCsv.contains("elena.rostova@lab.edu"))
  }
}
