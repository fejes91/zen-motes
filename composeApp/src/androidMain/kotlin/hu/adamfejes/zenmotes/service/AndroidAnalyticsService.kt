package hu.adamfejes.zenmotes.service

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import hu.adamfejes.zenmotes.BuildConfig
import hu.adamfejes.zenmotes.utils.Logger

class AndroidAnalyticsService(context: Context) : AnalyticsService {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val isReleaseBuild = !BuildConfig.DEBUG

    override fun trackEvent(eventName: String, parameters: Map<String, Any>) {
        Logger.d(
            "Analytics",
            "Event: $eventName, Parameters: $parameters, Reported to Firebase: $isReleaseBuild"
        )

        if (isReleaseBuild) {
            firebaseAnalytics.logEvent(eventName) {
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> param(key, value)
                        is Long -> param(key, value)
                        is Int -> param(key, value.toLong())
                        is Double -> param(key, value)
                        is Boolean -> param(key, if (value) 1L else 0L)
                        else -> param(key, value.toString())
                    }
                }
            }
        }
    }

    override fun trackScreenView(screenName: String) {
        trackEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to screenName
            )
        )
    }

    override fun trackGameStart() {
        trackEvent("game_start")
    }

    override fun trackGameEnd(score: Long, duration: Long) {
        trackEvent(
            "game_end", mapOf(
                "score" to score,
                "duration_seconds" to duration
            )
        )
    }

    override fun trackGameOver(achievedScore: Long, highScore: Long, isNewHighScore: Boolean) {
        trackEvent(
            "game_over", mapOf(
                "achieved_score" to achievedScore,
                "high_score" to highScore,
                "is_new_high_score" to isNewHighScore
            )
        )
    }

    override fun trackGamePause() {
        trackEvent("game_pause")
    }

    override fun trackGameResume() {
        trackEvent("game_resume")
    }

    override fun trackSettingsChanged(settingName: String, newValue: Any) {
        trackEvent(
            "settings_changed", mapOf(
                "setting_name" to settingName,
                "new_value" to newValue
            )
        )
    }

    override fun setUserProperty(propertyName: String, value: String) {
        firebaseAnalytics.setUserProperty(propertyName, value)
    }
}