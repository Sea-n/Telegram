package org.telegram.ui.Components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

import static android.R.id.tabs;

/**
 * Created by Sergio on 15/11/2016.
 */

public class TabsView extends FrameLayout implements NotificationCenter.NotificationCenterDelegate{

    private static final String TAG = "TabsView";
    private LinearLayout tabsContainer;
    private PlusPagerSlidingTabStrip pagerSlidingTabStrip;

    private ViewPager pager;
    private int currentPage;
    private ArrayList<Tab> tabsArray;
    private ArrayList<Integer> tabs_list;
    //private boolean isLayout;
    private boolean force;

    class Tab {
        private final String title;
        private final int res;
        private final int type;
        private int position; //0:All 1:Users 2:Groups 3:SuperGroups 4:Channels 5:Bots 6:Favs(8) 7:Groups+SuperGroups(9) 8:Creator(10) 9:Unread(11)
        private int unread;

        Tab(String title, int res, int type, int position) {
            this.title = title;
            this.res = res;
            this.type = type;
            this.position = position;
            this.unread = 0;
        }

        public String getTitle(){
            return this.title;
        }

        public int getRes(){
            return this.res;
        }

        public int getType(){
            //Log.e(TAG, "this.type " + this.type);
            return this.type;
        }

        public int getPosition(){
            return this.position;
        }

        public int getUnread(){
            return this.unread;
        }

        public void setUnread(int unread){
            this.unread = unread;
        }
    }

    public interface Listener {
        void onPageSelected(int position, int type);
        void onTabLongClick(int position, int type);
        void refresh(boolean bool);
        void onTabClick();
    }

    private Listener listener;

    //private int positions[] = {-1, -1, -1, -1, -1, -1, -1};
    private int positions[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1};

    //private static final int tabCount = 7;
    private static final int tabCount = 9;

    private void getTabsArrayList(){
        tabs_list = new ArrayList<>();
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        String stringArray = plusPreferences.getString("tabs_list", null);
        if (stringArray != null) {
            String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
            for (int i = 0; i < split.length; i++) {
                try{
                    String s = split[i];
                    if(s.length() > 0)tabs_list.add(Integer.parseInt(s));
                } catch (Exception e) {
                    FileLog.e( e);
                }
            }
        } else{
            for (int i = 0; i < plusPreferences.getInt("tabs_size", tabCount); i++) {
                    if(i == 0){
                        if(!Theme.plusHideAllTab)tabs_list.add(i);
                    } else if(i == 1){
                        if(!Theme.plusHideUsersTab)tabs_list.add(i);
                    } else if(i == 2){
                        if(!Theme.plusHideGroupsTab)tabs_list.add(i);
                    } else if(i == 3){
                        if(!Theme.plusHideSuperGroupsTab)tabs_list.add(i);
                    } else if(i == 4){
                        if(!Theme.plusHideChannelsTab)tabs_list.add(i);
                    } else if(i == 5){
                        if(!Theme.plusHideBotsTab)tabs_list.add(i);
                    } else if(i == 6){
                        if(!Theme.plusHideFavsTab)tabs_list.add(i);
                    } else if(i == 7){
                        if(!Theme.plusHideAdminTab)tabs_list.add(i);
                    } else if(i == 8){
                        if(!Theme.plusHideUnreadTab)tabs_list.add(i);
                    }
            }
            storeTabsArrayList();
        }
        //Log.e("TabsView", "getTabsArrayList " + tabs_list.toString());
    }

    private void storeTabsArrayList(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = plusPreferences.edit();
        mEdit.putString("tabs_list", tabs_list.toString()).apply();
    }

    private void loadArray() {
        getTabsArrayList();
        tabsArray.clear();
        int size = tabs_list.size();
        for(int i=0; i < size; i++) {
            int p = tabs_list.get(i);
            int type = Theme.tabType[p];
            if(type == 4 && !tabs_list.contains(3))type = 9;
            tabsArray.add(new Tab(Theme.tabTitles[p], Theme.tabIcons[p], type, p));
            positions[p] = i;
            //Log.e("TabsView", i + " loadArray " + p + " Theme.tabType[p] " + type + " positions[p] " + positions[p] + " plusHideSuperGroupsTab " + Theme.plusHideSuperGroupsTab);
        }
        if(size < 2 && !Theme.plusHideTabs){
            Theme.plusHideTabs = true;
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("hideTabs", Theme.plusHideTabs);
            editor.apply();
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 10);
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("TabsWarningMsg", R.string.TabsWarningMsg), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        FileLog.e( e);
                    }
                }
            });
        }
        pager.setAdapter(null);
        pager.setOffscreenPageLimit(size); // fixes bug with Nexus 5 6.0.1 and infinite scroll
        pager.setAdapter(new TabsAdapter());
        //Log.e(TAG, "OUT loadArray size " + tabsArray.size() + " Theme.plusDialogType " + Theme.plusDialogType + " Theme.plusSelectedTab " + Theme.plusSelectedTab);
        updatePagerItem();
    }

    public void reloadTabs(){
        //Log.e(TAG, "reloadTabs");
        loadArray();
        pager.getAdapter().notifyDataSetChanged();
    }

    public void updateTabsColors(){
        if(tabsContainer != null){
            paintTabs();
        }
        if(pagerSlidingTabStrip != null){
            pagerSlidingTabStrip.notifyDataSetChanged();
        }
    }

    private void paintTabs(){
        if(Theme.usePlusTheme) {
            tabsContainer.setBackgroundColor(Theme.chatsTabsBGColor == Theme.defColor ? Theme.chatsHeaderColor : Theme.chatsTabsBGColor);
            int val = Theme.chatsHeaderGradient;
            if (val > 0) {
                GradientDrawable.Orientation go;
                switch (val) {
                    case 2:
                        go = GradientDrawable.Orientation.LEFT_RIGHT;
                        break;
                    case 3:
                        go = GradientDrawable.Orientation.TL_BR;
                        break;
                    case 4:
                        go = GradientDrawable.Orientation.BL_TR;
                        break;
                    default:
                        go = GradientDrawable.Orientation.TOP_BOTTOM;
                }
                int gradColor = Theme.chatsHeaderGradientColor;
                int[] colors = new int[]{Theme.chatsHeaderColor, gradColor};
                GradientDrawable gd = new GradientDrawable(go, colors);
                if (Theme.chatsTabsBGColor == Theme.defColor)
                    tabsContainer.setBackgroundDrawable(gd);
            }
        } else{
            tabsContainer.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        }
    }

    private void updatePagerItem(){
        int size = tabsArray.size();
        //Log.e(TAG, "updatePagerItem plusDialogType " + Theme.plusDialogType + " plusSelectedTab " + Theme.plusSelectedTab);
        Theme.plusDialogType = Theme.plusHideTabs ? 0 : tabsArray.get(size > Theme.plusSelectedTab ? Theme.plusSelectedTab : size - 1).getType();
        if(Theme.plusDialogType == 4 && !tabs_list.contains(3)){
            Theme.plusDialogType = 9;
        }
        currentPage = Theme.plusSelectedTab;
        pager.setCurrentItem(currentPage);
    }

    public TabsView(Context context) {
        super(context);
        pager = new ViewPager(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };

        if (null == tabsArray) {
            tabsArray = new ArrayList<>();
            loadArray();
        }

        tabsContainer = new LinearLayout(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);

        paintTabs();
        addView(tabsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        pagerSlidingTabStrip = new PlusPagerSlidingTabStrip(context);
        pagerSlidingTabStrip.setShouldExpand(Theme.plusTabsShouldExpand);
        pagerSlidingTabStrip.setViewPager(pager);
        pagerSlidingTabStrip.setIndicatorHeight(AndroidUtilities.dp(3));

        pagerSlidingTabStrip.setDividerColor(0x00000000);
        pagerSlidingTabStrip.setUnderlineHeight(0);
        pagerSlidingTabStrip.setUnderlineColor(/*0xffe2e5e7*/0x00000000);

        tabsContainer.addView(pagerSlidingTabStrip, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));

        pagerSlidingTabStrip.setDelegate(new PlusPagerSlidingTabStrip.PlusScrollSlidingTabStripDelegate() {
            @Override
            public void onTabLongClick(int position) {
                //Log.e("TabsView", "onTabLongClick position " + position + " plusSelectedTab " + Theme.plusSelectedTab + " currentPage " + currentPage);
                if(Theme.plusSelectedTab == position) {
                    if (listener != null) {
                        listener.onTabLongClick(position, tabsArray.get(position).getType());
                    }
                }
            }

            @Override
            public void onTabsUpdated() {
                forceUpdateTabCounters();
                unreadCount();
            }

            @Override
            public void onTabClick() {
                if (listener != null) {
                    listener.onTabClick();
                }
            }
        });

        pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private boolean loop;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.e(TAG, "onPageScrolled " + position + " positionOffset " + positionOffset + " currentPage " + currentPage);
            }

            @Override
            public void onPageSelected(int position) {
                //Log.e(TAG,"onPageSelected " + position + " tabsArray.get(position).getType() " + tabsArray.get(position).getType());
                if (listener != null) {
                    listener.onPageSelected(position, tabsArray.get(position).getType());
                }
                currentPage = position;
                saveNewPage();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.e(TAG, "onPageScrollStateChanged " + state + " currentPage " + currentPage);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if(loop){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                pager.setCurrentItem(currentPage == 0 ? pager.getAdapter().getCount() - 1 : 0);
                            }
                        }, 100);
                        //Handler h = new Handler();
                        //h.postDelayed(new Runnable() {
                        //    @Override
                        //    public void run() {
                        //        pager.setCurrentItem(currentPage == 0 ? pager.getAdapter().getCount() - 1 : 0);
                        //    }
                        //}, 100);
                        loop = false;
                    }
                } else if (state == 1) {
                    loop = Theme.plusInfiniteTabsSwipe && (currentPage == 0 || currentPage == pager.getAdapter().getCount() - 1);
                } else if (state == 2) {
                    loop = false;
                }
            }
        });

        addView(pager, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        //Log.e(TAG, "TabsView currentPage " + currentPage);
        forceUpdateTabCounters();
        unreadCount();
    }

    private void saveNewPage() {
        Theme.plusSelectedTab = currentPage;
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.putInt("selectedTab", Theme.plusSelectedTab);
        Theme.plusDialogType = tabsArray.get(Theme.plusSelectedTab).getType();
        editor.putInt("dialogType", Theme.plusDialogType);
        editor.apply();
    }

    public ViewPager getPager(){
        return pager;
    }

    public void setListener(Listener value) {
        listener = value;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabsCounters);
        //Log.e(TAG, "onDetachedFromWindow");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabsCounters);
        //Log.e(TAG, "onAttachedToWindow");
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.refreshTabsCounters) {
            if(!Theme.plusHideTabs && tabsArray != null && tabsArray.size() > 1){
                unreadCount();
            }
        }
    }

    public void forceUpdateTabCounters(){
        force = true;
    }

    private void unreadCount(){
        if(!Theme.plusHideUnreadTab)unreadCount(MessagesController.getInstance().dialogsUnread, positions[8]);
        if(!Theme.plusHideAdminTab)unreadCount(MessagesController.getInstance().dialogsAdmin, positions[7]);
        if(!Theme.plusHideFavsTab)unreadCount(MessagesController.getInstance().dialogsFavs, positions[6]);
        if(!Theme.plusHideBotsTab)unreadCount(MessagesController.getInstance().dialogsBots, positions[5]);
        if(!Theme.plusHideChannelsTab)unreadCount(MessagesController.getInstance().dialogsChannels, positions[4]);
        unreadCountGroups();
        if(!Theme.plusHideUsersTab)unreadCount(MessagesController.getInstance().dialogsUsers, positions[1]);
        if(!Theme.plusHideAllTab)unreadCountAll(MessagesController.getInstance().dialogs, positions[0]);
    }

    private void unreadCountGroups(){
        if(!Theme.plusHideGroupsTab)unreadCount(!Theme.plusHideSuperGroupsTab ? MessagesController.getInstance().dialogsGroups : MessagesController.getInstance().dialogsGroupsAll, positions[2]);
        if(!Theme.plusHideSuperGroupsTab)unreadCount(MessagesController.getInstance().dialogsMegaGroups, positions[3]);
    }

    private void unreadCount(final ArrayList<TLRPC.TL_dialog> dialogs, int position){
        //Log.e("TabsView", "unreadCount position " + position);
        if(position == -1){
            return;
        }
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        boolean allMuted = true;
        int unreadCount = 0;
        if (dialogs != null && !dialogs.isEmpty()) {
            for(int a = 0; a < dialogs.size(); a++) {
                TLRPC.TL_dialog dialg = dialogs.get(a);
                if(dialg != null && dialg.unread_count > 0) {
                    boolean isMuted = MessagesController.getInstance().isDialogMuted(dialg.id);
                    if (!isMuted || !Theme.plusTabsCountersCountNotMuted) {
                        int i = dialg.unread_count;
                        if (i == 0 && plusPreferences.getInt("unread_" + dialg.id, 0) == 1) i = 1;
                        if (i > 0) {
                            if (Theme.plusTabsCountersCountChats) {
                                if (i > 0) unreadCount = unreadCount + 1;
                            } else {
                                unreadCount = unreadCount + i;
                            }
                            if (i > 0 && !isMuted) allMuted = false;
                        }
                    }
                }
            }
        }

        //Log.e("TabsView", "unreadCount position " + position + " unreadCount " + unreadCount + " allMuted " + allMuted);
        if(unreadCount != tabsArray.get(position).getUnread() || force) {
            //Log.e("TabsView", "unreadCount YES ");
            tabsArray.get(position).setUnread(unreadCount);
            pagerSlidingTabStrip.updateCounter(position, unreadCount, allMuted, force);
        }
    }

    private void unreadCountAll(ArrayList<TLRPC.TL_dialog> dialogs, int position){
        //Log.e("TabsView", "unreadCountAll position " + position);
        if(position == -1){
            return;
        }
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);

        boolean allMuted = true;
        int unreadCount = 0;

                if (dialogs != null && !dialogs.isEmpty()) {
                    for(int a = 0; a < dialogs.size(); a++) {
                        TLRPC.TL_dialog dialg = dialogs.get(a);
                        if(dialg != null && dialg.unread_count > 0) {
                            boolean isMuted = MessagesController.getInstance().isDialogMuted(dialg.id);
                            if (!isMuted || !Theme.plusTabsCountersCountNotMuted) {
                                int i = dialg.unread_count;
                                if (i == 0 && plusPreferences.getInt("unread_" + dialg.id, 0) == 1) i = 1;
                                if (i > 0) {
                                    if (Theme.plusTabsCountersCountChats) {
                                        if (i > 0) unreadCount = unreadCount + 1;
                                    } else {
                                        unreadCount = unreadCount + i;
                                    }
                                    if (i > 0 && !isMuted) allMuted = false;
                                }
                            }
                        }

                    }
                }

        //Log.e("TabsView", "unreadCountAll allPos " + position + " unreadCount " + unreadCount + " allMuted " + allMuted);
        if(unreadCount != tabsArray.get(position).getUnread() || force) {
            //Log.e("TabsView", "unreadCountAll YES ");
            tabsArray.get(position).setUnread(unreadCount);
            pagerSlidingTabStrip.updateCounter(position, unreadCount, allMuted, force);
            force = false;
        }

    }

    private class TabsAdapter extends PagerAdapter implements PlusPagerSlidingTabStrip.IconTabProvider {

        ///private Context context;

        //private TabsAdapter(Context context) {
        //    this.context = context;
        //}

        @Override
        public int getCount() {
            return tabsArray.size();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if(pagerSlidingTabStrip != null){
                pagerSlidingTabStrip.notifyDataSetChanged();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            View view = new View(viewGroup.getContext());
            viewGroup.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup viewGroup, int position, Object object) {
            viewGroup.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public int getPageIconResId(int position) {
            return tabsArray.get(position).getRes();
        }

        @Override
        public String getPageTitle(int position) {
            return tabsArray.get(position).getTitle();
        }
    }

}
