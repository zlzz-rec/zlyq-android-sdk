
package com.zlyq.client.android.analytics;

import java.io.Serializable;

public class ViewNode implements Serializable {
    private static final long serialVersionUID = -1242947408632673572L;
    private String viewPosition;
    private String viewOriginalPath;
    private String viewPath;
    private String viewContent;
    private String viewType;

    public ViewNode(String viewContent, String viewType) {
        this(null, null, null, viewContent, viewType);
    }

    public ViewNode(String viewPosition, String viewOriginalPath, String viewPath) {
        this(viewPosition, viewOriginalPath, viewPath, null, null);
    }

    public ViewNode(String viewPosition, String viewOriginalPath, String viewPath, String viewContent, String viewType) {
        this.viewPosition = viewPosition;
        this.viewOriginalPath = viewOriginalPath;
        this.viewPath = viewPath;
        this.viewContent = viewContent;
        this.viewType = viewType;
    }

    public String getViewPosition() {
        return viewPosition;
    }

    public void setViewPosition(String viewPosition) {
        this.viewPosition = viewPosition;
    }

    public String getViewOriginalPath() {
        return viewOriginalPath;
    }

    public void setViewOriginalPath(String viewOriginalPath) {
        this.viewOriginalPath = viewOriginalPath;
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public String getViewContent() {
        return viewContent;
    }

    public void setViewContent(String viewContent) {
        this.viewContent = viewContent;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

}
