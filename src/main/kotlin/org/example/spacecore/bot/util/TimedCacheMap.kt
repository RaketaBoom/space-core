package org.example.spacecore.bot.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.service.UserStateService
import java.util.concurrent.TimeUnit


class TimedCacheMap(userStateService: UserStateService) {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.HOURS)
        .maximumSize(15_000)
        .evictionListener<Long?, MutableList<Long>> { key, value, cause ->
            if (cause == RemovalCause.EXPIRED) {
                if (key != null)
                    userStateService.removeTempData(key, "level")
            }
        }
        .build<Long, MutableList<Long>>()

    operator fun get(key: Long): MutableList<Long>? {
        return cache.getIfPresent(key)
    }

    operator fun set(key: Long, value: MutableList<Long>) {
        cache.put(key, value)
    }

    fun remove(key: Long) {
        cache.invalidate(key)
    }

    fun refreshTimer(key: Long): Boolean {
        val currentValue = cache.getIfPresent(key)
        cache.put(key, currentValue)

        return true
    }
}