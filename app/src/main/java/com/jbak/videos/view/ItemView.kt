package com.jbak.videos.view

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jbak.videos.App
import com.jbak.videos.R
import com.jbak.videos.types.IItem
import kotlinx.android.synthetic.main.item_view.view.*
import tenet.lib.base.utils.TimeUtils

class ItemView : LinearLayout{
    var videoItem : IItem? = null;
    var mType : ItemListView.Type
    var mCenteredIcon : ImageView? = null;
    var mImage2 : ImageView? = null

    companion object {
        val IMAGE_OPTIONS = RequestOptions()
            .centerCrop();
    }
    constructor(context: Context, attrs: AttributeSet? = null, type: ItemListView.Type) : super(context, attrs) {
        mType = type
        attrs?.let {
            val ar = context.obtainStyledAttributes(it,R.styleable.ItemListView)
            mType = ItemListView.Type.values()[ar.getInt(R.styleable.ItemListView_itemType,0)]
            ar.recycle()
        }
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,ItemListView.Type.LIST)

    constructor(context: Context) : this(context,null,ItemListView.Type.LIST)


    private fun init() {
        LayoutInflater.from(context).inflate(mType.getLayoutId(),this,true);
        val pad = App.dpToPx(4);
        setPadding(pad,pad,pad,pad)
        if(mType == ItemListView.Type.SEARCH)
            setBackgroundColor(Color.WHITE)
        mCenteredIcon = findViewById(R.id.centered_icon)
        mCenteredIcon?.visibility = View.INVISIBLE
        mImage2 = findViewById(R.id.mImage2)
    }

    private fun loadImage(item: IItem) {
        if(mImage == null)
            return
        val url = item.getImageUrl()
        if(TextUtils.isEmpty(url))
            return
        Glide.with(context)
            .load(item.getImageUrl())
            .apply(IMAGE_OPTIONS)
            .into(mImage)

    }

    fun setItem(item: IItem) {
        videoItem = item
        loadImage(item)
        text.text = item.name
        text.visibility = if(mType.showText()) View.VISIBLE else View.GONE
        val d = item.getDuration()
        if(shortDesc != null) {
            if (d > 0 && mType.showText()) {
                shortDesc.text = TimeUtils.getTimeRangeText(item.getDuration(), true, null);
                shortDesc.visibility = View.VISIBLE
            } else {
                shortDesc.visibility = View.INVISIBLE

            }
        }

        if(mType == ItemListView.Type.NEXT || mType == ItemListView.Type.PREVIOUS) {
            mCenteredIcon?.run {
                val res = if(mType == ItemListView.Type.NEXT) R.drawable.ic_next else R.drawable.ic_prev
                setImageResource(res)
                visibility = View.VISIBLE
            }
        }
    }


}
