package io.github.acedroidx.danmaku.data.home

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DanmakuConfig::class], version = 2
)
abstract class DanmakuConfigDatabase : RoomDatabase() {
    abstract fun danmakuConfigDao(): DanmakuConfigDao
}