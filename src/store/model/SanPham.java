package store.model;

public class SanPham {
    private String maSP;
    private String ten;
    private long donGia;
    private int soLuongTon;
    private String hinhAnhPath;
    private String tenNhaCungCap; // <<<< ĐÃ THÊM

    public SanPham(String maSP, String ten, long donGia, int soLuongTon, String hinhAnhPath, String tenNhaCungCap) {
        this.maSP = maSP;
        this.ten = ten;
        this.donGia = donGia;
        this.soLuongTon = soLuongTon;
        this.hinhAnhPath = hinhAnhPath;
        this.tenNhaCungCap = tenNhaCungCap; // <<<< ĐÃ CẬP NHẬT
    }

    // --- Getters và Setters ---
    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = donGia; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public String getHinhAnhPath() { return hinhAnhPath; }
    public void setHinhAnhPath(String hinhAnhPath) { this.hinhAnhPath = hinhAnhPath; }
    
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }
}