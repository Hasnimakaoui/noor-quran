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
    private boolean inMushafReading = false;
    private String tab = "الرئيسية";
    private float downX;
    private WebView quranWebView;
    private MediaPlayer player;
    private String selectedEdition="";
    private int reciterScreenStage=0;
    private volatile int playbackRequestId=0;
    private String selectedReciter="عبد الباسط عبد الصمد — مجوّد";
    private int selectedSurah=1;
    private String selectedDhikrCategory = null;
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
        root=column(); root.setBackgroundColor(bg); root.setPadding(12,10,12,8);
        LinearLayout header=row(); header.setPadding(8,8,8,8); header.setGravity(Gravity.CENTER_VERTICAL); header.setBackground(shape(0xff132820,22,0xffb99a59));
        LinearLayout emblem=column(); emblem.setGravity(Gravity.CENTER); TextView brand=text("",24,gold,true); brand.setIncludeFontPadding(false); brand.setBackground(shape(0xff10231c,40,0xffc9a75d)); emblem.addView(brand,lp(40,40)); header.addView(emblem,lp(42,42));
        LinearLayout title=column(); title.setGravity(Gravity.CENTER_VERTICAL); title.setMinimumWidth(0); TextView appTitle=text("نُورُ القُرآن",20,cream,true); appTitle.setSingleLine(true); appTitle.setMaxLines(1); appTitle.setEllipsize(android.text.TextUtils.TruncateAt.END); title.addView(appTitle,lp(-1,28)); TextView appSubtitle=text("المصحف • التلاوة • الأذكار",10,muted,false); appSubtitle.setSingleLine(true); appSubtitle.setEllipsize(android.text.TextUtils.TruncateAt.END); title.addView(appSubtitle,lp(-1,18)); LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(0,46,1); titleParams.leftMargin=6; titleParams.rightMargin=6; header.addView(title,titleParams);
        TextView moon=text("☾",21,gold,true); moon.setBackground(shape(0xff263d34,36,0)); header.addView(moon,lp(36,36)); root.addView(header);
        View line=new View(this); line.setBackgroundColor(0xff263f35); LinearLayout.LayoutParams lineParams=lp(-1,1);lineParams.topMargin=9;root.addView(line,lineParams);
        body=column(); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1); bp.topMargin=8; root.addView(body,bp);
        nav=row(); nav.setBackground(shape(panel,24,0xff344e43)); nav.setPadding(3,4,3,4);
        String[] labels={"الرئيسية","المصحف","القراء","الأذكار"};
        for(String label:labels){ TextView n=text(label,12,label.equals(tab)?gold:muted,label.equals(tab)); n.setOnClickListener(v->{if(label.equals("المصحف"))inMushafReading=false; tab=label;showApp();}); nav.addView(n,new LinearLayout.LayoutParams(0,52,1)); }
        root.addView(nav,lp(-1,58)); setContentView(root); renderTab();
    }
    private void section(String s) { TextView t=text(s,19,cream,true); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setPadding(4,14,4,10); body.addView(t,lp(-1,-2)); }
    private void card(View v) { LinearLayout box=column(); box.setPadding(12,12,12,12); box.setBackground(shape(panel,20,0)); box.addView(v); LinearLayout.LayoutParams p=lp(-1,-2); p.bottomMargin=12; body.addView(box,p); }
    private void renderTab() {
        body.removeAllViews();
        if(tab.equals("الرئيسية")) {
            // Premium, balanced home screen. Keep all decorations inside measured cards
            // and let the scroll area occupy the available body height.
            ScrollView homeScroll=new ScrollView(this);
            homeScroll.setFillViewport(true);
            homeScroll.setClipToPadding(false);
            homeScroll.setVerticalScrollBarEnabled(false);
            LinearLayout home=column();
            home.setPadding(2,8,2,18);

            LinearLayout hero=column();
            hero.setGravity(Gravity.CENTER);
            hero.setPadding(14,16,14,16);
            GradientDrawable heroBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xff294b3b,0xff172f25,0xff0d1d17});
            heroBg.setCornerRadius(26);
            heroBg.setStroke(1,0xffd8b96f);
            hero.setBackground(heroBg);
            // Use a single clean, high-contrast emblem above the title.
            TextView crest=text("✦",30,0xffffd36b,true);
            crest.setGravity(Gravity.CENTER);
            crest.setIncludeFontPadding(false);
            crest.setShadowLayer(2.5f,0,0,0xffd8b96f);
            LinearLayout.LayoutParams crestLp=lp(58,58);
            crestLp.bottomMargin=8;
            hero.addView(crest,crestLp);
            TextView homeTitle=text("نُورُ القُرآن",28,cream,true);
            homeTitle.setGravity(Gravity.CENTER);
            homeTitle.setIncludeFontPadding(true);
            homeTitle.setSingleLine(true);
            homeTitle.setShadowLayer(1.5f,0,1,0xff000000);
            hero.addView(homeTitle,lp(-1,-2));
            TextView verse=text("وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا",19,0xffead49a,true); verse.setSingleLine(false); verse.setMaxLines(2); hero.addView(verse,lp(-1,-2));
            View goldLine=new View(this);
            goldLine.setBackground(shape(0xffb99a59,4,0));
            LinearLayout.LayoutParams goldLineLp=lp(72,2);
            goldLineLp.topMargin=5; goldLineLp.bottomMargin=7;
            hero.addView(goldLine,goldLineLp);
            hero.addView(text("سكينةٌ للقلب، ونورٌ للطريق",12,muted,false),lp(-1,-2));
            LinearLayout.LayoutParams heroLp=lp(-1,-2);
            heroLp.bottomMargin=14;
            home.addView(hero,heroLp);

            TextView sectionTitle=text("بوابتك إلى الطمأنينة",17,cream,true);
            sectionTitle.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            home.addView(sectionTitle,lp(-1,-2));

            LinearLayout mushaf=column();
            mushaf.setGravity(Gravity.CENTER);
            mushaf.setPadding(16,16,16,16);
            mushaf.setBackground(shape(0xff203c2e,22,0xffd0ad60));
            TextView mushafIcon=text("▤",30,gold,true);
            mushafIcon.setGravity(Gravity.CENTER);
            mushafIcon.setBackground(shape(0xff14291f,44,0xff8d7545));
            mushaf.addView(mushafIcon,lp(58,58));
            TextView mushafName=text("المصحف الشريف",20,cream,true);
            mushafName.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams mushafNameLp=lp(-1,-2);
            mushafNameLp.topMargin=8;
            mushaf.addView(mushafName,mushafNameLp);
            TextView mushafDesc=text("قراءة هادئة • 604 صفحة",12,0xffd9cba6,false);
            mushafDesc.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams mushafDescLp=lp(-1,-2);
            mushafDescLp.topMargin=4;
            mushaf.addView(mushafDesc,mushafDescLp);
            TextView openMushaf=button("متابعة القراءة",()->{tab="المصحف";showApp();});
            openMushaf.setPadding(12,8,12,8);
            LinearLayout.LayoutParams openMushafLp=lp(-1,-2);
            openMushafLp.topMargin=12;
            mushaf.addView(openMushaf,openMushafLp);
            LinearLayout.LayoutParams mushafLp=lp(-1,-2);
            mushafLp.topMargin=8; mushafLp.bottomMargin=12;
            home.addView(mushaf,mushafLp);

            // Vertical feature cards: icons, labels and actions each occupy separate rows.
            LinearLayout reciters=column();
            reciters.setGravity(Gravity.CENTER);
            reciters.setPadding(14,14,14,14);
            reciters.setBackground(shape(0xff172e24,20,0xffa98d50));
            TextView reciterIcon=text("♫",24,gold,true);
            reciterIcon.setGravity(Gravity.CENTER);
            reciterIcon.setBackground(shape(0xff10231b,40,0xff685b39));
            reciters.addView(reciterIcon,lp(50,50));
            TextView reciterName=text("مكتبة القراء",17,cream,true);
            reciterName.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams reciterNameLp=lp(-1,-2);
            reciterNameLp.topMargin=7;
            reciters.addView(reciterName,reciterNameLp);
            TextView reciterDesc=text("تلاوات قرآنية",12,muted,false);
            reciterDesc.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams reciterDescLp=lp(-1,-2);
            reciterDescLp.topMargin=3;
            reciters.addView(reciterDesc,reciterDescLp);
            TextView reciterButton=button("استمع الآن",()->{tab="القراء";showApp();});
            reciterButton.setPadding(12,8,12,8);
            LinearLayout.LayoutParams reciterButtonLp=lp(-1,-2);
            reciterButtonLp.topMargin=10;
            reciters.addView(reciterButton,reciterButtonLp);
            LinearLayout.LayoutParams recitersLp=lp(-1,-2);
            recitersLp.topMargin=6;
            recitersLp.bottomMargin=8;
            home.addView(reciters,recitersLp);

            LinearLayout adhkar=column();
            adhkar.setGravity(Gravity.CENTER);
            adhkar.setPadding(14,14,14,14);
            adhkar.setBackground(shape(0xff172e24,20,0xffa98d50));
            TextView adhkarIcon=text("",24,gold,true);
            adhkarIcon.setGravity(Gravity.CENTER);
            adhkarIcon.setBackground(shape(0xff10231b,40,0xff685b39));
            adhkar.addView(adhkarIcon,lp(50,50));
            TextView adhkarName=text("الأذكار",17,cream,true);
            adhkarName.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams adhkarNameLp=lp(-1,-2);
            adhkarNameLp.topMargin=7;
            adhkar.addView(adhkarName,adhkarNameLp);
            TextView adhkarDesc=text("ذكرٌ ودعاء",12,muted,false);
            adhkarDesc.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams adhkarDescLp=lp(-1,-2);
            adhkarDescLp.topMargin=3;
            adhkar.addView(adhkarDesc,adhkarDescLp);
            TextView adhkarButton=button("تصفّح الأذكار",()->{tab="الأذكار";showApp();});
            adhkarButton.setPadding(12,8,12,8);
            LinearLayout.LayoutParams adhkarButtonLp=lp(-1,-2);
            adhkarButtonLp.topMargin=10;
            adhkar.addView(adhkarButton,adhkarButtonLp);
            LinearLayout.LayoutParams adhkarLp=lp(-1,-2);
            adhkarLp.bottomMargin=8;
            home.addView(adhkar,adhkarLp);

            TextView footer=text("اللَّهُمَّ اجْعَلِ القُرْآنَ رَبِيعَ قُلُوبِنَا",13,0xffffd987,true); footer.setSingleLine(false); footer.setMaxLines(2);
            LinearLayout.LayoutParams footerLp=lp(-1,-2);
            footerLp.topMargin=8;
            home.addView(footer,footerLp);
            homeScroll.addView(home,new ScrollView.LayoutParams(-1,-2));
            body.addView(homeScroll,new LinearLayout.LayoutParams(-1,-1));
            return;
        }

        if(tab.equals("المصحف")) {
            if(!inMushafReading) {
                ScrollView indexScroll=new ScrollView(this);
                indexScroll.setFillViewport(true);
                LinearLayout index=column();
                index.setPadding(4,4,4,16);
                section("فهرس سور القرآن الكريم");
                index.addView(button("متابعة القراءة من الصفحة "+page,()->{inMushafReading=true;renderTab();}),lp(-1,48));
                String[] surahs={"الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه","الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم","لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر","فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق","الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج","نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات","القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر","المسد","الإخلاص","الفلق","الناس"};
                int[] startPages={1,2,50,77,106,128,151,177,187,208,221,235,249,255,262,267,282,293,305,312,322,332,342,350,359,367,377,385,396,404,411,415,418,428,434,440,446,453,458,467,477,483,489,496,499,502,507,511,515,518,520,523,526,528,531,534,537,542,545,549,551,553,554,556,558,560,562,564,566,568,570,572,574,575,577,578,580,582,583,585,586,587,588,589,590,591,592,593,594,595,596,597,598,599,600,601,602,603,604,604,604,604,604,604,604,604,604,604,604,604,604,604};
                for(int si=0;si<surahs.length;si++){
                    final int targetPage=startPages[si];
                    TextView item=button((si+1)+"  •  "+surahs[si],()->{page=targetPage;prefs.edit().putInt("page",page).apply();inMushafReading=true;renderTab();});
                    item.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                    LinearLayout.LayoutParams itemLp=lp(-1,48);itemLp.bottomMargin=5;index.addView(item,itemLp);
                }
                indexScroll.addView(index,new ScrollView.LayoutParams(-1,-2));
                body.addView(indexScroll,new LinearLayout.LayoutParams(-1,-1));
                return;
            }
            // Reading-only Mushaf: maximize the page and support finger-swipe page turning.
            LinearLayout reading=column();
            reading.setPadding(0,0,0,4);
            reading.setBackgroundColor(0xff091610);
            body.addView(reading,new LinearLayout.LayoutParams(-1,-1));

            LinearLayout readingHeader=row();
            readingHeader.setPadding(12,4,12,4);
            readingHeader.setBackground(shape(0xff132820,16,0xffb99a59));
            LinearLayout heading=column();
            heading.addView(text("المصحف الشريف",18,cream,true),lp(-1,30));
            heading.addView(text("قراءة فقط • المصحف المدني",11,muted,false),lp(-1,20));
            readingHeader.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
            TextView pageBadge=text(page+" / 604",14,gold,true);
            pageBadge.setBackground(shape(0xff20382c,14,0xff9c824c));
            readingHeader.addView(pageBadge,lp(86,42));
            reading.addView(readingHeader,lp(-1,-2));

            ImageView mushafPage=new ImageView(this);
            mushafPage.setAdjustViewBounds(false);
            mushafPage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mushafPage.setBackground(shape(0xfff8f0d9,8,0xffb99a59));
            mushafPage.setPadding(2,2,2,2);
            mushafPage.setContentDescription("صفحة المصحف "+page+"، اسحب يمينًا أو يسارًا لتقليب الصفحات");
            try(java.io.InputStream imageStream=getAssets().open(String.format(Locale.US,"mushaf/%03d.jpg",page))){
                mushafPage.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(imageStream,null));
            } catch(Exception imageError) {
                mushafPage.setImageDrawable(null);
                mushafPage.setBackground(shape(0xfff8f0d9,8,0xffb99a59));
            }
            LinearLayout.LayoutParams pageImageLp=new LinearLayout.LayoutParams(-1,0,1);
            pageImageLp.topMargin=6; pageImageLp.bottomMargin=6;
            reading.addView(mushafPage,pageImageLp);

            mushafPage.setOnTouchListener((v,event)->{
                if(event.getAction()==MotionEvent.ACTION_DOWN){downX=event.getX();return true;}
                if(event.getAction()==MotionEvent.ACTION_UP){
                    float delta=event.getX()-downX;
                    float threshold=Math.max(55f, getResources().getDisplayMetrics().density*42f);
                    if(Math.abs(delta)>=threshold){
                        // In Arabic reading, swipe right goes to the previous page.
                        changePage(delta>0?-1:1);
                    } else {
                        float x=event.getX();
                        if(x< v.getWidth()*0.28f) changePage(1);
                        else if(x>v.getWidth()*0.72f) changePage(-1);
                    }
                    return true;
                }
                return true;
            });

            LinearLayout pageControls=row();
            pageControls.setGravity(Gravity.CENTER);
            pageControls.setPadding(0,2,0,2);
            TextView previous=button("‹  السابقة",()->changePage(-1));
            TextView next=button("التالية  ›",()->changePage(1));
            pageControls.addView(previous,new LinearLayout.LayoutParams(0,-2,1));
            View controlGap=new View(this); pageControls.addView(controlGap,lp(8,1));
            pageControls.addView(next,new LinearLayout.LayoutParams(0,-2,1));
            reading.addView(pageControls,lp(-1,-2));
            TextView hint=text("اسحب الصفحة بإصبعك للتقليب",10,muted,false);
            reading.addView(hint,lp(-1,22));
        } else if(tab.equals("القراء")) {
            ScrollView reciterScroll=new ScrollView(this);
            reciterScroll.setFillViewport(true);
            LinearLayout reciterContent=column();
            reciterContent.setPadding(0,4,0,18);
            reciterScroll.addView(reciterContent,new ScrollView.LayoutParams(-1,-2));
            body.addView(reciterScroll,new LinearLayout.LayoutParams(-1,-1)); body=reciterContent;
            if(reciterScreenStage==0) {
                section("مكتبة القرّاء");
                reciterContent.addView(button("← العودة إلى الرئيسية",()->{tab="الرئيسية";reciterScreenStage=0;showApp();}),lp(-1,48));
                LinearLayout entry=column(); entry.setGravity(Gravity.CENTER); entry.setPadding(20,24,20,24);
                entry.setBackground(shape(0xff142a20,28,0xffc9a75d));
                TextView icon=text("♫",58,0xffffd36b,true); icon.setGravity(Gravity.CENTER);
                icon.setBackground(shape(0xff203c2e,100,0xffc9a75d));
                LinearLayout.LayoutParams iconLp=lp(144,144); iconLp.bottomMargin=18; entry.addView(icon,iconLp);
                entry.addView(text("اختر القارئ",23,cream,true),lp(-1,-2));
                entry.addView(text("اضغط هنا لاختيار صوت القارئ",14,muted,false),lp(-1,-2));
                LinearLayout.LayoutParams entryLp=lp(-1,-2); entryLp.topMargin=18; reciterContent.addView(entry,entryLp);
                entry.setOnClickListener(v->{reciterScreenStage=1;renderTab();});
            } else if(reciterScreenStage==1) {
                section("اختر القارئ");
                reciterContent.addView(button("← رجوع",()->{reciterScreenStage=0;renderTab();}),lp(-1,48));
                String[] reciterNames={"عبد الباسط عبد الصمد — مجوّد","أحمد العجمي","عبد الله خلف","محمد اللحيدان"};
                String[] editions={"عبد الباسط عبد الصمد","أحمد العجمي","عبد الله الخلف","محمد اللحيدان"};
                String[] symbols={"١","٢","٣","٤"};
                for(int ri=0;ri<reciterNames.length;ri++){
                    final int chosen=ri;
                    LinearLayout item=column(); item.setGravity(Gravity.CENTER); item.setPadding(16,16,16,16);
                    item.setBackground(shape(0xff172e24,22,0xffa98d50));
                    TextView portrait=text(symbols[ri],38,gold,true); portrait.setBackground(shape(0xff10231b,70,0xff685b39));
                    LinearLayout.LayoutParams portraitLp=lp(84,84); portraitLp.bottomMargin=8; item.addView(portrait,portraitLp);
                    item.addView(text(reciterNames[ri],18,cream,true),lp(-1,-2));
                    LinearLayout.LayoutParams itemLp=lp(-1,-2); itemLp.bottomMargin=12; reciterContent.addView(item,itemLp);
                    item.setOnClickListener(v->{selectedEdition=editions[chosen];selectedReciter=reciterNames[chosen];reciterScreenStage=2;renderTab();});
                }
            } else {
                section("اختر السورة للاستماع");
                reciterContent.addView(button("← تغيير القارئ: "+selectedReciter,()->{reciterScreenStage=1;renderTab();}),lp(-1,-2));
                audioStatus=text("اختر صورة السورة لبدء الاستماع بصوت "+selectedReciter,13,muted,false);
                card(audioStatus);
                String[] surahs={"الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه","الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم","لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر","فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق","الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج","نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات","القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر","المسد","الإخلاص","الفلق","الناس"};
                for(int si=0;si<surahs.length;si++){
                    final int chosenSurah=si+1;
                    LinearLayout tile=column(); tile.setGravity(Gravity.CENTER); tile.setPadding(10,14,10,14);
                    tile.setBackground(shape(0xff142a20,18,0xff526c54));
                    TextView picture=text("",28,0xffffd36b,true); picture.setBackground(shape(0xff203c2e,50,0xffa98d50));
                    LinearLayout.LayoutParams pictureLp=lp(66,66); pictureLp.bottomMargin=6; tile.addView(picture,pictureLp);
                    tile.addView(text(surahs[si],15,cream,true),lp(-1,-2));
                    LinearLayout.LayoutParams tileLp=lp(-1,-2); tileLp.bottomMargin=8; reciterContent.addView(tile,tileLp);
                    tile.setOnClickListener(v->{selectedSurah=chosenSurah;playSurah();});
                }
            }        } else if(tab.equals("الأذكار")) {
            ScrollView adhkarScroll = new ScrollView(this);
            LinearLayout adhkarContent = column();
            adhkarContent.setPadding(0,0,0,16);
            adhkarScroll.addView(adhkarContent,new ScrollView.LayoutParams(-1,-2));
            body.addView(adhkarScroll,new LinearLayout.LayoutParams(-1,-1));
            body=adhkarContent;
            JSONObject book=loadHisnBook();
            if(book==null) {
                section("تعذر تحميل كتاب حصن المسلم");
                card(text("تعذر فتح ملف الأذكار داخل التطبيق.",15,muted,false));
            } else if(selectedDhikrCategory!=null && book.has(selectedDhikrCategory)) {
                LinearLayout top=row();
                top.addView(button("→ جميع الأقسام",()->{selectedDhikrCategory=null;renderTab();}),lp(-2,48));
                top.addView(text(selectedDhikrCategory,18,gold,true),new LinearLayout.LayoutParams(0,-2,1));
                card(top);
                JSONObject category=book.optJSONObject(selectedDhikrCategory);
                JSONArray texts=category==null?null:category.optJSONArray("text");
                JSONArray notes=category==null?null:category.optJSONArray("footnote");
                if(texts!=null) for(int i=0;i<texts.length();i++) {
                    final int itemIndex=i;
                    LinearLayout item=column(); item.setPadding(14,14,14,14); item.setBackground(shape(0xff142820,18,0xff536b50));
                    TextView dhikr=text(texts.optString(i),19,cream,false);
                    dhikr.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); dhikr.setTextDirection(View.TEXT_DIRECTION_RTL); dhikr.setPadding(6,8,6,12);
                    item.addView(dhikr,lp(-1,-2));
                    if(notes!=null && i<notes.length() && !notes.optString(i).trim().isEmpty()) {
                        TextView note=text("المصدر / الحاشية: "+notes.optString(i),11,muted,false);
                        note.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); note.setTextDirection(View.TEXT_DIRECTION_RTL); item.addView(note,lp(-1,-2));
                    }
                    LinearLayout.LayoutParams itemLp=lp(-1,-2); itemLp.bottomMargin=12; body.addView(item,itemLp);
                    item.setOnLongClickListener(v->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("الذكر",texts.optString(itemIndex)));Toast.makeText(this,"تم نسخ الذكر",Toast.LENGTH_SHORT).show();return true;});
                }
            } else {
                section("حصن المسلم كاملًا");
                card(text("جميع أقسام الكتاب ونصوص الأذكار والحواشي المتاحة محفوظة داخل التطبيق وتعمل دون إنترنت. اضغط مطولًا على الذكر لنسخه.",14,muted,false));
                EditText search=new EditText(this); search.setSingleLine(true); search.setTextColor(cream); search.setHintTextColor(muted); search.setHint("ابحث عن قسم أو ذكر..."); search.setTextSize(16); search.setPadding(14,10,14,10); search.setBackground(shape(panel,18,gold)); card(search);
                LinearLayout categories=column(); body.addView(categories,lp(-1,-2));
                java.util.ArrayList<String> names=new java.util.ArrayList<>();
                java.util.Iterator<String> keys=book.keys(); while(keys.hasNext()) names.add(keys.next());
                java.util.Collections.sort(names,(a,b)->{if(a.equals("المقدمة"))return -1;if(b.equals("المقدمة"))return 1;if(a.equals("فضل الذكر"))return -1;if(b.equals("فضل الذكر"))return 1;return a.compareTo(b);});
                Runnable refreshCategories=()->{
                    categories.removeAllViews(); String query=search.getText().toString().trim(); int shown=0;
                    for(String name:names) {
                        JSONObject obj=book.optJSONObject(name); JSONArray arr=obj==null?null:obj.optJSONArray("text");
                        boolean match=query.isEmpty()||name.contains(query);
                        if(!match && arr!=null) for(int j=0;j<arr.length();j++) if(arr.optString(j).contains(query)){match=true;break;}
                        if(!match) continue;
                        int total=arr==null?0:arr.length();
                        TextView cat=button(name+"  •  "+total+" نصوص  ←",()->{selectedDhikrCategory=name;renderTab();});
                        LinearLayout.LayoutParams catLp=lp(-1,-2);catLp.bottomMargin=8;categories.addView(cat,catLp); shown++;
                    }
                    if(shown==0) categories.addView(text("لا توجد نتائج مطابقة.",14,muted,false),lp(-1,50));
                };
                refreshCategories.run();
                search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){} public void onTextChanged(CharSequence s,int start,int before,int count){refreshCategories.run();} public void afterTextChanged(android.text.Editable e){}});
            }
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


    private JSONObject loadHisnBook() {
        try (java.io.InputStream in=getAssets().open("hisn_almuslim.json"); java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192]; int n; while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
            return new JSONObject(new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));
        } catch(Exception e) { return null; }
    }

    private void playSurah() {
        final int requestId=++playbackRequestId;
        stopAudio();
        if(audioStatus!=null) audioStatus.setText("جارٍ البحث عن التلاوة...");
        final String wanted=selectedEdition;
        final int surah=selectedSurah;
        final String reciterLabel=selectedReciter;
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
                if(requestId!=playbackRequestId) return;
                JSONObject rootJson=new JSONObject(jsonText.toString());
                JSONArray reciters=rootJson.optJSONArray("reciters");
                String streamUrl=null;
                if(reciters!=null) for(int i=0;i<reciters.length()&&streamUrl==null;i++){
                    if(requestId!=playbackRequestId) return;
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
                        if(server.length()>0&&(available.isEmpty()||(","+available+",").contains(","+surah+","))){
                            if(!server.endsWith("/")) server+="/";
                            streamUrl=server+String.format(Locale.US,"%03d",surah)+".mp3";
                            break;
                        }
                    }
                }
                if(streamUrl==null) throw new Exception("لم نعثر على تلاوة متاحة لهذا القارئ");
                final String audioUrl=streamUrl;
                runOnUiThread(()->{
                    if(requestId!=playbackRequestId) return;
                    if(audioStatus!=null) audioStatus.setText("جارٍ تشغيل "+reciterLabel+"...");
                    try {
                        MediaPlayer mp=new MediaPlayer();
                        mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                        mp.setDataSource(audioUrl);
                        mp.setOnPreparedListener(p->{
                            if(requestId!=playbackRequestId){p.release();return;}
                            player=p;
                            p.start();
                            if(audioStatus!=null) audioStatus.setText("يعمل الآن: "+reciterLabel);
                        });
                        mp.setOnErrorListener((p,what,extra)->{
                            if(requestId==playbackRequestId&&audioStatus!=null) audioStatus.setText("تعذر تشغيل ملف التلاوة لهذا القارئ.");
                            if(player==p) player=null;
                            p.release(); return true;
                        });
                        player=mp;
                        mp.prepareAsync();
                    } catch(Exception ex) {
                        if(requestId==playbackRequestId&&audioStatus!=null) audioStatus.setText("تعذر تشغيل التلاوة.");
                    }
                });
            } catch(Exception ex) {
                runOnUiThread(()->{
                    if(requestId==playbackRequestId&&audioStatus!=null) audioStatus.setText("لم أتمكن من تحميل قائمة التلاوات. تحقق من الإنترنت وحاول مجدداً.");
                });
            } finally { if(connection!=null) connection.disconnect(); }
        }).start();
    }
    private void stopAudio() {
        try { if(player!=null){ if(player.isPlaying()) player.stop(); player.release(); player=null; } } catch(Exception ignored) { player=null; }
        if(audioStatus!=null) audioStatus.setText("تم إيقاف التلاوة");
    }
    @Override protected void onDestroy(){playbackRequestId++;stopAudio();super.onDestroy();}
    private void changePage(int delta) { page=Math.max(1,Math.min(604,page+delta)); prefs.edit().putInt("page",page).apply(); renderTab(); }
}
