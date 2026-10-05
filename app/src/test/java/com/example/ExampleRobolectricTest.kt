package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.model.MissionId
import com.example.model.MissionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("Desert Storm", appName)
  }

  @Test
  fun `mission completion unlocks subsequent missions and adds credits`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = GameRepository(context)

    val initialCredits = repository.credits.value

    val resultM1 = MissionResult(
      missionId = MissionId.MISSION_1,
      isSuccess = true,
      timeElapsedSeconds = 45f,
      stars = 3,
      score = 8000,
      creditsEarned = 2500,
      debriefNotes = listOf("Test passed")
    )
    repository.completeMission(resultM1)

    assertEquals(initialCredits + 2500, repository.credits.value)
    val m2Progress = repository.missionProgressMap.value[MissionId.MISSION_2]
    assertTrue(m2Progress?.isUnlocked == true)
  }
}
