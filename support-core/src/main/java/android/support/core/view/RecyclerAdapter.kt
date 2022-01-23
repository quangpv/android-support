package android.support.core.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class RecyclerAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRecyclerView: RecyclerView? = null
    private var mItems: List<T>? = null

    val items get() = mItems

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("unchecked_cast")
    open fun submit(newItems: List<T>?) {
        mItems = newItems
        notifyDataSetChanged()
    }

    private fun findLastVisibleItem(): Int {
        return when (val layoutManager = mRecyclerView!!.layoutManager) {
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
            else -> error("Not support ${layoutManager?.javaClass?.name}")
        }
    }

    private fun findFirstVisibleItem(): Int {
        return when (val layoutManager = mRecyclerView!!.layoutManager) {
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            else -> error("Not support ${layoutManager?.javaClass?.name}")
        }
    }

    fun setData(items: List<T>) {
        mItems = items
    }

    override fun getItemCount(): Int = mItems?.size ?: 0

    @Suppress("unchecked_cast")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderBindAble = (holder as? IHolder<T>) ?: return
        val newItem = mItems!![position]
        holderBindAble.bindIfNeeded(newItem)
    }

    @Suppress("unchecked_cast")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (!payloads.isNullOrEmpty()) (holder as? IHolder<T>)?.onChangedWith(payloads)
        else super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? IHolder<*>)?.onRecycled()
    }

}

interface DiffComparable<T> {
    fun isSameWith(item: T): Boolean = this == item
}

interface PairDiffComparable<T> {
    fun areItemsSame(old: T, new: T): Boolean = old == new
}

interface IHolder<T> {
    val item: T?

    @CallSuper
    @Suppress("unchecked_cast")
    fun bindIfNeeded(newItem: T) {
        val oldItem = this.item ?: return bind(newItem)

        val shouldNotUpdate = when {
            this is PairDiffComparable<*> -> {
                (this as PairDiffComparable<T>).areItemsSame(oldItem, newItem)
            }
            oldItem is DiffComparable<*> -> {
                (oldItem as DiffComparable<T>).isSameWith(newItem)
            }
            else -> false
        }
        if (shouldNotUpdate) return
        bind(newItem)
    }

    fun bind(item: T)
    fun onRecycled() {}
    fun onChangedWith(payload: Any? = null) {}
}

abstract class RecyclerHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView),
    IHolder<T> {

    constructor(parent: ViewGroup, @LayoutRes layoutId: Int) : this(
        LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
    )

    constructor(binding: ViewBinding) : this(binding.root)

    final override var item: T? = null
        private set

    override fun bind(item: T) {
        this.item = item
    }

    override fun onRecycled() {

    }

}
