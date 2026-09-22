package mx.xperience.unicorn.fragments.statusbar
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
class DynamicBarExpandedDemoView @JvmOverloads constructor(c:Context,a:AttributeSet?=null,d:Int=0):View(c,a,d){private var p=0f;private val anim=ValueAnimator.ofFloat(0f,1f).apply{duration=1800;repeatCount=ValueAnimator.INFINITE;repeatMode=ValueAnimator.REVERSE;addUpdateListener{p=it.animatedValue as Float;invalidate()}};private val paint=Paint(1);override fun onAttachedToWindow(){super.onAttachedToWindow();anim.start()};override fun onDetachedFromWindow(){anim.cancel();super.onDetachedFromWindow()};override fun onDraw(x:Canvas){super.onDraw(x);val h=height/2f;val w=width*(.25f+.2f*p);paint.color=Color.rgb(90,90,100);x.drawRoundRect(width/2f-w/2,h-16,width/2f+w/2,h+16,18f,18f,paint);paint.color=Color.WHITE;x.drawCircle(width/2f-w/2+14,h,5f,paint)}}