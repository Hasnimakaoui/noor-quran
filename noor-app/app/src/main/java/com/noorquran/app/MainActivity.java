package com.noorquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(12, 24, 22), panel = Color.rgb(23, 43, 37), gold = Color.rgb(211, 176, 104), cream = Color.rgb(246, 239, 220), muted = Color.rgb(174, 190, 175);
    private LinearLayout root, body, nav;
    private SharedPreferences prefs;
    private int page = 1, dhikrCount = 0;
    private String tab = "القرآن";
    private float downX;
    private WebView quranWebView;
    @Override public void onCreate(Bundle b) { super.onCreate(b); prefs=getSharedPreferences("noor",0); page=prefs.getInt("page",1); dhikrCount=prefs.getInt("dhikr",0); showApp(); }
    private GradientDrawable shape(int color, int radius, int stroke) { GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if(stroke>0)d.setStroke(1,stroke); return d; }
    private TextView text(String s,int size,int color,boolean bold) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); t.setPadding(8,8,8,8); return t; }
    private LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private LinearLayout.LayoutParams lp(int w,int h) { return new LinearLayout.LayoutParams(w,h); }
    private TextView button(String s,Runnable action) { TextView v=text(s,15,cream,true); v.setBackground(shape(panel,22,gold)); v.setPadding(14,12,14,12); v.setOnClickListener(x->action.run()); return v; }
    private void showApp() {
        root=column(); root.setBackgroundColor(bg); root.setPadding(16,12,16,8);
        LinearLayout header=row(); TextView brand=text("۞",30,gold,true); header.addView(brand,lp(48,54)); LinearLayout title=column(); title.addView(text("نور القرآن",23,cream,true),lp(-1,32)); title.addView(text("رفيقك اليومي مع كتاب الله",12,muted,false),lp(-1,22)); header.addView(title,new LinearLayout.LayoutParams(0,54,1)); header.addView(text("☾",26,gold,true),lp(42,54)); root.addView(header);
        View line=new View(this); line.setBackgroundColor(0xff3c594b); root.addView(line,lp(-1,1));
        body=column(); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1); bp.topMargin=8; root.addView(body,bp);
        nav=row(); nav.setBackground(shape(panel,24,0)); nav.setPadding(3,4,3,4);
        String[] labels={"القرآن","الأذكار","التسبيح","إنجازي"};
        for(String label:labels){ TextView n=text(label,12,label.equals(tab)?gold:muted,label.equals(tab)); n.setOnClickListener(v->{tab=label;showApp();}); nav.addView(n,new LinearLayout.LayoutParams(0,52,1)); }
        root.addView(nav,lp(-1,58)); setContentView(root); renderTab();
    }
    private void section(String s) { TextView t=text(s,19,cream,true); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setPadding(4,14,4,10); body.addView(t,lp(-1,-2)); }
    private void card(View v) { LinearLayout box=column(); box.setPadding(12,12,12,12); box.setBackground(shape(panel,20,0)); box.addView(v); LinearLayout.LayoutParams p=lp(-1,-2); p.bottomMargin=12; body.addView(box,p); }
    private void renderTab() {
        body.removeAllViews();
        if(tab.equals("القرآن")) {
            section("تابع تلاوتك");
            LinearLayout resume=column(); resume.addView(text("المصحف المدني",17,gold,true)); resume.addView(text("صفحتك المحفوظة: "+page+" من 604",14,cream,false));
            LinearLayout actions=row(); actions.addView(button("السابق",()->changePage(-1)),new LinearLayout.LayoutParams(0,-2,1)); actions.addView(button("التالي",()->changePage(1)),new LinearLayout.LayoutParams(0,-2,1)); resume.addView(actions);
            resume.addView(text("معاينة القارئ والصفحة",12,muted,false)); card(resume);
            TextView pageLabel=text("صفحة المصحف المدني رقم "+page,15,gold,true); card(pageLabel);
            quranWebView=new WebView(this); quranWebView.getSettings().setJavaScriptEnabled(true); quranWebView.getSettings().setDomStorageEnabled(true); quranWebView.setWebViewClient(new WebViewClient()); quranWebView.setBackgroundColor(cream);
            quranWebView.loadUrl("https://quran.com/page/"+page);
            LinearLayout.LayoutParams webParams=new LinearLayout.LayoutParams(-1,0,1); webParams.bottomMargin=10; body.addView(quranWebView,webParams);
            TextView sourceNote=text("يُعرض محتوى المصحف عبر الإنترنت. حفظ الصفحات للعمل دون إنترنت لم يُفعّل بعد.",11,muted,false); body.addView(sourceNote,lp(-1,-2));
            section("القرّاء");
            String[] reciters={"عبد الله خلف","أحمد العجمي","عبد الباسط عبد الصمد","محمد اللحيدان"};
            for(String r:reciters) { TextView item=text("♫    "+r+"     ›",15,cream,true); item.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); item.setBackground(shape(panel,16,0)); LinearLayout.LayoutParams p=lp(-1,50);p.bottomMargin=7;body.addView(item,p); item.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://quran.com/reciters")))); }
            
        } else if(tab.equals("الأذكار")) {
            section("أذكار يومية");
            String[] az={"سبحان الله","الحمد لله","لا إله إلا الله","الله أكبر","أستغفر الله"};
            for(String a:az){ TextView item=text(a+"\nاضغط لعدّ الذكر",19,cream,true); item.setBackground(shape(panel,18,gold)); LinearLayout.LayoutParams p=lp(-1,78);p.bottomMargin=10;body.addView(item,p); item.setOnClickListener(v->{Toast.makeText(this,a,Toast.LENGTH_SHORT).show();}); }
        } else if(tab.equals("التسبيح")) {
            section("المسبحة الإلكترونية");
            TextView count=text(String.valueOf(dhikrCount),48,gold,true); count.setBackground(shape(panel,100,gold)); LinearLayout.LayoutParams cp=lp(-1,180);cp.setMargins(20,20,20,20);body.addView(count,cp);
            TextView tap=button("اضغط للتسبيح",()->{dhikrCount++;prefs.edit().putInt("dhikr",dhikrCount).apply();renderTab();}); body.addView(tap,lp(-1,58));
            TextView reset=button("تصفير العداد",()->{dhikrCount=0;prefs.edit().putInt("dhikr",0).apply();renderTab();}); LinearLayout.LayoutParams rp=lp(-1,50);rp.topMargin=12;body.addView(reset,rp);
        } else {
            section("إنجازك مع القرآن");
            card(text("صفحة القراءة الحالية\n"+page+" / 604",22,gold,true));
            card(text("نسبة الصفحات المتقدمة\n"+Math.round(page*100.0/604)+"%",20,cream,true));
            card(text("الصفحة محفوظة تلقائياً على هذا الجهاز.",14,muted,false));
            body.addView(button("العودة إلى المصحف",()->{tab="القرآن";showApp();}),lp(-1,54));
        }
    }
    private void changePage(int delta) { page=Math.max(1,Math.min(604,page+delta)); prefs.edit().putInt("page",page).apply(); renderTab(); }
}
