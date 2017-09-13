/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CheckBoxSquare;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Arrays;

public class PlusManageTabsActivity extends BaseFragment {

    private RecyclerListView listView;
    private RecyclerListAdapter listAdapter;
    private ArrayList<Integer> tabsArray;

    private boolean needReorder;
    private boolean visibleChanged;

    private int color = Theme.usePlusTheme ? Theme.prefTitleColor : Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
    private int disabledColor = AndroidUtilities.getIntAlphaColor(color, 0.33f);//AndroidUtilities.setDarkColor(Theme.prefTitleColor, 0xaa);

    //private int tabs[] = {0, 1, 2, 3, 4, 5, 6};
    //private int visible[] = {0, 0, 0, 0, 0, 0, 0};
    private int tabs[] = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private int visible[] = {0, 0, 0, 0, 0, 0, 0, -1, -1};

    private ArrayList<Integer> tabs_list;

    private interface ItemTouchHelperAdapter {
        void swapElements(int fromPosition, int toPosition);
        //void onItemDismiss(int position);
    }

    private void storeVisibilityArray(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = plusPreferences.edit();
        mEdit.putString("tabs_visible", Arrays.toString(visible)).apply();
        refreshVisibility();
    }

    private void storeTabsArray(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = plusPreferences.edit();
        mEdit.putString("tabs_array", Arrays.toString(tabs)).apply();
        //Log.e("PlusManageTabs", "storeTabsArray " + Arrays.toString(tabs));
    }

    private void saveArray() {
        storeTabsArray();
        tabs_list = new ArrayList<>();
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = plusPreferences.edit();
        mEdit.remove("tabs_size");
        int size = tabs.length;
        //int k = 0;
        for(int i=0; i< tabs.length ; i++)
        {
            mEdit.remove("tab_" + i);
            if(visibleChanged && visible[i] < 0){
                size--;
                continue;
            }
            //Log.e("PlusManageTabs", "saveArray putInt tab_" + k + "= " + tabs[i]);
            tabs_list.add(tabs[i]);
            //Log.e("PlusManageTabs", "saveArray OUT i " + i + " tabs " + tabs[i] + " Theme.tabType " + Theme.tabType[tabs[i]] +" " + " " + visible[i] + " " + visibleChanged);
        }
        mEdit.commit();
        storeTabsArrayList();
        //Log.e("PlusManageTabs", "saveArray OUT " + size);
        boolean changed = false;
        if(size < 2){
            Theme.plusHideTabs = true;
            changed = true;
        } else{
            if(Theme.plusHideTabs){
                Theme.plusHideTabs = false;
                changed = true;
            }
        }
        if(changed){
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("hideTabs", Theme.plusHideTabs);
            editor.apply();
            //NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 10);
        }
    }

    private void storeTabsArrayList(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = plusPreferences.edit();
        mEdit.putString("tabs_list", tabs_list.toString()).apply();
        //Log.e("PlusManageTabs", "storeTabsArrayList " + plusPreferences.getString("tabs_list", ""));
    }

    private void refreshVisibility(){
        //Log.e("PlusManageTabs", "refreshVisibility " + Arrays.toString(visible));
        for (int i = 0; i < visible.length; i++) {
            int type = Theme.tabType[tabs[i]];
            changeVisibility(type, visible[i] != 0);
            //Log.e("PlusManageTabs", i + " refreshVisibility type " + type);
        }
    }

    private void getVisibilityArray(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        String stringArray = plusPreferences.getString("tabs_visible", null);
        //Log.e("PlusManageTabs", "getVisibilityArray " + Arrays.toString(visible));
        if (stringArray != null) {
            String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
            for (int i = 0; i < split.length; i++) {
                try{
                    String s = split[i];
                    if(s.length() > 0)visible[i] = Integer.parseInt(s);
                } catch (Exception e) {
                    visible[i] = -1;
                    FileLog.e( e);
                }
            }
        }
    }

    private void getTabsArray(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        String stringArray = plusPreferences.getString("tabs_array", null);
        if (stringArray != null) {
            String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
            for (int i = 0; i < split.length; i++) {
                try{
                    String s = split[i];
                    if(s.length() > 0)tabs[i] = Integer.parseInt(s);
                } catch (Exception e) {
                    tabs[i] = i;
                    FileLog.e( e);
                }
            }
        }
        //Log.e("PlusManageTabs", "getTabsArray " + Arrays.toString(tabs));
    }

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
            for (int i = 0; i < plusPreferences.getInt("tabs_size", tabs.length); i++) {
                int p = plusPreferences.getInt("tab_" + i, -1);
                tabs_list.add( p != -1 ? p : i);
            }
        }
        //Log.e("PlusManageTabs", "getTabsArrayList " + tabs_list.toString());
    }

    private void changeVisibility(int type, boolean visible){
        //Log.e("PlusManageTabs", "changeVisibility type " + type + " " + visible);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        switch(type) {
            case 0:
                Theme.plusHideAllTab = visible;
                editor.putBoolean("hideAllTab", visible);
                break;
            case 3:
                Theme.plusHideUsersTab = visible;
                editor.putBoolean("hideUsers", visible);
                break;
            case 4:
                Theme.plusHideGroupsTab = visible;
                editor.putBoolean("hideGroups", visible);
                break;
            case 7:
                Theme.plusHideSuperGroupsTab = visible;
                editor.putBoolean("hideSGroups", visible);
                break;
            case 5:
                Theme.plusHideChannelsTab = visible;
                editor.putBoolean("hideChannels", visible);
                break;
            case 6:
                Theme.plusHideBotsTab = visible;
                editor.putBoolean("hideBots", visible);
                break;
            case 8:
                Theme.plusHideFavsTab = visible;
                editor.putBoolean("hideFavs", visible);
                break;
            case 10:
                if(Theme.plusHideAdminTab != visible) {
                    Theme.plusHideAdminTab = visible;
                    editor.putBoolean("hideAdmin", visible);
                    if(!Theme.plusHideAdminTab) {
                        MessagesController.getInstance().sortDialogs(null);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                    }
                }
                break;
            case 11:
                if(Theme.plusHideUnreadTab != visible) {
                    Theme.plusHideUnreadTab = visible;
                    editor.putBoolean("hideUnread", visible);
                }
                break;
            //default:
                //Theme.plusHideAllTab = visible;
        }
        editor.apply();
    }

    private interface ItemTouchHelperViewHolder {
        void onItemSelected();
        void onItemClear();
    }

    private class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            //mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
            mAdapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            //mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }

    private class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TabCheckCell view = new TabCheckCell(parent.getContext());
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT /*50*/));
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, int position) {
            //holder.textView.setText(mItems.get(position));

            holder.tabCell.setTextAndIcon(Theme.tabTitles[tabs[position]], Theme.tabIcons[tabs[position]]);
            if(visible[position] < 0){
                holder.tabCell.setTextColor(disabledColor);
                holder.tabCell.setIconColor(disabledColor);
                holder.tabCell.setChecked(false);
            } else{
                holder.tabCell.setTextColor(color);
                holder.tabCell.setIconColor(color);
                holder.tabCell.setChecked(true);
            }
        }

        @Override
        public int getItemCount() {
            return tabs.length;//tabsArray.size();
        }

        @Override
        public void swapElements(int fromIndex, int toIndex) {
            if (fromIndex != toIndex) {
                needReorder = true;
            }

            int t2 = tabs[toIndex];
            int t1 = tabs[fromIndex];
            tabs[fromIndex] = t2;
            tabs[toIndex] = t1;
            int v2 = visible[toIndex];
            int v1 = visible[fromIndex];
            if(v1 != v2){
                visibleChanged = true;
            }
            visible[fromIndex] = v2;
            visible[toIndex] = v1;
            notifyItemMoved(fromIndex, toIndex);
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener {

            public final TabCheckCell tabCell;

            ItemViewHolder(View itemView) {
                super(itemView);
                tabCell = (TabCheckCell) itemView;
                if(tabCell.getChildAt(1) instanceof CheckBoxSquare){
                    tabCell.getChildAt(1).setOnClickListener(this);
                }
            }

            @Override
            public void onItemSelected() {
                itemView.setBackgroundColor(Color.LTGRAY);
            }

            @Override
            public void onItemClear() {
                itemView.setBackgroundColor(0);
            }

            @Override
            public void onClick(View view)
            {
                visibleChanged = true;
                if(visible[this.getPosition()] == -1){
                    tabCell.setTextColor(color);
                    tabCell.setIconColor(color);
                    tabCell.setChecked(true);
                    visible[this.getPosition()] = 0;
                } else{
                    tabCell.setTextColor(disabledColor);
                    tabCell.setIconColor(disabledColor);
                    tabCell.setChecked(false);
                    visible[this.getPosition()] = -1;
                }
            }
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        //resetArray();
        if (null == tabsArray) {
            getVisibilityArray();
            tabsArray = new ArrayList<>();
            //loadArray();
            getTabsArray();
            getTabsArrayList();
        }
        //updateRows();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        sendReorder();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setTitle(LocaleController.getString("Tabs", R.string.Tabs));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        //frameLayout.setBackgroundColor(0xfff0f0f0);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xfff0f0f0);
        frameLayout.addView(layout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        //listView.setHasFixedSize(true);
        listView.setFocusable(true);

        if(Theme.usePlusTheme)listView.setBackgroundColor(Theme.prefBGColor);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);

        listAdapter = new RecyclerListAdapter();
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(listAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(listView);

        layout.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0F, Gravity.LEFT ));

        listView.setAdapter(listAdapter);

        TextInfoPrivacyCell textInfoCell = new TextInfoPrivacyCell(context);
        //textInfoCell.setTextSize(15);
        textInfoCell.setText(LocaleController.getString("TabsScreenInfo", R.string.TabsScreenInfo));
        //textInfoCell.setBackgroundColor(color);
        layout.addView(textInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));

        return fragmentView;
    }

    private void sendReorder() {
        //Log.e("PlusManageTabs", "sendReorder needReorder " + needReorder);
        if(visibleChanged){
            storeVisibilityArray();
            needReorder = true;
        }
        if (!needReorder) {
            return;
        }
        needReorder = false;
        saveArray();
        visibleChanged = false;
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        if(Theme.usePlusTheme)updateTheme();
    }

    private void updateTheme(){
        actionBar.setBackgroundColor(Theme.prefActionbarColor);
        actionBar.setTitleColor(Theme.prefActionbarTitleColor);
        Drawable back = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_back);
        back.setColorFilter(Theme.prefActionbarIconsColor, PorterDuff.Mode.MULTIPLY);
        actionBar.setBackButtonDrawable(back);
        actionBar.setItemsColor(Theme.prefActionbarIconsColor, false);
    }

    public class TabCheckCell extends FrameLayout {

        private TextView textView;
        private CheckBoxSquare checkBox;
        private Drawable dRight;
        private boolean show = false;
        //private Paint paint;
        private boolean needDivider = true;

        public TabCheckCell(Context context) {
            super(context);

            /*if (paint == null) {
                paint = new Paint();
                paint.setColor(0xffd9d9d9);
                paint.setStrokeWidth(1);
            }*/

            dRight = getResources().getDrawable(R.drawable.ic_swap_vertical);
            dRight.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            // Read your drawable from somewhere

            Bitmap bitmap = ((BitmapDrawable) dRight).getBitmap();
            dRight = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, AndroidUtilities.dp(30), AndroidUtilities.dp(30), true));

            textView = new TextView(context);
            textView.setTextColor(color);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(15));

            //addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 0, 0, 0, 0));
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 17, 0, 54, 0));

            checkBox = new CheckBoxSquare(context, false);
            checkBox.setDuplicateParentStateEnabled(false);
            checkBox.setFocusable(false);
            checkBox.setFocusableInTouchMode(false);
            checkBox.setClickable(true);
            addView(checkBox, LayoutHelper.createFrame(25, 25, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 5, 20, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
        }

        public void setText(String text) {
            textView.setText(text);
            LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
            layoutParams.height = LayoutParams.MATCH_PARENT;
            layoutParams.topMargin = 0;
            textView.setLayoutParams(layoutParams);
            setWillNotDraw(true);
        }

        public void setChecked(boolean checked) {
            checkBox.setChecked(checked, true);
        }

        public void setTextAndIcon(String text, int resId) {
            try {
                textView.setText(text);
                Drawable d = getResources().getDrawable(resId);
                textView.setCompoundDrawablesWithIntrinsicBounds(d, null, show ? dRight : null, null);
                textView.setPadding(textView.getPaddingLeft(), textView.getPaddingTop(), AndroidUtilities.dp(25), textView.getPaddingBottom());
            } catch (Throwable e) {
                FileLog.e( e);
            }
        }

        public void setTextColor(int color) {
            textView.setTextColor(color);
        }

        public void setIconColor(int color) {
            try {
                Drawable d = textView.getCompoundDrawables()[0];
                d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                textView.setCompoundDrawablesWithIntrinsicBounds(d, null, show ? dRight : null, null);
            } catch (Throwable e) {
                FileLog.e( e);
            }
        }

    }
}
