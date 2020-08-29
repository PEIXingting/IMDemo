package hk.edu.cuhk.ie.iems5722.group5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



import java.util.ArrayList;

public class MessagesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Messages> mMessages;
    private Context mContext;
    private int message_send = 0;
    private int message_receive = 1;
    private int location_send = 2;
    private int location_receive = 3;
    private int log = 4;

    private static class ViewHolder {
        TextView message;
        TextView username;
        TextView time;
        TextView log;
    }

    MessagesAdapter(Context mContext, ArrayList<Messages> mMessages) {
        this.mInflater = LayoutInflater.from(mContext);
        this.mMessages = mMessages;
        this.mContext = mContext;
    }



    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int i) {
        return mMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int i) {
        if (mMessages.get(i).getType() == message_send) {
            return message_send;
        } else if (mMessages.get(i).getType() == message_receive) {
            return message_receive;
        } else if (mMessages.get(i).getType() == location_send) {
            return location_send;
        } else if (mMessages.get(i).getType() == location_receive) {
            return location_receive;
        } else {
            return log;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        int type = mMessages.get(position).getType();
        if (type == message_send) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.messages_send, null);
                holder.username = convertView.findViewById(R.id.avatar_send_text);
                holder.message = convertView.findViewById(R.id.messages_send);
                holder.time = convertView.findViewById(R.id.time_send);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.username.setText(mMessages.get(position).getUsername());
            holder.message.setText(mMessages.get(position).getMessage());
            holder.time.setText(mMessages.get(position).getTime());
        } else if (type == message_receive) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.messages_receive, null);
                holder.username = convertView.findViewById(R.id.avatar_receive_text);
                holder.message = convertView.findViewById(R.id.messages_receive);
                holder.time = convertView.findViewById(R.id.time_receive);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.username.setText(mMessages.get(position).getUsername());
            holder.message.setText(mMessages.get(position).getMessage());
            holder.time.setText(mMessages.get(position).getTime());
        } else if (type == location_send) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.location_send, null);
                holder.username = convertView.findViewById(R.id.avatar_location_send_text);
                holder.message = convertView.findViewById(R.id.text_location_send);
                holder.time = convertView.findViewById(R.id.time_location_send);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.username.setText(mMessages.get(position).getUsername());
            holder.time.setText(mMessages.get(position).getTime());
            final String url = mMessages.get(position).getMessage();
            holder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, LocationActivity.class);
                    intent.putExtra("type", 1);
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                    ((Activity)mContext).overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);
                }
            });
        } else if (type == location_receive) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.location_receive, null);
                holder.username = convertView.findViewById(R.id.avatar_location_text_receive);
                holder.message = convertView.findViewById(R.id.text_location_receive);
                holder.time = convertView.findViewById(R.id.time_location_receive);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.username.setText(mMessages.get(position).getUsername());
            holder.time.setText(mMessages.get(position).getTime());
            final String url = mMessages.get(position).getMessage();
            holder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, LocationActivity.class);
                    intent.putExtra("type", 1);
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                    ((Activity)mContext).overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);
                }
            });
        } else if (type == log) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.log, null);
                holder.log = convertView.findViewById(R.id.log);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.log.setText(mMessages.get(position).getMessage());
        }
        return convertView;
    }
}
