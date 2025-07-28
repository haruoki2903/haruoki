package donxuathang;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import mainui.MainUI; // <<<< THÊM IMPORT

public class TaoDonXuatDialog extends JDialog {
    // --- Các hằng số Style ---
    private static final Font FONT_BASE = new Font("Cambria", Font.PLAIN, 16);
    private static final Font FONT_BOLD = new Font("Cambria", Font.BOLD, 16);
    private static final Color COLOR_BACKGROUND = new Color(245, 248, 250);

    private JComboBox<String> cbKhachHang;
    private JButton btnTao, btnHuy, btnThemKhachHang;
    private boolean succeeded = false;
    private String maXuatMoi;
    private Connection conn;
    private Map<String, String> khachHangMap = new LinkedHashMap<>();

    // <<<< THAY ĐỔI 1: Thêm biến lưu tham chiếu MainUI >>>>
    private MainUI mainUiRef;

    /**
     * <<<< THAY ĐỔI 2: Sửa constructor để nhận tham chiếu MainUI >>>>
     */
    public TaoDonXuatDialog(Frame parent, Connection conn, MainUI mainUi) {
        super(parent, "Tạo Đơn Xuất Mới", true);
        this.conn = conn;
        this.mainUiRef = mainUi; // Lưu tham chiếu

        setSize(550, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_BACKGROUND);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(COLOR_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lbKhachHang = new JLabel("Chọn khách hàng:");
        lbKhachHang.setFont(FONT_BOLD);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(lbKhachHang, gbc);

        JPanel comboPanel = new JPanel(new BorderLayout(10, 0));
        comboPanel.setBackground(COLOR_BACKGROUND);
        
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(FONT_BASE);
        comboPanel.add(cbKhachHang, BorderLayout.CENTER);
        
        btnThemKhachHang = createStyledButton("+");
        comboPanel.add(btnThemKhachHang, BorderLayout.EAST);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(comboPanel, gbc);
        
        loadKhachHang();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_BACKGROUND);
        btnTao = createStyledButton("Tạo & Thêm Sản Phẩm");
        btnHuy = createStyledButton("Hủy");
        buttonPanel.add(btnTao);
        buttonPanel.add(btnHuy);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(20, 8, 8, 8);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // --- Sự kiện ---
        btnThemKhachHang.addActionListener(e -> {
            // <<<< THAY ĐỔI 3: Truyền tham chiếu vào dialog con >>>>
            ThemKhachHangDialog dialog = new ThemKhachHangDialog(this, conn, mainUiRef);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                loadKhachHang();
                // Tùy chọn: Chọn ngay khách hàng vừa tạo
                if(dialog.getTenKhachMoi() != null) {
                    cbKhachHang.setSelectedItem(dialog.getTenKhachMoi());
                }
            }
        });
        btnTao.addActionListener(e -> taoDonXuat());
        btnHuy.addActionListener(e -> dispose());
    }

    private void loadKhachHang() {
        cbKhachHang.removeAllItems();
        khachHangMap.clear();
        String sql = "SELECT ma_khach, ten FROM khachhang ORDER BY ten";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ten = rs.getString("ten");
                khachHangMap.put(ten, rs.getString("ma_khach"));
                cbKhachHang.addItem(ten);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taoDonXuat() {
        String selectedTenKhach = (String) cbKhachHang.getSelectedItem();
        if (selectedTenKhach == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String sqlGetLastId = "SELECT ma_xuat FROM donxuat ORDER BY CAST(SUBSTRING(ma_xuat, 3) AS INTEGER) DESC LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlGetLastId);
            int soThuTu = 1;
            if (rs.next()) {
                String lastId = rs.getString("ma_xuat");
                if (lastId != null && lastId.matches("XH\\d+")) {
                    try { soThuTu = Integer.parseInt(lastId.substring(2)) + 1; }
                    catch (NumberFormatException ex) { soThuTu = 1; }
                }
            }
            this.maXuatMoi = String.format("XH%03d", soThuTu);
            
            String maKhach = khachHangMap.get(selectedTenKhach);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO donxuat (ma_xuat, ma_khach, ngay_xuat, tong_tien) VALUES (?, ?, ?, 0)");
            ps.setString(1, this.maXuatMoi);
            ps.setString(2, maKhach);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();

            // <<<< THAY ĐỔI 4: Gọi refresh KPI ngay sau khi tạo đơn mới >>>>
            if(mainUiRef != null) {
                mainUiRef.refreshKpis();
            }
            
            succeeded = true;
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi SQL khi tạo đơn xuất: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.setFocusPainted(false);
        button.setOpaque(true);
        
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1), 
            BorderFactory.createEmptyBorder(8, 15, 8, 15)));
            
        button.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(240, 240, 240)); }
            @Override 
            public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    public boolean isSucceeded() { return succeeded; }
    public String getMaXuatMoi() { return maXuatMoi; }
}