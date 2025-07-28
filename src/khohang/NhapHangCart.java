// File: khohang/NhapHangCart.java
package khohang;

import java.util.LinkedHashMap;
import java.util.Map;

public class NhapHangCart {
    private static NhapHangCart instance;
    
    // Lớp nội tại để lưu thông tin chi tiết của mỗi mục trong giỏ hàng
    public static class CartItem {
        public String maSP;
        public String tenSP;
        public int soLuong;
        public int giaNhap;

        public CartItem(String maSP, String tenSP, int soLuong, int giaNhap) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.soLuong = soLuong;
            this.giaNhap = giaNhap;
        }
    }

    // Sử dụng Map để lưu trữ các mục, với key là mã sản phẩm
    private final Map<String, CartItem> itemsToOrder;

    private NhapHangCart() {
        itemsToOrder = new LinkedHashMap<>();
    }

    public static synchronized NhapHangCart getInstance() {
        if (instance == null) {
            instance = new NhapHangCart();
        }
        return instance;
    }

    /**
     * Thêm hoặc cập nhật một sản phẩm vào danh sách chờ nhập.
     * Nếu sản phẩm đã tồn tại, nó sẽ được cập nhật thông tin mới.
     * @param maSP Mã sản phẩm.
     * @param tenSP Tên sản phẩm.
     * @param soLuong Số lượng nhập.
     * @param giaNhap Giá nhập.
     */
    public void addItem(String maSP, String tenSP, int soLuong, int giaNhap) {
        CartItem item = new CartItem(maSP, tenSP, soLuong, giaNhap);
        itemsToOrder.put(maSP, item); // put sẽ tự động ghi đè nếu mã SP đã tồn tại
    }

    /**
     * Lấy toàn bộ Map của các sản phẩm (Mã SP -> CartItem).
     * @return Map<String, CartItem> chứa các sản phẩm.
     */
    public Map<String, CartItem> getItems() {
        return new LinkedHashMap<>(itemsToOrder); // Trả về một bản sao để đảm bảo an toàn
    }

    /**
     * Xóa sạch giỏ hàng sau khi đã sử dụng.
     */
    public void clearCart() {
        itemsToOrder.clear();
    }
    
    /**
     * Lấy số lượng sản phẩm khác nhau trong giỏ hàng.
     * @return số lượng sản phẩm.
     */
    public int getItemCount() {
        return itemsToOrder.size();
    }
}