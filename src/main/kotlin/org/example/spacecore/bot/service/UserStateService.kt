package org.example.spacecore.bot.service

import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.model.UserStateEntity
import org.example.spacecore.bot.repository.UserStateRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserStateService(
    private val userStateRepository: UserStateRepository,
    private val profileService: ProfileService
) {

    fun getUserState(telegramId: Long): UserStateEntity {
        // Сначала убеждаемся, что профиль существует
        profileService.getOrCreateProfile(telegramId)

        return userStateRepository.findByTelegramId(telegramId) ?: createDefaultUserState(telegramId)
    }

    fun getCurrentState(telegramId: Long): UserState {
        return getUserState(telegramId).state
    }

    fun updateState(telegramId: Long, newState: UserState): UserStateEntity {
        val currentState = getUserState(telegramId)

        return if (currentState.id == 0L) {
            // Новая запись
            val newEntity = currentState.copy(state = newState)
            userStateRepository.save(newEntity)
        } else {
            // Обновление существующей
            userStateRepository.updateState(telegramId, newState)
            getUserState(telegramId) // Получаем обновленную entity
        }
    }

    fun updateTempData(telegramId: Long, tempData: Map<String, Any>): UserStateEntity {
        val currentState = getUserState(telegramId)

        return if (currentState.id == 0L) {
            val newEntity = currentState.copy(tempData = tempData)
            userStateRepository.save(newEntity)
        } else {
            userStateRepository.updateTempData(telegramId, tempData)
            getUserState(telegramId)
        }
    }

    fun updateStateAndData(telegramId: Long, newState: UserState, tempData: Map<String, Any>): UserStateEntity {
        val currentState = getUserState(telegramId)

        return if (currentState.id == 0L) {
            val newEntity = currentState.copy(state = newState, tempData = tempData)
            userStateRepository.save(newEntity)
        } else {
            userStateRepository.updateStateAndData(telegramId, newState, tempData)
            getUserState(telegramId)
        }
    }

    fun getTempData(telegramId: Long): Map<String, Any> {
        return getUserState(telegramId).tempData
    }

    fun putTempData(telegramId: Long, key: String, value: Any): UserStateEntity {
        val currentData = getTempData(telegramId).toMutableMap()
        currentData[key] = value

        return updateTempData(telegramId, currentData)
    }

    fun removeTempData(telegramId: Long, key: String): UserStateEntity {
        val currentData = getTempData(telegramId).toMutableMap()
        currentData.remove(key)

        return updateTempData(telegramId, currentData)
    }

    fun clearTempData(telegramId: Long): UserStateEntity {
        return updateTempData(telegramId, emptyMap())
    }

    fun resetUserState(telegramId: Long): UserStateEntity {
        userStateRepository.deleteByTelegramId(telegramId)
        return createDefaultUserState(telegramId)
    }

    private fun createDefaultUserState(telegramId: Long): UserStateEntity {
        val defaultState = UserStateEntity(
            telegramId = telegramId,
            state = UserState.START,
            tempData = emptyMap()
        )
        return userStateRepository.save(defaultState)
    }
}