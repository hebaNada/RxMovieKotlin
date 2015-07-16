package com.taskworld.android.rxmovie.view.fragment

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import com.bumptech.glide.Glide
import com.taskworld.android.rxmovie.R
import com.taskworld.android.rxmovie.presentation.presenter.ItemListPresenter
import com.taskworld.android.rxmovie.presentation.presenter.holder.ItemListViewHolderPresenter
import com.taskworld.android.rxmovie.presentation.viewaction.ItemListViewAction
import com.taskworld.android.rxmovie.presentation.viewaction.holder.ItemListViewHolderViewAction
import fuel.util.build
import kotlinx.android.synthetic.fragment_item_list.itemListRecycler
import kotlinx.android.synthetic.recycler_item_list.view.itemListBackgroundImage
import kotlinx.android.synthetic.recycler_item_list.view.itemListTitleText
import reactiveandroid.scheduler.AndroidSchedulers
import reactiveandroid.support.v7.widget.scrolled
import reactiveandroid.util.liftObservable
import reactiveandroid.widget.text
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 7/15/15.
 */

class ItemListFragment : Fragment(), ItemListViewAction {

    //presenter
    val presenter = ItemListPresenter(this)

    //adapter
    val adapter = ItemListAdapter()

    var onFragmentAttached: ((Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_item_list, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super<Fragment>.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
    }

    fun setUpRecyclerView() {
        itemListRecycler.setAdapter(adapter)

        itemListRecycler.scrolled.subscribe {

            val visibleItemCount = itemListRecycler.manager.getChildCount()
            val totalItemCount = itemListRecycler.manager.getItemCount()
            val pastVisibleItems = itemListRecycler.manager.findFirstVisibleItemPosition()

            if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                presenter.loadMore()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)

        bindObservables()
    }

    fun bindObservables() {
        liftObservable(presenter.itemCount.observable.observeOn(AndroidSchedulers.mainThreadScheduler()), ::notifyAdapter)
    }

    override fun onStart() {
        super<Fragment>.onStart()

        presenter.onStart()
    }

    override fun onStop() {
        super<Fragment>.onStop()

        presenter.onStop()
    }

    override fun onAttach(activity: Activity?) {
        super<Fragment>.onAttach(activity)

        onFragmentAttached?.invoke(R.string.title_section1)
    }

    override fun onDetach() {
        super<Fragment>.onDetach()

        onFragmentAttached = null
    }

    fun notifyAdapter(_: Int) {
        adapter.notifyDataSetChanged()
    }

    inner class ItemListAdapter : RecyclerView.Adapter<ItemListViewHolder>() {

        var lastPosition = -1

        override fun getItemCount(): Int = presenter.itemCount.value

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemListViewHolder? {
            val v = LayoutInflater.from(parent?.getContext()).inflate(R.layout.recycler_item_list, parent, false)
            val viewHolder = ItemListViewHolder(v)
            return viewHolder
        }

        override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
            holder.presenter = presenter[position]
            holder.bindObservables()

            if (position > lastPosition) {
                holder.view.setScaleX(0.5f)
                holder.view.setScaleY(0.5f)

                build(holder.view.animate()) {
                    scaleX(1.0f)
                    scaleY(1.0f)
                    setDuration(400)
                }.start()

                lastPosition = position
            }
        }

        override fun onViewRecycled(holder: ItemListViewHolder) {
            holder.presenter.onStop()
        }

    }

    class ItemListViewHolder(val view: View) : RecyclerView.ViewHolder(view), ItemListViewHolderViewAction {

        var presenter: ItemListViewHolderPresenter by Delegates.notNull()

        fun bindObservables() {
            presenter.view = this

            view.itemListTitleText.text.bind(presenter.title)
            liftObservable(presenter.image.observable, ::setBackgroundImageUrl)
        }

        fun setBackgroundImageUrl(url: String) {
            Glide.with(view.getContext()).load(url).crossFade().into(view.itemListBackgroundImage)
        }

    }
}


