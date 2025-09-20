package org.example.spacecore.bot.service

import org.example.spacecore.bot.model.Gender
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.model.Vibe
import org.example.spacecore.bot.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

@Service
class ProfileService(
    private val profileRepository: ProfileRepository
) {

    fun getOrCreateProfile(userId: Long): Profile {
        return this.getOrCreateProfile(TelegramUser(userId, "", false))
    }

    fun getOrCreateProfile(telegramUser: TelegramUser): Profile {
        var profile = profileRepository.findByTelegramId(telegramUser.id)

        if (profile == null) {
            profile = createProfile(telegramUser)
        } else {
            if (profile.username != telegramUser.userName) {
                profileRepository.updateUsername(telegramUser.id, telegramUser.userName)
            }
        }

        return profile ?: createProfile(telegramUser)
    }

    private fun createProfile(telegramUser: TelegramUser): Profile {
        val profile = Profile(
            telegramId = telegramUser.id,
            username = telegramUser.userName,
            firstName = telegramUser.firstName,
            lastName = telegramUser.lastName,
            name = telegramUser.firstName ?: "User",
            age = 0,
            gender = Gender.OTHER,
            lookingFor = Gender.OTHER,
            description = "",
            photoId = "",
            vibe = Vibe.ZERO,
            isActive = false
        )
        //userStateService.resetUserState(telegramUser.id)
        return profileRepository.save(profile)
    }

    // Частичное обновление профиля
    fun updateProfilePartial(telegramId: Long, updates: Map<String, Any>): Boolean {
        return profileRepository.updatePartial(telegramId, updates)
    }

    // Обновление username, first_name, last_name
    fun updateUserName(telegramId: Long, telegramUser: TelegramUser): Boolean {
        return profileRepository.updateUserName(telegramId, telegramUser.userName, telegramUser.firstName, telegramUser.lastName)
    }

    // Обновление имени
    fun updateName(telegramId: Long, name: String): Boolean {
        return profileRepository.updateName(telegramId, name)
    }

    // Обновление возраста
    fun updateAge(telegramId: Long,age: Int): Boolean {
        return profileRepository.updateAge(telegramId, age)
    }

    // Обновление предпочтений
    fun updateLookingFor(telegramId: Long, lookingFor: Gender): Boolean {
        return profileRepository.updateLookingFor(telegramId, lookingFor)
    }

    // Обновление вайба
    fun updateVibe(telegramId: Long, vibe: Vibe): Boolean {
        return profileRepository.updateVibe(telegramId, vibe)
    }

    // Обновление описания
    fun updateDescription(telegramId: Long, description: String): Boolean {
        return profileRepository.updateDescription(telegramId, description)
    }

    // Обновление фото
    fun updatePhoto(telegramId: Long, photoId: String): Boolean {
        return profileRepository.updatePhoto(telegramId, photoId)
    }

    // Обновление пола
    fun updateGender(telegramId: Long, gender: Gender): Boolean {
        return profileRepository.updateGender(telegramId, gender)
    }

    // Обновление статуса активности
    fun updateActivityStatus(telegramId: Long, isActive: Boolean): Boolean {
        return profileRepository.updateActivityStatus(telegramId, isActive)
    }

    // Полное обновление профиля
    fun updateProfile(profile: Profile): Profile {
        return profileRepository.save(profile)
    }

    fun findMatchingProfiles(currentProfile: Profile): List<Profile> {
        return profileRepository.findMatchingProfiles(
            age = currentProfile.age,
            vibe = currentProfile.vibe.value,
            lookingFor = currentProfile.lookingFor,
            excludeTelegramId = currentProfile.telegramId
        )
    }

    fun getUserInfo(telegramId: Long): UserInfo {
        val profile = profileRepository.findByTelegramId(telegramId)

        return UserInfo(
            id = telegramId,
            username = profile?.username,
            firstName = profile?.firstName,
            lastName = profile?.lastName,
            name = profile?.name ?: "Пользователь",
            hasProfile = profile != null,
            profile = profile
        )
    }

    fun getUsername(telegramId: Long): String {
        val profile = profileRepository.findByTelegramId(telegramId)
        return when {
            profile?.username != null -> "@${profile.username}"
            profile?.firstName != null -> profile.firstName
            else -> "пользователь"
        }
    }

    data class UserInfo(
        val id: Long,
        val username: String?,
        val firstName: String?,
        val lastName: String?,
        val name: String,
        val hasProfile: Boolean,
        val profile: Profile?
    )
}