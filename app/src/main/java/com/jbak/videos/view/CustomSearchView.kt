package com.jbak.videos.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
import android.widget.EditText
import androidx.appcompat.R
import androidx.appcompat.widget.SearchView

class CustomSearchView : SearchView
    {

    var onSearchShowChange : OnSearchShowChangeListener? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?):super(context,attributeSet){
        init()
    }
    lateinit var mEditor : EditText ;
    private fun init() {
        mEditor = findViewById(R.id.search_src_text)
//        setOnCloseListener(this)
        //setOnSearchClickListener(this)
    }

    fun replaceSelectedText(text: String){
        val t = mEditor.text
        if(TextUtils.isEmpty(t)){
            mEditor.setText(text)
            mEditor.setSelection(text.length)
            return
        }
        val str = t.toString();
        var data = str.substring(0,mEditor.selectionStart)+text
        val sel = data.length
        data = data + str.substring(mEditor.selectionEnd)
        mEditor.setText(data)
        mEditor.setSelection(sel)
    }

    override fun onActionViewExpanded() {
        super.onActionViewExpanded()
        onSearchShowChange?.onSearchShowChange(true)
    }

    override fun onActionViewCollapsed() {
        super.onActionViewCollapsed()
        onSearchShowChange?.onSearchShowChange(false)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if(!isIconified && event.action == ACTION_DOWN && event.keyCode == KEYCODE_BACK){
            isIconified = true
        }
        return super.dispatchKeyEventPreIme(event)
    }

//    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
//        event?.run {
//            if(action == ACTION_UP && keyCode == KEYCODE_BACK)
//                this@CustomSearchView.isIconified = true
//        }
//        return super.onKeyPreIme(keyCode, event)
//    }

    interface OnSearchShowChangeListener {
        fun onSearchShowChange(visible : Boolean)
    }


}