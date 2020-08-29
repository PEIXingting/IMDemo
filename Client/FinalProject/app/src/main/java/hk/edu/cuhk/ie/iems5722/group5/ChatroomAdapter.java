package hk.edu.cuhk.ie.iems5722.group5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class ChatroomAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<ChatRoom> mChatRoom;
    private Context mContext;
    private static class ViewHolder{
        Button button;
    }

    ChatroomAdapter(Context mContext, ArrayList<ChatRoom> mChatRoom) {
        this.mInflater = LayoutInflater.from(mContext);
        this.mChatRoom = mChatRoom;
        this.mContext = mContext;
    }


    @Override
    public int getCount() {
        return mChatRoom.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatRoom.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.chatroom_button, null);
            holder.button = convertView.findViewById(R.id.chatroom_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.button.setText(mChatRoom.get(position).getName());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("name", mChatRoom.get(position).getName());
                intent.putExtra("username", mChatRoom.get(position).getUsername());
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);

            }
        });
        return convertView;
    }
}
