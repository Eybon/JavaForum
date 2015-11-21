package io.github.cr3ahal0.forum.server.impl.broadcast;

/**
 * Created by Maxime on 17/11/2015.
 */
public class HistoryAction {

    private ContentKind content;

    private ActionKind action;

    private Class classifier;

    private String data;

    public ActionKind getAction() {
        return action;
    }

    public void setAction(ActionKind action) {
        this.action = action;
    }

    public Class getClassifier() {
        return classifier;
    }

    public void setClassifier(Class classifier) {
        this.classifier = classifier;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ContentKind getContent() {
        return content;
    }

    public void setContent(ContentKind kind) {
        this.content = kind;
    }
}
