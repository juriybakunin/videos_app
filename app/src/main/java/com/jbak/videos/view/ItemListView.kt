package com.jbak.videos.view

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jbak.videos.App
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import com.jbak.getItemIndex
import com.jbak.videos.DataLoader
import com.jbak.videos.R
import com.jbak.videos.RecyclerUtils
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideosList
import kotlinx.android.synthetic.main.item_view_search.view.*
import tenet.lib.base.Err
import tenet.lib.base.utils.Utils
import tenet.lib.base.utils.ViewUtils


class ItemListView : RecyclerView {
    var processClickImage: Boolean = true
    private lateinit var gridManager: GridLayoutManager
    lateinit var itemAdapter: ItemViewAdapter
    private var type : Type = Type.LIST
    private var pagedScrollListener: RecyclerUtils.PagedScrollListener? = null;
    var onItemClick :OnItemClick? = null
    var onImageItemClick :OnItemClick? = null
    var iPageLoader : RecyclerUtils.IPageLoader? = null


    constructor(context: Context, attributeSet: AttributeSet?, type: Type) : super(context,attributeSet) {
        init(attributeSet, type)

    }
    constructor(context: Context) : this(context, null, Type.LIST)
    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, Type.LIST)


    private fun init(attributeSet: AttributeSet?, type: Type) {
        gridManager = GridLayoutManager(context,1)
        layoutManager = gridManager
        itemAdapter = ItemViewAdapter();
        adapter = itemAdapter
        var t = type
        attributeSet?.run {
            val attrs = context.obtainStyledAttributes(this,R.styleable.ItemListView)
            t = Type.values()[attrs.getInt(R.styleable.ItemListView_itemType,0)]
            attrs.recycle()
        }
        setType(t)
//        vertItemDecoration = createColorDecoration(VERTICAL, Color.WHITE, 12);
//        addItemDecoration(vertItemDecoration)
    }

    fun clear() {
        itemAdapter.setList(IItem.EMPTY_ITEM_LIST)
        itemAdapter.notifyDataSetChanged()
    }

    fun setType(type: Type){
        this.type = type
        adapter = null
        adapter = itemAdapter
        setLayout()
    }

    fun endLoad(ok : Boolean, items : IItem.IItemList){
        itemAdapter.stopLoad()
        if(ok) {
            itemAdapter.setList(items)
        }
    }

    fun setDesign(itemDesign: ItemDesign?){
        itemAdapter.itemDesign = itemDesign
        if(itemAdapter.itemCount > 0){
            ViewUtils.updateVisibleItems(this)
        }
    }

    fun isLoad() : Boolean{
        return iPageLoader?.isLoading?:false
    }

    fun setPageLoader(iPageLoader:RecyclerUtils.IPageLoader?) : RecyclerUtils.PagedScrollListener?{
        this.iPageLoader?.let {
            if(it is DataLoader)
                it.itemsLoadListeners.unregisterListener(itemAdapter)
        }
        this.iPageLoader = iPageLoader
        pagedScrollListener?.run{
            removeOnScrollListener(this)
        }
        if(this.iPageLoader == null) {
            return null
        }

        pagedScrollListener = object:RecyclerUtils.PagedScrollListener(iPageLoader){
            override fun loadNextPage() {
                super.loadNextPage()
                itemAdapter.startLoad()
            }
        }
        addOnScrollListener(pagedScrollListener!!)
        if(iPageLoader is DataLoader) {
            iPageLoader.itemsLoadListeners.registerListener(itemAdapter)
        }
        return pagedScrollListener;
    }





    fun setLayout(conf: Configuration = App.res().configuration){
        val port = isPortrait(conf)
        gridManager.spanCount = type.getColumnCount(port)
        gridManager.orientation = type.getOrientation(port)
        gridManager.reverseLayout = type.reverseLayout(port)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLayout(newConfig)
    }


    fun setCurrent(currentId : String?, moveToCurrent: Boolean = false) {
        itemAdapter.currentItemId = currentId
        if(currentId != null && moveToCurrent && itemAdapter.items != null){
            val index = itemAdapter.items!!.getItemIndex(currentId)
            if(index >= 0)
                scrollToPosition(index)
        }
        ViewUtils.updateVisibleItems(this)

    }
    fun setItems(items: IItem.IItemList, currentId : String? = null, moveToCurrent: Boolean = false) {
        itemAdapter.currentItemId = currentId
        itemAdapter.setList(items)
        if(currentId != null && moveToCurrent){
            val index = items.getItemIndex(currentId)
            if(index >= 0)
                scrollToPosition(index)
        }
    }

    /** Типы элементов, которые отображаются в ItemView */
    enum class Type {
        LIST, RELATED, PREVIOUS, NEXT, SEARCH, TEXT, SETTING;
        fun getColumnCount(port: Boolean) : Int{
            return when(this){
                SEARCH-> if(port) 1 else 2
                LIST -> if(port) 2 else 3
//                RELATED -> if(port) 2 else 1
                else -> 1
            }
        }

        fun showText() : Boolean{
            return this != PREVIOUS && this != NEXT
        }

        fun showDuration() : Boolean{
            return this != PREVIOUS && this != NEXT
        }

        fun getLayoutId() : Int{
            return when(this) {
                LIST -> R.layout.item_view
                SEARCH -> R.layout.item_view_search
                TEXT -> R.layout.item_view_text
                SETTING -> R.layout.item_view_setting
                else -> R.layout.item_view_related
            }
        }

        fun getOrientation(port: Boolean) : Int{
            return when(this){
                RELATED -> HORIZONTAL
                TEXT -> HORIZONTAL
                else -> VERTICAL
            }
        }

        fun reverseLayout(port: Boolean): Boolean {
            return when(this){
                SEARCH -> true
                else -> false
            }
        }
    }

    interface OnItemClick {
        fun onItemClick(iItem: IItem, view: View)
    }

    companion object {

        fun isPortrait(conf : Configuration = App.res().configuration) : Boolean{
            return conf.orientation == Configuration.ORIENTATION_PORTRAIT
        }

        class SpacesItemDecoration(private val space: Int, private val horz: Boolean) : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val itemList = parent as ItemListView
                val columns = itemList.gridManager.spanCount;
                val pos = itemList.gridManager.getPosition(view)
                if (horz) {
                    outRect.left = 0
                    if (pos % columns > 0)
                        outRect.right = 0
                    else
                        outRect.right = space
                } else
                    outRect.bottom = space
            }
        }

        fun createColorDecoration(orientation: Int, color: Int, sizeDp: Int): DividerItemDecoration {
            val decor = DividerItemDecoration(App.get(), orientation)
            decor.setDrawable(ShapeDrawable().apply {
                intrinsicHeight = App.dpToPx(sizeDp)
                paint.color = color
            })
            return decor
        }

        class ItemViewHolder(context: Context, type: Type) : ViewHolder(ItemView(context,null, type)) {
            fun view(): ItemView {
                return itemView as ItemView
            }
        }

    }
    class ItemViewAdapter : Adapter<ItemViewHolder>(), OnClickListener, DataLoader.OnItemsLoaded {

        var itemDesign: ItemDesign? = null
        var currentItemId: String? = null
        override fun onClick(v: View) {
            var itemView : ItemView? = null
            val image = v.id == R.id.mImage || v.id == R.id.mImage2
            if(v is ImageView) {
                var par = v.parent
                while (par !=null && !(par is ItemView))
                    par = par.parent
                if(par is ItemView)
                    itemView = par
            } else if(v is ItemView) {
                itemView = v
            }
            if(itemView == null)
                return
            val itemList = itemView.parent as ItemListView
            val onItemClick = if(image) itemList.onImageItemClick else itemList.onItemClick
            itemView.videoItem?.let {
                onItemClick?.onItemClick(it, v)
            }
        }

        var items: IItem.IItemList? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val  vh = ItemViewHolder(parent.context, (parent as ItemListView).type)
            vh.view().setOnClickListener(this)
            if(parent.onImageItemClick != null) {
                if(vh.view().mImage != null && parent.processClickImage)
                    vh.view().mImage.setOnClickListener(this)
                if(vh.view().mImage2 != null)
                    vh.view().mImage2!!.setOnClickListener(this)
            }
            return vh
        }

        override fun getItemCount(): Int {
            return items?.getCount() ?: 0
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items?.getItem(position)
            if (item != null) {
                val cur = item.id.equals(currentItemId)
                val iv = holder.view();
                iv.setItem(item)
                iv.setCurrent(cur)
                itemDesign?.apply(iv,cur)

            }
        }

        override fun onItemsLoaded(err: Err, videosList: VideosList) {
            if(err.isOk) {
                setList(videosList)
            }
        }

        fun setList(list: IItem.IItemList) {
            items = list
            notifyDataSetChanged()
        }

        fun startLoad() {

        }

        fun stopLoad() {

        }
    }

}
