package com.jbak.videos.types;

import tenet.lib.base.Interfaces;

public interface IItem extends Interfaces.IdNamed {
    String ID = "[ID]";
    IItemList EMPTY_ITEM_LIST = new IItemList() {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public IItem getItem(int pos) {
            return null;
        }
    };

    String getImageUrl();
    int getDuration();

    interface IItemList {
        int getCount();
        IItem getItem(int pos);
    }

    interface IUrlItem extends IItem{
        String getVideoUrl();
    }

    interface IVideoUrlLoader {
        String loadVideoUrlSync() throws Throwable;
    }
}
