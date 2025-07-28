package khohang;

import java.time.LocalDate;

public class SanPham {
    private String maSanPham;
    private String ten;
    private String donVi; // <<< SỬA TỪ "donViTinh" THÀNH "donVi"
    private int donGia;
    private LocalDate hanSuDung;
    private int soLuong;
    private String maKho;
    private String maNcc;

    public SanPham(String maSanPham, String ten, String donVi, int donGia, LocalDate hanSuDung, int soLuong, String maKho, String maNcc) {
        this.maSanPham = maSanPham;
        this.ten = ten;
        this.donVi = donVi; // <<< SỬA Ở ĐÂY
        this.donGia = donGia;
        this.hanSuDung = hanSuDung;
        this.soLuong = soLuong;
        this.maKho = maKho;
        this.maNcc = maNcc;
    }

    // --- Getters ---
    public String getMaSanPham() { return maSanPham; }
    public String getTen() { return ten; }
    public String getDonVi() { return donVi; } // <<< SỬA Ở ĐÂY
    public int getDonGia() { return donGia; }
    public LocalDate getHanSuDung() { return hanSuDung; }
    public int getSoLuong() { return soLuong; }
    public String getMaKho() { return maKho; }
    public String getMaNcc() { return maNcc; }

    @Override
    public String toString() {
        return String.format("Mã SP: %-10s | Tên: %-40s | SL: %-5d | HSD: %s", 
                             maSanPham, ten, soLuong, hanSuDung != null ? hanSuDung.toString() : "N/A");
    }
    
    // Cần thêm equals và hashCode để Stream.distinct() hoạt động chính xác
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPham sanPham = (SanPham) o;
        return maSanPham.equals(sanPham.maSanPham);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(maSanPham);
    }
}