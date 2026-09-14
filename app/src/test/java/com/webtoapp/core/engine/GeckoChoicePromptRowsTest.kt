package com.webtoapp.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.gecko.util.GeckoBundle
import org.mozilla.geckoview.GeckoSession

class GeckoChoicePromptRowsTest {

    @Test
    fun `flat choices preserve labels disabled state and selection`() {
        val rows = flattenGeckoChoiceRows(
            arrayOf(
                choice(ChoiceSpec(id = "a", label = "Alpha")),
                choice(ChoiceSpec(id = "b", label = "Beta", disabled = true, selected = true))
            )
        )

        assertEquals(listOf("Alpha", "Beta"), rows.map { it.label })
        assertTrue(rows[0].selectable)
        assertFalse(rows[1].selectable)
        assertTrue(rows[1].selected)
        assertEquals("b", rows[1].choice?.id)
    }

    @Test
    fun `disabled option group disables descendants and keeps its heading`() {
        val rows = flattenGeckoChoiceRows(
            arrayOf(
                choice(
                    ChoiceSpec(
                        id = "group",
                        label = "Group",
                        disabled = true,
                        items = listOf(ChoiceSpec(id = "child", label = "Child"))
                    )
                )
            )
        )

        assertEquals(2, rows.size)
        assertTrue(rows[0].groupLabel)
        assertNull(rows[0].choice)
        assertFalse(rows[1].selectable)
        assertTrue(rows[1].label.endsWith("Child"))
    }

    @Test
    fun `separators become visible non-selectable rows`() {
        val rows = flattenGeckoChoiceRows(
            arrayOf(choice(ChoiceSpec(id = "separator", label = "", separator = true)))
        )

        assertEquals(1, rows.size)
        assertTrue(rows.single().separator)
        assertFalse(rows.single().selectable)
        assertNull(rows.single().choice)
    }

    private fun choice(spec: ChoiceSpec): GeckoSession.PromptDelegate.ChoicePrompt.Choice {
        val constructor = GeckoSession.PromptDelegate.ChoicePrompt.Choice::class.java
            .getDeclaredConstructor(GeckoBundle::class.java)
            .apply { isAccessible = true }
        return constructor.newInstance(spec.toBundle())
    }

    private fun ChoiceSpec.toBundle(): GeckoBundle = GeckoBundle().apply {
        putBoolean("disabled", disabled)
        putString("icon", "")
        putString("id", id)
        putString("label", label)
        putBoolean("selected", selected)
        putBoolean("separator", separator)
        items?.let { children ->
            putBundleArray("items", children.map { it.toBundle() }.toTypedArray())
        }
    }

    private data class ChoiceSpec(
        val id: String,
        val label: String,
        val disabled: Boolean = false,
        val selected: Boolean = false,
        val separator: Boolean = false,
        val items: List<ChoiceSpec>? = null
    )
}
