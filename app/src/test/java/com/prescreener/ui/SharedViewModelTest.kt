package com.prescreener.ui

import com.prescreener.data.model.*
import com.prescreener.data.repository.SymptomRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModelTest {

    private lateinit var viewModel: SharedViewModel
    private val repository: SymptomRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SharedViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateAge updates patientInfo state`() {
        viewModel.updateAge("30")
        assertEquals("30", viewModel.patientInfo.value.age)
    }

    @Test
    fun `toggleChip adds and removes chip from symptomInput state`() {
        val chip = "Fever"
        viewModel.toggleChip(chip)
        assertTrue(viewModel.symptomInput.value.selectedChips.contains(chip))
        
        viewModel.toggleChip(chip)
        assertTrue(viewModel.symptomInput.value.selectedChips.isEmpty())
    }

    @Test
    fun `analyze success updates analysisState to Success`() = runTest {
        val response = AnalysisResponse(
            possibleCategories = listOf("General"),
            urgencyLevel = "NON-URGENT",
            doctorNote = "Test note"
        )
        coEvery { repository.analyze(any()) } returns Result.success(response)

        viewModel.updateAge("25")
        viewModel.analyze()

        assertTrue(viewModel.analysisState.value is UiState.Success)
        assertEquals(response, (viewModel.analysisState.value as UiState.Success).data)
    }

    @Test
    fun `analyze failure updates analysisState to Error`() = runTest {
        val errorMessage = "Network Error"
        coEvery { repository.analyze(any()) } returns Result.failure(Exception(errorMessage))

        viewModel.analyze()

        assertTrue(viewModel.analysisState.value is UiState.Error)
        assertEquals(errorMessage, (viewModel.analysisState.value as UiState.Error).message)
    }

    @Test
    fun `resetAll resets all states to initial values`() {
        viewModel.updateAge("40")
        viewModel.toggleChip("Headache")
        
        viewModel.resetAll()
        
        assertEquals("", viewModel.patientInfo.value.age)
        assertTrue(viewModel.symptomInput.value.selectedChips.isEmpty())
        assertTrue(viewModel.analysisState.value is UiState.Idle)
    }
}
