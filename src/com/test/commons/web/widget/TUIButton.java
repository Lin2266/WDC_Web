package com.test.commons.web.widget;

/**
 * 用來產生 HTML 畫面上的按鈕.
 */
public class TUIButton {
    private StringBuilder html;
    private String id; //id 屬性
    private String text; //文字敘述
    private String title; //title 屬性
    private String cssClass; //CSS 的 class 名
    private String style; //CSS 的 style 敘述
    private String onclick; //JavaScript 之 onclick 敘述
    
    public TUIButton(String text) {
        this.text = text;
    }
    
    public String getId() {
        return id;
    }

    public TUIButton setId(String id) {
        this.html = null;
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    //由建構式設值
    //public Button setText(String text) {
    //    this.text = text;
    //    return this;
    //}
    
    public String getTitle() {
        return title;
    }

    public TUIButton setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCssClass() {
        return cssClass;
    }
    
    private TUIButton setCssClass(String cssClass) {
        this.html = null;
        this.cssClass = cssClass;
        return this;
    }
    
    //只是為了方便
    public TUIButton setClass(String cssClass) {
        return setCssClass(cssClass);
    }
    
    public String getStyle() {
        return style;
    }
    
    public TUIButton setStyle(String style) {
        this.html = null;
        this.style = style;
        return this;
    }
    
    public String getOnclick() {
        return onclick;
    }

    public TUIButton setOnclick(String onclick) {
        this.html = null;
        this.onclick = onclick;
        return this;
    }
    
    public String toHTML() {
        //TODO: 是否 title, text, onclick 等內容須避開 " 字元?
        if(this.html == null) {
            this.html = new StringBuilder().append("<button");
            if(getCssClass() != null)
                this.html.append(" class=\"").append(getCssClass()).append("\"");
            if(getStyle() != null)
                this.html.append(" style=\"").append(getStyle()).append("\"");
            if(getTitle() != null)
                this.html.append(" title=\"").append(getTitle()).append("\"");
            if(getOnclick() != null)
                this.html.append(" onclick=\"").append(getOnclick()).append("\"");
            this.html.append(">");
            if(getText() != null)
                this.html.append(getText());
            this.html.append("</button>");
        }
        return this.html.toString();
    }
    
    @Override
    public String toString() {
        return toHTML();
    }
}
