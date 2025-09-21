package org.example.spacecore.bot.util

import com.github.benmanes.caffeine.cache.Caffeine
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.service.UserStateService
import java.util.concurrent.TimeUnit


class TimedCacheMap {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.HOURS)
        .maximumSize(15_000)
//        .evictionListener<Long?, MutableList<Profile>> { key, value, cause ->
//            userStateService.removeTempData(key, "level")
//        }
        .build<Long?, MutableList<Profile>>()

    operator fun get(key: Long): MutableList<Profile>? {
        return cache.getIfPresent(key)
    }

    operator fun set(key: Long, value: MutableList<Profile>) {
        cache.put(key, value)
    }
}