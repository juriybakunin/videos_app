package com.jbak.videos.view;

import java.util.HashMap;

public class ItemDesign extends HashMap<Integer,Integer> {
    public static ItemDesign DEFAULT = new ItemDesign();

    public static final int TEXT_COLOR = 1;
    public static final int TEXT_BACK_COLOR = 2;
    public static final int TEXT_COLOR_CURRENT = 3;
    public static final int TEXT_SHADOW = 4;
    public static final int TEXT_BACK_COLOR_CURRENT = 5;

    public ItemDesign set(int key, int val){
        put(key, val);
        return this;
    }

    private final void setProperty(ItemView itemView, boolean current, Integer key, Integer val){
        switch (key){
            case TEXT_COLOR:
                if(!current) {
                    itemView.getTextView().setTextColor(val);
                }
                break;
            case TEXT_COLOR_CURRENT:
                if(current) {
                    itemView.getTextView().setTextColor(val);
                }
                break;
            case TEXT_BACK_COLOR:
                if(!current) {
                    itemView.getTextView().setBackgroundColor(val);
                }
                break;
            case TEXT_BACK_COLOR_CURRENT:
                if(current) {
                    itemView.getTextView().setBackgroundColor(val);
                }
                break;
            case TEXT_SHADOW:
                if(val == 0){
                    itemView.getTextView().setShadowLayer(0,0,0,0);
                }
                break;
        }
    }
    public void apply(ItemView itemView, boolean current){
        if(isEmpty())
            return;
        for (Entry<Integer, Integer> e:entrySet()){
            setProperty(itemView, current, e.getKey(), e.getValue());
        }
    }

}
