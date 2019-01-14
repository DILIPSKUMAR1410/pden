package com.dk.pden.discuss.holder;

import android.view.View;
import android.widget.TextView;

import com.dk.pden.R;
import com.dk.pden.model.Thought;
import com.stfalcon.chatkit.messages.MessageHolders;

public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Thought> {

    private TextView senderName;
    private TextView isApproved;

    public CustomIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        senderName = itemView.findViewById(R.id.senderName);
        isApproved = itemView.findViewById(R.id.isApproved);

    }

    @Override
    public void onBind(Thought thought) {
        super.onBind(thought);
        senderName.setText(thought.user.getTarget().getBlockstackId());
        final Payload payload = (Payload) this.payload;
        if (payload.iamAdmin)
            if (thought.isApproved()) {
                isApproved.setText("Public");
            } else {
                isApproved.setText("Private");
            }
    }


    public static class Payload {
        public Boolean iamAdmin;
    }
}