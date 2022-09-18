package io.github.droidkaigi.confsched2022.zipline

import app.cash.zipline.ZiplineService
import io.github.droidkaigi.confsched2022.model.DroidKaigiSchedule

interface ScheduleModifier : ZiplineService {
    fun modify(
        schedule: DroidKaigiSchedule
    ): DroidKaigiSchedule
}
