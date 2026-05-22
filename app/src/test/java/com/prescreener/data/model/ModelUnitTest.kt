package com.prescreener.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelUnitTest {

    @Test
    fun `UrgencyLevel fromString matches correctly`() {
        assertEquals(UrgencyLevel.IMMEDIATE, UrgencyLevel.fromString("IMMEDIATE"))
        assertEquals(UrgencyLevel.IMMEDIATE, UrgencyLevel.fromString("immediate"))
        assertEquals(UrgencyLevel.URGENT, UrgencyLevel.fromString("URGENT"))
        assertEquals(UrgencyLevel.NON_URGENT, UrgencyLevel.fromString("NON-URGENT"))
        assertEquals(UrgencyLevel.UNKNOWN, UrgencyLevel.fromString("random string"))
    }

    @Test
    fun `BiologicalSex labels are correct`() {
        assertEquals("Male", BiologicalSex.MALE.label)
        assertEquals("Female", BiologicalSex.FEMALE.label)
        assertEquals("Prefer not to say", BiologicalSex.PREFER_NOT_TO_SAY.label)
    }
}
