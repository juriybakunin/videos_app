package com.jbak.videos.view

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.jbak.setVisGone
import com.jbak.setVisInvis
import com.jbak.videos.R
import com.jbak.videos.types.IItem
import kotlinx.android.synthetic.main.load_view.view.*
import kotlinx.android.synthetic.main.video_player_view.view.*

class LoadView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context,attributeSet){
    constructor(context: Context) : this(context, null)
    init {
        LayoutInflater.from(context).inflate(R.layout.load_view,this, true)
        orientation = VERTICAL
        setBackgroundColor(Color.BLACK)
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun setLoad(load: Boolean){
        setVisInvis(load)
        if(load) {
            setError(null)
            mProgress.setVisGone(true)
        }
    }

    fun setError(text:CharSequence?) {
        val err = !TextUtils.isEmpty(text)
        mErrorText.setVisGone(err)
        mErrorText.setText(text)
        if(err) {
            setVisInvis(true)
            mProgress.setVisGone(false)
        }
    }

    fun setItem(item: IItem){
        mTitle.setText(item.name)
        if(!TextUtils.isEmpty(item.imageUrl)) {
            mImage.visibility = View.VISIBLE
            Glide.with(mImage.context)
                .load(item.imageUrl)
                .into(mImage)
        } else {
            mImage.visibility = View.INVISIBLE
        }
    }
}