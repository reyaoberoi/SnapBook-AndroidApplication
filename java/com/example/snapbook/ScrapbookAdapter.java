package com.example.snapbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScrapbookAdapter extends RecyclerView.Adapter<ScrapbookAdapter.ScrapbookViewHolder> {

    private List<ScrapbookPage> pages;
    private ScrapbookJournalActivity context;
    private SimpleDateFormat dateFormat;

    public ScrapbookAdapter(List<ScrapbookPage> pages, ScrapbookJournalActivity context) {
        this.pages = pages;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ScrapbookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scrapbook_page_item, parent, false);
        return new ScrapbookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScrapbookViewHolder holder, int position) {
        ScrapbookPage page = pages.get(position);

        // Set title
        holder.titleText.setText(page.title);

        // Set date
        holder.dateText.setText(dateFormat.format(new Date(page.createdDate)));

        // Set preview info
        int imageCount = page.getImageCount();
        int textCount = page.getTextCount();
        holder.itemCountText.setText(imageCount + " photos • " + textCount + " notes");

        // Set preview text
        holder.previewText.setText(page.getPreviewText());

        // Load preview image
        String firstImagePath = page.getFirstImagePath();
        if (firstImagePath != null && new File(firstImagePath).exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(firstImagePath);
                if (bitmap != null) {
                    holder.previewImage.setImageBitmap(bitmap);
                    holder.previewImage.setVisibility(View.VISIBLE);
                    holder.placeholderIcon.setVisibility(View.GONE);
                } else {
                    setPlaceholderImage(holder);
                }
            } catch (Exception e) {
                setPlaceholderImage(holder);
            }
        } else {
            setPlaceholderImage(holder);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> context.editPage(page));
        
        holder.editButton.setOnClickListener(v -> context.editPage(page));
        
        holder.deleteButton.setOnClickListener(v -> context.deletePage(page));
    }

    private void setPlaceholderImage(ScrapbookViewHolder holder) {
        holder.previewImage.setVisibility(View.GONE);
        holder.placeholderIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public static class ScrapbookViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView dateText;
        TextView itemCountText;
        TextView previewText;
        ImageView previewImage;
        TextView placeholderIcon;
        Button editButton;
        Button deleteButton;

        public ScrapbookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.page_title);
            dateText = itemView.findViewById(R.id.page_date);
            itemCountText = itemView.findViewById(R.id.item_count);
            previewText = itemView.findViewById(R.id.preview_text);
            previewImage = itemView.findViewById(R.id.preview_image);
            placeholderIcon = itemView.findViewById(R.id.placeholder_icon);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}