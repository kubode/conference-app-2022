package io.github.droidkaigi.confsched2022.zipline

import co.touchlab.kermit.Logger
import io.github.droidkaigi.confsched2022.model.DroidKaigiSchedule
import io.github.droidkaigi.confsched2022.model.TimetableItem
import io.github.droidkaigi.confsched2022.model.TimetableItem.Special
import io.github.droidkaigi.confsched2022.model.TimetableItemId
import io.github.droidkaigi.confsched2022.model.TimetableItemList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@ExperimentalCoroutinesApi
class JsScheduleModifier() : ScheduleModifier {
    @OptIn(ExperimentalTime::class)
    override suspend fun modify(schedule: DroidKaigiSchedule): DroidKaigiSchedule {
        Logger.d("Hello JS world!")
        val now = TimeSource.Monotonic.markNow()
        val modified = schedule.copy(
            dayToTimetable = schedule.dayToTimetable.mapValues { timetable ->
                val modifiedSessions = timetable.value.timetableItems.map { timetableItem ->
                    timetableItem.modified()
                }
                timetable.value.copy(
                    timetableItems = TimetableItemList(
                        modifiedSessions.toPersistentList()
                    )
                )
            }.toPersistentMap()
        )
        Logger.d("JS modify took ${now.elapsedNow()}")
        return modified
    }
}

private fun TimetableItem.modified(): TimetableItem {
    Logger.v("Modifying $id $targetAudience")
    return if (this is Special &&
        // Day 1 "App bars" Lunch session
        id == TimetableItemId("b8528bb4-284c-424e-8be5-a4c1721e4ba8") &&
        targetAudience == "TBW"
    ) {
        Logger.v("Modified! $id")
        copy(
            targetAudience = "To be written.",
        )
    } else {
        this
    }
}
