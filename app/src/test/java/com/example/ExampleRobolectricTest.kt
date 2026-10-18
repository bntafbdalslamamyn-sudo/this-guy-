package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DefaultWorkoutSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context matches Body Complete`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Body Complete", appName)
    }

    @Test
    fun `weekly workout schedule contains all 7 days and official exercises`() {
        val days = DefaultWorkoutSchedule.weeklyDays
        assertEquals(7, days.size)
        assertEquals("صدر وتراي", days[0].titleAr)
        assertEquals("ظهر وباي", days[1].titleAr)
        assertEquals("أكتاف", days[2].titleAr)
        assertTrue(days[3].isRestDay)
        assertEquals("باي وتراي", days[4].titleAr)
        assertEquals("أرجل وبطن", days[5].titleAr)
        assertTrue(days[6].isRestDay)

        val exercises = DefaultWorkoutSchedule.defaultExercisesForUser("user_1")
        assertTrue(exercises.isNotEmpty())
        assertFalse(exercises.any { it.nameAr.contains("الشيخ محمود") || it.notesAr.contains("الشيخ محمود") })
    }
}
