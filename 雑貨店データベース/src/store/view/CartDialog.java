package store.view;

import databaseconnector.DatabaseConnector;
import mainui.MainUI;
import store.model.Cart;
import store.model.CartItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CartDialog extends JDialog {
    private final Cart cart;
    private final MainUI mainUiRef;
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalAmountLabel;
    private JComboBox<String> customerComboBox;
    private final Map<String, String> customerMap = new LinkedHashMap<>();

    public CartDialog(Frame parent, Cart cart, MainUI mainUiRef) {
        super(parent, "Giỏ hàng và Thanh toán", true);
        this.cart = cart;
        this.mainUiRef = mainUiRef;

        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initUI();
        loadCartData();
        loadCustomers();
    }

    private void initUI() {
        // --- Bảng sản phẩm trong giỏ ---
        String[] columnNames = {"Sản phẩm", "Đơn giá", "Số lượng", "Thành tiền", "Xóa"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Chỉ cho phép sửa cột "Số lượng" và "Xóa"
                return column == 2 || column == 4;
            }
        };
        cartTable = new JTable(tableModel);
        setupCartTableColumns();

        JScrollPane tableScrollPane = new JScrollPane(cartTable);
        
        // --- Panel dưới cùng (Thông tin khách hàng và thanh toán) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel khách hàng
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerPanel.add(new JLabel("Chọn khách hàng:"));
        customerComboBox = new JComboBox<>();
        customerComboBox.setPreferredSize(new Dimension(250, 30));
        customerPanel.add(customerComboBox);

        // Panel tổng tiền và nút thanh toán
        JPanel checkoutPanel = new JPanel(new BorderLayout());
        totalAmountLabel = new JLabel("Tổng cộng: 0 VNĐ");
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton checkoutButton = new JButton("XÁC NHẬN THANH TOÁN");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setBackground(new Color(34, 34, 34));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setPreferredSize(new Dimension(100, 50));
        checkoutButton.addActionListener(e -> processCheckout());

        checkoutPanel.add(totalAmountLabel, BorderLayout.CENTER);
        checkoutPanel.add(checkoutButton, BorderLayout.EAST);

        bottomPanel.add(customerPanel, BorderLayout.NORTH);
        bottomPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // Thêm các thành phần vào dialog
        getContentPane().add(tableScrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupCartTableColumns() {
        cartTable.setRowHeight(40);
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        // Cột số lượng: Sử dụng JSpinner để chỉnh sửa
        TableColumn quantityColumn = cartTable.getColumnModel().getColumn(2);
        quantityColumn.setCellEditor(new SpinnerEditor());

        // Cột xóa: Sử dụng một JButton
        TableColumn removeColumn = cartTable.getColumnModel().getColumn(4);
        removeColumn.setCellRenderer(new ButtonRenderer("Xóa"));
        removeColumn.setCellEditor(new ButtonEditor(new JCheckBox(), "Xóa"));
    }

    private void loadCartData() {
        tableModel.setRowCount(0);
        for (CartItem item : cart.getItems()) {
            tableModel.addRow(new Object[]{
                item.getTenSP(),
                item.getDonGia(),
                item.getSoLuongMua(),
                item.getThanhTien(),
                "Xóa"
            });
        }
        updateTotalAmount();
    }
    
    private void loadCustomers() {
        customerMap.clear();
        customerComboBox.removeAllItems();
        // Thêm lựa chọn khách vãng lai
        customerComboBox.addItem("Khách vãng lai");
        customerMap.put("Khách vãng lai", null);
        
        String sql = "SELECT ma_khach, ten FROM khachhang ORDER BY ten";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ten = rs.getString("ten");
                String ma = rs.getString("ma_khach");
                customerMap.put(ten, ma);
                customerComboBox.addItem(ten);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTotalAmount() {
        long total = cart.getTongTien();
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalAmountLabel.setText("Tổng cộng: " + currencyFormatter.format(total));
    }

    private void processCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedCustomerName = (String) customerComboBox.getSelectedItem();
        String maKhachHang = customerMap.get(selectedCustomerName);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán cho đơn hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Bắt đầu TRANSACTION

            // 1. Tạo đơn xuất mới
            String maXuat = generateNewMaXuat(conn);

            String sqlInsertDonXuat = "INSERT INTO donxuat (ma_xuat, ma_khach, ngay_xuat, tong_tien) VALUES (?, ?, CURRENT_DATE, 0)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertDonXuat)) {
                ps.setString(1, maXuat);
                ps.setString(2, maKhachHang); // Sẽ là NULL nếu là khách vãng lai
                ps.executeUpdate();
            }

            // 2. Thêm chi tiết đơn xuất
            String sqlInsertChiTiet = "INSERT INTO chitietxuat (ma_xuat, ma_san_pham, so_luong) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertChiTiet)) {
                for (CartItem item : cart.getItems()) {
                    ps.setString(1, maXuat);
                    ps.setString(2, item.getMaSP());
                    ps.setInt(3, item.getSoLuongMua());
                    ps.addBatch();
                }
                ps.executeBatch(); // Trigger trong DB sẽ tự động trừ kho và cập nhật tổng tiền
            }
            
            conn.commit(); // Hoàn thành TRANSACTION
            
            JOptionPane.showMessageDialog(this, "Thanh toán thành công! Đã tạo đơn xuất: " + maXuat, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // 3. Dọn dẹp
            cart.clear();
            if (mainUiRef != null) {
                mainUiRef.refreshKpis();
            }
            dispose(); // Đóng dialog

        } catch (SQLException ex) {
            try { if (conn != null) conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            // Xử lý lỗi đặc biệt (không đủ hàng) từ trigger
            if (ex.getMessage() != null && ex.getMessage().contains("KHONG DU HANG")) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Không đủ hàng trong kho", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thanh toán: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private String generateNewMaXuat(Connection conn) throws SQLException {
        String sql = "SELECT ma_xuat FROM donxuat ORDER BY CAST(SUBSTRING(ma_xuat, 3) AS INTEGER) DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                int num = Integer.parseInt(lastId.substring(2)) + 1;
                return "XH" + String.format("%03d", num);
            } else {
                return "XH001"; // Nếu là đơn đầu tiên
            }
        }
    }

    // --- Các lớp nội bộ để tùy chỉnh JTable ---

    // Lớp để render nút trong bảng
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Lớp để xử lý sự kiện click nút trong bảng
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox, String label) {
            super(checkBox);
            this.label = label;
            button = new JButton(label);
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                String maSP = cart.getItems().stream().skip(cartTable.getSelectedRow()).findFirst().get().getMaSP();
                cart.xoaSanPham(maSP);
                loadCartData(); // Tải lại bảng sau khi xóa
            }
            isPushed = false;
            return label;
        }
    }
    
    // Lớp để dùng JSpinner chỉnh sửa số lượng
    class SpinnerEditor extends DefaultCellEditor {
        JSpinner spinner;
        public SpinnerEditor() {
            super(new JTextField());
            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(value);
            return spinner;
        }

        public Object getCellEditorValue() {
            int newQuantity = (Integer) spinner.getValue();
            String maSP = cart.getItems().stream().skip(cartTable.getSelectedRow()).findFirst().get().getMaSP();
            cart.capNhatSoLuong(maSP, newQuantity);
            // Cập nhật lại dòng hiện tại thay vì load lại cả bảng
            SwingUtilities.invokeLater(() -> {
                tableModel.setValueAt(cart.getItems().stream().filter(i -> i.getMaSP().equals(maSP)).findFirst().get().getThanhTien(), cartTable.getSelectedRow(), 3);
                updateTotalAmount();
            });
            return newQuantity;
        }
    }
}