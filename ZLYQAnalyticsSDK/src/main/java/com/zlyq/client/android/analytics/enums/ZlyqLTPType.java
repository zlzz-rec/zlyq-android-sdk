package com.zlyq.client.android.analytics.enums;

public enum ZlyqLTPType {
    SCREEN_LTP_REFRESH(1),//下拉刷新
    SCREEN_LTP_NEXT_PAGE(2), //翻页
    SCREEN_LTP_NEXT_TAB(3),//标签切换
    SCREEN_LTP_POP_WINDOW(4), //局部弹出
    SCREEN_LTP_FILTER_REFRESH(5);//筛选刷新

    private int typeName;

    ZlyqLTPType(int typeName) {
        this.typeName = typeName;
    }

    /**
     * 根据类型的名称，返回类型的枚举实例。
     *
     * @param typeName 类型名称
     */
    public static ZlyqLTPType fromTypeName(int typeName) {
        for (ZlyqLTPType type : ZlyqLTPType.values()) {
            if (type.getTypeName() == typeName) {
                return type;
            }
        }
        return null;
    }

    public int getTypeName() {
        return this.typeName;
    }
}