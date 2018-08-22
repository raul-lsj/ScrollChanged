package com.raul.scrollchange;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recLeft;
    private RecyclerView recRight;
    private TextView rightTitle;

    private List<String> left;
    private List<ScrollBean> right;
    private ScrollLeftAdapter leftAdapter;
    private ScrollRightAdapter rightAdapter;
    //右侧title在数据中所对应的position集合
    private List<Integer> tPosition = new ArrayList<>();
    private Context mContext;
    //title的高度
    private int tHeight;
    //记录右侧当前title的索引
    private int first = 0;
    private GridLayoutManager rightManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        recLeft = findViewById(R.id.rec_left);
        recRight = findViewById(R.id.rec_right);
        rightTitle = findViewById(R.id.right_title);

        initData();

        initLeft();
        initRight();
    }

    private void initRight() {

        rightManager = new GridLayoutManager(mContext, 3);

        if (rightAdapter == null) {
            rightAdapter = new ScrollRightAdapter(R.layout.scroll_right, R.layout.layout_right_title, null);
            recRight.setLayoutManager(rightManager);
            recRight.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.set(dpToPx(mContext, getDimens(mContext, R.dimen.dp3))
                            , 0
                            , dpToPx(mContext, getDimens(mContext, R.dimen.dp3))
                            , dpToPx(mContext, getDimens(mContext, R.dimen.dp3)));
                }
            });
            recRight.setAdapter(rightAdapter);
        } else {
            rightAdapter.notifyDataSetChanged();
        }

        rightAdapter.setNewData(right);

        //设置右侧初始title
        if (right.get(first).isHeader) {
            rightTitle.setText(right.get(first).header);
        }

        recRight.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //获取右侧title的高度
                tHeight = rightTitle.getHeight();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //判断如果是header
                if (right.get(first).isHeader) {
                    //获取header的view
                    View view = rightManager.findViewByPosition(first);
                    if (view != null) {
                        //如果header顶部和父容器顶部距离大于等于tHeight,则设置偏移量
                        if (view.getTop() >= tHeight) {
                            rightTitle.setY(view.getTop() - tHeight);
                        } else {
                            //否则不设置
                            rightTitle.setY(0);
                        }
                    }
                }

                //因为每次滑动之后,右侧列表中可见的第一个item的position肯定会改变,并且右侧列表中可见的第一个item的position变换了之后,
                //才有可能改变右侧title的值,所以这个方法内的逻辑在右侧可见的第一个item的position改变之后一定会执行
                int firstPosition = rightManager.findFirstVisibleItemPosition();
                if (first != firstPosition && firstPosition >= 0) {
                    //给first赋值
                    first = firstPosition;
                    //不设置Y轴的偏移量
                    rightTitle.setY(0);

                    //判断如果右侧可见的第一个item是否是header,设置相应的值
                    if (right.get(first).isHeader) {
                        rightTitle.setText(right.get(first).header);
                    } else {
                        rightTitle.setText(right.get(first).t.getType());
                    }
                }

                //遍历左边列表,列表对应的内容等于右边的title,则设置左侧对应item高亮
                for (int i = 0; i < left.size(); i++) {
                    if (left.get(i).equals(rightTitle.getText().toString())) {
                        leftAdapter.selectItem(i);
                    }
                }

                //如果右边最后一个完全显示的item的position,等于bean中最后一条数据的position(也就是右侧列表拉到底了),
                //则设置左侧列表最后一条item高亮
                if (rightManager.findLastCompletelyVisibleItemPosition() == right.size() - 1) {
                    leftAdapter.selectItem(left.size() - 1);
                }
            }
        });
    }

    private void initLeft() {
        if (leftAdapter == null) {
            leftAdapter = new ScrollLeftAdapter(R.layout.scroll_left, null);
            recLeft.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            recLeft.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            recLeft.setAdapter(leftAdapter);
        } else {
            leftAdapter.notifyDataSetChanged();
        }

        leftAdapter.setNewData(left);

        leftAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    //点击左侧列表的相应item,右侧列表相应的title置顶显示
                    //(最后一组内容若不能填充右侧整个可见页面,则显示到右侧列表的最底端)
                    case R.id.item:
                        leftAdapter.selectItem(position);
                        rightManager.scrollToPositionWithOffset(tPosition.get(position), 0);
                        break;
                }
            }
        });
    }

    //获取数据(若请求服务端数据,请求到的列表需有序排列)
    private void initData() {
        left = new ArrayList<>();
        left.add("第一组");
        left.add("第二组略略略略略略略");
        left.add("第三组哈哈哈哈哈哈哈哈哈哈hahahahahahaha");
        left.add("第四组哈哈哈哈哈嗝~");
        left.add("第五组");
        left.add("第六组哎呀我去");
        left.add("第七组");

        right = new ArrayList<>();

        right.add(new ScrollBean(true, left.get(0)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("1111111", left.get(0))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("1111111", left.get(0))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("1111111", left.get(0))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("1111111", left.get(0))));

        right.add(new ScrollBean(true, left.get(1)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("2222222", left.get(1))));

        right.add(new ScrollBean(true, left.get(2)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("3333333", left.get(2))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("3333333", left.get(2))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("3333333", left.get(2))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("3333333", left.get(2))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("3333333", left.get(2))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("3333333", left.get(2))));

        right.add(new ScrollBean(true, left.get(3)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("4444444", left.get(3))));

        right.add(new ScrollBean(true, left.get(4)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("5555555", left.get(4))));

        right.add(new ScrollBean(true, left.get(5)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("6666666", left.get(5))));

        right.add(new ScrollBean(true, left.get(6)));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("7777777", left.get(6))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("7777777", left.get(6))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("7777777", left.get(6))));
        right.add(new ScrollBean(new ScrollBean.ScrollItemBean("7777777", left.get(6))));

        for (int i = 0; i < right.size(); i++) {
            if (right.get(i).isHeader) {
                //遍历右侧列表,判断如果是header,则将此header在右侧列表中所在的position添加到集合中
                tPosition.add(i);
            }
        }
    }

    /**
     * 获得资源 dimens (dp)
     *
     * @param context
     * @param id      资源id
     * @return
     */
    public float getDimens(Context context, int id) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float px = context.getResources().getDimension(id);
        return px / dm.density;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    public int dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5f);
    }
}
