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
    private final int bg = Color.rgb(8, 17, 16), panel = Color.rgb(17, 34, 30), gold = Color.rgb(220, 183, 105), cream = Color.rgb(249, 245, 232), muted = Color.rgb(153, 174, 163);
    private LinearLayout root, body, nav;
    private SharedPreferences prefs;
    private int page = 1, dhikrCount = 0;
    private String tab = "الرئيسية";
    private float downX;
    private WebView quranWebView;
    private MediaPlayer player;
    private String selectedEdition="";
    private String selectedReciter="عبد الباسط عبد الصمد — مجوّد";
    private int selectedSurah=1;
    private TextView audioStatus;
    @Override public void onCreate(Bundle b) { super.onCreate(b); prefs=getSharedPreferences("noor",0); page=prefs.getInt("page",1); dhikrCount=prefs.getInt("dhikr",0); showApp(); }
    private GradientDrawable shape(int color, int radius, int stroke) { GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if(stroke>0)d.setStroke(1,stroke); return d; }
    private TextView text(String s,int size,int color,boolean bold) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); t.setPadding(3,3,3,3); t.setIncludeFontPadding(true); return t; }
    private LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private LinearLayout.LayoutParams lp(int w,int h) { return new LinearLayout.LayoutParams(w,h); }
    private TextView button(String s,Runnable action) { TextView v=text(s,15,cream,true); v.setBackground(shape(panel,22,gold)); v.setPadding(14,12,14,12); v.setOnClickListener(x->action.run()); return v; }

    private class OrnamentView extends View {
        private final android.graphics.Paint p = new android.graphics.Paint(3);
        public OrnamentView(android.content.Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(android.graphics.Canvas canvas){
            super.onDraw(canvas); float w=getWidth(), h=getHeight(); p.setStyle(android.graphics.Paint.Style.STROKE); p.setStrokeWidth(1.3f); p.setColor(0x99d8b96f);
            float cy=h/2f; float step=Math.max(22f,Math.min(34f,h*0.8f));
            for(float x=step/2;x<w;x+=step){canvas.drawCircle(x,cy,step*0.28f,p); canvas.drawRect(x-step*0.2f,cy-step*0.2f,x+step*0.2f,cy+step*0.2f,p); canvas.drawCircle(x,cy,step*0.46f,p);}
            p.setStyle(android.graphics.Paint.Style.FILL); p.setColor(0xffd8b96f); canvas.drawCircle(step/2,cy,2.5f,p); canvas.drawCircle(w-step/2,cy,2.5f,p);
        }
    }
    private void showApp() {
        root=column(); root.setBackgroundColor(bg); root.setPadding(14,10,14,8);
        LinearLayout header=row(); header.setPadding(14,8,14,8); header.setBackground(shape(0xff132820,24,0xffb99a59));
        LinearLayout emblem=column(); TextView brand=text("۞",30,gold,true); brand.setBackground(shape(0xff10231c,44,0xffc9a75d)); emblem.addView(brand,lp(46,46)); header.addView(emblem,lp(44,48));
        LinearLayout title=column(); title.setGravity(Gravity.CENTER_VERTICAL); title.addView(text("نُورُ القُرآن",22,cream,true),lp(-1,30)); title.addView(text("المصحف • التلاوة • الأذكار",11,muted,false),lp(-1,20)); header.addView(title,new LinearLayout.LayoutParams(0,48,1));
        TextView moon=text("☾",23,gold,true); moon.setBackground(shape(0xff263d34,40,0)); header.addView(moon,lp(42,42)); root.addView(header);
        View line=new View(this); line.setBackgroundColor(0xff263f35); LinearLayout.LayoutParams lineParams=lp(-1,1);lineParams.topMargin=9;root.addView(line,lineParams);
        body=column(); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1); bp.topMargin=8; root.addView(body,bp);
        nav=row(); nav.setBackground(shape(panel,24,0xff344e43)); nav.setPadding(3,4,3,4);
        String[] labels={"الرئيسية","المصحف","القراء","الأذكار"};
        for(String label:labels){ TextView n=text(label,12,label.equals(tab)?gold:muted,label.equals(tab)); n.setOnClickListener(v->{tab=label.equals("القراء")?"المصحف":label;showApp();}); nav.addView(n,new LinearLayout.LayoutParams(0,52,1)); }
        root.addView(nav,lp(-1,58)); setContentView(root); renderTab();
    }
    private void section(String s) { TextView t=text(s,19,cream,true); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setPadding(4,14,4,10); body.addView(t,lp(-1,-2)); }
    private void card(View v) { LinearLayout box=column(); box.setPadding(12,12,12,12); box.setBackground(shape(panel,20,0)); box.addView(v); LinearLayout.LayoutParams p=lp(-1,-2); p.bottomMargin=12; body.addView(box,p); }
    private void renderTab() {
        body.removeAllViews();
        if(tab.equals("الرئيسية")) {
            ScrollView homeScroll=new ScrollView(this);
            LinearLayout home=column(); home.setPadding(2,6,2,12);
            LinearLayout hero=column(); hero.setGravity(Gravity.CENTER); hero.setPadding(18,20,18,22);
            GradientDrawable heroBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff203c30,0xff10241e,0xff0b1915});
            heroBg.setCornerRadius(30); heroBg.setStroke(1,0xffb99a59); hero.setBackground(heroBg);
            TextView ornament=text("۞  ✦  ❈  ✦  ۞",22,gold,true); hero.addView(ornament,lp(-1,40));
            hero.addView(text("نُورُ القُرآن",30,cream,true),lp(-1,48));
            hero.addView(text("وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا",22,0xffe2c98d,true),lp(-1,44));
            hero.addView(text("سكينةٌ للقلب، ونورٌ للطريق",13,muted,false),lp(-1,30));
            View divider=new View(this);divider.setBackgroundColor(0xff7d6840);LinearLayout.LayoutParams dl=lp(100,1);dl.topMargin=8;dl.bottomMargin=10;hero.addView(divider,dl);
            hero.addView(text("مصحفك وقراءك وأذكارك في مكانٍ واحد",12,cream,false),lp(-1,28));
            LinearLayout.LayoutParams hp=lp(-1,-2);hp.bottomMargin=16;home.addView(hero,hp);
            TextView quick=text("ابدأ رحلتك مع القرآن",19,cream,true);quick.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);home.addView(quick,lp(-1,42));
            LinearLayout mushaf=column();mushaf.setGravity(Gravity.CENTER);mushaf.setPadding(16,14,16,14);mushaf.setBackground(shape(0xff203c2e,24,0xffd0ad60));
            mushaf.addView(new OrnamentView(this),lp(-1,30));mushaf.addView(text("المُصْحَفُ الشَّرِيف",24,cream,true),lp(-1,44));mushaf.addView(text("مُصْحَفٌ مَدَنِيٌّ • ٦٠٤ صَفْحَة",13,0xffe3d2a4,true),lp(-1,30));mushaf.addView(text("تتابع من الصفحة "+page+" من 604",14,muted,false),lp(-1,32));
            TextView openMushaf=button("افتح المصحف  ←",()->{tab="المصحف";showApp();});mushaf.addView(openMushaf,lp(-1,48));
            LinearLayout.LayoutParams mp=lp(-1,-2);mp.bottomMargin=12;home.addView(mushaf,mp);
            LinearLayout duo=row();
            LinearLayout reciters=column();reciters.setGravity(Gravity.CENTER);reciters.setPadding(8,14,8,14);reciters.setBackground(shape(0xff20382c,22,0xffb99a59));
            reciters.addView(text("♫",32,gold,true),lp(-1,42));reciters.addView(text("القراء",18,cream,true),lp(-1,32));reciters.addView(text("استمع إلى تلاوات خاشعة",11,muted,false),lp(-1,34));reciters.addView(button("اختر قارئًا",()->{tab="المصحف";showApp();}),lp(-1,44));
            LinearLayout adhkar=column();adhkar.setGravity(Gravity.CENTER);adhkar.setPadding(8,14,8,14);adhkar.setBackground(shape(0xff20382c,22,0xffb99a59));
            adhkar.addView(text("❋",32,gold,true),lp(-1,42));adhkar.addView(text("الأذكار",18,cream,true),lp(-1,32));adhkar.addView(text("أذكار الصباح والمساء",11,muted,false),lp(-1,34));adhkar.addView(button("افتح الأذكار",()->{tab="الأذكار";showApp();}),lp(-1,44));
            duo.addView(reciters,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams gap=lp(10,1);duo.addView(new View(this),gap);duo.addView(adhkar,new LinearLayout.LayoutParams(0,-2,1));home.addView(duo);
            home.addView(new OrnamentView(this),lp(-1,38));home.addView(text("اللَّهُمَّ اجْعَلِ القُرْآنَ رَبِيعَ قُلُوبِنَا",15,0xffffd987,true),lp(-1,44));
            homeScroll.addView(home);body.addView(homeScroll,new LinearLayout.LayoutParams(-1,0,1));
            return;
        }

        if(tab.equals("المصحف")) {
            // Keep the whole Mushaf screen in one vertical scroll container so the
            // page image and recitation controls never compete for the same height.
            ScrollView tabScroll = new ScrollView(this);
            tabScroll.setFillViewport(false);
            tabScroll.setVerticalScrollBarEnabled(true);
            LinearLayout mushafContent = column();
            mushafContent.setPadding(0, 0, 0, 14);
            tabScroll.addView(mushafContent, new ScrollView.LayoutParams(-1, -2));
            body.addView(tabScroll, new LinearLayout.LayoutParams(-1, -1));
            body = mushafContent;
            LinearLayout hero=column(); hero.setPadding(16,14,16,14); hero.setBackground(shape(0xff172c25,24,0xff3c5947));
            LinearLayout heroTop=row(); LinearLayout heroTitles=column(); heroTitles.addView(text("المصحف المدني",21,cream,true),lp(-1,32)); heroTitles.addView(text("متابعة وردك اليومي",12,muted,false),lp(-1,22)); heroTop.addView(heroTitles,new LinearLayout.LayoutParams(0,-2,1)); heroTop.addView(text("604\nصفحة",13,gold,true),lp(68,54)); hero.addView(heroTop);
            View progressTrack=new View(this); progressTrack.setBackground(shape(0xff2d4539,8,0)); LinearLayout.LayoutParams trackLp=lp(-1,5);trackLp.topMargin=12;hero.addView(progressTrack,trackLp);
            TextView progressText=text("الصفحة "+page+" من 604  •  "+Math.round(page*100.0/604)+"٪",12,gold,true); progressText.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);hero.addView(progressText,lp(-1,28)); card(hero);
            LinearLayout actions=row(); actions.setPadding(0,0,0,8);
            actions.addView(button("‹  الصفحة السابقة",()->changePage(-1)),new LinearLayout.LayoutParams(0,-2,1));
            actions.addView(button("الصفحة التالية  ›",()->changePage(1)),new LinearLayout.LayoutParams(0,-2,1)); card(actions);
            LinearLayout marks=row();
            marks.addView(button("☆  حفظ العلامة",()->{prefs.edit().putInt("bookmark",page).apply();Toast.makeText(this,"تم حفظ العلامة عند الصفحة "+page,Toast.LENGTH_SHORT).show();}),new LinearLayout.LayoutParams(0,-2,1));
            marks.addView(button("العودة للعلامة",()->{page=prefs.getInt("bookmark",1);prefs.edit().putInt("page",page).apply();renderTab();}),new LinearLayout.LayoutParams(0,-2,1)); card(marks);
            TextView pageLabel=text("صفحة "+page,14,gold,true); pageLabel.setGravity(Gravity.CENTER); card(pageLabel);
            ImageView mushafPage=new ImageView(this);
            mushafPage.setAdjustViewBounds(false);
            mushafPage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mushafPage.setBackground(shape(cream,8,gold));
            mushafPage.setPadding(4,4,4,4);
            try(java.io.InputStream imageStream=getAssets().open(String.format(Locale.US,"mushaf/%03d.jpg",page))){
                mushafPage.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(imageStream,null));
                LinearLayout.LayoutParams imageParams=lp(-1,460);
                imageParams.bottomMargin=10;
                body.addView(mushafPage,imageParams);
            } catch(Exception imageError) {
                TextView fallback=text("صورة الصفحة غير متوفرة داخل هذه النسخة.",20,0xff243b2f,false);
                fallback.setPadding(18,22,18,22);
                card(fallback);
            }
            TextView sourceNote=text("صفحات المصحف المدني الأصلية محفوظة داخل التطبيق وتعمل دون إنترنت.",11,gold,false); body.addView(sourceNote,lp(-1,-2));
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
            body.addView(button("العودة إلى المصحف",()->{tab="المصحف";showApp();}),lp(-1,54));
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
