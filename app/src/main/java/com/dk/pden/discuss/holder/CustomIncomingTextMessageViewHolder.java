package com.dk.pden.discuss.holder;

import android.view.View;
import android.widget.TextView;

import com.dk.pden.R;
import com.dk.pden.model.Thought;
import com.stfalcon.chatkit.messages.MessageHolders;

public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Thought> {

    private TextView senderName;

    public CustomIncomingTextMessageViewHolder(View itemView) {
        super(itemView);
        senderName = itemView.findViewById(R.id.senderName);
    }

    @Override
    public void onBind(Thought thought) {
        super.onBind(thought);
        senderName.setText(thought.user.getTarget().getBlockstackId());
    }
}