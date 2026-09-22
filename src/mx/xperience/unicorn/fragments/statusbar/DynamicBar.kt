package mx.xperience.unicorn.fragments.statusbar
import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.preference.*
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import org.json.JSONArray
class DynamicBar : SettingsPreferenceFragment() {
 private val resolver get()=requireContext().contentResolver
 private val eventTypeIds=listOf("audio_recording","media","lyrics","call","notification","timer","stopwatch","alarm","charging","bluetooth","hotspot","ringer","vpn","clipboard","torch","promoted_ongoing","sports","app_switch","biometric_unlock")
 private var observer:ContentObserver?=null
 override fun onCreatePreferences(s:Bundle?,r:String?){ addPreferencesFromResource(R.xml.dynamic_bar); setupEvents(); updateKeyguard(); registerObserver() }
 private fun disabled():Set<String>{ val j=Settings.Secure.getStringForUser(resolver,"ax_dynamic_bar_events",UserHandle.USER_CURRENT)?:return emptySet(); return try{val a=JSONArray(j);(0 until a.length()).mapTo(mutableSetOf()){a.getString(it)}}catch(e:Exception){emptySet()} }
 private fun setupEvents(){ val d=disabled(); eventTypeIds.forEach{id->findPreference<SwitchPreferenceCompat>("event_$id")?.apply{isChecked=id !in d;setOnPreferenceChangeListener{_,v-> val n=if(v as Boolean) disabled()-id else disabled()+id; Settings.Secure.putStringForUser(resolver,"ax_dynamic_bar_events",if(n.isEmpty()) "" else JSONArray(n.toList()).toString(),UserHandle.USER_CURRENT); if(id=="notification") updateCompact();true}}}; updateCompact() }
 private fun updateCompact(){findPreference<Preference>("ax_dynamic_bar_compact_notifications")?.isVisible=findPreference<SwitchPreferenceCompat>("event_notification")?.isChecked==true}
 private fun updateKeyguard(){ val e=Settings.Secure.getIntForUser(resolver,"ax_dynamic_bar_keyguard_enabled",1,UserHandle.USER_CURRENT)==1; findPreference<Preference>("ax_dynamic_bar_keyguard_battery_chip_mode")?.isVisible=e }
 private fun registerObserver(){observer=object:ContentObserver(Handler(Looper.getMainLooper())){override fun onChange(s:Boolean,u:Uri?){if(u?.lastPathSegment=="ax_dynamic_bar_events") setupEvents(); if(u?.lastPathSegment=="ax_dynamic_bar_keyguard_enabled") updateKeyguard()}}; resolver.registerContentObserver(Settings.Secure.getUriFor("ax_dynamic_bar_events"),false,observer!!); resolver.registerContentObserver(Settings.Secure.getUriFor("ax_dynamic_bar_keyguard_enabled"),false,observer!!)}
 override fun onDestroy(){observer?.let{resolver.unregisterContentObserver(it)};super.onDestroy()}
 override fun getMetricsCategory()=MetricsProto.MetricsEvent.RAINBOW_UNICORN
 companion object { @JvmStatic fun reset(c:Context){val r=c.contentResolver; mapOf("ax_dynamic_bar_enabled" to 0,"ax_dynamic_bar_keyguard_enabled" to 1,"ax_dynamic_bar_compact_notifications" to 1,"ax_dynamic_bar_keyguard_battery_chip_mode" to 1,"ax_dynamic_bar_chip_style" to 0,"ax_dynamic_bar_lockscreen_media_enabled" to 0,"ax_dynamic_bar_lockscreen_media_lyrics_enabled" to 0).forEach{(k,v)->Settings.Secure.putIntForUser(r,k,v,UserHandle.USER_CURRENT)};Settings.Secure.putStringForUser(r,"ax_dynamic_bar_events","",UserHandle.USER_CURRENT)} }
}