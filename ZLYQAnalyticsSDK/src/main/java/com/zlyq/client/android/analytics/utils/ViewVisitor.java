
package com.zlyq.client.android.analytics.utils;

import android.annotation.TargetApi;
import android.view.View;

import java.util.List;

@TargetApi(16)
public abstract class ViewVisitor implements Pathfinder.Accumulator {

    private static final String TAG = "SA.ViewVisitor";
    private final List<Pathfinder.PathElement> mPath;
    private final Pathfinder mPathfinder;

    protected ViewVisitor(List<Pathfinder.PathElement> path) {
        mPath = path;
        mPathfinder = new Pathfinder();
    }

    public void visit(View rootView) {
        mPathfinder.findTargetsInRoot(rootView, mPath, this);
    }

    /**
     * 清除所有事件监听，调用后ViewVisitor将失效
     */
    public abstract void cleanup();

    protected abstract String name();
}
