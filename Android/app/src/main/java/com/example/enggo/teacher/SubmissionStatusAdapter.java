package com.example.enggo.teacher;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubmissionStatusAdapter extends RecyclerView.Adapter<SubmissionStatusAdapter.ViewHolder> {

    public interface OnSubmissionClickListener {
        void onClick(SubmissionStatusResponse submission);
    }

    private final List<SubmissionStatusResponse> submissions;
    private final OnSubmissionClickListener listener;

    public SubmissionStatusAdapter(List<SubmissionStatusResponse> submissions, OnSubmissionClickListener listener) {
        this.submissions = submissions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubmissionStatusResponse submission = submissions.get(position);
        holder.tvStudentName.setText(submission.getDisplayName());

        boolean submitted = submission.submitted;
        if (!submitted) {
            if (isPastDeadline(submission.deadline)) {
                holder.tvGradeStatus.setText("Missing");
                holder.tvGradeStatus.setBackgroundColor(0xFFFFEBEE);
                holder.tvGradeStatus.setTextColor(0xFFD32F2F);
            } else {
                holder.tvGradeStatus.setText("No submission");
                holder.tvGradeStatus.setBackgroundColor(0xFFF3E5F5);
                holder.tvGradeStatus.setTextColor(0xFF6A1B9A);
            }
            holder.layoutFile.setVisibility(View.GONE);
        } else {
            holder.tvGradeStatus.setText("Submitted");
            holder.tvGradeStatus.setBackgroundColor(0xFFE8F5E9);
            holder.tvGradeStatus.setTextColor(0xFF2E7D32);
            bindFileName(holder, submission.fileUrl);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(submission));
    }

    private String formatScore(Double score) {
        if (score == null) {
            return "";
        }
        if (score == Math.floor(score)) {
            return String.valueOf(score.intValue());
        }
        return String.valueOf(score);
    }

    private boolean isPastDeadline(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return false;
        }
        Date parsed = parseDate(deadline.trim());
        if (parsed == null) {
            return false;
        }
        return parsed.before(new Date());
    }

    private Date parseDate(String value) {
        SimpleDateFormat[] inputs = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        };
        for (SimpleDateFormat input : inputs) {
            try {
                Date parsed = input.parse(value);
                if (parsed != null) {
                    return parsed;
                }
            } catch (ParseException ignored) {
                // try next
            }
        }
        return null;
    }

    private void bindFileName(ViewHolder holder, String fileUrl) {
        if (TextUtils.isEmpty(fileUrl)) {
            holder.layoutFile.setVisibility(View.GONE);
            return;
        }
        holder.layoutFile.setVisibility(View.VISIBLE);
        String fileName = fileUrl;
        int slash = fileUrl.lastIndexOf('/');
        if (slash >= 0 && slash < fileUrl.length() - 1) {
            fileName = fileUrl.substring(slash + 1);
        }
        holder.tvFileName.setText(fileName);
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvStudentName;
        final TextView tvFileName;
        final TextView tvGradeStatus;
        final LinearLayout layoutFile;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvGradeStatus = itemView.findViewById(R.id.tvGradeStatus);
            layoutFile = itemView.findViewById(R.id.layoutFileAttachment);
        }
    }
}
