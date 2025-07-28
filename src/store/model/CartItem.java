package store.model;

public class CartItem {
    private String maSP;
    private String tenSP;
    private long donGia;
    private int soLuongMua;

    public CartItem(String maSP, String tenSP, long donGia, int soLuongMua) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.donGia = donGia;
        this.soLuongMua = soLuongMua;
    }
    
    public long getThanhTien() {
        return donGia * soLuongMua;
    }

    // Thêm các hàm getters và setters
    public String getMaSP() { return maSP; }
    public String getTenSP() { return tenSP; }
    public long getDonGia() { return donGia; }
    public int getSoLuongMua() { return soLuongMua; }
    public void setSoLuongMua(int soLuongMua) { this.soLuongMua = soLuongMua; }
}