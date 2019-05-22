package com.jbak.videos;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerUtils {
    public static int getFirstVisiblePos(RecyclerView.LayoutManager layoutManager){
        if(layoutManager instanceof LinearLayoutManager)
            return ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
        return 0;

    }

    public interface IPageLoader {
        boolean isLoading();
        void loadNextPage();
        boolean hasNextPage();
    }

    /** Обработчик скролла для загрузки постраничных данных */
    public static class PagedScrollListener extends RecyclerView.OnScrollListener {
        private int startAfter = 5;
        private IPageLoader pageLoader;
        public PagedScrollListener(IPageLoader loader){
            pageLoader = loader;
        }

        /** Устанавливает значение - за сколько элементов до последнего будет загружаться след страница */
        public PagedScrollListener setStartAfter(int startAfter){
            this.startAfter = startAfter;
            return this;
        }

        public void loadNextPage() {
            pageLoader.loadNextPage();
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(pageLoader.isLoading() || !pageLoader.hasNextPage())
                return;
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if(lm == null)
                return;
            int last = getFirstVisiblePos(lm) + lm.getChildCount();
            int total = lm.getItemCount();
            if (total - startAfter <= last){
                loadNextPage();
            }
        }
    }

}
