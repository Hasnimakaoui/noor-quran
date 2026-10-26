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
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(12, 24, 22), panel = Color.rgb(23, 43, 37), gold = Color.rgb(211, 176, 104), cream = Color.rgb(246, 239, 220), muted = Color.rgb(174, 190, 175);
    private LinearLayout root, body, nav;
    private SharedPreferences prefs;
    private int page = 1, dhikrCount = 0;
    private String tab = "القرآن";
    private float downX;
    private WebView quranWebView;
    private MediaPlayer player;
    private String selectedEdition="";
    private String selectedReciter="عبد الباسط عبد الصمد — مجوّد";
    private int selectedSurah=1;
    private TextView audioStatus;
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
            LinearLayout marks=row();
            marks.addView(button("حفظ علامة",()->{prefs.edit().putInt("bookmark",page).apply();Toast.makeText(this,"تم حفظ العلامة عند الصفحة "+page,Toast.LENGTH_SHORT).show();}),new LinearLayout.LayoutParams(0,-2,1));
            marks.addView(button("الذهاب للعلامة",()->{page=prefs.getInt("bookmark",1);prefs.edit().putInt("page",page).apply();renderTab();}),new LinearLayout.LayoutParams(0,-2,1));
            resume.addView(marks);
            resume.addView(text("معاينة القارئ والصفحة",12,muted,false)); card(resume);
            TextView pageLabel=text("صفحة المصحف المدني رقم "+page,15,gold,true); card(pageLabel);
            quranWebView=new WebView(this); quranWebView.getSettings().setJavaScriptEnabled(true); quranWebView.getSettings().setDomStorageEnabled(true); quranWebView.setWebViewClient(new WebViewClient()); quranWebView.setBackgroundColor(cream);
            quranWebView.loadUrl("https://quran.com/page/"+page);
            LinearLayout.LayoutParams webParams=new LinearLayout.LayoutParams(-1,0,1); webParams.bottomMargin=10; body.addView(quranWebView,webParams);
            TextView sourceNote=text("يُعرض محتوى المصحف عبر الإنترنت. حفظ الصفحات للعمل دون إنترنت لم يُفعّل بعد.",11,muted,false); body.addView(sourceNote,lp(-1,-2));
            section("استمع إلى التلاوة");
            String[] reciterNames={"عبد الباسط عبد الصمد — مجوّد","أحمد العجمي","عبد الله خلف","محمد اللحيدان"};
            String[] editions={"عبد الباسط عبد الصمد","أحمد العجمي","عبد الله الخلف","محمد اللحيدان"};
            Spinner reciterSpinner=new Spinner(this);
            reciterSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,reciterNames));
            int initialEdition=0; for(int i=0;i<editions.length;i++) if(editions[i].equals(selectedEdition)) initialEdition=i;
            reciterSpinner.setSelection(initialEdition);
            reciterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
                public void onNothingSelected(android.widget.AdapterView<?> parent){}
                public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){selectedEdition=editions[position];selectedReciter=reciterNames[position];}
            });
            card(reciterSpinner);
            String[] surahs={"الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه","الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم","لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر","فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق","الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج","نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات","القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر","المسد","الإخلاص","الفلق","الناس"};
            Spinner surahSpinner=new Spinner(this);
            surahSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,surahs));
            surahSpinner.setSelection(Math.max(0,Math.min(113,selectedSurah-1)));
            surahSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
                public void onNothingSelected(android.widget.AdapterView<?> parent){}
                public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){selectedSurah=position+1;}
            });
            card(surahSpinner);
            audioStatus=text("اختر القارئ والسورة ثم اضغط تشغيل",13,muted,false); card(audioStatus);
            LinearLayout audioButtons=row();
            audioButtons.addView(button("▶ تشغيل السورة",()->playSurah()),new LinearLayout.LayoutParams(0,-2,1));
            audioButtons.addView(button("■ إيقاف",()->stopAudio()),new LinearLayout.LayoutParams(0,-2,1));
            card(audioButtons);
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
    private void playSurah() {
        stopAudio();
        if(audioStatus!=null) audioStatus.setText("جارٍ البحث عن تلاوة "+selectedReciter+"...");
        final String wanted=selectedEdition;
        final int surah=selectedSurah;
        new Thread(()->{
            HttpURLConnection connection=null;
            try {
                URL api=new URL("https://www.mp3quran.net/api/v3/reciters?language=ar");
                connection=(HttpURLConnection)api.openConnection();
                connection.setConnectTimeout(12000); connection.setReadTimeout(12000);
                connection.setRequestMethod("GET");
                StringBuilder jsonText=new StringBuilder();
                try(BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"))){
                    String line; while((line=reader.readLine())!=null) jsonText.append(line);
                }
                JSONObject rootJson=new JSONObject(jsonText.toString());
                JSONArray reciters=rootJson.optJSONArray("reciters");
                String streamUrl=null;
                if(reciters!=null) for(int i=0;i<reciters.length()&&streamUrl==null;i++){
                    JSONObject reciter=reciters.optJSONObject(i);
                    String name=reciter==null?"":reciter.optString("name","");
                    boolean match=false;
                    if(wanted.contains("عبد الباسط")) match=name.contains("عبدالباسط")||name.contains("عبد الباسط");
                    else if(wanted.contains("العجمي")) match=name.contains("العجمي");
                    else if(wanted.contains("الخلف")) match=name.contains("الخلف")||name.contains("خلف");
                    else if(wanted.contains("اللحيدان")) match=name.contains("اللحيدان");
                    if(!match) continue;
                    JSONArray moshafs=reciter.optJSONArray("moshaf");
                    if(moshafs==null||moshafs.length()==0) continue;
                    for(int j=0;j<moshafs.length();j++){
                        JSONObject m=moshafs.optJSONObject(j);
                        if(m==null) continue;
                        String server=m.optString("server","");
                        String available=m.optString("surah_list","");
                        if(server.length()>0&&(available.isEmpty()||(","+available).contains(","+surah+","))){
                            if(!server.endsWith("/")) server+="/";
                            streamUrl=server+String.format(Locale.US,"%03d",surah)+".mp3";
                            break;
                        }
                    }
                }
                if(streamUrl==null) throw new Exception("لم نعثر على تلاوة متاحة لهذا القارئ");
                final String audioUrl=streamUrl;
                runOnUiThread(()->{
                    if(audioStatus!=null) audioStatus.setText("جارٍ تشغيل "+selectedReciter+"...");
                    try {
                        MediaPlayer mp=new MediaPlayer();
                        mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                        mp.setDataSource(audioUrl);
                        mp.setOnPreparedListener(p->{player=p;p.start();runOnUiThread(()->{if(audioStatus!=null)audioStatus.setText("يعمل الآن: "+selectedReciter);});});
                        mp.setOnErrorListener((p,what,extra)->{runOnUiThread(()->{if(audioStatus!=null)audioStatus.setText("تعذر تشغيل ملف التلاوة لهذا القارئ.");});p.release();return true;});
                        player=mp; mp.prepareAsync();
                    } catch(Exception ex) { if(audioStatus!=null) audioStatus.setText("تعذر تشغيل التلاوة."); }
                });
            } catch(Exception ex) {
                runOnUiThread(()->{if(audioStatus!=null)audioStatus.setText("لم أتمكن من تحميل قائمة التلاوات. تحقق من الإنترنت وحاول مجدداً.");});
            } finally { if(connection!=null) connection.disconnect(); }
        }).start();
    }
    private void stopAudio() {
        try { if(player!=null){ if(player.isPlaying()) player.stop(); player.release(); player=null; } } catch(Exception ignored) { player=null; }
        if(audioStatus!=null) audioStatus.setText("تم إيقاف التلاوة");
    }
    @Override protected void onDestroy(){stopAudio();super.onDestroy();}
    private void changePage(int delta) { page=Math.max(1,Math.min(604,page+delta)); prefs.edit().putInt("page",page).apply(); renderTab(); }
}
