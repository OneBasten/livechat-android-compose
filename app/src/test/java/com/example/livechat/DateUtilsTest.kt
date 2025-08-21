package com.example.livechat

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock

@RunWith(MockitoJUnitRunner::class)
class DateUtilsTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockStorage: FirebaseStorage

    @Mock
    private lateinit var mockContext: Context

    private lateinit var viewModel: LCViewModel

    @Before
    fun setup() {
        // Инициализируем ViewModel с моками
        viewModel = LCViewModel(mockAuth, mockFirestore, mockStorage, mockContext)
    }

    @Test
    fun `convertMillisToLocalTime should show 'только что' for recent time`() {
        val currentTime = System.currentTimeMillis()
        val result = viewModel.convertMillisToLocalTime(currentTime)
        assert(result == "только что" || result == "0 мин. назад") // Учтите локаль
    }

    @Test
    fun `convertMillisToLocalTime should show minutes ago`() {
        val time10MinutesAgo = System.currentTimeMillis() - 10 * 60 * 1000
        val result = viewModel.convertMillisToLocalTime(time10MinutesAgo)
        assertEquals("10 мин. назад", result)
    }
}