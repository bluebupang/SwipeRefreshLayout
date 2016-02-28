# SwipeRefreshLayout

SwipeRefreshLayout--Google官方提出的下拉刷新控件，广泛应用在各种APP中。一直想弄一个既能支持下拉刷新，又能够上拉加载，同时还能实现类似于QQ的滑动删除效果。上网找了很多资料，最后达到了下面的效果：
![这里写图片描述](http://img.blog.csdn.net/20160223222654568)

参考资料：
SwipeRefreshLayout的上拉加载与下拉刷新：
> http://blog.csdn.net/u012036813/article/details/38959507

Listview的滑动删除：
> http://blog.csdn.net/lmj623565791/article/details/22961279

Android事件传递机制：

> http://blog.csdn.net/yanzi1225627/article/details/22592831

接下来看看具体实现过程。

**1.界面布局：**

```
 <com.example.swiperefreshlayoutdemo.RefreshLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/swipe_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <com.example.swiperefreshlayoutdemo.QQListView
            android:id="@+id/list"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" >
        </com.example.swiperefreshlayoutdemo.QQListView>
    </com.example.swiperefreshlayoutdemo.RefreshLayout>
```
因为要实现上拉加载以及滑动删除，所以用的都是自定义的RefreshLayout以及Listview。
**2.代码实现：**

SwipeRefreshLayout的上拉加载实现有两种方法，一种是**实现滑动监听**，当滑动到底部时实现加载更多功能；一种是**通过事件传递机制监听上拉动作**，然后实现加载更多功能。先看第一种加载原理：

自定义的SwipeRefreshLayout：
```
//继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.

public class RefreshLayout extends SwipeRefreshLayout implements	OnScrollListener {

	// listview实例
	private ListView mListView;
	// 上拉接口监听器, 到了最底部的上拉加载操作
	private OnLoadListener mOnLoadListener;
	// ListView的加载中footer
	private View mListViewFooter;
	// 是否在加载中 ( 上拉加载更多 )
	private boolean isLoading = false;

	public RefreshLayout(Context context) {
		this(context, null);
	}

	public RefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		//一个圆形进度条
		mListViewFooter = LayoutInflater.from(context).inflate(
				R.layout.listview_footer, null, false);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// 初始化ListView对象
		if (mListView == null) {
			getListView();
		}
	}

	// 获取ListView对象
	private void getListView() {
		int childs = getChildCount();
		if (childs > 0) {
			View childView = getChildAt(0);
			if (childView instanceof ListView) {
				mListView = (ListView) childView;
				// 设置滚动监听器给ListView
				mListView.setOnScrollListener(this);
			}
		}
	}

	

	// 设置加载状态,添加或者移除加载更多圆形进度条
	public void setLoading(boolean loading) {
		isLoading = loading;
		if (isLoading) {
			mListView.addFooterView(mListViewFooter);
		} else {
			mListView.removeFooterView(mListViewFooter);

		}
	}

	//设置监听器
	public void setOnLoadListener(OnLoadListener loadListener) {
		mOnLoadListener = loadListener;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		// 判断是否到了最底部，并且不是在加载数据的状态
		if (mListView.getLastVisiblePosition() == mListView.getAdapter()
				.getCount() - 1 && isLoading == false) {
			// 首先设置加载状态
			setLoading(true);
			// 调用加载数据的方法
			mOnLoadListener.onLoad();

		}

	}

	// 加载更多的接口
	public interface OnLoadListener {
		public void onLoad();
	}
}
```

注释写的很清楚，实现滑动监听，然后初始化listview，写好加载任务接口与方法。在滑动监听方法里面，有几个参数注意一下：
 **firstVisibleItem** 表示在当前屏幕显示的第一个listItem在整个listView里面的位置（下标从0开始）
**visibleItemCount**表示在现时屏幕可以见到的ListItem(部分显示的ListItem也算)总数
**totalItemCount**表示ListView的ListItem总数
**listView.getLastVisiblePosition()**表示在现时屏幕最后一个ListItem (最后ListItem要完全显示出来才算)在整个ListView的位置（下标从0开始）
刚开始只判断是否滑动到了最底部，没有对加载状态进行判断，导致程序运行崩溃，在最底部加载数据时会一直加载。后来加上判断，默认不加载数据，isLoading==false，滑动到最底部加载数据时，设置为true，当加载完成以后，设置为false，加载完毕。

接下来是第二种加载原理：

```
public class RefreshLayout extends SwipeRefreshLayout {

	// listview实例
	private ListView mListView;
	// 上拉接口监听器, 到了最底部的上拉加载操作
	private OnLoadListener mOnLoadListener;
	// ListView的加载中footer
	private View mListViewFooter;
	// 是否在加载中 ( 上拉加载更多 )
	private boolean isLoading = false;

	// 按下时的y坐标
	private int mYDown;
	// 抬起时的y坐标
	private int mLastY;
	// 滑动到最下面时的上拉操作
	private int mTouchSlop;

	public RefreshLayout(Context context) {
		this(context, null);
	}

	public RefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mListViewFooter = LayoutInflater.from(context).inflate(
				R.layout.listview_footer, null, false);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// 初始化ListView对象
		if (mListView == null) {
			getListView();
		}
	}

	// 获取ListView对象
	private void getListView() {
		int childs = getChildCount();
		if (childs > 0) {
			View childView = getChildAt(0);
			if (childView instanceof ListView) {
				mListView = (ListView) childView;
			}
		}
	}

	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 按下
			mYDown = (int) event.getRawY();
			break;

		case MotionEvent.ACTION_MOVE:
			// 移动
			mLastY = (int) event.getRawY();
			break;

		case MotionEvent.ACTION_UP:
			// 抬起
			if ((mYDown - mLastY) >= mTouchSlop && isLoading == false) {
				// 设置状态
				setLoading(true);
				//
				mOnLoadListener.onLoad();
			}
			break;
		default:
			break;
		}

		return super.dispatchTouchEvent(event);
	}

	// 设置加载状态
	public void setLoading(boolean loading) {
		isLoading = loading;
		if (isLoading) {
			mListView.addFooterView(mListViewFooter);
		} else {
			mListView.removeFooterView(mListViewFooter);

		}
	}

	// 设置监听器
	public void setOnLoadListener(OnLoadListener loadListener) {
		mOnLoadListener = loadListener;
	}

	// 加载更多的接口
	public interface OnLoadListener {
		public void onLoad();
	}
}
```

初始化数据都一样，不同的是记录了两个Y坐标，一个按下，一个抬起，用来判断滑动到底部时的上拉动作。**getScaledTouchSlop**是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件，如viewpager就是用这个距离来判断用户是否翻页。
然后就是通过事件传递机制，拿到view的三个动作：
**MotionEvent.ACTION_DOWN**  按下View，是所有事件的开始
**MotionEvent.ACTION_MOVE**   滑动事件
**MotionEvent.ACTION_UP**       与down对应，表示抬起
当滑动的距离大于或者等于要求距离并且加载状态为false时，开始设置状态，加载数据，原理和上面一样。

接下里就是MainActivity的相关代码：

```
public class MainActivity extends Activity implements OnRefreshListener,
		OnLoadListener {

	private RefreshLayout swipeLayout;
	private QQListView listView;
	private MyAdapter adapter;
	private List<Integer> list = new ArrayList<Integer>();
	private int y = 11;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		setData();
		setListener();
	}

	private void initView() {
		swipeLayout = (RefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

	}

	private void setData() {
		list = new ArrayList<>();
		for (int i = 3; i < 12; i++) {
			list.add(i);
		}

		listView = (QQListView) findViewById(R.id.list);
		adapter = new MyAdapter(this, list);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				Toast.makeText(getApplicationContext(),
						"这是第" + String.valueOf(position + 1) + "项",
						Toast.LENGTH_SHORT).show();

			}
		});

		listView.setDelButtonClickListener(new DelButtonClickListener() {

			@Override
			public void clickHappend(int position) {
				list.remove(position);
				refresh();
			}
		});

	}

	private void setListener() {
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setOnLoadListener(this);
	}

	@Override
	public void onRefresh() {
		swipeLayout.postDelayed(new Runnable() {

			@Override
			public void run() {
				// 更新数据
				list.clear();
				y = 12;
				for (int i = 0; i < 13; i++) {
					list.add(i);
				}
				refresh();

				// 更新完后调用该方法结束刷新
				swipeLayout.setRefreshing(false);
			}
		}, 1000);

	}

	@Override
	public void onLoad() {
		swipeLayout.postDelayed(new Runnable() {

			@Override
			public void run() {
				// 更新数据
				y++;
				list.add(y);
				refresh();
				// 更新完后调用该方法结束刷新
				swipeLayout.setLoading(false);
			}
		}, 1000);

	}

	private void refresh() {
		adapter.setList(list);
		adapter.notifyDataSetChanged();
	}

}
```
实现自带的下拉刷新接口方法以及我们自己定义的上拉加载方法，然后在各自的方法里进行业务操作，我这里模拟的是简单数字刷新。到这里，SwipeRefreshLayout的下拉刷新与上拉加载已经完成。最后就是SwipeRefreshLayout的滑动删除。

这里的滑动删除参考的是鸿洋的这篇博客，代码注释，讲解都很详细:

> http://blog.csdn.net/lmj623565791/article/details/22961279

滑动删除item原理就是通过回调拿到item的position，然后执行删除操作，更新适配器即可。难点就是**解决上拉加载与下拉刷新操作与listview侧滑删除操作冲突的问题**

```
@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		
		//获得动作类型
		int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (action) {

		case MotionEvent.ACTION_DOWN:
			xDown = x;
			yDown = y;
			/**
			 * 如果当前popupWindow显示，则直接隐藏，然后屏蔽ListView的touch事件的下传
			 */
			if (mPopupWindow.isShowing()) {
				dismissPopWindow();
				return false;
			}
			// 获得当前手指按下时的item的位置
			mCurrentViewPos = pointToPosition(xDown, yDown);
			View view = getChildAt(mCurrentViewPos - getFirstVisiblePosition());
			mCurrentView = view;
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = x;
			yMove = y;
			int dx = xMove - xDown;
			int dy = yMove - yDown;
			/**
			 * 判断是否是从右到左的滑动
			 */
			if (xMove < xDown && Math.abs(dx) > touchSlop
					&& Math.abs(dy) < touchSlop) {
				// Log.e(TAG, "touchslop = " + touchSlop + " , dx = " + dx +
				// " , dy = " + dy);
				isSliding = true;
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		/**
		 * 如果是从右到左的滑动才相应 
		 */
		if (isSliding) {
			switch (action) {
			case MotionEvent.ACTION_MOVE:

				int[] location = new int[2];
				// 获得当前item的位置x与y 
				mCurrentView.getLocationOnScreen(location);
				// 设置popupWindow的动画  
				mPopupWindow
						.setAnimationStyle(R.style.popwindow_delete_btn_anim_style);
				mPopupWindow.update();
				mPopupWindow.showAtLocation(mCurrentView, Gravity.LEFT
						| Gravity.TOP, location[0] + mCurrentView.getWidth(),
						location[1] + mCurrentView.getHeight() / 2
								- mPopupWindowHeight / 2);
				// 设置删除按钮的回调  
				mDelBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mListener != null) {
							mListener.clickHappend(mCurrentViewPos);
							mPopupWindow.dismiss();
						}
					}
				});
				// Log.e(TAG, "mPopupWindow.getHeight()=" + mPopupWindowHeight);

				break;
			case MotionEvent.ACTION_UP:
				isSliding = false;

			}
			//  相应滑动期间屏幕itemClick事件，避免发生冲突
			return true;
		}

		return super.onTouchEvent(ev);
	}

```

通过dispatchTouchEvent事件传递机制，设置当前是否响应用户滑动，然后在onTouchEvent中判断是否响应，如果响应则popupWindow以动画的形式展示出来。当然屏幕上如果存在PopupWindow，则屏幕ListView的滚动与Item的点击，以及从右到左滑动时屏幕Item的click事件都将被屏蔽。
其中关于dispatchTouchEvent，这里原理说的比较详细，就不班门弄斧了：

> http://blog.csdn.net/yanzi1225627/article/details/22592831

最后截图的时候比较卡，效果还是不错的。
