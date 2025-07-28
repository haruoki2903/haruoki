package store.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public void themSanPham(String maSP, String tenSP, long donGia, int soLuong) {
        CartItem cartItem = items.get(maSP);
        if (cartItem != null) {
            // Nếu sản phẩm đã có, chỉ tăng số lượng
            cartItem.setSoLuongMua(cartItem.getSoLuongMua() + soLuong);
        } else {
            // Nếu chưa có, thêm mới
            items.put(maSP, new CartItem(maSP, tenSP, donGia, soLuong));
        }
    }

    public void capNhatSoLuong(String maSP, int soLuongMoi) {
        CartItem cartItem = items.get(maSP);
        if (cartItem != null) {
            if (soLuongMoi > 0) {
                cartItem.setSoLuongMua(soLuongMoi);
            } else {
                // Nếu số lượng mới <= 0, xóa khỏi giỏ
                items.remove(maSP);
            }
        }
    }

    public void xoaSanPham(String maSP) {
        items.remove(maSP);
    }
    
    public long getTongTien() {
        return items.values().stream().mapToLong(CartItem::getThanhTien).sum();
    }
    
    public int getSoLuongTongCong() {
        return items.values().stream().mapToInt(CartItem::getSoLuongMua).sum();
    }
    
    public Collection<CartItem> getItems() {
        return items.values();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}