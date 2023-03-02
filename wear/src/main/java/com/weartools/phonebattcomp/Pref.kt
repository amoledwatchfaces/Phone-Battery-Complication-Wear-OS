package com.weartools.phonebattcomp

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Pref(val context: Context) {
    // to make sure there's only one instance

    companion object{
        var data :SharedPreferences?=null
        fun getInstance(context:Context):SharedPreferences{
            if (data==null)
                data = PreferenceManager.getDefaultSharedPreferences(context)
            return data!!
        }
    }
    // WORLD CLOCK
    fun getTempUnit():Boolean { return getInstance(context).getBoolean("temp_unit",true) }
    fun setTempUnit(value: Boolean) { getInstance(context).edit().putBoolean("temp_unit",value).apply() }

    // LOCALE
    //fun updateLocale(s: String) { getInstance(context).edit().putString("locale",s).apply () }
    //fun getLocale(): String { return getInstance(context).getString("locale","en")?:"en" }

}