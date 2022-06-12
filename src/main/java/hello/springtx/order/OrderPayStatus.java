package hello.springtx.order;

public enum OrderPayStatus {

    WAIT("대기"),
    COMPLETE("완료");

    private final String description;

    OrderPayStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
