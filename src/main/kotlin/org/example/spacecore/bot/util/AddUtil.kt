package org.example.spacecore.bot.util

import org.example.spacecore.bot.model.Gender
import org.example.spacecore.bot.model.Vibe
import org.example.spacecore.bot.service.ProfileService
import org.telegram.telegrambots.meta.api.objects.User

class AddUtil {
    companion object{
        fun generateAngels(profileService: ProfileService){

            for (i in (0..10000)) {
                profileService.getOrCreateProfile(User.builder().id(i.toLong()).firstName("Ангелина$i").lastName("").userName("ilduseps").isBot(false).build())
                profileService.updateAge(i.toLong(), 22 + (0..8).random() - 4)
                profileService.updateVibe(i.toLong(), Vibe.fromInt((0..9).random()))
                profileService.updateGender(i.toLong(), Gender.FEMALE)
                profileService.updateLookingFor(i.toLong(), Gender.MALE)
                profileService.updateActivityStatus(i.toLong(), true)
                profileService.updatePhoto(i.toLong(), listOf("AgACAgIAAxkBAAIB02jOfIWf2G4O66rNscMoENQOhC0BAAP8MRvVT3lKeb1DToF_Pc0BAAMCAAN5AAM2BA", "AgACAgIAAxkBAAIDkmjPt3mY_Fem-7CrQNkU8MvVEdJCAALD9zEb1U-BSkqpxmC_YPHFAQADAgADeAADNgQ")[(0..1).random()])
                profileService.updateDescription(i.toLong(), listOf("Я бы любила тебя долго-долго!", "Люблю страстно!")[(0..1).random()])
            }
        }
    }
}