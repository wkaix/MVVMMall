package com.zwb.mvvm_mall.ui.goods.view

import android.content.Intent
import android.graphics.Typeface
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.zwb.mvvm_mall.base.view.BaseVMActivity
import com.zwb.mvvm_mall.bean.CommentEntity
import com.zwb.mvvm_mall.common.utils.StatusBarUtil
import com.zwb.mvvm_mall.common.utils.UIUtils
import com.zwb.mvvm_mall.ui.goods.viewmodel.GoodsViewModel
import com.zwb.mvvm_mall.ui.home.adapter.HomeGoodsAdapter
import kotlinx.android.synthetic.main.activity_goods_detail.*
import kotlinx.android.synthetic.main.layout_goodsdetail_comment.*
import kotlinx.android.synthetic.main.layout_goodsdetail_detail.*
import kotlinx.android.synthetic.main.layout_goodsdetail_goods.*
import kotlinx.android.synthetic.main.layout_goodsdetail_toolbar.*
import kotlin.math.min
import android.widget.LinearLayout
import com.youth.banner.listener.OnPageChangeListener
import com.zwb.mvvm_mall.bean.GoodsBannerEntity
import com.zwb.mvvm_mall.ui.goods.adapter.GoodsBannerAdapter
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer
import com.zwb.mvvm_mall.ui.goods.adapter.GoodsBannerAdapter.VideoHolder
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.zwb.mvvm_mall.R
import com.zwb.mvvm_mall.common.view.NumIndicator
import com.zwb.mvvm_mall.common.view.PersistentStaggeredGridLayoutManager


class GoodsDetailActivity : BaseVMActivity<GoodsViewModel>(){

    private var mScrollY = 0
    private var lastScrollY = 0

    var bannerPlayer: NormalGSYVideoPlayer? = null

    private var mCommentFragment: GoodsCommentFragment? = null

    override val layoutId = R.layout.activity_goods_detail
    override fun initView() {
        super.initView()
        StatusBarUtil.immersive(this)
        StatusBarUtil.setPaddingSmart(this, toolbar)
        ivBack.setOnClickListener {
            if(mCommentFragment!=null && mCommentFragment!!.isAdded){
                closeCommentFragment()
            }else{
                finish()
            }
        }
        val goodsName = intent.getStringExtra("goodsName")
        tvGoodsName.text = goodsName
        radioTabs.alpha = 0f
        toolbar.setBackgroundColor(0)

        initBannerData()
        initListener()
    }
    private fun initListener(){
        detailScrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            var h = UIUtils.dp2px(170f)
            var color = ContextCompat.getColor(this@GoodsDetailActivity, R.color.white) and 0x00ffffff

            override fun onScrollChange(
                v: NestedScrollView,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                if (lastScrollY < h) {
                    mScrollY = min(h, scrollY)
                    radioTabs.alpha = 1f * mScrollY / h
                    toolbar.setBackgroundColor(255 * mScrollY / h shl 24 or color)
                }
//                if (lastScrollY>detailBanner.height+toolbar.height) {
//                    if(bannerPlayer?.isInPlayingState!!){
//                        mainPlayer.visibility = View.VISIBLE
//                        mainPlayer.setUp("https://vod.300hu.com/4c1f7a6atransbjngwcloud1oss/444b0379221736599300878337/v.f30.mp4?dockingId=8f3e428b-c281-4f33-9395-85c40b1fd12b1", true, null)
//                        mainPlayer.backButton.visibility = View.GONE
//                        mainPlayer.fullscreenButton.visibility = View.GONE
//                        mainPlayer.startPlayLogic()
//                    }
//                }else{
//                    mainPlayer.visibility = View.GONE
//                    if(!bannerPlayer?.isInPlayingState!!){
//                        bannerPlayer?.startPlayLogic()
//                     }
//
//                }
                lastScrollY = scrollY
                if (scrollY == 0) {
                    ivShare.setImageResource(R.mipmap.iv_share_white)
                    ivShare.setBackgroundResource(R.drawable.round_black_background)
                    ivMessage.setImageResource(R.mipmap.iv_message_white)
                    ivMessage.setBackgroundResource(R.drawable.round_black_background)
                    ivBack.setImageResource(R.mipmap.iv_back_white)
                    ivBack.setBackgroundResource(R.drawable.round_black_background)
                    StatusBarUtil.darkMode(this@GoodsDetailActivity,false)
                } else {
                    ivShare.setImageResource(R.mipmap.iv_share_black)
                    ivShare.setBackgroundResource(0)
                    ivMessage.setImageResource(R.mipmap.iv_message_black)
                    ivMessage.setBackgroundResource(0)
                    ivBack.setImageResource(R.mipmap.iv_back_black)
                    ivBack.setBackgroundResource(0)
                    StatusBarUtil.darkMode(this@GoodsDetailActivity,true)
                }
                when {
                    lastScrollY+500<layoutComment.top ->{
                        radioTabs.check(R.id.rbGoods)
                        setRadioButtonStyle(rbGoods)
                    }
                    lastScrollY+toolbar.height>layoutComment.top && lastScrollY+toolbar.height<layoutDetail.top -> {
                        radioTabs.check(R.id.rbComment)
                        setRadioButtonStyle(rbComment)
                    }
                    lastScrollY+toolbar.height>layoutDetail.top && lastScrollY+toolbar.height<tvRecommend.top-> {
                        radioTabs.check(R.id.rbDetail)
                        setRadioButtonStyle(rbDetail)
                    }
                    lastScrollY+toolbar.height>tvRecommend.top-> {
                        radioTabs.check(R.id.rbRecommend)
                        setRadioButtonStyle(rbRecommend)
                    }
                }
            }
        })
        rbGoods.setOnClickListener {
            detailScrollView.scrollTo(0,layoutGoods.top-toolbar.height)
            setRadioButtonStyle(rbGoods)
        }
        rbComment.setOnClickListener {
            detailScrollView.scrollTo(0,layoutComment.top-toolbar.height)
            setRadioButtonStyle(rbComment)
        }
        rbDetail.setOnClickListener {
            detailScrollView.scrollTo(0,layoutDetail.top-toolbar.height)
            setRadioButtonStyle(rbDetail)
        }
        rbRecommend.setOnClickListener {
            detailScrollView.scrollTo(0,tvRecommend.top-toolbar.height)
            setRadioButtonStyle(rbRecommend)
        }
        tvSeeAllComment.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.push_left_in,
                R.anim.push_left_out)
            mCommentFragment = GoodsCommentFragment.newInstance(10)
            transaction.add(R.id.frameComment, mCommentFragment!!)
            transaction.commit()
            tvTitleComment.visibility  = View.VISIBLE
            radioTabs.visibility  = View.GONE
        }
    }

    private fun setRadioButtonStyle(selectedRB: RadioButton){
        rbGoods.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        rbGoods.setCompoundDrawables(null, null, null, null)
        rbComment.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        rbComment.setCompoundDrawables(null, null, null, null)
        rbDetail.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        rbDetail.setCompoundDrawables(null, null, null, null)
        rbRecommend.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        rbRecommend.setCompoundDrawables(null, null, null, null)

        // 使用代码设置drawableTop
        val drawable = resources.getDrawable(R.mipmap.red_o_line)
        // 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        selectedRB.setCompoundDrawables(null, null, null, drawable)
        selectedRB.typeface = Typeface.defaultFromStyle(Typeface.BOLD)

    }
    override fun initData() {
        super.initData()
        mViewModel.loadSeckillGoodsData()
        mViewModel.loadCommentList()
    }

    override fun initDataObserver() {
        super.initDataObserver()
        mViewModel.mSeckillGoods.observe(this, Observer {
            detailRecyclerView.layoutManager = PersistentStaggeredGridLayoutManager(2)
            detailRecyclerView.adapter = HomeGoodsAdapter(it.toMutableList())

            recommendRecyclerView.layoutManager = GridLayoutManager(this,3)
            recommendRecyclerView.adapter = HomeGoodsAdapter(it.toMutableList().subList(0,6),R.layout.item_round_goods_layout)
        })
        mViewModel.mCommentList.observe(this, Observer {
            setCommentData(it)
        })
    }

    private fun initBannerData() {
        detailBanner.addBannerLifecycleObserver(this)
        detailBanner.indicator = NumIndicator(this)
        detailBanner.isAutoLoop(false)
        detailBanner.adapter = GoodsBannerAdapter(this,GoodsBannerEntity.getTestData())
        detailBanner.addOnPageChangeListener(object :OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if(bannerPlayer==null){
                    val viewHolder = detailBanner.adapter.viewHolder
                    if (viewHolder is VideoHolder) {
                        bannerPlayer = viewHolder.player
                    }
                }
            }

            override fun onPageSelected(position: Int) {
            }

        })
    }

    private fun setCommentData(list: List<CommentEntity>){
        Glide.with(this).load(list[0].userIcon).into(ivCommentHeader)
        tvCommentUser.text = list[0].userName
        tvCommentDate.text = list[0].date
        tvComment.text = list[0].context

        val lp = LinearLayout.LayoutParams((UIUtils.getScreenWidth()-UIUtils.dp2px(32f))/4,(UIUtils.getScreenWidth()-UIUtils.dp2px(32f))/4)
        lp.setMargins(UIUtils.dp2px(2f), UIUtils.dp2px(2f), UIUtils.dp2px(2f), UIUtils.dp2px(2f))
        list[0].pics.forEach {
            val image = ImageView(this)
            image.layoutParams = lp
            Glide.with(this).load(it.url).into(image)
            layoutPics.addView(image)
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if(mCommentFragment!=null && mCommentFragment!!.isAdded){
                closeCommentFragment()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun closeCommentFragment(){
        tvTitleComment.visibility  = View.GONE
        radioTabs.visibility  = View.VISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.push_left_in,
            R.anim.push_left_out)
        transaction.remove(mCommentFragment!!)
        transaction.commit()
    }
    override fun onPause() {
        super.onPause()
        bannerPlayer?.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        bannerPlayer?.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        //释放所有
        bannerPlayer?.setVideoAllCallBack(null)
        super.onBackPressed()
    }
    companion object{
        fun launch(activity: FragmentActivity,goodsName:String) =
            activity.apply {
                val intent = Intent(this, GoodsDetailActivity::class.java)
                intent.putExtra("goodsName",goodsName)
                startActivity(intent)
            }
    }
}

