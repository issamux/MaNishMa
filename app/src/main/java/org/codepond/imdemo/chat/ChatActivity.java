package org.codepond.imdemo.chat;

import android.content.ComponentName;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.codepond.imdemo.BaseActivity;
import org.codepond.imdemo.ChatMessage;
import org.codepond.imdemo.R;
import org.codepond.imdemo.databinding.ActivityChatBinding;
import org.codepond.imdemo.service.chat.ChatService;
import org.codepond.imdemo.service.chat.MessagingService;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChatActivity extends BaseActivity {
    public static final String EXTRA_PARTICIPANT_JID = "extra_participant_jid";
    private MessageAdapter mAdapter;
    private RecyclerView mRecyclerView;
    ChatViewModel mChatViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String participantJid = getIntent().getStringExtra(EXTRA_PARTICIPANT_JID);
        String userJid = "test@localhost";
        mChatViewModel = new ChatViewModel(userJid, participantJid);
        ActivityChatBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        binding.setModel(mChatViewModel);
        mRecyclerView = (RecyclerView) findViewById(R.id.message_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MessageAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mChatViewModel.loadMessages();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MessagingService messagingService = ((ChatService.LocalBinder)service).getService();
        mChatViewModel.start(messagingService);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mChatViewModel.stop();
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private List<ChatMessage> mMessages;

        @Override
        public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new MessageViewHolder(inflater.inflate(R.layout.message_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MessageAdapter.MessageViewHolder vh, int position) {
            ChatMessage chatMessage = mMessages.get(position);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vh.container.getLayoutParams();
            if (chatMessage.getIncomingMessage()) {
                if (isPreviousAuthorSame(position, chatMessage.getFrom())) {
                    vh.container.setBackgroundResource(R.drawable.chat_bubble_incoming_ext);
                }
                else {
                    vh.author.setText(chatMessage.getFrom());
                    vh.author.setVisibility(android.view.View.VISIBLE);
                    vh.container.setBackgroundResource(R.drawable.chat_bubble_incoming);

                }
                lp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            }
            else {
                if (isPreviousAuthorSame(position, chatMessage.getFrom())) {
                    vh.container.setBackgroundResource(R.drawable.chat_bubble_outgoing_ext);
                }
                else {
                    vh.container.setBackgroundResource(R.drawable.chat_bubble_outgoing);
                }
                lp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            }
            vh.container.setLayoutParams(lp);
            vh.messageItem.setText(chatMessage.getMessageText());
            vh.timestamp.setText(SimpleDateFormat.getInstance().format(chatMessage.getTimestamp()));
        }

        private boolean isPreviousAuthorSame(int position, String displayName) {
            return position > 0 && mMessages.get(position - 1).getFrom().equals(displayName);
        }

        @Override
        public int getItemCount() {
            return mMessages != null ? mMessages.size() : 0;
        }

        void notifyNewMessageAdded() {
            final int newMessagePosition = mMessages.size();
            mAdapter.notifyItemInserted(newMessagePosition);
        }

        void setMessages(List<ChatMessage> chatMessages) {
            mMessages = chatMessages;
            notifyDataSetChanged();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageItem;
            TextView timestamp;
            TextView author;
            android.view.View container;
            MessageViewHolder(android.view.View view) {
                super(view);
                container = view.findViewById(R.id.message_container);
                messageItem = (TextView) view.findViewById(R.id.message_item);
                timestamp = (TextView) view.findViewById(R.id.timestamp);
                author = (TextView) view.findViewById(R.id.author);
            }
        }
    }
}