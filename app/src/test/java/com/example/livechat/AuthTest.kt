package com.example.livechat

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class AuthTest {

    @Mock
    lateinit var mockAuth: FirebaseAuth
    @Mock lateinit var mockFirestore: FirebaseFirestore
    @Mock lateinit var mockStorage: FirebaseStorage
    @Mock lateinit var mockContext: Context

    private lateinit var viewModel: LCViewModel

    @Before
    fun setup() {
        viewModel = LCViewModel(mockAuth, mockFirestore, mockStorage, mockContext)
    }

    @Test
    fun `login should call FirebaseAuth signIn`() {
        // 1. Подготовка моков
        val task = mock<Task<AuthResult>>()
        whenever(mockAuth.signInWithEmailAndPassword("test@test.com", "123456"))
            .thenReturn(task)

        // 2. Вызов метода
        viewModel.loginIn("test@test.com", "123456", mockContext)

        // 3. Проверка
        verify(mockAuth).signInWithEmailAndPassword("test@test.com", "123456")
    }
}