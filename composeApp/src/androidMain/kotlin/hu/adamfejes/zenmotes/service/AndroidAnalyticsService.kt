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

    private fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
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

    override fun trackScoreUpdate(amount: Int, isBonus: Boolean) {
        trackEvent(
            "score_update", mapOf(
                FirebaseAnalytics.Param.VALUE to amount,
                FirebaseAnalytics.Param.CONTENT_TYPE to isBonus
            )
        )
    }

    override fun trackGameOver(achievedScore: Long, highScore: Long, isNewHighScore: Boolean) {
        trackEvent(
            "game_over", mapOf(
                FirebaseAnalytics.Param.SCORE to achievedScore,
                FirebaseAnalytics.Param.VALUE to highScore,
                FirebaseAnalytics.Param.CONTENT_TYPE to isNewHighScore
            )
        )
    }

    override fun trackGamePause(currentScore: Int, countdownTime: Long) {
        trackEvent(
            "game_pause", mapOf(
                FirebaseAnalytics.Param.SCORE to currentScore,
                FirebaseAnalytics.Param.VALUE to countdownTime
            )
        )
    }

    override fun trackGameResume(currentScore: Int, countdownTime: Long) {
        trackEvent(
            "game_resume", mapOf(
                FirebaseAnalytics.Param.SCORE to currentScore,
                FirebaseAnalytics.Param.VALUE to countdownTime
            )
        )
    }

    override fun trackSettingsChanged(settingName: String, newValue: Any) {
        trackEvent(
            "settings_changed", mapOf(
                FirebaseAnalytics.Param.ITEM_NAME to settingName,
                FirebaseAnalytics.Param.VALUE to newValue
            )
        )
    }

    override fun trackAverageFPS(average: Int) {
        trackEvent(
            "average_fps", mapOf(
                FirebaseAnalytics.Param.VALUE to average
            )
        )
    }

    override fun setUserProperty(propertyName: String, value: String) {
        firebaseAnalytics.setUserProperty(propertyName, value)
    }
}