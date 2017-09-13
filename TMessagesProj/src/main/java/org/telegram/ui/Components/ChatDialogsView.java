package org.telegram.ui.Components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ChatDialogsView extends FrameLayout {

    private RecyclerListView listView;
    private RecyclerView.Adapter dialogsAdapter;
    private RecyclerView.Adapter membersAdapter;
    private ImageView btn;
    private TextView tv;
    private ChatActivity parentFragment;
    private LinearLayoutManager layoutManager;
    private boolean visible;
    private boolean refresh;
    private int textSize = 10;
    private int avatarSize = 40;
    private int listWidth = avatarSize + 20;
    private int listHeight = avatarSize + textSize + 25;
    private int dialogsType;
    private int chat_id;

    private boolean vertical;
    private boolean showMembers;

    private boolean disableLongCick;

    private ChatDialogsViewDelegate delegate;

    private TLRPC.ChatFull info;
    private ArrayList<Integer> sortedUsers;
    private TLRPC.Chat currentChat;
    private int creatorID;
    private int membersCount;
    private boolean loadingUsers;
    //private HashMap<Integer, TLRPC.ChatParticipant> participantsMap;
    private ArrayList<Integer> membersMap;
    //private boolean usersEndReached;
    private int classGuid;
    private int loadMoreMembersRow;

    public void setChatInfo(TLRPC.ChatFull chatInfo) {
        if(!showMembers)return;
        info = chatInfo;
        if(currentChat == null)currentChat = parentFragment.getCurrentChat();
        if (currentChat.megagroup) {
                //if(membersCount == info.participants_count)return;
                membersCount = info.participants_count;
                fetchUsersFromChannelInfo();
        } else {
                //if(membersCount == info.participants.participants.size())return;
                membersCount = info.participants.participants.size();
        }
        if(membersCount <= 1){
            showMembers = false;
            if (listView.getAdapter() != dialogsAdapter) {
                listView.setAdapter(dialogsAdapter);
                dialogsAdapter.notifyDataSetChanged();
            }
        }
        //Log.e("ChatDialogsView", "setChatInfo " + membersCount + " showMembers " + showMembers);
        updateOnlineCount();
    }

    private void getChannelParticipants(boolean reload) {
        //Log.e("ChatDialogsView", "0 getChannelParticipants reload " + reload + " chat_id " + chat_id);
        if (loadingUsers || membersMap == null || info == null) {
            return;
        }
        //Log.e("ChatDialogsView", "1 getChannelParticipants reload " + reload );
        loadingUsers = true;
        final int delay = !membersMap.isEmpty() && reload ? 300 : 0;

        final TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
        req.channel = MessagesController.getInputChannel(chat_id);
        req.filter = new TLRPC.TL_channelParticipantsRecent();
        req.offset = reload ? 0 : membersMap.size();
        req.limit = 200;
        int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_channels_channelParticipants res = (TLRPC.TL_channels_channelParticipants) response;
                            MessagesController.getInstance().putUsers(res.users, false);
                            if (req.offset == 0) {
                                membersMap.clear();
                                info.participants = new TLRPC.TL_chatParticipants();
                                MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                                MessagesStorage.getInstance().updateChannelUsers(chat_id, res.participants);
                            }
                            for (int a = 0; a < res.participants.size(); a++) {
                                TLRPC.TL_chatChannelParticipant participant = new TLRPC.TL_chatChannelParticipant();
                                participant.channelParticipant = res.participants.get(a);
                                participant.inviter_id = participant.channelParticipant.inviter_id;
                                participant.user_id = participant.channelParticipant.user_id;
                                participant.date = participant.channelParticipant.date;
                                if (!membersMap.contains(participant.user_id)) {
                                    info.participants.participants.add(participant);
                                    membersMap.add(participant.user_id);
                                }
                            }
                        }
                        updateOnlineCount();
                        loadingUsers = false;
                        if (info != null && info.participants != null && !info.participants.participants.isEmpty()) {
                            if (membersMap.size() > loadMoreMembersRow) {
                                loadMoreMembersRow = info.participants.participants.size();
                            }
                        }
                        if (listView.getAdapter() != null) {
                            listView.getAdapter().notifyDataSetChanged();
                        }
                    }
                }, delay);
            }
        });
        ConnectionsManager.getInstance().bindRequestToGuid(reqId, classGuid);
    }

    private void fetchUsersFromChannelInfo() {
        if (info instanceof TLRPC.TL_channelFull && info.participants != null) {
            for (int a = 0; a < info.participants.participants.size(); a++) {
                TLRPC.ChatParticipant chatParticipant = info.participants.participants.get(a);
                if(((TLRPC.TL_chatChannelParticipant) chatParticipant).channelParticipant instanceof TLRPC.TL_channelParticipantCreator){
                    creatorID = chatParticipant.user_id;
                }
            }
        }
    }

    private void updateOnlineCount() {
        //onlineCount = 0;
        int currentTime = ConnectionsManager.getInstance().getCurrentTime();
        sortedUsers.clear();
        if (info instanceof TLRPC.TL_chatFull || info instanceof TLRPC.TL_channelFull && info.participants_count <= 200 && info.participants != null) {
            for (int a = 0; a < info.participants.participants.size(); a++) {
                TLRPC.ChatParticipant participant = info.participants.participants.get(a);
                TLRPC.User user = MessagesController.getInstance().getUser(participant.user_id);
                if (user != null && user.status != null && (user.status.expires > currentTime || user.id == UserConfig.getClientUserId()) && user.status.expires > 10000) {
                    //onlineCount++;
                }
                sortedUsers.add(a);
                if (participant instanceof TLRPC.TL_chatParticipantCreator)creatorID = participant.user_id;
            }

            try {
                Collections.sort(sortedUsers, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer lhs, Integer rhs) {
                        TLRPC.User user1 = MessagesController.getInstance().getUser(info.participants.participants.get(rhs).user_id);
                        TLRPC.User user2 = MessagesController.getInstance().getUser(info.participants.participants.get(lhs).user_id);
                        int status1 = 0;
                        int status2 = 0;
                        if (user1 != null && user1.status != null) {
                            if (user1.id == UserConfig.getClientUserId()) {
                                status1 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                            } else {
                                status1 = user1.status.expires;
                            }
                            //Plus admin
                            if (user1.id == creatorID) {
                                status1 = ConnectionsManager.getInstance().getCurrentTime() + 50000 - 100;
                            }
                        }
                        if (user2 != null && user2.status != null) {
                            if (user2.id == UserConfig.getClientUserId()) {
                                status2 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                            } else {
                                status2 = user2.status.expires;
                            }
                            //Plus admin
                            if (user2.id == creatorID) {
                                status2 = ConnectionsManager.getInstance().getCurrentTime() + 50000 - 100;
                            }
                        }
                        if (status1 > 0 && status2 > 0) {
                            if (status1 > status2) {
                                return 1;
                            } else if (status1 < status2) {
                                return -1;
                            }
                            return 0;
                        } else if (status1 < 0 && status2 < 0) {
                            if (status1 > status2) {
                                return 1;
                            } else if (status1 < status2) {
                                return -1;
                            }
                            return 0;
                        } else if (status1 < 0 && status2 > 0 || status1 == 0 && status2 != 0) {
                            return -1;
                        } else if (status2 < 0 && status1 > 0 || status2 == 0 && status1 != 0) {
                            return 1;
                        }
                        return 0;
                    }
                });
            } catch (Exception e) {
                FileLog.e( e);
            }

            if (listView.getAdapter() != null) {
                listView.getAdapter().notifyItemRangeChanged(0, sortedUsers.size());
            }
        }
    }

    public interface ChatDialogsViewDelegate {
        void didPressedOnSubDialog(long did);
        void didPressedOnBtn(boolean visible);
        void didLongPressedOnSubDialog(long did, int type);
    }

    public void setDelegate(ChatDialogsViewDelegate delegate) {
        this.delegate = delegate;
    }

    public ChatDialogsView(Context context, BaseFragment fragment, long chat_id) {
        super(context);

        parentFragment = (ChatActivity)fragment;

        vertical = Theme.plusVerticalQuickBar;
        visible = false;
        refresh = false;
        dialogsType = Theme.plusQuickBarDialogType;
        if(vertical){
            setTranslationX(AndroidUtilities.dp(listWidth));
        } else{
            setTranslationY(-AndroidUtilities.dp(listHeight));
        }
        ((ViewGroup) fragment.getFragmentView()).setClipToPadding(false);
        setBackgroundColor(0x00000000);

        //Log.e("ChatDialogsView", "chat_id " + chat_id);
        if(chat_id < 0 && Theme.plusQuickBarShowMembers){
            currentChat = parentFragment.getCurrentChat();
            if(currentChat != null && (!ChatObject.isChannel(currentChat) || currentChat.megagroup)) {
                showMembers = true;
                sortedUsers = new ArrayList<>();
                if (currentChat.megagroup) {
                    loadMoreMembersRow = 32;
                    //participantsMap = new HashMap<>();
                    membersMap = new ArrayList<>();
                    classGuid = parentFragment.getCurrentClassGuid();
                    this.chat_id = -(int) chat_id;
                    getChannelParticipants(true);
                } else {
                    //participantsMap = null;
                    membersMap = null;
                }
                updateOnlineCount();
            }
        }
        if(!showMembers && dialogsType == -1){
            Theme.plusQuickBarDialogType = dialogsType = 0;
        }

        listView = new RecyclerListView(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent e) {
                if (getParent() != null && getParent().getParent() != null) {
                    getParent().getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(e);
            }
        };
        listView.setTag(9);
        listView.setBackgroundColor(Theme.usePlusTheme ? Theme.chatQuickBarColor : Theme.getColor(Theme.key_chat_goDownButton));
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(vertical ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(layoutManager);
        dialogsAdapter = new ChatDialogsAdapter(context, chat_id);
        membersAdapter = new ListAdapter(context);
        //listAdapter = showMembers && dialogsType < 0 ? membersAdapter : dialogsAdapter;
        //listView.setAdapter(listAdapter);
        listView.setAdapter(showMembers && dialogsType < 0 ? membersAdapter : dialogsAdapter);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (delegate != null) {
                    try{
                        delegate.didPressedOnSubDialog((Long) view.getTag());
                    } catch (Exception e) {
                        FileLog.e( e);
                    }
                }
            }
        });

        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
                @Override
                public boolean onItemClick(View view, int position) {
                    if (delegate != null) {
                        try{
                            delegate.didLongPressedOnSubDialog((Long) view.getTag(), dialogsType);
                        } catch (Exception e) {
                            FileLog.e( e);
                        }
                    }
                    return true;
                }
        });

        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT));
        btn = new ImageView(context);
        btn.setColorFilter(Theme.usePlusTheme ? Theme.chatQuickBarNamesColor != 0xff212121 ? Theme.chatQuickBarNamesColor : Theme.defColor : Theme.getColor(Theme.key_chat_goDownButtonIcon), PorterDuff.Mode.SRC_IN);
        btn.setImageResource(vertical ? R.drawable.ic_bar_open : R.drawable.search_down);
        btn.setScaleType(ImageView.ScaleType.CENTER);
        //btn.setBackgroundResource(vertical ? R.drawable.ic_bar_bg_v : R.drawable.ic_bar_bg);
        //int res = vertical ? R.drawable.ic_bar_bg_v : R.drawable.ic_bar_bg;
        Drawable d = context.getResources().getDrawable(vertical ? R.drawable.ic_bar_bg_v : R.drawable.ic_bar_bg);
        d.setColorFilter(Theme.usePlusTheme ? Theme.chatQuickBarColor : Theme.getColor(Theme.key_chat_goDownButton), PorterDuff.Mode.MULTIPLY);
        btn.setBackgroundDrawable(d);
        addView(btn, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, vertical ? (Theme.plusCenterQuickBarBtn ? Gravity.CENTER_VERTICAL : Gravity.BOTTOM) : (Theme.plusCenterQuickBarBtn ? Gravity.CENTER_HORIZONTAL : Gravity.TOP | Gravity.RIGHT), 0, vertical ? 0 : listHeight, vertical ? listWidth : 0, vertical ? (Theme.plusCenterQuickBarBtn ? 0 : listWidth) : 0));

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPressed();
            }
        });

        btn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!visible || disableLongCick)return false;
                changeDialogType();
                return true;
            }
        });

        btn.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeTop() {
                if(visible && !vertical)btnPressed();
            }
            public void onSwipeRight() {
                if(visible && vertical)btnPressed();
            }
            public void onSwipeLeft() {
                if(!visible && vertical)btnPressed();
            }
            public void onSwipeBottom() {
                if(!visible && !vertical)btnPressed();
            }
        });

        tv = new TextView(context);
        tv.setTextColor(Theme.usePlusTheme ? Theme.chatQuickBarNamesColor : Theme.getColor(Theme.key_chat_goDownButtonIcon));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        tv.setBackgroundColor(Theme.usePlusTheme ? Theme.chatQuickBarColor : Theme.getColor(Theme.key_chat_goDownButton));
        tv.setVisibility(INVISIBLE);
        addView(tv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, vertical ? AndroidUtilities.dp(4) : 0, 0, 0, 0));

        if(showMembers){
            listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (listView.getAdapter() == membersAdapter && membersMap != null && layoutManager.findLastVisibleItemPosition() > loadMoreMembersRow - 5) {
                        getChannelParticipants(false);
                    }
                }
            });
        }
    }

    public void changeDialogType(){
        //Log.e("ChatDialogsView", "changeDialogType dialogsType " + dialogsType);
        switch(dialogsType) {
            case 0:
                Theme.plusQuickBarDialogType = dialogsType = 8; //FAVS
                if(MessagesController.getInstance().dialogsFavs.size() == 0){
                    changeDialogType();
                }
                break;
            case 8:
                Theme.plusQuickBarDialogType = dialogsType = 3; //USERS
                if(MessagesController.getInstance().dialogsUsers.size() == 0){
                    changeDialogType();
                }
                break;
            case 3:
                Theme.plusQuickBarDialogType = dialogsType = 4; //GROUPS
                if(MessagesController.getInstance().dialogsGroups.size() == 0){
                    changeDialogType();
                }
                break;
            case 4:
                Theme.plusQuickBarDialogType = dialogsType = 7; //SUPERGROUPS
                if(MessagesController.getInstance().dialogsMegaGroups.size() == 0){
                    changeDialogType();
                }
                break;
            case 7:
                Theme.plusQuickBarDialogType = dialogsType = 5; //CHANNELS
                if(MessagesController.getInstance().dialogsChannels.size() == 0){
                    changeDialogType();
                }
                break;
            case 5:
                Theme.plusQuickBarDialogType = dialogsType = 6; //BOTS
                if(MessagesController.getInstance().dialogsBots.size() == 0){
                    changeDialogType();
                }
                break;
            case 6:
                if(showMembers){
                    Theme.plusQuickBarDialogType = dialogsType = -1;
                    if (listView.getAdapter() != membersAdapter) {
                        //listAdapter = membersAdapter;
                        listView.setAdapter(membersAdapter);
                    }
                } else {
                    Theme.plusQuickBarDialogType = dialogsType = 0; //ALL
                    if (listView.getAdapter() != dialogsAdapter) {
                        //listAdapter = dialogsAdapter;
                        listView.setAdapter(dialogsAdapter);
                    }
                }
                break;
            case -1:
                Theme.plusQuickBarDialogType = dialogsType = 0;
                if(showMembers){
                    if (listView.getAdapter() != dialogsAdapter) {
                        //listAdapter = dialogsAdapter;
                        listView.setAdapter(dialogsAdapter);
                    }
                }
                break;
            default:
                Theme.plusQuickBarDialogType = dialogsType = 0; //ALL
        }
        int title = R.string.ChatHints;
        if(listView != null){
            if(listView.getAdapter() != null){
                listView.getAdapter().notifyDataSetChanged();
                if(listView.getAdapter() instanceof  ChatDialogsAdapter){
                    title = ((ChatDialogsAdapter)listView.getAdapter()).getTitleRes();
                } else{
                    title = ((ListAdapter)listView.getAdapter()).getTitleRes();
                }
            }
            listView.scrollToPosition(0);
        }

        /*if(showMembers){
            title = ((ListAdapter)listAdapter).getTitleRes();
        } else{
            title = ((ChatDialogsAdapter)listAdapter).getTitleRes();
        }*/
        tv.setText(title);
        tv.setVisibility(VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(500);
        animation.setStartOffset(1000);
        tv.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                tv.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }
        });

        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.putInt("quickBarDialogType", dialogsType).apply();
    }

    public void refreshList(){
        if(listView.getAdapter() != null){
            listView.getAdapter().notifyDataSetChanged();
        }
    }

    public void btnPressed(){
        if (delegate != null) {
            delegate.didPressedOnBtn(visible);
        }
        if(!visible){
            AndroidUtilities.runOnUIThread(new Runnable() {
            //Handler handler = new Handler();
            //handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // To avoid btn long click after LEFT or BOTTOM swipe;
                    disableLongCick = false;
                }
            }, 500);
        }
        visible = !visible;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(refresh){
            if(listView.getAdapter() != null)listView.getAdapter().notifyDataSetChanged();
            refresh = false;
        }
    }

    public void onDestroy() {
        delegate = null;
        dialogsAdapter = null;
        membersAdapter = null;
    }

    public void needRefresh(boolean refresh){
        this.refresh = refresh;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public boolean isVisible(){
        return visible;
    }

    public int getListHeight(){
        return listHeight;
    }

    public int getListWidth(){
        return listWidth;
    }

    public void setBtnResId(int res){
        btn.setImageResource(res);
    }

    public class ChatDialogsAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private long chatId;
        private int InvisibleRow = 0;
        private int NoChatsRow = 1;
        private int ChatDialogRow = 2;

        private class Holder extends RecyclerView.ViewHolder {

            public Holder(View itemView) {
                super(itemView);
            }
        }

        private ChatDialogsAdapter(Context context, long chat_id) {
            mContext = context;
            chatId = chat_id;
        }

        @Override
        public int getItemCount() {
            return getDialogsArray().size();
        }

        private ArrayList<TLRPC.TL_dialog> getDialogsArray(){
            switch(dialogsType) {
                case 0:
                    return MessagesController.getInstance().dialogs;
                case 8:
                    return MessagesController.getInstance().dialogsFavs;
                case 3:
                    return MessagesController.getInstance().dialogsUsers;
                case 4:
                    return MessagesController.getInstance().dialogsGroups;
                case 7:
                    return MessagesController.getInstance().dialogsMegaGroups;
                case 5:
                    return MessagesController.getInstance().dialogsChannels;
                case 6:
                    return MessagesController.getInstance().dialogsBots;
                default:
                    return MessagesController.getInstance().dialogs;
            }
        }

        @Override
        public long getItemId(int i) {
            ArrayList<TLRPC.TL_dialog> arrayList = getDialogsArray();
            if (i < 0 || i >= arrayList.size()) {
                return 0;
            }
            return arrayList.get(i).id;
        }

        @Override
        public int getItemViewType(int i) {
            if(chatId == getItemId(i)){
                if(dialogsType != 0 && getItemCount() <= 1){
                    //delegate.listLoaded();
                    return NoChatsRow;
                }
                return InvisibleRow;
            }
            return ChatDialogRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if(viewType == ChatDialogRow){
                view = new ChatDialogCell(mContext);
                view.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(listWidth), AndroidUtilities.dp(listHeight)));
            } else if (viewType == NoChatsRow){
                view = new TextView(mContext);
                view.setLayoutParams(new RecyclerView.LayoutParams(Theme.plusVerticalQuickBar ? AndroidUtilities.dp(listWidth) : LayoutHelper.MATCH_PARENT, Theme.plusVerticalQuickBar ? LayoutHelper.MATCH_PARENT : AndroidUtilities.dp(listHeight)));
            } else if(viewType == InvisibleRow){
                view = new View(mContext);
                view.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                view.setVisibility(GONE);
            }
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
            if(holder.getItemViewType() == ChatDialogRow) {
                long id = getItemId(i);
                //int did = (int) id;
                ChatDialogCell cell = (ChatDialogCell) holder.itemView;
                cell.setTag(id);
                cell.setDialog(id);
            } else if(holder.getItemViewType() == NoChatsRow) {
                TextView tv = (TextView) holder.itemView;
                tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                String title = LocaleController.formatString("NoChatsYet", R.string.NoChatsYet, getTitleRes());
                tv.setText(title);
            }

        }

        private int getTitleRes(){
            switch(dialogsType) {
                case 0:
                    return R.string.ChatHints;
                case 8:
                    return R.string.Favorites;
                case 3:
                    return R.string.Users;
                case 4:
                    return R.string.Groups;
                case 7:
                    return R.string.SuperGroups;
                case 5:
                    return R.string.Channels;
                case 6:
                    return R.string.Bots;
                default:
                    return R.string.ChatHints;
            }
        }
    }

    public class ChatDialogCell extends FrameLayout {

        private BackupImageView imageView;
        private TextView nameTextView;
        private AvatarDrawable avatarDrawable = new AvatarDrawable();
        private ImageView adminImage;

        private Drawable countDrawable;
        private Drawable countDrawableGrey;
        private TextPaint countPaint;

        private int lastUnreadCount;
        private int countWidth;
        private StaticLayout countLayout;

        private long dialog_id;

        private boolean hideCounter;

        public ChatDialogCell(Context context) {
            super(context);
            //setBackgroundResource(R.drawable.list_selector);
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(54));
            avatarDrawable.setRadius(AndroidUtilities.dp(54));
            addView(imageView, LayoutHelper.createFrame(avatarSize, avatarSize, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 5, 0, 0));

            nameTextView = new TextView(context);
            nameTextView.setTextColor(Theme.usePlusTheme ? Theme.chatQuickBarNamesColor : Theme.getColor(Theme.key_chat_goDownButtonIcon));
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
            nameTextView.setMaxLines(2);
            nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            nameTextView.setLines(2);
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, avatarSize + 5, 6, 0));

            if (countDrawable == null) {
                countDrawable = getResources().getDrawable(R.drawable.bluecounter);
                countDrawableGrey = getResources().getDrawable(R.drawable.bluecounter);
                countDrawable.setColorFilter(Theme.usePlusTheme ? themePrefs.getInt("chatsCountBGColor", Theme.defColor) : Theme.getColor(Theme.key_chat_goDownButtonCounterBackground), PorterDuff.Mode.SRC_IN);
                countDrawableGrey.setColorFilter(Theme.usePlusTheme ? themePrefs.getInt("chatsCountSilentBGColor", themePrefs.getInt("chatsCountBGColor", 0xffb9b9b9)) : Theme.getColor(Theme.key_chat_goDownButtonCounterBackground), PorterDuff.Mode.SRC_IN);
                countPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
                countPaint.setTextSize(AndroidUtilities.dp(/*12*/11));
                countPaint.setColor(Theme.usePlusTheme ? themePrefs.getInt("chatsCountColor", 0xffffffff): Theme.getColor(Theme.key_chat_goDownButtonCounter));
                countPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            }

            adminImage = new ImageView(context);
            adminImage.setVisibility(GONE);
            addView(adminImage, LayoutHelper.createFrame(16, 16, Gravity.RIGHT | Gravity.TOP));

        }

        public void setIsAdmin(int value) {
            if (adminImage == null) {
                return;
            }
            adminImage.setVisibility(value != 0 ? VISIBLE : GONE);
            if (value == 1) {
                adminImage.setImageResource(R.drawable.admin_star);
                adminImage.setColorFilter(Theme.profileRowCreatorStarColor, PorterDuff.Mode.SRC_IN);
            } else if (value == 2) {
                adminImage.setImageResource(R.drawable.admin_star);
                adminImage.setColorFilter(Theme.profileRowAdminStarColor, PorterDuff.Mode.SRC_IN);
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    getBackground().setHotspot(event.getX(), event.getY());
                }
            }
            return super.onTouchEvent(event);
        }

        public void checkUnreadCounter(int mask) {
            if (mask != 0 && (mask & MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE) == 0 && (mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) == 0) {
                return;
            }
            TLRPC.TL_dialog dialog = MessagesController.getInstance().dialogs_dict.get(dialog_id);
            if (dialog != null && dialog.unread_count != 0) {
                if (lastUnreadCount != dialog.unread_count) {
                    lastUnreadCount = dialog.unread_count;
                    String countString = String.format("%d", lastUnreadCount);
                    if(lastUnreadCount > 99){
                        countString = "+99";
                        //countPaint.setTextSize(AndroidUtilities.dp(/*12*/10));
                    }
                    countWidth = Math.max(AndroidUtilities.dp(/*11*/5), (int) Math.ceil(countPaint.measureText(countString)));
                    countLayout = new StaticLayout(countString, countPaint, countWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                    if (mask != 0) {
                        invalidate();
                    }
                }
            } else if (countLayout != null) {
                if (mask != 0) {
                    invalidate();
                }
                lastUnreadCount = 0;
                countLayout = null;
            }
        }

        public void hideCounter(boolean hide){
            hideCounter = hide;
        }

        public void setDialog(long id) {
            dialog_id = id;
            TLRPC.FileLocation photo = null;
            int lower_id = (int)id;
            int high_id = (int)(id >> 32);
            TLRPC.User user = null;
            TLRPC.Chat chat = null;
            if (lower_id != 0) {
                if (lower_id > 0) {
                    user = MessagesController.getInstance().getUser(lower_id);
                } else {
                    chat = MessagesController.getInstance().getChat(-lower_id);
                }
            } else{
                TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(high_id);
                if (encryptedChat != null) {
                    user = MessagesController.getInstance().getUser(encryptedChat.user_id);
                }
            }
            if (user != null) {
                nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
                avatarDrawable.setInfo(user);
                if (user.photo != null) {
                    photo = user.photo.photo_small;
                }
            } else if (chat != null) {
                nameTextView.setText(chat.title);
                avatarDrawable.setInfo(chat);
                if (chat.photo != null) {
                    photo = chat.photo.photo_small;
                }
            } else {
                nameTextView.setText("");
            }
            imageView.setImage(photo, "50_50", avatarDrawable);
            if(!hideCounter)checkUnreadCounter(0);
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean result = super.drawChild(canvas, child, drawingTime);
            if (child == imageView) {
                if (countLayout != null) {
                    int top = AndroidUtilities.dp(2);
                    int left = /*AndroidUtilities.dp(avatarSize)*/AndroidUtilities.dp(8);
                    int x = left - AndroidUtilities.dp(5.5f);
                    if (MessagesController.getInstance().isDialogMuted(dialog_id)) {
                        countDrawableGrey.setBounds(x, top + AndroidUtilities.dp(2), x + countWidth + AndroidUtilities.dp(11), top + countDrawableGrey.getIntrinsicHeight() - AndroidUtilities.dp(4));
                        countDrawableGrey.draw(canvas);
                    } else {
                        countDrawable.setBounds(x, top + AndroidUtilities.dp(2), x + countWidth + AndroidUtilities.dp(11), top + countDrawable.getIntrinsicHeight() - AndroidUtilities.dp(4));
                        countDrawable.draw(canvas);
                    }
                    canvas.save();
                    canvas.translate(left, top + AndroidUtilities.dp(4));
                    countLayout.draw(canvas);
                    canvas.restore();
                }
            }
            return result;
        }

        public long getDialogId(){
            return dialog_id;
        }
    }

    public class OnSwipeTouchListener implements OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = /*100*/10;
            private static final int SWIPE_VELOCITY_THRESHOLD = /*100*/10;

            @Override
            public boolean onDown(MotionEvent e) {
                //Log.e("ChatDialogsView", "onDown");
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                disableLongCick = true;
                //Log.e("ChatDialogsView", "onFling");
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                        result = true;
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                    result = true;

                return result;
            }
        }

        public void onSwipeRight() {

        }

        public void onSwipeLeft() {

        }

        public void onSwipeTop() {

        }

        public void onSwipeBottom() {

        }
    }

    private class ListAdapter extends RecyclerListView.Adapter {
        private Context mContext;

        private class Holder extends RecyclerView.ViewHolder {
            public Holder(View itemView) {
                super(itemView);
            }
        }

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new ChatDialogCell(mContext);
            view.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(listWidth), AndroidUtilities.dp(listHeight)));
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
                    TLRPC.ChatParticipant part;
                    if (!sortedUsers.isEmpty()) {
                        part = info.participants.participants.get(sortedUsers.get(i));
                    } else {
                        part = info.participants.participants.get(i);
                    }
                    if (part != null) {
                        ChatDialogCell cell = (ChatDialogCell) holder.itemView;
                        if (part instanceof TLRPC.TL_chatChannelParticipant) {
                            TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                            if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                                cell.setIsAdmin(1);
                            } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin /*channelParticipant instanceof TLRPC.TL_channelParticipantEditor || channelParticipant instanceof TLRPC.TL_channelParticipantModerator*/) {
                                cell.setIsAdmin(2);
                            } else {
                                cell.setIsAdmin(0);
                            }
                        } else {
                            if (part instanceof TLRPC.TL_chatParticipantCreator) {
                                cell.setIsAdmin(1);
                            } else if (currentChat.admins_enabled && part instanceof TLRPC.TL_chatParticipantAdmin) {
                                cell.setIsAdmin(2);
                            } else {
                                cell.setIsAdmin(0);
                            }
                        }
                        long did = part.user_id;

                        cell.setTag(did);
                        cell.hideCounter(true);
                        cell.setDialog(did);
                    }
        }

        @Override
        public int getItemCount() {
            if (currentChat.megagroup) {
                return info != null && info.participants != null && !info.participants.participants.isEmpty() ? info.participants.participants.size() : 0;
            }else{
                return sortedUsers.size();
            }
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        private int getTitleRes(){
            return R.string.ChannelMembers;
        }
    }
}
