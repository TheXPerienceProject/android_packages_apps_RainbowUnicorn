package mx.xperience.unicorn.fragments.statusbar
import android.content.Context
import android.os.*
import android.provider.Settings
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
class DynamicIslandSettings:SettingsPreferenceFragment(){override fun onCreate(b:Bundle?){super.onCreate(b);addPreferencesFromResource(R.xml.dynamic_island_settings)};override fun getMetricsCategory()=MetricsProto.MetricsEvent.RAINBOW_UNICORN
 companion object{@JvmStatic fun reset(c:Context){val r=c.contentResolver;val m=mapOf("status_bar_show_dynamic_island" to 0,"status_bar_dynamic_island_width" to 110,"status_bar_dynamic_island_media_controls" to 1,"status_bar_dynamic_island_lyrics" to 0,"status_bar_dynamic_island_ongoing_activities" to 1,"status_bar_dynamic_island_calls" to 1,"status_bar_dynamic_island_screen_recording" to 1,"status_bar_dynamic_island_alarms" to 1,"status_bar_dynamic_island_stopwatch" to 1,"status_bar_dynamic_island_flashlight" to 1,"status_bar_dynamic_island_live_scores" to 1);m.forEach{(k,v)->Settings.System.putIntForUser(r,k,v,UserHandle.USER_CURRENT)}}}
}