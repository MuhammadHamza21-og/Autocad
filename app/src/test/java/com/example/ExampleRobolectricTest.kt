package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.ProjectJsonSerializer
import com.example.data.model.CadProject
import com.example.data.model.PlotGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("Smart Draftsman", appName)
    }

    @Test
    fun `test project JSON serialization with Android JSONObject`() {
        val original = CadProject(
            name = "Test 5 Marla",
            clientName = "Client ABC",
            plot = PlotGeometry(frontWidth = 25f, leftLength = 45f)
        )
        val json = ProjectJsonSerializer.toJson(original)
        assertNotNull(json)
        val parsed = ProjectJsonSerializer.fromJson(json)

        assertEquals(original.id, parsed.id)
        assertEquals(original.name, parsed.name)
        assertEquals(original.clientName, parsed.clientName)
        assertEquals(original.plot.frontWidth, parsed.plot.frontWidth, 0.01f)
        assertEquals(original.plot.leftLength, parsed.plot.leftLength, 0.01f)
    }
}
