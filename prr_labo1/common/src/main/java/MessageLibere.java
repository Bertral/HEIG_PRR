public class MessageLibere extends Message {
    private int newValue;

    public MessageLibere(TYPE type, long estampille, int originSite, int newValue) {
        super(type, estampille, originSite);
        this.newValue = newValue;
    }
    
    public int getNewValue() {
        return newValue;
    }

}
