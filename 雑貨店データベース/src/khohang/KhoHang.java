package khohang;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KhoHang {
    private String maKho;
    private String tenKho;
    private List<SanPham> danhSachSanPham;

    public KhoHang(String maKho, String tenKho) {
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.danhSachSanPham = new ArrayList<>();
    }
    
    // Giả lập việc tải sản phẩm từ DB vào kho
    public void setDanhSachSanPham(List<SanPham> allProducts) {
        this.danhSachSanPham = allProducts.stream()
                .filter(sp -> sp.getMaKho().equals(this.maKho))
                .collect(Collectors.toList());
    }

    public String getTenKho() {
        return tenKho;
    }

    public List<SanPham> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    /**
     * Kiểm tra các sản phẩm có số lượng dưới một ngưỡng nhất định.
     * @param nguongSoLuongThap Ngưỡng để coi là sắp hết hàng.
     * @return Danh sách sản phẩm sắp hết hàng.
     */
    public List<SanPham> getSanPhamSapHetHang(int nguongSoLuongThap) {
        return danhSachSanPham.stream()
                .filter(sp -> sp.getSoLuong() < nguongSoLuongThap)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra các sản phẩm sắp hết hạn sử dụng.
     * @param soNgaySapHetHan Số ngày tính từ hôm nay để coi là sắp hết hạn.
     * @return Danh sách sản phẩm sắp hết hạn.
     */
    public List<SanPham> getSanPhamSapHetHan(int soNgaySapHetHan) {
        LocalDate homNay = LocalDate.now();
        LocalDate ngayHetHan = homNay.plusDays(soNgaySapHetHan);
        
        return danhSachSanPham.stream()
                .filter(sp -> !sp.getHanSuDung().isBefore(homNay) && sp.getHanSuDung().isBefore(ngayHetHan))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tổng số lượng tất cả các mặt hàng trong kho.
     * @return Tổng số lượng.
     */
    public int getTongSoLuongHang() {
        return danhSachSanPham.stream().mapToInt(SanPham::getSoLuong).sum();
    }
}