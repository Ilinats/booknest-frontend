package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.response.DeviceTokenResponse
import com.example.booknest.domain.repository.NotificationsRepository

class RegisterDeviceTokenUseCase(
    private val notificationsRepository: NotificationsRepository
) {
    suspend operator fun invoke(request: RegisterDeviceTokenRequest): Result<DeviceTokenResponse> {
        return notificationsRepository.registerDeviceToken(request)
    }
}


