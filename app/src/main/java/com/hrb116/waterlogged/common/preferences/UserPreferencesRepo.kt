package com.hrb116.waterlogged.common.preferences

import android.content.Context

fun getUserName(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString(Preferences.USER_NAME.key, null)
}

fun saveUserName(context: Context, unit: String) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(Preferences.USER_NAME.key, unit).apply()
}

fun deleteUserName(context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove(Preferences.USER_NAME.key).apply()
}
