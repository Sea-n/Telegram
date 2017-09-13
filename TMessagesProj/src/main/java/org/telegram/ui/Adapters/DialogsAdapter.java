/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.query.DraftQuery;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import static android.view.View.GONE;

public class DialogsAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private int dialogsType;
    private long openedDialogId;
    private int currentCount;
	
	private ArrayList<TLRPC.TL_dialog> dialogsArray = new ArrayList<>();


    public DialogsAdapter(Context context, int type) {
        mContext = context;
        dialogsType = type;
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean isDataSetChanged() {
		dialogsArray = null;
        int current = currentCount;
        return current != getItemCount() || current == 1;
    }
	
	@Override
    public void notifyDataSetChanged() {
        dialogsArray = null;
        //Log.e("DialogsAdapter", "notifyDataSetChanged");
        super.notifyDataSetChanged();
    }

    /*private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        if (dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        return null;
    }*/
	//plus
	public ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        if(dialogsArray != null){
            return dialogsArray;
        } else{
            dialogsArray = sortDialogs();
            return dialogsArray;
        }
    }

	public ArrayList<TLRPC.TL_dialog> sortDialogs(){
        //Log.e("DialogsAdapter", "sortDialogs dialogsType " + dialogsType);
        switch(dialogsType) {
            case 0:
                if (Theme.plusSortAll == 0 || Theme.plusHideTabs) {
                    sortDefault(MessagesController.getInstance().dialogs);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogs);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogs);
                }
                return MessagesController.getInstance().dialogs;
            case 1:
                return MessagesController.getInstance().dialogsServerOnly;
            case 2:
                return MessagesController.getInstance().dialogsGroupsOnly;
            case 3:
                if(Theme.plusSortUsers == 0){
                    sortUsersDefault();
                }else{
                    //sortUsersByStatus();
                    sortUsersByStatusPinnedOnTop();
                }
                return MessagesController.getInstance().dialogsUsers;
            case 4:
                if(Theme.plusSortGroups == 0){
                    sortDefault(MessagesController.getInstance().dialogsGroups);
                }else{
                    //sortUnread(MessagesController.getInstance().dialogsGroups);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsGroups);
                }
                return MessagesController.getInstance().dialogsGroups;
            case 5:
                if(Theme.plusSortChannels == 0){
                    sortDefault(MessagesController.getInstance().dialogsChannels);
                }else{
                    //sortUnread(MessagesController.getInstance().dialogsChannels);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsChannels);
                }
                return MessagesController.getInstance().dialogsChannels;
            case 6:
                if(Theme.plusSortBots == 0){
                    sortDefault(MessagesController.getInstance().dialogsBots);
                }else{
                    //sortUnread(MessagesController.getInstance().dialogsBots);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsBots);
                }
                return MessagesController.getInstance().dialogsBots;
            case 7:
                if(Theme.plusSortSuperGroups == 0){
                    sortDefault(MessagesController.getInstance().dialogsMegaGroups);
                }else{
                    //sortUnread(MessagesController.getInstance().dialogsMegaGroups);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsMegaGroups);
                }
                return MessagesController.getInstance().dialogsMegaGroups;
            case 8:
                if(Theme.plusSortFavs == 0){
                    sortDefault(MessagesController.getInstance().dialogsFavs);
                }else{
                    //sortUnread(MessagesController.getInstance().dialogsFavs);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsFavs);
                }
                return MessagesController.getInstance().dialogsFavs;
            case 9:
                if(Theme.plusSortGroups == 0){
                    sortDefault(MessagesController.getInstance().dialogsGroupsAll);
                }else{
                    //sortUnread(MessagesController.getInstance().dialogsGroupsAll);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsGroupsAll);
                }
                return MessagesController.getInstance().dialogsGroupsAll;
            case 10:
                if(Theme.plusSortAdmin == 0){
                    sortDefault(MessagesController.getInstance().dialogsAdmin);
                }else{
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogsAdmin);
                }
                return MessagesController.getInstance().dialogsAdmin;
            case 11:
                if(Theme.plusSortUnread == 0){
                    //Log.e("DialogsAdapter", "sortDialogs Unread  " + Theme.plusSortUnread);
                    sortDefault(MessagesController.getInstance().dialogsUnread);
                } else {
                    if(Theme.plusSortUnread == 2){
                        sortUnreadPinedOnTopUnmutedFirst(MessagesController.getInstance().dialogsUnread);
                    } else {
                        sortUnreadPinedOnTop(MessagesController.getInstance().dialogsUnread);
                    }
                }
                return MessagesController.getInstance().dialogsUnread;
            default:
                if (Theme.plusSortAll == 0 || Theme.plusHideTabs) {
                    sortDefault(MessagesController.getInstance().dialogs);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogs);
                    sortUnreadPinedOnTop(MessagesController.getInstance().dialogs);
                }
                return MessagesController.getInstance().dialogs;
        }
    }
    //plus
    public void setDialogsType(int type){
        this.dialogsType = type;
    }

    public void sort(){
        getDialogsArray();
        notifyDataSetChanged();
    }

    /*private void sortUsersByStatus(){
        Collections.sort(MessagesController.getInstance().dialogsUsers, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog tl_dialog, TLRPC.TL_dialog tl_dialog2) {
                TLRPC.User user1 = MessagesController.getInstance().getUser((int) tl_dialog2.id);
                TLRPC.User user2 = MessagesController.getInstance().getUser((int) tl_dialog.id);
                int status1 = 0;
                int status2 = 0;
                if (user1 != null && user1.status != null) {
                    if (user1.id == UserConfig.getClientUserId()) {
                        status1 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                    } else {
                        status1 = user1.status.expires;
                    }
                }
                if (user2 != null && user2.status != null) {
                    if (user2.id == UserConfig.getClientUserId()) {
                        status2 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                    } else {
                        status2 = user2.status.expires;
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
    }*/

    private void sortUsersByStatusPinnedOnTop(){
        Collections.sort(MessagesController.getInstance().dialogsUsers, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                TLRPC.User user1 = MessagesController.getInstance().getUser((int) dialog2.id);
                TLRPC.User user2 = MessagesController.getInstance().getUser((int) dialog1.id);
                int status1 = 0;
                int status2 = 0;
                if (user1 != null && user1.status != null) {
                    if (user1.id == UserConfig.getClientUserId()) {
                        status1 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                    } else {
                        status1 = user1.status.expires;
                    }
                }
                if (user2 != null && user2.status != null) {
                    if (user2.id == UserConfig.getClientUserId()) {
                        status2 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                    } else {
                        status2 = user2.status.expires;
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
    }

    private void sortDefault(ArrayList<TLRPC.TL_dialog> dialogs){
        //Log.e("DialogsAdapter", "sortDefault dialogsType " + dialogsType);
        Collections.sort(dialogs, dialogComparator);
    }

    /*private void sortUnreadDefault(ArrayList<TLRPC.TL_dialog> dialogs){
        //Log.e("DialogsAdapter", "sortDefault dialogsType " + dialogsType);
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (MessagesController.getInstance().isDialogMuted(dialog1.id) && !MessagesController.getInstance().isDialogMuted(dialog2.id)) {
                    return 0;
                } else if (MessagesController.getInstance().isDialogMuted(dialog2.id) && !MessagesController.getInstance().isDialogMuted(dialog1.id)) {
                    return 1;
                } else if (MessagesController.getInstance().isDialogMuted(dialog2.id) && MessagesController.getInstance().isDialogMuted(dialog1.id)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    private void sortUnread(ArrayList<TLRPC.TL_dialog> dialogs){
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                try {
                    if (dialog1.unread_count == dialog2.unread_count) {
                        return 0;
                    } else if (dialog1.unread_count < dialog2.unread_count) {
                        return 1;
                    } else {
                        return -1;
                    }
                } catch (Exception e) {
                    FileLog.e( e);
                    return -1;
                }
            }
        });
    }*/

    private void sortUnreadPinedOnTop(ArrayList<TLRPC.TL_dialog> dialogs){
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (dialog1.unread_count == dialog2.unread_count) {
                    return 0;
                } else if (dialog1.unread_count < dialog2.unread_count) {
                    return 1;
                } else {
                    return -1;
                }

            }
        });
    }

    private void sortUnreadPinedOnTopUnmutedFirst(ArrayList<TLRPC.TL_dialog> dialogs){
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                /*if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if(!MessagesController.getInstance().isDialogMuted(dialog1.id) && MessagesController.getInstance().isDialogMuted(dialog2.id)){
                    return 1;
                } else if(MessagesController.getInstance().isDialogMuted(dialog1.id) && !MessagesController.getInstance().isDialogMuted(dialog2.id)){
                    return -1;
                } else if(MessagesController.getInstance().isDialogMuted(dialog1.id) && MessagesController.getInstance().isDialogMuted(dialog2.id)){

                }*/
                if(!MessagesController.getInstance().isDialogMuted(dialog2.id)){
                    return 1;
                } else if(!MessagesController.getInstance().isDialogMuted(dialog1.id)){
                    return -1;
                } else {
                    //if (dialog1.unread_count == dialog2.unread_count) {
                        return 0;
                    /*} else if (dialog1.unread_count < dialog2.unread_count) {
                        return 1;
                    } else {
                        return -1;
                    }*/
                }
            }
        });
    }

    private void sortUnmutedPinedOnTop(ArrayList<TLRPC.TL_dialog> dialogs){
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (MessagesController.getInstance().isDialogMuted(dialog1.id) && !MessagesController.getInstance().isDialogMuted(dialog2.id)) {
                    return 0;
                } else if (MessagesController.getInstance().isDialogMuted(dialog2.id) && !MessagesController.getInstance().isDialogMuted(dialog1.id)) {
                    return 1;
                } else if (MessagesController.getInstance().isDialogMuted(dialog2.id) && MessagesController.getInstance().isDialogMuted(dialog1.id)) {
                    return 1;
                } else {
                    return -1;
                }

            }
        });
    }

    private void sortUsersDefault(){
        Collections.sort(MessagesController.getInstance().dialogsUsers, dialogComparator);
    }

    private final Comparator<TLRPC.TL_dialog> dialogComparator = new Comparator<TLRPC.TL_dialog>() {
        @Override
        public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
            if (!dialog1.pinned && dialog2.pinned) {
                return 1;
            } else if (dialog1.pinned && !dialog2.pinned) {
                return -1;
            } else if (dialog1.pinned && dialog2.pinned) {
                if (dialog1.pinnedNum < dialog2.pinnedNum) {
                    return 1;
                } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                    return -1;
                } else {
                    return 0;
                }
            }
            TLRPC.DraftMessage draftMessage = DraftQuery.getDraft(dialog1.id);
            int date1 = draftMessage != null && draftMessage.date >= dialog1.last_message_date ? draftMessage.date : dialog1.last_message_date;
            draftMessage = DraftQuery.getDraft(dialog2.id);
            int date2 = draftMessage != null && draftMessage.date >= dialog2.last_message_date ? draftMessage.date : dialog2.last_message_date;
            if (date1 < date2) {
                return 1;
            } else if (date1 > date2) {
                return -1;
            }
            return 0;
        }
    };

    /*private final Comparator<TLRPC.TL_dialog> dialogComparatorOld = new Comparator<TLRPC.TL_dialog>() {
        @Override
        public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
            TLRPC.DraftMessage draftMessage = DraftQuery.getDraft(dialog1.id);
            int date1 = draftMessage != null && draftMessage.date >= dialog1.last_message_date ? draftMessage.date : dialog1.last_message_date;
            draftMessage = DraftQuery.getDraft(dialog2.id);
            int date2 = draftMessage != null && draftMessage.date >= dialog2.last_message_date ? draftMessage.date : dialog2.last_message_date;
            if (date1 < date2) {
                return 1;
            } else if (date1 > date2) {
                return -1;
            }
            return 0;
        }
    };*/
	//
    @Override
    public int getItemCount() {
        int count = getDialogsArray().size();
        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return 0;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        currentCount = count;
        return count;
    }

    public TLRPC.TL_dialog getItem(int i) {
        ArrayList<TLRPC.TL_dialog> arrayList = getDialogsArray();
        if (i < 0 || i >= arrayList.size()) {
            return null;
        }
        return arrayList.get(i);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView instanceof DialogCell) {
            ((DialogCell) holder.itemView).checkCurrentDialogIndex();
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return holder.getItemViewType() != 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = new DialogCell(mContext);
        } else if (viewType == 1) {
            view = new LoadingCell(mContext);
        }
		//plus
		else if (viewType == 2) {
            view = new View(mContext);
            view.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
		//
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
		//plus
		if(dialogsType > 2 && viewType == 1)view.setVisibility(GONE);
        /*if(Theme.usePlusTheme) {
            if (Theme.chatsRowGradient > 0) {
                GradientDrawable.Orientation go;
                switch (Theme.chatsRowGradient) {
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

                //int gradColor = themePrefs.getInt("chatsRowGradientColor", 0xffffffff);
                int[] colors = new int[]{Theme.chatsRowColor, Theme.chatsRowGradientColor};
                GradientDrawable gd = new GradientDrawable(go, colors);
                viewGroup.setBackgroundDrawable(gd);
            } else {
                viewGroup.setBackgroundColor(Theme.chatsRowColor);
            }
        }*/
		//
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            DialogCell cell = (DialogCell) viewHolder.itemView;
            cell.useSeparator = (i != getItemCount() - 1);
            TLRPC.TL_dialog dialog = getItem(i);
            if (dialogsType == 0) {
                if (AndroidUtilities.isTablet()) {
                    cell.setDialogSelected(dialog.id == openedDialogId);
                }
            }
            cell.setDialog(dialog, i, dialogsType);
        }
		// plus
        else if(viewHolder.getItemViewType() == 1){
            viewHolder.itemView.setVisibility(GONE);
        }
        //
    }

    @Override
    public int getItemViewType(int i) {
        if (i == getDialogsArray().size()) {
			//plus
			if(dialogsType > 2)return 2;
			//
            return 1;
        }
        return 0;
    }
}
