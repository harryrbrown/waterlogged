package com.example.waterlogged.tile

import androidx.wear.protolayout.expression.AppDataKey
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat

class WaterloggedTile {
    companion object {
        val KEY_WATER_INTAKE = AppDataKey<DynamicFloat>("water_intake")
        val KEY_WATER_GOAL = AppDataKey<DynamicFloat>("water_goal")
        val KEY_WATER_INTAKE_RATIO = AppDataKey<DynamicFloat>("water_intake_ratio")
    }
}