package com.example.hostelconnect;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder> {

    private Cursor cursor;
    private boolean isOwnerView;
    private OnActionListener actionListener;

    public interface OnActionListener {
        void onAction(int leaveId, String action);
    }

    public LeaveRequestAdapter(Cursor cursor, boolean isOwnerView) {
        this.cursor = cursor;
        this.isOwnerView = isOwnerView;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leave_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {

            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int fromDateIndex = cursor.getColumnIndex("from_date");
            int toDateIndex = cursor.getColumnIndex("to_date");
            int reasonIndex = cursor.getColumnIndex("reason");
            int statusIndex = cursor.getColumnIndex("status");
            int requestedDateIndex = cursor.getColumnIndex("requested_date");
            int phoneIndex = cursor.getColumnIndex("phone");

            final int leaveId = idIndex != -1 ? cursor.getInt(idIndex) : -1;

            if (nameIndex != -1)
                holder.tvName.setText(cursor.getString(nameIndex));

            if (fromDateIndex != -1 && toDateIndex != -1)
                holder.tvDates.setText(
                        cursor.getString(fromDateIndex) + " to " +
                                cursor.getString(toDateIndex)
                );

            if (reasonIndex != -1)
                holder.tvReason.setText(cursor.getString(reasonIndex));

            if (statusIndex != -1) {
                String status = cursor.getString(statusIndex);
                holder.tvStatus.setText(status);

                int statusColor;
                switch (status) {
                    case "Approved":
                        statusColor = android.R.color.holo_green_dark;
                        break;
                    case "Rejected":
                        statusColor = android.R.color.holo_red_dark;
                        break;
                    default:
                        statusColor = android.R.color.holo_orange_dark;
                }

                holder.tvStatus.setTextColor(
                        holder.itemView.getContext()
                                .getResources()
                                .getColor(statusColor)
                );

                // Show buttons only for owner view AND pending status
                if (isOwnerView && "Pending".equals(status)) {
                    holder.layoutActionButtons.setVisibility(View.VISIBLE);
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);

                    holder.btnApprove.setOnClickListener(v -> {
                        if (actionListener != null)
                            actionListener.onAction(leaveId, "Approve");
                    });

                    holder.btnReject.setOnClickListener(v -> {
                        if (actionListener != null)
                            actionListener.onAction(leaveId, "Reject");
                    });

                } else {
                    holder.layoutActionButtons.setVisibility(View.GONE);
                    holder.btnApprove.setVisibility(View.GONE);
                    holder.btnReject.setVisibility(View.GONE);
                }
            }

            if (requestedDateIndex != -1)
                holder.tvRequestedDate.setText(
                        "Requested: " + cursor.getString(requestedDateIndex)
                );

            if (isOwnerView && phoneIndex != -1) {
                holder.tvPhone.setVisibility(View.VISIBLE);
                holder.tvPhone.setText("Phone: " + cursor.getString(phoneIndex));
            } else {
                holder.tvPhone.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void updateCursor(Cursor newCursor) {
        if (cursor != null && !cursor.isClosed()) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public void closeCursor() {
        if (cursor != null && !cursor.isClosed()) cursor.close();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvName, tvDates, tvReason, tvStatus, tvRequestedDate, tvPhone;
        LinearLayout layoutActionButtons;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardLeaveRequest);
            tvName = itemView.findViewById(R.id.tvName);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}