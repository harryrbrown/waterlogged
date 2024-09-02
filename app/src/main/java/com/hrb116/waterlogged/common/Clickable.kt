package com.hrb116.waterlogged.common

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders

val emptyClickable = ModifiersBuilders.Clickable.Builder()
    .setOnClick(ActionBuilders.LoadAction.Builder().build())
    .setId("")
    .build()
